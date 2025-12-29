package com.example.fridgeapp.model;

public class ShoppingItem {
    private String id;
    private String name;
    private String quantity;
    private boolean isCompleted;
    private long timestamp;

    public ShoppingItem() {
        this.timestamp = System.currentTimeMillis();
    }

    public ShoppingItem(String id, String name, String quantity, boolean isCompleted) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.isCompleted = isCompleted;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}