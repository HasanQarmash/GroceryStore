package com.example.grocerystore.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.R;
import com.example.grocerystore.models.Offer;

import java.util.ArrayList;
import java.util.List;

public class AdminOffersAdapter extends RecyclerView.Adapter<AdminOffersAdapter.OfferViewHolder> {

    private List<Offer> offers = new ArrayList<>();
    private OnOfferActionListener listener;

    public interface OnOfferActionListener {
        void onDeleteOffer(Offer offer);
        void onEditOffer(Offer offer);
        void onToggleOfferStatus(Offer offer);
        void onViewOffer(Offer offer);
    }

    public AdminOffersAdapter(OnOfferActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        Offer offer = offers.get(position);
        holder.bind(offer);
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    public void updateOffers(List<Offer> newOffers) {
        this.offers.clear();
        this.offers.addAll(newOffers);
        notifyDataSetChanged();
    }

    class OfferViewHolder extends RecyclerView.ViewHolder {
        private TextView productNameText;
        private TextView categoryText;
        private TextView originalPriceText;
        private TextView discountedPriceText;
        private TextView discountPercentText;
        private TextView stockText;
        private TextView statusText;
        private ImageView statusIndicator;
        private ImageButton viewButton;
        private ImageButton editButton;
        private ImageButton toggleStatusButton;
        private ImageButton deleteButton;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameText = itemView.findViewById(R.id.product_name_text);
            categoryText = itemView.findViewById(R.id.category_text);
            originalPriceText = itemView.findViewById(R.id.original_price_text);
            discountedPriceText = itemView.findViewById(R.id.discounted_price_text);
            discountPercentText = itemView.findViewById(R.id.discount_percent_text);
            stockText = itemView.findViewById(R.id.stock_text);
            statusText = itemView.findViewById(R.id.status_text);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
            viewButton = itemView.findViewById(R.id.view_offer_button);
            editButton = itemView.findViewById(R.id.edit_offer_button);
            toggleStatusButton = itemView.findViewById(R.id.toggle_status_button);
            deleteButton = itemView.findViewById(R.id.delete_offer_button);
        }

        public void bind(Offer offer) {
            productNameText.setText(offer.getProductName());
            categoryText.setText(offer.getCategory());
            originalPriceText.setText(String.format("$%.2f", offer.getOriginalPrice()));
            discountedPriceText.setText(String.format("$%.2f", offer.getDiscountedPrice()));
            discountPercentText.setText(String.format("%.0f%% OFF", offer.getDiscountPercentage()));
            stockText.setText(String.valueOf(offer.getStockQuantity()));

            // Set status display
            if (offer.isActive()) {
                statusText.setText("ACTIVE");
                statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success_green));
                statusIndicator.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.success_green));
                toggleStatusButton.setImageResource(R.drawable.ic_pause_24);
            } else {
                statusText.setText("INACTIVE");
                statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.error_red));
                statusIndicator.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.error_red));
                toggleStatusButton.setImageResource(R.drawable.ic_play_arrow_24);
            }

            // Set click listeners
            viewButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewOffer(offer);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditOffer(offer);
                }
            });

            toggleStatusButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleOfferStatus(offer);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteOffer(offer);
                }
            });

            // Set discount text color
            discountPercentText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success_green));
        }
    }
}
