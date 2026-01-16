package ci553.happyshop.exception;

/**
 * Exception thrown when file operations fail.
 * 
 * This exception is used for errors related to reading, writing, or
 * managing order files, image files, and other file system operations.
 */
public class FileOperationException extends HappyShopException {
    
    private final String filePath;
    
    /**
     * Constructs a new FileOperationException with the specified detail message.
     * 
     * @param message the detail message
     */
    public FileOperationException(String message) {
        super(message);
        this.filePath = null;
    }
    
    /**
     * Constructs a new FileOperationException with the specified detail message and file path.
     * 
     * @param message the detail message
     * @param filePath the path of the file that caused the exception
     */
    public FileOperationException(String message, String filePath) {
        super(message + " (File: " + filePath + ")");
        this.filePath = filePath;
    }
    
    /**
     * Constructs a new FileOperationException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
        this.filePath = null;
    }
    
    /**
     * Constructs a new FileOperationException with the specified detail message, file path, and cause.
     * 
     * @param message the detail message
     * @param filePath the path of the file that caused the exception
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public FileOperationException(String message, String filePath, Throwable cause) {
        super(message + " (File: " + filePath + ")", cause);
        this.filePath = filePath;
    }
    
    /**
     * Gets the file path associated with this exception.
     * 
     * @return the file path, or null if not specified
     */
    public String getFilePath() {
        return filePath;
    }
}



