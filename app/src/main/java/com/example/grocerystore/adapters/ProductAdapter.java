package com.example.grocerystore.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.grocerystore.R;
import com.example.grocerystore.models.Product;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    
    private Context context;
    private List<Product> products;
    private List<Product> filteredProducts;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onFavoriteClick(Product product, int position);
        void onOrderClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        this.filteredProducts = new ArrayList<>(products);
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = filteredProducts.get(position);
        holder.bind(product, position);
    }

    @Override
    public int getItemCount() {
        return filteredProducts.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        this.filteredProducts = new ArrayList<>(newProducts);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredProducts.clear();
        if (query.isEmpty()) {
            filteredProducts.addAll(products);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Product product : products) {
                if (product.getName().toLowerCase().contains(lowerCaseQuery) ||
                    product.getCategory().toLowerCase().contains(lowerCaseQuery)) {
                    filteredProducts.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByCategory(String category) {
        filteredProducts.clear();
        if (category == null || category.equals("All Categories")) {
            filteredProducts.addAll(products);
        } else {
            for (Product product : products) {
                if (product.getCategory().equals(category)) {
                    filteredProducts.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByPriceRange(double minPrice, double maxPrice) {
        filteredProducts.clear();
        for (Product product : products) {
            if (product.getPrice() >= minPrice && product.getPrice() <= maxPrice) {
                filteredProducts.add(product);
            }
        }
        notifyDataSetChanged();
    }

    public void applyFilters(String searchQuery, String category, double minPrice, double maxPrice) {
        filteredProducts.clear();
        
        for (Product product : products) {
            boolean matchesSearch = searchQuery.isEmpty() || 
                                  product.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                  product.getCategory().toLowerCase().contains(searchQuery.toLowerCase());
            
            boolean matchesCategory = category == null || category.equals("All Categories") || 
                                    product.getCategory().equals(category);
            
            boolean matchesPrice = product.getPrice() >= minPrice && product.getPrice() <= maxPrice;
            
            if (matchesSearch && matchesCategory && matchesPrice) {
                filteredProducts.add(product);
            }
        }
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView productImage;
        private TextView productName;
        private TextView productCategory;
        private TextView productPrice;
        private TextView productStock;
        private ImageView favoriteButton;
        private Button orderButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productCategory = itemView.findViewById(R.id.product_category);
            productPrice = itemView.findViewById(R.id.product_price);
            productStock = itemView.findViewById(R.id.product_stock);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            orderButton = itemView.findViewById(R.id.order_button);
        }

        public void bind(Product product, int position) {
            productName.setText(product.getName());
            productCategory.setText(product.getCategory());
            productPrice.setText(product.getFormattedPrice());
            productStock.setText(product.getStockStatus());

            // Set stock status color
            if (product.getStockQuantity() <= 0) {
                productStock.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                orderButton.setEnabled(false);
                orderButton.setText("Out of Stock");
                orderButton.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            } else if (product.getStockQuantity() <= 10) {
                productStock.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                orderButton.setEnabled(true);
                orderButton.setText("Order Now");
                orderButton.setBackgroundColor(context.getResources().getColor(R.color.primary_green));
            } else {
                productStock.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                orderButton.setEnabled(true);
                orderButton.setText("Order Now");
                orderButton.setBackgroundColor(context.getResources().getColor(R.color.primary_green));
            }

            // Set favorite button state
            if (product.isFavorite()) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_filled_24);
                favoriteButton.setColorFilter(context.getResources().getColor(android.R.color.holo_red_light));
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite_border_24);
                favoriteButton.setColorFilter(context.getResources().getColor(android.R.color.darker_gray));
            }

            // Load product image - use same approach as AdminProductsAdapter for consistency
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                android.util.Log.d("ProductAdapter", "Loading image for " + product.getName() + ": " + product.getImageUrl());
                
                // Check if it's a local drawable or URL
                if (product.getImageUrl().startsWith("drawable://")) {
                    // Load from local drawable folder
                    String drawableName = product.getImageUrl().replace("drawable://", "");
                    android.util.Log.d("ProductAdapter", "Looking for local drawable: " + drawableName);
                    int resourceId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
                    android.util.Log.d("ProductAdapter", "Resource ID found: " + resourceId);
                    if (resourceId != 0) {
                        productImage.setImageResource(resourceId);
                    } else {
                        android.util.Log.e("ProductAdapter", "Failed to find drawable: " + drawableName);
                        productImage.setImageResource(R.drawable.placeholder_image);
                    }
                } else {
                    // For URL images, use Glide
                    android.util.Log.d("ProductAdapter", "Loading from URL: " + product.getImageUrl());
                    RequestOptions requestOptions = new RequestOptions()
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop();
                    
                    Glide.with(context)
                            .load(product.getImageUrl())
                            .apply(requestOptions)
                            .into(productImage);
                }
            } else {
                android.util.Log.d("ProductAdapter", "No image URL for product: " + product.getName());
                productImage.setImageResource(R.drawable.placeholder_image);
            }

            // Click listeners
            favoriteButton.setOnClickListener(v -> {
                if (listener != null) {
                    animateHeart(favoriteButton, !product.isFavorite());
                    listener.onFavoriteClick(product, position);
                }
            });

            orderButton.setOnClickListener(v -> {
                if (listener != null && product.isAvailable()) {
                    listener.onOrderClick(product);
                }
            });
        }

        private void animateHeart(ImageView heartView, boolean isFavorite) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.3f, 1.0f, 1.3f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            );
            scaleAnimation.setDuration(200);
            scaleAnimation.setRepeatCount(1);
            scaleAnimation.setRepeatMode(Animation.REVERSE);

            scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (isFavorite) {
                        heartView.setImageResource(R.drawable.ic_favorite_filled_24);
                        heartView.setColorFilter(context.getResources().getColor(android.R.color.holo_red_light));
                    } else {
                        heartView.setImageResource(R.drawable.ic_favorite_border_24);
                        heartView.setColorFilter(context.getResources().getColor(android.R.color.darker_gray));
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            heartView.startAnimation(scaleAnimation);
        }
    }
}
