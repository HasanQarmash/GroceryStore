package com.example.grocerystore.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grocerystore.NavigationActivity;
import com.example.grocerystore.R;
import com.example.grocerystore.adapters.ProductAdapter;
import com.example.grocerystore.database.DatabaseHelper;
import com.example.grocerystore.models.Order;
import com.example.grocerystore.models.Product;
import com.example.grocerystore.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductsFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private DatabaseHelper databaseHelper;
    private TextInputEditText searchEditText;
    private MaterialButton filterButton;
    private ChipGroup categoryChipGroup;
    private TextView resultsCount;
    private View emptyState;
    
    private List<Product> allProducts;
    private String currentSearchQuery = "";
    private String currentCategory = "All Categories";
    private double minPrice = 0.0;
    private double maxPrice = 100.0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupDatabase();
        setupRecyclerView();
        setupSearchAndFilters();
        loadProducts();
        loadCategories();
    }

    private void initializeViews(View view) {
        productsRecyclerView = view.findViewById(R.id.products_recycler_view);
        searchEditText = view.findViewById(R.id.search_edit_text);
        filterButton = view.findViewById(R.id.filter_button);
        categoryChipGroup = view.findViewById(R.id.category_chip_group);
        resultsCount = view.findViewById(R.id.results_count);
        emptyState = view.findViewById(R.id.empty_state);
    }

    private void setupDatabase() {
        databaseHelper = new DatabaseHelper(getContext());
    }

    private void setupRecyclerView() {
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialize with empty list first, will be updated in loadProducts()
        if (allProducts == null) {
            allProducts = new ArrayList<>();
        }
        productAdapter = new ProductAdapter(getContext(), allProducts);
        productAdapter.setOnProductClickListener(this);
        productsRecyclerView.setAdapter(productAdapter);
    }

    private void setupSearchAndFilters() {
        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Category filter
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    currentCategory = selectedChip.getText().toString();
                    applyFilters();
                }
            }
        });

        // Filter button
        filterButton.setOnClickListener(v -> showFilterDialog());
    }

    private void loadProducts() {
        allProducts = databaseHelper.getAllProducts();
        
        // Check favorites for current user
        User currentUser = getCurrentUser();
        int userId = 1; // Default user ID for demo
        if (currentUser != null) {
            userId = Math.abs(currentUser.getEmail().hashCode()) % 1000; // Generate consistent ID from email
            for (Product product : allProducts) {
                boolean isFavorite = databaseHelper.isProductFavorite(userId, product.getId());
                product.setFavorite(isFavorite);
            }
        }
        
        if (productAdapter != null) {
            productAdapter.updateProducts(allProducts);
        }
        updateResultsCount();
        checkEmptyState();
    }

    private void loadCategories() {
        List<String> categories = databaseHelper.getAllCategories();
        
        // Clear existing chips except "All Categories"
        int childCount = categoryChipGroup.getChildCount();
        for (int i = childCount - 1; i > 0; i--) {
            categoryChipGroup.removeViewAt(i);
        }
        
        // Add category chips
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String category : categories) {
            Chip chip = (Chip) inflater.inflate(R.layout.chip_filter_category, categoryChipGroup, false);
            chip.setText(category);
            chip.setCheckable(true);
            categoryChipGroup.addView(chip);
        }
    }

    private void applyFilters() {
        if (productAdapter != null) {
            productAdapter.applyFilters(currentSearchQuery, currentCategory, minPrice, maxPrice);
            updateResultsCount();
            checkEmptyState();
        }
    }

    private void updateResultsCount() {
        if (productAdapter != null) {
            int count = productAdapter.getItemCount();
            String text = count + " product" + (count != 1 ? "s" : "") + " found";
            resultsCount.setText(text);
        }
    }

    private void checkEmptyState() {
        if (productAdapter != null) {
            boolean isEmpty = productAdapter.getItemCount() == 0;
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            productsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        // Initialize filter views
        TextInputEditText minPriceEdit = dialogView.findViewById(R.id.min_price_edit);
        TextInputEditText maxPriceEdit = dialogView.findViewById(R.id.max_price_edit);
        MaterialButton clearFiltersButton = dialogView.findViewById(R.id.clear_filters_button);
        MaterialButton applyFiltersButton = dialogView.findViewById(R.id.apply_filters_button);
        
        // Set current values
        minPriceEdit.setText(String.valueOf(minPrice));
        maxPriceEdit.setText(String.valueOf(maxPrice));
        
        // Clear filters
        clearFiltersButton.setOnClickListener(v -> {
            minPrice = 0.0;
            maxPrice = 100.0;
            currentCategory = "All Categories";
            currentSearchQuery = "";
            
            // Reset UI
            searchEditText.setText("");
            categoryChipGroup.check(R.id.chip_all);
            
            applyFilters();
            dialog.dismiss();
        });
        
        // Apply filters
        applyFiltersButton.setOnClickListener(v -> {
            try {
                String minText = minPriceEdit.getText().toString().trim();
                String maxText = maxPriceEdit.getText().toString().trim();
                
                if (!minText.isEmpty()) {
                    minPrice = Double.parseDouble(minText);
                }
                if (!maxText.isEmpty()) {
                    maxPrice = Double.parseDouble(maxText);
                }
                
                if (minPrice > maxPrice) {
                    Toast.makeText(getContext(), "Min price cannot be greater than max price", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                applyFilters();
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter valid price values", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }

    @Override
    public void onFavoriteClick(Product product, int position) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to add favorites", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int userId = Math.abs(currentUser.getEmail().hashCode()) % 1000; // Generate consistent ID from email
        boolean newFavoriteState = !product.isFavorite();
        boolean success;
        
        if (newFavoriteState) {
            success = databaseHelper.addToFavorites(userId, product.getId());
        } else {
            success = databaseHelper.removeFromFavorites(userId, product.getId());
        }
        
        if (success) {
            product.setFavorite(newFavoriteState);
            productAdapter.notifyItemChanged(position);
            
            // Add heart animation
            if (newFavoriteState) {
                animateHeartPop(position);
                showHeartBounce();
            } else {
                animateHeartBreak(position);
            }
            
            String message = newFavoriteState ? "Added to favorites ❤️" : "Removed from favorites 💔";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to update favorites", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onOrderClick(Product product) {
        showOrderDialog(product);
    }

    private void showOrderDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_order_form, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        // Initialize views
        TextView productNameDialog = dialogView.findViewById(R.id.product_name_dialog);
        TextView productPriceDialog = dialogView.findViewById(R.id.product_price_dialog);
        TextView quantityText = dialogView.findViewById(R.id.quantity_text);
        TextView totalPrice = dialogView.findViewById(R.id.total_price);
        MaterialButton decreaseQuantity = dialogView.findViewById(R.id.decrease_quantity);
        MaterialButton increaseQuantity = dialogView.findViewById(R.id.increase_quantity);
        RadioGroup deliveryMethodGroup = dialogView.findViewById(R.id.delivery_method_group);
        TextInputEditText deliveryAddress = dialogView.findViewById(R.id.delivery_address);
        View addressLayout = dialogView.findViewById(R.id.address_layout);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancel_button);
        MaterialButton confirmOrderButton = dialogView.findViewById(R.id.confirm_order_button);
        
        // Set product info
        productNameDialog.setText(product.getName());
        productPriceDialog.setText(product.getFormattedPrice());
        
        // Quantity management
        final int[] quantity = {1};
        final double[] deliveryFee = {0.0};
        
        updateTotalPrice(product, quantity[0], deliveryFee[0], totalPrice);
        
        decreaseQuantity.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                quantityText.setText(String.valueOf(quantity[0]));
                updateTotalPrice(product, quantity[0], deliveryFee[0], totalPrice);
            }
        });
        
        increaseQuantity.setOnClickListener(v -> {
            if (quantity[0] < product.getStockQuantity()) {
                quantity[0]++;
                quantityText.setText(String.valueOf(quantity[0]));
                updateTotalPrice(product, quantity[0], deliveryFee[0], totalPrice);
            } else {
                Toast.makeText(getContext(), "Not enough stock available", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Delivery method
        deliveryMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_home_delivery) {
                deliveryFee[0] = 5.99;
                addressLayout.setVisibility(View.VISIBLE);
            } else {
                deliveryFee[0] = 0.0;
                addressLayout.setVisibility(View.GONE);
            }
            updateTotalPrice(product, quantity[0], deliveryFee[0], totalPrice);
        });
        
        // Cancel button
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        // Confirm order
        confirmOrderButton.setOnClickListener(v -> {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(), "Please log in to place orders", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String deliveryMethod = deliveryMethodGroup.getCheckedRadioButtonId() == R.id.radio_home_delivery ? 
                                  "home" : "pickup";
            
            if (deliveryMethod.equals("home")) {
                String address = deliveryAddress.getText().toString().trim();
                if (address.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter delivery address", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Create order
            Order order = new Order(
                Math.abs(currentUser.getEmail().hashCode()) % 1000, // Generate consistent ID from email
                product.getId(),
                product.getName(),
                quantity[0],
                product.getPrice(),
                deliveryMethod
            );
            
            // Set delivery address if home delivery
            if (deliveryMethod.equals("home")) {
                order.setDeliveryAddress(deliveryAddress.getText().toString().trim());
            }
            
            // Set order date
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            order.setOrderDate(currentDate);
            
            // Save order to database
            long orderId = databaseHelper.addOrder(order);
            
            if (orderId > 0) {
                Toast.makeText(getContext(), "Order placed successfully!", Toast.LENGTH_LONG).show();
                dialog.dismiss();
                
                // Refresh the products list to show updated stock
                loadProducts();
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

    private void updateTotalPrice(Product product, int quantity, double deliveryFee, TextView totalPriceView) {
        double total = (product.getPrice() * quantity) + deliveryFee;
        totalPriceView.setText(String.format(Locale.getDefault(), "$%.2f", total));
    }

    // Animation Methods for Favorites
    private void animateHeartPop(int position) {
        RecyclerView.ViewHolder viewHolder = productsRecyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            View favoriteIcon = viewHolder.itemView.findViewById(R.id.favorite_button);
            if (favoriteIcon != null) {
                android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(favoriteIcon, "scaleX", 1f, 1.5f, 1f);
                android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(favoriteIcon, "scaleY", 1f, 1.5f, 1f);
                
                android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
                animatorSet.playTogether(scaleX, scaleY);
                animatorSet.setDuration(300);
                animatorSet.setInterpolator(new android.view.animation.BounceInterpolator());
                animatorSet.start();
            }
        }
    }

    private void animateHeartBreak(int position) {
        RecyclerView.ViewHolder viewHolder = productsRecyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            View favoriteIcon = viewHolder.itemView.findViewById(R.id.favorite_button);
            if (favoriteIcon != null) {
                android.animation.ObjectAnimator shake = android.animation.ObjectAnimator.ofFloat(favoriteIcon, "translationX", 
                    0f, 10f, -10f, 10f, -10f, 5f, -5f, 0f);
                shake.setDuration(500);
                shake.start();
            }
        }
    }

    private void showHeartBounce() {
        // Create a floating heart animation overlay
        if (getView() != null) {
            android.widget.ImageView floatingHeart = new android.widget.ImageView(getContext());
            floatingHeart.setImageResource(R.drawable.ic_favorite_24);
            floatingHeart.setColorFilter(getResources().getColor(R.color.error_red));
            
            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = android.view.Gravity.CENTER;
            
            if (getView() instanceof android.view.ViewGroup) {
                ((android.view.ViewGroup) getView()).addView(floatingHeart, params);
                
                floatingHeart.setScaleX(0f);
                floatingHeart.setScaleY(0f);
                floatingHeart.setAlpha(0f);
                
                android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(floatingHeart, "scaleX", 0f, 2f, 1.5f, 0f);
                android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(floatingHeart, "scaleY", 0f, 2f, 1.5f, 0f);
                android.animation.ObjectAnimator alpha = android.animation.ObjectAnimator.ofFloat(floatingHeart, "alpha", 0f, 1f, 1f, 0f);
                android.animation.ObjectAnimator translateY = android.animation.ObjectAnimator.ofFloat(floatingHeart, "translationY", 0f, -200f);
                
                android.animation.AnimatorSet heartAnimation = new android.animation.AnimatorSet();
                heartAnimation.playTogether(scaleX, scaleY, alpha, translateY);
                heartAnimation.setDuration(1500);
                heartAnimation.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
                
                heartAnimation.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        if (getView() instanceof android.view.ViewGroup) {
                            ((android.view.ViewGroup) getView()).removeView(floatingHeart);
                        }
                    }
                });
                
                heartAnimation.start();
            }
        }
    }

    private User getCurrentUser() {
        if (getActivity() instanceof NavigationActivity) {
            return ((NavigationActivity) getActivity()).getCurrentUser();
        }
        return null;
    }

    public void refreshProducts() {
        loadProducts();
    }
}
