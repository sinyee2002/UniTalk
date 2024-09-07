package com.example.unitalk;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText currentPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private ImageView currentPasswordToggle, newPasswordToggle, confirmNewPasswordToggle;
    private Button changePasswordButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText);
        currentPasswordToggle = findViewById(R.id.currentPasswordToggle);
        newPasswordToggle = findViewById(R.id.newPasswordToggle);
        confirmNewPasswordToggle = findViewById(R.id.confirmNewPasswordToggle);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        View backButton = findViewById(R.id.back_button);

        setupPasswordToggle(currentPasswordEditText, currentPasswordToggle);
        setupPasswordToggle(newPasswordEditText, newPasswordToggle);
        setupPasswordToggle(confirmNewPasswordEditText, confirmNewPasswordToggle);

        changePasswordButton.setOnClickListener(v -> changePassword());

        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void setupPasswordToggle(EditText editText, ImageView toggle) {
        toggle.setOnClickListener(v -> {
            if (editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // Show password
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggle.setImageResource(R.drawable.baseline_visibility_24); // Change to 'show' icon
            } else {
                // Hide password
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggle.setImageResource(R.drawable.baseline_visibility_off_24); // Change to 'hide' icon
            }
            // Move cursor to the end of the text
            editText.setSelection(editText.getText().length());
        });
    }

    private void changePassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(ChangePasswordActivity.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(ChangePasswordActivity.this, "New passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isPasswordValid(newPassword)) {
            newPasswordEditText.setError("Password must be at least 8 characters long and include uppercase, lowercase, numbers, and symbols.");
            return;
        }


        mAuth.signInWithEmailAndPassword(mAuth.getCurrentUser().getEmail(), currentPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        mAuth.getCurrentUser().updatePassword(newPassword)
                                .addOnCompleteListener(this, updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(ChangePasswordActivity.this, "Password changed successfully.", Toast.LENGTH_SHORT).show();
                                        // Navigate to ProfileActivity
                                        Intent intent = new Intent(ChangePasswordActivity.this, ProfileActivity.class);
                                        startActivity(intent);
                                        finish(); // Close the ChangePasswordActivity
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this, "Error: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Current password is incorrect.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[^A-Za-z0-9].*");
    }
}
