// Updated LostAndFoundItem.java
package com.example.unitalk.model;

public class LostAndFoundItem {
    private String title;
    private String description;
    private String location;
    private String imageUrl;
    private String contactInfo;
    private int commentCount;
    private String username; // New field to store the username of the item creator



    // Existing default constructor
    public LostAndFoundItem() {}

    // Constructor with all fields, including username
    public LostAndFoundItem(String title, String description, String location, String imageUrl, int commentCount, String username) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.imageUrl = imageUrl;
        this.commentCount = commentCount;
        this.username = username;
    }

    // Getters and setters for all fields
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
