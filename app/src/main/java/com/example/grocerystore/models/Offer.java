package com.example.grocerystore.models;

import java.io.Serializable;

public class Offer implements Serializable {
    private int id;
    private int productId;
    private String name;
    private String productName;
    private String description;
    private double originalPrice;
    private double discountedPrice;
    private int discountPercent;
    private String imagePath;
    private String imageUrl;
    private String category;
    private String offerTitle;
    private String offerDescription;
    private String validFrom;
    private String validTo;
    private boolean isActive;
    private int stockQuantity;
    private long createdAt;
    private long expiresAt;

    // Default constructor
    public Offer() {}

    // Full constructor
    public Offer(int productId, String name, String description, double originalPrice, 
                 double discountedPrice, int discountPercent, String imagePath, String category,
                 String offerTitle, String offerDescription, String validFrom, String validTo, boolean isActive) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.discountPercent = discountPercent;
        this.imagePath = imagePath;
        this.category = category;
        this.offerTitle = offerTitle;
        this.offerDescription = offerDescription;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.isActive = isActive;
    }

    // Getters
    public int getProductId() { return productId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getOriginalPrice() { return originalPrice; }
    public double getDiscountedPrice() { return discountedPrice; }
    public int getDiscountPercent() { return discountPercent; }
    public String getImagePath() { return imagePath; }
    public String getCategory() { return category; }
    public String getOfferTitle() { return offerTitle; }
    public String getOfferDescription() { return offerDescription; }
    public String getValidFrom() { return validFrom; }
    public String getValidTo() { return validTo; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setProductId(int productId) { this.productId = productId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
    public void setDiscountedPrice(double discountedPrice) { this.discountedPrice = discountedPrice; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setCategory(String category) { this.category = category; }
    public void setOfferTitle(String offerTitle) { this.offerTitle = offerTitle; }
    public void setOfferDescription(String offerDescription) { this.offerDescription = offerDescription; }
    public void setValidFrom(String validFrom) { this.validFrom = validFrom; }
    public void setValidTo(String validTo) { this.validTo = validTo; }
    public void setActive(boolean active) { isActive = active; }

    // Additional getters and setters for database compatibility
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getProductName() { return productName != null ? productName : name; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getImageUrl() { return imageUrl != null ? imageUrl : imagePath; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    // Calculate savings amount
    public double getSavingsAmount() {
        return originalPrice - discountedPrice;
    }

    // Calculate discount percentage
    public double getDiscountPercentage() {
        if (originalPrice > 0) {
            return ((originalPrice - discountedPrice) / originalPrice) * 100;
        }
        return 0;
    }

    // Check if offer is valid for a given date
    public boolean isValidForDate(String date) {
        // Simple string comparison for demo (in real app, would use proper date parsing)
        return date.compareTo(validFrom) >= 0 && date.compareTo(validTo) <= 0;
    }

    @Override
    public String toString() {
        return "Offer{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", originalPrice=" + originalPrice +
                ", discountedPrice=" + discountedPrice +
                ", discountPercent=" + discountPercent +
                ", category='" + category + '\'' +
                ", offerTitle='" + offerTitle + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
