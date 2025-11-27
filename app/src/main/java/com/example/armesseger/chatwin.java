package com.example.armesseger;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatwin extends AppCompatActivity {

    private CircleImageView receiverProfileImage, sendButton, galleryButton;
    private TextView username, userStatus;
    private EditText messageBox;
    private RecyclerView messageList;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private String receiverUid, receiverName, receiverImage, senderUid;
    private String senderRoom, receiverRoom;

    private ArrayList<MessageModel> messageArrayList;
    private MessageAdapter messageAdapter;
    private ImageView backarrow;

    private static final int PICK_IMAGES_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatwin);

        receiverProfileImage = findViewById(R.id.profile_image);
        sendButton = findViewById(R.id.send_button);
        galleryButton = findViewById(R.id.gallery_button);
        messageBox = findViewById(R.id.message_input);
        messageList = findViewById(R.id.messages_recycler);
        backarrow = findViewById(R.id.backarrow);

        username = findViewById(R.id.username);
        userStatus = findViewById(R.id.user_status);

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

        username.setText(receiverName);
        userStatus.setText("Online");

        if (receiverImage != null && !receiverImage.isEmpty())
            Picasso.get().load(receiverImage).placeholder(R.drawable.maledefault).into(receiverProfileImage);
        else
            receiverProfileImage.setImageResource(R.drawable.maledefault);

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        messageArrayList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageArrayList, receiverImage);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messageList.setLayoutManager(layoutManager);
        messageList.setAdapter(messageAdapter);

        loadMessages();
        initCloudinary();

        backarrow.setOnClickListener(v -> chat());
        sendButton.setOnClickListener(v -> sendMessage());
        galleryButton.setOnClickListener(v -> openGallery());
        DatabaseReference statusRef = database.getReference("Users")
                .child(receiverUid)
                .child("status");

        statusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if (status == null || status.isEmpty()) {
                    userStatus.setText("Offline");
                    userStatus.setTextColor(Color.GRAY);
                } else {
                    userStatus.setText(status);
                    if (status.equalsIgnoreCase("online")) {
                        userStatus.setTextColor(Color.GREEN);
                    } else {
                        userStatus.setTextColor(Color.GRAY);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void chat() {
        startActivity(new Intent(chatwin.this, MainActivity.class));
        finish();
    }

    private void loadMessages() {
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
                Toast.makeText(chatwin.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initCloudinary() {
        HashMap config = new HashMap();
        config.put("cloud_name", "");
        config.put("api_key", "");
        config.put("api_secret", "");

        try {
            MediaManager.init(this, config);
        } catch (Exception ignored) {}
    }


    private void sendMessage() {
        String messageText = messageBox.getText().toString().trim();
        if (messageText.isEmpty()) return;

        messageBox.setText("");
        long timestamp = new Date().getTime();

        MessageModel message = new MessageModel(messageText, senderUid, timestamp);

        DatabaseReference senderRef = database.getReference("chats")
                .child(senderRoom)
                .child("Messages");

        senderRef.push().setValue(message).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DatabaseReference receiverRef = database.getReference("chats")
                        .child(receiverRoom)
                        .child("Messages");

                receiverRef.push().setValue(message);
                sendNotification(receiverUid, receiverName, messageText);
            }
        });
    }
    private void sendImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        long timestamp = System.currentTimeMillis();
        MessageModel message = new MessageModel(imageUrl, senderUid, timestamp, true);

        database.getReference("chats").child(senderRoom).child("Messages").push().setValue(message);
        database.getReference("chats").child(receiverRoom).child("Messages").push().setValue(message).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendNotification(receiverUid, receiverName, "sent a photo");
            }
        });
    }


    private void sendNotification(String receiverId, String title, String message) {
        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("Tokens").child(receiverId);
        String senderUsername = username.getText().toString();

        tokenRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String token = snapshot.getValue(String.class);
                    final String FCM_SERVER_KEY = "";
                    new Thread(() -> {
                        try {
                            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                            okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json; charset=utf-8");
                            String json = "{"
                                    + "\"to\":\"" + token + "\","
                                    + "\"notification\":{"
                                    + "\"title\":\"New message from " + senderUsername + "\","
                                    + "\"body\":\"" + message + "\","
                                    + "\"sound\":\"default\""
                                    + "},"
                                    + "\"data\":{"
                                    + "\"title\":\"" + senderUsername + "\","
                                    + "\"message\":\"" + message + "\","
                                    + "\"senderId\":\"" + senderUid + "\""
                                    + "}"
                                    + "}";

                            okhttp3.RequestBody body = okhttp3.RequestBody.create(json, mediaType);
                            okhttp3.Request request = new okhttp3.Request.Builder()
                                    .url("https://fcm.googleapis.com/fcm/send")
                                    .post(body)
                                    .addHeader("Authorization", "key=" + FCM_SERVER_KEY)
                                    .addHeader("Content-Type", "application/json")
                                    .build();

                            okhttp3.Response response = client.newCall(request).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();

                for (int i = 0; i < count; i++) {
                    Uri imgUri = data.getClipData().getItemAt(i).getUri();
                    sendImage(imgUri.toString());
                }

            } else if (data.getData() != null) {
                Uri imgUri = data.getData();
                sendImage(imgUri.toString());
            }
        }
    }

}
