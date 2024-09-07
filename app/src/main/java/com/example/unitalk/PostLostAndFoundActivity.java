// PostLostAndFoundActivity.java
package com.example.unitalk;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.unitalk.model.LostAndFoundItem;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PostLostAndFoundActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText;
    private Spinner locationSpinner;
    private ImageView selectedImageView;
    private MaterialButton uploadImageButton, submitButton;
    private Uri imageUri;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_lost_and_found);

        // Initialize views and Firebase references
        titleEditText = findViewById(R.id.item_title);
        descriptionEditText = findViewById(R.id.item_description);
        locationSpinner = findViewById(R.id.spinner_location);
        selectedImageView = findViewById(R.id.selected_image);
        uploadImageButton = findViewById(R.id.upload_image_button);
        submitButton = findViewById(R.id.submit_button);
        View backButton = findViewById(R.id.back_button);

        storageReference = FirebaseStorage.getInstance().getReference("lost_and_found_images");
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set click listeners
        uploadImageButton.setOnClickListener(view -> openFileChooser());
        submitButton.setOnClickListener(view -> fetchAndSubmitPost());
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 1);
    }

    // Add Glide dependency in build.gradle if not already included:
// implementation 'com.github.bumptech.glide:glide:4.12.0'
// annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Use Glide to load the selected image into the ImageView
            Glide.with(this)
                    .load(imageUri)
                    .into(selectedImageView);

            // Set the visibility of the selected image to visible
            selectedImageView.setVisibility(View.VISIBLE);
        }
    }


    // Fetch username from Firestore and submit the post
    private void fetchAndSubmitPost() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You need to be logged in to post", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch the username from Firestore using the user's UID
        String userId = user.getUid();
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Retrieve the username field from the Firestore document
                    String username = document.getString("username");
                    if (username != null && !username.isEmpty()) {
                        // Proceed to submit the post with the fetched username
                        submitPost(username);
                    } else {
                        Toast.makeText(this, "Username not found. Please update your profile.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "User data not found in Firestore.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to retrieve username.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    // Handle the permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }
    // Method to handle post submission after fetching the username
    private void submitPost(String username) {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String location = locationSpinner.getSelectedItem().toString().trim();

        if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileExtension = getFileExtension(imageUri);
        if (fileExtension == null || fileExtension.isEmpty()) {
            Toast.makeText(this, "Invalid file type selected", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + fileExtension);

        // Upload the image to Firebase Storage
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL after upload
                    fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // Create the LostAndFoundItem object with the uploader's username
                                LostAndFoundItem item = new LostAndFoundItem(
                                        title,
                                        description,
                                        location,
                                        uri.toString(),
                                        0,
                                        username // Use the fetched username
                                );

                                // Save the item to Firestore
                                db.collection("lost_and_found").add(item)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(PostLostAndFoundActivity.this, "Post Successful", Toast.LENGTH_SHORT).show();
                                            Log.d("FirestoreSuccess", "Item successfully added with ID: " + documentReference.getId());
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("FirestoreError", "Failed to add item to Firestore", e);
                                            Toast.makeText(PostLostAndFoundActivity.this, "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("StorageError", "Failed to get download URL", e);
                                Toast.makeText(PostLostAndFoundActivity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("StorageError", "Image upload failed", e);
                    Toast.makeText(PostLostAndFoundActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Helper method to get the file extension from the URI
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
