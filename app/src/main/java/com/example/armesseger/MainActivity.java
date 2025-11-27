package com.example.armesseger;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    RecyclerView mainRecyclerView;
    UserAdapter adapter;
    FirebaseDatabase database;
    ArrayList<Users> usersArrayList;
    EditText searchBar;
    ImageView friendRequestButton;
    String currentUserId;
    ImageView logout;
    private boolean doublebackexit = false;

    Uri photoUri;
    ImageView cameralogo;
    private static final int CAMERA_REQUEST_CODE = 1001;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, login.class));
            finish();
            return;
        }

        initCloudinary();

        mainRecyclerView = findViewById(R.id.mainrecyclerview);
        searchBar = findViewById(R.id.wholesearchbar);
        cameralogo = findViewById(R.id.cameralogo);
        friendRequestButton = findViewById(R.id.friendrequest1);
        logout = findViewById(R.id.logoutmain);

        currentUserId = auth.getCurrentUser().getUid();
        usersArrayList = new ArrayList<>();
        adapter = new UserAdapter(this, usersArrayList);

        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainRecyclerView.setAdapter(adapter);

        cameralogo.setOnClickListener(v -> openCamera());
        setupLogoutDialog();
        setupFriendRequestButton();
        loadFriendsOnly();
        setupSearchBar();
        updateFCMToken();
    }

    private void initCloudinary() {
        HashMap<String, String> config = new HashMap<>();
        config.put("cloud_name", "");
        config.put("api_key", "");
        config.put("api_secret", "");

        try {
            MediaManager.init(this, config);
        } catch (Exception ignored) {
        }
    }

    private void updateFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) return;
            String token = task.getResult();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("Tokens").child(userId);
            tokenRef.setValue(token);
        });
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (photoUri != null) {
                Intent intent = new Intent(MainActivity.this, SelectFriendActivity.class);
                intent.putExtra("imageUri", photoUri.toString());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doublebackexit) {
            finishAffinity();
            return;
        }
        this.doublebackexit = true;
        Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doublebackexit = false, 2000);
    }

    private void setupLogoutDialog() {
        logout.setOnClickListener(v -> {
            Dialog dialog = new Dialog(MainActivity.this, R.style.dialouge);
            dialog.setContentView(R.layout.dialog_layout);

            Button btnCancel = dialog.findViewById(R.id.btnCancel);
            Button btnLogout = dialog.findViewById(R.id.btnLogout);

            btnCancel.setOnClickListener(v1 -> dialog.dismiss());

            btnLogout.setOnClickListener(v12 -> {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, login.class));
                finish();
            });
            dialog.show();
        });
    }

    private void setupFriendRequestButton() {
        friendRequestButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Friendrequestui.class));
        });
    }

    private void loadFriendsOnly() {
        DatabaseReference reference = database.getReference().child("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user == null) continue;
                    String userId = dataSnapshot.getKey();
                    user.setUid(userId);
                    if (userId.equals(currentUserId)) continue;
                    DataSnapshot currentUserFriendsSnapshot = snapshot.child(currentUserId).child("friends");
                    boolean isFriend = currentUserFriendsSnapshot.child(userId).exists();
                    if (isFriend) {
                        user.setFriend(true);
                        usersArrayList.add(user);
                    }
                }

                adapter.updateList(usersArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
        });
    }

    private void filterList(String text) {
        ArrayList<Users> filtered = new ArrayList<>();
        for (Users user : usersArrayList) {
            if (user.getUsername() != null &&
                    user.getUsername().toLowerCase().contains(text.toLowerCase())) {
                filtered.add(user);
            }
        }
        adapter.updateList(filtered);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            DatabaseReference statusRef = database.getReference("users").child(currentUserId).child("status");
            statusRef.onDisconnect().setValue("offline");
            statusRef.setValue("online");
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (auth.getCurrentUser() != null) {
            database.getReference("users").child(currentUserId).child("status").setValue("offline");
            database.getReference("users").child(currentUserId).child("status").onDisconnect().cancel();
        }
    }
}