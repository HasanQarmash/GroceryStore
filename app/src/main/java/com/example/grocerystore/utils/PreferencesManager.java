package com.example.grocerystore.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREF_NAME = "GroceryStorePrefs";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Save login credentials when "Remember me" is checked
     */
    public void saveLoginCredentials(String email, String password, boolean rememberMe) {
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        if (rememberMe) {
            editor.putString(KEY_SAVED_EMAIL, email);
            editor.putString(KEY_SAVED_PASSWORD, password);
        } else {
            editor.remove(KEY_SAVED_EMAIL);
            editor.remove(KEY_SAVED_PASSWORD);
        }
        editor.apply();
    }

    /**
     * Get saved email if remember me is enabled
     */
    public String getSavedEmail() {
        if (isRememberMeEnabled()) {
            return prefs.getString(KEY_SAVED_EMAIL, "");
        }
        return "";
    }

    /**
     * Get saved password if remember me is enabled
     */
    public String getSavedPassword() {
        if (isRememberMeEnabled()) {
            return prefs.getString(KEY_SAVED_PASSWORD, "");
        }
        return "";
    }

    /**
     * Check if remember me is enabled
     */
    public boolean isRememberMeEnabled() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    /**
     * Set user as logged in
     */
    public void setLoggedIn(boolean isLoggedIn, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        if (isLoggedIn) {
            editor.putString(KEY_USER_EMAIL, email);
        } else {
            editor.remove(KEY_USER_EMAIL);
        }
        editor.apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get current logged in user email
     */
    public String getLoggedInUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Clear all login data (logout)
     */
    public void logout() {
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_EMAIL);
        // Keep remember me data if enabled
        editor.apply();
    }

    /**
     * Clear all login data including remember me
     */
    public void clearLoginData() {
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_SAVED_EMAIL);
        editor.remove(KEY_SAVED_PASSWORD);
        editor.remove(KEY_REMEMBER_ME);
        editor.apply();
    }

    /**
     * Clear all preferences
     */
    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}
