package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.utility.ProductListFormatter;
import ci553.happyshop.service.TrolleyService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 * You can either directly modify the CustomerModel class to implement the required tasks,
 * or create a subclass of CustomerModel and override specific methods where appropriate.
 */
public class CustomerModel {
    private CustomerView cusView;
    private DatabaseRW databaseRW; //Interface type, not specific implementation
                                  //Benefits: Flexibility: Easily change the database implementation.

    /**
     * Sets the CustomerView for this model.
     * @param cusView the CustomerView instance
     */
    public void setCusView(CustomerView cusView) {
        this.cusView = cusView;
    }

    /**
     * Gets the CustomerView associated with this model.
     * @return the CustomerView instance
     */
    public CustomerView getCusView() {
        return cusView;
    }

    /**
     * Sets the DatabaseRW for this model.
     * @param databaseRW the DatabaseRW instance
     */
    public void setDatabaseRW(DatabaseRW databaseRW) {
        this.databaseRW = databaseRW;
    }

    /**
     * Gets the DatabaseRW associated with this model.
     * @return the DatabaseRW instance
     */
    public DatabaseRW getDatabaseRW() {
        return databaseRW;
    }

    private Product theProduct =null; // product found from search
    private ArrayList<Product> trolley =  new ArrayList<>(); // a list of products in trolley
    private RemoveProductNotifier removeProductNotifier; // Notifier for insufficient stock products

    // Four UI elements to be passed to CustomerView for display updates.
    private String imageName = "imageHolder.jpg";                // Image to show in product preview (Search Page)
    private String displayLaSearchResult = "No Product was searched yet"; // Label showing search result message (Search Page)
    private String displayTaTrolley = "";                                // Text area content showing current trolley items (Trolley Page)
    private String displayTaReceipt = "";                                // Text area content showing receipt after checkout (Receipt Page)

    //SELECT productID, description, image, unitPrice,inStock quantity
    // Enhanced search: supports both product ID and name (like Warehouse client)
    void search() throws SQLException {
        // Get search keyword from either ID or Name field (prioritize ID if both are filled)
        String searchKeyword = getCusView().tfId.getText().trim();
        if(searchKeyword.isEmpty()) {
            searchKeyword = getCusView().tfName.getText().trim();
        }
        
        if(!searchKeyword.isEmpty()){
            // Use searchProduct() which searches by ID first, then by name if ID not found
            ArrayList<Product> searchResults = databaseRW.searchProduct(searchKeyword);
            
            if(!searchResults.isEmpty() && searchResults.get(0).getStockQuantity() > 0){
                // For customer search, we'll use the first result (exact ID match takes priority)
                // If multiple results from name search, show the first one
                theProduct = searchResults.get(0);
                
                double unitPrice = theProduct.getUnitPrice();
                String description = theProduct.getProductDescription();
                int stock = theProduct.getStockQuantity();
                String productId = theProduct.getProductId();

                String baseInfo = String.format("Product_Id: %s\n%s,\nPrice: £%.2f", productId, description, unitPrice);
                String quantityInfo = stock < 100 ? String.format("\n%d units left.", stock) : "";
                
                // If multiple results found (name search), inform the user
                if(searchResults.size() > 1) {
                    baseInfo += String.format("\n(%d more products found with similar name)", searchResults.size() - 1);
                }
                
                displayLaSearchResult = baseInfo + quantityInfo;
                System.out.println(displayLaSearchResult);
            }
            else if(!searchResults.isEmpty() && searchResults.get(0).getStockQuantity() <= 0){
                // Product found but out of stock
                theProduct = null;
                displayLaSearchResult = "Product found but currently out of stock: " + searchKeyword;
                System.out.println("Product found but out of stock: " + searchKeyword);
            }
            else{
                // No products found
                theProduct = null;
                displayLaSearchResult = "No Product was found with ID/Name: " + searchKeyword;
                System.out.println("No Product was found with ID/Name: " + searchKeyword);
            }
        }else{
            theProduct = null;
            displayLaSearchResult = "Please type Product ID or Name to search";
            System.out.println("Please type Product ID or Name to search.");
        }
        updateView();
    }

    void addToTrolley(){
        if(theProduct!= null){
            // Add the product to the trolley
            trolley.add(theProduct);
            
            // Merge duplicate products (combine quantities) and sort by product ID
            // This keeps the trolley organized and prevents duplicate entries
            trolley = TrolleyService.mergeAndSort(trolley);
            
            displayTaTrolley = ProductListFormatter.buildString(trolley); //build a String for trolley so that we can show it
        }
        else{
            displayLaSearchResult = "Please search for an available product before adding it to the trolley";
            System.out.println("must search and get an available product before add to trolley");
        }
        displayTaReceipt=""; // Clear receipt to switch back to trolleyPage (receipt shows only when not empty)
        updateView();
    }

