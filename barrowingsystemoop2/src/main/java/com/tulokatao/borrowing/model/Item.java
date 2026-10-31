package com.tulokatao.borrowing.model;

public class Item {
    private final int id;
    private String name;
    private String category;
    private ItemStatus status = ItemStatus.AVAILABLE;

    public Item(int id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public boolean isAvailable() { return status == ItemStatus.AVAILABLE; }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public ItemStatus getStatus() { return status; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setStatus(ItemStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("[%d] %-22s %-12s %s", id, name, category, status);
    }
}
