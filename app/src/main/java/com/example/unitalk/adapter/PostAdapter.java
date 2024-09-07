package com.example.unitalk.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.unitalk.ChatActivity;
import com.example.unitalk.ProfileActivity;
import com.example.unitalk.R;
import com.example.unitalk.Post;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {


    private List<Post> postList;
    private Context context;

    public PostAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.username.setText(post.getUsername());
        holder.description.setText(post.getDescription());
        holder.commentCount.setText(String.valueOf(post.getComments().size()));
        holder.likeCount.setText(String.valueOf(post.getLikeCount()));

        // Load the post image using Glide
        Glide.with(holder.itemView.getContext())
                .load(post.getImageUrl())
                .into(holder.imageView);

        // Check if the post is liked by the current user
        holder.isLiked = isPostLikedByUser(post);
        updateLikeIcon(holder, post);

        holder.username.setOnClickListener(v -> {
            String userId = post.getUserId(); // Check this value
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userId", userId);
            context.startActivity(intent);
        });

        // Like button click listener
        holder.likeIcon.setOnClickListener(v -> {
            holder.isLiked = !holder.isLiked;
            toggleLike(holder.isLiked, post, holder); // Pass the correct parameters
        });

        // Contact button listener
        holder.contactButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverUsername", post.getUsername());
            context.startActivity(intent);
        });

        // Comment adapter setup
        holder.commentAdapter = new CommentAdapter(new ArrayList<>());
        holder.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.commentsRecyclerView.setAdapter(holder.commentAdapter);
        loadComments(holder, post);

        // Send comment button listener
        holder.sendCommentButton.setOnClickListener(v -> {
            String commentText = holder.commentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                uploadComment(post, commentText, holder);
                holder.commentInput.setText("");
            } else {
                Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void toggleLike(boolean isLiked, Post post, ViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String documentId = post.getId();

        if (documentId == null || documentId.isEmpty()) {
            Log.e("ToggleLike", "Invalid document ID.");
            Toast.makeText(context, "Invalid post reference.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLiked) {
            // Increase the like count in Firestore and update the UI
            db.collection("posts")
                    .document(documentId)
                    .update("likeCount", FieldValue.increment(1))
                    .addOnSuccessListener(aVoid -> {
                        post.setLikeCount(post.getLikeCount() + 1);
                        holder.likeCount.setText(String.valueOf(post.getLikeCount()));
                        holder.likeIcon.setImageResource(R.drawable.ic_like_red);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update like count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Decrease the like count in Firestore and update the UI
            db.collection("posts")
                    .document(documentId)
                    .update("likeCount", FieldValue.increment(-1))
                    .addOnSuccessListener(aVoid -> {
                        post.setLikeCount(post.getLikeCount() - 1);
                        holder.likeCount.setText(String.valueOf(post.getLikeCount()));
                        holder.likeIcon.setImageResource(R.drawable.ic_like);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update like count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateLikeCount(ViewHolder holder, Post post, boolean isLiked) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String documentId = post.getId();

        if (documentId == null || documentId.isEmpty()) {
            Log.e("UpdateLikeCount", "Invalid document ID.");
            Toast.makeText(context, "Invalid post reference.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLiked) {
            db.collection("posts")
                    .document(documentId)
                    .update("likeCount", post.getLikeCount() + 1)
                    .addOnSuccessListener(aVoid -> {
                        holder.likeCount.setText(String.valueOf(post.getLikeCount() + 1));
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update like count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("posts")
                    .document(documentId)
                    .update("likeCount", post.getLikeCount() - 1)
                    .addOnSuccessListener(aVoid -> {
                        holder.likeCount.setText(String.valueOf(post.getLikeCount() - 1));
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update like count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadComments(ViewHolder holder, Post post) {
        String documentId = post.getId(); // Get the document ID from the Post object

        if (documentId == null || documentId.isEmpty()) {
            Log.e("LoadComments", "Invalid document ID.");
            Toast.makeText(context, "Invalid post reference.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("posts")
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

                        List<String> comments = new ArrayList<>();
                        if (value != null && !value.isEmpty()) {
                            for (QueryDocumentSnapshot document : value) {
                                String commentText = document.getString("text");
                                if (commentText != null) {
                                    comments.add(commentText);
                                }
                            }

                            holder.commentAdapter.comments.clear();
                            holder.commentAdapter.comments.addAll(comments);
                            holder.commentAdapter.notifyDataSetChanged();
                            holder.commentCount.setText(String.valueOf(comments.size()));
                        } else {
                            holder.commentCount.setText("0");
                        }
                    }
                });
    }

    private void uploadComment(Post post, String commentText, ViewHolder holder) {
        Map<String, Object> comment = new HashMap<>();
        comment.put("text", commentText);
        comment.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("posts")
                .document(post.getId())
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Comment successfully added", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to add comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View usernameTextView;
        TextView description, commentCount, username, likeCount;
        ImageView imageView, contactButton, likeIcon;
        EditText commentInput;
        ImageButton sendCommentButton;
        RecyclerView commentsRecyclerView;
        CommentAdapter commentAdapter;
        boolean isLiked; // Add this field

        public ViewHolder(View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.post_content);
            imageView = itemView.findViewById(R.id.post_image);
            contactButton = itemView.findViewById(R.id.button_contact);
            commentInput = itemView.findViewById(R.id.comment_input);
            sendCommentButton = itemView.findViewById(R.id.button_send_comment);
            commentCount = itemView.findViewById(R.id.comment_count);
            likeIcon = itemView.findViewById(R.id.like_icon);
            likeCount = itemView.findViewById(R.id.like_count);
            commentsRecyclerView = itemView.findViewById(R.id.comments_recycler_view);
            username = itemView.findViewById(R.id.post_username);
        }
    }


    public void updateItems(List<Post> items) {
        this.postList.clear();
        this.postList.addAll(items);
        notifyDataSetChanged();
    }

    private boolean isPostLikedByUser(Post post) {
        // Implement logic to check if the current user has liked the post
        // This can be done by checking a list of liked posts or a field in Firestore
        return false; // Placeholder
    }

    private void updateLikeIcon(ViewHolder holder, Post post) {
        // Here, you should determine if the post is liked or not
        // This example assumes a method `isPostLikedByCurrentUser(post)` which returns a boolean
        boolean isLiked = isPostLikedByCurrentUser(post);
        if (isLiked) {
            holder.likeIcon.setImageResource(R.drawable.ic_like_red);
        } else {
            holder.likeIcon.setImageResource(R.drawable.ic_like);
        }
    }
    private boolean isPostLikedByCurrentUser(Post post) {
        // Implement the logic to check if the current user has liked the post
        // This may involve checking a list of liked posts or querying Firestore
        return false; // Placeholder value
    }


}
