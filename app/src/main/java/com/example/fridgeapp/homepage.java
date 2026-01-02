package com.example.fridgeapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fridgeapp.shopping.shoppingFragment;
import com.google.android.material.tabs.TabLayout;
import com.example.fridgeapp.inventory.InventoryFragment;


public class homepage extends AppCompatActivity {

    private TabLayout tabLayout;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

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
                        selectedFragment = new recipesFragment();
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

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}