package com.example.grocerystore.fragments;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.NavigationActivity;
import com.example.grocerystore.R;
import com.example.grocerystore.adapters.OffersAdapter;
import com.example.grocerystore.database.DatabaseHelper;
import com.example.grocerystore.models.Offer;
import com.example.grocerystore.models.Order;
import com.example.grocerystore.models.Product;
import com.example.grocerystore.model.User;
import com.example.grocerystore.utils.PreferencesManager;
import com.example.grocerystore.utils.UserManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OffersFragment extends Fragment implements OffersAdapter.OnOfferActionListener {

    private RecyclerView offersRecyclerView;
    private TextView offersCountText;
    private View emptyState, loadingState;
    private MaterialButton refreshOffersButton;
    private ImageView headerIcon;

    private OffersAdapter offersAdapter;
    private DatabaseHelper databaseHelper;
    private PreferencesManager preferencesManager;
    private UserManager userManager;
    private User currentUser;
    
    private List<Offer> allOffers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_offers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        android.util.Log.d("OffersFragment", "onViewCreated() called - Fragment is being created");
        
        if (getActivity() != null) {
            getActivity().setTitle("Special Offers");
        }

        initializeServices();
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadCurrentUser();
        
        // Test database connection immediately
        testDatabaseConnection();
        
        loadOffers();
    }
    
    private void testDatabaseConnection() {
        if (getContext() == null) {
            android.util.Log.e("OffersFragment", "Context is null, cannot test database connection");
            return;
        }
        
        DatabaseHelper testHelper = null;
        try {
            android.util.Log.d("OffersFragment", "=== DATABASE CONNECTION TEST ===");
            testHelper = new DatabaseHelper(getContext());
            List<Offer> testOffers = testHelper.getAllOffers();
            android.util.Log.d("OffersFragment", "Database connection successful. Total offers: " + testOffers.size());
            
            // If no offers exist, create some test offers
            if (testOffers.isEmpty()) {
                android.util.Log.d("OffersFragment", "No offers found, creating test offers...");
                testHelper.createTestOffers();
                testOffers = testHelper.getAllOffers(); // Reload after creating test offers
                android.util.Log.d("OffersFragment", "After creating test offers, total count: " + testOffers.size());
            }
            
            for (int i = 0; i < testOffers.size(); i++) {
                Offer offer = testOffers.get(i);
                android.util.Log.d("OffersFragment", "TEST - Offer " + (i+1) + ": " + offer.getProductName() + 
                    " | Active: " + offer.isActive() + 
                    " | Expires: " + new java.util.Date(offer.getExpiresAt()));
            }
            android.util.Log.d("OffersFragment", "=== END DATABASE TEST ===");
        } catch (Exception e) {
            android.util.Log.e("OffersFragment", "Database connection failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (testHelper != null) {
                testHelper.close();
            }
        }
    }

    private void initializeServices() {
        if (getContext() != null) {
            databaseHelper = new DatabaseHelper(getContext());
            preferencesManager = new PreferencesManager(getContext());
            userManager = new UserManager(getContext());
        } else {
            android.util.Log.e("OffersFragment", "Context is null, cannot initialize services");
        }
        allOffers = new ArrayList<>();
    }

    private void initializeViews(View view) {
        offersRecyclerView = view.findViewById(R.id.offers_recycler_view);
        offersCountText = view.findViewById(R.id.offers_count_text);
        emptyState = view.findViewById(R.id.empty_state);
        loadingState = view.findViewById(R.id.loading_state);
        refreshOffersButton = view.findViewById(R.id.refresh_offers_button);
        headerIcon = view.findViewById(R.id.header_icon);
    }

    private void setupRecyclerView() {
        if (getContext() != null) {
            offersAdapter = new OffersAdapter(getContext(), allOffers, this);
            
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
            offersRecyclerView.setLayoutManager(layoutManager);
            offersRecyclerView.setAdapter(offersAdapter);
        } else {
            android.util.Log.e("OffersFragment", "Context is null, cannot setup RecyclerView");
        }
    }

    private void setupClickListeners() {
        if (refreshOffersButton != null) {
            refreshOffersButton.setOnClickListener(v -> {
                animateRefreshIcon();
                android.util.Log.d("OffersFragment", "Manual refresh button clicked");
                loadOffers();
            });
        }
        
        // Add long click listener for testing - creates a test offer
        if (refreshOffersButton != null) {
            refreshOffersButton.setOnLongClickListener(v -> {
                android.util.Log.d("OffersFragment", "Creating test offer for debugging...");
                createTestOffer();
                return true;
            });
        }
    }
    
    private void createTestOffer() {
        try {
            android.util.Log.d("OffersFragment", "=== CREATING PROPER TEST OFFER ===");
            
            // First check existing products to create realistic offers
            List<Product> products = databaseHelper.getAllProducts();
            android.util.Log.d("OffersFragment", "Available products: " + products.size());
            
            for (Product product : products) {
                android.util.Log.d("OffersFragment", "Product: " + product.getName() + " (ID: " + product.getId() + ")");
            }
            
            // Create offer based on existing product
            Offer testOffer = new Offer();
            if (!products.isEmpty()) {
                Product baseProduct = products.get(0); // Use first available product
                testOffer.setProductName(baseProduct.getName());
                testOffer.setName(baseProduct.getName()); // Set both fields
                testOffer.setCategory(baseProduct.getCategory());
                testOffer.setOriginalPrice(baseProduct.getPrice());
                testOffer.setDiscountedPrice(baseProduct.getPrice() * 0.8); // 20% discount
                testOffer.setImageUrl(baseProduct.getImageUrl());
                android.util.Log.d("OffersFragment", "Creating offer for existing product: " + baseProduct.getName());
            } else {
                // Fallback to generic offer
                testOffer.setProductName("Fresh Avocados");
                testOffer.setName("Fresh Avocados");
                testOffer.setCategory("Fruits");
                testOffer.setOriginalPrice(5.00);
                testOffer.setDiscountedPrice(3.50); // 30% discount
                testOffer.setImageUrl("drawable://product_avocado");
                android.util.Log.d("OffersFragment", "Creating generic avocado offer");
            }
            
            testOffer.setStockQuantity(50);
            testOffer.setDescription("Fresh and delicious - limited time offer!");
            testOffer.setActive(true);
            testOffer.setCreatedAt(System.currentTimeMillis());
            testOffer.setExpiresAt(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)); // 30 days
            
            android.util.Log.d("OffersFragment", "Test offer details:");
            android.util.Log.d("OffersFragment", "  - Product Name: " + testOffer.getProductName());
            android.util.Log.d("OffersFragment", "  - Active: " + testOffer.isActive());
            android.util.Log.d("OffersFragment", "  - Created At: " + testOffer.getCreatedAt());
            android.util.Log.d("OffersFragment", "  - Expires At: " + testOffer.getExpiresAt());
            android.util.Log.d("OffersFragment", "  - Image URL: " + testOffer.getImageUrl());
            
            // Test direct database access
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("product_name", testOffer.getProductName());
            values.put("category", testOffer.getCategory());
            values.put("original_price", testOffer.getOriginalPrice());
            values.put("discounted_price", testOffer.getDiscountedPrice());
            values.put("stock_quantity", testOffer.getStockQuantity());
            values.put("description", testOffer.getDescription());
            values.put("image_url", testOffer.getImageUrl());
            values.put("is_active", 1);
            values.put("created_at", testOffer.getCreatedAt());
            values.put("expires_at", testOffer.getExpiresAt());
            
            long directResult = db.insert("offers", null, values);
            android.util.Log.d("OffersFragment", "✅ Direct database insert result: " + directResult);
            
            // Also try with the helper method
            long helperResult = databaseHelper.addOffer(testOffer);
            android.util.Log.d("OffersFragment", "✅ Helper method insert result: " + helperResult);
            
            if (directResult != -1 || helperResult != -1) {
                Toast.makeText(getContext(), "Test offer created! Refreshing...", Toast.LENGTH_SHORT).show();
                loadOffers(); // Refresh immediately
            } else {
                android.util.Log.e("OffersFragment", "❌ Both insert methods failed");
                Toast.makeText(getContext(), "Failed to create test offer", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.util.Log.e("OffersFragment", "Error creating test offer: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getContext(), "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Public method to refresh offers - can be called from other fragments
    public void refreshOffers() {
        android.util.Log.d("OffersFragment", "External refresh request received");
        if (isAdded() && !isDetached()) {
            loadOffers();
        }
    }

    private void animateRefreshIcon() {
        if (headerIcon != null) {
            headerIcon.animate()
                    .rotation(360f)
                    .setDuration(500)
                    .withEndAction(() -> headerIcon.setRotation(0f))
                    .start();
        }
    }

    private void loadCurrentUser() {
        String email = preferencesManager.getLoggedInUserEmail();
        if (email != null && !email.isEmpty()) {
            currentUser = userManager.getUserByEmail(email);
        }
    }

    private void loadOffers() {
        showLoadingState();
        
        android.util.Log.d("OffersFragment", "=== SIMPLIFIED LOADING (LIKE ADMIN) ===");
        
        // Clear previous offers
        allOffers.clear();
        
        // Use the EXACT same approach as admin (which works)
        allOffers = databaseHelper.getAllOffers();
        
        android.util.Log.d("OffersFragment", "Loaded " + allOffers.size() + " offers using admin approach");
        
        // Log each offer
        for (int i = 0; i < allOffers.size(); i++) {
            Offer offer = allOffers.get(i);
            android.util.Log.d("OffersFragment", "Offer " + (i+1) + ": " + offer.getProductName() + 
                " | Active: " + offer.isActive());
        }
        
        android.util.Log.d("OffersFragment", "=== END SIMPLIFIED LOADING ===");
        
        updateUI();
        updateOffersCount();
    }

    private void updateUI() {
        android.util.Log.d("OffersFragment", "=== UPDATE UI DEBUG ===");
        android.util.Log.d("OffersFragment", "allOffers.size(): " + allOffers.size());
        android.util.Log.d("OffersFragment", "allOffers.isEmpty(): " + allOffers.isEmpty());
        
        if (allOffers.isEmpty()) {
            android.util.Log.d("OffersFragment", "❌ Showing empty state because allOffers is empty");
            showEmptyState();
        } else {
            android.util.Log.d("OffersFragment", "✅ Showing offers state with " + allOffers.size() + " offers");
            showOffersState();
            if (offersAdapter != null) {
                android.util.Log.d("OffersFragment", "✅ Updating adapter with offers");
                offersAdapter.updateOffers(allOffers);
            } else {
                android.util.Log.e("OffersFragment", "❌ offersAdapter is null!");
            }
        }
        android.util.Log.d("OffersFragment", "=== END UPDATE UI DEBUG ===");
    }

    private void updateOffersCount() {
        if (offersCountText != null) {
            int count = allOffers.size();
            String countText = count + " special offer" + (count != 1 ? "s" : "") + " available";
            offersCountText.setText(countText);
            offersCountText.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void showLoadingState() {
        if (loadingState != null) loadingState.setVisibility(View.VISIBLE);
        if (emptyState != null) emptyState.setVisibility(View.GONE);
        if (offersRecyclerView != null) offersRecyclerView.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        if (loadingState != null) loadingState.setVisibility(View.GONE);
        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        if (offersRecyclerView != null) offersRecyclerView.setVisibility(View.GONE);
    }

    private void showOffersState() {
        if (loadingState != null) loadingState.setVisibility(View.GONE);
        if (emptyState != null) emptyState.setVisibility(View.GONE);
        if (offersRecyclerView != null) offersRecyclerView.setVisibility(View.VISIBLE);
    }

    // OffersAdapter.OnOfferActionListener implementation
    @Override
    public void onAddToFavorites(Offer offer) {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to add favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = Math.abs(currentUser.getEmail().hashCode()) % 1000;
        
        android.util.Log.d("OffersFragment", "=== ADD TO FAVORITES DEBUG ===");
        android.util.Log.d("OffersFragment", "User ID: " + userId);
        android.util.Log.d("OffersFragment", "Offer Product ID: " + offer.getProductId());
        android.util.Log.d("OffersFragment", "Offer Name: " + offer.getName());
        
        // Check if it's already in favorites first
        boolean isAlreadyFavorite = databaseHelper.isProductFavorite(userId, offer.getProductId());
        android.util.Log.d("OffersFragment", "Is already favorite: " + isAlreadyFavorite);
        
        if (isAlreadyFavorite) {
            Toast.makeText(getContext(), offer.getName() + " is already in your favorites! ❤️", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Try to add to favorites
        boolean success = databaseHelper.addToFavorites(userId, offer.getProductId());
        android.util.Log.d("OffersFragment", "Add to favorites result: " + success);
        
        if (success) {
            Toast.makeText(getContext(), "✅ " + offer.getName() + " added to favorites! ❤️", Toast.LENGTH_SHORT).show();
            animateHeartPop(offer);
        } else {
            Toast.makeText(getContext(), "❌ Failed to add to favorites. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRemoveFromFavorites(Offer offer) {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to manage favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = Math.abs(currentUser.getEmail().hashCode()) % 1000;
        
        if (databaseHelper.removeFromFavorites(userId, offer.getProductId())) {
            Toast.makeText(getContext(), offer.getName() + " removed from favorites!", Toast.LENGTH_SHORT).show();
            animateHeartBreak(offer);
        } else {
            Toast.makeText(getContext(), "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onOrderOffer(Offer offer) {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to place orders", Toast.LENGTH_SHORT).show();
            return;
        }

        showOrderDialog(offer);
    }

    private void showOrderDialog(Offer offer) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_offer_order);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Initialize dialog views
        TextView productName = dialog.findViewById(R.id.product_name);
        TextView originalPrice = dialog.findViewById(R.id.original_price);
        TextView discountedPrice = dialog.findViewById(R.id.discounted_price);
        TextView discountBadge = dialog.findViewById(R.id.discount_badge);
        TextView quantityText = dialog.findViewById(R.id.quantity_text);
        TextView totalAmount = dialog.findViewById(R.id.total_amount);
        MaterialButton decreaseBtn = dialog.findViewById(R.id.decrease_quantity);
        MaterialButton increaseBtn = dialog.findViewById(R.id.increase_quantity);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancel_button);
        MaterialButton confirmOrderButton = dialog.findViewById(R.id.confirm_order_button);

        // Set product info
        productName.setText(offer.getName());
        originalPrice.setText("$" + String.format("%.2f", offer.getOriginalPrice()));
        discountedPrice.setText("$" + String.format("%.2f", offer.getDiscountedPrice()));
        discountBadge.setText(offer.getDiscountPercent() + "% OFF");
        
        // Apply strikethrough to original price
        originalPrice.setPaintFlags(originalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

        final int[] quantity = {1};
        quantityText.setText(String.valueOf(quantity[0]));

        // Quantity controls
        decreaseBtn.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                quantityText.setText(String.valueOf(quantity[0]));
                updateTotalPrice(offer, quantity[0], totalAmount);
            }
        });

        increaseBtn.setOnClickListener(v -> {
            quantity[0]++;
            quantityText.setText(String.valueOf(quantity[0]));
            updateTotalPrice(offer, quantity[0], totalAmount);
        });

        // Initial total price
        updateTotalPrice(offer, quantity[0], totalAmount);

        // Cancel button
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Confirm order
        confirmOrderButton.setOnClickListener(v -> {
            // Get offer product name - try both getName() and getProductName()
            String offerProductName = offer.getName();
            if (offerProductName == null || offerProductName.trim().isEmpty()) {
                offerProductName = offer.getProductName();
            }
            
            android.util.Log.d("OffersFragment", "=== ORDER VALIDATION DEBUG ===");
            android.util.Log.d("OffersFragment", "Offer ID: " + offer.getId());
            android.util.Log.d("OffersFragment", "Offer getName(): " + offer.getName());
            android.util.Log.d("OffersFragment", "Offer getProductName(): " + offer.getProductName());
            android.util.Log.d("OffersFragment", "Offer getProductId(): " + offer.getProductId());
            android.util.Log.d("OffersFragment", "Final offerProductName: " + offerProductName);
            
            // Find the actual product to ensure valid productId
            int validProductId = offer.getProductId();
            
            // Always try to find product by name since offers don't have reliable productId
            android.util.Log.d("OffersFragment", "Searching for product by name: " + offerProductName);
            List<Product> allProducts = databaseHelper.getAllProducts();
            android.util.Log.d("OffersFragment", "Total products in database: " + allProducts.size());
            
            for (Product product : allProducts) {
                android.util.Log.d("OffersFragment", "Checking product: " + product.getName() + " (ID: " + product.getId() + ")");
                
                // Try exact match first
                if (product.getName().equalsIgnoreCase(offerProductName)) {
                    validProductId = product.getId();
                    android.util.Log.d("OffersFragment", "✅ Found exact match with ID: " + validProductId);
                    break;
                }
                
                // Try partial match (contains)
                if (product.getName().toLowerCase().contains(offerProductName.toLowerCase()) || 
                    offerProductName.toLowerCase().contains(product.getName().toLowerCase())) {
                    validProductId = product.getId();
                    android.util.Log.d("OffersFragment", "✅ Found partial match with ID: " + validProductId);
                    break;
                }
            }
            
            // If still no valid product found, create a mock product for the order
            if (validProductId <= 0) {
                android.util.Log.d("OffersFragment", "⚠️ No matching product found, using offer data directly");
                validProductId = 999; // Use a mock ID for offers without matching products
            }
            
            // Create order with valid productId
            Order order = new Order(
                Math.abs(preferencesManager.getLoggedInUserEmail().hashCode()) % 1000,
                validProductId, // Use validated productId
                offerProductName, // Use the renamed variable
                quantity[0],
                offer.getDiscountedPrice(), // Use discounted price
                "pickup" // Default to pickup for now
            );

            // Set order date
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            order.setOrderDate(currentDate);

            // Save order to database
            long orderId = databaseHelper.addOrder(order);

            if (orderId > 0) {
                double savings = (offer.getOriginalPrice() - offer.getDiscountedPrice()) * quantity[0];
                Toast.makeText(getContext(), 
                    "Order placed successfully! You saved $" + String.format("%.2f", savings), 
                    Toast.LENGTH_LONG).show();
                dialog.dismiss();
                animateOfferSuccess(offer);
                
                // Refresh offers to show updated stock
                loadOffers();
            } else if (orderId == -2) {
                Toast.makeText(getContext(), "Sorry, insufficient stock available!", Toast.LENGTH_SHORT).show();
            } else if (orderId == -1) {
                Toast.makeText(getContext(), "Product not found!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updateTotalPrice(Offer offer, int quantity, TextView totalPriceView) {
        double total = offer.getDiscountedPrice() * quantity;
        double originalTotal = offer.getOriginalPrice() * quantity;
        double savings = originalTotal - total;
        
        String totalText = "Total: $" + String.format("%.2f", total) + 
                          "\nYou save: $" + String.format("%.2f", savings);
        totalPriceView.setText(totalText);
    }

    private void animateHeartPop(Offer offer) {
        // Find the view holder for this offer and animate the heart
        for (int i = 0; i < offersRecyclerView.getChildCount(); i++) {
            RecyclerView.ViewHolder viewHolder = offersRecyclerView.getChildViewHolder(offersRecyclerView.getChildAt(i));
            if (viewHolder instanceof OffersAdapter.OfferViewHolder) {
                OffersAdapter.OfferViewHolder offerVH = (OffersAdapter.OfferViewHolder) viewHolder;
                if (offerVH.getCurrentOffer() != null && offerVH.getCurrentOffer().getProductId() == offer.getProductId()) {
                    offerVH.animateHeartPop();
                    break;
                }
            }
        }
    }

    private void animateHeartBreak(Offer offer) {
        // Similar to animateHeartPop but with different animation
        for (int i = 0; i < offersRecyclerView.getChildCount(); i++) {
            RecyclerView.ViewHolder viewHolder = offersRecyclerView.getChildViewHolder(offersRecyclerView.getChildAt(i));
            if (viewHolder instanceof OffersAdapter.OfferViewHolder) {
                OffersAdapter.OfferViewHolder offerVH = (OffersAdapter.OfferViewHolder) viewHolder;
                if (offerVH.getCurrentOffer() != null && offerVH.getCurrentOffer().getProductId() == offer.getProductId()) {
                    offerVH.animateHeartBreak();
                    break;
                }
            }
        }
    }

    private void animateOfferSuccess(Offer offer) {
        // Create a success animation (sparkle effect)
        Toast.makeText(getContext(), "🎉 Great deal! Order confirmed with " + offer.getDiscountPercent() + "% savings!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh offers when fragment becomes visible to get fresh admin-created offers
        android.util.Log.d("OffersFragment", "Fragment resumed, reloading offers...");
        loadCurrentUser();
        loadOffers(); // Reload fresh offers from database
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear adapter to prevent memory leaks
        if (offersAdapter != null) {
            offersAdapter = null;
        }
        if (offersRecyclerView != null) {
            offersRecyclerView.setAdapter(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close database helper to prevent memory leaks
        if (databaseHelper != null) {
            databaseHelper.close();
            databaseHelper = null;
        }
    }
}
