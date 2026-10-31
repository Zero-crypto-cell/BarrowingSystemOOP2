package com.tulokatao.borrowing.model;

import java.time.LocalDate;

public class BorrowRequest {
    private final int id;
    private final Item item;
    private final User borrower;
    private final LocalDate requestDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private RequestStatus status = RequestStatus.PENDING;

    public BorrowRequest(int id, Item item, User borrower, LocalDate dueDate) {
        this.id = id;
        this.item = item;
        this.borrower = borrower;
        this.requestDate = LocalDate.now();
        this.dueDate = dueDate;
    }

    public int getId() { return id; }
    public Item getItem() { return item; }
    public User getBorrower() { return borrower; }
    public LocalDate getRequestDate() { return requestDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    @Override
    public String toString() {
        return String.format("Request #%d | %s | by %s | requested %s | due %s | returned %s | %s",
                id, item.getName(), borrower.getFullName(), requestDate, dueDate,
                returnDate == null ? "-" : returnDate.toString(), status);
    }
}
