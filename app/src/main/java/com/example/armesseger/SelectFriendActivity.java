package com.example.armesseger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelectFriendActivity extends AppCompatActivity {
    private RecyclerView friendList;
    private ArrayList<Users> users;
    private FriendSelectAdapter adapter;
    private Uri imageUri;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);
        initCloudinary();

        friendList = findViewById(R.id.friendList);
        users = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        currentUserId = auth.getUid();
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString == null) {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        imageUri = Uri.parse(imageUriString);

        adapter = new FriendSelectAdapter(users, this, this::sendImageToFriend);
        friendList.setLayoutManager(new LinearLayoutManager(this));
        friendList.setAdapter(adapter);

        loadFriends();
    }

    private void initCloudinary() {
        HashMap<String, String> config = new HashMap<>();
        config.put("cloud_name", "");
        config.put("api_key", "");
        config.put("api_secret", "");

        try {
            MediaManager.init(this, config);
        } catch (Exception ignored) {}
    }

    private void loadFriends() {
        database.getReference().child("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        users.clear();

                        DataSnapshot friendsMapSnapshot = snapshot.child(currentUserId).child("friends");

                        if (!friendsMapSnapshot.exists()) {
                            adapter.notifyDataSetChanged();
                            Toast.makeText(SelectFriendActivity.this, "You have no friends yet.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DataSnapshot friendSnap : friendsMapSnapshot.getChildren()) {
                            String friendId = friendSnap.getKey();

                            Users friend = snapshot.child(friendId).getValue(Users.class);
                            if (friend != null) {
                                friend.setUid(friendId);
                                users.add(friend);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(SelectFriendActivity.this, "Failed to load friends.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendImageToFriend(String friendId) {
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_LONG).show();

        MediaManager.get().upload(imageUri)
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) { }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        if (imageUrl == null) {
                            Toast.makeText(SelectFriendActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        sendMessageToFirebase(friendId, imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(SelectFriendActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { }
                })
                .dispatch();
    }

    private void sendMessageToFirebase(String friendId, String imageUrl) {
        String senderRoom = currentUserId + friendId;
        String receiverRoom = friendId + currentUserId;

        String key = database.getReference().push().getKey();
        long timestamp = System.currentTimeMillis();

        MessageModel msg = new MessageModel(imageUrl, currentUserId, timestamp, true);

        DatabaseReference senderRef = database.getReference()
                .child("chats").child(senderRoom).child("Messages").child(key);
        DatabaseReference receiverRef = database.getReference()
                .child("chats").child(receiverRoom).child("Messages").child(key);

        senderRef.setValue(msg).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                receiverRef.setValue(msg).addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        Toast.makeText(SelectFriendActivity.this, "Image Sent!", Toast.LENGTH_SHORT).show();

                        Users friend = getFriendById(friendId);
                        if (friend != null) {
                            Intent intent = new Intent(SelectFriendActivity.this, chatwin.class);
                            intent.putExtra("userId", friend.getUid());
                            intent.putExtra("username", friend.getUsername());
                            intent.putExtra("profile_image", friend.getImageUrl());
                            startActivity(intent);
                        }
                        finish();
                    } else {
                        Toast.makeText(SelectFriendActivity.this, "Failed to send to receiver", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(SelectFriendActivity.this, "Failed to send to sender", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Users getFriendById(String friendId) {
        for (Users u : users) {
            if (friendId.equals(u.getUid())) return u;
        }
        return null;
    }
}