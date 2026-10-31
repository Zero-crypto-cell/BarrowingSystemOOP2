package com.tulokatao.borrowing.ui;

import com.tulokatao.borrowing.model.BorrowRequest;
import com.tulokatao.borrowing.model.Item;
import com.tulokatao.borrowing.model.User;
import com.tulokatao.borrowing.service.BorrowingSystem;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final BorrowingSystem borrowingSystem = new BorrowingSystem();

    public static void main(String[] args) {
        printBanner();
        
        while (true) {
            User currentUser = loginScreen();
            if (currentUser == null) {
                break;
            }
            
            System.out.println("\nWelcome, " + currentUser.getUsername() + "!");
            if (currentUser.isAdmin()) {
                adminMenu(currentUser);
            } else {
                borrowerMenu(currentUser);
            }
        }
        
        System.out.println("\nThank you for using the CCS Item Borrowing System. Goodbye!");
    }

    private static void printBanner() {
        System.out.println("=========================================");
        System.out.println("    CCS ITEM BORROWING SYSTEM CONSOLE    ");
        System.out.println("=========================================");
    }

    private static User loginScreen() {
        while (true) {
            System.out.println("\n--- LOGIN SCREEN ---");
            String username = ask("Username (leave blank to exit): ");
            if (username.isEmpty()) {
                return null;
            }
            
            String password = ask("Password: ");
            User user = borrowingSystem.login(username, password);
            
            if (user != null) {
                return user;
            }
            System.out.println("❌ Invalid username or password. Please try again.");
        }
    }

    private static void borrowerMenu(User user) {
        while (true) {
            printMenuHeader("Borrower Menu");
            System.out.println("1. Search available items");
            System.out.println("2. Request to borrow an item");
            System.out.println("3. View my borrowing history");
            System.out.println("0. Log out");
            
            String choice = ask("Choose an option: ");
            switch (choice) {
                case "1" -> searchItems();
                case "2" -> requestBorrow(user);
                case "3" -> printAll(borrowingSystem.getHistory(user), "No borrowing history found.");
                case "0" -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("❌ Invalid choice. Please select from 0 to 3.");
            }
        }
    }

    private static void adminMenu(User user) {
        while (true) {
            printMenuHeader("Admin Menu");
            System.out.println("1. Search available items");
            System.out.println("2. View all items");
            System.out.println("3. Add a new item");
            System.out.println("4. Update an item");
            System.out.println("5. Remove an item");
            System.out.println("6. Approve or reject a borrow request");
            System.out.println("7. Record the return of an item");
            System.out.println("8. View all borrowing history");
            System.out.println("0. Log out");
            
            String choice = ask("Choose an option: ");
            switch (choice) {
                case "1" -> searchItems();
                case "2" -> printAll(borrowingSystem.getAllItems(), "No items registered yet.");
                case "3" -> runSafe(() -> {
                    String name = ask("Item Name: ");
                    String category = ask("Category: ");
                    Item item = borrowingSystem.addItem(name, category);
                    System.out.println("✅ Successfully added: " + item);
                });
                case "4" -> runSafe(() -> {
                    int id = askInt("Item ID to update: ");
                    String newName = ask("New name (leave blank to keep current): ");
                    String newCategory = ask("New category (leave blank to keep current): ");
                    borrowingSystem.updateItem(id, newName, newCategory);
                    System.out.println("✅ Item updated successfully.");
                });
                case "5" -> runSafe(() -> {
                    int id = askInt("Item ID to remove: ");
                    borrowingSystem.removeItem(id);
                    System.out.println("✅ Item removed successfully.");
                });
                case "6" -> handleRequests();
                case "7" -> handleReturn();
                case "8" -> printAll(borrowingSystem.getHistory(user), "No history records found.");
                case "0" -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("❌ Invalid choice. Please select from 0 to 8.");
            }
        }
    }

    private static void searchItems() {
        String query = ask("Search query (leave blank to show all available): ");
        List<Item> foundItems = borrowingSystem.searchAvailable(query);
        printAll(foundItems, "No available items match your search.");
    }

    private static void requestBorrow(User user) {
        runSafe(() -> {
            printAll(borrowingSystem.searchAvailable(""), "No available items right now.");
            int itemId = askInt("Enter Item ID to borrow: ");
            LocalDate dueDate = askDate("Return date (YYYY-MM-DD): ");
            
            BorrowRequest request = borrowingSystem.requestBorrow(user, itemId, dueDate);
            System.out.println("✅ Request sent successfully (Status: Pending). Details: " + request);
        });
    }

    private static void handleRequests() {
        List<BorrowRequest> pendingRequests = borrowingSystem.getPendingRequests();
        printAll(pendingRequests, "No pending requests to review.");
        if (pendingRequests.isEmpty()) return;

        runSafe(() -> {
            int requestId = askInt("Enter Request ID to process: ");
            String action = ask("Action - (A)pprove or (R)eject?: ").toUpperCase();
            
            if (action.startsWith("A")) {
                borrowingSystem.approve(requestId);
                System.out.println("✅ Request approved successfully.");
            } else if (action.startsWith("R")) {
                borrowingSystem.reject(requestId);
                System.out.println("❌ Request rejected.");
            } else {
                System.out.println("Operation cancelled.");
            }
        });
    }

    private static void handleReturn() {
        List<BorrowRequest> activeBorrows = borrowingSystem.getBorrowedRecords();
        printAll(activeBorrows, "No items are currently checked out.");
        if (activeBorrows.isEmpty()) return;

        runSafe(() -> {
            int requestId = askInt("Enter Request ID of the returned item: ");
            borrowingSystem.recordReturn(requestId);
            System.out.println("✅ Return recorded successfully. Item is available again.");
        });
    }

    // ==================== UTILITY / HELPER METHODS ====================

    private static String ask(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int askInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(ask(prompt));
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid integer number.");
            }
        }
    }

    private static LocalDate askDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(ask(prompt));
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    private static <T> void printAll(List<T> list, String emptyMessage) {
        System.out.println("\n-----------------------------------------");
        if (list == null || list.isEmpty()) {
            System.out.println(emptyMessage);
        } else {
            list.forEach(System.out::println);
        }
        System.out.println("-----------------------------------------");
    }

    private static void printMenuHeader(String title) {
        System.out.println("\n=== " + title + " ===");
    }

    private static void runSafe(Runnable action) {
        try {
            action.run();
        } catch (DateTimeParseException e) {
            System.out.println("❌ Error: Invalid date format. Please use YYYY-MM-DD.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("❌ Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ An unexpected error occurred: " + e.getMessage());
        }
    }
}