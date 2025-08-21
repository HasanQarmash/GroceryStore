package com.example.grocerystore.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.grocerystore.NavigationActivity;
import com.example.grocerystore.R;
import com.example.grocerystore.model.User;
import com.example.grocerystore.utils.PasswordEncryption;
import com.example.grocerystore.utils.PreferencesManager;
import com.example.grocerystore.utils.UserManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;

    // UI Components
    private ImageView profileImageView;
    private TextInputEditText firstNameEdit, lastNameEdit, emailEdit, phoneEdit;
    private TextInputEditText currentPasswordEdit, newPasswordEdit, confirmPasswordEdit;
    private TextInputLayout firstNameLayout, lastNameLayout, emailLayout, phoneLayout;
    private TextInputLayout currentPasswordLayout, newPasswordLayout, confirmPasswordLayout;
    private Button saveChangesButton, changeProfilePictureButton, changePasswordButton;

    // Services
    private UserManager userManager;
    private PreferencesManager preferencesManager;
    private User currentUser;

    // Password validation patterns
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            getActivity().setTitle("My Profile");
        }

        initializeServices();
        initializeViews(view);
        setupValidation();
        setupClickListeners();
        loadUserData();
    }

    private void initializeServices() {
        userManager = new UserManager(getContext());
        preferencesManager = new PreferencesManager(getContext());
    }

    private void initializeViews(View view) {
        profileImageView = view.findViewById(R.id.profile_image);
        
        firstNameEdit = view.findViewById(R.id.first_name_edit);
        lastNameEdit = view.findViewById(R.id.last_name_edit);
        emailEdit = view.findViewById(R.id.email_edit);
        phoneEdit = view.findViewById(R.id.phone_edit);
        
        currentPasswordEdit = view.findViewById(R.id.current_password_edit);
        newPasswordEdit = view.findViewById(R.id.new_password_edit);
        confirmPasswordEdit = view.findViewById(R.id.confirm_password_edit);
        
        firstNameLayout = view.findViewById(R.id.first_name_layout);
        lastNameLayout = view.findViewById(R.id.last_name_layout);
        emailLayout = view.findViewById(R.id.email_layout);
        phoneLayout = view.findViewById(R.id.phone_layout);
        
        currentPasswordLayout = view.findViewById(R.id.current_password_layout);
        newPasswordLayout = view.findViewById(R.id.new_password_layout);
        confirmPasswordLayout = view.findViewById(R.id.confirm_password_layout);
        
        saveChangesButton = view.findViewById(R.id.save_changes_button);
        changeProfilePictureButton = view.findViewById(R.id.change_profile_picture_button);
        changePasswordButton = view.findViewById(R.id.change_password_button);
    }

    private void setupValidation() {
        // First Name validation
        firstNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFirstName();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Last Name validation
        lastNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateLastName();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Phone validation
        phoneEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhone();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Password validation
        newPasswordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateNewPassword();
                validateConfirmPassword();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        confirmPasswordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        saveChangesButton.setOnClickListener(v -> saveProfileChanges());
        
        changeProfilePictureButton.setOnClickListener(v -> showImagePickerDialog());
        
        changePasswordButton.setOnClickListener(v -> changePassword());
    }

    private void loadUserData() {
        String email = preferencesManager.getLoggedInUserEmail();
        if (email != null && !email.isEmpty()) {
            currentUser = userManager.getUserByEmail(email);
            
            if (currentUser != null) {
                firstNameEdit.setText(currentUser.getFirstName());
                lastNameEdit.setText(currentUser.getLastName());
                emailEdit.setText(currentUser.getEmail());
                phoneEdit.setText(currentUser.getPhoneNumber());
                
                // Email should be read-only
                emailEdit.setEnabled(false);
                emailLayout.setHelperText("Email cannot be changed");
                
                // Load profile picture if exists
                loadProfilePicture();
            } else {
                Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfilePicture() {
        // This method can be expanded to load actual profile pictures
        // For now, we'll use the default icon
        profileImageView.setImageResource(R.drawable.ic_person_24);
    }

    // Validation Methods
    private boolean validateFirstName() {
        String firstName = firstNameEdit.getText().toString().trim();
        if (firstName.isEmpty()) {
            firstNameLayout.setError("First name is required");
            return false;
        } else if (firstName.length() < 2) {
            firstNameLayout.setError("First name must be at least 2 characters");
            return false;
        } else {
            firstNameLayout.setError(null);
            return true;
        }
    }

    private boolean validateLastName() {
        String lastName = lastNameEdit.getText().toString().trim();
        if (lastName.isEmpty()) {
            lastNameLayout.setError("Last name is required");
            return false;
        } else if (lastName.length() < 2) {
            lastNameLayout.setError("Last name must be at least 2 characters");
            return false;
        } else {
            lastNameLayout.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        String phone = phoneEdit.getText().toString().trim();
        if (!phone.isEmpty()) {
            if (phone.length() < 10) {
                phoneLayout.setError("Phone number must be at least 10 digits");
                return false;
            } else if (!phone.matches("^[+]?[0-9\\s-()]+$")) {
                phoneLayout.setError("Invalid phone number format");
                return false;
            }
        }
        phoneLayout.setError(null);
        return true;
    }

    private boolean validateCurrentPassword() {
        String currentPassword = currentPasswordEdit.getText().toString();
        if (currentPassword.isEmpty()) {
            currentPasswordLayout.setError("Current password is required");
            return false;
        }
        
        // Verify current password
        if (currentUser != null) {
            String decryptedPassword = PasswordEncryption.decryptPassword(currentUser.getPassword());
            if (!decryptedPassword.equals(currentPassword)) {
                currentPasswordLayout.setError("Current password is incorrect");
                return false;
            }
        }
        
        currentPasswordLayout.setError(null);
        return true;
    }

    private boolean validateNewPassword() {
        String newPassword = newPasswordEdit.getText().toString();
        if (!newPassword.isEmpty()) {
            if (newPassword.length() < 8) {
                newPasswordLayout.setError("Password must be at least 8 characters");
                return false;
            } else if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                newPasswordLayout.setError("Password must contain uppercase, lowercase, number, and special character");
                return false;
            }
        }
        newPasswordLayout.setError(null);
        return true;
    }

    private boolean validateConfirmPassword() {
        String newPassword = newPasswordEdit.getText().toString();
        String confirmPassword = confirmPasswordEdit.getText().toString();
        
        if (!newPassword.isEmpty() && !confirmPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                confirmPasswordLayout.setError("Passwords do not match");
                return false;
            }
        }
        
        confirmPasswordLayout.setError(null);
        return true;
    }

    private void saveProfileChanges() {
        if (!validateFirstName() || !validateLastName() || !validatePhone()) {
            Toast.makeText(getContext(), "Please fix the errors above", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null) {
            // Update user data
            currentUser.setFirstName(firstNameEdit.getText().toString().trim());
            currentUser.setLastName(lastNameEdit.getText().toString().trim());
            currentUser.setPhoneNumber(phoneEdit.getText().toString().trim());

            // Update user in database
            if (userManager.updateUser(currentUser)) {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                
                // Refresh navigation header
                if (getActivity() instanceof NavigationActivity) {
                    ((NavigationActivity) getActivity()).refreshUserData();
                }
            } else {
                Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void changePassword() {
        if (!validateCurrentPassword() || !validateNewPassword() || !validateConfirmPassword()) {
            Toast.makeText(getContext(), "Please fix the password errors", Toast.LENGTH_SHORT).show();
            return;
        }

        String newPassword = newPasswordEdit.getText().toString();
        if (newPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a new password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null) {
            // Update password
            currentUser.setPassword(PasswordEncryption.encryptPassword(newPassword));
            
            if (userManager.updateUser(currentUser)) {
                Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                
                // Clear password fields
                currentPasswordEdit.setText("");
                newPasswordEdit.setText("");
                confirmPasswordEdit.setText("");
            } else {
                Toast.makeText(getContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Change Profile Picture");
        builder.setItems(new String[]{"Choose from Gallery", "Take Photo", "Remove Picture"}, 
            (dialog, which) -> {
                switch (which) {
                    case 0:
                        openGallery();
                        break;
                    case 1:
                        openCamera();
                        break;
                    case 2:
                        removeProfilePicture();
                        break;
                }
            });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST);
        } else {
            Toast.makeText(getContext(), "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeProfilePicture() {
        profileImageView.setImageResource(R.drawable.ic_person_24);
        Toast.makeText(getContext(), "Profile picture removed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri imageUri = data.getData();
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    profileImageView.setImageBitmap(bitmap);
                    Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAMERA_REQUEST) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                profileImageView.setImageBitmap(imageBitmap);
                Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
