package com.example.armesseger;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

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

public class Friendrequestui extends AppCompatActivity {

    RecyclerView recyclerView;
    FriendRequestAdapter adapter;
    ArrayList<Users> userList; // All users
    ArrayList<Users> filteredList; // Display list (Requests or Search results)
    EditText searchBar;
    DatabaseReference database;
    String currentUserId;

    // Hold the current user's relationships for efficient filtering/status setting
    DataSnapshot currentUserDataSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendrequestui);

        recyclerView = findViewById(R.id.friendRequestRecyclerView);
        searchBar = findViewById(R.id.search_bar);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference().child("users");

        userList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new FriendRequestAdapter(
                this,
                filteredList,
                new FriendRequestAdapter.OnRequestActionListener() {
                    @Override
                    public void onAccept(Users user) { acceptRequest(user); }

                    @Override
                    public void onReject(Users user) { rejectRequest(user); }

                    @Override
                    public void onSendRequest(Users user) { sendFriendRequest(user); }
                },
                false
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadAllUsers();
        setupSearchBar();
    }

    private void loadAllUsers() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                filteredList.clear();

                // --- Step 1: Get Current User's Relationships ---
                currentUserDataSnapshot = snapshot.child(currentUserId);
                DataSnapshot myReceivedRequests = currentUserDataSnapshot.child("receivedRequests");
                DataSnapshot mySentRequests = currentUserDataSnapshot.child("sentRequests");
                DataSnapshot myFriends = currentUserDataSnapshot.child("friends");

                for (DataSnapshot data : snapshot.getChildren()) {
                    Users user = data.getValue(Users.class);
                    if (user == null) continue;

                    user.setUid(data.getKey());

                    if (user.getUid().equals(currentUserId)) continue;

                    // --- Step 2: Set relationship flags from Current User's perspective ---

                    // Has this user sent a request to me? (I have received it)
                    user.setHasReceivedRequest(myReceivedRequests.exists() &&
                            myReceivedRequests.child(user.getUid()).exists());

                    // Have I sent a request to this user?
                    user.setHasSentRequest(mySentRequests.exists() &&
                            mySentRequests.child(user.getUid()).exists());

                    // Is this user my friend?
                    user.setFriend(myFriends.exists() &&
                            myFriends.child(user.getUid()).exists());

                    // --- Step 3: Initial filtering for Friend Requests UI (only show received requests) ---
                    if (user.isHasReceivedRequest()) filteredList.add(user);

                    userList.add(user); // Add all users for search functionality
                }

                adapter.isSearchMode = false;
                adapter.updateList(filteredList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().toLowerCase();
                filteredList.clear();

                if (!text.isEmpty()) {
                    adapter.isSearchMode = true;

                    for (Users user : userList) {
                        if (user.getUsername() != null &&
                                user.getUsername().toLowerCase().contains(text)) {
                            filteredList.add(user);
                        }
                    }
                } else {
                    adapter.isSearchMode = false;

                    // When search is cleared, revert to showing only received requests
                    for (Users user : userList) {
                        if (user.isHasReceivedRequest()) filteredList.add(user);
                    }
                }

                adapter.updateList(filteredList);
            }
        });
    }

    private void sendFriendRequest(Users user) {
        // Current user sends request
        database.child(currentUserId).child("sentRequests").child(user.getUid()).setValue(true);
        // Target user receives request
        database.child(user.getUid()).child("receivedRequests").child(currentUserId).setValue(true);
    }

    private void acceptRequest(Users user) {
        // Add to friends list for both users
        database.child(currentUserId).child("friends").child(user.getUid()).setValue(true);
        database.child(user.getUid()).child("friends").child(currentUserId).setValue(true);

        // Remove from received/sent requests
        database.child(currentUserId).child("receivedRequests").child(user.getUid()).removeValue();
        database.child(user.getUid()).child("sentRequests").child(currentUserId).removeValue();
    }

    private void rejectRequest(Users user) {
        // Remove from received/sent requests
        database.child(currentUserId).child("receivedRequests").child(user.getUid()).removeValue();
        database.child(user.getUid()).child("sentRequests").child(currentUserId).removeValue();
    }
}