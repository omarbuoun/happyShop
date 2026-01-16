package ci553.happyshop.exception;

/**
 * Base exception class for all HappyShop application exceptions.
 * 
 * This provides a common base for all custom exceptions in the system,
 * allowing for consistent error handling and messaging.
 * 
 * All custom exceptions in HappyShop should extend this class.
 */
public class HappyShopException extends Exception {
    
    /**
     * Constructs a new HappyShopException with the specified detail message.
     * 
     * @param message the detail message
     */
    public HappyShopException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new HappyShopException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public HappyShopException(String message, Throwable cause) {
        super(message, cause);
    }
}



