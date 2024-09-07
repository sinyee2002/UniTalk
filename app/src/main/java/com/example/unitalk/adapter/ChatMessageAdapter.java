// ChatMessageAdapter.java
package com.example.unitalk.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.unitalk.R;
import com.example.unitalk.model.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.bumptech.glide.Glide;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final Context context;
    private final String currentUserId;





    public ChatMessageAdapter(List<ChatMessage> chatMessages, Context context) {
        this.chatMessages = chatMessages;
        this.context = context;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = (currentUser != null) ? currentUser.getUid() : null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);

        // Correctly display sent or received messages
        if (message.getSenderId().equals(currentUserId)) {
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatTextView.setText(message.getMessageText());

            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                holder.rightChatImageView.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.image_placeholder)  // optional placeholder image
                        .into(holder.rightChatImageView);
                holder.rightChatImageView.setVisibility(View.VISIBLE);
            } else {
                holder.rightChatImageView.setVisibility(View.GONE);
            }



        } else {
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatTextView.setText(message.getMessageText());

            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                holder.leftChatImageView.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .into(holder.leftChatImageView);
                holder.leftChatImageView.setVisibility(View.VISIBLE);
            } else {
                holder.leftChatImageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftChatLayout, rightChatLayout;
        TextView leftChatTextView, rightChatTextView;
        ImageView leftChatImageView, rightChatImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextView = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextView = itemView.findViewById(R.id.right_chat_textview);
            leftChatImageView = itemView.findViewById(R.id.left_chat_imageview);  // Add this
            rightChatImageView = itemView.findViewById(R.id.right_chat_imageview);
        }

        public void bind(ChatMessage message) {
            Log.d("ChatMessageAdapter", "Current User ID: " + currentUserId);
            Log.d("ChatMessageAdapter", "Message Sender ID: " + message.getSenderId());

            // Resetting the visibility to avoid unexpected behavior
            rightChatLayout.setVisibility(View.GONE);
            leftChatLayout.setVisibility(View.GONE);

            if (message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
                // Display message on the right for the sender
                rightChatLayout.setVisibility(View.VISIBLE);
                rightChatTextView.setText(message.getMessageText());
            } else {
                // Display message on the left for the receiver
                leftChatLayout.setVisibility(View.VISIBLE);
                leftChatTextView.setText(message.getMessageText());
            }

        }

    }
}
