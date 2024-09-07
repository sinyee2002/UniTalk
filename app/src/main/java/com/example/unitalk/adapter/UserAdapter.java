package com.example.unitalk.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.unitalk.MessageActivity;
import com.example.unitalk.R;
import com.example.unitalk.model.Chats;
import com.example.unitalk.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyHolder> {

    private Context context;
    private List<User> userList;
    private boolean isChat;
    private String lastMessage;
    private FirebaseUser firebaseUser;

    public UserAdapter(Context context, List<User> userList, boolean isChat) {
        this.context = context;
        this.userList = userList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layoutofusers, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        User user = userList.get(position);
        String friendId = user.getId();

        // Bind user data to the ViewHolder
        holder.username.setText(user.getUsername()); // Ensure these methods exist in your User class
        holder.lastMessage.setText(user.getStatus()); // Adjust based on what you need to display



        // Display the last message if required
        if (isChat) {
            displayLastMessage(friendId, holder.lastMessage);
        } else {
            holder.lastMessage.setVisibility(View.GONE);
        }

        // Show online/offline status if isChat is true
        if (isChat) {
            if (user.getStatus().equals("online")) {
                holder.imageOn.setVisibility(View.VISIBLE);
                holder.imageOff.setVisibility(View.GONE);
            } else {
                holder.imageOn.setVisibility(View.GONE);
                holder.imageOff.setVisibility(View.VISIBLE);
            }
        } else {
            holder.imageOn.setVisibility(View.GONE);
            holder.imageOff.setVisibility(View.GONE);
        }

        // Set up click listener for each item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("friendId", friendId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        TextView username, lastMessage;
        CircleImageView imageView, imageOn, imageOff;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username_userfrag); // Ensure these IDs exist in your layout
            imageView = itemView.findViewById(R.id.image_user_userfrag);
            imageOn = itemView.findViewById(R.id.image_online);
            imageOff = itemView.findViewById(R.id.image_offline);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }

    private void displayLastMessage(final String friendId, final TextView lastMessageTextView) {
        lastMessage = "default";
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chats chat = ds.getValue(Chats.class);
                    if (firebaseUser != null && chat != null) {
                        if ((chat.getSender().equals(friendId) && chat.getReceiver().equals(firebaseUser.getUid())) ||
                                (chat.getSender().equals(firebaseUser.getUid()) && chat.getReceiver().equals(friendId))) {
                            lastMessage = chat.getMessage();
                        }
                    }
                }

                switch (lastMessage) {
                    case "default":
                        lastMessageTextView.setText("No message");
                        break;
                    default:
                        lastMessageTextView.setText(lastMessage);
                        break;
                }
                lastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }
}
