package ci553.happyshop.service;

import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.orderManagement.OrderState;
import ci553.happyshop.storageAccess.OrderFileManager;
import ci553.happyshop.utility.StorageLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service class for managing order history and analytics.
 * 
 * Responsibilities:
 * - Load order history from collected and cancelled folders
 * - Calculate order statistics (total orders, revenue, popular products)
 * - Provide data for order history display
 * - Support export functionality
 * 
 * This service follows the Single Responsibility Principle by
 * separating order history operations from business logic.
 */
public class OrderHistoryService {
    
    private static final Path collectedPath = StorageLocation.collectedPath;
    private static final Path cancelledPath = StorageLocation.cancelledPath;

    /**
     * Loads all historical orders from collected and cancelled folders.
     * 
     * @return List of order summary strings (ID, State, Date, Total)
     * @throws IOException if there's an error reading order files
     */
    public static ArrayList<String> loadOrderHistory() throws IOException {
        ArrayList<String> history = new ArrayList<>();
        
        // Load collected orders
        history.addAll(loadOrdersFromFolder(collectedPath, OrderState.Collected));
        
        // Load cancelled orders
        history.addAll(loadOrdersFromFolder(cancelledPath, OrderState.Cancelled));
        
        return history;
    }

    /**
     * Loads orders from a specific folder and formats them for display.
     * 
     * @param folderPath The folder to read orders from
     * @param state The state of orders in this folder
     * @return List of formatted order strings
     * @throws IOException if there's an error reading files
     */
    private static ArrayList<String> loadOrdersFromFolder(Path folderPath, OrderState state) throws IOException {
        ArrayList<String> orders = new ArrayList<>();
        
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            return orders;
        }

        try (Stream<Path> fileStream = Files.list(folderPath)) {
            List<Path> files = fileStream
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".txt"))
                .sorted((p1, p2) -> {
                    // Sort by order ID (extracted from filename) in descending order
                    try {
                        int id1 = Integer.parseInt(p1.getFileName().toString().replace(".txt", ""));
                        int id2 = Integer.parseInt(p2.getFileName().toString().replace(".txt", ""));
                        return Integer.compare(id2, id1); // Descending order
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .toList();

            for (Path file : files) {
                try {
                    String fileName = file.getFileName().toString();
                    int orderId = Integer.parseInt(fileName.substring(0, fileName.lastIndexOf('.')));
                    String orderContent = OrderFileManager.readOrderFile(folderPath, orderId);
                    String formatted = formatOrderSummary(orderId, state, orderContent);
                    orders.add(formatted);
                } catch (Exception e) {
                    System.err.println("Error reading order file: " + file + " - " + e.getMessage());
                }
            }
        }
        
        return orders;
    }

    /**
     * Formats an order summary for display in the history view.
     * 
     * @param orderId The order ID
     * @param state The order state
     * @param orderContent The full order file content
     * @return Formatted string for display
     */
    private static String formatOrderSummary(int orderId, OrderState state, String orderContent) {
        String[] lines = orderContent.split("\n");
        String dateTime = "";
        double total = 0.0;
        
        // Extract date/time based on state
        for (String line : lines) {
            if (state == OrderState.Collected && line.startsWith("CollectedDateTime:")) {
                dateTime = line.substring("CollectedDateTime:".length()).trim();
            } else if (state == OrderState.Cancelled && line.startsWith("CancelledDateTime:")) {
                dateTime = line.substring("CancelledDateTime:".length()).trim();
            } else if (line.startsWith("Total")) {
                // Extract total: " Total                               £  66.97"
                try {
                    String totalStr = line.substring(line.indexOf("£") + 1).trim();
                    total = Double.parseDouble(totalStr);
                } catch (Exception e) {
                    // If parsing fails, total remains 0.0
                }
            }
        }
        
        return String.format("Order #%d | %s | %s | £%.2f", orderId, state, dateTime, total);
    }

    /**
     * Calculates order statistics from historical orders.
     * 
     * @return OrderStatistics object containing calculated metrics
     * @throws IOException if there's an error reading order files
     */
    public static OrderStatistics calculateStatistics() throws IOException {
        ArrayList<String> history = loadOrderHistory();
        
        int totalOrders = history.size();
        double totalRevenue = 0.0;
        int collectedOrders = 0;
        int cancelledOrders = 0;
        
        // Parse revenue from history strings
        for (String order : history) {
            try {
                // Extract total from format: "Order #X | State | Date | £XX.XX"
                int poundIndex = order.lastIndexOf("£");
                if (poundIndex != -1) {
                    String totalStr = order.substring(poundIndex + 1).trim();
                    totalRevenue += Double.parseDouble(totalStr);
                }
                
                if (order.contains("Collected")) {
                    collectedOrders++;
                } else if (order.contains("Cancelled")) {
                    cancelledOrders++;
                }
            } catch (Exception e) {
                // Skip orders with parsing errors
            }
        }
        
        return new OrderStatistics(totalOrders, totalRevenue, collectedOrders, cancelledOrders);
    }

    /**
     * Data class for order statistics.
     */
    public static class OrderStatistics {
        public final int totalOrders;
        public final double totalRevenue;
        public final int collectedOrders;
        public final int cancelledOrders;
        public final double averageOrderValue;
        public final double cancellationRate;

        public OrderStatistics(int totalOrders, double totalRevenue, int collectedOrders, int cancelledOrders) {
            this.totalOrders = totalOrders;
            this.totalRevenue = totalRevenue;
            this.collectedOrders = collectedOrders;
            this.cancelledOrders = cancelledOrders;
            this.averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;
            this.cancellationRate = totalOrders > 0 ? (double) cancelledOrders / totalOrders * 100 : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                "Total Orders: %d\n" +
                "Total Revenue: £%.2f\n" +
                "Collected Orders: %d\n" +
                "Cancelled Orders: %d\n" +
                "Average Order Value: £%.2f\n" +
                "Cancellation Rate: %.1f%%",
                totalOrders, totalRevenue, collectedOrders, cancelledOrders,
                averageOrderValue, cancellationRate
            );
        }
    }
}

