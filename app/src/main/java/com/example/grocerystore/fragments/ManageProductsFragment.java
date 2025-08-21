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
import com.example.grocerystore.adapters.AdminProductsAdapter;
import com.example.grocerystore.database.DatabaseHelper;
import com.example.grocerystore.models.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ManageProductsFragment extends Fragment implements AdminProductsAdapter.OnProductActionListener {

    private RecyclerView productsRecyclerView;
    private AdminProductsAdapter productsAdapter;
    private DatabaseHelper databaseHelper;
    private FloatingActionButton addProductFab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            getActivity().setTitle("Manage Products");
        }

        initializeViews(view);
        initializeData();
        setupRecyclerView();
        loadProducts();
    }

    private void initializeViews(View view) {
        productsRecyclerView = view.findViewById(R.id.products_recycler_view);
        addProductFab = view.findViewById(R.id.add_product_fab);
        
        addProductFab.setOnClickListener(v -> showAddProductDialog());
    }

    private void initializeData() {
        databaseHelper = new DatabaseHelper(getContext());
    }

    private void setupRecyclerView() {
        productsAdapter = new AdminProductsAdapter(this);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productsRecyclerView.setAdapter(productsAdapter);
    }

    private void loadProducts() {
        List<Product> products = databaseHelper.getAllProducts();
        productsAdapter.updateProducts(products);
    }

    private void showAddProductDialog() {
        showProductDialog(null, "Add Product");
    }

    @Override
    public void onDeleteProduct(Product product) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (databaseHelper.deleteProduct(product.getId())) {
                        Toast.makeText(getContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();
                        loadProducts(); // Refresh the list
                    } else {
                        Toast.makeText(getContext(), "Failed to delete product", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditProduct(Product product) {
        showProductDialog(product, "Edit Product");
    }

    @Override
    public void onViewProduct(Product product) {
        showProductDetailsDialog(product);
    }

    private void showProductDetailsDialog(Product product) {
        String details = "Name: " + product.getName() + "\n" +
                "Category: " + product.getCategory() + "\n" +
                "Price: $" + String.format("%.2f", product.getPrice()) + "\n" +
                "Description: " + product.getDescription();

        new AlertDialog.Builder(getContext())
                .setTitle("Product Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showProductDialog(Product product, String title) {
        boolean isEdit = product != null;
        
        // Create input fields
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText nameEdit = new EditText(getContext());
        nameEdit.setHint("Product Name");
        if (isEdit) nameEdit.setText(product.getName());
        layout.addView(nameEdit);

        EditText descriptionEdit = new EditText(getContext());
        descriptionEdit.setHint("Description");
        if (isEdit) descriptionEdit.setText(product.getDescription());
        layout.addView(descriptionEdit);

        EditText priceEdit = new EditText(getContext());
        priceEdit.setHint("Price");
        priceEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (isEdit) priceEdit.setText(String.valueOf(product.getPrice()));
        layout.addView(priceEdit);

        EditText categoryEdit = new EditText(getContext());
        categoryEdit.setHint("Category");
        if (isEdit) categoryEdit.setText(product.getCategory());
        layout.addView(categoryEdit);

        EditText imageUrlEdit = new EditText(getContext());
        imageUrlEdit.setHint("Image URL (optional)");
        if (isEdit) imageUrlEdit.setText(product.getImageUrl());
        layout.addView(imageUrlEdit);

        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(layout)
                .setPositiveButton(isEdit ? "Update" : "Add", (dialog, which) -> {
                    String name = nameEdit.getText().toString().trim();
                    String description = descriptionEdit.getText().toString().trim();
                    String priceStr = priceEdit.getText().toString().trim();
                    String category = categoryEdit.getText().toString().trim();
                    String imageUrl = imageUrlEdit.getText().toString().trim();

                    if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double price = Double.parseDouble(priceStr);
                        
                        if (isEdit) {
                            // Update existing product
                            product.setName(name);
                            product.setDescription(description);
                            product.setPrice(price);
                            product.setCategory(category);
                            product.setImageUrl(imageUrl.isEmpty() ? "https://via.placeholder.com/150" : imageUrl);

                            if (databaseHelper.updateProduct(product)) {
                                Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
                                loadProducts();
                            } else {
                                Toast.makeText(getContext(), "Failed to update product", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Add new product
                            Product newProduct = new Product(
                                0, // ID will be auto-generated
                                name,
                                category,
                                price,
                                1, // Default stock quantity
                                imageUrl.isEmpty() ? "https://via.placeholder.com/150" : imageUrl
                            );
                            newProduct.setDescription(description);

                            if (databaseHelper.addProduct(newProduct) > 0) {
                                Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
                                loadProducts();
                            } else {
                                Toast.makeText(getContext(), "Failed to add product", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Please enter a valid price", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts(); // Refresh data when returning to fragment
    }
}
