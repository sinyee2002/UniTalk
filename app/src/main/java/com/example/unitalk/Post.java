package com.example.unitalk;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private String id; // Required for identifying posts
    private String username;
    private String description;
    private String profileImageUrl;
    private String imageUrl;
    private int likeCount;
    private List<String> comments;
    private String userId;

    // Default constructor (required for Firestore)
    public Post() {
        comments = new ArrayList<>();
    }

    // Constructor with all fields
    public Post(String id, String username, String description, String profileImageUrl, String imageUrl, int likeCount, List<String> comments) {
        this.id = id;
        this.username = username;
        this.description = description;
        this.profileImageUrl = profileImageUrl;
        this.imageUrl = imageUrl;
        this.likeCount = likeCount;
        this.comments = comments != null ? comments : new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public void addComment(String comment) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        comments.add(comment);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
