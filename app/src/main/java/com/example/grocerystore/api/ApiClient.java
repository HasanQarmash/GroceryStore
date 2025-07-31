package com.example.grocerystore.api;

// Simplified API client for Phase 1 demo
// Will be updated with Retrofit in Phase 2
public class ApiClient {
    private static final String BASE_URL = "https://mocki.io/";

    public static ApiService getApiService() {
        // Return a dummy implementation for Phase 1
        return new ApiService() {
            @Override
            public com.example.grocerystore.model.CategoryResponse getCategories() {
                // This is just a placeholder for demo purposes
                return null;
            }
        };
    }
}
