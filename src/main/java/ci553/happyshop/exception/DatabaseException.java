package ci553.happyshop.exception;

/**
 * Exception thrown when database operations fail.
 * 
 * This exception wraps SQLException and provides more context-specific
 * error messages for database-related issues.
 */
public class DatabaseException extends HappyShopException {
    
    /**
     * Constructs a new DatabaseException with the specified detail message.
     * 
     * @param message the detail message
     */
    public DatabaseException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new DatabaseException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the underlying SQLException or other cause
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates a DatabaseException from an SQLException with a user-friendly message.
     * 
     * @param operation the operation that failed (e.g., "searching product", "updating stock")
     * @param cause the underlying SQLException
     * @return a DatabaseException with a formatted message
     */
    public static DatabaseException fromSQLException(String operation, Throwable cause) {
        return new DatabaseException(
            "Database error while " + operation + ": " + cause.getMessage(),
            cause
        );
    }
}



