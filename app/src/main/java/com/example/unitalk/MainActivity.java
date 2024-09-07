package com.example.unitalk;

import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Replace with your activity's layout resource

        // BottomNavigationView initialization
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the default selected item, if needed
        bottomNavigationView.setSelectedItemId(R.id.homepage);

        // Set up the navigation item selection listener using if-else statements
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.homepage) {
                // Handle home page click
                loadFragment(new HomePageFragment()); // Replace with your HomePageFragment class
                return true;
            } else if (itemId == R.id.marketplace) {
                // Handle marketplace click
                loadFragment(new MarketplaceFragment()); // Replace with your MarketplaceFragment class
                return true;
            } else if (itemId == R.id.lostandfound) {
                // Handle lost and found click
                loadFragment(new LostAndFoundFragment()); // Replace with your LostAndFoundFragment class
                return true;
            } else if (itemId == R.id.notification) {
                // Handle notification click
                loadFragment(new NotificationFragment()); // Replace with your NotificationFragment class
                return true;
            } else if (itemId== R.id.chatroom) {
                loadFragment(new ChatRoomFragment());
                return true;
            }

            return false;
        });

        // Load the default fragment on activity launch
        if (savedInstanceState == null) {
            loadFragment(new HomePageFragment()); // Load your default fragment
        }
    }

    // Method to load the selected fragment
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment) // Make sure fragment_container is defined in your activity_main.xml
                .commit();
    }





}
