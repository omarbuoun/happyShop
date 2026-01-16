package ci553.happyshop.orderManagement;

import java.util.TreeMap;

/**
 * Interface for objects that observe changes to the order map in OrderHub.
 * 
 * This interface follows the Observer pattern, allowing objects to be notified
 * when orders are created, updated, or their states change.
 * 
 * Implementing classes should register themselves with OrderHub to receive
 * notifications about order state changes.
 * 
 * @see OrderHub
 */
public interface OrderObserver {
    
    /**
     * Called by OrderHub to notify the observer of changes to the order map.
     * 
     * The observer should update its internal state and refresh its display
     * based on the provided order map.
     * 
     * @param orderMap A TreeMap containing order IDs as keys and their current states as values.
     *                 The map may be filtered based on the observer's requirements.
     */
    void updateOrderMap(TreeMap<Integer, OrderState> orderMap);
    
    /**
     * Returns the order states that this observer is interested in.
     * 
     * This allows OrderHub to filter the order map before notifying the observer.
     * If null or empty array is returned, the observer will receive all orders.
     * 
     * @return An array of OrderState values that this observer wants to receive,
     *         or null/empty array to receive all orders
     */
    default OrderState[] getInterestedStates() {
        return null; // By default, receive all orders
    }
}



