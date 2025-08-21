package com.example.grocerystore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.grocerystore.utils.PreferencesManager;
import com.example.grocerystore.utils.UserManager;
import com.example.grocerystore.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private CheckBox rememberMeCheckBox;
    private Button loginButton;
    private TextView signupText;
    private TextView forgotPasswordText;

    private PreferencesManager preferencesManager;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize managers
        preferencesManager = new PreferencesManager(this);
        userManager = new UserManager(this);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        loginButton = findViewById(R.id.loginButton);
        signupText = findViewById(R.id.signupText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        // Load saved credentials if remember me was enabled
        loadSavedCredentials();

        // Set click listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to registration
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle forgot password (future implementation)
                Toast.makeText(LoginActivity.this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate inputs
        if (!ValidationUtils.isNotEmpty(email)) {
            emailEditText.setError(getString(R.string.email_required));
            emailEditText.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            emailEditText.setError(getString(R.string.email_invalid));
            emailEditText.requestFocus();
            return;
        }

        if (!ValidationUtils.isNotEmpty(password)) {
            passwordEditText.setError(getString(R.string.password_required));
            passwordEditText.requestFocus();
            return;
        }

        // Authenticate user
        if (userManager.authenticateUser(email, password)) {
            // Login successful
            
            // Save credentials if remember me is checked
            preferencesManager.saveLoginCredentials(email, password, rememberMeCheckBox.isChecked());
            
            // Set user as logged in
            preferencesManager.setLoggedIn(true, email);
            
            // Show success message
            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
            
            // Navigate to main navigation
            Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Login failed
            Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_LONG).show();
            passwordEditText.setText("");
            passwordEditText.requestFocus();
        }
    }

    private void loadSavedCredentials() {
        if (preferencesManager.isRememberMeEnabled()) {
            emailEditText.setText(preferencesManager.getSavedEmail());
            passwordEditText.setText(preferencesManager.getSavedPassword());
            rememberMeCheckBox.setChecked(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Check if user is already logged in
        if (preferencesManager.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
