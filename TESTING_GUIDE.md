# HappyShop Application - Complete Testing Guide

This guide provides step-by-step instructions for testing all the features and improvements that have been implemented in the HappyShop application.

## 🚀 Starting the Application

The application should launch automatically with multiple windows:
- **Customer Client** windows (2 instances)
- **Picker Client** windows (2 instances)
- **Order Tracker** windows (2 instances)
- **Warehouse Client** windows (2 instances)
- **Order History View** window
- **Emergency Exit** window

---

## 📋 Testing Checklist

### Phase 1: Core Functionality Improvements

#### ✅ Test 1: Trolley Management - Merge Duplicate Products

**What to Test:** When adding the same product multiple times, quantities should be merged and products sorted by ID.

**Steps:**
1. In a **Customer Client** window:
   - Search for a product (e.g., enter "0001" in the ID field and click 🔍)
   - Click "Add to Trolley"
   - Search for the same product again (e.g., "0001")
   - Click "Add to Trolley" again (multiple times if desired)
   - **Expected Result:** The trolley should show only ONE entry for product "0001" with the combined quantity (e.g., if added 3 times with quantity 1 each, it should show quantity 3)
   - **Expected Result:** Products in the trolley should be sorted by product ID in ascending order

**Verification:**
- Check the trolley display on the right side of the Customer window
- Products should be merged (no duplicates)
- Products should be sorted by ID (0001, 0002, 0003, etc.)

---

#### ✅ Test 2: Insufficient Stock Handling

**What to Test:** When checking out with insufficient stock, products should be removed and a notification window should appear.

**Steps:**
1. In a **Customer Client** window:
   - Add products to trolley (ensure you have some products)
   - In a **Warehouse Client** window, reduce stock of one of the products to a low amount (e.g., set stock to 1)
   - Go back to **Customer Client**
   - Click "Check Out"
   - **Expected Result:** A notification window should appear showing which products were removed due to insufficient stock
   - **Expected Result:** The trolley should be updated to remove insufficient products
   - **Expected Result:** A message should appear in the search result area

**Verification:**
- Notification window should show product ID, description, available stock, and requested quantity
- Trolley should no longer contain the insufficient products
- Search result area should show a message about removed products

---

#### ✅ Test 3: Enhanced Search (ID and Name)

**What to Test:** Search should work by both product ID and product name.

**Steps:**
1. **Search by Product ID:**
   - In a **Customer Client** window
   - Enter a product ID in the "Product ID" field (e.g., "0001")
   - Click 🔍
   - **Expected Result:** Product should be found and displayed

2. **Search by Product Name:**
   - Clear the ID field
   - Enter a product name in the "Name" field (e.g., "TV" or "Radio")
   - Click 🔍
   - **Expected Result:** Products matching the name should be found

3. **Priority Test:**
   - Enter an ID in the "Product ID" field (e.g., "0001")
   - Enter a different name in the "Name" field (e.g., "Radio")
   - Click 🔍
   - **Expected Result:** Search should prioritize ID over name (should find product 0001, not search by "Radio")

**Verification:**
- Search results should appear in the left panel
- Product information should display correctly
- ID search should take priority over name search

---

### Phase 2: New Features

#### ✅ Test 4: Order Cancellation

**What to Test:** Customers can cancel orders that are in "Ordered" state, and stock should be restored.

**Steps:**
1. **Create an Order:**
   - In a **Customer Client** window, add products to trolley
   - Click "Check Out"
   - Note the Order ID from the receipt

2. **Cancel the Order:**
   - In an **Order Tracker** window
   - Enter the Order ID in the "Order ID" field
   - Click "Cancel Order"
   - **Expected Result:** Order should be cancelled successfully
   - **Expected Result:** Order should disappear from the Order Tracker list
   - **Expected Result:** Stock should be restored (verify in Warehouse Client)

3. **Verify Stock Restoration:**
   - In a **Warehouse Client** window
   - Search for one of the products that was in the cancelled order
   - **Expected Result:** Stock quantity should be restored (increased by the ordered quantity)

