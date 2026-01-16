package ci553.happyshop.orderManagement;

import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.client.orderTracker.OrderTracker;
import ci553.happyshop.client.picker.PickerModel;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.storageAccess.DatabaseRWFactory;
import ci553.happyshop.storageAccess.OrderFileManager;
import ci553.happyshop.utility.StorageLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * <p>{@code OrderHub} serves as the heart of the ordering system.
 * This class implements the Singleton pattern to ensure a single instance governs
 * all order-related logic across the system.</p>
 *
 * <p> It is the central coordinator responsible for managing all orders. It handles:
 *   Creating and tracking orders
 *   Maintaining and updating the internal order map, <OrderId, OrderState>
 *   Delegating file-related operations (e.g., updating state and moving files) to OrderFileManager class
 *   Loading orders in the "ordered" and "progressing" states from storage during system startup
 *
 * <p> OrderHub also follows the Observer pattern: it notifies registered observers such as OrderTracker
 * and PickerModel whenever the order data changes, keeping the UI and business logic in sync.</p>
 *
 * <p>As the heart of the ordering system, OrderHub connects customers, pickers, and tracker,
 * managementing logic into a unified workflow.</p>
 */

public class OrderHub  {
    private static OrderHub orderHub; //singleton instance

    private final Path orderedPath = StorageLocation.orderedPath;
    private final Path progressingPath = StorageLocation.progressingPath;
    private final Path collectedPath = StorageLocation.collectedPath;
    private final Path cancelledPath = StorageLocation.cancelledPath;

    private TreeMap<Integer,OrderState> orderMap = new TreeMap<>();
    private TreeMap<Integer,OrderState> OrderedOrderMap = new TreeMap<>();
    private TreeMap<Integer,OrderState> progressingOrderMap = new TreeMap<>();
    
    /**
     * Tracks which orders are currently locked by pickers.
     * This prevents multiple pickers from claiming the same order simultaneously.
     * The locking mechanism is centralized in OrderHub to ensure thread safety.
     */
    private final java.util.Set<Integer> lockedOrderIds = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /**
     * Unified list to hold all registered OrderObserver instances.
     * These observers are notified whenever the orderMap is updated,
     * but each observer is only notified of the parts of the orderMap that are relevant to them.
     * - OrderTrackers will be notified of the full orderMap, including all orders (ordered, progressing, collected),
     *   but collected orders are shown for a limited time (10 seconds).
     * - PickerModels will be notified only of orders in the "ordered" or "progressing" states, filtering out collected orders.
     * 
     * @deprecated The separate lists (orderTrackerList, pickerModelList) are maintained for backward compatibility
     * but new code should use the unified observerList.
     */
    private ArrayList<OrderObserver> observerList = new ArrayList<>();
    
    /**
     * @deprecated Use observerList instead. Maintained for backward compatibility.
     */
    @Deprecated
    private ArrayList<OrderTracker> orderTrackerList = new ArrayList<>();
    
    /**
     * @deprecated Use observerList instead. Maintained for backward compatibility.
     */
    @Deprecated
    private ArrayList<PickerModel> pickerModelList = new ArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    //Singleton pattern
    private OrderHub() {}
    public static OrderHub getOrderHub() {
        if (orderHub == null)
            orderHub = new OrderHub();
            return orderHub;
    }

    //Creates a new order using the provided list of products.
    //and also notify picker and orderTracker
    public Order newOrder(ArrayList<Product> trolley) throws IOException, SQLException {
        int orderId = OrderCounter.generateOrderId(); //get unique orderId
        String orderedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        //make an Order Object: id, Ordered_state, orderedDateTime, and productsList(trolley)
        Order theOrder = new Order(orderId,OrderState.Ordered,orderedDateTime,trolley);

        //write order details to file for the orderId in orderedPath (ie. orders/ordered)
        String orderDetail = theOrder.orderDetails();
        Path path = orderedPath;
        OrderFileManager.createOrderFile(path, orderId, orderDetail);

        orderMap.put(orderId, theOrder.getState()); //add the order to orderMap,state is Ordered initially
        notifyObservers(); //notify all observers using the unified interface
        // Also call deprecated methods for backward compatibility
        notifyOrderTrackers();
        notifyPickerModels();
        
        return theOrder;
    }

    /**
     * Registers an OrderObserver to receive updates about order changes.
     * This is the preferred method for registering observers.
     * 
     * @param observer the observer to register
     */
    public void registerObserver(OrderObserver observer) {
        if (observer != null && !observerList.contains(observer)) {
            observerList.add(observer);
        }
    }

