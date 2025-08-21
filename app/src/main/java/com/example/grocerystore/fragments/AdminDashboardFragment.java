package com.example.grocerystore.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.grocerystore.R;
import com.example.grocerystore.database.DatabaseHelper;
import com.example.grocerystore.utils.UserManager;
import com.google.android.material.card.MaterialCardView;

public class AdminDashboardFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private UserManager userManager;
    
    private TextView totalUsersText;
    private TextView totalProductsText;
    private TextView totalOrdersText;
    private TextView pendingOrdersText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            getActivity().setTitle("Admin Dashboard");
        }

        initializeViews(view);
        initializeData();
        setupQuickActions(view);
        loadDashboardData();
    }

    private void initializeViews(View view) {
        totalUsersText = view.findViewById(R.id.total_users_text);
        totalProductsText = view.findViewById(R.id.total_products_text);
        totalOrdersText = view.findViewById(R.id.total_orders_text);
        pendingOrdersText = view.findViewById(R.id.pending_orders_text);
    }

    private void initializeData() {
        databaseHelper = new DatabaseHelper(getContext());
        userManager = new UserManager(getContext());
    }

    private void setupQuickActions(View view) {
        // Set up click listeners for quick action cards
        MaterialCardView manageUsersCard = view.findViewById(R.id.manage_users_card);
        MaterialCardView manageProductsCard = view.findViewById(R.id.manage_products_card);
        MaterialCardView manageOrdersCard = view.findViewById(R.id.manage_orders_card);
        MaterialCardView manageOffersCard = view.findViewById(R.id.manage_offers_card);

        manageUsersCard.setOnClickListener(v -> {
            // Navigate to user management
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ManageUsersFragment())
                    .addToBackStack(null)
                    .commit();
            }
        });

        manageProductsCard.setOnClickListener(v -> {
            // Navigate to product management
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ManageProductsFragment())
                    .addToBackStack(null)
                    .commit();
            }
        });

        manageOrdersCard.setOnClickListener(v -> {
            // Navigate to order management
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ManageOrdersFragment())
                    .addToBackStack(null)
                    .commit();
            }
        });

        manageOffersCard.setOnClickListener(v -> {
            // Navigate to special offers management
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ManageSpecialOffersFragment())
                    .addToBackStack(null)
                    .commit();
            }
        });
    }

    private void loadDashboardData() {
        // Load statistics
        int totalUsers = userManager.getUserCount();
        int totalProducts = databaseHelper.getProductCount();
        int totalOrders = databaseHelper.getTotalOrdersCount();
        int pendingOrders = databaseHelper.getPendingOrdersCount();

        // Update UI
        totalUsersText.setText(String.valueOf(totalUsers));
        totalProductsText.setText(String.valueOf(totalProducts));
        totalOrdersText.setText(String.valueOf(totalOrders));
        pendingOrdersText.setText(String.valueOf(pendingOrders));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData(); // Refresh data when returning to fragment
    }
}
