package com.example.unitalk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unitalk.adapter.LostAndFoundAdapter;
import com.example.unitalk.model.LostAndFoundItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class LostAndFoundFragment extends Fragment {

    private ImageView menuButton;
    private RecyclerView recyclerView;
    private LostAndFoundAdapter adapter;
    private List<LostAndFoundItem> itemList;
    private EditText searchBar;
    private FirebaseAuth mAuth;

    private Button searchButton, filterButton, uploadButton;
    private TextView textViewUsername; // TextView to display the username

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_lost_and_found, container, false);

        mAuth = FirebaseAuth.getInstance();

        // Initialize the menu button
        menuButton = view.findViewById(R.id.menu_button);
// Set up click listener for the menu button
        menuButton.setOnClickListener(v -> showPopupMenu());



        // Initialize RecyclerView using the inflated view
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the list and adapter
        itemList = new ArrayList<>();
        adapter = new LostAndFoundAdapter(itemList, getContext());
        recyclerView.setAdapter(adapter);

        // Initialize search bar and buttons
        searchBar = view.findViewById(R.id.search_bar);
        searchButton = view.findViewById(R.id.search_button);
        filterButton = view.findViewById(R.id.filter_button);
        uploadButton = view.findViewById(R.id.upload_lost_item_button);

        // Initialize username TextView
        textViewUsername = view.findViewById(R.id.username);

        // Display the signed-in user's name
        displayUsername();

        // Set up click listeners
        searchButton.setOnClickListener(v -> performSearch());
        filterButton.setOnClickListener(v -> showFilterDialog());
        // Set up click listener for the upload button
        uploadButton.setOnClickListener(v -> {
            try {
                // Intent to start the PostLostAndFoundActivity
                Intent intent = new Intent(getActivity(), PostLostAndFoundActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                // Log error and show a toast message if the activity cannot start
                Log.e("LostAndFoundFragment", "Error starting PostLostAndFoundActivity", e);
                Toast.makeText(getContext(), "Failed to open upload screen.", Toast.LENGTH_SHORT).show();
            }
        });



        // Load lost and found items
        loadLostAndFoundItems();

        return view; // Return the inflated view
    }

    // Method to display the signed-in user's username
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

    private void loadLostAndFoundItems() {
        FirebaseFirestore.getInstance().collection("lost_and_found")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(getContext(), "Failed to load data.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        itemList.clear();
                        if (value != null) {
                            for (QueryDocumentSnapshot document : value) {
                                LostAndFoundItem item = document.toObject(LostAndFoundItem.class);
                                itemList.add(item);
                            }
                            adapter.notifyDataSetChanged(); // Notify adapter that data has changed
                        }
                    }
                });
    }

    private void performSearch() {
        String query = searchBar.getText().toString().trim();

        // Show all items if the search bar is empty
        if (query.isEmpty()) {
            loadLostAndFoundItems();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("lost_and_found")
                .whereGreaterThanOrEqualTo("title", query)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        LostAndFoundItem item = document.toObject(LostAndFoundItem.class);
                        if (item.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                                item.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                                item.getLocation().toLowerCase().contains(query.toLowerCase())) {
                            itemList.add(item);
                        }
                    }
                    if (itemList.isEmpty()) {
                        Toast.makeText(getContext(), "No items found.", Toast.LENGTH_SHORT).show();
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("LostAndFoundFragment", "Search failed: ", e);
                    Toast.makeText(getContext(), "Search failed.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showFilterDialog() {
        final String[] locations = {"Kampar", "Sungai Long"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Filter by location")
                .setItems(locations, (dialog, which) -> filterByLocation(locations[which]));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void filterByLocation(String location) {
        FirebaseFirestore.getInstance().collection("lost_and_found")
                .whereEqualTo("location", location)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(getContext(), "Failed to filter data.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        itemList.clear();
                        if (value != null) {
                            for (QueryDocumentSnapshot document : value) {
                                LostAndFoundItem item = document.toObject(LostAndFoundItem.class);
                                itemList.add(item);
                            }
                            adapter.notifyDataSetChanged(); // Notify the adapter
                        }
                    }
                });
    }
}