    /**
     * Unregisters an OrderObserver so it no longer receives updates.
     * 
     * @param observer the observer to unregister
     */
    public void unregisterObserver(OrderObserver observer) {
        observerList.remove(observer);
    }

    /**
     * Notifies all registered observers of changes to the order map.
     * Each observer receives a filtered view of the order map based on
     * the states they are interested in (via getInterestedStates()).
     */
    public void notifyObservers() {
        for (OrderObserver observer : observerList) {
            TreeMap<Integer, OrderState> filteredMap = getFilteredOrderMap(observer);
            observer.updateOrderMap(filteredMap);
        }
    }

    /**
     * Gets a filtered order map based on the observer's interested states.
     * If the observer doesn't specify interested states, returns the full map.
     * 
     * @param observer the observer requesting the filtered map
     * @return a filtered TreeMap containing only the states the observer is interested in
     */
    private TreeMap<Integer, OrderState> getFilteredOrderMap(OrderObserver observer) {
        OrderState[] interestedStates = observer.getInterestedStates();
        
        // If no specific states requested, return full map
        if (interestedStates == null || interestedStates.length == 0) {
            return new TreeMap<>(orderMap);
        }
        
        // Filter map to only include interested states
        TreeMap<Integer, OrderState> filteredMap = new TreeMap<>();
        for (Map.Entry<Integer, OrderState> entry : orderMap.entrySet()) {
            for (OrderState state : interestedStates) {
                if (entry.getValue() == state) {
                    filteredMap.put(entry.getKey(), entry.getValue());
                    break;
                }
            }
        }
        return filteredMap;
    }

    /**
     * Attempts to lock an order for a picker.
     * This prevents multiple pickers from claiming the same order simultaneously.
     * 
     * @param orderId the order ID to lock
     * @return true if the order was successfully locked, false if it was already locked
     */
    public synchronized boolean lockOrder(int orderId) {
        if (lockedOrderIds.contains(orderId)) {
            return false; // Order is already locked
        }
        lockedOrderIds.add(orderId);
        return true; // Successfully locked the order
    }

    /**
     * Unlocks an order, making it available for other pickers.
     * 
     * @param orderId the order ID to unlock
     */
    public synchronized void unlockOrder(int orderId) {
        lockedOrderIds.remove(orderId);
    }

    /**
     * Checks if an order is currently locked.
     * 
     * @param orderId the order ID to check
     * @return true if the order is locked, false otherwise
     */
    public synchronized boolean isOrderLocked(int orderId) {
        return lockedOrderIds.contains(orderId);
    }

    /**
     * Gets the first unlocked order ID from the order map that matches the specified state.
     * This is used by pickers to find available orders to process.
     * 
     * @param state the order state to look for (typically OrderState.Ordered)
     * @return the first unlocked order ID, or null if no unlocked orders are available
     */
    public synchronized Integer getFirstUnlockedOrder(OrderState state) {
        for (Map.Entry<Integer, OrderState> entry : orderMap.entrySet()) {
            int orderId = entry.getKey();
            if (entry.getValue() == state && !isOrderLocked(orderId)) {
                return orderId;
            }
        }
        return null;
    }

    //Registers an OrderTracker to receive updates about changes.
    // @deprecated Use registerObserver() instead
    @Deprecated
    public void registerOrderTracker(OrderTracker orderTracker){
        orderTrackerList.add(orderTracker);
        registerObserver(orderTracker); // Also register in unified list
    }
    
    //Notifies all registered observer_OrderTrackers to update and display the latest orderMap.
    // @deprecated Use notifyObservers() instead
    @Deprecated
    public void notifyOrderTrackers(){
        for(OrderTracker orderTracker : orderTrackerList){
            orderTracker.setOrderMap(orderMap);
        }
    }

    //Registers a PickerModel to receive updates about changes.
    // @deprecated Use registerObserver() instead
    @Deprecated
    public void registerPickerModel(PickerModel pickerModel){
        pickerModelList.add(pickerModel);
        registerObserver(pickerModel); // Also register in unified list
    }

    //notify all pickers to show orderMap (only ordered and progressing states orders)
    // @deprecated Use notifyObservers() instead
    @Deprecated
    public void notifyPickerModels(){
        TreeMap<Integer,OrderState> orderMapForPicker = new TreeMap<>();
        progressingOrderMap = filterOrdersByState(OrderState.Progressing);
        OrderedOrderMap = filterOrdersByState(OrderState.Ordered);
        orderMapForPicker.putAll(progressingOrderMap);
        orderMapForPicker.putAll(OrderedOrderMap);
        for(PickerModel pickerModel : pickerModelList){
            pickerModel.setOrderMap(orderMapForPicker);
        }
    }