4. **Test Cancellation Restrictions:**
   - Try to cancel an order that is in "Progressing" state (after a picker claims it)
   - **Expected Result:** Cancellation should fail with an appropriate message
   - **Expected Result:** Only "Ordered" orders can be cancelled

**Verification:**
- Order Tracker should show the order state
- Cancelled orders should move to cancelled folder
- Stock quantities should be restored correctly
- Error messages should appear for invalid cancellation attempts

---

#### ✅ Test 5: Order History & Analytics

**What to Test:** View historical orders, statistics, and export functionality.

**Steps:**
1. **View Order History:**
   - Open the **Order History View** window
   - **Expected Result:** Should display:
     - Total number of orders
     - Total revenue
     - Cancellation rate
     - List of all historical orders (collected and cancelled)

2. **Refresh Data:**
   - Create some new orders and complete them
   - Click "Refresh" button in Order History View
   - **Expected Result:** Statistics and order list should update

3. **Export to CSV:**
   - Click "Export to CSV" button
   - Choose a location to save the file
   - **Expected Result:** CSV file should be created with order data
   - Open the CSV file and verify it contains order information

4. **Export to JSON:**
   - Click "Export to JSON" button
   - Choose a location to save the file
   - **Expected Result:** JSON file should be created with order data
   - Open the JSON file and verify it contains order information

**Verification:**
- Statistics should be accurate
- Order list should show all past orders
- Exported files should be readable and contain correct data
- Dates and times should be formatted correctly

---

#### ✅ Test 6: Low Stock Alert System

**What to Test:** Automatic and manual stock alerts when products are low on stock.

**Steps:**
1. **Automatic Alert After Stock Update:**
   - In a **Warehouse Client** window
   - Search for a product
   - Click "Edit"
   - Change the stock to 5 (below threshold of 10)
   - Click "Submit"
   - **Expected Result:** An alert window should appear showing low stock warning

2. **Automatic Alert After Adding New Product:**
   - In **Warehouse Client**
   - Click the dropdown to switch to "Add New Product"
   - Add a new product with stock of 3
   - Click "Submit"
   - **Expected Result:** Alert should appear for low stock

3. **Stock Change Preview Alert:**
   - In **Warehouse Client**
   - Search and edit a product
   - In the "Change Stock By" field, enter a value that would make stock ≤ 10
   - Click ➕ or ➖ to apply the change
   - **Expected Result:** A preview alert should appear before submitting

4. **Manual Low Stock Check:**
   - In **Warehouse Client**
   - Click the "⚠️ Check Low Stock" button
   - **Expected Result:** An alert window should appear showing:
     - All products with low stock (≤ 10)
     - All products that are out of stock (0)
     - Or a message saying "All products have sufficient stock" if none are low

5. **Test Out of Stock Alert:**
   - In **Warehouse Client**
   - Edit a product and set stock to 0
   - Click "Submit"
   - **Expected Result:** Alert should show "OUT OF STOCK" message

**Verification:**
- Alerts should appear automatically after stock operations
- Manual check should scan all products
- Alert messages should be clear and informative
- Threshold is set to 10 units by default

---

### Phase 3: Refactoring Improvements

#### ✅ Test 7: Encapsulation (No Direct Field Access)

**What to Test:** Verify that encapsulation is working (this is mostly internal, but you can verify the application still works correctly).

**Steps:**
- All previous tests should work correctly
- The application should behave the same as before
- No errors should occur related to field access

**Verification:**
- Application runs without errors
- All features work as expected
- No compilation errors

---

#### ✅ Test 8: Service Layer Integration

**What to Test:** Verify that service layer is being used correctly.

**Steps:**
1. **Validation Service:**
   - In **Warehouse Client**
   - Try to add a product with invalid ID (e.g., "001" instead of "0001")
   - **Expected Result:** Error message should appear with validation errors
   - Try to add product with invalid price (e.g., "100.123" - too many decimals)
   - **Expected Result:** Validation error should appear
   - Try to add product with negative stock
   - **Expected Result:** Validation error should appear

