package com.example.fridgeapp;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.fridgeapp.adapter.ShoppingAdapter;
import com.example.fridgeapp.model.ShoppingItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class shoppingFragment extends Fragment {

    private RecyclerView recyclerView;
    private ShoppingAdapter adapter;
    private FloatingActionButton fabAddItem;
    private TextView tvItemCount;
    private TextView tvEmptyState;

    private List<ShoppingItem> shoppingItems = new ArrayList<>();
    private List<ShoppingItem> inventoryItems = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shoppingfrag, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewShopping);
        fabAddItem = view.findViewById(R.id.fabAddItem);
        tvItemCount = view.findViewById(R.id.tvItemCount);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        setupRecyclerView();
        loadShoppingItems();

        fabAddItem.setOnClickListener(v -> showAddItemDialog());

        return view;
    }

    private void setupRecyclerView() {
        adapter = new ShoppingAdapter(
                shoppingItems,
                (item, isChecked) -> {
                    if (isChecked) {
                        moveToInventory(item);
                    }
                },
                this::deleteItem
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadShoppingItems() {
        if (shoppingItems.isEmpty()) {
            shoppingItems.add(new ShoppingItem(
                    UUID.randomUUID().toString(),
                    "Milk",
                    "2 bottles",
                    false
            ));
            shoppingItems.add(new ShoppingItem(
                    UUID.randomUUID().toString(),
                    "Bread",
                    "1 loaf",
                    false
            ));
        }

        adapter.updateItems(shoppingItems);
        updateItemCount();
        updateEmptyState();
    }

    private void showAddItemDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_shopping_item, null);

        EditText etItemName = dialogView.findViewById(R.id.etItemName);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        Button btnAddItem = dialogView.findViewById(R.id.btnAddItem);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        setupQuickAddButton(dialogView, R.id.btnMilk, "Milk", etItemName);
        setupQuickAddButton(dialogView, R.id.btnEggs, "Eggs", etItemName);
        setupQuickAddButton(dialogView, R.id.btnBread, "Bread", etItemName);
        setupQuickAddButton(dialogView, R.id.btnButter, "Butter", etItemName);
        setupQuickAddButton(dialogView, R.id.btnChicken, "Chicken", etItemName);
        setupQuickAddButton(dialogView, R.id.btnTomatoes, "Tomatoes", etItemName);
        setupQuickAddButton(dialogView, R.id.btnRice, "Rice", etItemName);
        setupQuickAddButton(dialogView, R.id.btnLettuce, "Lettuce", etItemName);
        setupQuickAddButton(dialogView, R.id.btnOil, "Cooking Oil", etItemName);

        btnAddItem.setOnClickListener(v -> {
            String itemName = etItemName.getText().toString().trim();
            String quantity = etQuantity.getText().toString().trim();

            if (itemName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter item name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (quantity.isEmpty()) {
                Toast.makeText(getContext(), "Please enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            addShoppingItem(itemName, quantity);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupQuickAddButton(View dialogView, int buttonId, String itemName, EditText etItemName) {
        Button button = dialogView.findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(v -> etItemName.setText(itemName));
        }
    }

    private void addShoppingItem(String name, String quantity) {
        ShoppingItem newItem = new ShoppingItem(
                UUID.randomUUID().toString(),
                name,
                quantity,
                false
        );

        shoppingItems.add(newItem);
        adapter.updateItems(shoppingItems);
        updateItemCount();
        updateEmptyState();

        Toast.makeText(getContext(), "Item added to shopping list", Toast.LENGTH_SHORT).show();
    }

    private void moveToInventory(ShoppingItem item) {
        inventoryItems.add(item);
        shoppingItems.remove(item);

        adapter.updateItems(shoppingItems);
        updateItemCount();
        updateEmptyState();

        Toast.makeText(getContext(),
                item.getName() + " moved to inventory",
                Toast.LENGTH_SHORT).show();
    }

    private void deleteItem(ShoppingItem item) {
        shoppingItems.remove(item);
        adapter.removeItem(item);
        updateItemCount();
        updateEmptyState();

        Toast.makeText(getContext(), "Item removed", Toast.LENGTH_SHORT).show();
    }

    private void updateItemCount() {
        int count = shoppingItems.size();
        int completedCount = 0;
        for (ShoppingItem item : shoppingItems) {
            if (item.isCompleted()) {
                completedCount++;
            }
        }
        tvItemCount.setText(count + " items to buy • " + completedCount + " completed");
    }

    private void updateEmptyState() {
        if (shoppingItems.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}