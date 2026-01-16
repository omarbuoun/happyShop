# HappyShop Development Plan

## Overview
This plan outlines the development tasks to improve and extend the HappyShop system, addressing:
1. **Completing Existing TODOs** (Critical functionality)
2. **Adding Meaningful New Features** (Business value)
3. **Refactoring for Better OO Design** (Code quality - LO1 & LO3)
4. **Implementing Comprehensive Testing** (Quality assurance)

---

## Phase 1: Complete Existing TODOs (Priority: HIGH)

### 1.1 CustomerModel - Trolley Management
**Location:** `CustomerModel.java` lines 65-82

**Tasks:**
- [ ] **Merge duplicate products** in trolley by product ID (combine quantities)
- [ ] **Sort trolley** by product ID for better organization
- [ ] **Refactor `addToTrolley()`** to use a dedicated `TrolleyManager` service class

**Why:** Currently, adding the same product multiple times creates duplicates. This violates business logic and causes stock calculation errors.

**Implementation:**
```java
// Create TrolleyManager service
public class TrolleyManager {
    public static ArrayList<Product> mergeAndSort(ArrayList<Product> trolley) {
        // Group by ID, merge quantities, sort by ID
    }
}
```

### 1.2 CustomerModel - Insufficient Stock Handling
**Location:** `CustomerModel.java` lines 108-126

**Tasks:**
- [ ] **Remove insufficient stock products** from trolley automatically
- [ ] **Use RemoveProductNotifier** to show user-friendly notification window
- [ ] **Close notifier window** appropriately after user actions

**Why:** Currently shows error in search result label instead of proper notification. Poor UX.

**Implementation:**
- Integrate `RemoveProductNotifier.showRemovalMsg()`
- Remove products from trolley before showing notification
- Update trolley display after removal

### 1.3 CustomerModel - Enhanced Search
**Location:** `CustomerModel.java` line 38

**Tasks:**
- [ ] **Upgrade search** to support both product ID and name (like Warehouse)
- [ ] **Use `databaseRW.searchProduct(keyword)`** instead of `searchByProductId()`

**Why:** Warehouse already has this feature. Customer should have the same capability.

---

## Phase 2: Add Meaningful New Features (Priority: HIGH)

### 2.1 Order Cancellation Feature
**New Functionality:**
- [ ] **Customer can cancel orders** in "Ordered" state
- [ ] **Restore stock** when order is cancelled
- [ ] **Update OrderHub** to handle cancellation state
- [ ] **Add "Cancel Order" button** to OrderTracker window

**Business Value:** Allows customers to change their mind, improves customer satisfaction.

**Files to Create/Modify:**
- `OrderState.java` - Add `Cancelled` state
- `OrderTracker.java` - Add cancel button and handler
- `OrderHub.java` - Add `cancelOrder()` method
- `DatabaseRW.java` - Add `restoreStock()` method

### 2.2 Order History & Analytics
**New Functionality:**
- [ ] **Order History Window** - View all past orders (collected/cancelled)
- [ ] **Order Statistics Dashboard** - Total orders, revenue, popular products
- [ ] **Export Order Reports** - CSV/JSON export functionality

**Business Value:** Provides insights for business decisions, customer service.

**Files to Create:**
- `OrderHistoryService.java` - Service to manage order history
- `OrderAnalytics.java` - Calculate statistics
- `OrderHistoryView.java` - UI for viewing history
- `ReportExporter.java` - Export functionality

### 2.3 Low Stock Alert System
**New Functionality:**
- [ ] **Automatic alerts** when product stock falls below threshold (e.g., 10 units)
- [ ] **Alert notification** in Warehouse window
- [ ] **Alert history** tracking

**Business Value:** Prevents stockouts, improves inventory management.

**Files to Create:**
- `StockAlertService.java` - Monitor and trigger alerts
- `StockAlertView.java` - Display alerts
- Modify `DerbyRW.java` - Check stock levels on updates

### 2.4 Product Categories
**New Functionality:**
- [ ] **Add category field** to Product and database
- [ ] **Category filter** in Customer search
- [ ] **Category management** in Warehouse

**Business Value:** Better product organization, improved user experience.

**Files to Modify:**
- `Product.java` - Add category field
- `SetDatabase.java` - Add category column
- `DerbyRW.java` - Update queries
- `CustomerView.java` - Add category filter UI

### 2.5 Order Status Notifications
**New Functionality:**
- [ ] **Real-time notifications** when order status changes
- [ ] **Sound/visual alerts** for pickers when new orders arrive
- [ ] **Email-style notification** system

