// LostAndFoundActivity.java
package com.example.unitalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LostAndFoundActivity extends AppCompatActivity {

    private static final String TAG = "LostAndFoundActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_and_found);

        // Initialize Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

    // Listener for Bottom Navigation Item Selection
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();

                    if (itemId == R.id.homepage) {
                        startActivity(new Intent(LostAndFoundActivity.this, MainActivity.class));
                    } else if (itemId == R.id.marketplace) {
                        // Launch MarketplaceActivity
                        startActivity(new Intent(LostAndFoundActivity.this, MarketplaceActivity.class));
                    } else if (itemId == R.id.lostandfound) {
                        startActivity(new Intent(LostAndFoundActivity.this, LostAndFoundActivity.class));


                    } else if (itemId == R.id.notification) {
                        // Launch NotificationActivity
                        startActivity(new Intent(LostAndFoundActivity.this, NotificationActivity.class));
                    }
                    else if (itemId == R.id.chatroom) {
                        // Launch NotificationActivity

                    }


                    else {
                        Log.e(TAG, "Unknown menu item selected");
                    }
                    return true;
                }
            };
}
