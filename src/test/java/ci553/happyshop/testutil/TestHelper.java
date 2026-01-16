package ci553.happyshop.testutil;

import ci553.happyshop.catalogue.Product;

import java.util.ArrayList;

/**
 * Test helper utility class for creating test data and common test operations.
 * 
 * This class provides static factory methods for creating test products,
 * test data sets, and other common test utilities.
 */
public class TestHelper {
    
    /**
     * Creates a test product with default values.
     * 
     * @param productId the product ID
     * @return a Product instance with default test values
     */
    public static Product createTestProduct(String productId) {
        return createTestProduct(productId, "Test Product " + productId, productId + ".jpg", 100.0, 50);
    }
    
    /**
     * Creates a test product with specified values.
     * 
     * @param productId the product ID
     * @param description the product description
     * @param imageName the image file name
     * @param price the unit price
     * @param stock the stock quantity
     * @return a Product instance
     */
    public static Product createTestProduct(String productId, String description, 
                                           String imageName, double price, int stock) {
        Product product = new Product(productId, description, imageName, price, stock);
        product.setOrderedQuantity(1);
        return product;
    }
    
    /**
     * Creates a list of test products.
     * 
     * @param count the number of products to create
     * @return a list of test products with IDs "0001", "0002", etc.
     */
    public static ArrayList<Product> createTestProducts(int count) {
        ArrayList<Product> products = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String productId = String.format("%04d", i);
            products.add(createTestProduct(productId));
        }
        return products;
    }
    
    /**
     * Creates a product with specified ordered quantity.
     * 
     * @param productId the product ID
     * @param orderedQuantity the ordered quantity
     * @return a Product instance with the specified ordered quantity
     */
    public static Product createTestProductWithQuantity(String productId, int orderedQuantity) {
        Product product = createTestProduct(productId);
        product.setOrderedQuantity(orderedQuantity);
        return product;
    }
    
    /**
     * Creates a product with low stock (below threshold).
     * 
     * @param productId the product ID
     * @param stock the stock quantity (should be <= 10 for low stock)
     * @return a Product instance with low stock
     */
    public static Product createLowStockProduct(String productId, int stock) {
        return createTestProduct(productId, "Low Stock Product", productId + ".jpg", 50.0, stock);
    }
    
    /**
     * Creates a product with no stock.
     * 
     * @param productId the product ID
     * @return a Product instance with 0 stock
     */
    public static Product createOutOfStockProduct(String productId) {
        return createTestProduct(productId, "Out of Stock Product", productId + ".jpg", 50.0, 0);
    }
}



