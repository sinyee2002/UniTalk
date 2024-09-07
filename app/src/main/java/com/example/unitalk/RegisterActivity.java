package com.example.unitalk;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText usernameEditText;
    private ImageView togglePasswordVisibilityImageView;
    private ImageView backButton;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        usernameEditText = findViewById(R.id.username);

        togglePasswordVisibilityImageView = findViewById(R.id.togglePasswordVisibility);
        backButton = findViewById(R.id.backButton);
        Button registerButton = findViewById(R.id.registerButton);
        TextView signInLink = findViewById(R.id.signInLink);

        // Set the default input type to password
        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        registerButton.setOnClickListener(v -> registerUser());
        signInLink.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, SignInActivity.class)));

        togglePasswordVisibilityImageView.setOnClickListener(v -> togglePasswordVisibility());
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordValid(password)) {
            passwordEditText.setError("Password must be at least 8 characters long and include uppercase, lowercase, numbers, and symbols.");
            return;
        }

        checkUsernameUnique(username, isUnique -> {
            if (isUnique) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    saveUserDataToFirestore(user, username);
                                    sendVerificationEmail(user);
                                    startActivity(new Intent(RegisterActivity.this, RegistrationSuccessfulActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "User creation failed.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                usernameEditText.setError("Username already taken. Please choose another one.");
            }
        });
    }

    private void checkUsernameUnique(String username, OnCompleteListener<Boolean> listener) {
        mFirestore.collection("users").whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isUnique = task.getResult().isEmpty();
                        listener.onComplete(isUnique);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to check username uniqueness.", Toast.LENGTH_SHORT).show();
                        listener.onComplete(false); // Treat as not unique if error occurs
                    }
                });
    }

    private void saveUserDataToFirestore(FirebaseUser user, String username) {
        if (user != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("username", username);// Ensure this is the field name in Firestore
            userData.put("image", "default_image_url");
            DocumentReference userDocRef = mFirestore.collection("users").document(user.getUid());
            userDocRef.set(userData)
                    .addOnSuccessListener(aVoid -> Log.d("RegisterActivity", "User data saved successfully."))
                    .addOnFailureListener(e -> Log.e("RegisterActivity", "Error saving user data: " + e.getMessage()));

        }

    }


    private void sendVerificationEmail(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[^A-Za-z0-9].*");
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide the password
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            togglePasswordVisibilityImageView.setImageResource(R.drawable.baseline_visibility_off_24);
        } else {
            // Show the password
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            togglePasswordVisibilityImageView.setImageResource(R.drawable.baseline_visibility_24);
        }
        // Move the cursor to the end of the text
        passwordEditText.setSelection(passwordEditText.getText().length());
        // Toggle the visibility flag
        isPasswordVisible = !isPasswordVisible;
    }

    private interface OnCompleteListener<T> {
        void onComplete(T result);
    }
}
