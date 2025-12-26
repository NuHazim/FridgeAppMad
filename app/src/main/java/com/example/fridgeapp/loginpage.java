package com.example.fridgeapp;

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

public class loginpage extends AppCompatActivity {

    // Layouts
    private LinearLayout welcomeLayout;
    private ScrollView registerLayout;
    private ScrollView loginLayout;

    // Welcome Page
    private Button btnWelcomeLogin;
    private Button btnWelcomeCreateAccount;

    // Register Page
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

    // Login Page
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

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        // Layouts
        welcomeLayout = findViewById(R.id.welcomeLayout);
        registerLayout = findViewById(R.id.registerLayout);
        loginLayout = findViewById(R.id.loginLayout);

        // Welcome Page
        btnWelcomeLogin = findViewById(R.id.btnWelcomeLogin);
        btnWelcomeCreateAccount = findViewById(R.id.btnWelcomeCreateAccount);

        // Register Page
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

        // Login Page
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
        // Welcome Page Navigation
        btnWelcomeLogin.setOnClickListener(v -> showLoginPage());
        btnWelcomeCreateAccount.setOnClickListener(v -> showRegisterPage());

        // Register Page
        btnRegisterTogglePassword.setOnClickListener(v -> toggleRegisterPasswordVisibility());
        btnSignUp.setOnClickListener(v -> handleSignUp());
        tvRegisterLogin.setOnClickListener(v -> showLoginPage());

        // Login Page
        btnLoginTogglePassword.setOnClickListener(v -> toggleLoginPasswordVisibility());
        btnLogin.setOnClickListener(v -> handleLogin());
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        tvLoginSignUp.setOnClickListener(v -> showRegisterPage());

        // Social Login Placeholders
        btnLoginFacebook.setOnClickListener(v ->
                Toast.makeText(this, "Facebook login coming soon", Toast.LENGTH_SHORT).show());
        btnLoginGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Google login coming soon", Toast.LENGTH_SHORT).show());
        btnLoginApple.setOnClickListener(v ->
                Toast.makeText(this, "Apple login coming soon", Toast.LENGTH_SHORT).show());
    }

    // Page Navigation
    private void showWelcomePage() {
        welcomeLayout.setVisibility(View.VISIBLE);
        registerLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.GONE);
        clearAllFields();
    }

    private void showRegisterPage() {
        welcomeLayout.setVisibility(View.GONE);
        registerLayout.setVisibility(View.VISIBLE);
        loginLayout.setVisibility(View.GONE);
        clearRegisterErrors();
    }

    private void showLoginPage() {
        welcomeLayout.setVisibility(View.GONE);
        registerLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.VISIBLE);
        clearLoginErrors();
    }

    // Register Page Methods
    private void toggleRegisterPasswordVisibility() {
        if (isRegisterPasswordVisible) {
            // Hide password
            etRegisterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnRegisterTogglePassword.setText("\uf070"); // fa-eye-slash
        } else {
            // Show password
            etRegisterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnRegisterTogglePassword.setText("\uf06e"); // fa-eye
        }
        isRegisterPasswordVisible = !isRegisterPasswordVisible;
        etRegisterPassword.setSelection(etRegisterPassword.getText().length());
    }

    private void handleSignUp() {
        clearRegisterErrors();

        String username = etRegisterUsername.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim();
        String password = etRegisterPassword.getText().toString();
        boolean termsAccepted = cbRegisterTerms.isChecked();

        boolean isValid = true;

        // Validate Username
        if (TextUtils.isEmpty(username)) {
            tvRegisterUsernameError.setText("Username is required");
            tvRegisterUsernameError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (username.length() < 3) {
            tvRegisterUsernameError.setText("Username must be at least 3 characters");
            tvRegisterUsernameError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Validate Email
        if (TextUtils.isEmpty(email)) {
            tvRegisterEmailError.setText("Email is required");
            tvRegisterEmailError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvRegisterEmailError.setText("Invalid email address");
            tvRegisterEmailError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Validate Password
        if (TextUtils.isEmpty(password)) {
            tvRegisterPasswordError.setText("Password is required");
            tvRegisterPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (password.length() < 6) {
            tvRegisterPasswordError.setText("Password must be at least 6 characters");
            tvRegisterPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Validate Terms
        if (!termsAccepted) {
            Toast.makeText(this, "Please accept the terms and privacy policy", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {
            // Show loading
            pbRegister.setVisibility(View.VISIBLE);
            btnSignUp.setEnabled(false);

            // Simulate registration process
            // In real app, you would make API call here
            new android.os.Handler().postDelayed(() -> {
                pbRegister.setVisibility(View.GONE);
                btnSignUp.setEnabled(true);

                // Success message
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

                // Navigate to main app or login
                // For now, just show login page
                showLoginPage();

            }, 2000);
        }
    }

    // Login Page Methods
    private void toggleLoginPasswordVisibility() {
        if (isLoginPasswordVisible) {
            // Hide password
            etLoginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnLoginTogglePassword.setText("\uf070"); // fa-eye-slash
        } else {
            // Show password
            etLoginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnLoginTogglePassword.setText("\uf06e"); // fa-eye
        }
        isLoginPasswordVisible = !isLoginPasswordVisible;
        etLoginPassword.setSelection(etLoginPassword.getText().length());
    }

    private void handleLogin() {
        clearLoginErrors();

        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString();

        boolean isValid = true;

        // Validate Email
        if (TextUtils.isEmpty(email)) {
            tvLoginEmailError.setText("Email is required");
            tvLoginEmailError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvLoginEmailError.setText("Invalid email address");
            tvLoginEmailError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Validate Password
        if (TextUtils.isEmpty(password)) {
            tvLoginPasswordError.setText("Password is required");
            tvLoginPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (isValid) {
            // Show loading
            pbLogin.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);

            // Simulate login process
            // In real app, you would make API call here
            new android.os.Handler().postDelayed(() -> {
                pbLogin.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                // Success message
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                // Navigate to main app
                // Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                // startActivity(intent);
                // finish();

            }, 2000);
        }
    }

    private void handleForgotPassword() {
        Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show();
        // Implement forgot password functionality
    }

    // Helper Methods
    private void clearRegisterErrors() {
        tvRegisterUsernameError.setVisibility(View.GONE);
        tvRegisterEmailError.setVisibility(View.GONE);
        tvRegisterPasswordError.setVisibility(View.GONE);
    }

    private void clearLoginErrors() {
        tvLoginEmailError.setVisibility(View.GONE);
        tvLoginPasswordError.setVisibility(View.GONE);
    }

    private void clearAllFields() {
        // Register fields
        etRegisterUsername.setText("");
        etRegisterEmail.setText("");
        etRegisterPassword.setText("");
        cbRegisterTerms.setChecked(false);
        clearRegisterErrors();

        // Login fields
        etLoginEmail.setText("");
        etLoginPassword.setText("");
        clearLoginErrors();
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