package com.example.fridgeapp;

import com.example.fridgeapp.model.ShoppingItem;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListManager {
    private static ShoppingListManager instance;
    private List<ShoppingItem> shoppingItems;

    private ShoppingListManager() {
        shoppingItems = new ArrayList<>();
    }

    public static ShoppingListManager getInstance() {
        if (instance == null) {
            instance = new ShoppingListManager();
        }
        return instance;
    }

    public List<ShoppingItem> getShoppingItems() {
        return shoppingItems;
    }

    public void addItem(ShoppingItem item) {
        shoppingItems.add(item);
    }

    public void removeItem(ShoppingItem item) {
        shoppingItems.remove(item);
    }

    public void clearItems() {
        shoppingItems.clear();
    }

    public int getItemCount() {
        return shoppingItems.size();
    }
}