2. **Trolley Service:**
   - In **Customer Client**
   - Add same product multiple times
   - **Expected Result:** Products should be merged and sorted (as tested in Test 1)

3. **Stock Alert Service:**
   - Test as described in Test 6

**Verification:**
- Validation errors should be clear and specific
- Services should work seamlessly
- Error messages should be user-friendly

---

#### ✅ Test 9: Observer Pattern Enhancement

**What to Test:** Verify that OrderHub properly notifies all observers.

**Steps:**
1. **Create an Order:**
   - In **Customer Client**, create an order
   - **Expected Result:** 
     - Order should appear in **Order Tracker** window(s)
     - Order should appear in **Picker Client** window(s) (only if in "Ordered" or "Progressing" state)

2. **Change Order State:**
   - In **Picker Client**, click "Progressing" to claim an order
   - **Expected Result:**
     - Order should update in **Order Tracker** (state changes to "Progressing")
     - Order should update in other **Picker Client** windows
     - Order should be locked (other pickers can't claim it)

3. **Complete Order:**
   - In **Picker Client**, click "Collected" when order is ready
   - **Expected Result:**
     - Order should update in **Order Tracker** (state changes to "Collected")
     - Order should disappear from **Picker Client** windows after 10 seconds
     - Order should appear in **Order History View**

**Verification:**
- All observers should receive updates simultaneously
- Order states should be consistent across all windows
- Locking should prevent multiple pickers from claiming the same order

---

#### ✅ Test 10: Order Locking (Thread Safety)

**What to Test:** Verify that order locking prevents race conditions.

**Steps:**
1. **Create Multiple Orders:**
   - Create 2-3 orders from different Customer Client windows

2. **Multiple Pickers Claim Orders:**
   - In **Picker Client 1**, click "Progressing"
   - In **Picker Client 2**, click "Progressing"
   - **Expected Result:** Each picker should get a different order
   - **Expected Result:** No two pickers should get the same order

3. **Verify Locking:**
   - After a picker claims an order, try to claim the same order from another picker
   - **Expected Result:** The second picker should get a different order (the first one is locked)

**Verification:**
- Orders should be distributed correctly
- No duplicate order assignments
- Locking mechanism should work properly

---

## 🧪 Running Automated Tests

To run the automated test suite:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
.\mvnw.cmd test
```

This will run all unit tests and integration tests, including:
- TrolleyService tests
- ValidationService tests
- StockAlertService tests
- CustomerModel tests
- PickerModel tests
- WarehouseModel tests
- OrderHub integration tests

---

## 📊 Test Results Summary

After completing all tests, you should verify:

✅ **Trolley Management:** Products merge and sort correctly  
✅ **Insufficient Stock:** Notifications appear and products are removed  
✅ **Enhanced Search:** Works by both ID and name  
✅ **Order Cancellation:** Orders can be cancelled and stock restored  
✅ **Order History:** Statistics and export work correctly  
✅ **Low Stock Alerts:** Automatic and manual alerts function properly  
✅ **Observer Pattern:** All windows update correctly  
✅ **Order Locking:** Thread-safe order assignment works  

---

## 🐛 Troubleshooting

**If the application doesn't start:**
- Check that JAVA_HOME is set correctly
- Ensure all dependencies are downloaded (run `.\mvnw.cmd clean compile` first)
- Check that no other instance is running

**If tests fail:**
- Ensure the database is initialized (run `SetDatabase` if needed)
- Check that order file system is set up (run `SetOrderFileSystem` if needed)

**If windows don't appear:**
- Check that JavaFX is properly configured
- Verify module-info.java exports are correct

---

## 📝 Notes

- The application uses an embedded Derby database
- Order files are stored in the `orders/` directory
- Images are stored in the `images/` directory
- All windows can be moved and resized independently
- Multiple instances of each client type can run simultaneously

---

**Happy Testing! 🎉**

