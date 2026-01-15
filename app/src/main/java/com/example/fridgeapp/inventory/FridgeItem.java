package com.example.fridgeapp.inventory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FridgeItem {
    public String docId;
    public String name;
    public String category;
    public String expiry;
    public int quantity;
    public String unit;

    public FridgeItem(){
    }

    public FridgeItem(String docId, String name, String category, String expiry, int quantity, String unit) {
        this.docId = docId;
        this.name = name;
        this.category = category;
        this.expiry = expiry;
        this.quantity = quantity;
        this.unit = unit;
    }

    // ADD THESE GETTER METHODS:
    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getCategory(){return category;}

    public long getDaysLeft() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expiryDate = sdf.parse(expiry);
            Date today = new Date();
            long diff = expiryDate.getTime() - today.getTime();
            return TimeUnit.MILLISECONDS.toDays(diff);
        } catch (Exception e) {
            return 0;
        }
    }
}