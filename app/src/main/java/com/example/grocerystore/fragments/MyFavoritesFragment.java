package com.example.grocerystore.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.NavigationActivity;
import com.example.grocerystore.R;
import com.example.grocerystore.adapters.EnhancedFavoritesAdapter;
import com.example.grocerystore.database.DatabaseHelper;
import com.example.grocerystore.model.User;
import com.example.grocerystore.models.Order;
import com.example.grocerystore.models.Product;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyFavoritesFragment extends Fragment implements EnhancedFavoritesAdapter.OnFavoriteActionListener {
    
    private RecyclerView favoritesRecyclerView;
    private EnhancedFavoritesAdapter favoritesAdapter;
    private DatabaseHelper databaseHelper;
    private View emptyState;
    private MaterialButton browseProductsButton;
    private TextView favoritesCountText;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_enhanced_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            getActivity().setTitle("My Favorites");
        }
        
        initializeViews(view);
        setupDatabase();
        setupRecyclerView();
        setupSwipeToRemove();
        loadFavoriteProducts();
    }

    private void initializeViews(View view) {
        favoritesRecyclerView = view.findViewById(R.id.favorites_recycler_view);
        emptyState = view.findViewById(R.id.empty_state);
        browseProductsButton = view.findViewById(R.id.browse_products_button);
        favoritesCountText = view.findViewById(R.id.favorites_count_text);
        
        if (browseProductsButton != null) {
            browseProductsButton.setOnClickListener(v -> navigateToProducts());
        }
    }

    private void setupDatabase() {
        databaseHelper = new DatabaseHelper(getContext());
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        favoritesRecyclerView.setLayoutManager(layoutManager);
        
        // Add item animations
        favoritesRecyclerView.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
    }

    private void setupSwipeToRemove() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (favoritesAdapter != null && position >= 0) {
                    Product product = favoritesAdapter.getProduct(position);
                    removeFromFavoritesWithAnimation(product, position);
                }
            }
        };
        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(favoritesRecyclerView);
    }

    private void loadFavoriteProducts() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            showEmptyState();
            return;
        }
        
        int userId = Math.abs(currentUser.getEmail().hashCode()) % 1000;
        List<Product> favoriteProducts = databaseHelper.getFavoriteProducts(userId);
        
        // Debug: Check what image URLs favorites have
        android.util.Log.d("MyFavoritesFragment", "=== LOADING FAVORITES DEBUG ===");
        android.util.Log.d("MyFavoritesFragment", "User ID: " + userId);
        android.util.Log.d("MyFavoritesFragment", "Total favorites found: " + favoriteProducts.size());
        
        for (Product product : favoriteProducts) {
            android.util.Log.d("MyFavoritesFragment", "FAVORITE PRODUCT: " +
                product.getName() + " | Image URL: " + product.getImageUrl() + 
                " | Category: " + product.getCategory());
        }
        
        if (favoriteProducts.isEmpty()) {
            showEmptyState();
        } else {
            showFavoriteProducts(favoriteProducts);
        }
        
        updateFavoritesCount(favoriteProducts.size());
    }

    private void showFavoriteProducts(List<Product> favoriteProducts) {
        favoritesAdapter = new EnhancedFavoritesAdapter(getContext(), favoriteProducts);
        favoritesAdapter.setOnFavoriteActionListener(this);
        favoritesRecyclerView.setAdapter(favoritesAdapter);
        
        emptyState.setVisibility(View.GONE);
        favoritesRecyclerView.setVisibility(View.VISIBLE);
        
        // Animate the RecyclerView appearance
        animateRecyclerViewEntrance();
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        favoritesRecyclerView.setVisibility(View.GONE);
        
        // Animate empty state
        animateEmptyStateEntrance();
    }

    private void updateFavoritesCount(int count) {
        if (favoritesCountText != null) {
            if (count > 0) {
                favoritesCountText.setVisibility(View.VISIBLE);
                favoritesCountText.setText(String.format(Locale.getDefault(), "%d favorite%s", count, count == 1 ? "" : "s"));
                
                // Animate count update
                animateCountUpdate();
            } else {
                favoritesCountText.setVisibility(View.GONE);
            }
        }
    }

    private void navigateToProducts() {
        if (getActivity() instanceof NavigationActivity) {
            NavigationActivity activity = (NavigationActivity) getActivity();
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProductsFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onRemoveFromFavorites(Product product, int position) {
        removeFromFavoritesWithAnimation(product, position);
    }

    @Override
    public void onOrderProduct(Product product) {
        showOrderDialog(product);
    }

    private void removeFromFavoritesWithAnimation(Product product, int position) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to manage favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = Math.abs(currentUser.getEmail().hashCode()) % 1000;
        
        // Animate removal
        animateItemRemoval(position, () -> {
            boolean success = databaseHelper.removeFromFavorites(userId, product.getId());
            
            if (success) {
                product.setFavorite(false);
                
                if (favoritesAdapter != null) {
                    favoritesAdapter.removeItem(position);
                    updateFavoritesCount(favoritesAdapter.getItemCount());
                    
                    if (favoritesAdapter.getItemCount() == 0) {
                        showEmptyState();
                    }
                }
                
                // Show snackbar with undo option
                showUndoSnackbar(product, position, userId);
                
            } else {
                // Re-add item if removal failed
                if (favoritesAdapter != null) {
                    favoritesAdapter.notifyItemChanged(position);
                }
                Toast.makeText(getContext(), "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUndoSnackbar(Product product, int position, int userId) {
        if (getView() != null) {
            Snackbar snackbar = Snackbar.make(getView(), 
                "Removed " + product.getName() + " from favorites", 
                Snackbar.LENGTH_LONG);
            
            snackbar.setAction("UNDO", v -> {
                // Re-add to favorites
                boolean success = databaseHelper.addToFavorites(userId, product.getId());
                if (success) {
                    product.setFavorite(true);
                    loadFavoriteProducts(); // Reload to get proper order
                    
                    // Animate re-addition with heart pop
                    animateHeartPop();
                    
                    Toast.makeText(getContext(), "Added back to favorites", Toast.LENGTH_SHORT).show();
                }
            });
            
            snackbar.show();
        }
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
                
                // Animate quantity change
                animateQuantityChange(quantityText, false);
            }
        });
        
        increaseQuantity.setOnClickListener(v -> {
            if (quantity[0] < product.getStockQuantity()) {
                quantity[0]++;
                quantityText.setText(String.valueOf(quantity[0]));
                updateTotalPrice(product, quantity[0], deliveryFee[0], totalPrice);
                
                // Animate quantity change
                animateQuantityChange(quantityText, true);
            } else {
                // Animate error
                animateError(quantityText);
                Toast.makeText(getContext(), "Not enough stock available", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Delivery method
        deliveryMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_home_delivery) {
                deliveryFee[0] = 5.99;
                addressLayout.setVisibility(View.VISIBLE);
                animateViewSlideIn(addressLayout);
            } else {
                deliveryFee[0] = 0.0;
                animateViewSlideOut(addressLayout);
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
                    animateError(deliveryAddress);
                    Toast.makeText(getContext(), "Please enter delivery address", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Create order
            Order order = new Order(
                Math.abs(currentUser.getEmail().hashCode()) % 1000,
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
            
            if (orderId != -1) {
                // Animate success
                animateOrderSuccess(confirmOrderButton);
                
                Toast.makeText(getContext(), "Order placed successfully! 🎉", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            } else {
                animateError(confirmOrderButton);
                Toast.makeText(getContext(), "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Animate dialog entrance
        dialog.setOnShowListener(dialogInterface -> animateDialogEntrance(dialogView));
        
        dialog.show();
    }

    private void updateTotalPrice(Product product, int quantity, double deliveryFee, TextView totalPriceView) {
        double total = (product.getPrice() * quantity) + deliveryFee;
        totalPriceView.setText(String.format(Locale.getDefault(), "$%.2f", total));
        
        // Animate price update
        animatePriceUpdate(totalPriceView);
    }

    // Animation Methods
    private void animateRecyclerViewEntrance() {
        favoritesRecyclerView.setAlpha(0f);
        favoritesRecyclerView.setTranslationY(100f);
        
        favoritesRecyclerView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateEmptyStateEntrance() {
        emptyState.setAlpha(0f);
        emptyState.setScaleX(0.8f);
        emptyState.setScaleY(0.8f);
        
        emptyState.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void animateCountUpdate() {
        if (favoritesCountText != null) {
            favoritesCountText.setScaleX(0.5f);
            favoritesCountText.setScaleY(0.5f);
            
            favoritesCountText.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setInterpolator(new BounceInterpolator())
                    .start();
        }
    }

    private void animateItemRemoval(int position, Runnable onComplete) {
        RecyclerView.ViewHolder viewHolder = favoritesRecyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            View itemView = viewHolder.itemView;
            
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(itemView, "scaleX", 1f, 0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(itemView, "scaleY", 1f, 0f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(itemView, "alpha", 1f, 0f);
            
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY, alpha);
            animatorSet.setDuration(300);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            
            animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    onComplete.run();
                }
            });
            
            animatorSet.start();
        } else {
            onComplete.run();
        }
    }

    private void animateHeartPop() {
        // Create a heart icon animation (could be improved with a floating heart)
        if (getView() != null) {
            View heartIcon = getView().findViewById(R.id.heart_animation);
            if (heartIcon != null) {
                heartIcon.setVisibility(View.VISIBLE);
                heartIcon.setScaleX(0f);
                heartIcon.setScaleY(0f);
                
                ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(heartIcon, "scaleX", 0f, 1.2f);
                ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(heartIcon, "scaleY", 0f, 1.2f);
                ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(heartIcon, "scaleX", 1.2f, 1f);
                ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(heartIcon, "scaleY", 1.2f, 1f);
                
                AnimatorSet popAnimation = new AnimatorSet();
                popAnimation.play(scaleXUp).with(scaleYUp);
                popAnimation.play(scaleXDown).with(scaleYDown).after(scaleXUp);
                popAnimation.setDuration(300);
                popAnimation.setInterpolator(new BounceInterpolator());
                
                popAnimation.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        heartIcon.setVisibility(View.GONE);
                    }
                });
                
                popAnimation.start();
            }
        }
    }

    private void animateQuantityChange(View view, boolean increase) {
        view.setScaleX(increase ? 1.2f : 0.8f);
        view.setScaleY(increase ? 1.2f : 0.8f);
        
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(new BounceInterpolator())
                .start();
    }

    private void animateError(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        shake.setDuration(500);
        shake.start();
    }

    private void animateViewSlideIn(View view) {
        view.setAlpha(0f);
        view.setTranslationY(-50f);
        
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateViewSlideOut(View view) {
        view.animate()
                .alpha(0f)
                .translationY(-50f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    private void animateOrderSuccess(View button) {
        ObjectAnimator pulse = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.1f, 1f);
        
        AnimatorSet pulseSet = new AnimatorSet();
        pulseSet.playTogether(pulse, pulseY);
        pulseSet.setDuration(200);
        pulseSet.start();
    }

    private void animatePriceUpdate(TextView priceView) {
        priceView.setScaleX(1.1f);
        priceView.setScaleY(1.1f);
        
        priceView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(150)
                .start();
    }

    private void animateDialogEntrance(View dialogView) {
        dialogView.setScaleX(0.7f);
        dialogView.setScaleY(0.7f);
        dialogView.setAlpha(0f);
        
        dialogView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private User getCurrentUser() {
        if (getActivity() instanceof NavigationActivity) {
            return ((NavigationActivity) getActivity()).getCurrentUser();
        }
        return null;
    }

    public void refreshFavorites() {
        loadFavoriteProducts();
    }
}
