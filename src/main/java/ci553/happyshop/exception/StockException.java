package ci553.happyshop.exception;

import ci553.happyshop.catalogue.Product;
import java.util.ArrayList;

/**
 * Exception thrown when stock-related operations fail.
 * 
 * This exception is used for errors related to insufficient stock,
 * stock updates, and stock restoration.
 */
public class StockException extends HappyShopException {
    
    private final ArrayList<Product> insufficientProducts;
    
    /**
     * Constructs a new StockException with the specified detail message.
     * 
     * @param message the detail message
     */
    public StockException(String message) {
        super(message);
        this.insufficientProducts = new ArrayList<>();
    }
    
    /**
     * Constructs a new StockException with the specified detail message and list of insufficient products.
     * 
     * @param message the detail message
     * @param insufficientProducts the list of products with insufficient stock
     */
    public StockException(String message, ArrayList<Product> insufficientProducts) {
        super(message);
        this.insufficientProducts = insufficientProducts != null ? insufficientProducts : new ArrayList<>();
    }
    
    /**
     * Constructs a new StockException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public StockException(String message, Throwable cause) {
        super(message, cause);
        this.insufficientProducts = new ArrayList<>();
    }
    
    /**
     * Gets the list of products with insufficient stock.
     * 
     * @return the list of products with insufficient stock
     */
    public ArrayList<Product> getInsufficientProducts() {
        return new ArrayList<>(insufficientProducts); // Return a copy for immutability
    }
    
    /**
     * Checks if there are any insufficient products.
     * 
     * @return true if there are insufficient products, false otherwise
     */
    public boolean hasInsufficientProducts() {
        return !insufficientProducts.isEmpty();
    }
}



