package com.example.unitalk;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import android.location.Geocoder;
import android.location.Address;
import java.util.List;
import java.util.Locale;

public class SellerInfoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView sellerUsername, productName, productPrice, productDescription;
    private ImageView productImage;
    private GoogleMap mMap;
    private double sellerLat = 4.336214; // Example lat
    private double sellerLng = 101.142111; // Example lng
    private FloatingActionButton fabChat;
    private AutoCompleteTextView sellerLocation;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_info);

        // Initialize views
        sellerUsername = findViewById(R.id.sellerUsername);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productDescription = findViewById(R.id.productDescription);
        productImage = findViewById(R.id.productImage);

        fabChat = findViewById(R.id.fabChat);

        View backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> onBackPressed());

        // Get product and seller information from the Intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("sellerUsername");

        String name = intent.getStringExtra("productName");
        double price = intent.getDoubleExtra("productPrice", 0);
        String description = intent.getStringExtra("productDescription");
        String imageUrl = intent.getStringExtra("productImageUrl");
        double sellerLat = intent.getDoubleExtra("sellerLat", 0.0);
        double sellerLng = intent.getDoubleExtra("sellerLng", 0.0);

        Log.d("SellerInfoActivity", "Latitude: " + sellerLat + ", Longitude: " + sellerLng);

        // Set data to views
        sellerUsername.setText(username);
        productName.setText(name);
        productPrice.setText(String.format("RM %.2f", price));
        productDescription.setText(description);

        // Load product image using Glide
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_product_placeholder)
                .into(productImage);

        // Load map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fabChat.setOnClickListener(v -> {
            Intent chatIntent = new Intent(SellerInfoActivity.this, ChatActivity.class);
            chatIntent.putExtra("receiverUsername", username);
            chatIntent.putExtra("receiverId", getReceiverIdByUsername(username)); // Ensure this method gets the receiver's ID
            chatIntent.putExtra("productImage", imageUrl); // URL of the product image
            chatIntent.putExtra("productPrice", price);

            startActivity(chatIntent);
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.d("SellerInfoActivity", "Latitude: " + sellerLat + ", Longitude: " + sellerLng);

        // Set the seller's location on the map
        LatLng sellerLocation = new LatLng(sellerLat, sellerLng);
        mMap.addMarker(new MarkerOptions().position(sellerLocation).title("Seller Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sellerLocation, 15));
    }
    FirebaseFirestore db = FirebaseFirestore.getInstance();








    private String getReceiverIdByUsername(String username) {
        // Implement the logic to fetch receiver ID by username
        // This could involve querying the database for the user's ID based on the username
        // For now, you might just return a dummy ID or handle it differently based on your app's requirements
        return "dummyReceiverId"; // Modify this as necessary
    }
}
