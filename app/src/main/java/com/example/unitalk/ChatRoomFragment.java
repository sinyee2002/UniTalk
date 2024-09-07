package com.example.unitalk;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.view.MenuItem;

import com.example.unitalk.model.ChatRoom;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.List;

public class ChatRoomFragment extends Fragment {

    private View chatRoomsView;
    private ImageView menuButton;
    private RecyclerView chatRoomsList;
    private TextView textViewUsername;

    private CollectionReference chatRoomsRef;
    private CollectionReference usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public ChatRoomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        chatRoomsView = inflater.inflate(R.layout.fragment_chat_room, container, false);

        chatRoomsList = chatRoomsView.findViewById(R.id.chat_room_list);
        chatRoomsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        menuButton=chatRoomsView.findViewById(R.id.menu_button);
        textViewUsername=chatRoomsView.findViewById(R.id.username);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        chatRoomsRef = FirebaseFirestore.getInstance().collection("chatRooms");
        usersRef = db.collection("users");

        setupMenuButton(chatRoomsView);
        displayUsername();

        return chatRoomsView;
    }

    private void displayUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid(); // Get the current user's UID

            // Fetch the username from Firestore using the UID
            FirebaseFirestore.getInstance().collection("users") // Adjust collection name if different
                    .document(userId)
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

    @Override
    public void onStart() {
        super.onStart();

        // Configure FirestoreRecyclerOptions
        FirestoreRecyclerOptions<ChatRoom> options =
                new FirestoreRecyclerOptions.Builder<ChatRoom>()
                        .setQuery(chatRoomsRef.whereArrayContains("userIds", currentUserId), ChatRoom.class)
                        .build();

        // Set up FirestoreRecyclerAdapter
        FirestoreRecyclerAdapter<ChatRoom, ChatRoomViewHolder> adapter =
                new FirestoreRecyclerAdapter<ChatRoom, ChatRoomViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatRoomViewHolder holder, final int position, @NonNull ChatRoom model) {
                        // Retrieve the chatRoomId from the Firestore snapshot
                        final String chatRoomId = getSnapshots().getSnapshot(position).getId();

                        // Fetch chat room details using Firestore
                        chatRoomsRef.document(chatRoomId).addSnapshotListener((snapshot, error) -> {
                            if (error != null) {
                                // Handle errors
                                Log.e("ChatRoomFragment", "Error fetching chat room details", error);
                                return;
                            }

                            if (snapshot != null && snapshot.exists()) {
                                String lastMessage = snapshot.getString("lastMessage");
                                holder.lastMessage.setText(lastMessage != null ? lastMessage : "No Messages Yet");

                                // Extract userIds to identify the receiver
                                List<String> userIds = (List<String>) snapshot.get("userIds");
                                if (userIds != null && userIds.size() == 2) {
                                    // Determine the receiver ID
                                    String receiverId = userIds.get(0).equals(currentUserId) ? userIds.get(1) : userIds.get(0);
                                    Log.d("ChatRoomFragment", "Receiver ID: " + receiverId);

                                    // Fetch and display the receiver's username before navigating
                                    fetchReceiverUsername(receiverId, holder, chatRoomId);
                                } else {
                                    Log.e("ChatRoomFragment", "UserIds are not correctly formatted or missing.");
                                }
                            }
                        });
                    }

                    // Method to fetch the receiver's username based on the receiver ID and set up navigation
                    private void fetchReceiverUsername(String receiverId, ChatRoomViewHolder holder, String chatRoomId) {
                        FirebaseFirestore.getInstance().collection("users").document(receiverId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String username = documentSnapshot.getString("username"); // Make sure the field name is correct
                                        if (username != null) {
                                            holder.roomName.setText(username); // Display the receiver's username
                                            Log.d("ChatRoomFragment", "Receiver Username: " + username);

                                            // Set up the click listener to go to the chat activity
                                            holder.itemView.setOnClickListener(v -> {
                                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                chatIntent.putExtra("chatRoomId", chatRoomId);
                                                chatIntent.putExtra("receiverId", receiverId); // Pass the receiver ID to the ChatActivity
                                                chatIntent.putExtra("receiverUsername", username); // Pass the receiver username to the ChatActivity
                                                startActivity(chatIntent);
                                            });
                                        } else {
                                            holder.roomName.setText("Unknown User");
                                            Log.e("ChatRoomFragment", "Username is null.");
                                        }
                                    } else {
                                        holder.roomName.setText("User Not Found");
                                        Log.e("ChatRoomFragment", "User document does not exist.");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    holder.roomName.setText("Error Loading Name");
                                    Log.e("ChatRoomFragment", "Failed to fetch username", e);
                                });
                    }

                    @NonNull
                    @Override
                    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
                        return new ChatRoomViewHolder(view);
                    }
                };

        // Set adapter to the RecyclerView
        chatRoomsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView roomName, lastMessage;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);

            roomName = itemView.findViewById(R.id.room_name);
            lastMessage = itemView.findViewById(R.id.last_message);
        }
    }

    private void setupMenuButton(View view) {
        ImageView menuButton = view.findViewById(R.id.menu_button); // Ensure this ID matches your top menu button
        menuButton.setOnClickListener(v -> showPopupMenu()); // Call showPopupMenu() when the button is clicked
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(getContext(), menuButton);

        // Add menu items programmatically
        popupMenu.getMenu().add(0, 1, 0, "Settings"); // First parameter: groupId, second: itemId, third: order, fourth: title


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1: // Profile option
                        openSettings();
                        return true;

                    default:
                        return false;
                }
            }
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


}
