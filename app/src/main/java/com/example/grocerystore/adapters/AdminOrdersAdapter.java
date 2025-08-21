package com.example.grocerystore.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.R;
import com.example.grocerystore.models.Order;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.OrderViewHolder> {

    private List<Order> orders = new ArrayList<>();
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onUpdateOrderStatus(Order order);
        void onViewOrderDetails(Order order);
        void onDeleteOrder(Order order);
    }

    public AdminOrdersAdapter(OnOrderActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
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
        this.orders.clear();
        this.orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView orderIdText;
        private TextView userIdText;
        private TextView statusText;
        private TextView totalAmountText;
        private TextView orderDateText;
        private ImageButton viewButton;
        private ImageButton updateStatusButton;
        private ImageButton deleteButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.order_id_text);
            userIdText = itemView.findViewById(R.id.user_id_text);
            statusText = itemView.findViewById(R.id.order_status_text);
            totalAmountText = itemView.findViewById(R.id.total_amount_text);
            orderDateText = itemView.findViewById(R.id.order_date_text);
            viewButton = itemView.findViewById(R.id.view_order_button);
            updateStatusButton = itemView.findViewById(R.id.update_status_button);
            deleteButton = itemView.findViewById(R.id.delete_order_button);
        }

        public void bind(Order order) {
            orderIdText.setText("Order #" + order.getId());
            userIdText.setText("User ID: " + order.getUserId());
            statusText.setText(order.getStatus());
            totalAmountText.setText("$" + String.format("%.2f", order.getTotalAmount()));
            orderDateText.setText(order.getOrderDate());

            // Set status text color based on status
            switch (order.getStatus().toLowerCase()) {
                case "pending":
                    statusText.setTextColor(itemView.getContext().getColor(R.color.warning));
                    break;
                case "processing":
                    statusText.setTextColor(itemView.getContext().getColor(R.color.primary));
                    break;
                case "shipped":
                    statusText.setTextColor(itemView.getContext().getColor(R.color.secondary));
                    break;
                case "delivered":
                    statusText.setTextColor(itemView.getContext().getColor(R.color.success));
                    break;
                case "cancelled":
                    statusText.setTextColor(itemView.getContext().getColor(R.color.error));
                    break;
                default:
                    statusText.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
                    break;
            }

            viewButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewOrderDetails(order);
                }
            });

            updateStatusButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpdateOrderStatus(order);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteOrder(order);
                }
            });
        }
    }
}