    // Filters orderMap that match the specified state, a helper class used by notifyPickerModel()
    private TreeMap<Integer, OrderState> filterOrdersByState(OrderState state) {
        TreeMap<Integer, OrderState> filteredOrderMap = new TreeMap<>(); // New map to hold filtered orders
        // Loop through the orderMap and add matching orders to filteredOrders
        for (Map.Entry<Integer, OrderState> entry : orderMap.entrySet()) {
            if (entry.getValue() == state) {
                filteredOrderMap.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredOrderMap;
    }

    //Changes the state of the specified order, updates its file, and moves it to the appropriate folder.
    //trigger by PickerModel
    public void changeOrderStateMoveFile(int orderId, OrderState newState) throws IOException {
        if(orderMap.containsKey(orderId) && !orderMap.get(orderId).equals(newState))
        {
            //change orderState in OrderMap, notify all observers
            orderMap.put(orderId, newState);
            notifyObservers(); //notify all observers using the unified interface
            // Also call deprecated methods for backward compatibility
            notifyOrderTrackers();
            notifyPickerModels();

            //change orderState in order file and move the file to new state folder
            switch(newState){
                case OrderState.Progressing:
                    OrderFileManager.updateAndMoveOrderFile(orderId, newState,orderedPath,progressingPath);
                    break;
                case OrderState.Collected:
                    OrderFileManager.updateAndMoveOrderFile(orderId, newState,progressingPath,collectedPath);
                    unlockOrder(orderId); // Unlock the order when it's collected
                    removeCollectedOrder(orderId); //Scheduled removal
                    break;
                case OrderState.Cancelled:
                    OrderFileManager.updateAndMoveOrderFile(orderId, newState,orderedPath,cancelledPath);
                    unlockOrder(orderId); // Unlock the order if it was cancelled
                    break;
            }
        }
    }

    /**
     * Removes collected orders from the system after they have been collected for 10 seconds.
     *
     * This ensures that collected orders are cleared from the active order pool and are no longer displayed
     * by the OrderTracker after the brief period. This keeps the system focused on orders in the
     * "ordered" and "progressing" states.
     * The 10-second delay gives enough time for any final updates, and providing a short window for review of completed orders.
     */
    private void removeCollectedOrder(int orderId) {
        if (orderMap.containsKey(orderId)) {
            // Schedule removal after a few seconds
            scheduler.schedule(() -> {
                orderMap.remove(orderId); //remove collected order
                System.out.println("Order " + orderId + " removed from tracker and OrdersMap.");
                notifyObservers(); //notify all observers using the unified interface
                notifyOrderTrackers(); // Also call deprecated method for backward compatibility
            }, 10, TimeUnit.SECONDS );
        }
    }

    // Reads details of an order for display in the picker once they started preparing the order.
    public String  getOrderDetailForPicker(int orderId) throws IOException {
        OrderState state = orderMap.get(orderId);
        if(state.equals(OrderState.Progressing)) {
            return OrderFileManager.readOrderFile(progressingPath,orderId);
        }else{
            return "the fuction is only for picker";
        }
    }

    /**
     * Cancels an order that is in "Ordered" state.
     * Only orders in "Ordered" state can be cancelled (not progressing or collected orders).
     * 
     * This method:
     * 1. Validates that the order exists and is in "Ordered" state
     * 2. Reads the order file to extract product information
     * 3. Restores stock for all products in the order
     * 4. Changes order state to "Cancelled"
     * 5. Moves the order file to the cancelled folder
     * 6. Notifies all observers (OrderTrackers and PickerModels)
     * 
     * @param orderId The ID of the order to cancel
     * @return true if cancellation was successful, false if order cannot be cancelled
     * @throws IOException if there's an error reading/writing order files
     * @throws SQLException if there's an error restoring stock in the database
     */
    public boolean cancelOrder(int orderId) throws IOException, SQLException {
        // Check if order exists and is in Ordered state (only Ordered orders can be cancelled)
        if(!orderMap.containsKey(orderId)) {
            System.out.println("Order " + orderId + " not found.");
            return false;
        }
        
        OrderState currentState = orderMap.get(orderId);
        if(!currentState.equals(OrderState.Ordered)) {
            System.out.println("Order " + orderId + " cannot be cancelled. Only orders in 'Ordered' state can be cancelled. Current state: " + currentState);
            return false;
        }

        // Read order file to extract product list
        String orderContent = OrderFileManager.readOrderFile(orderedPath, orderId);
        ArrayList<Product> productsToRestore = parseProductsFromOrderFile(orderContent);

        // Restore stock for all products in the order
        if(!productsToRestore.isEmpty()) {
            DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();
            databaseRW.restoreStock(productsToRestore);
            System.out.println("Stock restored for cancelled order " + orderId);
        }

        // Change order state to Cancelled and move file
        changeOrderStateMoveFile(orderId, OrderState.Cancelled);
        
        System.out.println("Order " + orderId + " has been cancelled successfully.");
        return true;
    }

    /**
     * Parses product information from an order file content.
     * Extracts product IDs, quantities, and other details from the Items section.
     * 
     * @param orderContent The full content of the order file
     * @return A list of Product objects with ordered quantities set
     */
    private ArrayList<Product> parseProductsFromOrderFile(String orderContent) throws SQLException {
        ArrayList<Product> products = new ArrayList<>();
        DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();
        
        String[] lines = orderContent.split("\n");
        boolean inItemsSection = false;
        
        for(String line : lines) {
            line = line.trim();
            
            // Start parsing when we reach the Items section
            if(line.equals("Items:")) {
                inItemsSection = true;
                continue;
            }
            
            // Stop parsing when we reach the Total line or separator
            if(inItemsSection && (line.startsWith("-") || line.startsWith("Total"))) {
                break;
            }
            
            // Parse product lines: " 0001 Description ( 2) £ 100.00"
            if(inItemsSection && !line.isEmpty()) {
                try {
                    // Extract product ID (first 7 characters, trimmed)
                    String productId = line.substring(0, 7).trim();
                    
                    // Find quantity in parentheses: ( 2)
                    int quantityStart = line.indexOf('(');
                    int quantityEnd = line.indexOf(')');
                    if(quantityStart != -1 && quantityEnd != -1) {
                        String quantityStr = line.substring(quantityStart + 1, quantityEnd).trim();
                        int quantity = Integer.parseInt(quantityStr);
                        
                        // Look up product from database to get full details
                        Product product = databaseRW.searchByProductId(productId);
                        if(product != null) {
                            product.setOrderedQuantity(quantity);
                            products.add(product);
                        }
                    }
                } catch(Exception e) {
                    System.err.println("Error parsing product line: " + line + " - " + e.getMessage());
                }
            }
        }
        
        return products;
    }

    //Initializes the internal order map by loading the uncollected orders from the file system.
    // Called during system startup by the Main class.
    public void initializeOrderMap(){
        ArrayList<Integer> orderedIds = orderIdsLoader(orderedPath);
        ArrayList<Integer> progressingIds = orderIdsLoader(progressingPath);
        if(orderedIds.size()>0){
            for(Integer orderId : orderedIds){
                orderMap.put(orderId, OrderState.Ordered);
            }
        }
        if(progressingIds.size()>0){
            for(Integer orderId : progressingIds){
                orderMap.put(orderId, OrderState.Progressing);
            }
        }
        notifyObservers(); //notify all observers using the unified interface
        // Also call deprecated methods for backward compatibility
        notifyOrderTrackers();
        notifyPickerModels();
        System.out.println("orderMap initilized. "+ orderMap.size() + " orders in total, including:");
        System.out.println( orderedIds.size() + " Ordered orders, " +progressingIds.size() + " Progressing orders " );
    }

    // Loads a list of order IDs from the specified directory.
    // Used internally by initializeOrderMap().
    private ArrayList<Integer> orderIdsLoader(Path dir) {
        ArrayList<Integer> orderIds = new ArrayList<>();

        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try (Stream<Path> fileStream = Files.list(dir)) {
                // Process the stream without checking it separately
                List<Path> files = fileStream.filter(Files::isRegularFile).toList();

                if (files.isEmpty()) {
                    System.out.println(dir + " is empty");
                } else {
                    for (Path file : files) {
                        String fileName = file.getFileName().toString();
                        if (fileName.endsWith(".txt")) { // Ensure it's a .txt file
                            try {
                                int orderId = Integer.parseInt(fileName.substring(0, fileName.lastIndexOf('.')));
                                orderIds.add(orderId);
                                System.out.println(orderId);
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid file name: " + fileName);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading " + dir + ", " + e.getMessage());
            }
        } else {
            System.out.println(dir + " does not exist.");
        }
        return orderIds;
    }

}
