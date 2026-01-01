package com.example.fridgeapp.inventory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fridgeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import android.widget.Toast;


public class InventoryFragment extends Fragment {

    private static final int SCAN_REQUEST_CODE = 100;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> scanBarcodeLauncher;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference itemsRef;

    RecyclerView recyclerView;
    FridgeAdapter adapter;
    ArrayList<FridgeItem> fridgeItemList = new ArrayList<>();

    Button btnAddItem;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        // ✅ Inflate FIRST
        return inflater.inflate(R.layout.inventoryfrag, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        itemsRef = db.collection("users")
                .document(Objects.requireNonNull(auth.getUid()))
                .collection("items");

        btnAddItem = view.findViewById(R.id.btnAddItem);
        recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new FridgeAdapter(fridgeItemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


        itemsRef.addSnapshotListener((snapshots, error) -> {
            if (error != null) return;
            fridgeItemList.clear();
            for (DocumentSnapshot doc : snapshots) {
                FridgeItem item = doc.toObject(FridgeItem.class);
                fridgeItemList.add(item);
            }
            adapter.notifyDataSetChanged();
        });


        // 3️⃣ Activity Result launchers
        scanBarcodeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        showConfirmDialog(
                                data.getStringExtra("PRODUCT_NAME"),
                                data.getStringExtra("CATEGORY"),
                                data.getStringExtra("EXPIRY_DATE")
                        );
                    }
                }
        );

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openScanner(); // your method to launch BarcodeActivity
                    } else {
                        Toast.makeText(getContext(),
                                "Camera permission is required to scan barcodes",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 4️⃣ AddItemBottomSheet logic
        btnAddItem.setOnClickListener(v -> {
            AddItemBottomSheet sheet = new AddItemBottomSheet();
            sheet.setListener(new AddItemBottomSheet.AddItemListener() {
                @Override
                public void onScanSelected() {
                    if (ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                        Intent intent = new Intent(getActivity(), BarcodeActivity.class);
                        scanBarcodeLauncher.launch(intent);

                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                    }
                }
                @Override
                public void onManualSelected() {
                    showConfirmDialog(null, null, null);
                }
            });
            sheet.show(getParentFragmentManager(), "AddItemBottomSheet");
        });
        loadItemsFromDb();
    }


    private void openScanner() {
        Intent intent = new Intent(getActivity(), BarcodeActivity.class);
        scanBarcodeLauncher.launch(intent);
    }

    private void showConfirmDialog(
            @Nullable String productName,
            @Nullable String category,
            @Nullable String expiryDate
    ) {

        ConfirmItemDialogFragment dialog = new ConfirmItemDialogFragment();

        Bundle args = new Bundle();
        if (productName != null) args.putString("PRODUCT_NAME", productName);
        if (category != null) args.putString("CATEGORY", category);
        if (expiryDate != null) args.putString("EXPIRY_DATE", expiryDate);
        dialog.setArguments(args);

        dialog.setListener((name, categoryResult, expiry, quantity, unit) -> {
            saveItem(name, categoryResult, expiry, quantity, unit);
        });

        dialog.show(getParentFragmentManager(), "ConfirmItemDialog");
    }

    private void saveItem(
            String name,
            String category,
            String expiry,
            int quantity,
            String unit
    ) {
        String userId = auth.getCurrentUser().getUid();
        FridgeItem item = new FridgeItem(name, category, expiry, quantity, unit);

        db.collection("users")
                .document(userId)
                .collection("items")
                .add(item)
                .addOnSuccessListener(doc -> {
                    fridgeItemList.add(item);
                    adapter.notifyItemInserted(fridgeItemList.size() - 1);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to save item",
                                Toast.LENGTH_SHORT).show()
                );
        // 🔜 Later replace this with Firestore:
        // itemsRef.add(new FridgeItem(...));
    }

    private void loadItemsFromDb() {

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("items")
                .get()
                .addOnSuccessListener(query -> {

                    fridgeItemList.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        FridgeItem item = doc.toObject(FridgeItem.class);
                        fridgeItemList.add(item);
                    }

                    adapter.notifyDataSetChanged();
                });
    }





}
