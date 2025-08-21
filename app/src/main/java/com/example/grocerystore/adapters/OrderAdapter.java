package com.example.grocerystore.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.R;
import com.example.grocerystore.models.Order;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private OnOrderActionListener onOrderActionListener;

    public interface OnOrderActionListener {
        void onCancelOrder(Order order);
        void onReorder(Order order);
    }

    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
    }

    public void setOnOrderActionListener(OnOrderActionListener listener) {
        this.onOrderActionListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView orderIdText, orderStatusText, orderDateText;
        private TextView productNameText, quantityText, unitPriceText, totalPriceText;
        private TextView deliveryMethodText, deliveryAddressText;
        private ImageView syncStatusIcon;
        private MaterialButton cancelButton, reorderButton;
        private View actionButtonsLayout;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            
            orderIdText = itemView.findViewById(R.id.order_id);
            orderStatusText = itemView.findViewById(R.id.order_status);
            orderDateText = itemView.findViewById(R.id.order_date);
            productNameText = itemView.findViewById(R.id.product_name);
            quantityText = itemView.findViewById(R.id.quantity);
            unitPriceText = itemView.findViewById(R.id.unit_price);
            totalPriceText = itemView.findViewById(R.id.total_price);
            deliveryMethodText = itemView.findViewById(R.id.delivery_method);
            deliveryAddressText = itemView.findViewById(R.id.delivery_address);
            syncStatusIcon = itemView.findViewById(R.id.sync_status);
            cancelButton = itemView.findViewById(R.id.btn_cancel_order);
            reorderButton = itemView.findViewById(R.id.btn_reorder);
            actionButtonsLayout = itemView.findViewById(R.id.action_buttons);
        }

        public void bind(Order order) {
            // Order ID
            orderIdText.setText("Order #" + order.getId());
            
            // Status with appropriate styling
            setupStatusDisplay(order.getStatus());
            
            // Date formatting
            setupDateDisplay(order.getOrderDate());
            
            // Product details
            productNameText.setText(order.getProductName());
            quantityText.setText(order.getQuantity() + " items");
            unitPriceText.setText(String.format(Locale.getDefault(), "$%.2f", order.getUnitPrice()));
            totalPriceText.setText(order.getFormattedTotalPrice());
            
            // Delivery method
            setupDeliveryDisplay(order);
            
            // Sync status
            setupSyncStatus(order.isSynced());
            
            // Action buttons
            setupActionButtons(order);
        }

        private void setupStatusDisplay(String status) {
            orderStatusText.setText(capitalizeFirst(status));
            
            int backgroundResId;
            int textColor = ContextCompat.getColor(context, R.color.white);
            
            switch (status.toLowerCase()) {
                case "pending":
                    backgroundResId = R.drawable.status_pending_background;
                    break;
                case "approved":
                case "confirmed":
                    backgroundResId = R.drawable.status_approved_background;
                    break;
                case "delivered":
                    backgroundResId = R.drawable.status_delivered_background;
                    break;
                case "cancelled":
                    backgroundResId = R.drawable.status_pending_background; // Reuse for now
                    textColor = ContextCompat.getColor(context, R.color.error_color);
                    break;
                default:
                    backgroundResId = R.drawable.status_pending_background;
                    break;
            }
            
            orderStatusText.setBackgroundResource(backgroundResId);
            orderStatusText.setTextColor(textColor);
        }

        private void setupDateDisplay(String dateString) {
            if (dateString != null && !dateString.isEmpty()) {
                try {
                    // Parse the stored date
                    SimpleDateFormat storedFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date date = storedFormat.parse(dateString);
                    
                    // Format for display
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                    orderDateText.setText(displayFormat.format(date));
                } catch (ParseException e) {
                    orderDateText.setText(dateString);
                }
            } else {
                orderDateText.setText("Date not available");
            }
        }

        private void setupDeliveryDisplay(Order order) {
            String deliveryMethod = order.getDeliveryMethod();
            if ("home".equalsIgnoreCase(deliveryMethod)) {
                deliveryMethodText.setText("Home Delivery");
                deliveryMethodText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_shipping_24, 0, 0, 0);
                
                // Show delivery address if available
                if (order.getDeliveryAddress() != null && !order.getDeliveryAddress().isEmpty()) {
                    deliveryAddressText.setText(order.getDeliveryAddress());
                    deliveryAddressText.setVisibility(View.VISIBLE);
                } else {
                    deliveryAddressText.setVisibility(View.GONE);
                }
            } else {
                deliveryMethodText.setText("Store Pickup");
                deliveryMethodText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_store_24, 0, 0, 0);
                deliveryAddressText.setVisibility(View.GONE);
            }
        }

        private void setupSyncStatus(boolean isSynced) {
            if (isSynced) {
                syncStatusIcon.setImageResource(R.drawable.ic_check_circle_24);
                syncStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_delivered));
            } else {
                syncStatusIcon.setImageResource(R.drawable.ic_sync_24);
                syncStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_pending));
            }
        }

        private void setupActionButtons(Order order) {
            String status = order.getStatus().toLowerCase();
            
            // Show action buttons only for certain statuses
            if ("pending".equals(status) || "delivered".equals(status)) {
                actionButtonsLayout.setVisibility(View.VISIBLE);
                
                // Cancel button only for pending orders
                if ("pending".equals(status)) {
                    cancelButton.setVisibility(View.VISIBLE);
                    cancelButton.setOnClickListener(v -> {
                        if (onOrderActionListener != null) {
                            onOrderActionListener.onCancelOrder(order);
                        }
                    });
                } else {
                    cancelButton.setVisibility(View.GONE);
                }
                
                // Reorder button for all eligible statuses
                reorderButton.setOnClickListener(v -> {
                    if (onOrderActionListener != null) {
                        onOrderActionListener.onReorder(order);
                    }
                });
            } else {
                actionButtonsLayout.setVisibility(View.GONE);
            }
        }

        private String capitalizeFirst(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        }
    }
}
