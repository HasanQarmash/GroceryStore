package com.example.grocerystore.api;

import com.example.grocerystore.model.ApiProduct;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

// REST API service interface for fetching product categories
public interface ApiService {
    
    @GET("v1/cede0a18-239c-4370-a84f-93fd197c5111")
    Call<List<ApiProduct>> getProducts();
}
