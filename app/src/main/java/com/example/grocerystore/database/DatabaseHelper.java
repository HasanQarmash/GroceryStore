package com.example.grocerystore.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.grocerystore.models.Product;
import com.example.grocerystore.models.Order;
import com.example.grocerystore.models.Offer;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "grocery_store.db";
    private static final int DATABASE_VERSION = 7;

    // Products table
    private static final String TABLE_PRODUCTS = "products";
    private static final String COLUMN_PRODUCT_ID = "id";
    private static final String COLUMN_PRODUCT_NAME = "name";
    private static final String COLUMN_PRODUCT_CATEGORY = "category";
    private static final String COLUMN_PRODUCT_PRICE = "price";
    private static final String COLUMN_PRODUCT_STOCK = "stock_quantity";
    private static final String COLUMN_PRODUCT_IMAGE = "image_url";
    private static final String COLUMN_PRODUCT_DESCRIPTION = "description";
    private static final String COLUMN_PRODUCT_AVAILABLE = "is_available";

    // Orders table
    private static final String TABLE_ORDERS = "orders";
    private static final String COLUMN_ORDER_ID = "id";
    private static final String COLUMN_ORDER_USER_ID = "user_id";
    private static final String COLUMN_ORDER_PRODUCT_ID = "product_id";
    private static final String COLUMN_ORDER_PRODUCT_NAME = "product_name";
    private static final String COLUMN_ORDER_QUANTITY = "quantity";
    private static final String COLUMN_ORDER_UNIT_PRICE = "unit_price";
    private static final String COLUMN_ORDER_TOTAL_PRICE = "total_price";
    private static final String COLUMN_ORDER_DELIVERY_METHOD = "delivery_method";
    private static final String COLUMN_ORDER_STATUS = "status";
    private static final String COLUMN_ORDER_DATE = "order_date";
    private static final String COLUMN_ORDER_DELIVERY_ADDRESS = "delivery_address";
    private static final String COLUMN_ORDER_SYNCED = "is_synced";

    // Favorites table
    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_FAVORITE_ID = "id";
    private static final String COLUMN_FAVORITE_USER_ID = "user_id";
    private static final String COLUMN_FAVORITE_PRODUCT_ID = "product_id";

    // Offers table
    private static final String TABLE_OFFERS = "offers";
    private static final String COLUMN_OFFER_ID = "id";
    private static final String COLUMN_OFFER_PRODUCT_ID = "product_id";
    private static final String COLUMN_OFFER_PRODUCT_NAME = "product_name";
    private static final String COLUMN_OFFER_CATEGORY = "category";
    private static final String COLUMN_OFFER_ORIGINAL_PRICE = "original_price";
    private static final String COLUMN_OFFER_DISCOUNTED_PRICE = "discounted_price";
    private static final String COLUMN_OFFER_STOCK_QUANTITY = "stock_quantity";
    private static final String COLUMN_OFFER_DESCRIPTION = "description";
    private static final String COLUMN_OFFER_IMAGE_URL = "image_url";
    private static final String COLUMN_OFFER_IS_ACTIVE = "is_active";
    private static final String COLUMN_OFFER_CREATED_AT = "created_at";
    private static final String COLUMN_OFFER_EXPIRES_AT = "expires_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            android.util.Log.d("DatabaseHelper", "=== CREATING DATABASE TABLES ===");
            
            // Create products table
            String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS + "(" +
                    COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_PRODUCT_NAME + " TEXT NOT NULL," +
                    COLUMN_PRODUCT_CATEGORY + " TEXT NOT NULL," +
                    COLUMN_PRODUCT_PRICE + " REAL NOT NULL," +
                    COLUMN_PRODUCT_STOCK + " INTEGER NOT NULL," +
                    COLUMN_PRODUCT_IMAGE + " TEXT," +
                    COLUMN_PRODUCT_DESCRIPTION + " TEXT," +
                    COLUMN_PRODUCT_AVAILABLE + " INTEGER DEFAULT 1" +
                    ")";
            db.execSQL(createProductsTable);
            android.util.Log.d("DatabaseHelper", "✅ Products table created");

            // Create orders table
            String createOrdersTable = "CREATE TABLE " + TABLE_ORDERS + "(" +
                    COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_ORDER_USER_ID + " INTEGER NOT NULL," +
                    COLUMN_ORDER_PRODUCT_ID + " INTEGER NOT NULL," +
                    COLUMN_ORDER_PRODUCT_NAME + " TEXT NOT NULL," +
                    COLUMN_ORDER_QUANTITY + " INTEGER NOT NULL," +
                    COLUMN_ORDER_UNIT_PRICE + " REAL NOT NULL," +
                    COLUMN_ORDER_TOTAL_PRICE + " REAL NOT NULL," +
                    COLUMN_ORDER_DELIVERY_METHOD + " TEXT NOT NULL," +
                    COLUMN_ORDER_STATUS + " TEXT DEFAULT 'pending'," +
                    COLUMN_ORDER_DATE + " TEXT," +
                    COLUMN_ORDER_DELIVERY_ADDRESS + " TEXT," +
                    COLUMN_ORDER_SYNCED + " INTEGER DEFAULT 0" +
                    ")";
            db.execSQL(createOrdersTable);
            android.util.Log.d("DatabaseHelper", "✅ Orders table created");

            // Create favorites table
            String createFavoritesTable = "CREATE TABLE " + TABLE_FAVORITES + "(" +
                    COLUMN_FAVORITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_FAVORITE_USER_ID + " INTEGER NOT NULL," +
                    COLUMN_FAVORITE_PRODUCT_ID + " INTEGER NOT NULL," +
                    "UNIQUE(" + COLUMN_FAVORITE_USER_ID + "," + COLUMN_FAVORITE_PRODUCT_ID + ")" +
                    ")";
            db.execSQL(createFavoritesTable);
            android.util.Log.d("DatabaseHelper", "✅ Favorites table created");

            // Create offers table
            String createOffersTable = "CREATE TABLE " + TABLE_OFFERS + "(" +
                    COLUMN_OFFER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_OFFER_PRODUCT_ID + " INTEGER," +
                    COLUMN_OFFER_PRODUCT_NAME + " TEXT NOT NULL," +
                    COLUMN_OFFER_CATEGORY + " TEXT NOT NULL," +
                    COLUMN_OFFER_ORIGINAL_PRICE + " REAL NOT NULL," +
                    COLUMN_OFFER_DISCOUNTED_PRICE + " REAL NOT NULL," +
                    COLUMN_OFFER_STOCK_QUANTITY + " INTEGER NOT NULL," +
                    COLUMN_OFFER_DESCRIPTION + " TEXT," +
                    COLUMN_OFFER_IMAGE_URL + " TEXT," +
                    COLUMN_OFFER_IS_ACTIVE + " INTEGER DEFAULT 1," +
                    COLUMN_OFFER_CREATED_AT + " INTEGER," +
                    COLUMN_OFFER_EXPIRES_AT + " INTEGER," +
                    "FOREIGN KEY(" + COLUMN_OFFER_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COLUMN_PRODUCT_ID + ")" +
                    ")";
            db.execSQL(createOffersTable);
            android.util.Log.d("DatabaseHelper", "✅ Offers table created");

            // Insert sample products
            addRealProductsWithImages(db);
            
            android.util.Log.d("DatabaseHelper", "✅ Database creation completed successfully");
            
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error creating database", e);
            // Don't rethrow - let app continue with empty database
        }
        
        // Don't insert sample orders automatically - they will be created per user
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        android.util.Log.d("DatabaseHelper", "=== DATABASE UPGRADE ===");
        android.util.Log.d("DatabaseHelper", "Upgrading from version " + oldVersion + " to " + newVersion);
        
        // Instead of dropping all tables, preserve offers if they exist
        if (oldVersion < 5) {
            // Version 5: Fix image URLs and preserve offers
            try {
                // Check if offers table exists
                Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + TABLE_OFFERS + "'", null);
                boolean offersTableExists = cursor.getCount() > 0;
                cursor.close();
                
                if (!offersTableExists) {
                    // Create offers table if it doesn't exist
                    String createOffersTable = "CREATE TABLE " + TABLE_OFFERS + "(" +
                            COLUMN_OFFER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                            COLUMN_OFFER_PRODUCT_NAME + " TEXT NOT NULL," +
                            COLUMN_OFFER_CATEGORY + " TEXT NOT NULL," +
                            COLUMN_OFFER_ORIGINAL_PRICE + " REAL NOT NULL," +
                            COLUMN_OFFER_DISCOUNTED_PRICE + " REAL NOT NULL," +
                            COLUMN_OFFER_STOCK_QUANTITY + " INTEGER NOT NULL," +
                            COLUMN_OFFER_DESCRIPTION + " TEXT," +
                            COLUMN_OFFER_IMAGE_URL + " TEXT," +
                            COLUMN_OFFER_IS_ACTIVE + " INTEGER DEFAULT 1," +
                            COLUMN_OFFER_CREATED_AT + " INTEGER," +
                            COLUMN_OFFER_EXPIRES_AT + " INTEGER" +
                            ")";
                    db.execSQL(createOffersTable);
                    android.util.Log.d("DatabaseHelper", "Created offers table");
                }
                
                // Only recreate other tables that need changes
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
                
                // Recreate non-offers tables
                String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS + "(" +
                        COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_PRODUCT_NAME + " TEXT NOT NULL," +
                        COLUMN_PRODUCT_CATEGORY + " TEXT NOT NULL," +
                        COLUMN_PRODUCT_PRICE + " REAL NOT NULL," +
                        COLUMN_PRODUCT_STOCK + " INTEGER NOT NULL," +
                        COLUMN_PRODUCT_IMAGE + " TEXT," +
                        COLUMN_PRODUCT_DESCRIPTION + " TEXT," +
                        COLUMN_PRODUCT_AVAILABLE + " INTEGER DEFAULT 1" +
                        ")";
                db.execSQL(createProductsTable);

                String createOrdersTable = "CREATE TABLE " + TABLE_ORDERS + "(" +
                        COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_ORDER_USER_ID + " INTEGER NOT NULL," +
                        COLUMN_ORDER_PRODUCT_ID + " INTEGER NOT NULL," +
                        COLUMN_ORDER_PRODUCT_NAME + " TEXT NOT NULL," +
                        COLUMN_ORDER_QUANTITY + " INTEGER NOT NULL," +
                        COLUMN_ORDER_UNIT_PRICE + " REAL NOT NULL," +
                        COLUMN_ORDER_TOTAL_PRICE + " REAL NOT NULL," +
                        COLUMN_ORDER_DELIVERY_METHOD + " TEXT NOT NULL," +
                        COLUMN_ORDER_STATUS + " TEXT DEFAULT 'pending'," +
                        COLUMN_ORDER_DATE + " TEXT," +
                        COLUMN_ORDER_DELIVERY_ADDRESS + " TEXT," +
                        COLUMN_ORDER_SYNCED + " INTEGER DEFAULT 0" +
                        ")";
                db.execSQL(createOrdersTable);

                String createFavoritesTable = "CREATE TABLE " + TABLE_FAVORITES + "(" +
                        COLUMN_FAVORITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_FAVORITE_USER_ID + " INTEGER NOT NULL," +
                        COLUMN_FAVORITE_PRODUCT_ID + " INTEGER NOT NULL," +
                        "UNIQUE(" + COLUMN_FAVORITE_USER_ID + ", " + COLUMN_FAVORITE_PRODUCT_ID + ")" +
                        ")";
                db.execSQL(createFavoritesTable);
                
                // Insert sample products
                addRealProductsWithImages(db);
                
                // Create test offers for debugging
                createTestOffers();
                
                android.util.Log.d("DatabaseHelper", "✅ Database upgrade completed - offers preserved");
                
            } catch (Exception e) {
                android.util.Log.e("DatabaseHelper", "Error during upgrade, falling back to full recreate", e);
                // Fallback to original method if upgrade fails
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_OFFERS);
                onCreate(db);
            }
        }
        
        if (oldVersion < 7) {
            // Version 7: Add product_id column to offers table
            try {
                android.util.Log.d("DatabaseHelper", "Adding product_id column to offers table");
                
                // Check if product_id column already exists
                Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_OFFERS + ")", null);
                boolean hasProductIdColumn = false;
                while (cursor.moveToNext()) {
                    String columnName = cursor.getString(1);
                    if (COLUMN_OFFER_PRODUCT_ID.equals(columnName)) {
                        hasProductIdColumn = true;
                        break;
                    }
                }
                cursor.close();
                
                if (!hasProductIdColumn) {
                    // Add the product_id column
                    db.execSQL("ALTER TABLE " + TABLE_OFFERS + " ADD COLUMN " + COLUMN_OFFER_PRODUCT_ID + " INTEGER");
                    android.util.Log.d("DatabaseHelper", "Added product_id column to offers table");
                    
                    // Update existing offers with proper product_ids by matching product names
                    Cursor offersCursor = db.rawQuery("SELECT " + COLUMN_OFFER_ID + ", " + COLUMN_OFFER_PRODUCT_NAME + " FROM " + TABLE_OFFERS, null);
                    while (offersCursor.moveToNext()) {
                        int offerId = offersCursor.getInt(0);
                        String productName = offersCursor.getString(1);
                        
                        // Find matching product by name
                        Cursor productCursor = db.rawQuery("SELECT " + COLUMN_PRODUCT_ID + " FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_PRODUCT_NAME + " = ?", new String[]{productName});
                        if (productCursor.moveToFirst()) {
                            int productId = productCursor.getInt(0);
                            ContentValues values = new ContentValues();
                            values.put(COLUMN_OFFER_PRODUCT_ID, productId);
                            db.update(TABLE_OFFERS, values, COLUMN_OFFER_ID + " = ?", new String[]{String.valueOf(offerId)});
                            android.util.Log.d("DatabaseHelper", "Updated offer " + offerId + " with product_id " + productId);
                        }
                        productCursor.close();
                    }
                    offersCursor.close();
                    
                    android.util.Log.d("DatabaseHelper", "✅ Successfully updated offers table with product_ids");
                } else {
                    android.util.Log.d("DatabaseHelper", "product_id column already exists in offers table");
                }
                
            } catch (Exception e) {
                android.util.Log.e("DatabaseHelper", "Error adding product_id to offers table", e);
            }
        }
        
        android.util.Log.d("DatabaseHelper", "=== END DATABASE UPGRADE ===");
    }

    private void insertSampleProducts(SQLiteDatabase db) {
        String[][] sampleProducts = {
                {"Fresh Apples", "Fruits", "3.99", "50", "https://example.com/apple.jpg", "Fresh red apples, perfect for snacking"},
                {"Bananas", "Fruits", "2.49", "75", "https://example.com/banana.jpg", "Ripe yellow bananas, rich in potassium"},
                {"Whole Milk", "Dairy", "4.99", "25", "https://example.com/milk.jpg", "Fresh whole milk, 1 gallon"},
                {"Bread", "Bakery", "2.99", "40", "https://example.com/bread.jpg", "Freshly baked whole wheat bread"},
                {"Chicken Breast", "Meat", "8.99", "30", "https://example.com/chicken.jpg", "Boneless chicken breast, per pound"},
                {"Broccoli", "Vegetables", "3.49", "20", "https://example.com/broccoli.jpg", "Fresh green broccoli crowns"},
                {"Pasta", "Pantry", "1.99", "60", "https://example.com/pasta.jpg", "Spaghetti pasta, 1 lb box"},
                {"Orange Juice", "Beverages", "5.99", "15", "https://example.com/juice.jpg", "Fresh squeezed orange juice"},
                {"Greek Yogurt", "Dairy", "6.49", "35", "https://example.com/yogurt.jpg", "Plain Greek yogurt, 32 oz"},
                {"Salmon Fillet", "Seafood", "12.99", "12", "https://example.com/salmon.jpg", "Fresh Atlantic salmon fillet"},
                {"Lettuce", "Vegetables", "2.29", "45", "https://example.com/lettuce.jpg", "Crisp romaine lettuce head"},
                {"Eggs", "Dairy", "3.99", "55", "https://example.com/eggs.jpg", "Grade A large eggs, dozen"}
        };

        for (String[] product : sampleProducts) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PRODUCT_NAME, product[0]);
            values.put(COLUMN_PRODUCT_CATEGORY, product[1]);
            values.put(COLUMN_PRODUCT_PRICE, Double.parseDouble(product[2]));
            values.put(COLUMN_PRODUCT_STOCK, Integer.parseInt(product[3]));
            values.put(COLUMN_PRODUCT_IMAGE, product[4]);
            values.put(COLUMN_PRODUCT_DESCRIPTION, product[5]);
            values.put(COLUMN_PRODUCT_AVAILABLE, 1);
            db.insert(TABLE_PRODUCTS, null, values);
        }
    }

    private void insertSampleOrders(SQLiteDatabase db) {
        // Sample user ID (using hash of example email)
        int demoUserId = Math.abs("demo@grocerystore.com".hashCode());
        
        // Sample orders with different statuses and dates
        String[][] sampleOrders = {
            {"1", "Fresh Apples", "2", "3.99", "home", "pending", "2025-01-30 10:15:00", "123 Main St, City, Country"},
            {"2", "Bananas", "3", "2.49", "pickup", "approved", "2025-01-29 14:30:00", ""},
            {"3", "Whole Milk", "1", "4.99", "home", "delivered", "2025-01-28 09:45:00", "123 Main St, City, Country"},
            {"4", "Chicken Breast", "2", "8.99", "home", "pending", "2025-01-31 11:20:00", "123 Main St, City, Country"},
            {"5", "Organic Lettuce", "1", "2.99", "pickup", "delivered", "2025-01-27 16:10:00", ""},
            {"6", "Greek Yogurt", "4", "1.99", "home", "approved", "2025-01-30 13:05:00", "456 Oak Ave, City, Country"}
        };

        for (String[] order : sampleOrders) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ORDER_USER_ID, demoUserId);
            values.put(COLUMN_ORDER_PRODUCT_ID, Integer.parseInt(order[0]));
            values.put(COLUMN_ORDER_PRODUCT_NAME, order[1]);
            values.put(COLUMN_ORDER_QUANTITY, Integer.parseInt(order[2]));
            values.put(COLUMN_ORDER_UNIT_PRICE, Double.parseDouble(order[3]));
            values.put(COLUMN_ORDER_TOTAL_PRICE, 
                Integer.parseInt(order[2]) * Double.parseDouble(order[3]));
            values.put(COLUMN_ORDER_DELIVERY_METHOD, order[4]);
            values.put(COLUMN_ORDER_STATUS, order[5]);
            values.put(COLUMN_ORDER_DATE, order[6]);
            values.put(COLUMN_ORDER_DELIVERY_ADDRESS, order[7]);
            values.put(COLUMN_ORDER_SYNCED, "delivered".equals(order[5]) ? 1 : 0); // Delivered orders are synced
            db.insert(TABLE_ORDERS, null, values);
        }
    }

    public void createSampleOrdersForUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Check if user already has orders
        Cursor cursor = db.query(TABLE_ORDERS, new String[]{"COUNT(*)"}, 
            COLUMN_ORDER_USER_ID + " = ?", new String[]{String.valueOf(userId)}, 
            null, null, null);
        
        int existingOrderCount = 0;
        if (cursor.moveToFirst()) {
            existingOrderCount = cursor.getInt(0);
        }
        cursor.close();
        
        // Only create sample orders if user has no orders
        if (existingOrderCount == 0) {
            String[][] sampleOrders = {
                {"1", "Fresh Apples", "2", "3.99", "home", "pending", "2025-01-30 10:15:00", "123 Main St, City, Country"},
                {"2", "Bananas", "3", "2.49", "pickup", "approved", "2025-01-29 14:30:00", ""},
                {"3", "Whole Milk", "1", "4.99", "home", "delivered", "2025-01-28 09:45:00", "123 Main St, City, Country"},
                {"4", "Chicken Breast", "2", "8.99", "home", "pending", "2025-01-31 11:20:00", "123 Main St, City, Country"},
                {"5", "Organic Lettuce", "1", "2.99", "pickup", "delivered", "2025-01-27 16:10:00", ""},
                {"6", "Greek Yogurt", "4", "1.99", "home", "approved", "2025-01-30 13:05:00", "456 Oak Ave, City, Country"}
            };

            for (String[] order : sampleOrders) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_ORDER_USER_ID, userId);
                values.put(COLUMN_ORDER_PRODUCT_ID, Integer.parseInt(order[0]));
                values.put(COLUMN_ORDER_PRODUCT_NAME, order[1]);
                values.put(COLUMN_ORDER_QUANTITY, Integer.parseInt(order[2]));
                values.put(COLUMN_ORDER_UNIT_PRICE, Double.parseDouble(order[3]));
                values.put(COLUMN_ORDER_TOTAL_PRICE, 
                    Integer.parseInt(order[2]) * Double.parseDouble(order[3]));
                values.put(COLUMN_ORDER_DELIVERY_METHOD, order[4]);
                values.put(COLUMN_ORDER_STATUS, order[5]);
                values.put(COLUMN_ORDER_DATE, order[6]);
                values.put(COLUMN_ORDER_DELIVERY_ADDRESS, order[7]);
                values.put(COLUMN_ORDER_SYNCED, "delivered".equals(order[5]) ? 1 : 0);
                db.insert(TABLE_ORDERS, null, values);
            }
        }
    }

    // Product methods
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        android.util.Log.d("DatabaseHelper", "=== GETTING ALL PRODUCTS ===");
        
        Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, COLUMN_PRODUCT_NAME);

        android.util.Log.d("DatabaseHelper", "Products cursor count: " + cursor.getCount());

        if (cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
                
                android.util.Log.d("DatabaseHelper", "Product: " + product.getName() + 
                    " | Image: " + product.getImageUrl() + 
                    " | Category: " + product.getCategory());
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        android.util.Log.d("DatabaseHelper", "=== END GETTING ALL PRODUCTS ===");
        return products;
    }

    public Product getProductById(int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_PRODUCT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(productId)};
        
        Cursor cursor = db.query(TABLE_PRODUCTS, null, selection, selectionArgs, null, null, null);
        
        Product product = null;
        if (cursor.moveToFirst()) {
            product = cursorToProduct(cursor);
        }
        cursor.close();
        return product;
    }

    public List<Product> searchProducts(String query) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_PRODUCT_NAME + " LIKE ?";
        String[] selectionArgs = {"%" + query + "%"};
        
        Cursor cursor = db.query(TABLE_PRODUCTS, null, selection, selectionArgs, null, null, COLUMN_PRODUCT_NAME);

        if (cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }

    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_PRODUCT_CATEGORY + " = ?";
        String[] selectionArgs = {category};
        
        Cursor cursor = db.query(TABLE_PRODUCTS, null, selection, selectionArgs, null, null, COLUMN_PRODUCT_NAME);

        if (cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }

    public List<Product> getProductsByPriceRange(double minPrice, double maxPrice) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_PRODUCT_PRICE + " BETWEEN ? AND ?";
        String[] selectionArgs = {String.valueOf(minPrice), String.valueOf(maxPrice)};
        
        Cursor cursor = db.query(TABLE_PRODUCTS, null, selection, selectionArgs, null, null, COLUMN_PRODUCT_NAME);

        if (cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT DISTINCT " + COLUMN_PRODUCT_CATEGORY + " FROM " + TABLE_PRODUCTS + " ORDER BY " + COLUMN_PRODUCT_CATEGORY;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    private Product cursorToProduct(Cursor cursor) {
        Product product = new Product();
        product.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)));
        product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)));
        product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_CATEGORY)));
        product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)));
        product.setStockQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_STOCK)));
        product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_IMAGE)));
        product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_DESCRIPTION)));
        product.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_AVAILABLE)) == 1);
        return product;
    }

    // Favorites methods
    public boolean addToFavorites(int userId, int productId) {
        // First check if it's already in favorites
        if (isProductFavorite(userId, productId)) {
            android.util.Log.d("DatabaseHelper", "Product " + productId + " is already in favorites for user " + userId);
            return false; // Already in favorites
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAVORITE_USER_ID, userId);
        values.put(COLUMN_FAVORITE_PRODUCT_ID, productId);
        
        long result = db.insert(TABLE_FAVORITES, null, values);
        boolean success = result != -1;
        
        android.util.Log.d("DatabaseHelper", "Adding to favorites - User: " + userId + 
            ", Product: " + productId + ", Success: " + success);
        
        return success;
    }

    public boolean removeFromFavorites(int userId, int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_FAVORITE_USER_ID + " = ? AND " + COLUMN_FAVORITE_PRODUCT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId), String.valueOf(productId)};
        
        int deletedRows = db.delete(TABLE_FAVORITES, selection, selectionArgs);
        return deletedRows > 0;
    }

    public boolean isProductFavorite(int userId, int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_FAVORITE_USER_ID + " = ? AND " + COLUMN_FAVORITE_PRODUCT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId), String.valueOf(productId)};
        
        Cursor cursor = db.query(TABLE_FAVORITES, null, selection, selectionArgs, null, null, null);
        boolean isFavorite = cursor.getCount() > 0;
        cursor.close();
        return isFavorite;
    }

    public List<Product> getFavoriteProducts(int userId) {
        List<Product> favoriteProducts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        android.util.Log.d("DatabaseHelper", "=== LOADING FAVORITE PRODUCTS FOR USER " + userId + " ===");
        
        String query = "SELECT p.* FROM " + TABLE_PRODUCTS + " p " +
                       "INNER JOIN " + TABLE_FAVORITES + " f ON p." + COLUMN_PRODUCT_ID + " = f." + COLUMN_FAVORITE_PRODUCT_ID +
                       " WHERE f." + COLUMN_FAVORITE_USER_ID + " = ? " +
                       "ORDER BY f." + COLUMN_FAVORITE_ID + " DESC"; // Show most recently added first
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        android.util.Log.d("DatabaseHelper", "Found " + cursor.getCount() + " favorite products");

        if (cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                product.setFavorite(true);
                
                android.util.Log.d("DatabaseHelper", "FAVORITE PRODUCT: " + product.getName() + 
                    " | ID: " + product.getId() + 
                    " | Image: " + product.getImageUrl() + 
                    " | Category: " + product.getCategory() + 
                    " | Price: " + product.getPrice());
                
                favoriteProducts.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        android.util.Log.d("DatabaseHelper", "=== END LOADING FAVORITES ===");
        return favoriteProducts;
    }

    // Order methods
    public long addOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // First check if there's enough stock
        Product product = getProductById(order.getProductId());
        if (product == null) {
            return -1; // Product not found
        }
        
        if (product.getStockQuantity() < order.getQuantity()) {
            return -2; // Insufficient stock
        }
        
        // Start transaction to ensure data consistency
        db.beginTransaction();
        try {
            // Add the order
            ContentValues values = new ContentValues();
            values.put(COLUMN_ORDER_USER_ID, order.getUserId());
            values.put(COLUMN_ORDER_PRODUCT_ID, order.getProductId());
            values.put(COLUMN_ORDER_PRODUCT_NAME, order.getProductName());
            values.put(COLUMN_ORDER_QUANTITY, order.getQuantity());
            values.put(COLUMN_ORDER_UNIT_PRICE, order.getUnitPrice());
            values.put(COLUMN_ORDER_TOTAL_PRICE, order.getTotalPrice());
            values.put(COLUMN_ORDER_DELIVERY_METHOD, order.getDeliveryMethod());
            values.put(COLUMN_ORDER_STATUS, order.getStatus());
            values.put(COLUMN_ORDER_DATE, order.getOrderDate());
            values.put(COLUMN_ORDER_DELIVERY_ADDRESS, order.getDeliveryAddress());
            values.put(COLUMN_ORDER_SYNCED, order.isSynced() ? 1 : 0);
            
            long orderId = db.insert(TABLE_ORDERS, null, values);
            
            if (orderId != -1) {
                // Update product stock
                int newStock = product.getStockQuantity() - order.getQuantity();
                ContentValues stockValues = new ContentValues();
                stockValues.put(COLUMN_PRODUCT_STOCK, newStock);
                stockValues.put(COLUMN_PRODUCT_AVAILABLE, newStock > 0 ? 1 : 0);
                
                int updated = db.update(TABLE_PRODUCTS, stockValues, 
                    COLUMN_PRODUCT_ID + " = ?", new String[]{String.valueOf(order.getProductId())});
                
                if (updated > 0) {
                    db.setTransactionSuccessful();
                    return orderId;
                } else {
                    return -3; // Failed to update stock
                }
            }
            
            return -4; // Failed to add order
        } finally {
            db.endTransaction();
        }
    }

    public List<Order> getUserOrders(int userId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_ORDER_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        Cursor cursor = db.query(TABLE_ORDERS, null, selection, selectionArgs, null, null, COLUMN_ORDER_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Order order = cursorToOrder(cursor);
                orders.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    private Order cursorToOrder(Cursor cursor) {
        Order order = new Order();
        order.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID)));
        order.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_USER_ID)));
        order.setProductId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_PRODUCT_ID)));
        order.setProductName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_PRODUCT_NAME)));
        order.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_QUANTITY)));
        order.setUnitPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ORDER_UNIT_PRICE)));
        order.setTotalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ORDER_TOTAL_PRICE)));
        order.setDeliveryMethod(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DELIVERY_METHOD)));
        order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_STATUS)));
        order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DATE)));
        order.setDeliveryAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DELIVERY_ADDRESS)));
        order.setSynced(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_SYNCED)) == 1);
        return order;
    }

    // Order status management methods
    public boolean updateOrderStatus(int orderId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // If cancelling an order, we need to restore the stock
        if ("cancelled".equals(newStatus)) {
            return cancelOrderAndRestoreStock(orderId, db);
        } else {
            // Regular status update
            ContentValues values = new ContentValues();
            values.put(COLUMN_ORDER_STATUS, newStatus);
            values.put(COLUMN_ORDER_SYNCED, 0); // Mark as unsynced since status changed
            
            int rowsAffected = db.update(TABLE_ORDERS, values, 
                COLUMN_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
            return rowsAffected > 0;
        }
    }

    private boolean cancelOrderAndRestoreStock(int orderId, SQLiteDatabase db) {
        // Start transaction to ensure data consistency
        db.beginTransaction();
        try {
            // First get the order details
            String selection = COLUMN_ORDER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(orderId)};
            
            Cursor cursor = db.query(TABLE_ORDERS, null, selection, selectionArgs, null, null, null);
            
            if (!cursor.moveToFirst()) {
                cursor.close();
                return false; // Order not found
            }
            
            Order order = cursorToOrder(cursor);
            cursor.close();
            
            // Check if order is already cancelled
            if ("cancelled".equals(order.getStatus())) {
                db.setTransactionSuccessful();
                return true; // Already cancelled, nothing to do
            }
            
            // Update order status to cancelled
            ContentValues orderValues = new ContentValues();
            orderValues.put(COLUMN_ORDER_STATUS, "cancelled");
            orderValues.put(COLUMN_ORDER_SYNCED, 0);
            
            int orderUpdated = db.update(TABLE_ORDERS, orderValues, 
                COLUMN_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
            
            if (orderUpdated > 0) {
                // Restore the stock for the product
                Product product = getProductById(order.getProductId());
                if (product != null) {
                    int newStock = product.getStockQuantity() + order.getQuantity();
                    ContentValues stockValues = new ContentValues();
                    stockValues.put(COLUMN_PRODUCT_STOCK, newStock);
                    stockValues.put(COLUMN_PRODUCT_AVAILABLE, 1); // Make available again
                    
                    db.update(TABLE_PRODUCTS, stockValues, 
                        COLUMN_PRODUCT_ID + " = ?", new String[]{String.valueOf(order.getProductId())});
                }
                
                db.setTransactionSuccessful();
                return true;
            }
            
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public boolean cancelOrder(int orderId) {
        return updateOrderStatus(orderId, "cancelled");
    }

    public List<Order> getUserOrdersByStatus(int userId, String status) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selection = COLUMN_ORDER_USER_ID + " = ? AND " + COLUMN_ORDER_STATUS + " = ?";
        String[] selectionArgs = {String.valueOf(userId), status};
        
        Cursor cursor = db.query(TABLE_ORDERS, null, selection, selectionArgs, 
            null, null, COLUMN_ORDER_DATE + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                orders.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    public int getOrderCountByStatus(int userId, String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_ORDER_USER_ID + " = ? AND " + COLUMN_ORDER_STATUS + " = ?";
        String[] selectionArgs = {String.valueOf(userId), status};
        
        Cursor cursor = db.query(TABLE_ORDERS, new String[]{"COUNT(*)"}, 
            selection, selectionArgs, null, null, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getTotalOrderCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_ORDER_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        Cursor cursor = db.query(TABLE_ORDERS, new String[]{"COUNT(*)"}, 
            selection, selectionArgs, null, null, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<Order> getUnsyncedOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selection = COLUMN_ORDER_SYNCED + " = ?";
        String[] selectionArgs = {"0"};
        
        Cursor cursor = db.query(TABLE_ORDERS, null, selection, selectionArgs, 
            null, null, COLUMN_ORDER_DATE + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                orders.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    public boolean markOrderAsSynced(int orderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_SYNCED, 1);
        
        int rowsAffected = db.update(TABLE_ORDERS, values, 
            COLUMN_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
        return rowsAffected > 0;
    }

    // Admin methods for dashboard
    public int getProductCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, new String[]{"COUNT(*)"}, 
            null, null, null, null, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getTotalOrdersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, new String[]{"COUNT(*)"}, 
            null, null, null, null, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getPendingOrdersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_ORDER_STATUS + " = ?";
        String[] selectionArgs = {"pending"};
        
        Cursor cursor = db.query(TABLE_ORDERS, new String[]{"COUNT(*)"}, 
            selection, selectionArgs, null, null, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_ORDERS, null, null, null, 
            null, null, COLUMN_ORDER_DATE + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                orders.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    public boolean deleteOrder(int orderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_ORDERS, 
            COLUMN_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
        return rowsAffected > 0;
    }

    public boolean deleteProduct(int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_PRODUCTS, 
            COLUMN_PRODUCT_ID + " = ?", new String[]{String.valueOf(productId)});
        return rowsAffected > 0;
    }

    public long addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, product.getName());
        values.put(COLUMN_PRODUCT_CATEGORY, product.getCategory());
        values.put(COLUMN_PRODUCT_PRICE, product.getPrice());
        values.put(COLUMN_PRODUCT_STOCK, product.getStockQuantity());
        values.put(COLUMN_PRODUCT_IMAGE, product.getImageUrl());
        values.put(COLUMN_PRODUCT_DESCRIPTION, product.getDescription());
        values.put(COLUMN_PRODUCT_AVAILABLE, product.isAvailable() ? 1 : 0);
        
        return db.insert(TABLE_PRODUCTS, null, values);
    }

    public boolean updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, product.getName());
        values.put(COLUMN_PRODUCT_CATEGORY, product.getCategory());
        values.put(COLUMN_PRODUCT_PRICE, product.getPrice());
        values.put(COLUMN_PRODUCT_STOCK, product.getStockQuantity());
        values.put(COLUMN_PRODUCT_IMAGE, product.getImageUrl());
        values.put(COLUMN_PRODUCT_DESCRIPTION, product.getDescription());
        values.put(COLUMN_PRODUCT_AVAILABLE, product.isAvailable() ? 1 : 0);
        
        int rowsAffected = db.update(TABLE_PRODUCTS, values, 
            COLUMN_PRODUCT_ID + " = ?", new String[]{String.valueOf(product.getId())});
        return rowsAffected > 0;
    }

    // Offer CRUD operations
    public long addOffer(com.example.grocerystore.models.Offer offer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        android.util.Log.d("DatabaseHelper", "=== ADDING OFFER TO DATABASE ===");
        android.util.Log.d("DatabaseHelper", "Product ID: " + offer.getProductId());
        android.util.Log.d("DatabaseHelper", "Product Name: " + offer.getProductName());
        android.util.Log.d("DatabaseHelper", "Category: " + offer.getCategory());
        android.util.Log.d("DatabaseHelper", "Original Price: " + offer.getOriginalPrice());
        android.util.Log.d("DatabaseHelper", "Discounted Price: " + offer.getDiscountedPrice());
        android.util.Log.d("DatabaseHelper", "Stock: " + offer.getStockQuantity());
        android.util.Log.d("DatabaseHelper", "Active: " + offer.isActive());
        android.util.Log.d("DatabaseHelper", "Created At: " + offer.getCreatedAt());
        android.util.Log.d("DatabaseHelper", "Expires At: " + offer.getExpiresAt());
        
        values.put(COLUMN_OFFER_PRODUCT_ID, offer.getProductId());
        values.put(COLUMN_OFFER_PRODUCT_NAME, offer.getProductName());
        values.put(COLUMN_OFFER_CATEGORY, offer.getCategory());
        values.put(COLUMN_OFFER_ORIGINAL_PRICE, offer.getOriginalPrice());
        values.put(COLUMN_OFFER_DISCOUNTED_PRICE, offer.getDiscountedPrice());
        values.put(COLUMN_OFFER_STOCK_QUANTITY, offer.getStockQuantity());
        values.put(COLUMN_OFFER_DESCRIPTION, offer.getDescription());
        values.put(COLUMN_OFFER_IMAGE_URL, offer.getImageUrl());
        values.put(COLUMN_OFFER_IS_ACTIVE, offer.isActive() ? 1 : 0);
        values.put(COLUMN_OFFER_CREATED_AT, offer.getCreatedAt());
        values.put(COLUMN_OFFER_EXPIRES_AT, offer.getExpiresAt());
        
        long result = db.insert(TABLE_OFFERS, null, values);
        android.util.Log.d("DatabaseHelper", "Offer insertion result: " + result);
        android.util.Log.d("DatabaseHelper", "=== END ADDING OFFER ===");
        
        return result;
    }

    public List<com.example.grocerystore.models.Offer> getAllOffers() {
        List<com.example.grocerystore.models.Offer> offers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        android.util.Log.d("DatabaseHelper", "=== QUERYING ALL OFFERS ===");
        
        Cursor cursor = db.query(TABLE_OFFERS, null, null, null, null, null, 
            COLUMN_OFFER_CREATED_AT + " DESC");
        
        android.util.Log.d("DatabaseHelper", "Cursor count: " + cursor.getCount());
        
        if (cursor.moveToFirst()) {
            do {
                com.example.grocerystore.models.Offer offer = new com.example.grocerystore.models.Offer();
                offer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OFFER_ID)));
                
                // Get product_id with safe handling for null values
                int productIdIndex = cursor.getColumnIndex(COLUMN_OFFER_PRODUCT_ID);
                if (productIdIndex != -1 && !cursor.isNull(productIdIndex)) {
                    offer.setProductId(cursor.getInt(productIdIndex));
                } else {
                    offer.setProductId(0); // Default to 0 if no product_id
                }
                
                String productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_PRODUCT_NAME));
                offer.setProductName(productName);
                offer.setName(productName); // Set both name and productName to the same value
                
                offer.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_CATEGORY)));
                offer.setOriginalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_OFFER_ORIGINAL_PRICE)));
                offer.setDiscountedPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_OFFER_DISCOUNTED_PRICE)));
                offer.setStockQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OFFER_STOCK_QUANTITY)));
                offer.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_DESCRIPTION)));
                offer.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_IMAGE_URL)));
                offer.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OFFER_IS_ACTIVE)) == 1);
                offer.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OFFER_CREATED_AT)));
                offer.setExpiresAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OFFER_EXPIRES_AT)));
                
                // Calculate discount percentage
                if (offer.getOriginalPrice() > 0) {
                    int discountPercent = (int) Math.round(((offer.getOriginalPrice() - offer.getDiscountedPrice()) / offer.getOriginalPrice()) * 100);
                    offer.setDiscountPercent(discountPercent);
                }
                
                android.util.Log.d("DatabaseHelper", "Retrieved offer: " + offer.getProductName() + 
                    " | Name: " + offer.getName() +
                    " | Product ID: " + offer.getProductId() +
                    " | Active: " + offer.isActive() + 
                    " | Discount: " + offer.getDiscountPercent() + "%" +
                    " | Expires: " + offer.getExpiresAt());
                
                offers.add(offer);
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        android.util.Log.d("DatabaseHelper", "Total offers retrieved: " + offers.size());
        android.util.Log.d("DatabaseHelper", "=== END QUERYING OFFERS ===");
        
        return offers;
    }

    public List<com.example.grocerystore.models.Offer> getActiveOffers() {
        List<com.example.grocerystore.models.Offer> offers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selection = COLUMN_OFFER_IS_ACTIVE + " = ?";
        String[] selectionArgs = {"1"};
        
        Cursor cursor = db.query(TABLE_OFFERS, null, selection, selectionArgs, null, null, 
            COLUMN_OFFER_CREATED_AT + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                com.example.grocerystore.models.Offer offer = new com.example.grocerystore.models.Offer();
                offer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OFFER_ID)));
                offer.setProductName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_PRODUCT_NAME)));
                offer.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_CATEGORY)));
                offer.setOriginalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_OFFER_ORIGINAL_PRICE)));
                offer.setDiscountedPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_OFFER_DISCOUNTED_PRICE)));
                offer.setStockQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OFFER_STOCK_QUANTITY)));
                offer.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_DESCRIPTION)));
                offer.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_IMAGE_URL)));
                offer.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OFFER_IS_ACTIVE)) == 1);
                offer.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OFFER_CREATED_AT)));
                offer.setExpiresAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OFFER_EXPIRES_AT)));
                offers.add(offer);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return offers;
    }

    public boolean updateOffer(com.example.grocerystore.models.Offer offer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_OFFER_PRODUCT_NAME, offer.getProductName());
        values.put(COLUMN_OFFER_CATEGORY, offer.getCategory());
        values.put(COLUMN_OFFER_ORIGINAL_PRICE, offer.getOriginalPrice());
        values.put(COLUMN_OFFER_DISCOUNTED_PRICE, offer.getDiscountedPrice());
        values.put(COLUMN_OFFER_STOCK_QUANTITY, offer.getStockQuantity());
        values.put(COLUMN_OFFER_DESCRIPTION, offer.getDescription());
        values.put(COLUMN_OFFER_IMAGE_URL, offer.getImageUrl());
        values.put(COLUMN_OFFER_IS_ACTIVE, offer.isActive() ? 1 : 0);
        values.put(COLUMN_OFFER_EXPIRES_AT, offer.getExpiresAt());
        
        int rowsAffected = db.update(TABLE_OFFERS, values, 
            COLUMN_OFFER_ID + " = ?", new String[]{String.valueOf(offer.getId())});
        return rowsAffected > 0;
    }

    public boolean deleteOffer(int offerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_OFFERS, 
            COLUMN_OFFER_ID + " = ?", new String[]{String.valueOf(offerId)});
        return rowsAffected > 0;
    }

    public int getActiveOffersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_OFFER_IS_ACTIVE + " = ?";
        String[] selectionArgs = {"1"};
        
        Cursor cursor = db.query(TABLE_OFFERS, new String[]{"COUNT(*)"}, 
            selection, selectionArgs, null, null, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    public void initializeRealProductsIfEmpty() {
        List<Product> existingProducts = getAllProducts();
        if (existingProducts.isEmpty()) {
            Log.d("DatabaseHelper", "Database is empty, adding real products with images");
            addRealProductsWithImages();
        } else {
            Log.d("DatabaseHelper", "Products already exist: " + existingProducts.size() + " products found");
            // Check if products are using old URLs - if so, update them
            boolean needsUpdate = false;
            for (Product product : existingProducts) {
                if (product.getImageUrl() != null && product.getImageUrl().startsWith("https://")) {
                    needsUpdate = true;
                    break;
                }
            }
            if (needsUpdate) {
                Log.d("DatabaseHelper", "Products have old URLs, updating to local images");
                clearAndReinitializeProducts();
            }
        }
    }
    
    public void clearAndReinitializeProducts() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PRODUCTS);
        Log.d("DatabaseHelper", "Cleared existing products, adding new ones with local images");
        addRealProductsWithImages();
    }
    
    // For testing - force reset all data
    public void forceResetForTesting() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PRODUCTS);
        db.execSQL("DELETE FROM " + TABLE_OFFERS);
        Log.d("DatabaseHelper", "FORCE RESET: Cleared all products and offers for testing");
        addRealProductsWithImages();
    }
    
    private void addRealProductsWithImages() {
        addRealProductsWithImages(this.getWritableDatabase());
    }
    
    private void addRealProductsWithImages(SQLiteDatabase db) {
        // Fruits (only using JPGs you have)
        insertProduct(db, 0, "Fresh Bananas", "Fruits", 2.99, 50, 
            "drawable://product_bananas", "Fresh yellow bananas, rich in potassium", true);
        insertProduct(db, 0, "Red Apples", "Fruits", 3.49, 40, 
            "drawable://product_apples", "Fresh red apples, perfect for snacking", true);
        insertProduct(db, 0, "Strawberries", "Fruits", 5.99, 25, 
            "drawable://product_strawberries", "Sweet fresh strawberries", true);
        insertProduct(db, 0, "Avocados", "Fruits", 6.99, 30, 
            "drawable://product_avocados", "Ripe avocados, perfect for guacamole", true);
        
        // Vegetables
        insertProduct(db, 0, "Fresh Carrots", "Vegetables", 2.49, 60, 
            "drawable://product_carrots", "Crisp fresh carrots", true);
        insertProduct(db, 0, "Broccoli", "Vegetables", 3.99, 45, 
            "drawable://product_broccoli", "Fresh green broccoli crowns", true);
        insertProduct(db, 0, "Bell Peppers", "Vegetables", 4.49, 35, 
            "drawable://product_bell_peppers", "Colorful bell peppers", true);
        insertProduct(db, 0, "Spinach Leaves", "Vegetables", 3.29, 40, 
            "drawable://product_spinach", "Fresh spinach leaves", true);
        insertProduct(db, 0, "Tomatoes", "Vegetables", 3.79, 55, 
            "drawable://product_tomatoes", "Ripe red tomatoes", true);
        
        // Dairy
        insertProduct(db, 0, "Fresh Milk", "Dairy", 4.99, 20, 
            "drawable://product_milk", "Fresh whole milk, 1 gallon", true);
        insertProduct(db, 0, "Greek Yogurt", "Dairy", 5.49, 25, 
            "drawable://product_yogurt", "Plain Greek yogurt, 32 oz", true);
        insertProduct(db, 0, "Cheddar Cheese", "Dairy", 7.99, 15, 
            "drawable://product_cheese", "Sharp cheddar cheese", true);
        insertProduct(db, 0, "Fresh Eggs", "Dairy", 3.99, 30, 
            "drawable://product_eggs", "Grade A large eggs, dozen", true);
        
        // Meat & Seafood
        insertProduct(db, 0, "Chicken Breast", "Meat", 12.99, 20, 
            "drawable://product_chicken", "Boneless chicken breast, per pound", true);
        insertProduct(db, 0, "Ground Beef", "Meat", 8.99, 18, 
            "drawable://product_beef", "Lean ground beef", true);
        insertProduct(db, 0, "Salmon Fillet", "Meat", 15.99, 12, 
            "drawable://product_salmon", "Fresh Atlantic salmon fillet", true);
        
        // Bakery
        insertProduct(db, 0, "Bagels", "Bakery", 3.99, 20, 
            "drawable://product_bagels", "Fresh baked bagels", true);
        
        // Beverages
        insertProduct(db, 0, "Orange Juice", "Beverages", 4.49, 30, 
            "drawable://product_orange_juice", "Fresh squeezed orange juice", true);
        insertProduct(db, 0, "Coffee Beans", "Beverages", 12.99, 20, 
            "drawable://product_coffee", "Premium coffee beans", true);
        insertProduct(db, 0, "Green Tea", "Beverages", 8.99, 25, 
            "drawable://product_tea", "Organic green tea", true);
        
        // Snacks
        insertProduct(db, 0, "Mixed Nuts", "Snacks", 9.99, 22, 
            "drawable://product_nuts", "Assorted mixed nuts", true);
        insertProduct(db, 0, "Dark Chocolate", "Snacks", 5.99, 30, 
            "drawable://product_chocolate", "Rich dark chocolate bar", true);
        insertProduct(db, 0, "Granola Bars", "Snacks", 6.49, 35, 
            "drawable://product_granola", "Healthy granola bars", true);
        
        Log.d("DatabaseHelper", "Added 23 real products with JPG images from drawable folder");
    }
    
    // Method to create test offers based on existing products
    public void createTestOffers() {
        android.util.Log.d("DatabaseHelper", "=== CREATING TEST OFFERS ===");
        
        // Get some existing products
        List<Product> products = getAllProducts();
        
        if (products.size() < 3) {
            android.util.Log.e("DatabaseHelper", "Not enough products to create test offers");
            return;
        }
        
        // Create 3 test offers from existing products
        for (int i = 0; i < Math.min(3, products.size()); i++) {
            Product product = products.get(i);
            
            Offer offer = new Offer();
            offer.setProductName(product.getName());
            offer.setCategory(product.getCategory());
            offer.setOriginalPrice(product.getPrice());
            offer.setDiscountedPrice(product.getPrice() * 0.8); // 20% discount
            offer.setStockQuantity(product.getStockQuantity());
            offer.setDescription("Special offer on " + product.getName());
            offer.setImageUrl(product.getImageUrl()); // Use same image as product
            offer.setActive(true);
            offer.setCreatedAt(System.currentTimeMillis());
            offer.setExpiresAt(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)); // 30 days
            
            long result = addOffer(offer);
            android.util.Log.d("DatabaseHelper", "Created test offer for " + product.getName() + 
                " with image: " + product.getImageUrl() + " (result: " + result + ")");
        }
        
        android.util.Log.d("DatabaseHelper", "=== END CREATING TEST OFFERS ===");
    }
    
    private void insertProduct(SQLiteDatabase db, int id, String name, String category, 
                              double price, int stock, String imageUrl, String description, boolean available) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);
        values.put(COLUMN_PRODUCT_CATEGORY, category);
        values.put(COLUMN_PRODUCT_PRICE, price);
        values.put(COLUMN_PRODUCT_STOCK, stock);
        values.put(COLUMN_PRODUCT_IMAGE, imageUrl);
        values.put(COLUMN_PRODUCT_DESCRIPTION, description);
        values.put(COLUMN_PRODUCT_AVAILABLE, available ? 1 : 0);
        db.insert(TABLE_PRODUCTS, null, values);
    }
}
