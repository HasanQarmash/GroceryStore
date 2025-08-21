package com.example.grocerystore;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.grocerystore.fragments.ContactUsFragment;
import com.example.grocerystore.fragments.HomeFragment;
import com.example.grocerystore.fragments.MyFavoritesFragment;
import com.example.grocerystore.fragments.MyOrdersFragment;
import com.example.grocerystore.fragments.OffersFragment;
import com.example.grocerystore.fragments.ProductsFragment;
import com.example.grocerystore.fragments.ProfileFragment;
// Admin fragments
import com.example.grocerystore.fragments.AdminDashboardFragment;
import com.example.grocerystore.fragments.ManageUsersFragment;
import com.example.grocerystore.fragments.ManageProductsFragment;
import com.example.grocerystore.fragments.ManageOrdersFragment;
import com.example.grocerystore.fragments.AddAdminFragment;
import com.example.grocerystore.fragments.AddSpecialOfferFragment;
import com.example.grocerystore.model.User;
import com.example.grocerystore.utils.PreferencesManager;
import com.example.grocerystore.utils.UserManager;
import com.google.android.material.navigation.NavigationView;

public class NavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    
    private PreferencesManager preferencesManager;
    private UserManager userManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Initialize managers
        preferencesManager = new PreferencesManager(this);
        userManager = new UserManager(this);
        
        // Initialize database with real products if empty
        com.example.grocerystore.database.DatabaseHelper databaseHelper = 
            new com.example.grocerystore.database.DatabaseHelper(this);
        databaseHelper.initializeRealProductsIfEmpty();
        
        // For testing: uncomment this line to force reset database with your images
        // databaseHelper.forceResetForTesting();

        // Initialize views
        initializeViews();
        
        // Setup navigation
        setupNavigationDrawer();
        
        // Load user data
        loadCurrentUser();
        
        // Set default fragment
        if (savedInstanceState == null) {
            setDefaultFragment();
        }
        
        // Setup back press handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
    }

    private void setupNavigationDrawer() {
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void loadCurrentUser() {
        String email = preferencesManager.getLoggedInUserEmail();
        if (email != null && !email.isEmpty()) {
            currentUser = userManager.getUserByEmail(email);
            updateNavigationHeader();
            setupMenuForUserRole();
        }
    }

    private void setupMenuForUserRole() {
        if (currentUser != null && currentUser.isAdmin()) {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.activity_admin_navigation_drawer);
        } else {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.activity_navigation_drawer);
        }
    }

    private void setDefaultFragment() {
        if (currentUser != null && currentUser.isAdmin()) {
            loadFragment(new AdminDashboardFragment());
            navigationView.setCheckedItem(R.id.nav_admin_dashboard);
        } else {
            loadFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView userNameText = headerView.findViewById(R.id.user_name);
        TextView userEmailText = headerView.findViewById(R.id.user_email);

        if (currentUser != null) {
            String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();
            userNameText.setText(fullName);
            userEmailText.setText(currentUser.getEmail());
            
            // Debug logging
            System.out.println("DEBUG: Navigation header updated with: " + fullName + " (" + currentUser.getEmail() + ")");
        } else {
            // Check if we have logged-in user email but no user data
            String loggedInEmail = preferencesManager.getLoggedInUserEmail();
            if (loggedInEmail != null && !loggedInEmail.isEmpty()) {
                // Try to reload user data
                currentUser = userManager.getUserByEmail(loggedInEmail);
                if (currentUser != null) {
                    String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();
                    userNameText.setText(fullName);
                    userEmailText.setText(currentUser.getEmail());
                    System.out.println("DEBUG: Reloaded user data: " + fullName);
                } else {
                    userNameText.setText("User Not Found");
                    userEmailText.setText(loggedInEmail);
                    System.out.println("DEBUG: User not found for email: " + loggedInEmail);
                }
            } else {
                userNameText.setText("Guest User");
                userEmailText.setText("guest@grocerystore.com");
                System.out.println("DEBUG: No logged-in email found");
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        // User menu items
        if (itemId == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.nav_products) {
            fragment = new ProductsFragment();
        } else if (itemId == R.id.nav_my_orders) {
            fragment = new MyOrdersFragment();
        } else if (itemId == R.id.nav_my_favorites) {
            fragment = new MyFavoritesFragment();
        } else if (itemId == R.id.nav_offers) {
            fragment = new OffersFragment();
        } else if (itemId == R.id.nav_profile) {
            fragment = new ProfileFragment();
        } 
        // Admin menu items
        else if (itemId == R.id.nav_admin_dashboard) {
            fragment = new AdminDashboardFragment();
        } else if (itemId == R.id.nav_manage_users) {
            fragment = new ManageUsersFragment();
        } else if (itemId == R.id.nav_manage_products) {
            fragment = new ManageProductsFragment();
        } else if (itemId == R.id.nav_manage_orders) {
            fragment = new ManageOrdersFragment();
        } else if (itemId == R.id.nav_add_admin) {
            fragment = new AddAdminFragment();
        } else if (itemId == R.id.nav_add_special_offer) {
            fragment = new AddSpecialOfferFragment();
        }
        // Common items
        else if (itemId == R.id.nav_contact_us) {
            fragment = new ContactUsFragment();
        } else if (itemId == R.id.nav_logout) {
            showLogoutConfirmation();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (fragment != null) {
            loadFragment(fragment);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void performLogout() {
        // Clear login preferences
        preferencesManager.clearLoginData();
        
        // Show logout message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        
        // Navigate back to login
        Intent intent = new Intent(NavigationActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void refreshUserData() {
        loadCurrentUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when activity resumes in case it was updated
        refreshUserData();
    }
}
