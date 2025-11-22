package com.example.armesseger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        mainRecyclerView = findViewById(R.id.mainrecyclerview);
        searchBar = findViewById(R.id.wholesearchbar);
        friendRequestButton = findViewById(R.id.friendrequest1);

        logout = findViewById(R.id.logoutmain);
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, login.class));
            finish();
            return;
        }

        currentUserId = auth.getCurrentUser().getUid();
        usersArrayList = new ArrayList<>();
        adapter = new UserAdapter(this, usersArrayList);

        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainRecyclerView.setAdapter(adapter);

        setupFriendRequestButton();
        loadFriendsOnly(); // load only friends
        setupSearchBar();
    }

    private void setupFriendRequestButton() {
        friendRequestButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Friendrequestui.class));
        });
    }

    // Load only friends of current user
    private void loadFriendsOnly() {
        DatabaseReference reference = database.getReference().child("users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user == null) continue;

                    user.setUid(dataSnapshot.getKey());

                    // Skip current user
                    if (user.getUid().equals(currentUserId)) continue;

                    // Check if this user is a friend
                    DataSnapshot friendsSnapshot = dataSnapshot.child("friends");
                    boolean isFriend = friendsSnapshot.exists() &&
                            friendsSnapshot.child(currentUserId).exists();

                    if (isFriend) {
                        user.setFriend(true);
                        usersArrayList.add(user);
                    }
                }

                adapter.updateList(usersArrayList); // refresh friend-only list
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

    // Filter search to only show friends
    private void filterList(String text) {
        ArrayList<Users> filteredList = new ArrayList<>();

        for (Users user : usersArrayList) {
            if (user.getUsername() != null &&
                    user.getUsername().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(user);
            }
        }

        adapter.updateList(filteredList);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            database.getReference("users")
                    .child(auth.getCurrentUser().getUid())
                    .child("status")
                    .setValue("online");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (auth.getCurrentUser() != null) {
            database.getReference("users")
                    .child(auth.getCurrentUser().getUid())
                    .child("status")
                    .setValue("offline");
        }
    }
}
