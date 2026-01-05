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

    private RecyclerView recyclerView;
    private ShoppingAdapter adapter;
    private LinearLayout btnAddItemBar;
    private TextView tvItemCount, tvCompletedCount, tvEmptyState;

    private ShoppingListManager shoppingListManager;
    private List<ShoppingItem> shoppingItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
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

        updateUI();
        return view;
    }

    private void setupAdapter() {
        adapter = new ShoppingAdapter(
                shoppingItems,
                (item, isChecked) -> {
                    shoppingListManager.updateItem(item, isChecked);
                    updateUI();
                },
                item -> {
                    shoppingListManager.removeItem(item);
                    adapter.notifyDataSetChanged();
                    updateUI();
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
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
            String name = etItemName.getText().toString().trim();
            String qty = etQuantity.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Enter item name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (qty.isEmpty()) qty = "1";

            ShoppingItem newItem = new ShoppingItem(
                    UUID.randomUUID().toString(),
                    name,
                    qty,
                    false
            );

            shoppingListManager.addItem(newItem);
            adapter.notifyDataSetChanged();
            updateUI();

            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupQuickAddButton(View dialogView, int buttonId,
                                     String value, EditText etItemName) {

        Button btn = dialogView.findViewById(buttonId);
        if (btn != null) btn.setOnClickListener(v -> etItemName.setText(value));
    }

    private void updateUI() {
        int count = shoppingItems.size();
        int completed = 0;

        for (ShoppingItem i : shoppingItems)
            if (i.isCompleted()) completed++;

        tvItemCount.setText(count + " items");
        tvCompletedCount.setText(completed + " completed");

        if (count == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        updateUI();
    }
}
