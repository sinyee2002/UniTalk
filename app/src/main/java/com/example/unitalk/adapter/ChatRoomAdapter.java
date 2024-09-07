package com.example.unitalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unitalk.R;
import com.example.unitalk.model.ChatRoom;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private final List<ChatRoom> chatRooms;
    private final Context context;
    private final OnChatRoomClickListener clickListener;

    public ChatRoomAdapter(List<ChatRoom> chatRooms, Context context, OnChatRoomClickListener clickListener) {
        this.chatRooms = chatRooms;
        this.context = context;
        this.clickListener = clickListener;

        // Sort chat rooms by timestamp to display the newest messages at the top
        sortChatRoomsByNewestFirst();
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);
        holder.roomName.setText(chatRoom.getRoomName());
        holder.lastMessage.setText(chatRoom.getLastMessage());

        // Set the formatted timestamp
        holder.messageTime.setText(formatTimestamp(chatRoom.getLastMessageTimeStamp()));

        // Set the unread message count
        int unreadCount = chatRoom.getUnreadCount();
        if (unreadCount > 0) {
            holder.unreadCount.setVisibility(View.VISIBLE);
            holder.unreadCount.setText(String.valueOf(unreadCount));
        } else {
            holder.unreadCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> clickListener.onChatRoomClick(chatRoom));
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    // Method to format the timestamp to a readable time format like '12:45 PM'
    private String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    // Method to sort chat rooms by the newest message timestamp first
    private void sortChatRoomsByNewestFirst() {
        Collections.sort(chatRooms, new Comparator<ChatRoom>() {
            @Override
            public int compare(ChatRoom o1, ChatRoom o2) {
                // Sort in descending order (newest first)
                return Long.compare(o2.getLastMessageTimeStamp(), o1.getLastMessageTimeStamp());
            }
        });
    }

    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView roomName;
        TextView lastMessage;
        TextView messageTime;
        TextView unreadCount; // Added unreadCount TextView

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.room_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            messageTime = itemView.findViewById(R.id.message_time);
            unreadCount = itemView.findViewById(R.id.unread_count); // Bind to the unread count TextView
        }
    }

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom);
    }
}
