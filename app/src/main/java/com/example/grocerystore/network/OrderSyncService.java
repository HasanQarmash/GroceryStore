package com.example.grocerystore.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.example.grocerystore.database.DatabaseHelper;
import com.example.grocerystore.models.Order;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class OrderSyncService {
    private static final String TAG = "OrderSyncService";
    private static final String BASE_URL = "https://api.grocerystore.com"; // Replace with actual API URL
    private static final String SYNC_ORDERS_ENDPOINT = "/api/orders/sync";
    private static final String UPDATE_ORDER_STATUS_ENDPOINT = "/api/orders/update-status";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private OnSyncCompleteListener syncListener;

    public interface OnSyncCompleteListener {
        void onSyncStarted();
        void onSyncCompleted(boolean success, int syncedCount);
        void onSyncError(String error);
    }

    public OrderSyncService(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
    }

    public void setSyncListener(OnSyncCompleteListener listener) {
        this.syncListener = listener;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void syncOrders() {
        if (!isNetworkAvailable()) {
            if (syncListener != null) {
                syncListener.onSyncError("No internet connection available");
            }
            return;
        }

        new SyncOrdersTask().execute();
    }

    private class SyncOrdersTask extends AsyncTask<Void, Void, SyncResult> {
        @Override
        protected void onPreExecute() {
            if (syncListener != null) {
                syncListener.onSyncStarted();
            }
        }

        @Override
        protected SyncResult doInBackground(Void... voids) {
            try {
                // Get unsynced orders from local database
                List<Order> unsyncedOrders = databaseHelper.getUnsyncedOrders();
                
                if (unsyncedOrders.isEmpty()) {
                    return new SyncResult(true, 0, "No orders to sync");
                }

                // Sync orders with server
                int syncedCount = 0;
                for (Order order : unsyncedOrders) {
                    if (syncOrderToServer(order)) {
                        databaseHelper.markOrderAsSynced(order.getId());
                        syncedCount++;
                    }
                }

                // Fetch updated order statuses from server
                fetchOrderUpdatesFromServer();

                return new SyncResult(true, syncedCount, "Sync completed successfully");
                
            } catch (Exception e) {
                Log.e(TAG, "Error during sync", e);
                return new SyncResult(false, 0, "Sync failed: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(SyncResult result) {
            if (syncListener != null) {
                if (result.success) {
                    syncListener.onSyncCompleted(true, result.syncedCount);
                } else {
                    syncListener.onSyncError(result.message);
                }
            }
        }
    }

    private boolean syncOrderToServer(Order order) {
        try {
            // Convert order to JSON
            JSONObject orderJson = orderToJson(order);
            
            // Send to server
            URL url = new URL(BASE_URL + SYNC_ORDERS_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Write JSON to request body
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(orderJson.toString());
            writer.flush();
            writer.close();
            os.close();

            // Check response
            int responseCode = conn.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED;

        } catch (Exception e) {
            Log.e(TAG, "Error syncing order to server", e);
            return false;
        }
    }

    private void fetchOrderUpdatesFromServer() {
        // This is a simplified version - in a real app, you'd implement proper API calls
        // to fetch order status updates from the server and update local database
        
        try {
            // Simulate server response with random status updates
            simulateServerUpdates();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching updates from server", e);
        }
    }

    private void simulateServerUpdates() {
        // Simulate some order status updates for demonstration
        // In a real app, this would be actual API calls
        
        // For now, randomly update some pending orders to approved/delivered
        // This is just for demonstration purposes
        
        Log.d(TAG, "Simulating server updates (demo mode)");
        
        // You would implement actual server communication here
        // Example:
        // 1. Send request to get order updates
        // 2. Parse server response
        // 3. Update local database with new statuses
        // 4. Mark updated orders as synced
    }

    private JSONObject orderToJson(Order order) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", order.getId());
        json.put("userId", order.getUserId());
        json.put("productId", order.getProductId());
        json.put("productName", order.getProductName());
        json.put("quantity", order.getQuantity());
        json.put("unitPrice", order.getUnitPrice());
        json.put("totalPrice", order.getTotalPrice());
        json.put("deliveryMethod", order.getDeliveryMethod());
        json.put("status", order.getStatus());
        json.put("orderDate", order.getOrderDate());
        json.put("deliveryAddress", order.getDeliveryAddress());
        return json;
    }

    // Public method to update order status (with server sync)
    public void updateOrderStatus(int orderId, String newStatus, OnOrderUpdateListener listener) {
        new UpdateOrderStatusTask(orderId, newStatus, listener).execute();
    }

    public interface OnOrderUpdateListener {
        void onUpdateCompleted(boolean success, String message);
    }

    private class UpdateOrderStatusTask extends AsyncTask<Void, Void, Boolean> {
        private int orderId;
        private String newStatus;
        private OnOrderUpdateListener listener;
        private String errorMessage;

        public UpdateOrderStatusTask(int orderId, String newStatus, OnOrderUpdateListener listener) {
            this.orderId = orderId;
            this.newStatus = newStatus;
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Update local database first
                boolean localUpdateSuccess = databaseHelper.updateOrderStatus(orderId, newStatus);
                
                if (!localUpdateSuccess) {
                    errorMessage = "Failed to update local database";
                    return false;
                }

                // If online, try to sync with server
                if (isNetworkAvailable()) {
                    boolean serverSyncSuccess = syncStatusUpdateToServer(orderId, newStatus);
                    // Even if server sync fails, local update succeeded, so return true
                    if (!serverSyncSuccess) {
                        Log.w(TAG, "Server sync failed but local update succeeded for order " + orderId);
                    }
                    return true; // Local update succeeded, that's what matters for user experience
                } else {
                    // No internet, but local update succeeded
                    return true;
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error updating order status", e);
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (listener != null) {
                if (success) {
                    listener.onUpdateCompleted(true, "Order status updated successfully");
                } else {
                    listener.onUpdateCompleted(false, errorMessage != null ? errorMessage : "Update failed");
                }
            }
        }
    }

    private boolean syncStatusUpdateToServer(int orderId, String newStatus) {
        try {
            JSONObject updateData = new JSONObject();
            updateData.put("orderId", orderId);
            updateData.put("status", newStatus);
            
            URL url = new URL(BASE_URL + UPDATE_ORDER_STATUS_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(updateData.toString());
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            boolean success = responseCode == HttpURLConnection.HTTP_OK;
            
            if (success) {
                // Mark as synced in local database
                databaseHelper.markOrderAsSynced(orderId);
            }
            
            return success;

        } catch (Exception e) {
            Log.e(TAG, "Error syncing status update to server", e);
            return false;
        }
    }

    private static class SyncResult {
        boolean success;
        int syncedCount;
        String message;

        SyncResult(boolean success, int syncedCount, String message) {
            this.success = success;
            this.syncedCount = syncedCount;
            this.message = message;
        }
    }
}
