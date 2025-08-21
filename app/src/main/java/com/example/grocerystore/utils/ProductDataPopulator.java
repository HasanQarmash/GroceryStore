package com.example.grocerystore.utils;

import android.content.Context;
import com.example.grocerystore.database.DatabaseHelper;
import com.example.grocerystore.models.Product;
import java.util.ArrayList;
import java.util.List;

public class ProductDataPopulator {
    
    private DatabaseHelper databaseHelper;
    
    public ProductDataPopulator(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }
    
    public void populateRealProducts() {
        // Check if products already exist
        List<Product> existingProducts = databaseHelper.getAllProducts();
        if (!existingProducts.isEmpty()) {
            return; // Products already exist
        }
        
        List<Product> realProducts = createRealProductList();
        
        for (Product product : realProducts) {
            databaseHelper.addProduct(product);
        }
    }
    
    private List<Product> createRealProductList() {
        List<Product> products = new ArrayList<>();
        
        // Fruits
        products.add(new Product(0, "Fresh Bananas", "Fruits", 2.99, 50, 
            "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Sweet and ripe bananas, perfect for snacking or baking");
        
        products.add(new Product(0, "Red Apples", "Fruits", 3.49, 40, 
            "https://images.unsplash.com/photo-1568702846914-96b305d2aaeb?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Crisp and juicy red apples, great source of fiber");
        
        products.add(new Product(0, "Fresh Oranges", "Fruits", 4.99, 35, 
            "https://images.unsplash.com/photo-1582979512210-99b6a53386f9?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Vitamin C rich oranges, perfect for fresh juice");
        
        products.add(new Product(0, "Strawberries", "Fruits", 5.99, 25, 
            "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Sweet and fresh strawberries, perfect for desserts");
        
        products.add(new Product(0, "Avocados", "Fruits", 6.99, 30, 
            "https://images.unsplash.com/photo-1560272564-c83b66b1ad12?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Creamy avocados, rich in healthy fats");
        
        // Vegetables
        products.add(new Product(0, "Fresh Carrots", "Vegetables", 2.49, 60, 
            "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Crunchy orange carrots, high in beta-carotene");
        
        products.add(new Product(0, "Broccoli", "Vegetables", 3.99, 45, 
            "https://images.unsplash.com/photo-1459411621453-7b03977f4bfc?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Fresh green broccoli, packed with vitamins");
        
        products.add(new Product(0, "Bell Peppers", "Vegetables", 4.49, 35, 
            "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Colorful bell peppers, sweet and crunchy");
        
        products.add(new Product(0, "Spinach Leaves", "Vegetables", 3.29, 40, 
            "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Fresh baby spinach, rich in iron and vitamins");
        
        products.add(new Product(0, "Tomatoes", "Vegetables", 3.79, 55, 
            "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Ripe red tomatoes, perfect for salads and cooking");
        
        // Dairy
        products.add(new Product(0, "Fresh Milk", "Dairy", 4.99, 20, 
            "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Fresh whole milk, rich in calcium and protein");
        
        products.add(new Product(0, "Greek Yogurt", "Dairy", 5.49, 25, 
            "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Creamy Greek yogurt, high in protein");
        
        products.add(new Product(0, "Cheddar Cheese", "Dairy", 7.99, 15, 
            "https://images.unsplash.com/photo-1486297678162-eb2a19b0a32d?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Sharp cheddar cheese, perfect for sandwiches");
        
        products.add(new Product(0, "Fresh Eggs", "Dairy", 3.99, 30, 
            "https://images.unsplash.com/photo-1518569656558-1f25e69d93d7?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Farm fresh eggs, great source of protein");
        
        // Meat
        products.add(new Product(0, "Chicken Breast", "Meat", 12.99, 20, 
            "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Lean chicken breast, perfect for healthy meals");
        
        products.add(new Product(0, "Ground Beef", "Meat", 8.99, 18, 
            "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Fresh ground beef, great for burgers and tacos");
        
        products.add(new Product(0, "Salmon Fillet", "Meat", 15.99, 12, 
            "https://images.unsplash.com/photo-1574781330855-d0db8cc6a79c?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Fresh salmon fillet, rich in omega-3 fatty acids");
        
        // Bakery
        products.add(new Product(0, "Whole Wheat Bread", "Bakery", 3.49, 25, 
            "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Fresh whole wheat bread, perfect for toast");
        
        products.add(new Product(0, "Croissants", "Bakery", 4.99, 15, 
            "https://images.unsplash.com/photo-1555507036-ab794f4d85a3?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Buttery French croissants, perfect for breakfast");
        
        products.add(new Product(0, "Bagels", "Bakery", 3.99, 20, 
            "https://images.unsplash.com/photo-1551106652-a5bcf4b29ab6?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Fresh bagels, great with cream cheese");
        
        // Beverages
        products.add(new Product(0, "Orange Juice", "Beverages", 4.49, 30, 
            "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Fresh orange juice, 100% pure with no added sugar");
        
        products.add(new Product(0, "Coffee Beans", "Beverages", 12.99, 20, 
            "https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Premium coffee beans, perfect for your morning brew");
        
        products.add(new Product(0, "Green Tea", "Beverages", 8.99, 25, 
            "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Premium green tea, rich in antioxidants");
        
        // Snacks
        products.add(new Product(0, "Mixed Nuts", "Snacks", 9.99, 22, 
            "https://images.unsplash.com/photo-1599599810694-57a2ca1943c6?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Premium mixed nuts, perfect healthy snack");
        
        products.add(new Product(0, "Dark Chocolate", "Snacks", 5.99, 30, 
            "https://images.unsplash.com/photo-1511381939415-e44015466834?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Rich dark chocolate, 70% cocoa content");
        
        products.add(new Product(0, "Granola Bars", "Snacks", 6.49, 35, 
            "https://images.unsplash.com/photo-1571919743851-2c0df9c9d44b?w=300&h=300&fit=crop"));
        products.get(products.size()-1).setDescription("Healthy granola bars with oats and honey");
        
        return products;
    }
}
