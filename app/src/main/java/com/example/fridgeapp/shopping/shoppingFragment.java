package com.example.fridgeapp.shopping;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fridgeapp.R;
import com.example.fridgeapp.inventory.FridgeItem;
import com.example.fridgeapp.model.ShoppingItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class shoppingFragment extends Fragment {

    private RecyclerView recyclerView;
    private ShoppingAdapter adapter;
    private Button btnAddItemBar, btnCompletePurchase;
    private TextView tvItemCount, tvCompletedCount, tvEmptyState;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference shoppingRef;
    private List<ShoppingItem> shoppingItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.shoppingfrag, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        shoppingRef = db.collection("users")
                .document(Objects.requireNonNull(auth.getUid()))
                .collection("shoppingItems");

        recyclerView = view.findViewById(R.id.recyclerViewShopping);
        btnAddItemBar = view.findViewById(R.id.btnAddItemBar);
        btnCompletePurchase = view.findViewById(R.id.btnCompletePurchase);
        tvItemCount = view.findViewById(R.id.tvItemCount);
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        setupAdapter();
        loadItemsFromFirebase();

        btnAddItemBar.setOnClickListener(v -> showAddItemDialog());
        btnCompletePurchase.setOnClickListener(v -> completePurchase());

        return view;
    }

    private void setupAdapter() {
        adapter = new ShoppingAdapter(
                shoppingItems,
                (item, isChecked) -> {
                    item.setCompleted(isChecked);
                    updateItemInFirebase(item);
                    updateUI();
                },
                item -> {
                    deleteItemFromFirebase(item);
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadItemsFromFirebase() {
        shoppingRef.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error loading items", Toast.LENGTH_SHORT).show();
                return;
            }

            shoppingItems.clear();
            for (DocumentSnapshot doc : snapshots) {
                ShoppingItem item = doc.toObject(ShoppingItem.class);
                if (item != null) {
                    item.setId(doc.getId());
                    shoppingItems.add(item);
                }
            }
            adapter.notifyDataSetChanged();
            updateUI();
        });
    }

    private void showAddItemDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_shopping_item, null);

        EditText etItemName = dialogView.findViewById(R.id.etItemName);
        EditText etQuantityNumber = dialogView.findViewById(R.id.etQuantityNumber);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        Spinner spinnerUnit = dialogView.findViewById(R.id.spinnerUnit);
        Button btnAddItem = dialogView.findViewById(R.id.btnAddItem);
        TextView btnClose = dialogView.findViewById(R.id.btnClose);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Setup quick add buttons
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
            String qtyStr = etQuantityNumber.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            String unit = spinnerUnit.getSelectedItem().toString();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Enter item name", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantityNumber = 1;
            if (!qtyStr.isEmpty()) {
                try {
                    quantityNumber = Integer.parseInt(qtyStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            ShoppingItem newItem = new ShoppingItem(
                    null,
                    name,
                    category,
                    quantityNumber,
                    unit,
                    false
            );

            addItemToFirebase(newItem);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupQuickAddButton(View dialogView, int buttonId,
                                     String value, EditText etItemName) {
        Button btn = dialogView.findViewById(buttonId);
        if (btn != null) btn.setOnClickListener(v -> etItemName.setText(value));
    }

    private void addItemToFirebase(ShoppingItem item) {
        shoppingRef.add(item)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(getContext(), "Item added", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add item", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateItemInFirebase(ShoppingItem item) {
        if (item.getId() != null) {
            shoppingRef.document(item.getId())
                    .set(item)
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to update item", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void deleteItemFromFirebase(ShoppingItem item) {
        if (item.getId() != null) {
            shoppingRef.document(item.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Item deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to delete item", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void completePurchase() {
        List<ShoppingItem> completedItems = new ArrayList<>();

        for (ShoppingItem item : shoppingItems) {
            if (item.isCompleted()) {
                completedItems.add(item);
            }
        }

        if (completedItems.isEmpty()) {
            Toast.makeText(getContext(), "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Transfer to inventory
        String userId = auth.getCurrentUser().getUid();
        CollectionReference inventoryRef = db.collection("users")
                .document(userId)
                .collection("items");

        int totalItems = completedItems.size();

        for (ShoppingItem shoppingItem : completedItems) {
            // First, uncheck in Firebase IMMEDIATELY
            shoppingItem.setCompleted(false);
            updateItemInFirebase(shoppingItem);

            // Calculate expiry date based on category
            String expiryDate = calculateExpiryDate(shoppingItem.getCategory());

            // Create FridgeItem
            FridgeItem fridgeItem = new FridgeItem(
                    null,
                    shoppingItem.getName(),
                    shoppingItem.getCategory(),
                    expiryDate,
                    shoppingItem.getQuantityNumber(),
                    shoppingItem.getUnit()
            );

            // Add to inventory
            inventoryRef.add(fridgeItem)
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(),
                                "Failed to add " + shoppingItem.getName() + " to inventory",
                                Toast.LENGTH_SHORT).show();
                    });
        }

        Toast.makeText(getContext(),
                totalItems + " items added to inventory",
                Toast.LENGTH_SHORT).show();
    }

    private String calculateExpiryDate(String category) {
        int daysToAdd;

        switch (category) {
            case "Dairy":
                daysToAdd = 14;
                break;
            case "Meat":
                daysToAdd = 3;
                break;
            case "Vegetables":
                daysToAdd = 7;
                break;
            case "Fruits":
                daysToAdd = 7;
                break;
            case "Snacks":
                daysToAdd = 90;
                break;
            case "Beverages":
                daysToAdd = 20;
                break;
            case "Others":
            default:
                daysToAdd = 14;
                break;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private void updateUI() {
        int count = shoppingItems.size();
        int completed = 0;

        for (ShoppingItem i : shoppingItems) {
            if (i.isCompleted()) completed++;
        }

        tvItemCount.setText(count + " items");
        tvCompletedCount.setText(completed + " completed");

        // Show/hide complete purchase button
        if (completed > 0) {
            btnCompletePurchase.setVisibility(View.VISIBLE);
        } else {
            btnCompletePurchase.setVisibility(View.GONE);
        }

        // Show/hide empty state
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
        updateUI();
    }
}