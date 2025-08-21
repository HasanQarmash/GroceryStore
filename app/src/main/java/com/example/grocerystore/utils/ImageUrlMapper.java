package com.example.grocerystore.utils;

/**
 * Utility class for handling image URLs from the API
 * Since the API returns example.com URLs, we map them to actual drawable resources
 */
public class ImageUrlMapper {

    /**
     * Maps API image URLs to local drawable resource names
     * @param imageUrl The image URL from the API
     * @return The drawable resource name (without @drawable/ prefix)
     */
    public static String mapImageUrlToDrawable(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "ic_product_placeholder";
        }
        
        // Extract filename from URL and map to local resources
        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        
        switch (filename.toLowerCase()) {
            case "apple.png":
                return "product_apples";
            case "banana.png":
                return "product_bananas";
            case "orange.png":
                return "product_oranges";
            case "tomato.png":
                return "product_tomatoes";
            case "cucumber.png":
                return "product_cucumbers";
            case "potato.png":
                return "product_potatoes";
            case "milk.png":
                return "product_milk";
            case "cheese.png":
                return "product_cheese";
            case "bread.png":
            case "whitebread.png":
                return "product_bagels";
            default:
                // Return a default placeholder if no mapping found
                return "ic_product_placeholder";
        }
    }
    
    /**
     * Gets the full drawable resource identifier
     * @param imageUrl The image URL from the API
     * @return The full drawable resource identifier
     */
    public static String getDrawableResource(String imageUrl) {
        return "@drawable/" + mapImageUrlToDrawable(imageUrl);
    }
}
