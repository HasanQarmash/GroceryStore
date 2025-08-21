package com.example.grocerystore.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.grocerystore.R;
import com.google.android.material.button.MaterialButton;

public class ContactUsFragment extends Fragment {

    private MaterialButton btnCall, btnEmail, btnLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact_us, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            getActivity().setTitle("Contact Us");
        }

        initializeViews(view);
        setupClickListeners();
    }

    private void initializeViews(View view) {
        btnCall = view.findViewById(R.id.btnCall);
        btnEmail = view.findViewById(R.id.btnEmail);
        btnLocation = view.findViewById(R.id.btnLocation);
    }

    private void setupClickListeners() {
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
    }

    private void makePhoneCall() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:+970599000000"));
            startActivity(callIntent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to make phone call", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:YourStoreName@store.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Customer Support Inquiry");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Dear Store Team,\n\nI would like to inquire about...\n\nBest regards,");
            
            if (emailIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(emailIntent);
            } else {
                Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to send email", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMap() {
        try {
            // Coordinates for Birzeit University (Birzeit, Palestine)
            String location = "31.966900,35.183000";
            String label = "Birzeit University";
            String uriString = "geo:" + location + "?q=" + location + "(" + label + ")";
            
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
            mapIntent.setPackage("com.google.android.apps.maps");
            
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // If Google Maps is not installed, open in browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                    Uri.parse("https://www.google.com/maps?q=" + location));
                startActivity(browserIntent);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to open map", Toast.LENGTH_SHORT).show();
        }
    }
}
