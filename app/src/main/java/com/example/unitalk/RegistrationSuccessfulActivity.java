package com.example.unitalk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrationSuccessfulActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_successful);

        Button signInButton = findViewById(R.id.signInButton);

        signInButton.setOnClickListener(v -> {
            // Redirect to SignInActivity
            startActivity(new Intent(RegistrationSuccessfulActivity.this, SignInActivity.class));
            finish(); // Optional: Call finish() to close the RegistrationSuccessActivity
        });
    }
}
