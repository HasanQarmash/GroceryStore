package com.example.grocerystore.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.R;
import com.example.grocerystore.models.Product;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class EnhancedFavoritesAdapter extends RecyclerView.Adapter<EnhancedFavoritesAdapter.FavoriteViewHolder> {
    
    private Context context;
    private List<Product> favoriteProducts;
    private OnFavoriteActionListener listener;
    
    public interface OnFavoriteActionListener {
        void onRemoveFromFavorites(Product product, int position);
        void onOrderProduct(Product product);
    }
    
    public EnhancedFavoritesAdapter(Context context, List<Product> favoriteProducts) {
        this.context = context;
        this.favoriteProducts = favoriteProducts;
    }
    
    public void setOnFavoriteActionListener(OnFavoriteActionListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_enhanced_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Product product = favoriteProducts.get(position);
        
        holder.productName.setText(product.getName());
        holder.productPrice.setText(product.getFormattedPrice());
        holder.productCategory.setText(product.getCategory());
        
        // Set stock status
        if (product.getStockQuantity() > 0) {
            holder.stockStatus.setText("In Stock (" + product.getStockQuantity() + ")");
            holder.stockStatus.setTextColor(context.getResources().getColor(R.color.primary_green));
            holder.orderButton.setEnabled(true);
            holder.orderButton.setAlpha(1.0f);
        } else {
            holder.stockStatus.setText("Out of Stock");
            holder.stockStatus.setTextColor(context.getResources().getColor(R.color.error_red));
            holder.orderButton.setEnabled(false);
            holder.orderButton.setAlpha(0.5f);
        }
        
        // Set product image - improved image loading with better debugging
        android.util.Log.d("EnhancedFavoritesAdapter", "=== LOADING IMAGE FOR FAVORITE ===");
        android.util.Log.d("EnhancedFavoritesAdapter", "Product: " + product.getName());
        android.util.Log.d("EnhancedFavoritesAdapter", "Image URL: " + product.getImageUrl());
        android.util.Log.d("EnhancedFavoritesAdapter", "Category: " + product.getCategory());
        
        boolean imageLoaded = false;
        
        if (product.getImageUrl() != null && !product.getImageUrl().trim().isEmpty()) {
            String imageUrl = product.getImageUrl().trim();
            
            // Check if it's a local drawable
            if (imageUrl.startsWith("drawable://")) {
                String drawableName = imageUrl.replace("drawable://", "");
                android.util.Log.d("EnhancedFavoritesAdapter", "Looking for drawable: " + drawableName);
                
                int resourceId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
                android.util.Log.d("EnhancedFavoritesAdapter", "Resource ID: " + resourceId);
                
                if (resourceId != 0) {
                    holder.productImage.setImageResource(resourceId);
                    imageLoaded = true;
                    android.util.Log.d("EnhancedFavoritesAdapter", "✅ Successfully loaded drawable: " + drawableName);
                } else {
                    android.util.Log.e("EnhancedFavoritesAdapter", "❌ Drawable not found: " + drawableName);
                }
            } else {
                // Try to treat as direct resource name
                android.util.Log.d("EnhancedFavoritesAdapter", "Trying as direct resource name: " + imageUrl);
                int resourceId = context.getResources().getIdentifier(imageUrl, "drawable", context.getPackageName());
                if (resourceId != 0) {
                    holder.productImage.setImageResource(resourceId);
                    imageLoaded = true;
                    android.util.Log.d("EnhancedFavoritesAdapter", "✅ Successfully loaded direct resource: " + imageUrl);
                } else {
                    android.util.Log.e("EnhancedFavoritesAdapter", "❌ Direct resource not found: " + imageUrl);
                }
            }
        }
        
        // If no image was loaded, use category-based fallback
        if (!imageLoaded) {
            android.util.Log.d("EnhancedFavoritesAdapter", "Using category-based fallback for: " + product.getCategory());
            setProductImage(holder.productImage, product.getCategory());
        }
        
        android.util.Log.d("EnhancedFavoritesAdapter", "=== END IMAGE LOADING ===");
        
        // Set up click listeners with animations
        holder.favoriteButton.setOnClickListener(v -> {
            animateHeartBreak(holder.favoriteButton);
            if (listener != null) {
                listener.onRemoveFromFavorites(product, position);
            }
        });
        
        holder.orderButton.setOnClickListener(v -> {
            if (product.getStockQuantity() > 0) {
                animateOrderButton(holder.orderButton);
                if (listener != null) {
                    listener.onOrderProduct(product);
                }
            }
        });
        
        // Card click animation
        holder.cardView.setOnClickListener(v -> animateCardClick(holder.cardView));
        
        // Animate item entrance
        animateItemEntrance(holder.itemView, position);
    }
    
    @Override
    public int getItemCount() {
        return favoriteProducts.size();
    }
    
    public Product getProduct(int position) {
        return favoriteProducts.get(position);
    }
    
    public void removeItem(int position) {
        if (position >= 0 && position < favoriteProducts.size()) {
            favoriteProducts.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }
    }
    
    private void setProductImage(ImageView imageView, String category) {        
        android.util.Log.d("EnhancedFavoritesAdapter", "Setting category-based image for: " + category);
        
        if (category == null) {
            category = "default";
        }
        
        int imageResource = getCategoryIcon(category);
        imageView.setImageResource(imageResource);
        android.util.Log.d("EnhancedFavoritesAdapter", "Set category image for " + category + " with resource ID: " + imageResource);
    }
    
    private int getCategoryIcon(String category) {
        // Fallback to simple category icons
        switch (category.toLowerCase().trim()) {
            case "fruits":
                return R.drawable.ic_fruit_24;
            case "vegetables":
                return R.drawable.ic_vegetable_24;
            case "dairy":
                return R.drawable.ic_dairy_24;
            case "meat":
                return R.drawable.ic_meat_24;
            case "beverages":
                return R.drawable.ic_beverage_24;
            case "snacks":
                return R.drawable.ic_snack_24;
            default:
                return R.drawable.ic_shopping_cart_24;
        }
    }
    
    // Animation Methods
    private void animateItemEntrance(View itemView, int position) {
        itemView.setAlpha(0f);
        itemView.setTranslationY(100f);
        itemView.setScaleX(0.8f);
        itemView.setScaleY(0.8f);
        
        itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setStartDelay(position * 50L) // Stagger animation
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
    
    private void animateHeartBreak(View heartButton) {
        // Create a heart break animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(heartButton, "scaleX", 1f, 1.3f, 0.8f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(heartButton, "scaleY", 1f, 1.3f, 0.8f, 1.1f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(heartButton, "rotation", 0f, 15f, -15f, 10f, -10f, 0f);
        
        AnimatorSet heartBreakAnimation = new AnimatorSet();
        heartBreakAnimation.playTogether(scaleX, scaleY, rotation);
        heartBreakAnimation.setDuration(600);
        heartBreakAnimation.setInterpolator(new BounceInterpolator());
        
        heartBreakAnimation.start();
    }
    
    private void animateOrderButton(View orderButton) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(orderButton, "scaleX", 1f, 0.9f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(orderButton, "scaleY", 1f, 0.9f, 1.1f, 1f);
        
        AnimatorSet orderAnimation = new AnimatorSet();
        orderAnimation.playTogether(scaleX, scaleY);
        orderAnimation.setDuration(300);
        orderAnimation.setInterpolator(new BounceInterpolator());
        
        orderAnimation.start();
    }
    
    private void animateCardClick(View cardView) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 0.95f, 1f);
        
        AnimatorSet clickAnimation = new AnimatorSet();
        clickAnimation.playTogether(scaleX, scaleY);
        clickAnimation.setDuration(150);
        
        clickAnimation.start();
    }
    
    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productCategory;
        TextView stockStatus;
        MaterialButton favoriteButton;
        MaterialButton orderButton;
        
        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.favorite_card);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productCategory = itemView.findViewById(R.id.product_category);
            stockStatus = itemView.findViewById(R.id.stock_status);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            orderButton = itemView.findViewById(R.id.order_button);
        }
    }
}
