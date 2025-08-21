package com.example.grocerystore.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.grocerystore.R;
import com.example.grocerystore.models.Offer;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.OfferViewHolder> {

    private Context context;
    private List<Offer> offers;
    private OnOfferActionListener listener;

    public interface OnOfferActionListener {
        void onAddToFavorites(Offer offer);
        void onRemoveFromFavorites(Offer offer);
        void onOrderOffer(Offer offer);
    }

    public OffersAdapter(Context context, List<Offer> offers, OnOfferActionListener listener) {
        this.context = context;
        this.offers = offers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        android.util.Log.d("OffersAdapter", "onBindViewHolder called for position: " + position);
        Offer offer = offers.get(position);
        android.util.Log.d("OffersAdapter", "Binding offer: " + offer.getProductName());
        holder.bind(offer);
    }

    @Override
    public int getItemCount() {
        int count = offers.size();
        android.util.Log.d("OffersAdapter", "getItemCount() called, returning: " + count);
        return count;
    }

    public void updateOffers(List<Offer> newOffers) {
        android.util.Log.d("OffersAdapter", "=== UPDATE OFFERS DEBUG ===");
        android.util.Log.d("OffersAdapter", "Received " + newOffers.size() + " new offers");
        android.util.Log.d("OffersAdapter", "Current offers count before update: " + this.offers.size());
        
        this.offers.clear();
        this.offers.addAll(newOffers);
        
        android.util.Log.d("OffersAdapter", "Current offers count after update: " + this.offers.size());
        
        for (int i = 0; i < this.offers.size(); i++) {
            Offer offer = this.offers.get(i);
            android.util.Log.d("OffersAdapter", "Offer " + (i+1) + ": " + offer.getProductName());
        }
        
        notifyDataSetChanged();
        android.util.Log.d("OffersAdapter", "notifyDataSetChanged() called");
        android.util.Log.d("OffersAdapter", "=== END UPDATE OFFERS DEBUG ===");
    }

    public class OfferViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView productImage;
        private TextView productName;
        private TextView originalPrice;
        private TextView discountedPrice;
        private TextView discountPercent;
        private TextView offerTitle;
        private TextView category;
        private MaterialButton favoriteButton;
        private MaterialButton orderButton;
        private View discountBadge;
        
        private Offer currentOffer;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = (MaterialCardView) itemView; // The root view is the card
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            originalPrice = itemView.findViewById(R.id.original_price);
            discountedPrice = itemView.findViewById(R.id.discounted_price);
            discountPercent = itemView.findViewById(R.id.discount_badge);
            offerTitle = itemView.findViewById(R.id.product_name); // Same as product name
            category = itemView.findViewById(R.id.category_text);
            favoriteButton = itemView.findViewById(R.id.add_to_favorites_button);
            orderButton = itemView.findViewById(R.id.order_now_button);
            discountBadge = itemView.findViewById(R.id.discount_badge);
        }

        public void bind(Offer offer) {
            currentOffer = offer;
            
            // Set product information
            productName.setText(offer.getName());
            originalPrice.setText("$" + String.format("%.2f", offer.getOriginalPrice()));
            discountedPrice.setText("$" + String.format("%.2f", offer.getDiscountedPrice()));
            discountPercent.setText(offer.getDiscountPercent() + "% OFF");
            offerTitle.setText(offer.getOfferTitle());
            category.setText(offer.getCategory());
            
            // Apply strikethrough to original price
            originalPrice.setPaintFlags(originalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            
            // Set product image using same approach as other adapters for consistency
            if (offer.getImageUrl() != null && !offer.getImageUrl().isEmpty()) {
                android.util.Log.d("OffersAdapter", "Loading image for offer " + offer.getName() + ": " + offer.getImageUrl());
                
                // Check if it's a local drawable or URL
                if (offer.getImageUrl().startsWith("drawable://")) {
                    // Load from local drawable folder
                    String drawableName = offer.getImageUrl().replace("drawable://", "");
                    android.util.Log.d("OffersAdapter", "Looking for local drawable: " + drawableName);
                    int resourceId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
                    android.util.Log.d("OffersAdapter", "Resource ID found: " + resourceId);
                    if (resourceId != 0) {
                        productImage.setImageResource(resourceId);
                        android.util.Log.d("OffersAdapter", "✅ Successfully loaded drawable for offer: " + offer.getName());
                    } else {
                        android.util.Log.e("OffersAdapter", "❌ Failed to find drawable: " + drawableName);
                        setProductImage(offer.getCategory());
                    }
                } else {
                    // Load from URL using Glide
                    android.util.Log.d("OffersAdapter", "Loading offer image from URL: " + offer.getImageUrl());
                    RequestOptions requestOptions = new RequestOptions()
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop();
                    
                    Glide.with(context)
                            .load(offer.getImageUrl())
                            .apply(requestOptions)
                            .into(productImage);
                }
            } else {
                android.util.Log.d("OffersAdapter", "No image URL for offer: " + offer.getName() + ", using category fallback");
                setProductImage(offer.getCategory());
            }
            
            // Setup click listeners
            favoriteButton.setOnClickListener(v -> {
                if (listener != null) {
                    // Toggle favorite state (in real app, would check current state)
                    listener.onAddToFavorites(offer);
                }
            });
            
            orderButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderOffer(offer);
                }
            });
            
            // Add entrance animation
            animateItemEntrance();
        }

        private void setProductImage(String category) {
            int imageResource;
            switch (category.toLowerCase()) {
                case "produce":
                    imageResource = R.drawable.ic_vegetable_24;
                    break;
                case "dairy":
                    imageResource = R.drawable.ic_shopping_cart_24;
                    break;
                case "bakery":
                    imageResource = R.drawable.ic_shopping_cart_24;
                    break;
                case "meat":
                    imageResource = R.drawable.ic_meat_24;
                    break;
                case "beverages":
                    imageResource = R.drawable.ic_shopping_cart_24;
                    break;
                case "frozen":
                    imageResource = R.drawable.ic_shopping_cart_24;
                    break;
                default:
                    imageResource = R.drawable.ic_shopping_cart_24;
                    break;
            }
            productImage.setImageResource(imageResource);
        }

        private void animateItemEntrance() {
            // Entrance animation with staggered timing
            cardView.setAlpha(0f);
            cardView.setScaleX(0.8f);
            cardView.setScaleY(0.8f);
            
            cardView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setStartDelay(getAdapterPosition() * 50L)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.1f))
                    .start();
        }

        public void animateHeartPop() {
            favoriteButton.animate()
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        favoriteButton.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start();
                    })
                    .start();
        }

        public void animateHeartBreak() {
            favoriteButton.animate()
                    .translationX(-10f)
                    .setDuration(50)
                    .withEndAction(() -> {
                        favoriteButton.animate()
                                .translationX(10f)
                                .setDuration(50)
                                .withEndAction(() -> {
                                    favoriteButton.animate()
                                            .translationX(0f)
                                            .setDuration(50)
                                            .start();
                                })
                                .start();
                    })
                    .start();
        }

        public Offer getCurrentOffer() {
            return currentOffer;
        }
    }
}
