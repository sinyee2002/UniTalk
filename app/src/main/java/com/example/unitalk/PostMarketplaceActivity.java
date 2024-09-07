package com.example.unitalk;

import android.content.ContentResolver;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class PostMarketplaceActivity extends AppCompatActivity {

    private EditText productNameEditText, productPriceEditText, productDescriptionEditText;
    private Button uploadPhotoButton, submitPostButton;
    private ImageView uploadedImageView;
    private Uri imageUri;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_marketplace);

        // Initialize views
        productNameEditText = findViewById(R.id.productName);
        productPriceEditText = findViewById(R.id.productPrice);
        productDescriptionEditText = findViewById(R.id.productDescription);
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        submitPostButton = findViewById(R.id.submitPostButton);
        uploadedImageView = findViewById(R.id.uploadedImageView);
        backButton = findViewById(R.id.backButton);

        // Initialize Firebase references
        storageReference = FirebaseStorage.getInstance().getReference("product_images");
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set up click listeners
        uploadPhotoButton.setOnClickListener(view -> openFileChooser());
        submitPostButton.setOnClickListener(view -> submitPost());
        backButton.setOnClickListener(view -> finish());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadedImageView.setImageURI(imageUri);
            uploadedImageView.setVisibility(View.VISIBLE);
        }
    }

    private void submitPost() {
        String productName = productNameEditText.getText().toString().trim();
        String productPriceStr = productPriceEditText.getText().toString().trim();
        String productDescription = productDescriptionEditText.getText().toString().trim();

        if (productName.isEmpty() || productPriceStr.isEmpty() || productDescription.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double productPrice;

        try {
            productPrice = Double.parseDouble(productPriceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You need to be logged in to post", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        // Retrieve username from Firestore
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User userProfile = documentSnapshot.toObject(User.class);
                        String username = userProfile != null ? userProfile.getUsername() : "Unknown";

                        if (imageUri != null) {
                            StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                                    + "." + getFileExtension(imageUri));

                            fileReference.putFile(imageUri)
                                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                                            .addOnSuccessListener(uri -> {
                                                Product product = new Product(productName, productPrice, productDescription, uri.toString(), username, 0.0, 0.0);
                                                db.collection("products").add(product)
                                                        .addOnSuccessListener(documentReference -> {
                                                            Toast.makeText(PostMarketplaceActivity.this, "Post Successful", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(PostMarketplaceActivity.this, "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(PostMarketplaceActivity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }))
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(PostMarketplaceActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to retrieve user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
