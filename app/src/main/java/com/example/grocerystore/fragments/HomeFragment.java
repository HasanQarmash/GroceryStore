package com.example.grocerystore.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.grocerystore.R;
import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {

    private MaterialButton btnViewOffers, btnMyOrders, btnContactUs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Set the title in the toolbar
        if (getActivity() != null) {
            getActivity().setTitle("Home");
        }
        
        initializeViews(view);
        setupQuickActions();
    }
    
    private void initializeViews(View view) {
        btnViewOffers = view.findViewById(R.id.btnViewOffers);
        btnMyOrders = view.findViewById(R.id.btnMyOrders);
        btnContactUs = view.findViewById(R.id.btnContactUs);
    }
    
    private void setupQuickActions() {
        // View Offers button
        btnViewOffers.setOnClickListener(v -> {
            navigateToFragment(new OffersFragment(), "Offers");
        });
        
        // My Orders button
        btnMyOrders.setOnClickListener(v -> {
            navigateToFragment(new MyOrdersFragment(), "My Orders");
        });
        
        // Contact Us button
        btnContactUs.setOnClickListener(v -> {
            navigateToFragment(new ContactUsFragment(), "Contact Us");
        });
    }
    
    private void navigateToFragment(Fragment fragment, String title) {
        if (getActivity() != null) {
            getActivity().setTitle(title);
            
            // Use the same navigation method as NavigationActivity
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}
