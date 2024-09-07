package com.example.unitalk;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unitalk.adapter.PostAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomePageFragment extends Fragment {

    private TextView Profileusername;
    private TextView postInput;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private static final int POST_REQUEST_CODE = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        Profileusername = view.findViewById(R.id.Profileusername);
        postInput = view.findViewById(R.id.post_input);
        recyclerView = view.findViewById(R.id.recyclerView);

        // Initialize RecyclerView and Adapter
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(postAdapter);

        Profileusername.setOnClickListener(v -> openUserProfile());

        // Initialize menu button
        View menuButton = view.findViewById(R.id.menu_button);
        setupMenuButton(menuButton);

        // Load user profile
        loadUserProfile();
        updatePostsWithUserIds();

        // Handle post input submission
        postInput.setOnClickListener(v -> openPostHomepage());

        // Load initial data
        loadData();

        return view;
    }

    private void openPostHomepage() {
        Intent intent = new Intent(getActivity(), PostHomepage.class);
        startActivityForResult(intent, POST_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == POST_REQUEST_CODE && resultCode == RESULT_OK) {
            refreshHomepage();
        }
    }

    private void setupMenuButton(View view) {
        view.setOnClickListener(v -> showPopupMenu());
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(getContext(), getView().findViewById(R.id.menu_button));
        popupMenu.getMenu().add(0, 1, 0, "Settings");

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                openSettings();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void openSettings() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            intent.putExtra("userId", user.getUid()); // Pass the current user's ID
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private final BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshHomepage();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(refreshReceiver, new IntentFilter("REFRESH_HOMEPAGE"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshReceiver);
    }

    private void refreshHomepage() {
        loadData();
    }

    private void loadData() {
        mFirestore.collection("posts").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String postId = document.getId(); // Get the document ID
                        Post post = document.toObject(Post.class);
                        post.setId(postId);

                        posts.add(post);
                    }
                    postAdapter.updateItems(posts);
                    updateUI(posts);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI(List<Post> posts) {
        postList.clear();
        postList.addAll(posts);
        postAdapter.notifyDataSetChanged();
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DocumentReference docRef = mFirestore.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");

                    if (username != null) {
                        Profileusername.setText(username);
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to load user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void openUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            intent.putExtra("userId", user.getUid()); // Pass the current user's ID
            startActivity(intent);
        }
    }

    private void updatePostsWithUserIds() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String postId = document.getId();
                    String username = document.getString("username");

                    // Fetch userId based on username
                    db.collection("users")
                            .whereEqualTo("username", username)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    for (QueryDocumentSnapshot userDoc : querySnapshot) {
                                        String userId = userDoc.getId();

                                        // Update post document with userId
                                        db.collection("posts").document(postId)
                                                .update("userId", userId)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Successfully updated userId
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Handle update failure
                                                });
                                        break; // Assuming usernames are unique
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure to fetch userId
                            });
                }
            }
        });
    }
}