    void checkOut() throws IOException, SQLException {
        System.out.println("checkOut() called. Trolley size: " + trolley.size()); // Debug output
        if(!trolley.isEmpty()){
            // Group the products in the trolley by productId to optimize stock checking
            // Check the database for sufficient stock for all products in the trolley.
            // If any products are insufficient, the update will be rolled back.
            // If all products are sufficient, the database will be updated, and insufficientProducts will be empty.
            // Note: If the trolley is already organized (merged and sorted), grouping is unnecessary.
            ArrayList<Product> groupedTrolley= groupProductsById(trolley);
            System.out.println("Grouped trolley size: " + groupedTrolley.size()); // Debug output
            ArrayList<Product> insufficientProducts= databaseRW.purchaseStocks(groupedTrolley);
            System.out.println("Insufficient products count: " + insufficientProducts.size()); // Debug output

            if(insufficientProducts.isEmpty()){ // If stock is sufficient for all products
                System.out.println("Stock is sufficient, creating order..."); // Debug output
                // Close notifier window if it's showing from a previous insufficient stock situation
                if(removeProductNotifier != null) {
                    removeProductNotifier.closeNotifierWindow();
                }
                
                //get OrderHub and tell it to make a new Order
                OrderHub orderHub =OrderHub.getOrderHub();
                Order theOrder = orderHub.newOrder(trolley);
                System.out.println("Order created with ID: " + theOrder.getOrderId()); // Debug output
                trolley.clear();
                displayTaTrolley ="";
                displayTaReceipt = String.format(
                        "Order_ID: %s\nOrdered_Date_Time: %s\n%s",
                        theOrder.getOrderId(),
                        theOrder.getOrderedDateTime(),
                        ProductListFormatter.buildString(theOrder.getProductList())
                );
                System.out.println("Receipt generated:\n" + displayTaReceipt); // Debug output
            }
            else{ // Some products have insufficient stock — remove them and notify the customer
                System.out.println("Insufficient stock detected"); // Debug output
                // Step 1: Remove products with insufficient stock from the trolley
                removeInsufficientProductsFromTrolley(insufficientProducts);
                
                // Step 2: Build error message for the notification
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append("The following products were removed from your trolley due to insufficient stock:\n\n");
                for(Product p : insufficientProducts){
                    errorMsg.append("\u2022 "+ p.getProductId()).append(", ")
                            .append(p.getProductDescription()).append(" (Only ")
                            .append(p.getStockQuantity()).append(" available, ")
                            .append(p.getOrderedQuantity()).append(" requested)\n");
                }
                
                // Step 3: Update trolley display after removal
                trolley = TrolleyService.mergeAndSort(trolley); // Re-organize trolley after removal
                displayTaTrolley = ProductListFormatter.buildString(trolley);
                
                // Step 4: Show notification window using RemoveProductNotifier
                initializeRemoveProductNotifierIfNeeded();
                removeProductNotifier.showRemovalMsg(errorMsg.toString());
                
                theProduct=null;
                displayLaSearchResult = "Some products were removed from your trolley. Please check the notification window.";
                System.out.println("stock is not enough - products removed from trolley");
            }
        }
        else{
            displayTaTrolley = "Your trolley is empty";
            System.out.println("Your trolley is empty");
        }
        System.out.println("Calling updateView()..."); // Debug output
        updateView();
        System.out.println("updateView() completed"); // Debug output
    }

    /**
     * Groups products by their productId to optimize database queries and updates.
     * By grouping products, we can check the stock for a given `productId` once, rather than repeatedly
     */
    private ArrayList<Product> groupProductsById(ArrayList<Product> proList) {
        Map<String, Product> grouped = new HashMap<>();
        for (Product p : proList) {
            String id = p.getProductId();
            if (grouped.containsKey(id)) {
                Product existing = grouped.get(id);
                existing.setOrderedQuantity(existing.getOrderedQuantity() + p.getOrderedQuantity());
            } else {
                // Make a shallow copy to avoid modifying the original
                grouped.put(id,new Product(p.getProductId(),p.getProductDescription(),
                        p.getProductImageName(),p.getUnitPrice(),p.getStockQuantity()));
            }
        }
        return new ArrayList<>(grouped.values());
    }

    void cancel(){
        trolley.clear();
        displayTaTrolley="";
        // Close notifier window if it's showing
        if(removeProductNotifier != null) {
            removeProductNotifier.closeNotifierWindow();
        }
        updateView();
    }
    void closeReceipt(){
        displayTaReceipt="";
    }

    void updateView() {
        if(theProduct != null){
            imageName = theProduct.getProductImageName();
            String relativeImageUrl = StorageLocation.imageFolder +imageName; //relative file path, eg images/0001.jpg
            // Get the full absolute path to the image
            Path imageFullPath = Paths.get(relativeImageUrl).toAbsolutePath();
            imageName = imageFullPath.toUri().toString(); //get the image full Uri then convert to String
            System.out.println("Image absolute path: " + imageFullPath); // Debugging to ensure path is correct
        }
        else{
            imageName = "imageHolder.jpg";
        }
        getCusView().update(imageName, displayLaSearchResult, displayTaTrolley,displayTaReceipt);
    }
     // extra notes:
     //Path.toUri(): Converts a Path object (a file or a directory path) to a URI object.
     //File.toURI(): Converts a File object (a file on the filesystem) to a URI object

    /**
     * Removes products with insufficient stock from the trolley.
     * This method identifies products that need to be removed by matching product IDs
     * and removes them from the trolley list.
     * 
     * @param insufficientProducts List of products that have insufficient stock
     */
    private void removeInsufficientProductsFromTrolley(ArrayList<Product> insufficientProducts) {
        // Create a set of product IDs to remove for efficient lookup
        java.util.Set<String> idsToRemove = new java.util.HashSet<>();
        for(Product p : insufficientProducts) {
            idsToRemove.add(p.getProductId());
        }
        
        // Remove products from trolley that match the insufficient product IDs
        trolley.removeIf(product -> idsToRemove.contains(product.getProductId()));
    }
    
    /**
     * Initializes the RemoveProductNotifier if it hasn't been created yet.
     * This ensures the notifier is ready to display messages when needed.
     */
    private void initializeRemoveProductNotifierIfNeeded() {
        if(removeProductNotifier == null) {
            removeProductNotifier = new RemoveProductNotifier();
            removeProductNotifier.cusView = getCusView();
        }
    }

    //for test only
    public ArrayList<Product> getTrolley() {
        return trolley;
    }
}
