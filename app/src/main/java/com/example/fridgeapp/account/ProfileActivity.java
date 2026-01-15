package com.example.fridgeapp.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.fridgeapp.R;
import com.example.fridgeapp.homepage;
import com.example.fridgeapp.loginpage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView acc_account_name, acc_account_email;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        acc_account_name = findViewById(R.id.acc_account_name);
        acc_account_email = findViewById(R.id.acc_account_email);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupClickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String username = document.getString("username");
                            String email = document.getString("email");

                            acc_account_name.setText(username);
                            acc_account_email.setText(email);
                        } else {
                            Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Failed to load profile",
                                    Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        //code to go back to homepage
        ImageView acc_ic_back = findViewById(R.id.ic_back);
        acc_ic_back.setOnClickListener(v -> homepageActivity());

        //code to go username edit
        LinearLayout acc_account_user_row = findViewById(R.id.acc_account_user_row);
        acc_account_user_row.setOnClickListener(v -> editProfileActivity());

        LinearLayout acc_account_sign_out_row = findViewById(R.id.acc_account_sign_out_row);
        acc_account_sign_out_row.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            signOutActivity();
        });
    }


    private void signOutActivity() {
        Intent intent = new Intent(this, loginpage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void editProfileActivity() {
        Intent intent = new Intent(this,EditProfileActivity.class);
        startActivity(intent);
    }

    private void homepageActivity() {
        Intent intent = new Intent(this, homepage.class);
        startActivity(intent);
    }
}
