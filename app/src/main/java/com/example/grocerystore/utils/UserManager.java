package com.example.grocerystore.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.grocerystore.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final String PREF_NAME = "UserDatabase";
    private static final String KEY_USERS = "users";

    private SharedPreferences prefs;
    private Gson gson;

    public UserManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        
        // Initialize default admin user if not exists
        initializeDefaultAdmin();
    }

    /**
     * Initialize default admin user
     */
    private void initializeDefaultAdmin() {
        if (getUserByEmail("admin@admin.com") == null) {
            User adminUser = new User(
                "admin@admin.com",
                "Admin",
                "User", 
                "Admin123!",
                "Male",
                "Ramallah",
                "+970599000000",
                "admin"
            );
            registerUser(adminUser);
        }
    }

    /**
     * Register a new user
     */
    public boolean registerUser(User user) {
        List<User> users = getAllUsers();
        
        // Check if email already exists
        for (User existingUser : users) {
            if (existingUser.getEmail().equalsIgnoreCase(user.getEmail())) {
                return false; // Email already exists
            }
        }
        
        // Assign unique ID to new user
        int nextId = 1;
        for (User existingUser : users) {
            if (existingUser.getId() >= nextId) {
                nextId = existingUser.getId() + 1;
            }
        }
        user.setId(nextId);
        
        // Encrypt password before storing
        user.setPassword(PasswordEncryption.encryptPassword(user.getPassword()));
        
        // Add new user
        users.add(user);
        saveUsers(users);
        return true;
    }

    /**
     * Authenticate user login
     */
    public boolean authenticateUser(String email, String password) {
        List<User> users = getAllUsers();
        
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                String decryptedPassword = PasswordEncryption.decryptPassword(user.getPassword());
                return decryptedPassword.equals(password);
            }
        }
        
        return false; // User not found or password incorrect
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        List<User> users = getAllUsers();
        
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        
        return null; // User not found
    }

    /**
     * Check if email is already registered
     */
    public boolean isEmailRegistered(String email) {
        return getUserByEmail(email) != null;
    }

    /**
     * Update an existing user
     */
    public boolean updateUser(User updatedUser) {
        List<User> users = getAllUsers();
        
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getEmail().equalsIgnoreCase(updatedUser.getEmail())) {
                // Update the user at this position
                users.set(i, updatedUser);
                saveUsers(users);
                return true;
            }
        }
        
        return false; // User not found
    }

    /**
     * Save users list to SharedPreferences
     */
    private void saveUsers(List<User> users) {
        String usersJson = gson.toJson(users);
        prefs.edit().putString(KEY_USERS, usersJson).apply();
    }

    /**
     * Clear all users (for testing purposes)
     */
    public void clearAllUsers() {
        prefs.edit().remove(KEY_USERS).apply();
    }

    /**
     * Get total number of registered users
     */
    public int getUserCount() {
        return getAllUsers().size();
    }

    /**
     * Get all users (for admin access)
     */
    public List<User> getAllUsersForAdmin() {
        return getAllUsers();
    }

    /**
     * Get all users - public method for admin access
     */
    public List<User> getAllUsers() {
        String usersJson = prefs.getString(KEY_USERS, "[]");
        Type listType = new TypeToken<List<User>>(){}.getType();
        List<User> users = gson.fromJson(usersJson, listType);
        return users != null ? users : new ArrayList<>();
    }

    /**
     * Delete a user by ID (admin function)
     */
    public boolean deleteUser(int userId) {
        List<User> users = getAllUsers();
        
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getId() == userId) {
                // Don't allow deleting admin users
                if (user.isAdmin()) {
                    return false;
                }
                users.remove(i);
                saveUsers(users);
                return true;
            }
        }
        
        return false; // User not found
    }

    /**
     * Delete a user by email (admin function)
     */
    public boolean deleteUser(String email) {
        List<User> users = getAllUsers();
        
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().equalsIgnoreCase(email)) {
                // Don't allow deleting the main admin
                if ("admin@admin.com".equals(email)) {
                    return false;
                }
                users.remove(i);
                saveUsers(users);
                return true;
            }
        }
        
        return false; // User not found
    }

    /**
     * Check if user is admin
     */
    public boolean isUserAdmin(String email) {
        User user = getUserByEmail(email);
        return user != null && user.isAdmin();
    }

    /**
     * Register a new admin user
     */
    public boolean registerAdmin(User user) {
        user.setRole("admin");
        return registerUser(user);
    }
}
