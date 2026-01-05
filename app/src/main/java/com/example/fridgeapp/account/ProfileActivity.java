package com.example.fridgeapp.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.fridgeapp.R;
import com.example.fridgeapp.homepage;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        //code to go back to homepage
        ImageView acc_ic_back = findViewById(R.id.ic_back);
        acc_ic_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homepageActivity();
            }
        });

        //code to go username edit
        LinearLayout acc_account_user_row = findViewById(R.id.acc_account_user_row);
        acc_account_user_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfileActivity();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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