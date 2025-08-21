package com.example.grocerystore.utils;

public class PasswordEncryption {
    private static final int CAESAR_SHIFT = 7; // Caesar cipher shift value
    
    // WARNING: This is a simple Caesar cipher implementation for educational purposes only.
    // For production apps, use proper encryption libraries like BCrypt or Android Keystore.

    /**
     * Encrypt password using Caesar cipher
     * NOTE: This is for educational demonstration only, not production-ready
     * @param password The plain text password
     * @return Encrypted password
     */
    public static String encryptPassword(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }

        StringBuilder encrypted = new StringBuilder();
        
        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            
            // Encrypt alphabetic characters
            if (Character.isLetter(ch)) {
                char base = Character.isLowerCase(ch) ? 'a' : 'A';
                ch = (char) ((ch - base + CAESAR_SHIFT) % 26 + base);
            }
            // Encrypt digits
            else if (Character.isDigit(ch)) {
                ch = (char) ((ch - '0' + CAESAR_SHIFT) % 10 + '0');
            }
            // Special characters remain unchanged for simplicity
            
            encrypted.append(ch);
        }
        
        return encrypted.toString();
    }

    /**
     * Decrypt password using Caesar cipher
     * @param encryptedPassword The encrypted password
     * @return Decrypted password
     */
    public static String decryptPassword(String encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            return encryptedPassword;
        }

        StringBuilder decrypted = new StringBuilder();
        
        for (int i = 0; i < encryptedPassword.length(); i++) {
            char ch = encryptedPassword.charAt(i);
            
            // Decrypt alphabetic characters
            if (Character.isLetter(ch)) {
                char base = Character.isLowerCase(ch) ? 'a' : 'A';
                ch = (char) ((ch - base - CAESAR_SHIFT + 26) % 26 + base);
            }
            // Decrypt digits
            else if (Character.isDigit(ch)) {
                ch = (char) ((ch - '0' - CAESAR_SHIFT + 10) % 10 + '0');
            }
            // Special characters remain unchanged
            
            decrypted.append(ch);
        }
        
        return decrypted.toString();
    }
}
