package com.example.grocerystore.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.grocerystore.R;
import com.example.grocerystore.database.DatabaseHelper;
import com.example.grocerystore.models.Offer;
import com.example.grocerystore.models.Product;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddSpecialOfferFragment extends Fragment {

    private TextInputLayout originalPriceLayout, discountedPriceLayout, 
                           stockQuantityLayout, descriptionLayout;
    private TextInputEditText originalPriceInput, discountedPriceInput,
                             stockQuantityInput, descriptionInput;
    private AutoCompleteTextView categorySpinner, productSpinner;
    private ImageView imagePreview;
    private MaterialButton uploadImageButton, saveOfferButton;
    private MaterialCardView formCard;
    private com.google.android.material.switchmaterial.SwitchMaterial activeStatusSwitch;
    
    private DatabaseHelper databaseHelper;
    private Uri selectedImageUri;
    private String selectedImagePath;
    private Offer editingOffer; // For editing existing offers
    private List<Product> availableProducts;
    private Product selectedProduct;
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    // Activity result launcher for image selection
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    // Activity result launcher for permissions
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        displaySelectedImage();
                    }
                }
            }
        );
        
        // Initialize permission launcher
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(getContext(), "Permission denied to access gallery", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_special_offer, container, false);
        
        databaseHelper = new DatabaseHelper(getContext());
        
        initializeViews(view);
        loadAvailableProducts();
        setupProductSpinner();
        setupCategorySpinner();
        setupListeners();
        
        // Check if we're editing an existing offer
        checkForEditMode();
        
        animateCardEntry();
        
        return view;
    }

    private void initializeViews(View view) {
        formCard = view.findViewById(R.id.form_card);
        
        originalPriceLayout = view.findViewById(R.id.original_price_layout);
        discountedPriceLayout = view.findViewById(R.id.discounted_price_layout);
        stockQuantityLayout = view.findViewById(R.id.stock_quantity_layout);
        descriptionLayout = view.findViewById(R.id.description_layout);
        
        originalPriceInput = view.findViewById(R.id.original_price_input);
        discountedPriceInput = view.findViewById(R.id.discounted_price_input);
        stockQuantityInput = view.findViewById(R.id.stock_quantity_input);
        descriptionInput = view.findViewById(R.id.description_input);
        
        productSpinner = view.findViewById(R.id.product_spinner);
        categorySpinner = view.findViewById(R.id.category_spinner);
        imagePreview = view.findViewById(R.id.image_preview);
        uploadImageButton = view.findViewById(R.id.upload_image_button);
        saveOfferButton = view.findViewById(R.id.save_offer_button);
        activeStatusSwitch = view.findViewById(R.id.active_status_switch);
    }

    private void loadAvailableProducts() {
        availableProducts = databaseHelper.getAllProducts();
        android.util.Log.d("AddSpecialOfferFragment", "Loaded " + availableProducts.size() + " products for selection");
    }
    
    private void setupProductSpinner() {
        if (availableProducts == null || availableProducts.isEmpty()) {
            Toast.makeText(getContext(), "No products available. Please add products first.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Create array of product names for the spinner
        String[] productNames = new String[availableProducts.size()];
        for (int i = 0; i < availableProducts.size(); i++) {
            productNames[i] = availableProducts.get(i).getName();
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            productNames
        );
        productSpinner.setAdapter(adapter);
        
        // Set up item selection listener
        productSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedProductName = (String) parent.getItemAtPosition(position);
            
            // Find the selected product
            for (Product product : availableProducts) {
                if (product.getName().equals(selectedProductName)) {
                    selectedProduct = product;
                    onProductSelected(product);
                    break;
                }
            }
        });
    }
    
    private void onProductSelected(Product product) {
        android.util.Log.d("AddSpecialOfferFragment", "Product selected: " + product.getName());
        
        // Auto-fill some fields based on the selected product
        categorySpinner.setText(product.getCategory(), false);
        originalPriceInput.setText(String.valueOf(product.getPrice()));
        
        // Suggest a discounted price (20% off by default)
        double suggestedDiscount = product.getPrice() * 0.8;
        discountedPriceInput.setText(String.format("%.2f", suggestedDiscount));
        
        // Auto-load product image if available
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            selectedImagePath = product.getImageUrl();
            loadProductImage(product.getImageUrl());
        }
        
        Toast.makeText(getContext(), "Product details auto-filled! Adjust prices as needed.", Toast.LENGTH_SHORT).show();
    }
    
    private void loadProductImage(String imageUrl) {
        // This is a simplified version - in a real app you'd use Glide or Picasso
        try {
            if (imageUrl.startsWith("drawable://")) {
                String drawableName = imageUrl.replace("drawable://", "");
                int resourceId = getResources().getIdentifier(drawableName, "drawable", requireContext().getPackageName());
                if (resourceId != 0) {
                    imagePreview.setImageResource(resourceId);
                    imagePreview.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("AddSpecialOfferFragment", "Error loading product image: " + e.getMessage());
        }
    }

    private void setupCategorySpinner() {
        String[] categories = {
            "Fruits", "Vegetables", "Dairy", "Meat", "Bakery", 
            "Beverages", "Snacks", "Frozen", "Canned Goods", "Other"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            categories
        );
        categorySpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        uploadImageButton.setOnClickListener(v -> {
            animateButtonClick(uploadImageButton);
            checkPermissionAndOpenPicker();
        });
        
        saveOfferButton.setOnClickListener(v -> {
            animateButtonClick(saveOfferButton);
            validateAndSaveOffer();
        });
    }

    private void checkForEditMode() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("edit_offer")) {
            editingOffer = (Offer) args.getSerializable("edit_offer");
            if (editingOffer != null) {
                populateFieldsForEdit();
                saveOfferButton.setText("Update Offer");
                if (getActivity() != null) {
                    getActivity().setTitle("Edit Special Offer");
                }
            }
        }
    }

    private void populateFieldsForEdit() {
        // Set product spinner to the current offer's product
        if (editingOffer.getProductName() != null) {
            productSpinner.setText(editingOffer.getProductName(), false);
            
            // Find and set the selected product
            for (Product product : availableProducts) {
                if (product.getName().equals(editingOffer.getProductName())) {
                    selectedProduct = product;
                    break;
                }
            }
        }
        
        originalPriceInput.setText(String.valueOf(editingOffer.getOriginalPrice()));
        discountedPriceInput.setText(String.valueOf(editingOffer.getDiscountedPrice()));
        stockQuantityInput.setText(String.valueOf(editingOffer.getStockQuantity()));
        descriptionInput.setText(editingOffer.getDescription());
        categorySpinner.setText(editingOffer.getCategory(), false);
        activeStatusSwitch.setChecked(editingOffer.isActive());
        
        // Load existing image if available
        selectedImagePath = editingOffer.getImageUrl();
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            loadProductImage(selectedImagePath);
        }
    }

    private void checkPermissionAndOpenPicker() {
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void displaySelectedImage() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                requireContext().getContentResolver(), selectedImageUri);
            imagePreview.setImageBitmap(bitmap);
            imagePreview.setVisibility(View.VISIBLE);
            
            // Store the image path (simplified - in real app would save to internal storage)
            selectedImagePath = selectedImageUri.toString();
            
            // Animate image appearance
            Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
            imagePreview.startAnimation(fadeIn);
            
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateAndSaveOffer() {
        if (!validateInputs()) {
            return;
        }

        try {
            // Get data from selected product instead of manual input
            if (selectedProduct == null) {
                showErrorMessage("Please select a product first");
                return;
            }
            
            String productName = selectedProduct.getName();
            String category = selectedProduct.getCategory();
            double originalPrice = Double.parseDouble(originalPriceInput.getText().toString().trim());
            double discountedPrice = Double.parseDouble(discountedPriceInput.getText().toString().trim());
            int stockQuantity = Integer.parseInt(stockQuantityInput.getText().toString().trim());
            String description = descriptionInput.getText().toString().trim();
            boolean isActive = activeStatusSwitch.isChecked();

            android.util.Log.d("AddSpecialOfferFragment", "Creating offer for product: " + productName + 
                " (ID: " + selectedProduct.getId() + ")");

            // Use selected product's image unless a custom one was uploaded
            String productImageUrl = selectedImagePath;
            if (productImageUrl == null || productImageUrl.isEmpty()) {
                productImageUrl = selectedProduct.getImageUrl();
            }

            // Create or update offer object
            Offer offer;
            boolean isUpdate = editingOffer != null;
            
            if (isUpdate) {
                // Update existing offer
                offer = editingOffer;
                offer.setProductId(selectedProduct.getId());
                offer.setProductName(productName);
                offer.setCategory(category);
                offer.setOriginalPrice(originalPrice);
                offer.setDiscountedPrice(discountedPrice);
                offer.setStockQuantity(stockQuantity);
                offer.setDescription(description);
                offer.setActive(isActive);
                if (productImageUrl != null) {
                    offer.setImageUrl(productImageUrl);
                }
            } else {
                // Create new offer
                offer = new Offer();
                offer.setProductId(selectedProduct.getId()); // Always has valid product ID now
                offer.setProductName(productName);
                offer.setCategory(category);
                offer.setOriginalPrice(originalPrice);
                offer.setDiscountedPrice(discountedPrice);
                offer.setStockQuantity(stockQuantity);
                offer.setDescription(description);
                offer.setImageUrl(productImageUrl != null ? productImageUrl : "");
                offer.setActive(isActive);
                offer.setCreatedAt(System.currentTimeMillis());
                offer.setExpiresAt(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)); // 30 days
            }

            // Save to database
            long result;
            if (isUpdate) {
                result = databaseHelper.updateOffer(offer) ? 1 : -1;
            } else {
                android.util.Log.d("AddSpecialOfferFragment", "=== SAVING NEW OFFER ===");
                android.util.Log.d("AddSpecialOfferFragment", "Product ID: " + offer.getProductId());
                android.util.Log.d("AddSpecialOfferFragment", "Product Name: " + offer.getProductName());
                android.util.Log.d("AddSpecialOfferFragment", "Category: " + offer.getCategory());
                android.util.Log.d("AddSpecialOfferFragment", "Original Price: " + offer.getOriginalPrice());
                android.util.Log.d("AddSpecialOfferFragment", "Discounted Price: " + offer.getDiscountedPrice());
                android.util.Log.d("AddSpecialOfferFragment", "Stock: " + offer.getStockQuantity());
                android.util.Log.d("AddSpecialOfferFragment", "Active: " + offer.isActive());
                android.util.Log.d("AddSpecialOfferFragment", "Created At: " + offer.getCreatedAt());
                android.util.Log.d("AddSpecialOfferFragment", "Expires At: " + offer.getExpiresAt());
                
                result = databaseHelper.addOffer(offer);
                android.util.Log.d("AddSpecialOfferFragment", "Database insert result: " + result);
            }
            
            if (result != -1) {
                showSuccessMessage(isUpdate ? "Offer updated successfully!" : "Offer created successfully!");
                if (!isUpdate) {
                    clearForm();
                } else {
                    // Navigate back to manage offers
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                }
                
                // TODO: Sync to backend/server (REST API call)
                syncToBackend(offer);
                
            } else {
                showErrorMessage(isUpdate ? "Failed to update offer" : "Failed to save offer to database");
            }

        } catch (NumberFormatException e) {
            showErrorMessage("Please enter valid numbers for prices and quantity");
        } catch (Exception e) {
            showErrorMessage("An error occurred while saving the offer: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate product selection
        if (selectedProduct == null || TextUtils.isEmpty(productSpinner.getText())) {
            productSpinner.setError("Please select a product");
            isValid = false;
        } else {
            productSpinner.setError(null);
        }

        // Category is auto-filled from selected product, no need to validate

        // Validate original price
        if (TextUtils.isEmpty(originalPriceInput.getText())) {
            originalPriceLayout.setError("Original price is required");
            isValid = false;
        } else {
            try {
                double originalPrice = Double.parseDouble(originalPriceInput.getText().toString());
                if (originalPrice <= 0) {
                    originalPriceLayout.setError("Price must be greater than 0");
                    isValid = false;
                } else {
                    originalPriceLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                originalPriceLayout.setError("Enter a valid price");
                isValid = false;
            }
        }

        // Validate discounted price
        if (TextUtils.isEmpty(discountedPriceInput.getText())) {
            discountedPriceLayout.setError("Discounted price is required");
            isValid = false;
        } else {
            try {
                double discountedPrice = Double.parseDouble(discountedPriceInput.getText().toString());
                double originalPrice = TextUtils.isEmpty(originalPriceInput.getText()) ? 0 : 
                    Double.parseDouble(originalPriceInput.getText().toString());
                
                if (discountedPrice <= 0) {
                    discountedPriceLayout.setError("Discounted price must be greater than 0");
                    isValid = false;
                } else if (discountedPrice >= originalPrice) {
                    discountedPriceLayout.setError("Discounted price must be less than original price");
                    isValid = false;
                } else {
                    discountedPriceLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                discountedPriceLayout.setError("Enter a valid price");
                isValid = false;
            }
        }

        // Validate stock quantity
        if (TextUtils.isEmpty(stockQuantityInput.getText())) {
            stockQuantityLayout.setError("Stock quantity is required");
            isValid = false;
        } else {
            try {
                int quantity = Integer.parseInt(stockQuantityInput.getText().toString());
                if (quantity <= 0) {
                    stockQuantityLayout.setError("Quantity must be greater than 0");
                    isValid = false;
                } else {
                    stockQuantityLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                stockQuantityLayout.setError("Enter a valid quantity");
                isValid = false;
            }
        }

        // Validate description
        if (TextUtils.isEmpty(descriptionInput.getText())) {
            descriptionLayout.setError("Description is required");
            isValid = false;
        } else {
            descriptionLayout.setError(null);
        }

        return isValid;
    }

    private void syncToBackend(Offer offer) {
        // TODO: Implement REST API call to sync offer to backend
        // This is a placeholder for the backend synchronization
        // In a real app, you would make an HTTP request here
        
        // Example of what the API call might look like:
        /*
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<OfferResponse> call = apiService.createOffer(offer);
        call.enqueue(new Callback<OfferResponse>() {
            @Override
            public void onResponse(Call<OfferResponse> call, Response<OfferResponse> response) {
                if (response.isSuccessful()) {
                    // Offer synced successfully
                    Toast.makeText(getContext(), "Offer synced to server", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<OfferResponse> call, Throwable t) {
                // Handle sync failure
                Toast.makeText(getContext(), "Failed to sync to server", Toast.LENGTH_SHORT).show();
            }
        });
        */
        
        // For now, just show a message
        Toast.makeText(getContext(), "Offer saved locally (server sync would happen here)", Toast.LENGTH_SHORT).show();
    }

    private void showSuccessMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message + " 🎉", Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.success_green))
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                .show();
        }
    }

    private void showErrorMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error_red))
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                .show();
        }
    }

    private void clearForm() {
        productSpinner.setText("", false);
        categorySpinner.setText("", false);
        originalPriceInput.setText("");
        discountedPriceInput.setText("");
        stockQuantityInput.setText("");
        descriptionInput.setText("");
        imagePreview.setVisibility(View.GONE);
        imagePreview.setImageDrawable(null);
        selectedImageUri = null;
        selectedImagePath = null;
        selectedProduct = null;
        
        // Clear any errors
        originalPriceLayout.setError(null);
        discountedPriceLayout.setError(null);
        stockQuantityLayout.setError(null);
        descriptionLayout.setError(null);
        productSpinner.setError(null);
    }

    private void animateCardEntry() {
        if (formCard != null) {
            Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
            formCard.startAnimation(slideUp);
        }
    }

    private void animateButtonClick(View button) {
        Animation bounce = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);
        button.startAnimation(bounce);
    }
}
