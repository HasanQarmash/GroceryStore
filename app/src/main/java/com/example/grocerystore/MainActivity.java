package com.example.grocerystore;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.grocerystore.utils.PreferencesManager;

public class MainActivity extends AppCompatActivity {

    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize preferences manager
        preferencesManager = new PreferencesManager(this);
        
        // Check if user is already logged in
        if (preferencesManager.isLoggedIn()) {
            // User is logged in, go to navigation activity
            startActivity(new Intent(this, NavigationActivity.class));
            finish();
        } else {
            // User not logged in, go to splash screen
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }
    }
}