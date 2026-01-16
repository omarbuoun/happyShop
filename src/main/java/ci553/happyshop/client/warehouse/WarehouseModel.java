package ci553.happyshop.client.warehouse;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.storageAccess.DerbyRW;
import ci553.happyshop.storageAccess.ImageFileManager;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.service.StockAlertService;
import ci553.happyshop.service.ValidationService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class WarehouseModel {
    private WarehouseView view;
    private DatabaseRW databaseRW; //Interface type, not specific implementation
                         //Benefits: Flexibility: Easily change the database implementation.

    private ArrayList<Product> productList = new ArrayList<>(); // search results fetched from the database
    private Product theSelectedPro; // the product selected from the ListView before the user edits or deletes
    private String theNewProId;

    //information used to update editProduct child in WarehouseView
    private String displayIdEdit="";
    private String displayPriceEdit="";
    private String displayStockEdit="";
    private String displayDescriptionEdit="";
    private String displayImageUrlEdit ="WarehouseImageHolder.jpg";

    private HistoryWindow historyWindow;
    private AlertSimulator alertSimulator;

    /**
     * Sets the WarehouseView for this model.
     * @param view the WarehouseView instance
     */
    public void setView(WarehouseView view) {
        this.view = view;
    }

    /**
     * Gets the WarehouseView associated with this model.
     * @return the WarehouseView instance
     */
    public WarehouseView getView() {
        return view;
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

    /**
     * Sets the HistoryWindow for this model.
     * @param historyWindow the HistoryWindow instance
     */
    public void setHistoryWindow(HistoryWindow historyWindow) {
        this.historyWindow = historyWindow;
    }

    /**
     * Gets the HistoryWindow associated with this model.
     * @return the HistoryWindow instance
     */
    public HistoryWindow getHistoryWindow() {
        return historyWindow;
    }

    /**
     * Sets the AlertSimulator for this model.
     * @param alertSimulator the AlertSimulator instance
     */
    public void setAlertSimulator(AlertSimulator alertSimulator) {
        this.alertSimulator = alertSimulator;
    }

    /**
     * Gets the AlertSimulator associated with this model.
     * @return the AlertSimulator instance
     */
    public AlertSimulator getAlertSimulator() {
        return alertSimulator;
    }

    /**
     * Gets the display ID for editing.
     * @return the display ID
     */
    public String getDisplayIdEdit() {
        return displayIdEdit;
    }

    /**
     * Sets the display ID for editing.
     * @param displayIdEdit the display ID
     */
    public void setDisplayIdEdit(String displayIdEdit) {
        this.displayIdEdit = displayIdEdit;
    }

    /**
     * Gets the display price for editing.
     * @return the display price
     */
    public String getDisplayPriceEdit() {
        return displayPriceEdit;
    }

    /**
     * Sets the display price for editing.
     * @param displayPriceEdit the display price
     */
    public void setDisplayPriceEdit(String displayPriceEdit) {
        this.displayPriceEdit = displayPriceEdit;
    }

    /**
     * Gets the display stock for editing.
     * @return the display stock
     */
    public String getDisplayStockEdit() {
        return displayStockEdit;
    }

    /**
     * Sets the display stock for editing.
     * @param displayStockEdit the display stock
     */
    public void setDisplayStockEdit(String displayStockEdit) {
        this.displayStockEdit = displayStockEdit;
    }

    /**
     * Gets the display description for editing.
     * @return the display description
     */
    public String getDisplayDescriptionEdit() {
        return displayDescriptionEdit;
    }

    /**
     * Sets the display description for editing.
     * @param displayDescriptionEdit the display description
     */
    public void setDisplayDescriptionEdit(String displayDescriptionEdit) {
        this.displayDescriptionEdit = displayDescriptionEdit;
    }

    /**
     * Gets the display image URL for editing.
     * @return the display image URL
     */
    public String getDisplayImageUrlEdit() {
        return displayImageUrlEdit;
    }

    /**
     * Sets the display image URL for editing.
     * @param displayImageUrlEdit the display image URL
     */
    public void setDisplayImageUrlEdit(String displayImageUrlEdit) {
        this.displayImageUrlEdit = displayImageUrlEdit;
    }
    private String displayInputErrorMsg =""; //error message showing in the alertSimulator
    private ArrayList<String> displayManageHistory = new ArrayList<>();// Manage Product history
                                                               //shows in the HistoryWindow
    private enum ManageProductType{
        Edited,
        Deleted,
        New
    }

    private enum UpdateForAction{
        //actions in Search Page
        BtnSearch,  //actually its updating the Observable ProductList
        BtnEdit,
        BtnDelete,

        //actions in Editing an existing product page
        BtnChangeStockBy, // Refers to both "+" and "−" buttons for changing stock
        BtnSummitEdit,
        BtnCancelEdit,

        // actions in Adding a new product to stock page
        BtnCancelNew,
        BtnSummitNew,

        //show user input error message in alertSimulator
        ShowInputErrorMsg
    }

    void doSearch() throws SQLException {
        String keyword = getView().tfSearchKeyword.getText().trim();
        if (!keyword.equals("")) {
            productList = getDatabaseRW().searchProduct(keyword);
        }
        else{
            productList.clear();
            System.out.println("please type product ID or name to search");
        }
        updateView(UpdateForAction.BtnSearch);
    }

    /**
     * Scans all products for low stock conditions and displays alerts.
     * This method checks all products in the database and shows alerts for
     * products that are out of stock or below the low stock threshold.
     */
    void doCheckLowStock() throws SQLException {
        try {
            ArrayList<Product> allProducts = getDatabaseRW().getAllProducts();
            String alertMsg = StockAlertService.generateCombinedAlertMessage(allProducts);
            
            if(alertMsg != null && getAlertSimulator() != null) {
                getAlertSimulator().showErrorMsg(alertMsg);
            } else if(getAlertSimulator() != null) {
                getAlertSimulator().showErrorMsg("✅ All products have sufficient stock!\n\nNo low stock alerts at this time.");
            }
        } catch (SQLException e) {
            System.err.println("Error checking low stock: " + e.getMessage());
            if(getAlertSimulator() != null) {
                getAlertSimulator().showErrorMsg("Error checking stock levels: " + e.getMessage());
            }
            throw e;
        }
    }

    void doDelete() throws SQLException, IOException {
        System.out.println("delete gets called in model");
        Product pro  = getView().obrLvProducts.getSelectionModel().getSelectedItem();
        if (pro != null ) {
            theSelectedPro = pro;
            productList.remove(theSelectedPro); //remove the product from product List

            //update databse: delete the product from database
            getDatabaseRW().deleteProduct(theSelectedPro.getProductId());

            //delete the image from imageFolder "images/"
            String imageName = theSelectedPro.getProductImageName(); //eg 0011.jpg;
            ImageFileManager.deleteImageFile(StorageLocation.imageFolder, imageName);

            updateView(UpdateForAction.BtnDelete);
            theSelectedPro = null;
        }
        else{
            System.out.println("No product was selected");
        }
    }

    void doEdit() {
        System.out.println("Edit gets called in model");
        Product pro = getView().obrLvProducts.getSelectionModel().getSelectedItem();
        if (pro != null) {
            theSelectedPro = pro;
            displayIdEdit = theSelectedPro.getProductId();
            displayPriceEdit = String.format("%.2f", theSelectedPro.getUnitPrice());
            displayStockEdit = String.valueOf (theSelectedPro.getStockQuantity());
            displayDescriptionEdit = theSelectedPro.getProductDescription();

            String relativeImageUri = StorageLocation.imageFolder + theSelectedPro.getProductImageName();
            Path imageFullPath = Paths.get(relativeImageUri).toAbsolutePath();
            displayImageUrlEdit = imageFullPath.toUri().toString();//build the full path Uri

            System.out.println("get new pro image name: " + displayImageUrlEdit);
            updateView(UpdateForAction.BtnEdit);
        }
        else{
            System.out.println("No product was selected");
        }

    }

    void doCancel(){
       if(getView().theProFormMode.equals("EDIT")){
           updateView(UpdateForAction.BtnCancelEdit);
           theSelectedPro = null;
       }
       if(getView().theProFormMode.equals("NEW")){
           updateView(UpdateForAction.BtnCancelNew);
           theNewProId = null;
       }
    }
    void doSummit() throws SQLException, IOException {
        if(getView().theProFormMode.equals("EDIT")){
            doSubmitEdit();
        }
        if(getView().theProFormMode.equals("NEW")){
            doSubmitNew();
        }
    }

    private void doSubmitEdit() throws IOException, SQLException {
        System.out.println("ok edit is called");
        if(theSelectedPro!=null) {
            String id=theSelectedPro.getProductId();
            System.out.println("theSelectedPro " + id); //debug purpose
            String imageName = theSelectedPro.getProductImageName();

            String textPrice = getView().tfPriceEdit.getText().trim();
            String textStock = getView().tfStockEdit.getText().trim();
            String description = getView().taDescriptionEdit.getText().trim();

            if(getView().isUserSelectedImageEdit == true){  //if the user changed image
                ImageFileManager.deleteImageFile(StorageLocation.imageFolder, imageName); //delete the old image
                //copy the user selected image to project image folder
                //we use productId as image name, but we need to get its extension from the user selected image
                String newImageNameWithExtension = ImageFileManager.copyFileToDestination(getView().userSelectedImageUriEdit, StorageLocation.imageFolder,id);
                imageName = newImageNameWithExtension;
            }

            boolean hasUnappliedStockChange = !getView().tfChangeByEdit.getText().trim().isEmpty();
            ValidationService.ValidationResult validationResult = ValidationService.validateEditProduct(
                textPrice, textStock, description, hasUnappliedStockChange);
            if (!validationResult.isValid()) {
                displayInputErrorMsg = validationResult.getErrorMessage();
                updateView(UpdateForAction.ShowInputErrorMsg);
            }
            else {
                double price = Double.parseDouble(textPrice);
                int stock= Integer.parseInt(textStock);
                //update datbase
                getDatabaseRW().updateProduct(id,description,price,imageName,stock);

                // Check for low stock alert after update
                checkAndShowStockAlert(id);

                updateView(UpdateForAction.BtnSummitEdit);
                theSelectedPro=null;
            }
        }
        else{
            System.out.println("No Product Selected");
        }
    }

    void doChangeStockBy(String addOrSub) throws SQLException {
        int oldStock = Integer.parseInt(getView().tfStockEdit.getText().trim());
        int newStock =oldStock;
        String TextChangeBy = getView().tfChangeByEdit.getText().trim();
        if(!TextChangeBy.isEmpty()){
            if(validateInputChangeStockBy(TextChangeBy)==false){
                updateView(UpdateForAction.ShowInputErrorMsg);
            } else{
                int changeBy = Integer.parseInt(TextChangeBy);
                switch(addOrSub){
                    case "add":
                        newStock = oldStock + changeBy;
                        break;
                    case "sub":
                        newStock = oldStock - changeBy;
                        break;
                }
                displayStockEdit = String.valueOf (newStock);
                
                // Check for low stock alert after stock change (preview before submit)
                if(theSelectedPro != null && newStock <= StockAlertService.DEFAULT_LOW_STOCK_THRESHOLD) {
                    String alertMsg = StockAlertService.generateAlertMessage(
                        new Product(theSelectedPro.getProductId(), theSelectedPro.getProductDescription(),
                            theSelectedPro.getProductImageName(), theSelectedPro.getUnitPrice(), newStock));
                    if(alertMsg != null) {
                        // Show warning in alert simulator
                        getAlertSimulator().showErrorMsg("⚠️ Stock Alert Preview:\n\n" + alertMsg);
                    }
                }
                
                updateView(UpdateForAction.BtnChangeStockBy);
            }
        }
    }

    private  boolean validateInputChangeStockBy(String txChangeBy) throws SQLException {
        ValidationService.ValidationResult result = ValidationService.validateStockChangeBy(txChangeBy);
        if (!result.isValid()) {
            displayInputErrorMsg = result.getErrorMessage();
            return false;
        }
        return true;
    }

    private void doSubmitNew() throws SQLException, IOException {
        System.out.println("Adding new Pro in model");

        //all info(input from user) about the new product
        theNewProId = getView().tfIdNewPro.getText().trim();
        String textPrice = getView().tfPriceNewPro.getText().trim();
        String textStock = getView().tfStockNewPro.getText().trim();
        String description = getView().taDescriptionNewPro.getText().trim();
        String iPath = getView().imageUriNewPro; //image Path from the imageChooser in View class

        //validate input using ValidationService
        ValidationService.ValidationResult validationResult = ValidationService.validateNewProduct(
            theNewProId, textPrice, textStock, description, iPath, getDatabaseRW());
        if (!validationResult.isValid()) {
            displayInputErrorMsg = validationResult.getErrorMessage();
            updateView(UpdateForAction.ShowInputErrorMsg);
        } else {
            // Handle image: if user selected an image, copy it; otherwise use default image
            String imageNameWithExtension;
            if (iPath != null && !iPath.trim().isEmpty()) {
                // User selected an image - copy it to project image folder using productId as image name
                // and get the image extension from the source image
                imageNameWithExtension = ImageFileManager.copyFileToDestination(getView().imageUriNewPro, StorageLocation.imageFolder, theNewProId);
            } else {
                // No image selected - use default image name (productId.jpg)
                imageNameWithExtension = theNewProId + ".jpg";
            }
            
            double price = Double.parseDouble(textPrice);
            int stock = Integer.parseInt(textStock);

            //insertNewProduct to databse (String id, String des,double price,String image,int stock)
            //a record in databse looks like ('0001', '40 inch TV', 269.00,'0001.jpg',100)"
            getDatabaseRW().insertNewProduct(theNewProId,description,price,imageNameWithExtension,stock);
            updateView(UpdateForAction.BtnSummitNew);
            theNewProId = null;
        }
    }

    // Note: validateInputEditChild and validateInputNewProChild methods have been removed.
    // Validation logic is now handled by ValidationService to improve separation of concerns.


    private void updateView(UpdateForAction updateFor){
        switch (updateFor) {
            case UpdateForAction.BtnSearch:
                view.updateObservableProductList(productList);
                break;
            case UpdateForAction.BtnEdit:
                getView().updateEditProductChild(getDisplayIdEdit(), getDisplayPriceEdit(), getDisplayStockEdit(), getDisplayDescriptionEdit(), getDisplayImageUrlEdit());
                break;
            case UpdateForAction.BtnDelete:
                getView().updateObservableProductList(productList); //update search page in view
                showManageStockHistory(ManageProductType.Deleted);
                getView().resetEditChild();
                getAlertSimulator().closeAlertSimulatorWindow();//close AlertSimulatorWindow if exists
                break;

            case UpdateForAction.BtnChangeStockBy:
                getView().updateBtnAddSub(getDisplayStockEdit());
                getAlertSimulator().closeAlertSimulatorWindow();//close AlertSimulatorWindow if exists
                break;

            case UpdateForAction.BtnCancelEdit:
                getView().resetEditChild();
                getAlertSimulator().closeAlertSimulatorWindow();//close AlertSimulatorWindow if exists
                break;

            case UpdateForAction.BtnSummitEdit:
                showManageStockHistory(ManageProductType.Edited);
                getView().resetEditChild();
                getAlertSimulator().closeAlertSimulatorWindow();//close AlertSimulatorWindow if exists
                break;

            case UpdateForAction.BtnCancelNew:
                getView().resetNewProChild();
                getAlertSimulator().closeAlertSimulatorWindow();//close AlertSimulatorWindow if exists
                break;

            case UpdateForAction.BtnSummitNew:
                showManageStockHistory(ManageProductType.New );
                getView().resetNewProChild();
                getAlertSimulator().closeAlertSimulatorWindow();//close AlertSimulatorWindow if exists
                break;

            case UpdateForAction.ShowInputErrorMsg:
                getAlertSimulator().showErrorMsg(displayInputErrorMsg);
        }
    }

    private void showManageStockHistory(ManageProductType type){
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String record="";
        switch (type) {
            case ManageProductType.Edited:
                record = theSelectedPro.getProductId() + " edited successfully, " + dateTime;
                break;
            case ManageProductType.Deleted:
                record = theSelectedPro.getProductId() + " deleted successfully, " + dateTime;
                break;
            case ManageProductType.New :
                record = theNewProId + " added to database successfully, " + dateTime;
        }
        if(!record.equals(""))
            displayManageHistory.add(record);
        getHistoryWindow().showManageHistory(displayManageHistory);
    }

    /**
     * Checks a product for low stock conditions and shows an alert if needed.
     * This method is called after stock updates to proactively alert warehouse staff.
     * 
     * @param productId The product ID to check
     */
    private void checkAndShowStockAlert(String productId) {
        try {
            String alertMsg = StockAlertService.checkProductAndGenerateAlert(productId);
            if(alertMsg != null && getAlertSimulator() != null) {
                getAlertSimulator().showErrorMsg(alertMsg);
            }
        } catch (SQLException e) {
            System.err.println("Error checking stock alert for product " + productId + ": " + e.getMessage());
        }
    }

}
