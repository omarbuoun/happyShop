package ci553.happyshop.client.orderTracker;

import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.orderManagement.OrderObserver;
import ci553.happyshop.orderManagement.OrderState;
import ci553.happyshop.utility.UIStyle;
import ci553.happyshop.utility.WinPosManager;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

/**
 * OrderTracker class is for tracking orders and their states.
 * It displays an ordersMap(a list of orders with their associated states) in a TextArea.
 * The ordersMap data is received from the OrderHub.
 */

public class OrderTracker implements OrderObserver {
    private final int WIDTH = UIStyle.trackerWinWidth;
    private final int HEIGHT = UIStyle.trackerWinHeight;

    // TreeMap (orderID,state) holding order IDs and their corresponding states.
    private static final TreeMap<Integer, OrderState> ordersMap = new TreeMap<>();
    private final TextArea taDisplay; //area to show all orderId and their state on the GUI

     //Constructor initializes the UI, a title Label, and a TextArea for displaying the order details.
    public OrderTracker() {
        Label laTitle = new Label("Order_ID,  State");
        laTitle.setStyle(UIStyle.labelTitleStyle);

        taDisplay = new TextArea();
        taDisplay.setEditable(false);
        taDisplay.setStyle(UIStyle.textFiledStyle);

        // Cancel order section
        Label laCancelTitle = new Label("Cancel Order");
        laCancelTitle.setStyle(UIStyle.labelTitleStyle);
        
        Label laOrderId = new Label("Order ID:");
        laOrderId.setStyle(UIStyle.labelStyle);
        TextField tfOrderId = new TextField();
        tfOrderId.setPromptText("Enter order ID");
        tfOrderId.setStyle(UIStyle.textFiledStyle);
        tfOrderId.setPrefWidth(100);
        
        Button btnCancel = new Button("Cancel Order");
        btnCancel.setStyle(UIStyle.buttonStyle);
        btnCancel.setOnAction(e -> {
            try {
                String orderIdText = tfOrderId.getText().trim();
                if(!orderIdText.isEmpty()) {
                    int orderId = Integer.parseInt(orderIdText);
                    OrderHub orderHub = OrderHub.getOrderHub();
                    boolean cancelled = orderHub.cancelOrder(orderId);
                    if(cancelled) {
                        tfOrderId.clear();
                        System.out.println("Order " + orderId + " cancelled successfully");
                    } else {
                        System.out.println("Failed to cancel order " + orderId);
                    }
                }
            } catch(NumberFormatException ex) {
                System.err.println("Invalid order ID format");
            } catch(IOException | SQLException ex) {
                System.err.println("Error cancelling order: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        HBox hbCancel = new HBox(10, laOrderId, tfOrderId, btnCancel);
        hbCancel.setAlignment(Pos.CENTER);
        
        VBox vbox = new VBox(10, laTitle, taDisplay, laCancelTitle, hbCancel);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setStyle(UIStyle. rootStyleGray);

        Scene scene = new Scene(vbox, WIDTH, HEIGHT);
        Stage window = new Stage();
        window.setScene(scene);
        window.setTitle("🛒Order Tracker");

        // Registers the window's position with WinPosManager.
        WinPosManager.registerWindow(window,WIDTH,HEIGHT); //calculate position x and y for this window
        window.show();
    }

    /**
     * Registers this OrderTracker instance with the OrderHub.
     * This allows the OrderTracker to receive updates on order state changes.
     */
    public void registerWithOrderHub(){
        OrderHub orderHub = OrderHub.getOrderHub();
        orderHub.registerOrderTracker(this);
    }

    /**
     * Sets the order map with new data and refreshes the display.
     * This method is called by OrderHub when order states are updated.
     * @deprecated Use updateOrderMap() instead (implements OrderObserver interface)
     */
    @Deprecated
    public void setOrderMap(TreeMap<Integer, OrderState> om) {
        updateOrderMap(om);
    }

    /**
     * Updates the order map with new data and refreshes the display.
     * This method is called by OrderHub when order states are updated.
     * Implements the OrderObserver interface.
     * 
     * @param orderMap A TreeMap containing order IDs as keys and their current states as values
     */
    @Override
    public void updateOrderMap(TreeMap<Integer, OrderState> orderMap) {
        ordersMap.clear(); // Clears the current map to replace it with the new data.
        ordersMap.putAll(orderMap);// Adds all new order data to the map.
        displayOrderMap();// Updates the display with the new order map.
    }

     //Displays the current order map in the TextArea.
     //Iterates over the ordersMap and formats each order ID and state for display.
    private void displayOrderMap() {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Integer, OrderState> entry : ordersMap.entrySet()) {
            int orderId = entry.getKey();
            OrderState orderState = entry.getValue();
            sb.append(orderId).append(" ".repeat(5)).append(orderState).append("\n");
        }
        String textDisplay = sb.toString();
        taDisplay.setText(textDisplay);
    }

}
