package ci553.happyshop.service;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.storageAccess.DatabaseRWFactory;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Service class for monitoring stock levels and generating low stock alerts.
 * 
 * Responsibilities:
 * - Check products for low stock conditions
 * - Generate alert messages for low stock products
 * - Provide threshold-based stock monitoring
 * 
 * This service follows the Single Responsibility Principle by
 * separating stock alert logic from other business operations.
 */
public class StockAlertService {
    
    /**
     * Default low stock threshold - products with stock below this will trigger alerts.
     * This can be configured per product or globally.
     */
    public static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    /**
     * Checks if a product has low stock based on the default threshold.
     * 
     * @param product The product to check
     * @return true if stock is below threshold, false otherwise
     */
    public static boolean isLowStock(Product product) {
        return isLowStock(product, DEFAULT_LOW_STOCK_THRESHOLD);
    }

    /**
     * Checks if a product has low stock based on a custom threshold.
     * 
     * @param product The product to check
     * @param threshold The stock threshold below which an alert should be triggered
     * @return true if stock is below threshold, false otherwise
     */
    public static boolean isLowStock(Product product, int threshold) {
        if (product == null) {
            return false;
        }
        return product.getStockQuantity() > 0 && product.getStockQuantity() <= threshold;
    }

    /**
     * Checks if a product is out of stock.
     * 
     * @param product The product to check
     * @return true if stock is 0 or less, false otherwise
     */
    public static boolean isOutOfStock(Product product) {
        if (product == null) {
            return false;
        }
        return product.getStockQuantity() <= 0;
    }

    /**
     * Scans all products in the database and returns a list of products with low stock.
     * 
     * @return List of products with stock below the threshold
     * @throws SQLException if there's an error accessing the database
     */
    public static ArrayList<Product> scanForLowStockProducts() throws SQLException {
        return scanForLowStockProducts(DEFAULT_LOW_STOCK_THRESHOLD);
    }

    /**
     * Scans all products in the database and returns a list of products with low stock.
     * 
     * @param threshold The stock threshold to use
     * @return List of products with stock below the threshold
     * @throws SQLException if there's an error accessing the database
     */
    public static ArrayList<Product> scanForLowStockProducts(int threshold) throws SQLException {
        ArrayList<Product> lowStockProducts = new ArrayList<>();
        DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();
        
        // Get all products from the database
        ArrayList<Product> allProducts = databaseRW.getAllProducts();
        
        // Check each product for low stock
        for (Product product : allProducts) {
            if (isLowStock(product, threshold) || isOutOfStock(product)) {
                lowStockProducts.add(product);
            }
        }
        
        return lowStockProducts;
    }

    /**
     * Checks a specific product after stock update and generates an alert message if needed.
     * 
     * @param productId The product ID to check
     * @return Alert message if stock is low, null otherwise
     * @throws SQLException if there's an error accessing the database
     */
    public static String checkProductAndGenerateAlert(String productId) throws SQLException {
        DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();
        Product product = databaseRW.searchByProductId(productId);
        
        if (product == null) {
            return null;
        }
        
        if (isOutOfStock(product)) {
            return String.format("⚠️ OUT OF STOCK: Product %s (%s) has 0 units remaining!",
                product.getProductId(), product.getProductDescription());
        } else if (isLowStock(product)) {
            return String.format("⚠️ LOW STOCK: Product %s (%s) has only %d units remaining (threshold: %d)",
                product.getProductId(), product.getProductDescription(), 
                product.getStockQuantity(), DEFAULT_LOW_STOCK_THRESHOLD);
        }
        
        return null;
    }

    /**
     * Generates a formatted alert message for a low stock product.
     * 
     * @param product The product with low stock
     * @return Formatted alert message
     */
    public static String generateAlertMessage(Product product) {
        if (product == null) {
            return null;
        }
        
        if (isOutOfStock(product)) {
            return String.format("⚠️ OUT OF STOCK\nProduct ID: %s\nDescription: %s\nCurrent Stock: 0\n\nAction Required: Restock immediately!",
                product.getProductId(), product.getProductDescription());
        } else if (isLowStock(product)) {
            return String.format("⚠️ LOW STOCK ALERT\nProduct ID: %s\nDescription: %s\nCurrent Stock: %d\nThreshold: %d\n\nConsider restocking soon.",
                product.getProductId(), product.getProductDescription(), 
                product.getStockQuantity(), DEFAULT_LOW_STOCK_THRESHOLD);
        }
        
        return null;
    }

    /**
     * Checks multiple products and generates a combined alert message.
     * 
     * @param products List of products to check
     * @return Combined alert message for all low stock products, or null if none
     */
    public static String generateCombinedAlertMessage(ArrayList<Product> products) {
        if (products == null || products.isEmpty()) {
            return null;
        }
        
        ArrayList<Product> lowStockProducts = new ArrayList<>();
        ArrayList<Product> outOfStockProducts = new ArrayList<>();
        
        for (Product product : products) {
            if (isOutOfStock(product)) {
                outOfStockProducts.add(product);
            } else if (isLowStock(product)) {
                lowStockProducts.add(product);
            }
        }
        
        if (lowStockProducts.isEmpty() && outOfStockProducts.isEmpty()) {
            return null;
        }
        
        StringBuilder alert = new StringBuilder();
        alert.append("📦 STOCK ALERT SUMMARY\n");
        alert.append("=".repeat(40)).append("\n\n");
        
        if (!outOfStockProducts.isEmpty()) {
            alert.append("🚨 OUT OF STOCK (").append(outOfStockProducts.size()).append("):\n");
            for (Product product : outOfStockProducts) {
                alert.append(String.format("  • %s - %s (0 units)\n", 
                    product.getProductId(), product.getProductDescription()));
            }
            alert.append("\n");
        }
        
        if (!lowStockProducts.isEmpty()) {
            alert.append("⚠️ LOW STOCK (").append(lowStockProducts.size()).append("):\n");
            for (Product product : lowStockProducts) {
                alert.append(String.format("  • %s - %s (%d units)\n", 
                    product.getProductId(), product.getProductDescription(), 
                    product.getStockQuantity()));
            }
        }
        
        return alert.toString();
    }
}

