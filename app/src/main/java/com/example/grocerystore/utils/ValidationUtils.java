package com.example.grocerystore.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    // Password validation pattern (at least 5 chars, 1 letter, 1 number, 1 special char)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{5,}$"
    );
    
    // Phone number pattern for Palestinian numbers
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[0-9]{9}$" // 9 digits after country code
    );

    /**
     * Validate email format
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate password strength
     * @param password The password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validate name (minimum 3 characters)
     * @param name The name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 3;
    }

    /**
     * Validate phone number
     * @param phone The phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Check if two passwords match
     * @param password The first password
     * @param confirmPassword The second password
     * @return true if they match, false otherwise
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Check if string is not empty
     * @param text The text to check
     * @return true if not empty, false if null or empty
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Get country code by city
     * @param city The selected city
     * @return Country code string
     */
    public static String getCountryCodeByCity(String city) {
        if (city == null) return "+970";
        
        switch (city.toLowerCase()) {
            case "ramallah":
            case "jerusalem":
            case "nablus":
                return "+970"; // Palestinian territories area code
            case "gaza":
                return "+970"; // Gaza Strip uses same code
            default:
                return "+970"; // Default Palestinian code
        }
    }
}
