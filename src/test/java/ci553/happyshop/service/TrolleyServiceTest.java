package ci553.happyshop.service;

import ci553.happyshop.catalogue.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrolleyService class.
 * 
 * These tests verify:
 * - Merging duplicate products by combining quantities
 * - Sorting products by product ID
 * - Handling edge cases (empty list, null, single product)
 * 
 * Why these tests are important:
 * - Ensures trolley organization works correctly
 * - Prevents duplicate product entries
 * - Validates quantity aggregation logic
 * - Confirms sorting maintains order consistency
 */
@DisplayName("TrolleyService Tests")
class TrolleyServiceTest {

    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUp() {
        // Create test products with different IDs
        product1 = new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100);
        product1.setOrderedQuantity(1);
        
        product2 = new Product("0002", "DAB Radio", "0002.jpg", 29.99, 50);
        product2.setOrderedQuantity(1);
        
        product3 = new Product("0003", "Toaster", "0003.jpg", 19.99, 30);
        product3.setOrderedQuantity(1);
    }

    @Test
    @DisplayName("Should merge duplicate products and combine quantities")
    void testMergeDuplicateProducts() {
        // Arrange: Create trolley with duplicate product IDs
        ArrayList<Product> trolley = new ArrayList<>();
        Product duplicate1 = new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100);
        duplicate1.setOrderedQuantity(1);
        Product duplicate2 = new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100);
        duplicate2.setOrderedQuantity(2);
        Product duplicate3 = new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100);
        duplicate3.setOrderedQuantity(1);
        
        trolley.add(duplicate1);
        trolley.add(duplicate2);
        trolley.add(duplicate3);

        // Act: Merge and sort
        ArrayList<Product> result = TrolleyService.mergeAndSort(trolley);

        // Assert: Should have only one product with combined quantity
        assertEquals(1, result.size(), "Should merge duplicates into single product");
        assertEquals("0001", result.get(0).getProductId());
        assertEquals(4, result.get(0).getOrderedQuantity(), 
            "Quantity should be sum of all duplicates (1 + 2 + 1 = 4)");
    }

    @Test
    @DisplayName("Should sort products by product ID in ascending order")
    void testSortByProductId() {
        // Arrange: Create trolley with products in random order
        ArrayList<Product> trolley = new ArrayList<>();
        trolley.add(product3); // 0003
        trolley.add(product1); // 0001
        trolley.add(product2); // 0002

        // Act: Merge and sort
        ArrayList<Product> result = TrolleyService.mergeAndSort(trolley);

        // Assert: Products should be sorted by ID
        assertEquals(3, result.size());
        assertEquals("0001", result.get(0).getProductId(), "First product should be 0001");
        assertEquals("0002", result.get(1).getProductId(), "Second product should be 0002");
        assertEquals("0003", result.get(2).getProductId(), "Third product should be 0003");
    }

    @Test
    @DisplayName("Should merge duplicates and sort correctly together")
    void testMergeAndSortTogether() {
        // Arrange: Mix of duplicates and different products in random order
        ArrayList<Product> trolley = new ArrayList<>();
        
        // Add product 0002 (quantity 1)
        Product p2a = new Product("0002", "DAB Radio", "0002.jpg", 29.99, 50);
        p2a.setOrderedQuantity(1);
        trolley.add(p2a);
        
        // Add product 0001 (quantity 2)
        Product p1a = new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100);
        p1a.setOrderedQuantity(2);
        trolley.add(p1a);
        
        // Add duplicate of 0002 (quantity 3)
        Product p2b = new Product("0002", "DAB Radio", "0002.jpg", 29.99, 50);
        p2b.setOrderedQuantity(3);
        trolley.add(p2b);
        
        // Add duplicate of 0001 (quantity 1)
        Product p1b = new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100);
        p1b.setOrderedQuantity(1);
        trolley.add(p1b);

        // Act: Merge and sort
        ArrayList<Product> result = TrolleyService.mergeAndSort(trolley);

        // Assert: Should have 2 products, sorted by ID, with merged quantities
        assertEquals(2, result.size(), "Should merge to 2 unique products");
        assertEquals("0001", result.get(0).getProductId());
        assertEquals(3, result.get(0).getOrderedQuantity(), "0001 quantity should be 2+1=3");
        assertEquals("0002", result.get(1).getProductId());
        assertEquals(4, result.get(1).getOrderedQuantity(), "0002 quantity should be 1+3=4");
    }

    @Test
    @DisplayName("Should handle empty trolley")
    void testEmptyTrolley() {
        // Arrange
        ArrayList<Product> emptyTrolley = new ArrayList<>();

        // Act
        ArrayList<Product> result = TrolleyService.mergeAndSort(emptyTrolley);

        // Assert
        assertNotNull(result, "Should return empty list, not null");
        assertTrue(result.isEmpty(), "Should return empty list");
    }

    @Test
    @DisplayName("Should handle null trolley")
    void testNullTrolley() {
        // Act
        ArrayList<Product> result = TrolleyService.mergeAndSort(null);

        // Assert
        assertNotNull(result, "Should return empty list, not null");
        assertTrue(result.isEmpty(), "Should return empty list for null input");
    }

    @Test
    @DisplayName("Should handle single product")
    void testSingleProduct() {
        // Arrange
        ArrayList<Product> trolley = new ArrayList<>();
        trolley.add(product1);

        // Act
        ArrayList<Product> result = TrolleyService.mergeAndSort(trolley);

        // Assert
        assertEquals(1, result.size());
        assertEquals("0001", result.get(0).getProductId());
        assertEquals(1, result.get(0).getOrderedQuantity());
    }

    @Test
    @DisplayName("Should not modify original trolley list")
    void testOriginalListNotModified() {
        // Arrange
        ArrayList<Product> originalTrolley = new ArrayList<>();
        originalTrolley.add(product1);
        originalTrolley.add(product2);
        int originalSize = originalTrolley.size();

        // Act
        ArrayList<Product> result = TrolleyService.mergeAndSort(originalTrolley);

        // Assert: Original list should be unchanged
        assertEquals(originalSize, originalTrolley.size(), 
            "Original list size should not change");
        assertNotSame(originalTrolley, result, 
            "Should return a new list, not modify original");
    }

    @Test
    @DisplayName("Should preserve product properties when merging")
    void testPreserveProductProperties() {
        // Arrange: Add same product multiple times
        ArrayList<Product> trolley = new ArrayList<>();
        Product p1 = new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100);
        p1.setOrderedQuantity(1);
        Product p2 = new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100);
        p2.setOrderedQuantity(2);
        trolley.add(p1);
        trolley.add(p2);

        // Act
        ArrayList<Product> result = TrolleyService.mergeAndSort(trolley);

        // Assert: Properties should be preserved (except quantity which is merged)
        assertEquals(1, result.size());
        Product merged = result.get(0);
        assertEquals("0001", merged.getProductId());
        assertEquals("40 inch TV", merged.getProductDescription());
        assertEquals("0001.jpg", merged.getProductImageName());
        assertEquals(269.00, merged.getUnitPrice(), 0.01);
        assertEquals(100, merged.getStockQuantity());
        assertEquals(3, merged.getOrderedQuantity(), "Quantity should be merged");
    }

    @Test
    @DisplayName("Should handle products with zero quantity")
    void testZeroQuantity() {
        // Arrange
        ArrayList<Product> trolley = new ArrayList<>();
        Product p1 = new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100);
        p1.setOrderedQuantity(0);
        Product p2 = new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100);
        p2.setOrderedQuantity(2);
        trolley.add(p1);
        trolley.add(p2);

        // Act
        ArrayList<Product> result = TrolleyService.mergeAndSort(trolley);

        // Assert
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getOrderedQuantity(), 
            "Should combine 0 + 2 = 2");
    }
}



