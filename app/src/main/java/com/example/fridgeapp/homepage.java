package com.example.fridgeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fridgeapp.account.ProfileActivity;
import com.example.fridgeapp.recipes.RecipesFragment;
import com.example.fridgeapp.shopping.shoppingFragment;
import com.google.android.material.tabs.TabLayout;
import com.example.fridgeapp.inventory.InventoryFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class homepage extends AppCompatActivity {

    private TabLayout tabLayout;
    private FragmentManager fragmentManager;

    private TextView tvItemsCount;
    private TextView tvExpiringCount;
    private TextView tvToBuyCount;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        // Account button region
        TextView profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfileActivity();
            }
        });

        tvItemsCount = findViewById(R.id.tvItemsCount);
        tvExpiringCount = findViewById(R.id.tvExpiringCount);
        tvToBuyCount = findViewById(R.id.tvToBuyCount);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        listenForItemsCount();

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize TabLayout
        tabLayout = findViewById(R.id.tabLayout);
        fragmentManager = getSupportFragmentManager();

        // Load default fragment (Inventory)
        loadFragment(new InventoryFragment());

        // Setup tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment = null;

                switch (tab.getPosition()) {
                    case 0:
                        selectedFragment = new InventoryFragment();
                        break;
                    case 1:
                        selectedFragment = new shoppingFragment();
                        break;
                    case 2:
                        selectedFragment = new RecipesFragment();
                        break;
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }

    private void listenForItemsCount() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("items")
                .addSnapshotListener((snapshot, e) ->{

                    if(snapshot == null) return;

                    int totalItems = snapshot.size();
                    int expiringSoon = 0;
                    int itemToBuy = 0;

                    long now = System.currentTimeMillis();

                    for(DocumentSnapshot doc : snapshot){
                        String expiry = doc.getString("expiry");
                        if(expiry == null) continue;

                        try{
                            SimpleDateFormat sdf =
                                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                            Date expiryDate = sdf.parse(expiry);

                            long diffDays =
                                    (expiryDate.getTime() - now) / (1000 * 60 * 60 * 24);

                            if (diffDays <= 3) {
                                expiringSoon++;
                            }
                        }catch (Exception ignored){}
                    }
                    tvItemsCount.setText(String.valueOf(totalItems));
                    tvExpiringCount.setText(String.valueOf(expiringSoon));
                });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    //to profile activity
    private void changeProfileActivity(){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
}