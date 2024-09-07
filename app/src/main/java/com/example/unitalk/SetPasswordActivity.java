package com.example.unitalk;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SetPasswordActivity extends AppCompatActivity {

    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button setPasswordButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private ImageView passwordToggle, confirmPasswordToggle;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        setPasswordButton = findViewById(R.id.setPasswordButton);
        passwordToggle = findViewById(R.id.togglePasswordVisibility);
        confirmPasswordToggle = findViewById(R.id.toggleConfirmPasswordVisibility);

        setPasswordButton.setOnClickListener(v -> setPasswordForUser());

        passwordToggle.setOnClickListener(v -> togglePasswordVisibility());
        confirmPasswordToggle.setOnClickListener(v -> toggleConfirmPasswordVisibility());
    }

    private void setPasswordForUser() {
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(SetPasswordActivity.this, "Both fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(SetPasswordActivity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordValid(password)) {
            passwordEditText.setError("Password must be at least 8 characters long and include uppercase, lowercase, numbers, and symbols.");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    savePasswordToFirestore(user.getUid(), password); // Call the correct method
                    Toast.makeText(SetPasswordActivity.this, "Password set successfully.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SetPasswordActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(SetPasswordActivity.this, "Error setting password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(SetPasswordActivity.this, "No user is signed in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePasswordToFirestore(String userId, String password) {
        DocumentReference docRef = mFirestore.collection("users").document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("password", password); // Save the password

        docRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Password successfully set, proceed to MainActivity
                    startActivity(new Intent(SetPasswordActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SetPasswordActivity.this, "Error updating password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24);  // Assuming you have this icon for invisible state
        } else {
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwordToggle.setImageResource(R.drawable.baseline_visibility_24);  // Assuming you have this icon for visible state
        }
        isPasswordVisible = !isPasswordVisible;
        passwordEditText.setSelection(passwordEditText.getText().length()); // Move cursor to the end
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            confirmPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            confirmPasswordToggle.setImageResource(R.drawable.baseline_visibility_off_24);  // Assuming you have this icon
        } else {
            confirmPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            confirmPasswordToggle.setImageResource(R.drawable.baseline_visibility_24);  // Assuming you have this icon
        }
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length()); // Move cursor to the end
    }
}
