package com.example.unitalk.model;

public class ChatMessage {
    private String senderId;
    private String receiverId;
    private String messageText;
    private long timestamp;
    private String imageUrl;

    // Default constructor required for Firestore
    public ChatMessage() {}



    // Getters and setters
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() { return imageUrl; }  // Getter for imageUrl
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
