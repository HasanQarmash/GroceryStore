package com.example.grocerystore.model;

public class ApiProduct {
    private int id;
    private String category;
    private String name;
    private double price;
    private int stock;
    private String image_url;
    private boolean offer;

    // Constructors
    public ApiProduct() {}

    public ApiProduct(int id, String category, String name, double price, int stock, String image_url, boolean offer) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.image_url = image_url;
        this.offer = offer;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getImage_url() {
        return image_url;
    }

    public boolean isOffer() {
        return offer;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setOffer(boolean offer) {
        this.offer = offer;
    }

    @Override
    public String toString() {
        return "ApiProduct{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", image_url='" + image_url + '\'' +
                ", offer=" + offer +
                '}';
    }
}
