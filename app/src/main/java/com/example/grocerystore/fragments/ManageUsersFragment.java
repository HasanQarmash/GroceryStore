package com.example.grocerystore.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.R;
import com.example.grocerystore.adapters.AdminUsersAdapter;
import com.example.grocerystore.fragments.AddAdminFragment;
import com.example.grocerystore.model.User;
import com.example.grocerystore.utils.UserManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ManageUsersFragment extends Fragment implements AdminUsersAdapter.OnUserActionListener {

    private RecyclerView usersRecyclerView;
    private AdminUsersAdapter usersAdapter;
    private UserManager userManager;
    private FloatingActionButton addUserFab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            getActivity().setTitle("Manage Users");
        }

        initializeViews(view);
        initializeData();
        setupRecyclerView();
        loadUsers();
    }

    private void initializeViews(View view) {
        usersRecyclerView = view.findViewById(R.id.users_recycler_view);
        addUserFab = view.findViewById(R.id.add_user_fab);
        
        addUserFab.setOnClickListener(v -> showAddUserDialog());
    }

    private void initializeData() {
        userManager = new UserManager(getContext());
    }

    private void setupRecyclerView() {
        usersAdapter = new AdminUsersAdapter(this);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRecyclerView.setAdapter(usersAdapter);
    }

    private void loadUsers() {
        List<User> users = userManager.getAllUsers();
        usersAdapter.updateUsers(users);
    }

    private void showAddUserDialog() {
        // Navigate to Add Admin/User fragment
        if (getActivity() != null) {
            AddAdminFragment addAdminFragment = new AddAdminFragment();
            getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, addAdminFragment)
                .addToBackStack(null)
                .commit();
        }
    }

    @Override
    public void onDeleteUser(User user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getFirstName() + " " + user.getLastName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (userManager.deleteUser(user.getId())) {
                        Toast.makeText(getContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
                        loadUsers(); // Refresh the list
                    } else {
                        Toast.makeText(getContext(), "Failed to delete user", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditUser(User user) {
        // Show edit user dialog
        showEditUserDialog(user);
    }

    @Override
    public void onViewUser(User user) {
        // Show user details in a dialog or new fragment
        showUserDetailsDialog(user);
    }

    private void showUserDetailsDialog(User user) {
        String details = "Name: " + user.getFirstName() + " " + user.getLastName() + "\n" +
                "Email: " + user.getEmail() + "\n" +
                "Phone: " + user.getPhoneNumber() + "\n" +
                "Role: " + user.getRole();

        new AlertDialog.Builder(getContext())
                .setTitle("User Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showEditUserDialog(User user) {
        // Create input fields
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText firstNameEdit = new EditText(getContext());
        firstNameEdit.setHint("First Name");
        firstNameEdit.setText(user.getFirstName());
        layout.addView(firstNameEdit);

        EditText lastNameEdit = new EditText(getContext());
        lastNameEdit.setHint("Last Name");
        lastNameEdit.setText(user.getLastName());
        layout.addView(lastNameEdit);

        EditText phoneEdit = new EditText(getContext());
        phoneEdit.setHint("Phone Number");
        phoneEdit.setText(user.getPhoneNumber());
        layout.addView(phoneEdit);

        new AlertDialog.Builder(getContext())
                .setTitle("Edit User")
                .setView(layout)
                .setPositiveButton("Update", (dialog, which) -> {
                    String firstName = firstNameEdit.getText().toString().trim();
                    String lastName = lastNameEdit.getText().toString().trim();
                    String phone = phoneEdit.getText().toString().trim();

                    if (firstName.isEmpty() || lastName.isEmpty()) {
                        Toast.makeText(getContext(), "Name fields cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update user
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setPhoneNumber(phone);

                    if (userManager.updateUser(user)) {
                        Toast.makeText(getContext(), "User updated successfully", Toast.LENGTH_SHORT).show();
                        loadUsers(); // Refresh the list
                    } else {
                        Toast.makeText(getContext(), "Failed to update user", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers(); // Refresh data when returning to fragment
    }
}
