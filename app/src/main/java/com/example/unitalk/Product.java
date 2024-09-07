package com.example.unitalk;

public class Product {
    private String name;
    private double price;  // Ensure this is a double
    private String description;
    private String imageUrl;
    private String username;


    private double sellerLat; // Add seller latitude
    private double sellerLng; // Add seller longitude
    // Default constructor required for calls to DataSnapshot.getValue(Product.class)
    public Product() {
    }

    public Product(String name, double price, String description, String imageUrl, String username, double sellerLat, double sellerLng) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.username = username;
        this.sellerLat = sellerLat; // Set seller latitude
        this.sellerLng = sellerLng; // Set seller longitude
    }

    // Add getters for latitude and longitude
    public double getSellerLat() {
        return sellerLat;
    }

    public double getSellerLng() {
        return sellerLng;
    }
    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