**Business Value:** Improves responsiveness, reduces order processing time.

**Files to Create:**
- `NotificationService.java` - Central notification manager
- `NotificationView.java` - Notification display component

---

## Phase 3: Refactoring for Better OO Design (Priority: MEDIUM-HIGH)

### 3.1 Encapsulation Improvements
**Issues:** Public fields in Models (CustomerModel, WarehouseModel, PickerModel)

**Tasks:**
- [ ] **Convert public fields to private** with getters/setters
- [ ] **Use dependency injection** properly
- [ ] **Remove direct View-Model coupling** via public fields

**Files to Refactor:**
- `CustomerModel.java` - Encapsulate `cusView`, `databaseRW`
- `WarehouseModel.java` - Encapsulate `view`, `databaseRW`, `historyWindow`, `alertSimulator`
- `PickerModel.java` - Encapsulate `pickerView`

**OO Principle:** Encapsulation - data hiding and controlled access

### 3.2 Service Layer Introduction
**Issue:** Business logic mixed with Model classes

**Tasks:**
- [ ] **Create Service Layer** to separate business logic from data models
- [ ] **Extract validation logic** into `ValidationService`
- [ ] **Extract trolley operations** into `TrolleyService`
- [ ] **Extract order operations** into `OrderService`

**Files to Create:**
- `service/ValidationService.java` - Centralized validation
- `service/TrolleyService.java` - Trolley business logic
- `service/OrderService.java` - Order business logic
- `service/StockService.java` - Stock management logic

**OO Principle:** Separation of Concerns, Single Responsibility

### 3.3 Observer Pattern Enhancement
**Issue:** OrderHub uses ArrayList for observers, no proper observer interface

**Tasks:**
- [ ] **Create `OrderObserver` interface** for type safety
- [ ] **Refactor OrderTracker and PickerModel** to implement interface
- [ ] **Use proper observer pattern** with interface contracts

**Files to Create/Modify:**
- `orderManagement/OrderObserver.java` - Observer interface
- `OrderHub.java` - Use interface instead of concrete types
- `OrderTracker.java` - Implement interface
- `PickerModel.java` - Implement interface

**OO Principle:** Dependency Inversion, Interface Segregation

### 3.4 Factory Pattern Enhancement
**Issue:** DatabaseRWFactory is simple, could be more flexible

**Tasks:**
- [ ] **Enhance factory** to support configuration-based selection
- [ ] **Add factory for services** (ServiceFactory)
- [ ] **Support dependency injection** through factories

**Files to Modify:**
- `DatabaseRWFactory.java` - Enhanced factory pattern
- `service/ServiceFactory.java` - New factory for services

**OO Principle:** Factory Pattern, Dependency Injection

### 3.5 Exception Handling Strategy
**Issue:** Exceptions thrown but not properly handled, no custom exceptions

**Tasks:**
- [ ] **Create custom exception classes** (OrderException, StockException, etc.)
- [ ] **Implement proper exception handling** with user-friendly messages
- [ ] **Add exception logging** mechanism

**Files to Create:**
- `exception/OrderException.java`
- `exception/StockException.java`
- `exception/DatabaseException.java`
- `exception/ValidationException.java`

**OO Principle:** Error handling, Robustness

### 3.6 Static State Issues
**Issue:** `PickerModel` uses static `lockedOrderIds` - potential concurrency issues

**Tasks:**
- [ ] **Move locking mechanism** to OrderHub (single source of truth)
- [ ] **Use proper synchronization** for order locking
- [ ] **Create `OrderLockManager`** service

**Files to Create:**
- `orderManagement/OrderLockManager.java` - Centralized locking

**OO Principle:** Thread safety, State management

### 3.7 Code Duplication Reduction
**Issues:**
- Similar validation code in WarehouseModel
- Path building code repeated
- Image handling code duplicated

**Tasks:**
- [ ] **Extract common validation** to ValidationService
- [ ] **Create PathBuilder utility** class
- [ ] **Consolidate image handling** logic

**Files to Create:**
- `utility/PathBuilder.java`
- `service/ImageService.java` - Centralized image operations

