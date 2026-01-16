package ci553.happyshop.service;

import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.testutil.MockDatabaseRW;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationService class.
 * 
 * These tests verify:
 * - Product ID format validation
 * - Product ID availability checking
 * - Price validation (format, range, decimal places)
 * - Stock quantity validation
 * - Description validation
 * - Image path validation
 * - Combined validation for edit and new product operations
 * 
 * Why these tests are important:
 * - Ensures input validation works correctly
 * - Prevents invalid data from entering the system
 * - Validates business rules are enforced
 * - Confirms proper error message generation
 */
@DisplayName("ValidationService Tests")
class ValidationServiceTest {

    private MockDatabaseRW mockDatabaseRW;

    @BeforeEach
    void setUp() {
        mockDatabaseRW = new MockDatabaseRW();
    }

    @Test
    @DisplayName("Should validate correct product ID format")
    void testValidProductIdFormat() {
        assertTrue(ValidationService.isValidProductIdFormat("0001"));
        assertTrue(ValidationService.isValidProductIdFormat("1234"));
        assertTrue(ValidationService.isValidProductIdFormat("9999"));
    }

    @Test
    @DisplayName("Should reject invalid product ID formats")
    void testInvalidProductIdFormat() {
        assertFalse(ValidationService.isValidProductIdFormat("001")); // Too short
        assertFalse(ValidationService.isValidProductIdFormat("00001")); // Too long
        assertFalse(ValidationService.isValidProductIdFormat("abcd")); // Not digits
        assertFalse(ValidationService.isValidProductIdFormat("00-1")); // Contains dash
        assertFalse(ValidationService.isValidProductIdFormat(null)); // Null
        assertFalse(ValidationService.isValidProductIdFormat("")); // Empty
    }

    @Test
    @DisplayName("Should validate correct price format")
    void testValidPrice() {
        ValidationService.ValidationResult result1 = ValidationService.validatePrice("100.00");
        assertTrue(result1.isValid(), "Valid price should pass");

        ValidationService.ValidationResult result2 = ValidationService.validatePrice("50.5");
        assertTrue(result2.isValid(), "Price with one decimal should pass");

        ValidationService.ValidationResult result3 = ValidationService.validatePrice("100");
        assertTrue(result3.isValid(), "Integer price should pass");
    }

    @Test
    @DisplayName("Should reject invalid price formats")
    void testInvalidPrice() {
        ValidationService.ValidationResult result1 = ValidationService.validatePrice("100.123");
        assertFalse(result1.isValid(), "Price with more than 2 decimals should fail");
        assertTrue(result1.getErrorMessage().contains("decimal places"));

        ValidationService.ValidationResult result2 = ValidationService.validatePrice("0");
        assertFalse(result2.isValid(), "Zero price should fail");
        assertTrue(result2.getErrorMessage().contains("positive"));

        ValidationService.ValidationResult result3 = ValidationService.validatePrice("-10");
        assertFalse(result3.isValid(), "Negative price should fail");

        ValidationService.ValidationResult result4 = ValidationService.validatePrice("abc");
        assertFalse(result4.isValid(), "Non-numeric price should fail");
    }

    @Test
    @DisplayName("Should validate correct stock quantity")
    void testValidStock() {
        ValidationService.ValidationResult result1 = ValidationService.validateStock("100");
        assertTrue(result1.isValid(), "Valid stock should pass");

        ValidationService.ValidationResult result2 = ValidationService.validateStock("0");
        assertTrue(result2.isValid(), "Zero stock should pass");
    }

    @Test
    @DisplayName("Should reject invalid stock quantities")
    void testInvalidStock() {
        ValidationService.ValidationResult result1 = ValidationService.validateStock("-1");
        assertFalse(result1.isValid(), "Negative stock should fail");
        assertTrue(result1.getErrorMessage().contains("negative"));

        ValidationService.ValidationResult result2 = ValidationService.validateStock("abc");
        assertFalse(result2.isValid(), "Non-numeric stock should fail");
    }

    @Test
    @DisplayName("Should validate non-empty description")
    void testValidDescription() {
        ValidationService.ValidationResult result = ValidationService.validateDescription("Test Product");
        assertTrue(result.isValid(), "Non-empty description should pass");
    }

