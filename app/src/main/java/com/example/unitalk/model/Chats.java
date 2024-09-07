package com.example.unitalk.model;

public class Chats {

    // Corrected spelling of 'receiver'
    private String sender, receiver, message;
    private boolean isSeen;

    // Constructor with all fields
    public Chats(String sender, String receiver, String message, boolean isSeen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isSeen = isSeen;
    }

    // Default constructor required for Firebase
    public Chats() {
    }

    // Getter and setter methods
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {  // Corrected spelling here
        return receiver;
    }

    public void setReceiver(String receiver) {  // Corrected spelling here
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {  // Corrected getter name to match convention
        return isSeen;
    }

    public void setSeen(boolean isSeen) {  // Corrected setter name to match convention
        this.isSeen = isSeen;
    }
}
