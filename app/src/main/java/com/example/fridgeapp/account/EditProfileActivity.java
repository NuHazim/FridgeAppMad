package com.example.fridgeapp.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fridgeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    // 1. Declare UI components and Firebase instances
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnSubmit;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DocumentReference userDocRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no user is found
            return;
        }
        currentUserId = auth.getCurrentUser().getUid();
        userDocRef = db.collection("users").document(currentUserId);
        etUsername = findViewById(R.id.profile_username);
        etEmail = findViewById(R.id.profile_email);
        etPassword = findViewById(R.id.profile_password);
        btnSubmit = findViewById(R.id.profile_submit);
        ImageView ic_back = findViewById(R.id.ic_back);

        etEmail.setEnabled(false);
        etPassword.setVisibility(View.GONE); // Hide the password field as we are only editing the username
        findViewById(R.id.profile_password_desc).setVisibility(View.GONE); // Hide password label

        loadUserData();

        ic_back.setOnClickListener(v -> finish()); // Use finish() to go back
        btnSubmit.setOnClickListener(v -> saveUsernameChanges());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserData() {
        btnSubmit.setEnabled(false); // Disable button while loading
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String username = documentSnapshot.getString("username");
                String email = documentSnapshot.getString("email");

                etUsername.setText(username);
                etEmail.setText(email);
                btnSubmit.setEnabled(true);
            } else {
                Toast.makeText(this, "Profile data not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
        });
    }


    private void saveUsernameChanges() {
        String newUsername = etUsername.getText().toString().trim();

        if (TextUtils.isEmpty(newUsername)) {
            etUsername.setError("Username cannot be empty");
            return;
        }

        btnSubmit.setEnabled(false);

        userDocRef.update("username", newUsername)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Username updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to update username: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSubmit.setEnabled(true);
                });
    }

}
