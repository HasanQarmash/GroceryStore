package com.example.grocerystore.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.R;
import com.example.grocerystore.adapters.AdminOrdersAdapter;
import com.example.grocerystore.database.DatabaseHelper;
import com.example.grocerystore.models.Order;

import java.util.Arrays;
import java.util.List;

public class ManageOrdersFragment extends Fragment implements AdminOrdersAdapter.OnOrderActionListener {

    private RecyclerView ordersRecyclerView;
    private AdminOrdersAdapter ordersAdapter;
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            getActivity().setTitle("Manage Orders");
        }

        initializeViews(view);
        initializeData();
        setupRecyclerView();
        loadOrders();
    }

    private void initializeViews(View view) {
        ordersRecyclerView = view.findViewById(R.id.orders_recycler_view);
    }

    private void initializeData() {
        databaseHelper = new DatabaseHelper(getContext());
    }

    private void setupRecyclerView() {
        ordersAdapter = new AdminOrdersAdapter(this);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ordersRecyclerView.setAdapter(ordersAdapter);
    }

    private void loadOrders() {
        List<Order> orders = databaseHelper.getAllOrders();
        ordersAdapter.updateOrders(orders);
    }

    @Override
    public void onUpdateOrderStatus(Order order) {
        showUpdateStatusDialog(order);
    }

    @Override
    public void onViewOrderDetails(Order order) {
        showOrderDetailsDialog(order);
    }

    @Override
    public void onDeleteOrder(Order order) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Order")
                .setMessage("Are you sure you want to delete this order?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (databaseHelper.deleteOrder(order.getId())) {
                        Toast.makeText(getContext(), "Order deleted successfully", Toast.LENGTH_SHORT).show();
                        loadOrders(); // Refresh the list
                    } else {
                        Toast.makeText(getContext(), "Failed to delete order", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showUpdateStatusDialog(Order order) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_order_status, null);
        Spinner statusSpinner = dialogView.findViewById(R.id.status_spinner);

        // Setup spinner with order statuses
        List<String> statuses = Arrays.asList("Pending", "Processing", "Shipped", "Delivered", "Cancelled");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        // Set current status
        int currentIndex = statuses.indexOf(order.getStatus());
        if (currentIndex >= 0) {
            statusSpinner.setSelection(currentIndex);
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Update Order Status")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newStatus = statusSpinner.getSelectedItem().toString();
                    if (databaseHelper.updateOrderStatus(order.getId(), newStatus)) {
                        Toast.makeText(getContext(), "Order status updated successfully", Toast.LENGTH_SHORT).show();
                        loadOrders(); // Refresh the list
                    } else {
                        Toast.makeText(getContext(), "Failed to update order status", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showOrderDetailsDialog(Order order) {
        String details = "Order ID: " + order.getId() + "\n" +
                "User ID: " + order.getUserId() + "\n" +
                "Status: " + order.getStatus() + "\n" +
                "Total Amount: $" + String.format("%.2f", order.getTotalAmount()) + "\n" +
                "Order Date: " + order.getOrderDate();

        new AlertDialog.Builder(getContext())
                .setTitle("Order Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders(); // Refresh data when returning to fragment
    }
}
