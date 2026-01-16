package ci553.happyshop.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when validation fails.
 * 
 * This exception is used for input validation errors and can contain
 * multiple validation error messages.
 */
public class ValidationException extends HappyShopException {
    
    private final List<String> validationErrors;
    
    /**
     * Constructs a new ValidationException with the specified detail message.
     * 
     * @param message the detail message
     */
    public ValidationException(String message) {
        super(message);
        this.validationErrors = new ArrayList<>();
        this.validationErrors.add(message);
    }
    
    /**
     * Constructs a new ValidationException with a list of validation errors.
     * 
     * @param validationErrors the list of validation error messages
     */
    public ValidationException(List<String> validationErrors) {
        super(formatErrors(validationErrors));
        this.validationErrors = new ArrayList<>(validationErrors);
    }
    
    /**
     * Constructs a new ValidationException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.validationErrors = new ArrayList<>();
        this.validationErrors.add(message);
    }
    
    /**
     * Gets the list of validation error messages.
     * 
     * @return the list of validation error messages
     */
    public List<String> getValidationErrors() {
        return new ArrayList<>(validationErrors); // Return a copy for immutability
    }
    
    /**
     * Formats a list of error messages into a single string.
     * 
     * @param errors the list of error messages
     * @return a formatted string containing all errors
     */
    private static String formatErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Validation failed";
        }
        return String.join("\n", errors);
    }
}



