package ci553.happyshop.client.picker;

import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.orderManagement.OrderObserver;
import ci553.happyshop.orderManagement.OrderState;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * PickerModel represents the logic order picker.
 * PickerModel handles two main responsibilities:
 * 1. Observing OrderHub.
 * 2. Notifying PickerView to Updates user interface.
 *
 * 1. Observing OrderHub.
 * PickerModel is an observer of  OrderHub, receiving orderMap from OrderHub.
 * When a picker claims a task, PickerModel:
 * - Retrieves the first unlocked order from the orderMap.
 * - Locks the selected order to prevent other pickers from accessing it.
 * - Notifies OrderHub to update the orderMap, and begin preparation of the order.
 *
 * Once the order is collected by the customer, PickerModel:
 * - Unlocks the order.
 * - Notifies OrderHub to update the orderMap.
 * - Begins the next task if available.
 *
 * All changes in order state are centralized through OrderHub to ensure synchronization.
 * No picker directly changes the display before OrderHub updates the shared orderMap;
 * instead, each PickerModel waits for OrderHub's notification to refresh its state.
 *
 * Imagine the interaction flow:
 * PickerModel: "Hey OrderHub, I found an order that needs to be prepared. Please update the orderMap."
 * OrderHub: "Got it. I'll update the orderMap first."
 * OrderHub (after updating): "Attention all pickers: the orderMap has changed. Please refresh your views."
 *
 * This ensures that all PickerModels stay in sync by only updating their local state
 * in response to centralized changes made by the OrderHub.
 */

public class PickerModel implements OrderObserver {
    private PickerView pickerView;
    private OrderHub orderHub = OrderHub.getOrderHub();

    /**
     * Sets the PickerView for this model.
     * @param pickerView the PickerView instance
     */
    public void setPickerView(PickerView pickerView) {
        this.pickerView = pickerView;
    }

    /**
     * Gets the PickerView associated with this model.
     * @return the PickerView instance
     */
    public PickerView getPickerView() {
        return pickerView;
    }

    //two elements that need to be passed to PickerView for updating.
    private String displayTaOrderMap="";
    private String displayTaOrderDetail ="";

    // TreeMap (orderID,state) holding order IDs and their corresponding states.
    // This is now instance-level (not static) and is updated via OrderHub notifications.
    private TreeMap<Integer, OrderState> orderMap = new TreeMap<>();

    private int theOrderId=0; //Order ID assigned to a picker;
                              // 0 means no order is currently assigned.
    private OrderState theOrderState;

    /**
     * Attempts to find an unlocked order for this picker and mark it as progressing.
     * The order will be locked to prevent other pickers from accessing it.
     * Only the first unlocked order found will be processed.
     * 
     * Locking is now handled by OrderHub to ensure thread safety across all picker instances.
     */
    public void doProgressing() throws IOException {
        // Use OrderHub to get the first unlocked order
        Integer orderId = orderHub.getFirstUnlockedOrder(OrderState.Ordered);
        
        if (orderId != null) {
            // Try to lock the order through OrderHub
            if (orderHub.lockOrder(orderId)) {
                theOrderId = orderId; // Save the assigned orderId to this picker and update its state
                theOrderState = OrderState.Progressing;
                notifyOrderHub();// Notify the OrderHub about the state change
                updatePickerView(); // Refresh picker view
            }
            // If locking failed (shouldn't happen since we checked, but handle gracefully)
        }
    }

    public void doCollected() throws IOException {
        if(theOrderId != 0 && orderHub.isOrderLocked(theOrderId)){
            theOrderState = OrderState.Collected;
            notifyOrderHub(); // Notify the OrderHub about the state change
            displayTaOrderDetail = "";
            updatePickerView(); // update picker view
            int orderIdToUnlock = theOrderId; // Save before resetting
            theOrderId = 0;  //reset to no order is with the picker
            orderHub.unlockOrder(orderIdToUnlock); // Unlock the order through OrderHub
        }
    }

    // Registers this PickerModel instance with the OrderHub
    //so it can receive updates about orderMap changes.
    public void registerWithOrderHub(){
        OrderHub orderHub = OrderHub.getOrderHub();
        orderHub.registerPickerModel(this);
    }

    //Notifies the OrderHub of a change in the order state.
    //If the order is moving to the 'Progressing' state, asks OrderHub to read the order detail
    // from the file system for displaying in the pickerView.
    private void notifyOrderHub() throws IOException {
        orderHub.changeOrderStateMoveFile(theOrderId, theOrderState);
        if (theOrderState == OrderState.Progressing) {
            // Read order file, ie. order details
            displayTaOrderDetail = orderHub.getOrderDetailForPicker(theOrderId);
        }
    }

    // Sets the order map with new data and refreshes the display.
    // This method is called by OrderHub to set orderMap for picker.
    // @deprecated Use updateOrderMap() instead (implements OrderObserver interface)
    @Deprecated
    public void setOrderMap(TreeMap<Integer,OrderState> om) {
        updateOrderMap(om);
    }

    /**
     * Updates the order map with new data and refreshes the display.
     * This method is called by OrderHub when order states are updated.
     * Implements the OrderObserver interface.
     * 
     * @param orderMap A TreeMap containing order IDs as keys and their current states as values.
     *                 For pickers, this map is filtered to only include Ordered and Progressing orders.
     */
    @Override
    public void updateOrderMap(TreeMap<Integer, OrderState> orderMap) {
        this.orderMap.clear();
        this.orderMap.putAll(orderMap);
        displayTaOrderMap = buildOrderMapString();
        updatePickerView();
    }

    /**
     * Returns the order states that this picker is interested in.
     * Pickers only need to see orders in "Ordered" or "Progressing" states.
     * 
     * @return An array containing OrderState.Ordered and OrderState.Progressing
     */
    @Override
    public OrderState[] getInterestedStates() {
        return new OrderState[]{OrderState.Ordered, OrderState.Progressing};
    }

    //Builds a formatted string representing the current order map.
    //Each line contains the order ID followed by its state, aligned with spacing.
    private String buildOrderMapString() {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Integer, OrderState> entry : orderMap.entrySet()) {
            int orderId = entry.getKey();
            OrderState orderState = entry.getValue();
            sb.append(orderId).append(" ".repeat(8)).append(orderState).append("\n");
        }
        return sb.toString();
    }

    private void updatePickerView()
    {
        pickerView.update(displayTaOrderMap,displayTaOrderDetail);
    }
}
