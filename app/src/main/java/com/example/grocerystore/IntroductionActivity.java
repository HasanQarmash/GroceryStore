package com.example.grocerystore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class IntroductionActivity extends AppCompatActivity {

    private Button connectButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        // Initialize views
        connectButton = findViewById(R.id.connectButton);
        progressBar = findViewById(R.id.progressBar);

        // Set click listener for connect button
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simulateConnection();
            }
        });
    }

    private void simulateConnection() {
        // Show loading state
        showLoading(true);

        // Simulate API call with a delay (for Phase 1 demo)
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showLoading(false);
                
                // Show success message
                Toast.makeText(IntroductionActivity.this, 
                    "✅ Connected successfully! (Demo mode)", 
                    Toast.LENGTH_SHORT).show();
                
                // Navigate to login screen
                Intent intent = new Intent(IntroductionActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000); // 2 second delay to simulate network call
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            connectButton.setText(R.string.connecting);
            connectButton.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            connectButton.setText(R.string.connect_button);
            connectButton.setEnabled(true);
            progressBar.setVisibility(View.GONE);
        }
    }
}
