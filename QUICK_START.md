# Quick Start Development Guide

## 🎯 Immediate Next Steps

### Step 1: Complete TODOs (Start Here!)
These are **critical functionality gaps** that must be fixed first.

1. **CustomerModel.addToTrolley()** - Merge & Sort
   - File: `src/main/java/ci553/happyshop/client/customer/CustomerModel.java`
   - Lines: 65-82
   - Action: Create `TrolleyService` to merge duplicates and sort by ID

2. **CustomerModel.checkOut()** - Insufficient Stock Handling
   - File: Same as above
   - Lines: 108-126
   - Action: Use `RemoveProductNotifier` to show proper notification

3. **CustomerModel.search()** - Enhanced Search
   - File: Same as above
   - Line: 38
   - Action: Use `databaseRW.searchProduct()` instead of `searchByProductId()`

### Step 2: Add First New Feature
**Order Cancellation** - High business value, moderate complexity

- Add `Cancelled` to `OrderState` enum
- Add `cancelOrder()` to `OrderHub`
- Add cancel button to `OrderTracker`
- Implement stock restoration in `DatabaseRW`

### Step 3: Start Refactoring
**Encapsulation** - Quick wins, improves code quality

- Convert public fields to private in:
  - `CustomerModel` (cusView, databaseRW)
  - `WarehouseModel` (view, databaseRW, etc.)
  - `PickerModel` (pickerView)

### Step 4: Begin Testing
**Test Infrastructure** - Set up early

- Create `src/test/java` directory structure
- Create test database setup
- Write first unit test for `CustomerModel.search()`

---

## 📋 Priority Order

1. ✅ **Phase 1: TODOs** (Week 1)
2. ✅ **Phase 4: Testing Setup** (Week 1 - parallel)
3. ✅ **Phase 2: Order Cancellation** (Week 1-2)
4. ✅ **Phase 3: Encapsulation** (Week 2)
5. ✅ **Phase 2: Order History** (Week 2)
6. ✅ **Phase 3: Service Layer** (Week 3)
7. ✅ **Phase 2: Stock Alerts** (Week 3)
8. ✅ **Phase 4: Complete Testing** (Week 4)

---

## 🛠️ Development Workflow

### For Each Feature/Task:

1. **Create a branch:**
   ```bash
   git checkout -b feature/order-cancellation
   ```

2. **Write tests first** (TDD approach):
   - Create test file
   - Write failing test
   - Implement feature
   - Make test pass

3. **Refactor:**
   - Improve code structure
   - Remove duplication
   - Add documentation

4. **Commit:**
   ```bash
   git add .
   git commit -m "feat: Add order cancellation feature"
   ```

5. **Merge:**
   ```bash
   git checkout main
   git merge feature/order-cancellation
   ```

---

## 📝 Testing Checklist

For each class/feature, ensure:

- [ ] Unit tests cover main methods
- [ ] Edge cases are tested
- [ ] Error scenarios are tested
- [ ] Integration tests for workflows
- [ ] Tests are documented (what/why/how)

---

## 🎓 Assessment Alignment

### LO1: Object-Oriented Design
- ✅ Encapsulation (private fields, getters/setters)
- ✅ Inheritance (if applicable)
- ✅ Polymorphism (interfaces, abstract classes)
- ✅ Design patterns (Observer, Factory, Service)

### LO3: Code Quality
- ✅ Separation of concerns (Service layer)
- ✅ DRY principle (no duplication)
- ✅ SOLID principles
- ✅ Maintainability

### Testing Requirements
- ✅ Unit tests (JUnit)
- ✅ Integration tests
- ✅ Test documentation
- ✅ Coverage > 70%

---

## 📚 Key Files Reference

### Models (Business Logic)
- `CustomerModel.java` - Customer operations
- `PickerModel.java` - Picker operations  
- `WarehouseModel.java` - Warehouse operations

### Core Systems
- `OrderHub.java` - Order management (Singleton)
- `OrderCounter.java` - ID generation
- `DatabaseRW.java` - Database interface

### Views (UI)
- `CustomerView.java` - Customer UI
- `PickerView.java` - Picker UI
- `WarehouseView.java` - Warehouse UI

### Utilities
- `ProductListFormatter.java` - Formatting
- `StorageLocation.java` - Path constants

---

## 🚨 Common Issues & Solutions

### Issue: Public fields causing coupling
**Solution:** Convert to private with getters/setters, use dependency injection

### Issue: Business logic in Model classes
**Solution:** Extract to Service layer (TrolleyService, OrderService, etc.)

### Issue: No error handling
**Solution:** Create custom exceptions, implement try-catch with user messages

### Issue: Static state in PickerModel
**Solution:** Move locking to OrderHub, use proper synchronization

### Issue: Code duplication
**Solution:** Extract common logic to utility classes or services

---

## 📊 Progress Tracking

Use the todo list in your IDE or track in `DEVELOPMENT_PLAN.md`.

Mark tasks as:
- ✅ Complete
- 🔄 In Progress  
- ⏳ Pending

---

## 💡 Tips

1. **Start small** - Complete one TODO before moving to next
2. **Test frequently** - Run tests after each change
3. **Commit often** - Small, focused commits
4. **Document decisions** - Explain why you made changes
5. **Refactor incrementally** - Don't break existing functionality

---

**Good luck with your development! 🚀**



