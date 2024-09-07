package com.example.unitalk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.firebase.auth.FirebaseUser;


public class MarketplaceActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MarketplaceAdapter adapter;
    private List<Product> productList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView usernameTextView;
    private FirebaseAuth auth;
    private ImageView menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketplace);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadProductsFromFirestore);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        productList = new ArrayList<>();
        adapter = new MarketplaceAdapter(this, productList);
        recyclerView.setAdapter(adapter);

        // Initialize username TextView
        usernameTextView = findViewById(R.id.username);

        // Initialize menu button
        menuButton = findViewById(R.id.menu_button);
        setupMenuButton(menuButton);

        // Load current username
        loadUsername();

        // Load products from Firestore
        loadProductsFromFirestore();
    }

    private void setupMenuButton(View view) {
        menuButton.setOnClickListener(v -> showPopupMenu()); // Call showPopupMenu() when the button is clicked
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, menuButton);

        // Add menu items programmatically
        popupMenu.getMenu().add(0, 1, 0, "Settings"); // First parameter: groupId, second: itemId, third: order, fourth: title

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) { // Settings option
                openSettings();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void openSettings() {
        Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadUsername() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(firebaseUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username != null && !username.isEmpty()) {
                                usernameTextView.setText(username);
                            } else {
                                usernameTextView.setText(firebaseUser.getEmail()); // Fallback to email if username is missing
                            }
                        } else {
                            usernameTextView.setText(firebaseUser.getEmail()); // Fallback to email if no document
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MarketplaceActivity", "Error getting user document", e);
                        usernameTextView.setText(firebaseUser.getEmail()); // Fallback to email on error
                    });
        } else {
            usernameTextView.setText("Guest"); // Default text if no user is signed in
        }
    }

    private void loadProductsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear(); // Clear existing data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            String name = (String) data.get("name");
                            Object priceObj = data.get("price");
                            double price = 0;
                            if (priceObj instanceof Double) {
                                price = (Double) priceObj;
                            } else if (priceObj instanceof String) {
                                try {
                                    price = Double.parseDouble((String) priceObj);
                                } catch (NumberFormatException e) {
                                    Log.e("MarketplaceActivity", "Error parsing price", e);
                                }
                            }
                            String description = (String) data.get("description");
                            String imageUrl = (String) data.get("imageUrl");
                            String username = (String) data.get("username");

                            double sellerLat = data.containsKey("sellerLat") ? (double) data.get("sellerLat") : 0.0;
                            double sellerLng = data.containsKey("sellerLng") ? (double) data.get("sellerLng") : 0.0;
                            Product product = new Product(name, price, description, imageUrl, username, sellerLat, sellerLng);
                            productList.add(product);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("MarketplaceActivity", "Error getting documents: ", task.getException());
                    }

                    // Stop the refresh animation
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    public void openPostMarketplaceActivity(View view) {
        Intent intent = new Intent(this, PostMarketplaceActivity.class);
        startActivity(intent);
    }
}
