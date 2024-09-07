package com.example.unitalk.model;

import java.util.List;

public class ChatRoom {
    private String roomId;
    private String roomName;
    private String lastMessage;
    private long lastMessageTimeStamp;
    private int unreadCount;
    private List<String> userIds;

    // Default constructor for Firestore
    public ChatRoom() {
    }

    public ChatRoom(String roomId, String roomName, String lastMessage, long lastMessageTimeStamp, List<String> userIds) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.lastMessage = lastMessage;
        this.lastMessageTimeStamp = lastMessageTimeStamp;
        this.userIds = userIds;
    }

    // Getters and Setters
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTimeStamp() { return lastMessageTimeStamp; }
    public void setLastMessageTimeStamp(long lastMessageTimeStamp) { this.lastMessageTimeStamp = lastMessageTimeStamp; }

    public List<String> getUserIds() { return userIds; }
    public void setUserIds(List<String> userIds) { this.userIds = userIds; }

    // Add this getter for unreadCount
    public int getUnreadCount() {
        return unreadCount;
    }

    // Optionally add a setter if needed
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
