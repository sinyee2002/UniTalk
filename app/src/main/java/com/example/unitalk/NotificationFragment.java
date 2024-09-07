package com.example.unitalk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unitalk.adapter.NotificationAdapter;
import com.example.unitalk.model.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter notificationAdapter;
    private List<NotificationModel> notificationList;
    private TextView textViewUsername, noNotificationsText;
    private ImageView userProfile, menuButton;
    private FirebaseFirestore db;
    private String currentUserId;
    private FirebaseAuth mAuth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        Button clearAllButton = view.findViewById(R.id.clear_all_button);
        clearAllButton.setOnClickListener(v -> clearAllNotifications());
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firestore and UI components
        db = FirebaseFirestore.getInstance();
        recyclerViewNotifications = view.findViewById(R.id.recycler_view_notifications);
        userProfile = view.findViewById(R.id.user_profile);
        menuButton = view.findViewById(R.id.menu_button);
        textViewUsername = view.findViewById(R.id.username);
        noNotificationsText = view.findViewById(R.id.no_notifications_text);


        setupMenuButton(menuButton);

        notificationList = new ArrayList<>();
        setUpRecyclerView();

        // Display the current user's username
        displayUsername();

        // Load notifications for the current user
        loadNotifications();

        // Profile button click handler
        userProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Profile clicked", Toast.LENGTH_SHORT).show();
        });


        return view;
    }

    // Set up RecyclerView with the NotificationAdapter
    private void setUpRecyclerView() {
        notificationAdapter = new NotificationAdapter(notificationList, getContext());
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNotifications.setAdapter(notificationAdapter);
    }

    // Display the current user's username
    private void displayUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();

            // Fetch the username from Firestore using the UID
            db.collection("users")
                    .document(currentUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username != null && !username.isEmpty()) {
                                textViewUsername.setText(username);
                            } else {
                                textViewUsername.setText("Unknown User");
                                Toast.makeText(getContext(), "Username not set. Update your profile.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            textViewUsername.setText("Unknown User");
                            Toast.makeText(getContext(), "User document not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Error fetching username", e);
                        textViewUsername.setText("Unknown User");
                        Toast.makeText(getContext(), "Failed to fetch username.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            textViewUsername.setText("No User Signed In");
            Toast.makeText(getContext(), "Please sign in to access this feature.", Toast.LENGTH_SHORT).show();
        }
    }

    // Show the popup menu for profile and logout options
    private void setupMenuButton(View view) {
        menuButton.setOnClickListener(v -> showPopupMenu());
    }

    // Show the popup menu for settings option
    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(getContext(), menuButton);
        // Add settings option to the menu
        popupMenu.getMenu().add(0, 1, 0, "Settings");

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                openSettings();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void openSettings() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            intent.putExtra("userId", user.getUid()); // Pass the current user's ID
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    // Load notifications from Firestore for the current user
    private void loadNotifications() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUserId = user.getUid();

        // Real-time listener for notifications with improved error handling
        db.collection("Notifications")
                .whereEqualTo("receiverId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("NotificationFragment", "Error getting notifications: ", error);
                        Toast.makeText(getContext(), "Failed to load notifications: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        notificationList.clear();
                        toggleNoNotificationsMessage(false);
                        for (DocumentChange docChange : value.getDocumentChanges()) {
                            NotificationModel notification = docChange.getDocument().toObject(NotificationModel.class);
                            notification.setDocumentId(docChange.getDocument().getId());
                            if (notification != null) {
                                // Fetch sender's username using senderId
                                fetchSenderUsername(notification);
                            } else {
                                Log.e("NotificationFragment", "Error parsing notification document.");
                            }
                        }
                    } else {
                        toggleNoNotificationsMessage(true);
                    }
                });
    }

    // Fetch the sender's username using the senderId
    private void fetchSenderUsername(NotificationModel notification) {
        String senderId = notification.getSenderId();

        if (senderId != null) {
            // Fetch username from Firestore
            db.collection("users")
                    .document(senderId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String senderName = documentSnapshot.getString("username");
                            notification.setSenderName(senderName != null ? senderName : "Unknown Sender");
                        } else {
                            notification.setSenderName("Unknown Sender");
                        }
                        // Add the notification to the list after setting the sender name
                        notificationList.add(notification);
                        notificationAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NotificationFragment", "Failed to fetch sender username: ", e);
                        notification.setSenderName("Unknown Sender");
                        notificationList.add(notification);
                        notificationAdapter.notifyDataSetChanged();
                    });
        } else {
            // Handle cases where senderId is missing
            notification.setSenderName("Unknown Sender");
            notificationList.add(notification);
            notificationAdapter.notifyDataSetChanged();
        }
    }

    // Method to toggle the visibility of the "No Notifications" message
    private void toggleNoNotificationsMessage(boolean show) {
        if (show) {
            noNotificationsText.setVisibility(View.VISIBLE);
            recyclerViewNotifications.setVisibility(View.GONE);
        } else {
            noNotificationsText.setVisibility(View.GONE);
            recyclerViewNotifications.setVisibility(View.VISIBLE);
        }
    }
    private void clearAllNotifications() {
        db.collection("Notifications")
                .whereEqualTo("receiverId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference().delete();
                    }
                    notificationList.clear();
                    notificationAdapter.notifyDataSetChanged();
                    toggleNoNotificationsMessage(true);
                    Toast.makeText(getContext(), "All notifications cleared", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to clear notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