**OO Principle:** DRY (Don't Repeat Yourself)

---

## Phase 4: Comprehensive Testing (Priority: HIGH)

### 4.1 Unit Tests

#### 4.1.1 Model Tests
- [ ] **CustomerModelTest.java**
  - Test `search()` with valid/invalid product IDs
  - Test `addToTrolley()` with merging and sorting
  - Test `checkOut()` with sufficient/insufficient stock
  - Test `cancel()` clears trolley
  - Test `groupProductsById()` logic

- [ ] **PickerModelTest.java**
  - Test `doProgressing()` locks order correctly
  - Test `doCollected()` unlocks and updates state
  - Test order locking prevents duplicate assignment
  - Test `setOrderMap()` updates display correctly

- [ ] **WarehouseModelTest.java**
  - Test `doSearch()` returns correct products
  - Test `doEdit()` validates input correctly
  - Test `doDelete()` removes product properly
  - Test validation methods (`validateInputEditChild`, etc.)

#### 4.1.2 Service Tests
- [ ] **TrolleyServiceTest.java**
  - Test merging duplicate products
  - Test sorting by product ID
  - Test quantity aggregation

- [ ] **ValidationServiceTest.java**
  - Test price validation (positive, 2 decimals)
  - Test stock validation (non-negative)
  - Test product ID format validation
  - Test description validation

- [ ] **OrderServiceTest.java**
  - Test order creation
  - Test order state transitions
  - Test order cancellation

#### 4.1.3 Utility Tests
- [ ] **OrderCounterTest.java**
  - Test sequential ID generation
  - Test file locking behavior
  - Test concurrent access handling

- [ ] **ProductListFormatterTest.java**
  - Test formatting with various product lists
  - Test total calculation

### 4.2 Integration Tests

- [ ] **OrderHubIntegrationTest.java**
  - Test order lifecycle (Ordered → Progressing → Collected)
  - Test observer notifications
  - Test order file creation and movement
  - Test order map initialization

- [ ] **DatabaseIntegrationTest.java**
  - Test product CRUD operations
  - Test stock purchase with transactions
  - Test concurrent database access
  - Use in-memory database for testing

- [ ] **OrderFileSystemTest.java**
  - Test order file creation
  - Test file movement between states
  - Test order file reading

### 4.3 Mock Testing

- [ ] **CustomerModelTest with Mock DatabaseRW**
  - Mock database responses
  - Test without actual database connection
  - Test error scenarios

- [ ] **OrderHubTest with Mock Observers**
  - Verify observer notifications
  - Test without actual UI components

### 4.4 Test Infrastructure

- [ ] **Create test resources** (`src/test/resources/`)
  - Test database setup
  - Test images
  - Test order files

- [ ] **Test configuration**
  - Separate test database
  - Test data fixtures
  - Test cleanup utilities

---

## Implementation Timeline

### Week 1: Foundation & TODOs
- Complete Phase 1 (TODOs)
- Set up testing infrastructure
- Begin Phase 3.1 (Encapsulation)

### Week 2: Core Features
- Implement Phase 2.1 (Order Cancellation)
- Implement Phase 2.2 (Order History)
- Continue Phase 3 (Refactoring)

### Week 3: Advanced Features & Refactoring
- Implement Phase 2.3 (Stock Alerts)
- Implement Phase 2.4 (Categories)
- Complete Phase 3 (Refactoring)

### Week 4: Testing & Polish
- Complete Phase 4 (Testing)
- Bug fixes
- Documentation
- Final review

---

## Success Criteria

### Functional Requirements
✅ All TODOs completed
✅ At least 3 new meaningful features implemented
✅ System runs without regressions
✅ All new features work correctly

### Code Quality (LO1 & LO3)
✅ Improved encapsulation (no public fields)
✅ Proper separation of concerns (Service layer)
✅ Reduced code duplication
✅ Better error handling
✅ Improved maintainability

### Testing (Required)
✅ Unit tests for all models
✅ Integration tests for core workflows
✅ Test coverage > 70%
✅ All tests pass
✅ Tests demonstrate what/why/how

---

## Notes

- **Prioritize completing TODOs first** - these are critical functionality gaps
- **Test as you develop** - don't leave testing until the end
- **Refactor incrementally** - don't break existing functionality
- **Document design decisions** - explain why changes were made
- **Use Git branches** - one feature per branch for clean history

---

## Files Summary

### New Files to Create (~25 files)
- Services: 5 files
- Exceptions: 4 files
- Tests: 15+ files
- New Features: 5+ files

### Files to Modify (~15 files)
- Models: 3 files
- Views: 3 files
- OrderHub: 1 file
- Database: 2 files
- Others: 6 files

---

**Total Estimated Effort:** 4 weeks (assuming part-time development)



