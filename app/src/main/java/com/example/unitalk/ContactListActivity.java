package com.example.unitalk;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.unitalk.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactListActivity extends AppCompatActivity {

    private RecyclerView contactsList;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list); // Ensure this layout exists

        // Initialize Firebase Authentication and Database references
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Initialize RecyclerView
        contactsList = findViewById(R.id.contacts_list); // Ensure this ID exists in your layout
        contactsList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set up FirebaseRecyclerOptions
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(usersRef, User.class)
                        .build();

        // Set up FirebaseRecyclerAdapter
        FirebaseRecyclerAdapter<User, ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull User model) {
                        // Display the sender's username and status
                        if (model.getUsername() != null) {
                            holder.userName.setText(model.getUsername()); // Display sender's username
                            Log.d("ContactListActivity", "Retrieved Username: " + model.getUsername());
                        } else {
                            Log.e("ContactListActivity", "Username is null");
                        }

                        if (model.getStatus() != null) {
                            holder.userStatus.setText(model.getStatus());
                        }

                        // Load profile image if available
                        if (model.getImageURL() != null && !model.getImageURL().equals("default")) {
                            Picasso.get().load(model.getImageURL()).placeholder(R.drawable.person_icon).into(holder.profileImage);
                        }

                        // Logging to check what data is being passed
                        Log.d("ContactListActivity", "Passing Receiver Username: " + model.getUsername());
                        Log.d("ContactListActivity", "Passing Receiver ID: " + getRef(position).getKey());

                        // In ContactListActivity - Ensure correct intent data is passed
                        holder.itemView.setOnClickListener(v -> {
                            String username = model.getUsername();
                            String userId = getRef(position).getKey();
                            if (username == null || username.isEmpty()) {
                                Log.e("ContactListActivity", "Username is empty or null, fetching from database.");
                                fetchUsername(userId); // Fetch username using userId if null
                            } else {
                                startChatActivity(username, userId);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
                        return new ContactsViewHolder(view);
                    }
                };

        contactsList.setAdapter(adapter);
        adapter.startListening();
    }

    // ViewHolder class for the contacts
    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.contact_name); // Ensure these IDs exist in your layout
            userStatus = itemView.findViewById(R.id.contact_status);
            profileImage = itemView.findViewById(R.id.contact_image);
        }
    }

    // Fetch username from database if not initially available
    private void fetchUsername(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("username")) {
                    String username = snapshot.child("username").getValue(String.class);
                    if (username != null) {
                        Log.d("ContactListActivity", "Fetched Username: " + username);
                        startChatActivity(username, userId);
                    } else {
                        Log.e("ContactListActivity", "Fetched username is null.");
                    }
                } else {
                    Log.e("ContactListActivity", "Username not found for userId: " + userId);
                    Toast.makeText(ContactListActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ContactListActivity", "Failed to fetch username", error.toException());
            }
        });
    }

    // Start ChatActivity with the fetched or passed username and userId
    private void startChatActivity(String username, String userId) {
        Intent chatIntent = new Intent(ContactListActivity.this, ChatActivity.class);
        chatIntent.putExtra("receiverUsername", username); // Ensure the username is passed correctly
        chatIntent.putExtra("receiverId", userId); // Also pass the receiver ID
        startActivity(chatIntent);
    }
}
