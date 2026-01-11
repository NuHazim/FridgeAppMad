package com.example.fridgeapp.model;

public class ShoppingItem {
    public String id;
    public String name;
    public String category;
    public int quantityNumber;
    public String unit;
    public boolean isCompleted;

    // Empty constructor for Firebase
    public ShoppingItem() {
    }

    public ShoppingItem(String id, String name, String category, int quantityNumber, String unit, boolean isCompleted) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantityNumber = quantityNumber;
        this.unit = unit;
        this.isCompleted = isCompleted;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantityNumber() {
        return quantityNumber;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setQuantityNumber(int quantityNumber) {
        this.quantityNumber = quantityNumber;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}