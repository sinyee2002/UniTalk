package com.example.unitalk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private EditText emailEditText;
    private EditText passwordEditText;
    private ImageView togglePasswordVisibility;
    private boolean isPasswordVisible = false;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance(); // Initialize Firestore
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        Button signInButton = findViewById(R.id.signInButton);
        ImageView googleSignInButton = findViewById(R.id.googleSignInButton);
        TextView registerLink = findViewById(R.id.registerLink);
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);

        forgotPassword.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, ForgotPasswordActivity.class)));

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(v -> signInWithEmail());
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        registerLink.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, RegisterActivity.class)));

        // Toggle password visibility
        togglePasswordVisibility.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.baseline_visibility_off_24); // Replace with your "hidden" icon
            } else {
                passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.baseline_visibility_24); // Replace with your "visible" icon
            }
            passwordEditText.setSelection(passwordEditText.getText().length()); // Move cursor to end
            isPasswordVisible = !isPasswordVisible;
        });
    }

    private void signInWithEmail() {
        String input = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (input.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignInActivity.this, "Username/Email and password must not be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the input is an email
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            signInWithEmailPassword(input, password);
        } else {
            // Assume it's a username
            fetchEmailFromUsername(input, password);
        }
    }

    private void fetchEmailFromUsername(String username, String password) {
        mFirestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String email = document.getString("email");
                                signInWithEmailPassword(email, password);
                            }
                        } else {
                            Toast.makeText(SignInActivity.this, "Username not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignInActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithEmailPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                // Email/Password users go directly to MainActivity
                                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(SignInActivity.this, "Please verify your email before signing in.", Toast.LENGTH_SHORT).show();
                                mAuth.signOut(); // Sign out the user if the email is not verified
                            }
                        }
                    } else {
                        Toast.makeText(SignInActivity.this, "Sign In failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 9001) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(SignInActivity.this, "Google Sign In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveGoogleUserToFirestore(user); // Handle Google sign-in user
                        }
                    } else {
                        Toast.makeText(SignInActivity.this, "Google Sign In failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

// Inside SignInActivity.java

    private void saveGoogleUserToFirestore(FirebaseUser user) {
        DocumentReference docRef = mFirestore.collection("users").document(user.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Check if the user has set a password
                String password = documentSnapshot.getString("password");
                if (password == null || password.isEmpty()) {
                    // No password set, direct user to set password
                    Intent intent = new Intent(SignInActivity.this, SetPasswordActivity.class);
                    intent.putExtra("userId", user.getUid());
                    startActivity(intent);
                    finish();
                } else {
                    // Password is already set, proceed to MainActivity
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    finish();
                }
            } else {
                // New Google user, add user data and ask to set a password
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("email", user.getEmail());
                userMap.put("username", user.getDisplayName());
                userMap.put("password", ""); // Explicitly set password as empty to signify a new user

                docRef.set(userMap)
                        .addOnSuccessListener(aVoid -> {
                            // Direct Google user to SetPasswordActivity
                            Intent intent = new Intent(SignInActivity.this, SetPasswordActivity.class);
                            intent.putExtra("userId", user.getUid());
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(SignInActivity.this, "Error saving user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

}
