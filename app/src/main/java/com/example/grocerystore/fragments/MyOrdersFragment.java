package com.example.grocerystore.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.NavigationActivity;
import com.example.grocerystore.R;
import com.example.grocerystore.adapters.OrderAdapter;
import com.example.grocerystore.database.DatabaseHelper;
import com.example.grocerystore.models.Order;
import com.example.grocerystore.model.User;
import com.example.grocerystore.network.OrderSyncService;
import com.example.grocerystore.utils.PreferencesManager;
import com.example.grocerystore.utils.UserManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class MyOrdersFragment extends Fragment implements OrderAdapter.OnOrderActionListener {

    private RecyclerView ordersRecyclerView;
    private TextView ordersCountText;
    private ImageView syncStatusHeader;
    private ChipGroup statusFilterChips;
    private View emptyState, loadingState;
    private MaterialButton startShoppingButton;

    private OrderAdapter orderAdapter;
    private DatabaseHelper databaseHelper;
    private OrderSyncService syncService;
    private PreferencesManager preferencesManager;
    private UserManager userManager;
    
    private List<Order> allOrders;
    private List<Order> filteredOrders;
    private User currentUser;
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            getActivity().setTitle("My Orders");
        }

        initializeViews(view);
        setupServices();
        setupRecyclerView();
        setupFilterChips();
        setupClickListeners();
        loadCurrentUser();
        loadOrders();
        
        // Start sync if online
        if (syncService.isNetworkAvailable()) {
            syncOrders();
        }
    }

    private void initializeViews(View view) {
        ordersRecyclerView = view.findViewById(R.id.orders_recycler_view);
        ordersCountText = view.findViewById(R.id.orders_count);
        syncStatusHeader = view.findViewById(R.id.sync_status_header);
        statusFilterChips = view.findViewById(R.id.status_filter_chips);
        emptyState = view.findViewById(R.id.empty_state);
        loadingState = view.findViewById(R.id.loading_state);
        startShoppingButton = view.findViewById(R.id.btnStartShopping);
    }

    private void setupServices() {
        databaseHelper = new DatabaseHelper(getContext());
        syncService = new OrderSyncService(getContext());
        preferencesManager = new PreferencesManager(getContext());
        userManager = new UserManager(getContext());
        
        syncService.setSyncListener(new OrderSyncService.OnSyncCompleteListener() {
            @Override
            public void onSyncStarted() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        syncStatusHeader.setImageResource(R.drawable.ic_sync_24);
                        syncStatusHeader.setColorFilter(ContextCompat.getColor(getContext(), R.color.status_pending));
                    });
                }
            }

            @Override
            public void onSyncCompleted(boolean success, int syncedCount) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (success) {
                            syncStatusHeader.setImageResource(R.drawable.ic_check_circle_24);
                            syncStatusHeader.setColorFilter(ContextCompat.getColor(getContext(), R.color.status_delivered));
                            if (syncedCount > 0) {
                                Toast.makeText(getContext(), 
                                    syncedCount + " orders synced successfully", Toast.LENGTH_SHORT).show();
                                loadOrders(); // Reload to show updated data
                            }
                        } else {
                            syncStatusHeader.setImageResource(R.drawable.ic_sync_24);
                            syncStatusHeader.setColorFilter(ContextCompat.getColor(getContext(), R.color.error_color));
                        }
                    });
                }
            }

            @Override
            public void onSyncError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        syncStatusHeader.setImageResource(R.drawable.ic_sync_24);
                        syncStatusHeader.setColorFilter(ContextCompat.getColor(getContext(), R.color.error_color));
                        Toast.makeText(getContext(), "Sync failed: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void setupRecyclerView() {
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        allOrders = new ArrayList<>();
        filteredOrders = new ArrayList<>();
        orderAdapter = new OrderAdapter(getContext(), filteredOrders);
        orderAdapter.setOnOrderActionListener(this);
        ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void setupFilterChips() {
        statusFilterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                String filter = "all";
                
                if (checkedId == R.id.chip_pending) {
                    filter = "pending";
                } else if (checkedId == R.id.chip_approved) {
                    filter = "approved";
                } else if (checkedId == R.id.chip_delivered) {
                    filter = "delivered";
                } else {
                    filter = "all";
                }
                
                applyFilter(filter);
            }
        });
    }

    private void setupClickListeners() {
        startShoppingButton.setOnClickListener(v -> {
            // Navigate to products fragment
            if (getActivity() instanceof NavigationActivity) {
                NavigationActivity navActivity = (NavigationActivity) getActivity();
                
                // Create and load ProductsFragment
                ProductsFragment productsFragment = new ProductsFragment();
                navActivity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, productsFragment)
                    .addToBackStack(null)
                    .commit();
                
                // Update navigation menu selection
                navActivity.findViewById(R.id.nav_products).setSelected(true);
            }
        });

        syncStatusHeader.setOnClickListener(v -> {
            if (syncService.isNetworkAvailable()) {
                syncOrders();
            } else {
                Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCurrentUser() {
        String email = preferencesManager.getLoggedInUserEmail();
        if (email != null && !email.isEmpty()) {
            currentUser = userManager.getUserByEmail(email);
        }
        
        // If no user is found, it means the user data might not be properly saved
        // Don't create a demo user, instead handle the empty state properly
        if (currentUser == null) {
            // Log for debugging
            System.out.println("DEBUG: No user found for email: " + email);
        }
    }

    private void loadOrders() {
        showLoadingState();
        
        if (currentUser != null) {
            int userId = generateUserId(currentUser.getEmail());
            
            // Create sample orders for this user if they don't have any
            databaseHelper.createSampleOrdersForUser(userId);
            
            allOrders = databaseHelper.getUserOrders(userId);
            applyFilter(currentFilter);
            updateOrdersCount();
        } else {
            allOrders.clear();
            filteredOrders.clear();
            updateUI();
        }
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        filteredOrders.clear();
        
        if ("all".equals(filter)) {
            filteredOrders.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                if (filter.equalsIgnoreCase(order.getStatus())) {
                    filteredOrders.add(order);
                }
            }
        }
        
        updateUI();
    }

    private void updateUI() {
        if (filteredOrders.isEmpty()) {
            showEmptyState();
        } else {
            showOrdersList();
            orderAdapter.updateOrders(filteredOrders);
        }
    }

    private void updateOrdersCount() {
        int totalCount = allOrders.size();
        String countText;
        
        if (totalCount == 0) {
            countText = "You have no orders";
        } else if (totalCount == 1) {
            countText = "You have 1 order";
        } else {
            countText = "You have " + totalCount + " orders";
        }
        
        ordersCountText.setText(countText);
    }

    private void showLoadingState() {
        loadingState.setVisibility(View.VISIBLE);
        ordersRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
    }

    private void showOrdersList() {
        loadingState.setVisibility(View.GONE);
        ordersRecyclerView.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        loadingState.setVisibility(View.GONE);
        ordersRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }

    private void syncOrders() {
        syncService.syncOrders();
    }

    private int generateUserId(String email) {
        return Math.abs(email.hashCode()) % 1000;
    }

    // OrderAdapter.OnOrderActionListener implementation
    @Override
    public void onCancelOrder(Order order) {
        syncService.updateOrderStatus(order.getId(), "cancelled", 
            new OrderSyncService.OnOrderUpdateListener() {
                @Override
                public void onUpdateCompleted(boolean success, String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(getContext(), "Order cancelled successfully", Toast.LENGTH_SHORT).show();
                                loadOrders(); // Reload to show updated status
                            } else {
                                Toast.makeText(getContext(), "Failed to cancel order: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
    }

    @Override
    public void onReorder(Order order) {
        // Reorder functionality - create a new order with same details
        if (currentUser != null) {
            int userId = generateUserId(currentUser.getEmail());
            
            // Create new order based on the existing one
            Order newOrder = new Order(userId, order.getProductId(), order.getProductName(), 
                order.getQuantity(), order.getUnitPrice(), order.getDeliveryMethod());
            newOrder.setDeliveryAddress(order.getDeliveryAddress());
            
            // Set current date/time
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", 
                java.util.Locale.getDefault());
            newOrder.setOrderDate(sdf.format(new java.util.Date()));
            
            long result = databaseHelper.addOrder(newOrder);
            
            if (result != -1) {
                Toast.makeText(getContext(), "Reorder placed successfully!", Toast.LENGTH_SHORT).show();
                loadOrders(); // Reload to show new order
                
                // Auto-sync new order if online
                if (syncService.isNetworkAvailable()) {
                    syncOrders();
                }
            } else {
                Toast.makeText(getContext(), "Failed to place reorder", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh orders when fragment becomes visible
        loadOrders();
    }
}
