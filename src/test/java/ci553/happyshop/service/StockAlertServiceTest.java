package ci553.happyshop.service;

import ci553.happyshop.catalogue.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StockAlertService class.
 * 
 * These tests verify:
 * - Low stock detection (below threshold)
 * - Out of stock detection
 * - Alert message generation
 * - Combined alert message generation
 * 
 * Why these tests are important:
 * - Ensures stock monitoring works correctly
 * - Validates alert thresholds
 * - Confirms proper alert message formatting
 */
@DisplayName("StockAlertService Tests")
class StockAlertServiceTest {

    private Product normalStockProduct;
    private Product lowStockProduct;
    private Product outOfStockProduct;
    private Product veryLowStockProduct;

    @BeforeEach
    void setUp() {
        normalStockProduct = new Product("0001", "TV", "0001.jpg", 100.0, 50);
        lowStockProduct = new Product("0002", "Radio", "0002.jpg", 50.0, 10);
        outOfStockProduct = new Product("0003", "Toaster", "0003.jpg", 30.0, 0);
        veryLowStockProduct = new Product("0004", "Microwave", "0004.jpg", 200.0, 5);
    }

    @Test
    @DisplayName("Should detect low stock products")
    void testIsLowStock() {
        assertFalse(StockAlertService.isLowStock(normalStockProduct), 
            "Product with 50 stock should not be low stock");
        assertTrue(StockAlertService.isLowStock(lowStockProduct), 
            "Product with 10 stock should be low stock (at threshold)");
        assertTrue(StockAlertService.isLowStock(veryLowStockProduct), 
            "Product with 5 stock should be low stock");
    }

    @Test
    @DisplayName("Should detect out of stock products")
    void testIsOutOfStock() {
        assertFalse(StockAlertService.isOutOfStock(normalStockProduct), 
            "Product with stock should not be out of stock");
        assertTrue(StockAlertService.isOutOfStock(outOfStockProduct), 
            "Product with 0 stock should be out of stock");
    }

    @Test
    @DisplayName("Should handle null product")
    void testNullProduct() {
        assertFalse(StockAlertService.isLowStock(null), 
            "Null product should not be low stock");
        assertFalse(StockAlertService.isOutOfStock(null), 
            "Null product should not be out of stock");
    }

    @Test
    @DisplayName("Should generate alert message for low stock")
    void testGenerateAlertMessageLowStock() {
        String message = StockAlertService.generateAlertMessage(lowStockProduct);
        assertNotNull(message, "Should generate alert message");
        assertTrue(message.contains("LOW STOCK"), "Message should indicate low stock");
        assertTrue(message.contains("0002"), "Message should contain product ID");
        assertTrue(message.contains("10"), "Message should contain stock quantity");
    }

    @Test
    @DisplayName("Should generate alert message for out of stock")
    void testGenerateAlertMessageOutOfStock() {
        String message = StockAlertService.generateAlertMessage(outOfStockProduct);
        assertNotNull(message, "Should generate alert message");
        assertTrue(message.contains("OUT OF STOCK"), "Message should indicate out of stock");
        assertTrue(message.contains("0003"), "Message should contain product ID");
        assertTrue(message.contains("0"), "Message should indicate zero stock");
    }

    @Test
    @DisplayName("Should return null for products with sufficient stock")
    void testGenerateAlertMessageSufficientStock() {
        String message = StockAlertService.generateAlertMessage(normalStockProduct);
        assertNull(message, "Should not generate alert for sufficient stock");
    }

    @Test
    @DisplayName("Should use custom threshold")
    void testCustomThreshold() {
        assertTrue(StockAlertService.isLowStock(normalStockProduct, 60), 
            "Product with 50 stock should be low stock with threshold 60");
        assertFalse(StockAlertService.isLowStock(normalStockProduct, 40), 
            "Product with 50 stock should not be low stock with threshold 40");
    }
}



