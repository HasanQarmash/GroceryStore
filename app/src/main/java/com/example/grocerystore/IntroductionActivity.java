package com.example.grocerystore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.grocerystore.api.ApiService;
import com.example.grocerystore.api.RetrofitClient;
import com.example.grocerystore.model.ApiProduct;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IntroductionActivity extends AppCompatActivity {

    private static final String TAG = "IntroductionActivity";
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
                connectToAPI();
            }
        });
    }

    private void connectToAPI() {
        Log.d(TAG, "Starting API connection...");
        
        // Show loading state
        showLoading(true);

        // Get API service instance
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        
        // Make the API call
        Call<List<ApiProduct>> call = apiService.getProducts();
        call.enqueue(new Callback<List<ApiProduct>>() {
            @Override
            public void onResponse(Call<List<ApiProduct>> call, Response<List<ApiProduct>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ApiProduct> products = response.body();
                    Log.d(TAG, "API Success: Received " + products.size() + " products");
                    
                    // Log categories and products for debugging
                    logProductCategories(products);
                    
                    // Show success message
                    Toast.makeText(IntroductionActivity.this, 
                        "✅ Connected successfully! Found " + products.size() + " products", 
                        Toast.LENGTH_SHORT).show();
                    
                    // Navigate to login screen
                    Intent intent = new Intent(IntroductionActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    
                } else {
                    Log.e(TAG, "API Error: Response not successful - " + response.code());
                    showError("Failed to fetch data from server. Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ApiProduct>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "API Failure: " + t.getMessage(), t);
                showError("Connection failed. Please check your internet connection and try again.");
            }
        });
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
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }
    
    /**
     * Logs the product categories and sample products from the API
     * This demonstrates how the fetched data can be processed
     */
    private void logProductCategories(List<ApiProduct> products) {
        // Count products by category
        int fruitsCount = 0, vegetablesCount = 0, dairyCount = 0, bakeryCount = 0;
        
        for (ApiProduct product : products) {
            String category = product.getCategory();
            switch (category) {
                case "Fruits":
                    fruitsCount++;
                    break;
                case "Vegetables":
                    vegetablesCount++;
                    break;
                case "Dairy":
                    dairyCount++;
                    break;
                case "Bakery":
                    bakeryCount++;
                    break;
            }
            
            // Log first product of each category
            if ((category.equals("Fruits") && fruitsCount == 1) ||
                (category.equals("Vegetables") && vegetablesCount == 1) ||
                (category.equals("Dairy") && dairyCount == 1) ||
                (category.equals("Bakery") && bakeryCount == 1)) {
                
                Log.d(TAG, "Sample " + category + " product: " + product.getName() + 
                     " - Price: $" + product.getPrice() + 
                     " - Stock: " + product.getStock() + 
                     " - Has Offer: " + product.isOffer());
            }
        }
        
        Log.d(TAG, "Product Categories Summary:");
        Log.d(TAG, "- Fruits: " + fruitsCount + " products");
        Log.d(TAG, "- Vegetables: " + vegetablesCount + " products");
        Log.d(TAG, "- Dairy: " + dairyCount + " products");
        Log.d(TAG, "- Bakery: " + bakeryCount + " products");
    }
}
