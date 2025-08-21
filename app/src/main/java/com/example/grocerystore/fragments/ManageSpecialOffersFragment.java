package com.example.grocerystore.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.R;
import com.example.grocerystore.adapters.AdminOffersAdapter;
import com.example.grocerystore.database.DatabaseHelper;
import com.example.grocerystore.models.Offer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ManageSpecialOffersFragment extends Fragment implements AdminOffersAdapter.OnOfferActionListener {

    private RecyclerView recyclerView;
    private AdminOffersAdapter adapter;
    private FloatingActionButton fabAddOffer;
    private DatabaseHelper databaseHelper;
    private List<Offer> offersList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_special_offers, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupFAB();
        
        databaseHelper = new DatabaseHelper(getContext());
        loadOffers();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.offers_recycler_view);
        fabAddOffer = view.findViewById(R.id.fab_add_offer);
    }

    private void setupRecyclerView() {
        offersList = new ArrayList<>();
        adapter = new AdminOffersAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFAB() {
        fabAddOffer.setOnClickListener(v -> {
            // Navigate to Add Special Offer fragment
            if (getActivity() != null) {
                Fragment addOfferFragment = new AddSpecialOfferFragment();
                getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, addOfferFragment)
                    .addToBackStack(null)
                    .commit();
            }
        });
    }

    private void loadOffers() {
        offersList = databaseHelper.getAllOffers();
        adapter.updateOffers(offersList);
        
        if (offersList.isEmpty()) {
            // Show empty state message
            Toast.makeText(getContext(), "No special offers found. Add some offers!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDeleteOffer(Offer offer) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Offer")
            .setMessage("Are you sure you want to delete this special offer: " + offer.getProductName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (databaseHelper.deleteOffer(offer.getId())) {
                    offersList.remove(offer);
                    adapter.updateOffers(offersList);
                    Toast.makeText(getContext(), "Offer deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to delete offer", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onEditOffer(Offer offer) {
        // Navigate to AddSpecialOfferFragment with existing offer data for editing
        if (getActivity() != null) {
            Fragment editOfferFragment = new AddSpecialOfferFragment();
            
            // Pass the offer data to the fragment
            Bundle args = new Bundle();
            args.putSerializable("edit_offer", offer);
            editOfferFragment.setArguments(args);
            
            getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, editOfferFragment)
                .addToBackStack(null)
                .commit();
        }
    }

    @Override
    public void onToggleOfferStatus(Offer offer) {
        offer.setActive(!offer.isActive());
        if (databaseHelper.updateOffer(offer)) {
            adapter.updateOffers(offersList);
            String status = offer.isActive() ? "activated" : "deactivated";
            Toast.makeText(getContext(), "Offer " + status + " successfully", Toast.LENGTH_SHORT).show();
        } else {
            offer.setActive(!offer.isActive()); // Revert the change
            Toast.makeText(getContext(), "Failed to update offer status", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewOffer(Offer offer) {
        // Show offer details in a dialog
        String details = "Product: " + offer.getProductName() + "\n" +
                        "Category: " + offer.getCategory() + "\n" +
                        "Original Price: $" + String.format("%.2f", offer.getOriginalPrice()) + "\n" +
                        "Sale Price: $" + String.format("%.2f", offer.getDiscountedPrice()) + "\n" +
                        "Discount: " + String.format("%.1f", offer.getDiscountPercentage()) + "%\n" +
                        "Stock: " + offer.getStockQuantity() + "\n" +
                        "Status: " + (offer.isActive() ? "Active" : "Inactive") + "\n\n" +
                        "Description:\n" + offer.getDescription();
        
        new AlertDialog.Builder(getContext())
            .setTitle("Offer Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the offers list when returning to this fragment
        loadOffers();
    }
}
