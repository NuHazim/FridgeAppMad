package com.example.fridgeapp;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.fridgeapp.adapter.ShoppingAdapter;
import com.example.fridgeapp.model.ShoppingItem;
import java.util.List;
import java.util.UUID;

public class shoppingFragment extends Fragment {

    private static final String TAG = "ShoppingFragment";
    private RecyclerView recyclerView;
    private ShoppingAdapter adapter;
    private LinearLayout btnAddItemBar;
    private TextView tvItemCount;
    private TextView tvCompletedCount;
    private TextView tvEmptyState;

    private ShoppingListManager shoppingListManager;
    private List<ShoppingItem> shoppingItems;
    private boolean isFirstLoad = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        View view = inflater.inflate(R.layout.shoppingfrag, container, false);

        // Get the singleton instance
        shoppingListManager = ShoppingListManager.getInstance();
        shoppingItems = shoppingListManager.getShoppingItems();

        // Find all views
        recyclerView = view.findViewById(R.id.recyclerViewShopping);
        btnAddItemBar = view.findViewById(R.id.btnAddItemBar);
        tvItemCount = view.findViewById(R.id.tvItemCount);
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        Log.d(TAG, "Views initialized");

        // Only add sample items if list is empty on first app launch
        if (shoppingItems.isEmpty() && isFirstLoad) {
            addSampleItems();
            isFirstLoad = false;
        }

        // Setup adapter
        setupAdapter();

        // Setup click listener for add button bar
        btnAddItemBar.setOnClickListener(v -> {
            Log.d(TAG, "Add button clicked");
            showAddItemDialog();
        });

        return view;
    }

    private void addSampleItems() {
        shoppingListManager.addItem(new ShoppingItem(
                UUID.randomUUID().toString(),
                "Milk",
                "2 bottles",
                false
        ));
        shoppingListManager.addItem(new ShoppingItem(
                UUID.randomUUID().toString(),
                "Bread",
                "2 loaves",
                false
        ));
        shoppingListManager.addItem(new ShoppingItem(
                UUID.randomUUID().toString(),
                "Apples",
                "5pcs",
                false
        ));

        Log.d(TAG, "Sample items added. Total items: " + shoppingItems.size());
    }

    private void setupAdapter() {
        Log.d(TAG, "Setting up adapter with " + shoppingItems.size() + " items");

        adapter = new ShoppingAdapter(
                shoppingItems,
                (item, isChecked) -> {
                    Log.d(TAG, "Item checked: " + item.getName());
                    item.setCompleted(isChecked);
                    updateUI();
                    if (isChecked) {
                        Toast.makeText(getContext(), item.getName() + " marked as completed!", Toast.LENGTH_SHORT).show();
                    }
                },
                item -> {
                    Log.d(TAG, "Item deleted: " + item.getName());
                    shoppingListManager.removeItem(item);
                    adapter.notifyDataSetChanged();
                    updateUI();
                    Toast.makeText(getContext(), "Item removed", Toast.LENGTH_SHORT).show();
                }
        );

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setAdapter(adapter);

        updateUI();

        Log.d(TAG, "Adapter setup complete");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            updateUI();
        }
        Log.d(TAG, "onResume - Items: " + shoppingItems.size());
    }

    private void showAddItemDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_shopping_item, null);

        EditText etItemName = dialogView.findViewById(R.id.etItemName);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        Button btnAddItem = dialogView.findViewById(R.id.btnAddItem);
        TextView btnClose = dialogView.findViewById(R.id.btnClose);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        // Close button
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Quick add buttons
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

            addItem(itemName, quantity);
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

    private void addItem(String name, String quantity) {
        Log.d(TAG, "Adding item: " + name);

        ShoppingItem newItem = new ShoppingItem(
                UUID.randomUUID().toString(),
                name,
                quantity,
                false
        );

        shoppingListManager.addItem(newItem);
        adapter.notifyDataSetChanged();
        updateUI();

        Toast.makeText(getContext(), "Item added!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Total items now: " + shoppingItems.size());
    }

    private void updateUI() {
        int count = shoppingItems.size();
        int completedCount = 0;

        for (ShoppingItem item : shoppingItems) {
            if (item.isCompleted()) {
                completedCount++;
            }
        }

        tvItemCount.setText(count + " items to buy");
        tvCompletedCount.setText(completedCount + " completed");

        if (count == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "UI updated. Items: " + count + ", Completed: " + completedCount);
    }
}