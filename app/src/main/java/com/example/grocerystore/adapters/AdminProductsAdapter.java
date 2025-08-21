package com.example.grocerystore.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.R;
import com.example.grocerystore.models.Product;

import java.util.ArrayList;
import java.util.List;

public class AdminProductsAdapter extends RecyclerView.Adapter<AdminProductsAdapter.ProductViewHolder> {

    private List<Product> products = new ArrayList<>();
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onDeleteProduct(Product product);
        void onEditProduct(Product product);
        void onViewProduct(Product product);
    }

    public AdminProductsAdapter(OnProductActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products.clear();
        this.products.addAll(newProducts);
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView productImage;
        private TextView nameText;
        private TextView categoryText;
        private TextView priceText;
        private ImageButton viewButton;
        private ImageButton editButton;
        private ImageButton deleteButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            nameText = itemView.findViewById(R.id.product_name_text);
            categoryText = itemView.findViewById(R.id.product_category_text);
            priceText = itemView.findViewById(R.id.product_price_text);
            viewButton = itemView.findViewById(R.id.view_product_button);
            editButton = itemView.findViewById(R.id.edit_product_button);
            deleteButton = itemView.findViewById(R.id.delete_product_button);
        }

        public void bind(Product product) {
            nameText.setText(product.getName());
            categoryText.setText(product.getCategory());
            priceText.setText("$" + String.format("%.2f", product.getPrice()));

            // Load product image using Glide (same logic as ProductAdapter)
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                android.util.Log.d("AdminProductsAdapter", "Loading image for " + product.getName() + ": " + product.getImageUrl());
                
                // Check if it's a local drawable or URL
                if (product.getImageUrl().startsWith("drawable://")) {
                    // Load from local drawable folder
                    String drawableName = product.getImageUrl().replace("drawable://", "");
                    android.util.Log.d("AdminProductsAdapter", "Looking for local drawable: " + drawableName);
                    int resourceId = itemView.getContext().getResources().getIdentifier(drawableName, "drawable", itemView.getContext().getPackageName());
                    android.util.Log.d("AdminProductsAdapter", "Resource ID found: " + resourceId);
                    if (resourceId != 0) {
                        productImage.setImageResource(resourceId);
                    } else {
                        android.util.Log.e("AdminProductsAdapter", "Failed to find drawable: " + drawableName);
                        productImage.setImageResource(R.drawable.placeholder_image);
                    }
                } else {
                    // For URL images, we would use Glide here, but for now use placeholder
                    android.util.Log.d("AdminProductsAdapter", "URL image not supported in admin adapter yet");
                    productImage.setImageResource(R.drawable.placeholder_image);
                }
            } else {
                android.util.Log.d("AdminProductsAdapter", "No image URL for product: " + product.getName());
                productImage.setImageResource(R.drawable.placeholder_image);
            }

            viewButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProduct(product);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditProduct(product);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteProduct(product);
                }
            });
        }
    }
}
