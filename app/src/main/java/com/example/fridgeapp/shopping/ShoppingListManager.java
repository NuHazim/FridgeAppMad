package com.example.fridgeapp.shopping;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fridgeapp.model.ShoppingItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListManager {

    private static ShoppingListManager instance;
    private final String PREF_NAME = "shopping_list_pref";
    private final String KEY_LIST = "shopping_items";

    private List<ShoppingItem> shoppingItems;
    private SharedPreferences preferences;
    private Gson gson = new Gson();

    private ShoppingListManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        loadItems();
    }

    public static ShoppingListManager getInstance(Context context) {
        if (instance == null) {
            instance = new ShoppingListManager(context);
        }
        return instance;
    }

    public List<ShoppingItem> getShoppingItems() {
        return shoppingItems;
    }

    public void addItem(ShoppingItem item) {
        shoppingItems.add(item);
        saveItems();
    }

    public void removeItem(ShoppingItem item) {
        shoppingItems.remove(item);
        saveItems();
    }

    public void clearItems() {
        shoppingItems.clear();
        saveItems();
    }

    private void saveItems() {
        String json = gson.toJson(shoppingItems);
        preferences.edit().putString(KEY_LIST, json).apply();
    }

    private void loadItems() {
        String json = preferences.getString(KEY_LIST, null);

        if (json == null) {
            shoppingItems = new ArrayList<>();
            return;
        }

        Type type = new TypeToken<List<ShoppingItem>>() {}.getType();
        shoppingItems = gson.fromJson(json, type);

        if (shoppingItems == null)
            shoppingItems = new ArrayList<>();
    }
}
