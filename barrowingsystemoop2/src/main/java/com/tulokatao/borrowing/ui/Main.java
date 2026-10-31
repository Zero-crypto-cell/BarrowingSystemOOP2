package com.tulokatao.borrowing.ui;

import com.tulokatao.borrowing.model.*;
import com.tulokatao.borrowing.service.BorrowingSystem;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static final BorrowingSystem system = new BorrowingSystem();

    public static void main(String[] args) {
        System.out.println("=== CCS Item Borrowing System ===");
        while (true) {
            User user = loginScreen();
            if (user == null) break;
            System.out.println("\nWelcome, " + user + "!");
            if (user.isAdmin()) adminMenu(user); else borrowerMenu(user);
        }
        System.out.println("Goodbye!");
    }

    private static User loginScreen() {
        while (true) {
            System.out.print("\nUsername (blank to exit): ");
            String u = in.nextLine().trim();
            if (u.isEmpty()) return null;
            System.out.print("Password: ");
            String p = in.nextLine();
            User user = system.login(u, p);
            if (user != null) return user;
            System.out.println("Invalid username or password.");
        }
    }

    private static void borrowerMenu(User user) {
        while (true) {
            System.out.println("\n--- Borrower Menu ---");
            System.out.println("1. Search available items");
            System.out.println("2. Request to borrow an item");
            System.out.println("3. View my borrowing history");
            System.out.println("0. Log out");
            switch (ask("Choose: ")) {
                case "1" -> searchItems();
                case "2" -> requestBorrow(user);
                case "3" -> printAll(system.getHistory(user), "No borrowing history yet.");
                case "0" -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void adminMenu(User user) {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Search available items");
            System.out.println("2. View all items");
            System.out.println("3. Add a new item");
            System.out.println("4. Update an item");
            System.out.println("5. Remove an item");
            System.out.println("6. Approve or reject a borrow request");
            System.out.println("7. Record the return of an item");
            System.out.println("8. View borrowing history");
            System.out.println("0. Log out");
            switch (ask("Choose: ")) {
                case "1" -> searchItems();
                case "2" -> printAll(system.getAllItems(), "No items yet.");
                case "3" -> run(() -> {
                    Item i = system.addItem(ask("Name: "), ask("Category: "));
                    System.out.println("Added: " + i);
                });
                case "4" -> run(() -> {
                    int id = askInt("Item ID: ");
                    system.updateItem(id, ask("New name (blank = keep): "), ask("New category (blank = keep): "));
                    System.out.println("Item updated.");
                });
                case "5" -> run(() -> {
                    system.removeItem(askInt("Item ID: "));
                    System.out.println("Item removed.");
                });
                case "6" -> handleRequests();
                case "7" -> handleReturn();
                case "8" -> printAll(system.getHistory(user), "No borrowing history yet.");
                case "0" -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void searchItems() {
        List<Item> found = system.searchAvailable(ask("Search (blank = show all): "));
        printAll(found, "No available items match.");
    }

    private static void requestBorrow(User user) {
        run(() -> {
            printAll(system.searchAvailable(""), "No available items right now.");
            int id = askInt("Item ID to borrow: ");
            LocalDate due = LocalDate.parse(ask("Return date (YYYY-MM-DD): "));
            BorrowRequest r = system.requestBorrow(user, id, due);
            System.out.println("Request sent (status: pending). " + r);
        });
    }

    private static void handleRequests() {
        List<BorrowRequest> pending = system.getPendingRequests();
        printAll(pending, "No pending requests.");
        if (pending.isEmpty()) return;
        run(() -> {
            int id = askInt("Request ID: ");
            String choice = ask("(A)pprove or (R)eject? ").toUpperCase();
            if (choice.startsWith("A")) { system.approve(id); System.out.println("Request approved."); }
            else if (choice.startsWith("R")) { system.reject(id); System.out.println("Request rejected."); }
            else System.out.println("Cancelled.");
        });
    }

    private static void handleReturn() {
        List<BorrowRequest> borrowed = system.getBorrowedRecords();
        printAll(borrowed, "No items are currently borrowed.");
        if (borrowed.isEmpty()) return;
        run(() -> {
            system.recordReturn(askInt("Request ID of returned item: "));
            System.out.println("Return recorded. Item is available again.");
        });
    }

    // ---------- helpers ----------
    private static String ask(String prompt) { System.out.print(prompt); return in.nextLine().trim(); }

    private static int askInt(String prompt) {
        while (true) {
            try { return Integer.parseInt(ask(prompt)); }
            catch (NumberFormatException e) { System.out.println("Please enter a number."); }
        }
    }

    private static <T> void printAll(List<T> list, String emptyMsg) {
        if (list.isEmpty()) { System.out.println(emptyMsg); return; }
        list.forEach(System.out::println);
    }

    private static void run(Runnable action) {
        try { action.run(); }
        catch (DateTimeParseException e) { System.out.println("Invalid date format. Use YYYY-MM-DD."); }
        catch (IllegalArgumentException | IllegalStateException e) { System.out.println("Error: " + e.getMessage()); }
    }
}
