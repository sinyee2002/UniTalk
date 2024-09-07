package com.example.unitalk;

public class User {
    private String username;
    private String email;

    // No-argument constructor required by Firestore
    public User() {
    }

    // Constructor with arguments
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
