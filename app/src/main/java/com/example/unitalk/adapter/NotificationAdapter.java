package com.example.unitalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unitalk.R;
import com.example.unitalk.model.NotificationModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<NotificationModel> notificationList;
    private final Context context;

    public NotificationAdapter(List<NotificationModel> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);
        if (notification != null) {
            String senderName = notification.getSenderName() != null && !notification.getSenderName().isEmpty()
                    ? notification.getSenderName()
                    : "Unknown Sender";

            // Display the message "You have a message from [sender]"
            String displayMessage = "You have a message from " + senderName;
            holder.notificationTextView.setText(displayMessage);
            holder.notificationTimestamp.setText(formatTimestamp(notification.getTimestamp()));
            holder.notificationIcon.setImageResource(R.drawable.ic_notification);

            // Set click listener for the clear icon to remove the notification
            holder.clearNotificationIcon.setOnClickListener(v -> removeNotification(position));
        } else {
            holder.notificationTextView.setText("No notification details available.");
            holder.notificationTimestamp.setText(""); // No timestamp for empty notifications
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    // Method to format the timestamp to a readable date string
    private String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    // Method to remove the notification from Firestore and update the list
    private void removeNotification(int position) {
        if (position >= 0 && position < notificationList.size()) {
            NotificationModel notification = notificationList.get(position);
            String documentId = notification.getDocumentId();

            if (documentId != null && !documentId.isEmpty()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("Notifications").document(documentId);

                docRef.delete()
                        .addOnSuccessListener(aVoid -> {
                            notificationList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, notificationList.size());
                            Toast.makeText(context, "Notification removed successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to remove notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(context, "Invalid document ID; cannot remove from Firestore.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to clear all notifications from Firestore and update the list
    public void clearAllNotifications() {
        // Reference to the Firestore database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Loop through the notification list and delete each notification from Firestore
        for (NotificationModel notification : notificationList) {
            String documentId = notification.getDocumentId();
            if (documentId != null && !documentId.isEmpty()) {
                DocumentReference docRef = db.collection("Notifications").document(documentId);
                docRef.delete()
                        .addOnFailureListener(e -> {
                            // Optional: Handle failure if you need to track errors
                            Toast.makeText(context, "Failed to remove notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }

        // Clear the list and notify the adapter
        notificationList.clear();
        notifyDataSetChanged();

        Toast.makeText(context, "All notifications cleared", Toast.LENGTH_SHORT).show();
    }


    // ViewHolder class to hold references to UI components of each item
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationTextView;
        TextView notificationTimestamp;
        ImageView notificationIcon;
        ImageView clearNotificationIcon;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationTextView = itemView.findViewById(R.id.notification_text);
            notificationTimestamp = itemView.findViewById(R.id.notification_timestamp);
            notificationIcon = itemView.findViewById(R.id.notification_icon);
            clearNotificationIcon = itemView.findViewById(R.id.clear_notification_icon);
        }
    }
}
