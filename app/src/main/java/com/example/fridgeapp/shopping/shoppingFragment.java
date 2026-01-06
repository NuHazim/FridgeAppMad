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
import android.widget.TextView;
import android.widget.Toast;

import com.example.fridgeapp.R;
import com.example.fridgeapp.model.ShoppingItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class shoppingFragment extends Fragment {

    private static final String TAG = "ShoppingFragment";

    private RecyclerView recyclerView;
    private ShoppingAdapter adapter;
    private Button btnAddItemBar;
    private TextView tvItemCount, tvCompletedCount, tvEmptyState;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference shoppingItemsRef;
    private ListenerRegistration listenerRegistration;

    private List<ShoppingItem> shoppingItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.shoppingfrag, container, false);

        // Initialize views first
        recyclerView = view.findViewById(R.id.recyclerViewShopping);
        btnAddItemBar = view.findViewById(R.id.btnAddItemBar);
        tvItemCount = view.findViewById(R.id.tvItemCount);
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        // Check if views are properly inflated
        if (recyclerView == null || btnAddItemBar == null ||
                tvItemCount == null || tvCompletedCount == null || tvEmptyState == null) {
            Log.e(TAG, "One or more views are null!");
            Toast.makeText(getContext(), "Layout error", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize Firebase
        try {
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            // Check if user is logged in
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                Log.e(TAG, "User is not logged in");
                Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
                showEmptyState();
                return view;
            }

            // Reference to user's shopping items
            shoppingItemsRef = db.collection("users")
                    .document(currentUser.getUid())
                    .collection("shoppingItems");

            Log.d(TAG, "Firebase initialized successfully for user: " + currentUser.getUid());

        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Firebase error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            showEmptyState();
            return view;
        }

        setupAdapter();
        setupRealtimeListener();

        btnAddItemBar.setOnClickListener(v -> showAddItemDialog());

        return view;
    }

    private void setupAdapter() {
        try {
            adapter = new ShoppingAdapter(
                    shoppingItems,
                    // On checkbox toggle
                    (item, isChecked) -> {
                        updateItemInFirebase(item, isChecked);
                    },
                    // On delete button click
                    item -> {
                        deleteItemFromFirebase(item);
                    }
            );

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
            Log.d(TAG, "Adapter setup complete");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up adapter: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error setting up list", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRealtimeListener() {
        if (shoppingItemsRef == null) {
            Log.e(TAG, "shoppingItemsRef is null, cannot setup listener");
            return;
        }

        try {
            // Real-time listener for shopping items
            // Removed orderBy to avoid index requirement issue
            listenerRegistration = shoppingItemsRef
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) {
                            Log.e(TAG, "Listen failed: " + error.getMessage(), error);
                            Toast.makeText(getContext(),
                                    "Failed to load shopping list: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshots != null) {
                            shoppingItems.clear();
                            for (DocumentSnapshot doc : snapshots) {
                                try {
                                    ShoppingItem item = doc.toObject(ShoppingItem.class);
                                    if (item != null) {
                                        item.setId(doc.getId());
                                        shoppingItems.add(item);
                                        Log.d(TAG, "Loaded item: " + item.getName());
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing document: " + e.getMessage(), e);
                                }
                            }

                            // Sort by timestamp manually
                            shoppingItems.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));

                            adapter.notifyDataSetChanged();
                            updateUI();
                            Log.d(TAG, "Loaded " + shoppingItems.size() + " items");
                        }
                    });

            Log.d(TAG, "Realtime listener setup complete");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up listener: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error connecting to database", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddItemDialog() {
        if (getContext() == null) return;

        try {
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

                addItemToFirebase(name, qty);
                dialog.dismiss();
            });

            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing dialog: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error opening dialog", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupQuickAddButton(View dialogView, int buttonId,
                                     String value, EditText etItemName) {
        try {
            Button btn = dialogView.findViewById(buttonId);
            if (btn != null) {
                btn.setOnClickListener(v -> etItemName.setText(value));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up quick add button: " + e.getMessage());
        }
    }

    private void addItemToFirebase(String name, String quantity) {
        if (shoppingItemsRef == null) {
            Log.e(TAG, "Cannot add item: shoppingItemsRef is null");
            Toast.makeText(getContext(), "Error: Not connected to database", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create item data map
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("name", name);
            itemData.put("quantity", quantity);
            itemData.put("completed", false);
            itemData.put("timestamp", System.currentTimeMillis());

            // Add to Firebase
            shoppingItemsRef
                    .add(itemData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Item added with ID: " + documentReference.getId());
                        Toast.makeText(getContext(),
                                "Item added successfully",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding item", e);
                        Toast.makeText(getContext(),
                                "Failed to add item: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception adding item: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error adding item", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateItemInFirebase(ShoppingItem item, boolean isCompleted) {
        if (shoppingItemsRef == null || item.getId() == null) {
            Log.e(TAG, "Cannot update item: refs are null");
            return;
        }

        try {
            shoppingItemsRef
                    .document(item.getId())
                    .update("completed", isCompleted)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Item updated successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating item", e);
                        Toast.makeText(getContext(),
                                "Failed to update item",
                                Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception updating item: " + e.getMessage(), e);
        }
    }

    private void deleteItemFromFirebase(ShoppingItem item) {
        if (shoppingItemsRef == null || item.getId() == null) {
            Log.e(TAG, "Cannot delete item: refs are null");
            return;
        }

        try {
            shoppingItemsRef
                    .document(item.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Item deleted successfully");
                        Toast.makeText(getContext(),
                                "Item deleted",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting item", e);
                        Toast.makeText(getContext(),
                                "Failed to delete item",
                                Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception deleting item: " + e.getMessage(), e);
        }
    }

    private void updateUI() {
        try {
            int count = shoppingItems.size();
            int completed = 0;

            for (ShoppingItem item : shoppingItems) {
                if (item.isCompleted()) {
                    completed++;
                }
            }

            tvItemCount.setText(count + " items");
            tvCompletedCount.setText(completed + " completed");

            if (count == 0) {
                showEmptyState();
            } else {
                tvEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage(), e);
        }
    }

    private void showEmptyState() {
        if (tvEmptyState != null && recyclerView != null) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove listener to prevent memory leaks
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            Log.d(TAG, "Listener removed");
        }
    }
}