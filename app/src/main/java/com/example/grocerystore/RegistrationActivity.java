package com.example.grocerystore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.grocerystore.model.User;
import com.example.grocerystore.utils.UserManager;
import com.example.grocerystore.utils.ValidationUtils;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Spinner genderSpinner;
    private Spinner citySpinner;
    private TextView countryCodeText;
    private EditText phoneEditText;
    private Button registerButton;
    private TextView loginText;

    private UserManager userManager;
    private String selectedGender = "";
    private String selectedCity = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize manager
        userManager = new UserManager(this);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        genderSpinner = findViewById(R.id.genderSpinner);
        citySpinner = findViewById(R.id.citySpinner);
        countryCodeText = findViewById(R.id.countryCodeText);
        phoneEditText = findViewById(R.id.phoneEditText);
        registerButton = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginText);

        // Setup spinners
        setupGenderSpinner();
        setupCitySpinner();

        // Set click listeners
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to login
                finish();
            }
        });
    }

    private void setupGenderSpinner() {
        String[] genderOptions = {
            getString(R.string.select_gender),
            getString(R.string.male),
            getString(R.string.female)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedGender = genderOptions[position];
                } else {
                    selectedGender = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGender = "";
            }
        });
    }

    private void setupCitySpinner() {
        String[] cityOptions = {
            getString(R.string.select_city),
            getString(R.string.ramallah),
            getString(R.string.jerusalem),
            getString(R.string.nablus),
            getString(R.string.gaza)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, cityOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedCity = cityOptions[position];
                    updateCountryCode();
                } else {
                    selectedCity = "";
                    countryCodeText.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCity = "";
                countryCodeText.setText("");
            }
        });
    }

    private void updateCountryCode() {
        String countryCode = ValidationUtils.getCountryCodeByCity(selectedCity);
        countryCodeText.setText(countryCode);
    }

    private void handleRegistration() {
        // Get form data
        String email = emailEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Validate all inputs
        if (!validateInputs(email, firstName, lastName, password, confirmPassword, phone)) {
            return;
        }

        // Create user object
        String fullPhone = countryCodeText.getText().toString() + phone;
        User newUser = new User(email, firstName, lastName, password, selectedGender, selectedCity, fullPhone);

        // Register user
        if (userManager.registerUser(newUser)) {
            // Registration successful
            Toast.makeText(this, getString(R.string.registration_success), Toast.LENGTH_LONG).show();
            
            // Navigate back to login
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            // Registration failed (email already exists)
            Toast.makeText(this, "Email already exists. Please use a different email.", Toast.LENGTH_LONG).show();
            emailEditText.setError("Email already exists");
            emailEditText.requestFocus();
        }
    }

    private boolean validateInputs(String email, String firstName, String lastName, 
                                   String password, String confirmPassword, String phone) {
        
        // Email validation
        if (!ValidationUtils.isNotEmpty(email)) {
            emailEditText.setError(getString(R.string.email_required));
            emailEditText.requestFocus();
            return false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            emailEditText.setError(getString(R.string.email_invalid));
            emailEditText.requestFocus();
            return false;
        }

        // First name validation
        if (!ValidationUtils.isValidName(firstName)) {
            firstNameEditText.setError(getString(R.string.first_name_required));
            firstNameEditText.requestFocus();
            return false;
        }

        // Last name validation
        if (!ValidationUtils.isValidName(lastName)) {
            lastNameEditText.setError(getString(R.string.last_name_required));
            lastNameEditText.requestFocus();
            return false;
        }

        // Password validation
        if (!ValidationUtils.isValidPassword(password)) {
            passwordEditText.setError(getString(R.string.password_weak));
            passwordEditText.requestFocus();
            return false;
        }

        // Confirm password validation
        if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.passwords_not_match));
            confirmPasswordEditText.requestFocus();
            return false;
        }

        // Gender validation
        if (!ValidationUtils.isNotEmpty(selectedGender)) {
            Toast.makeText(this, getString(R.string.gender_required), Toast.LENGTH_SHORT).show();
            return false;
        }

        // City validation
        if (!ValidationUtils.isNotEmpty(selectedCity)) {
            Toast.makeText(this, getString(R.string.city_required), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Phone validation
        if (!ValidationUtils.isValidPhone(phone)) {
            phoneEditText.setError(getString(R.string.phone_invalid));
            phoneEditText.requestFocus();
            return false;
        }

        return true;
    }
}
