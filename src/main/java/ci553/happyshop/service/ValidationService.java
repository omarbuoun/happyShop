package ci553.happyshop.service;

import ci553.happyshop.storageAccess.DatabaseRW;

import java.sql.SQLException;

/**
 * Service class for validating user input and business rules.
 * 
 * Responsibilities:
 * - Validate product IDs (format and uniqueness)
 * - Validate prices (format, range, decimal places)
 * - Validate stock quantities (format, range)
 * - Validate product descriptions
 * - Validate image paths
 * 
 * This service follows the Single Responsibility Principle by
 * centralizing all validation logic in one place, making it
 * easier to maintain and test.
 */
public class ValidationService {

    /**
     * Validates a product ID format (must be exactly 4 digits).
     * 
     * @param productId the product ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidProductIdFormat(String productId) {
        return productId != null && productId.matches("\\d{4}");
    }

    /**
     * Validates that a product ID is available (not already in use).
     * 
     * @param productId the product ID to check
     * @param databaseRW the database access object
     * @return true if available, false if already in use
     * @throws SQLException if there's an error accessing the database
     */
    public static boolean isProductIdAvailable(String productId, DatabaseRW databaseRW) throws SQLException {
        return databaseRW.isProIdAvailable(productId);
    }

    /**
     * Validates a price string format and value.
     * 
     * @param priceStr the price as a string
     * @return ValidationResult containing validation status and error message
     */
    public static ValidationResult validatePrice(String priceStr) {
        ValidationResult result = new ValidationResult();
        
        try {
            double price = Double.parseDouble(priceStr);
            
            // Validate: Ensure at most two decimal places
            if (!priceStr.matches("^[0-9]+(\\.[0-9]{0,2})?$")) {
                result.addError("Price can have at most two decimal places.");
                return result;
            }
            
            if (price <= 0) {
                result.addError("Price must be a positive number.");
                return result;
            }
            
            result.setValid(true);
        } catch (NumberFormatException e) {
            result.addError("Invalid price format.");
        }
        
        return result;
    }

    /**
     * Validates a stock quantity string format and value.
     * 
     * @param stockStr the stock quantity as a string
     * @return ValidationResult containing validation status and error message
     */
    public static ValidationResult validateStock(String stockStr) {
        ValidationResult result = new ValidationResult();
        
        try {
            int stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                result.addError("Stock quantity cannot be negative.");
                return result;
            }
            result.setValid(true);
        } catch (NumberFormatException e) {
            result.addError("Invalid stock quantity format.");
        }
        
        return result;
    }

    /**
     * Validates a stock change amount (for add/subtract operations).
     * 
     * @param changeByStr the change amount as a string
     * @return ValidationResult containing validation status and error message
     */
    public static ValidationResult validateStockChangeBy(String changeByStr) {
        ValidationResult result = new ValidationResult();
        
        try {
            Integer.parseInt(changeByStr);
            result.setValid(true);
        } catch (NumberFormatException e) {
            result.addError("Invalid stock quantity format.");
        }
        
        return result;
    }

    /**
     * Validates that a description is not empty.
     * 
     * @param description the description to validate
     * @return ValidationResult containing validation status and error message
     */
    public static ValidationResult validateDescription(String description) {
        ValidationResult result = new ValidationResult();
        
        if (description == null || description.trim().isEmpty()) {
            result.addError("Product description cannot be empty.");
            return result;
        }
        
        result.setValid(true);
        return result;
    }

    /**
     * Validates that an image path is provided (optional).
     * Image selection is optional - if not provided, a default image will be used.
     * 
     * @param imageUri the image URI/path (can be null or empty)
     * @return ValidationResult containing validation status (always valid since image is optional)
     */
    public static ValidationResult validateImagePath(String imageUri) {
        ValidationResult result = new ValidationResult();
        // Image is optional, so validation always passes
        result.setValid(true);
        return result;
    }

    /**
     * Validates all fields for editing an existing product.
     * 
     * @param priceStr the price string
     * @param stockStr the stock string
     * @param description the description
     * @param hasUnappliedStockChange whether there's an unapplied stock change
     * @return ValidationResult containing all validation errors
     */
    public static ValidationResult validateEditProduct(String priceStr, String stockStr, 
                                                       String description, boolean hasUnappliedStockChange) {
        ValidationResult result = new ValidationResult();
        
        // Validate price
        ValidationResult priceResult = validatePrice(priceStr);
        if (!priceResult.isValid()) {
            result.addErrors(priceResult.getErrors());
        }
        
        // Validate stock
        ValidationResult stockResult = validateStock(stockStr);
        if (!stockResult.isValid()) {
            result.addErrors(stockResult.getErrors());
        }
        
        // Validate description
        ValidationResult descResult = validateDescription(description);
        if (!descResult.isValid()) {
            result.addErrors(descResult.getErrors());
        }
        
        // Check for unapplied stock change
        if (hasUnappliedStockChange) {
            result.addError("Change stock by not applied.");
        }
        
        return result;
    }

    /**
     * Validates all fields for creating a new product.
     * 
     * @param productId the product ID
     * @param priceStr the price string
     * @param stockStr the stock string
     * @param description the description
     * @param imageUri the image URI
     * @param databaseRW the database access object
     * @return ValidationResult containing all validation errors
     * @throws SQLException if there's an error accessing the database
     */
    public static ValidationResult validateNewProduct(String productId, String priceStr, String stockStr,
                                                       String description, String imageUri, 
                                                       DatabaseRW databaseRW) throws SQLException {
        ValidationResult result = new ValidationResult();
        
        // Validate product ID format
        if (!isValidProductIdFormat(productId)) {
            result.addError("Product ID must be exactly 4 digits.");
        } else {
            // Check if ID is available
            if (!isProductIdAvailable(productId, databaseRW)) {
                result.addError("Product ID " + productId + " is not available.");
            }
        }
        
        // Validate price
        ValidationResult priceResult = validatePrice(priceStr);
        if (!priceResult.isValid()) {
            result.addErrors(priceResult.getErrors());
        }
        
        // Validate stock
        ValidationResult stockResult = validateStock(stockStr);
        if (!stockResult.isValid()) {
            result.addErrors(stockResult.getErrors());
        }
        
        // Validate description
        ValidationResult descResult = validateDescription(description);
        if (!descResult.isValid()) {
            result.addErrors(descResult.getErrors());
        }
        
        // Validate image path (optional - no validation needed)
        // Image selection is optional, so we don't add any errors if it's missing
        
        // If no errors were added, mark as valid
        if (!result.hasErrors()) {
            result.setValid(true);
        }
        
        return result;
    }

    /**
     * Inner class to hold validation results.
     * Encapsulates whether validation passed and any error messages.
     */
    public static class ValidationResult {
        private boolean valid = false;
        private StringBuilder errorMessage = new StringBuilder();

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public void addError(String error) {
            if (errorMessage.length() > 0) {
                errorMessage.append("\n");
            }
            errorMessage.append("• ").append(error);
            valid = false;
        }

        public void addErrors(StringBuilder errors) {
            if (errors != null && errors.length() > 0) {
                if (errorMessage.length() > 0) {
                    errorMessage.append("\n");
                }
                errorMessage.append(errors);
                valid = false;
            }
        }

        public String getErrorMessage() {
            return errorMessage.toString();
        }

        public StringBuilder getErrors() {
            return errorMessage;
        }

        public boolean hasErrors() {
            return errorMessage.length() > 0;
        }
    }
}



