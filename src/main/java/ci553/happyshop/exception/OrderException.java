package ci553.happyshop.exception;

/**
 * Exception thrown when order-related operations fail.
 * 
 * This exception is used for errors related to order creation, cancellation,
 * state changes, and order file operations.
 */
public class OrderException extends HappyShopException {
    
    private final int orderId;
    
    /**
     * Constructs a new OrderException with the specified detail message.
     * 
     * @param message the detail message
     */
    public OrderException(String message) {
        super(message);
        this.orderId = 0;
    }
    
    /**
     * Constructs a new OrderException with the specified detail message and order ID.
     * 
     * @param message the detail message
     * @param orderId the order ID related to this exception
     */
    public OrderException(String message, int orderId) {
        super(message + " (Order ID: " + orderId + ")");
        this.orderId = orderId;
    }
    
    /**
     * Constructs a new OrderException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public OrderException(String message, Throwable cause) {
        super(message, cause);
        this.orderId = 0;
    }
    
    /**
     * Constructs a new OrderException with the specified detail message, order ID, and cause.
     * 
     * @param message the detail message
     * @param orderId the order ID related to this exception
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public OrderException(String message, int orderId, Throwable cause) {
        super(message + " (Order ID: " + orderId + ")", cause);
        this.orderId = orderId;
    }
    
    /**
     * Gets the order ID associated with this exception.
     * 
     * @return the order ID, or 0 if not specified
     */
    public int getOrderId() {
        return orderId;
    }
}



