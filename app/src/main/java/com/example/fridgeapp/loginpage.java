package com.example.fridgeapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class loginpage extends AppCompatActivity {

    private LinearLayout welcomeLayout;
    private ScrollView registerLayout;
    private ScrollView loginLayout;

    private Button btnWelcomeLogin;
    private Button btnWelcomeCreateAccount;

    private EditText etRegisterUsername;
    private EditText etRegisterEmail;
    private EditText etRegisterPassword;
    private TextView tvRegisterUsernameError;
    private TextView tvRegisterEmailError;
    private TextView tvRegisterPasswordError;
    private TextView btnRegisterTogglePassword;
    private CheckBox cbRegisterTerms;
    private Button btnSignUp;
    private ProgressBar pbRegister;
    private TextView tvRegisterLogin;
    private boolean isRegisterPasswordVisible = false;

    private EditText etLoginEmail;
    private EditText etLoginPassword;
    private TextView tvLoginEmailError;
    private TextView tvLoginPasswordError;
    private TextView btnLoginTogglePassword;
    private Button btnLogin;
    private ProgressBar pbLogin;
    private TextView tvForgotPassword;
    private TextView tvLoginSignUp;
    private ImageButton btnLoginFacebook;
    private ImageButton btnLoginGoogle;
    private ImageButton btnLoginApple;
    private boolean isLoginPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpage);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        initializeViews();
        setupListeners();
        setupSignUpAndLogin(auth);
    }

    private void initializeViews() {

        welcomeLayout = findViewById(R.id.welcomeLayout);
        registerLayout = findViewById(R.id.registerLayout);
        loginLayout = findViewById(R.id.loginLayout);

        btnWelcomeLogin = findViewById(R.id.btnWelcomeLogin);
        btnWelcomeCreateAccount = findViewById(R.id.btnWelcomeCreateAccount);

        etRegisterUsername = findViewById(R.id.etRegisterUsername);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        tvRegisterUsernameError = findViewById(R.id.tvRegisterUsernameError);
        tvRegisterEmailError = findViewById(R.id.tvRegisterEmailError);
        tvRegisterPasswordError = findViewById(R.id.tvRegisterPasswordError);
        btnRegisterTogglePassword = findViewById(R.id.btnRegisterTogglePassword);
        cbRegisterTerms = findViewById(R.id.cbRegisterTerms);
        btnSignUp = findViewById(R.id.btnSignUp);
        pbRegister = findViewById(R.id.pbRegister);
        tvRegisterLogin = findViewById(R.id.tvRegisterLogin);

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        tvLoginEmailError = findViewById(R.id.tvLoginEmailError);
        tvLoginPasswordError = findViewById(R.id.tvLoginPasswordError);
        btnLoginTogglePassword = findViewById(R.id.btnLoginTogglePassword);
        btnLogin = findViewById(R.id.btnLogin);
        pbLogin = findViewById(R.id.pbLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvLoginSignUp = findViewById(R.id.tvLoginSignUp);
        btnLoginFacebook = findViewById(R.id.btnLoginFacebook);
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        btnLoginApple = findViewById(R.id.btnLoginApple);
    }

    private void setupListeners() {

        btnWelcomeLogin.setOnClickListener(v -> showLoginPage());
        btnWelcomeCreateAccount.setOnClickListener(v -> showRegisterPage());
        btnRegisterTogglePassword.setOnClickListener(v -> toggleRegisterPasswordVisibility());

        tvRegisterLogin.setOnClickListener(v -> showLoginPage());

        btnLoginTogglePassword.setOnClickListener(v -> toggleLoginPasswordVisibility());
        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show());
        tvLoginSignUp.setOnClickListener(v -> showRegisterPage());
    }

    private void setupSignUpAndLogin(FirebaseAuth auth){
        btnLogin.setOnClickListener(v -> handleLogin(auth));
        btnSignUp.setOnClickListener(v -> handleSignUp(auth));
    }

    private void showWelcomePage() {
        welcomeLayout.setVisibility(View.VISIBLE);
        registerLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.GONE);
    }

    private void showRegisterPage() {
        welcomeLayout.setVisibility(View.GONE);
        registerLayout.setVisibility(View.VISIBLE);
        loginLayout.setVisibility(View.GONE);
    }

    private void showLoginPage() {
        welcomeLayout.setVisibility(View.GONE);
        registerLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.VISIBLE);
    }

    private void toggleRegisterPasswordVisibility() {
        if (isRegisterPasswordVisible) {
            etRegisterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            etRegisterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        isRegisterPasswordVisible = !isRegisterPasswordVisible;
        etRegisterPassword.setSelection(etRegisterPassword.getText().length());
    }

    private void toggleLoginPasswordVisibility() {
        if (isLoginPasswordVisible) {
            etLoginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            etLoginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        isLoginPasswordVisible = !isLoginPasswordVisible;
        etLoginPassword.setSelection(etLoginPassword.getText().length());
    }

    private void handleSignUp(FirebaseAuth auth) {

        String username = etRegisterUsername.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim();
        String password = etRegisterPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        pbRegister.setVisibility(View.VISIBLE);
        btnSignUp.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(auth.getUid())
                            .set(new HashMap<String, Object>() {{
                                put("email", email);
                            }});

                    startActivity(new Intent(this, homepage.class));
                    finish();
                });
//        new android.os.Handler().postDelayed(() -> {
//
//            pbRegister.setVisibility(View.GONE);
//            btnSignUp.setEnabled(true);
//
//            Toast.makeText(this, "Register success", Toast.LENGTH_SHORT).show();
//
//            Intent intent = new Intent(loginpage.this, homepage.class);
//            startActivity(intent);
//            finish();
//
//        }, 2000);
    }

    private void handleLogin(FirebaseAuth auth) {

        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        pbLogin.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);


        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(result -> {
                    startActivity(new Intent(this, homepage.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );

//        new android.os.Handler().postDelayed(() -> {
//
//            pbLogin.setVisibility(View.GONE);
//            btnLogin.setEnabled(true);
//
//            Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show();
//
//            Intent intent = new Intent(loginpage.this, homepage.class);
//            startActivity(intent);
//            finish();
//
//        }, 2000);
    }

    @Override
    public void onBackPressed() {
        if (registerLayout.getVisibility() == View.VISIBLE || loginLayout.getVisibility() == View.VISIBLE) {
            showWelcomePage();
        } else {
            super.onBackPressed();
        }
    }
}
