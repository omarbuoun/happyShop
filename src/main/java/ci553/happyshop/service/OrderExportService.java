package ci553.happyshop.service;

import ci553.happyshop.storageAccess.OrderFileManager;
import ci553.happyshop.utility.StorageLocation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service class for exporting order data to various formats.
 * 
 * Responsibilities:
 * - Export order history to CSV format
 * - Export order history to JSON format
 * - Provide formatted export data
 * 
 * This service follows the Single Responsibility Principle by
 * separating export operations from other business logic.
 */
public class OrderExportService {
    
    private static final Path collectedPath = StorageLocation.collectedPath;
    private static final Path cancelledPath = StorageLocation.cancelledPath;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Exports order history to a CSV file.
     * 
     * @param outputPath The path where the CSV file should be saved
     * @throws IOException if there's an error reading or writing files
     */
    public static void exportToCSV(String outputPath) throws IOException {
        Path csvPath = Paths.get(outputPath);
        
        try (BufferedWriter writer = Files.newBufferedWriter(csvPath)) {
            // Write CSV header
            writer.write("Order ID,State,Ordered Date,Collected Date,Cancelled Date,Total");
            writer.newLine();
            
            // Export collected orders
            exportOrdersFromFolderToCSV(collectedPath, "Collected", writer);
            
            // Export cancelled orders
            exportOrdersFromFolderToCSV(cancelledPath, "Cancelled", writer);
        }
        
        System.out.println("Order history exported to CSV: " + outputPath);
    }

    /**
     * Exports orders from a specific folder to CSV format.
     * 
     * @param folderPath The folder containing order files
     * @param state The state of orders in this folder
     * @param writer The CSV writer
     * @throws IOException if there's an error reading files
     */
    private static void exportOrdersFromFolderToCSV(Path folderPath, String state, BufferedWriter writer) throws IOException {
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            return;
        }

        try (Stream<Path> fileStream = Files.list(folderPath)) {
            List<Path> files = fileStream
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".txt"))
                .toList();

            for (Path file : files) {
                try {
                    String fileName = file.getFileName().toString();
                    int orderId = Integer.parseInt(fileName.substring(0, fileName.lastIndexOf('.')));
                    String orderContent = OrderFileManager.readOrderFile(folderPath, orderId);
                    writeOrderToCSV(orderId, state, orderContent, writer);
                } catch (Exception e) {
                    System.err.println("Error exporting order: " + file + " - " + e.getMessage());
                }
            }
        }
    }

    /**
     * Writes a single order to CSV format.
     * 
     * @param orderId The order ID
     * @param state The order state
     * @param orderContent The order file content
     * @param writer The CSV writer
     * @throws IOException if there's an error writing
     */
    private static void writeOrderToCSV(int orderId, String state, String orderContent, BufferedWriter writer) throws IOException {
        String[] lines = orderContent.split("\n");
        String orderedDate = "";
        String collectedDate = "";
        String cancelledDate = "";
        double total = 0.0;
        
        for (String line : lines) {
            if (line.startsWith("OrderedDateTime:")) {
                orderedDate = line.substring("OrderedDateTime:".length()).trim();
            } else if (line.startsWith("CollectedDateTime:")) {
                collectedDate = line.substring("CollectedDateTime:".length()).trim();
            } else if (line.startsWith("CancelledDateTime:")) {
                cancelledDate = line.substring("CancelledDateTime:".length()).trim();
            } else if (line.startsWith("Total")) {
                try {
                    String totalStr = line.substring(line.indexOf("£") + 1).trim();
                    total = Double.parseDouble(totalStr);
                } catch (Exception e) {
                    // If parsing fails, total remains 0.0
                }
            }
        }
        
        // Write CSV row
        writer.write(String.format("%d,%s,%s,%s,%s,%.2f",
            orderId, state, orderedDate, collectedDate, cancelledDate, total));
        writer.newLine();
    }

    /**
     * Exports order history to a JSON file.
     * 
     * @param outputPath The path where the JSON file should be saved
     * @throws IOException if there's an error reading or writing files
     */
    public static void exportToJSON(String outputPath) throws IOException {
        Path jsonPath = Paths.get(outputPath);
        ArrayList<String> history = OrderHistoryService.loadOrderHistory();
        
        try (BufferedWriter writer = Files.newBufferedWriter(jsonPath)) {
            writer.write("{\n");
            writer.write("  \"exportDate\": \"" + LocalDateTime.now().format(dateTimeFormatter) + "\",\n");
            writer.write("  \"totalOrders\": " + history.size() + ",\n");
            writer.write("  \"orders\": [\n");
            
            for (int i = 0; i < history.size(); i++) {
                writer.write("    \"" + history.get(i).replace("\"", "\\\"") + "\"");
                if (i < history.size() - 1) {
                    writer.write(",");
                }
                writer.newLine();
            }
            
            writer.write("  ]\n");
            writer.write("}");
        }
        
        System.out.println("Order history exported to JSON: " + outputPath);
    }
}



