package com.example.unitalk.model;

import java.io.Serializable; // Import Serializable interface

public class NotificationModel implements Serializable { // Implement Serializable
    private String senderId;
    private String senderName; // This should match the Firestore field exactly
    private String messageText;
    private long timestamp;
    private String documentId;
    private int notificationCount; // For counting grouped notifications

    private String type;
    // Default constructor required for Firestore

    public NotificationModel() {
        this.senderId = "";
        this.senderName = "";
        this.messageText = "";
        this.timestamp = 0L;
        this.documentId = "";
        this.notificationCount = 0;
        this.type = "";
    }

    public NotificationModel(String senderId, String senderName, String messageText, long timestamp,
                             String documentId, int notificationCount, String type) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.documentId = documentId;
        this.notificationCount = notificationCount;
        this.type = type;
    }

    public NotificationModel(String senderName, long timestamp, String documentId) {
        this.senderName = senderName;
        this.timestamp = timestamp;
        this.documentId = documentId;
    }

    // Getters and Setters
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
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

    public int getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
