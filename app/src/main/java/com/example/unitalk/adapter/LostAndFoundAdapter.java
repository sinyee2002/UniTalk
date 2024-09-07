// LostAndFoundAdapter.java
package com.example.unitalk.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.unitalk.ChatActivity;
import com.example.unitalk.R;
import com.example.unitalk.model.LostAndFoundItem;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LostAndFoundAdapter extends RecyclerView.Adapter<LostAndFoundAdapter.ViewHolder> {

    private List<LostAndFoundItem> itemList;
    private Context context;

    public LostAndFoundAdapter(List<LostAndFoundItem> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lost_and_found, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostAndFoundItem item = itemList.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
        holder.location.setText(item.getLocation());
        holder.commentCount.setText(String.valueOf(item.getCommentCount()));
        holder.username.setText(item.getUsername());

        // Load the item image using Glide
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .into(holder.imageView);

        // Set click listener for the contact button to start ChatActivity with the uploader's username
        holder.contactButton.setOnClickListener(v -> {
            Log.d("LostAndFoundAdapter", "Passing username: " + item.getUsername());

            // Create an intent to start ChatActivity
            Intent intent = new Intent(context, ChatActivity.class);
            // Pass the uploader's username to the ChatActivity
            intent.putExtra("receiverUsername", item.getUsername()); // Pass the username to find receiverId later
            context.startActivity(intent);
        });

        // Load comments for the item
        loadComments(holder, item);

        // Set up the send button click listener for adding comments
        holder.sendCommentButton.setOnClickListener(v -> {
            String commentText = holder.commentInput.getText().toString().trim();
            if (commentText.isEmpty()) {
                Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Upload the comment to Firestore
            uploadComment(item, commentText, holder);

            // Clear the input field after sending
            holder.commentInput.setText("");
        });

        // Optionally, set up the comment input field listener for Enter key as well
        holder.commentInput.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                String commentText = holder.commentInput.getText().toString().trim();
                if (!commentText.isEmpty()) {
                    uploadComment(item, commentText, holder);
                    holder.commentInput.setText(""); // Clear the input after submission
                } else {
                    Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    // Function to load comments from Firestore and display them
    private void loadComments(ViewHolder holder, LostAndFoundItem item) {
        String documentId = item.getTitle(); // Replace with unique ID if necessary

        if (documentId == null || documentId.isEmpty()) {
            Log.e("LoadComments", "Invalid document ID.");
            Toast.makeText(context, "Invalid item reference.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("lost_and_found")
                .document(documentId)
                .collection("comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(context, "Failed to load comments", Toast.LENGTH_SHORT).show();
                            Log.e("LoadComments", "Error loading comments: ", error);
                            return;
                        }

                        holder.commentContainer.removeAllViews();

                        if (value != null && !value.isEmpty()) {
                            int count = 0;
                            for (QueryDocumentSnapshot document : value) {
                                String commentText = document.getString("text");
                                if (commentText != null) {
                                    TextView commentView = new TextView(context);
                                    commentView.setText(commentText);
                                    holder.commentContainer.addView(commentView);
                                    count++;
                                }
                            }

                            holder.commentCount.setText(String.valueOf(count));
                        } else {
                            holder.commentCount.setText("0");
                        }
                    }
                });
    }

    // Function to upload the comment to Firestore
    private void uploadComment(LostAndFoundItem item, String commentText, ViewHolder holder) {
        Map<String, Object> comment = new HashMap<>();
        comment.put("text", commentText);
        comment.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("lost_and_found")
                .document(item.getTitle())
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Comment successfully added", Toast.LENGTH_SHORT).show();
                    loadComments(holder, item);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to add comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, location, commentCount, username;
        ImageView imageView, contactButton;
        EditText commentInput;
        ImageButton sendCommentButton; // Declare the send button
        ViewGroup commentContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_view_title);
            description = itemView.findViewById(R.id.text_view_description);
            location = itemView.findViewById(R.id.text_view_location);
            imageView = itemView.findViewById(R.id.image_view_item);
            contactButton = itemView.findViewById(R.id.button_contact);
            commentInput = itemView.findViewById(R.id.comment_input);
            sendCommentButton = itemView.findViewById(R.id.button_send_comment); // Initialize the send button
            commentCount = itemView.findViewById(R.id.text_view_comment_count);
            commentContainer = itemView.findViewById(R.id.comment_container);
            username = itemView.findViewById(R.id.username);
        }
    }

    // Method to update items
    public void updateItems(List<LostAndFoundItem> items) {
        this.itemList.clear();
        this.itemList.addAll(items);
        notifyDataSetChanged();
    }
}
