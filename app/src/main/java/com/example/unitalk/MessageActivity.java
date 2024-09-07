package com.example.unitalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.unitalk.adapter.MessageAdapter;
import com.example.unitalk.model.Chats;
import com.example.unitalk.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private String friendid, message, myid;
    private CircleImageView imageViewOnToolbar;
    private TextView usernameonToolbar;
    private Toolbar toolbar;
    private FirebaseUser firebaseUser;

    private EditText et_message;
    private Button send;

    private DatabaseReference reference;

    private List<Chats> chatsList;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    private ValueEventListener seenlistener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        toolbar = findViewById(R.id.toolbar_message);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageViewOnToolbar = findViewById(R.id.profile_image_toolbar_message);
        usernameonToolbar = findViewById(R.id.username_ontoolbar_message);

        recyclerView = findViewById(R.id.recyclerview_messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        send = findViewById(R.id.send_messsage_btn);
        et_message = findViewById(R.id.edit_message_text);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myid = firebaseUser != null ? firebaseUser.getUid() : null; // Current user's ID

        // Retrieve the friend ID from the Intent
        friendid = getIntent().getStringExtra("friendid");
        Log.d("MessageActivity", "Received friend ID: " + friendid);
        if (friendid == null || friendid.isEmpty()) {
            Toast.makeText(this, "Receiver not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        Log.d("MessageActivity", "friendid retrieved: " + friendid);

        // Fetch the friend's details from Firebase
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(friendid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    usernameonToolbar.setText(user.getUsername());
                    if ("default".equals(user.getImageURL())) {
                        imageViewOnToolbar.setImageResource(R.drawable.person_icon);
                    } else {
                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(imageViewOnToolbar);
                    }
                } else {
                    Toast.makeText(MessageActivity.this, "Sender data is invalid.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this, "Failed to retrieve sender.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        seenMessage(friendid);

        et_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                send.setEnabled(s.toString().length() > 0);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().startsWith(" ")) {
                    et_message.getText().insert(0, " ");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        send.setOnClickListener(v -> {
            message = et_message.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(myid, friendid, message);
                et_message.setText("");
            }
        });
    }

    private void seenMessage(final String friendid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");

        seenlistener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chats chats = ds.getValue(Chats.class);
                    if (chats != null && chats.getReceiver().equals(myid) && chats.getSender().equals(friendid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void readMessages(final String myid, final String friendid, final String imageURL) {
        chatsList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chats chats = ds.getValue(Chats.class);
                    if (chats != null && ((chats.getSender().equals(myid) && chats.getReceiver().equals(friendid)) ||
                            (chats.getSender().equals(friendid) && chats.getReceiver().equals(myid)))) {
                        chatsList.add(chats);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, chatsList, imageURL);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage(final String myid, final String friendid, final String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myid);
        hashMap.put("receiver", friendid);
        hashMap.put("message", message);
        hashMap.put("isseen", false);

        reference.child("Chats").push().setValue(hashMap);

        DatabaseReference chatListReference = FirebaseDatabase.getInstance().getReference("Chatslist").child(myid).child(friendid);
        chatListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatListReference.child("id").setValue(friendid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateStatus(final String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateStatus("offline");
        if (reference != null && seenlistener != null) {
            reference.removeEventListener(seenlistener);
        }
    }
}
