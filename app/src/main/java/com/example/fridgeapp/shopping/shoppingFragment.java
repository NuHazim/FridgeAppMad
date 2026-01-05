package com.example.fridgeapp.shopping;

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

import com.example.fridgeapp.R;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.shoppingfrag, container, false);

        shoppingListManager = ShoppingListManager.getInstance(requireContext());

        shoppingItems = shoppingListManager.getShoppingItems();

        recyclerView = view.findViewById(R.id.recyclerViewShopping);
        btnAddItemBar = view.findViewById(R.id.btnAddItemBar);
        tvItemCount = view.findViewById(R.id.tvItemCount);
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        setupAdapter();

        btnAddItemBar.setOnClickListener(v -> showAddItemDialog());

        return view;
    }

    private void setupAdapter() {

        adapter = new ShoppingAdapter(
                shoppingItems,
                (item, isChecked) -> {
                    item.setCompleted(isChecked);
                    updateUI();
                    if (isChecked) {
                        Toast.makeText(getContext(), item.getName() + " marked as completed!", Toast.LENGTH_SHORT).show();
                    }
                },
                item -> {
                    shoppingListManager.removeItem(item);
                    adapter.notifyDataSetChanged();
                    updateUI();
                    Toast.makeText(getContext(), "Item removed", Toast.LENGTH_SHORT).show();
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setAdapter(adapter);

        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            updateUI();
        }
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

        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Quick Add Buttons (works for Button / TextView / Chips)
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

    // FIXED — accepts any clickable view type
    private void setupQuickAddButton(View dialogView, int buttonId, String itemName, EditText etItemName) {
        View button = dialogView.findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(v -> etItemName.setText(itemName));
        }
    }

    private void addItem(String name, String quantity) {

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
    }

    private void updateUI() {
        int count = shoppingItems.size();
        int completedCount = 0;

        for (ShoppingItem item : shoppingItems) {
            if (item.isCompleted()) completedCount++;
        }

        tvItemCount.setText(count + " items to buy");
        tvCompletedCount.setText(completedCount + " completed");

        if (count == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
