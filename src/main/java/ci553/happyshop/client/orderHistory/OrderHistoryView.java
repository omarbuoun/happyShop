package ci553.happyshop.client.orderHistory;

import ci553.happyshop.service.OrderExportService;
import ci553.happyshop.service.OrderHistoryService;
import ci553.happyshop.utility.UIStyle;
import ci553.happyshop.utility.WinPosManager;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Order History View displays historical orders and statistics.
 * 
 * Features:
 * - View all past orders (collected and cancelled)
 * - Display order statistics (total orders, revenue, etc.)
 * - Export order history to CSV or JSON
 * 
 * This view provides business insights and allows data export for analysis.
 */
public class OrderHistoryView {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 500;
    
    private Stage window;
    private Scene scene;
    private TextArea taHistory;
    private TextArea taStatistics;
    private Button btnRefresh;
    private Button btnExportCSV;
    private Button btnExportJSON;

    public OrderHistoryView() {
        createScene();
    }

    private void createScene() {
        // Title
        Label laTitle = new Label("📊 Order History & Analytics");
        laTitle.setStyle(UIStyle.labelTitleStyle);

        // Statistics section
        Label laStatsTitle = new Label("Statistics:");
        laStatsTitle.setStyle(UIStyle.labelStyle);
        taStatistics = new TextArea();
        taStatistics.setEditable(false);
        taStatistics.setPrefRowCount(6);
        taStatistics.setStyle(UIStyle.textFiledStyle);

        // History section
        Label laHistoryTitle = new Label("Order History:");
        laHistoryTitle.setStyle(UIStyle.labelStyle);
        taHistory = new TextArea();
        taHistory.setEditable(false);
        taHistory.setPrefRowCount(15);
        taHistory.setStyle(UIStyle.textFiledStyle);

        // Buttons
        btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setStyle(UIStyle.buttonStyle);
        btnRefresh.setOnAction(e -> refreshHistory());

        btnExportCSV = new Button("📄 Export CSV");
        btnExportCSV.setStyle(UIStyle.buttonStyle);
        btnExportCSV.setOnAction(e -> exportToCSV());

        btnExportJSON = new Button("📋 Export JSON");
        btnExportJSON.setStyle(UIStyle.buttonStyle);
        btnExportJSON.setOnAction(e -> exportToJSON());

        HBox hbButtons = new HBox(10, btnRefresh, btnExportCSV, btnExportJSON);
        hbButtons.setAlignment(Pos.CENTER);

        // Layout
        VBox vbStats = new VBox(5, laStatsTitle, taStatistics);
        VBox vbHistory = new VBox(5, laHistoryTitle, taHistory);
        VBox vbox = new VBox(10, laTitle, vbStats, vbHistory, hbButtons);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setStyle(UIStyle.rootStyle);

        scene = new Scene(vbox, WIDTH, HEIGHT);
    }

    public void start(Stage window) {
        this.window = window;
        window.setScene(scene);
        window.setTitle("📊 Order History & Analytics");
        WinPosManager.registerWindow(window, WIDTH, HEIGHT);
        window.show();
        
        // Load initial data
        refreshHistory();
    }

    private void refreshHistory() {
        try {
            // Load and display statistics
            OrderHistoryService.OrderStatistics stats = OrderHistoryService.calculateStatistics();
            taStatistics.setText(stats.toString());

            // Load and display history
            ArrayList<String> history = OrderHistoryService.loadOrderHistory();
            if (history.isEmpty()) {
                taHistory.setText("No order history available.");
            } else {
                StringBuilder historyText = new StringBuilder();
                for (String order : history) {
                    historyText.append(order).append("\n");
                }
                taHistory.setText(historyText.toString());
            }
        } catch (IOException e) {
            taHistory.setText("Error loading order history: " + e.getMessage());
            System.err.println("Error loading order history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Order History to CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("order_history_" + 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        
        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            try {
                OrderExportService.exportToCSV(file.getAbsolutePath());
                showAlert("Export Successful", "Order history exported to:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Export Failed", "Error exporting to CSV:\n" + e.getMessage());
                System.err.println("Error exporting to CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void exportToJSON() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Order History to JSON");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        fileChooser.setInitialFileName("order_history_" + 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json");
        
        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            try {
                OrderExportService.exportToJSON(file.getAbsolutePath());
                showAlert("Export Successful", "Order history exported to:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Export Failed", "Error exporting to JSON:\n" + e.getMessage());
                System.err.println("Error exporting to JSON: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}



