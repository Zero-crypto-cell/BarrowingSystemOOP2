package com.tulokatao.borrowing.service;

import com.tulokatao.borrowing.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Holds all the data and the business rules of the Item Borrowing System. */
public class BorrowingSystem {
    private final List<User> users = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final List<BorrowRequest> requests = new ArrayList<>();
    private int nextItemId = 1;
    private int nextRequestId = 1;

    public BorrowingSystem() {
        users.add(new User(1, "admin", "admin123", "CCS Admin", Role.ADMIN));
        users.add(new User(2, "student1", "pass123", "Juan Dela Cruz", Role.BORROWER));
        users.add(new User(3, "student2", "pass123", "Maria Santos", Role.BORROWER));
        addItem("Epson Projector", "Projector");
        addItem("Extension Cord 5m", "Electrical");
        addItem("HDMI Cable 3m", "Cable");
        addItem("Multimeter", "Lab tool");
    }

    // Feature 1: Log in
    public User login(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.checkPassword(password)) return u;
        }
        return null;
    }

    // Feature 2: Search for available items (blank keyword = all available)
    public List<Item> searchAvailable(String keyword) {
        String k = keyword.trim().toLowerCase();
        return items.stream()
                .filter(Item::isAvailable)
                .filter(i -> i.getName().toLowerCase().contains(k)
                          || i.getCategory().toLowerCase().contains(k))
                .collect(Collectors.toList());
    }

    public List<Item> getAllItems() { return new ArrayList<>(items); }

    public Item findItem(int id) {
        return items.stream().filter(i -> i.getId() == id).findFirst().orElse(null);
    }

    // Feature 3: Request to borrow an item
    public BorrowRequest requestBorrow(User borrower, int itemId, LocalDate dueDate) {
        Item item = findItem(itemId);
        if (item == null) throw new IllegalArgumentException("Item not found.");
        if (!item.isAvailable()) throw new IllegalStateException("Item is not available.");
        if (!dueDate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Return date must be after today.");
        BorrowRequest r = new BorrowRequest(nextRequestId++, item, borrower, dueDate);
        requests.add(r);
        return r;
    }

    // Feature 4: View borrowing history (admin sees all, borrower sees own)
    public List<BorrowRequest> getHistory(User user) {
        return requests.stream()
                .filter(r -> user.isAdmin() || r.getBorrower().getId() == user.getId())
                .collect(Collectors.toList());
    }

    // Feature 5: Add a new item
    public Item addItem(String name, String category) {
        if (name.isBlank()) throw new IllegalArgumentException("Name is required.");
        Item item = new Item(nextItemId++, name.trim(), category.trim());
        items.add(item);
        return item;
    }

    // Feature 6: Update or remove an item
    public void updateItem(int id, String name, String category) {
        Item item = findItem(id);
        if (item == null) throw new IllegalArgumentException("Item not found.");
        if (!name.isBlank()) item.setName(name.trim());
        if (!category.isBlank()) item.setCategory(category.trim());
    }

    public void removeItem(int id) {
        Item item = findItem(id);
        if (item == null) throw new IllegalArgumentException("Item not found.");
        if (!item.isAvailable()) throw new IllegalStateException("Cannot remove a borrowed item.");
        items.remove(item);
    }

    // Feature 7: Approve or reject a borrow request
    public List<BorrowRequest> getPendingRequests() {
        return requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .collect(Collectors.toList());
    }

    private BorrowRequest findRequest(int id) {
        return requests.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    public void approve(int requestId) {
        BorrowRequest r = findRequest(requestId);
        if (r == null || r.getStatus() != RequestStatus.PENDING)
            throw new IllegalArgumentException("No such pending request.");
        if (!r.getItem().isAvailable())
            throw new IllegalStateException("Item is no longer available.");
        r.setStatus(RequestStatus.APPROVED);
        r.getItem().setStatus(ItemStatus.BORROWED);
        // other pending requests for the same item can no longer be granted
        for (BorrowRequest other : requests) {
            if (other != r && other.getItem() == r.getItem()
                    && other.getStatus() == RequestStatus.PENDING) {
                other.setStatus(RequestStatus.REJECTED);
            }
        }
    }

    public void reject(int requestId) {
        BorrowRequest r = findRequest(requestId);
        if (r == null || r.getStatus() != RequestStatus.PENDING)
            throw new IllegalArgumentException("No such pending request.");
        r.setStatus(RequestStatus.REJECTED);
    }

    // Feature 8: Record the return of an item
    public List<BorrowRequest> getBorrowedRecords() {
        return requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.APPROVED)
                .collect(Collectors.toList());
    }

    public void recordReturn(int requestId) {
        BorrowRequest r = findRequest(requestId);
        if (r == null || r.getStatus() != RequestStatus.APPROVED)
            throw new IllegalArgumentException("No such active borrow record.");
        r.setStatus(RequestStatus.RETURNED);
        r.setReturnDate(LocalDate.now());
        r.getItem().setStatus(ItemStatus.AVAILABLE);
    }
}
