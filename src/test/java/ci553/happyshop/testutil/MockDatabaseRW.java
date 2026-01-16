package ci553.happyshop.testutil;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of DatabaseRW for testing purposes.
 * 
 * This mock allows tests to control database behavior without requiring
 * an actual database connection. It stores products in memory and provides
 * configurable responses for testing different scenarios.
 */
public class MockDatabaseRW implements DatabaseRW {
    
    private final Map<String, Product> products = new HashMap<>();
    private final ArrayList<Product> searchResults = new ArrayList<>();
    private boolean throwException = false;
    private SQLException exceptionToThrow = null;
    
    /**
     * Adds a product to the mock database.
     * 
     * @param product the product to add
     */
    public void addProduct(Product product) {
        products.put(product.getProductId(), product);
    }
    
    /**
     * Sets the search results that will be returned by searchProduct().
     * 
     * @param results the list of products to return
     */
    public void setSearchResults(ArrayList<Product> results) {
        searchResults.clear();
        searchResults.addAll(results);
    }
    
    /**
     * Sets a single product as the search result.
     * 
     * @param product the product to return
     */
    public void setSearchResult(Product product) {
        searchResults.clear();
        if (product != null) {
            searchResults.add(product);
            addProduct(product);
        }
    }
    
    /**
     * Configures the mock to throw an exception on the next operation.
     * 
     * @param exception the exception to throw
     */
    public void setExceptionToThrow(SQLException exception) {
        this.throwException = true;
        this.exceptionToThrow = exception;
    }
    
    /**
     * Clears all products and search results.
     */
    public void clear() {
        products.clear();
        searchResults.clear();
        throwException = false;
        exceptionToThrow = null;
    }
    
    @Override
    public ArrayList<Product> searchProduct(String keyword) throws SQLException {
        if (throwException) {
            throw exceptionToThrow != null ? exceptionToThrow : new SQLException("Mock exception");
        }
        
        // If keyword matches a product ID, return that product
        if (products.containsKey(keyword)) {
            ArrayList<Product> result = new ArrayList<>();
            result.add(products.get(keyword));
            return result;
        }
        
        // Otherwise return configured search results
        return new ArrayList<>(searchResults);
    }
    
    @Override
    public Product searchByProductId(String productId) throws SQLException {
        if (throwException) {
            throw exceptionToThrow != null ? exceptionToThrow : new SQLException("Mock exception");
        }
        return products.get(productId);
    }
    
    @Override
    public ArrayList<Product> purchaseStocks(ArrayList<Product> proList) throws SQLException {
        if (throwException) {
            throw exceptionToThrow != null ? exceptionToThrow : new SQLException("Mock exception");
        }
        
        // Simulate stock checking: return products with insufficient stock
        ArrayList<Product> insufficientProducts = new ArrayList<>();
        for (Product product : proList) {
            Product dbProduct = products.get(product.getProductId());
            if (dbProduct != null && dbProduct.getStockQuantity() < product.getOrderedQuantity()) {
                insufficientProducts.add(product);
            } else if (dbProduct != null) {
                // Update stock in mock database by creating a new product with reduced stock
                int newStock = dbProduct.getStockQuantity() - product.getOrderedQuantity();
                Product updatedProduct = new Product(
                    dbProduct.getProductId(),
                    dbProduct.getProductDescription(),
                    dbProduct.getProductImageName(),
                    dbProduct.getUnitPrice(),
                    newStock
                );
                updatedProduct.setOrderedQuantity(dbProduct.getOrderedQuantity());
                products.put(product.getProductId(), updatedProduct);
            }
        }
        return insufficientProducts;
    }
    
    @Override
    public void updateProduct(String id, String des, double price, String imageName, int stock) throws SQLException {
        if (throwException) {
            throw exceptionToThrow != null ? exceptionToThrow : new SQLException("Mock exception");
        }
        Product product = products.get(id);
        if (product != null) {
            // Update product properties
            // Note: Product class may not have setters for all fields
        }
    }
    
    @Override
    public void deleteProduct(String id) throws SQLException {
        if (throwException) {
            throw exceptionToThrow != null ? exceptionToThrow : new SQLException("Mock exception");
        }
        products.remove(id);
    }
    
    @Override
    public boolean isProIdAvailable(String productId) throws SQLException {
        if (throwException) {
            throw exceptionToThrow != null ? exceptionToThrow : new SQLException("Mock exception");
        }
        return !products.containsKey(productId);
    }
    
    @Override
    public void insertNewProduct(String id, String des, double price, String image, int stock) throws SQLException {
        if (throwException) {
            throw exceptionToThrow != null ? exceptionToThrow : new SQLException("Mock exception");
        }
        Product product = new Product(id, des, image, price, stock);
        products.put(id, product);
    }
    
    @Override
    public void restoreStock(ArrayList<Product> proList) throws SQLException {
        if (throwException) {
            throw exceptionToThrow != null ? exceptionToThrow : new SQLException("Mock exception");
        }
        for (Product product : proList) {
            Product dbProduct = products.get(product.getProductId());
            if (dbProduct != null) {
                // Create new product with restored stock
                int newStock = dbProduct.getStockQuantity() + product.getOrderedQuantity();
                Product updatedProduct = new Product(
                    dbProduct.getProductId(),
                    dbProduct.getProductDescription(),
                    dbProduct.getProductImageName(),
                    dbProduct.getUnitPrice(),
                    newStock
                );
                updatedProduct.setOrderedQuantity(dbProduct.getOrderedQuantity());
                products.put(product.getProductId(), updatedProduct);
            }
        }
    }
    
    @Override
    public ArrayList<Product> getAllProducts() throws SQLException {
        if (throwException) {
            throw exceptionToThrow != null ? exceptionToThrow : new SQLException("Mock exception");
        }
        return new ArrayList<>(products.values());
    }
}