    @Test
    @DisplayName("Should reject empty description")
    void testInvalidDescription() {
        ValidationService.ValidationResult result1 = ValidationService.validateDescription("");
        assertFalse(result1.isValid(), "Empty description should fail");

        ValidationService.ValidationResult result2 = ValidationService.validateDescription("   ");
        assertFalse(result2.isValid(), "Whitespace-only description should fail");

        ValidationService.ValidationResult result3 = ValidationService.validateDescription(null);
        assertFalse(result3.isValid(), "Null description should fail");
    }

    @Test
    @DisplayName("Should validate image path")
    void testValidImagePath() {
        ValidationService.ValidationResult result = ValidationService.validateImagePath("/path/to/image.jpg");
        assertTrue(result.isValid(), "Valid image path should pass");
    }

    @Test
    @DisplayName("Should reject null or empty image path")
    void testInvalidImagePath() {
        ValidationService.ValidationResult result1 = ValidationService.validateImagePath(null);
        assertFalse(result1.isValid(), "Null image path should fail");

        ValidationService.ValidationResult result2 = ValidationService.validateImagePath("");
        assertFalse(result2.isValid(), "Empty image path should fail");
    }

    @Test
    @DisplayName("Should validate new product with all valid fields")
    void testValidateNewProductValid() throws SQLException {
        ValidationService.ValidationResult result = ValidationService.validateNewProduct(
            "0001", "100.00", "50", "Test Product", "/path/to/image.jpg", mockDatabaseRW
        );
        assertTrue(result.isValid(), "All valid fields should pass");
    }

    @Test
    @DisplayName("Should reject new product with invalid ID format")
    void testValidateNewProductInvalidId() throws SQLException {
        ValidationService.ValidationResult result = ValidationService.validateNewProduct(
            "001", "100.00", "50", "Test Product", "/path/to/image.jpg", mockDatabaseRW
        );
        assertFalse(result.isValid(), "Invalid ID format should fail");
        assertTrue(result.getErrorMessage().contains("4 digits"));
    }

    @Test
    @DisplayName("Should reject new product with duplicate ID")
    void testValidateNewProductDuplicateId() throws SQLException {
        // Add a product with ID "0001" to mock database
        ci553.happyshop.catalogue.Product existingProduct = 
            new ci553.happyshop.catalogue.Product("0001", "Existing", "0001.jpg", 100.0, 50);
        mockDatabaseRW.addProduct(existingProduct);

        ValidationService.ValidationResult result = ValidationService.validateNewProduct(
            "0001", "100.00", "50", "Test Product", "/path/to/image.jpg", mockDatabaseRW
        );
        assertFalse(result.isValid(), "Duplicate ID should fail");
        assertTrue(result.getErrorMessage().contains("not available"));
    }

    @Test
    @DisplayName("Should validate edit product with all valid fields")
    void testValidateEditProductValid() {
        ValidationService.ValidationResult result = ValidationService.validateEditProduct(
            "100.00", "50", "Test Product", false
        );
        assertTrue(result.isValid(), "All valid fields should pass");
    }

    @Test
    @DisplayName("Should reject edit product with unapplied stock change")
    void testValidateEditProductUnappliedChange() {
        ValidationService.ValidationResult result = ValidationService.validateEditProduct(
            "100.00", "50", "Test Product", true
        );
        assertFalse(result.isValid(), "Unapplied stock change should fail");
        assertTrue(result.getErrorMessage().contains("not applied"));
    }

    @Test
    @DisplayName("Should collect multiple validation errors")
    void testMultipleValidationErrors() throws SQLException {
        ValidationService.ValidationResult result = ValidationService.validateNewProduct(
            "001", // Invalid format
            "100.123", // Too many decimals
            "-5", // Negative stock
            "", // Empty description
            null, // No image
            mockDatabaseRW
        );
        assertFalse(result.isValid(), "Should fail with multiple errors");
        assertTrue(result.getErrorMessage().contains("4 digits"));
        assertTrue(result.getErrorMessage().contains("decimal places"));
        assertTrue(result.getErrorMessage().contains("negative"));
        assertTrue(result.getErrorMessage().contains("empty"));
        assertTrue(result.getErrorMessage().contains("image"));
    }
}



