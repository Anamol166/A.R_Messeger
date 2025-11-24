package com.example.armesseger;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Added import for clarity
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatwin extends AppCompatActivity {

    private CircleImageView receiverProfileImage, sendButton;
    private TextView receiverNameText, receiverStatusText;
    private EditText messageBox;
    private RecyclerView messageList;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private String receiverUid, receiverName, receiverImage, senderUid;
    private String senderRoom, receiverRoom;

    private ArrayList<MessageModel> messageArrayList;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatwin);

        receiverProfileImage = findViewById(R.id.profile_image);
        sendButton = findViewById(R.id.send_button);
        messageBox = findViewById(R.id.message_input);
        messageList = findViewById(R.id.messages_recycler);
        receiverNameText = findViewById(R.id.header_name);
        receiverStatusText = findViewById(R.id.status);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        senderUid = currentUser.getUid();

        receiverUid = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("username");
        receiverImage = getIntent().getStringExtra("profile_image");

        receiverNameText.setText(receiverName != null ? receiverName : "User");
        receiverStatusText.setText("Online");

        if (receiverImage != null && !receiverImage.isEmpty())
            Picasso.get().load(receiverImage).placeholder(R.drawable.maledefault).into(receiverProfileImage);
        else
            receiverProfileImage.setImageResource(R.drawable.maledefault);
        if (receiverUid == null || receiverUid.isEmpty()) {
            Toast.makeText(this, "Receiver ID is missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        messageArrayList = new ArrayList<>();

        messageAdapter = new MessageAdapter(this, messageArrayList, receiverImage);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messageList.setLayoutManager(layoutManager);
        messageList.setAdapter(messageAdapter);

        loadMessages();

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        if (senderRoom == null) return;

        DatabaseReference messageRef = database.getReference("chats")
                .child(senderRoom)
                .child("Messages");

        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MessageModel message = ds.getValue(MessageModel.class);
                    if (message != null) messageArrayList.add(message);
                }
                messageAdapter.notifyDataSetChanged();
                if (!messageArrayList.isEmpty())
                    messageList.scrollToPosition(messageArrayList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(chatwin.this, "Failed to load messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = messageBox.getText().toString().trim();
        if (messageText.isEmpty() || senderRoom == null || receiverRoom == null || senderUid == null) return;

        messageBox.setText("");
        long timestamp = new Date().getTime();
        MessageModel message = new MessageModel(messageText, senderUid, timestamp);

        //Push to senderRoom
        DatabaseReference senderRef = database.getReference("chats")
                .child(senderRoom)
                .child("Messages");

        senderRef.push().setValue(message).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //Push to receiverRoom
                DatabaseReference receiverRef = database.getReference("chats")
                        .child(receiverRoom)
                        .child("Messages");
                receiverRef.push().setValue(message);
            } else {
                Toast.makeText(chatwin.this, "Failed to send message: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}