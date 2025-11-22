package com.example.armesseger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Added for a better user experience on request action

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    Context context;
    ArrayList<Users> requests;
    OnRequestActionListener listener;
    public boolean isSearchMode; // true = search, false = normal friend request mode

    public interface OnRequestActionListener {
        void onAccept(Users user);
        void onReject(Users user);
        void onSendRequest(Users user);
    }

    public FriendRequestAdapter(Context context, ArrayList<Users> requests,
                                OnRequestActionListener listener, boolean isSearchMode) {
        this.context = context;
        this.requests = requests;
        this.listener = listener;
        this.isSearchMode = isSearchMode;
    }

    public void updateList(ArrayList<Users> newList) {
        this.requests = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isSearchMode) {
            // friendsendingui should have tvName, profileImage, and sendButton
            view = LayoutInflater.from(context).inflate(R.layout.friendsendingui, parent, false);
        } else {
            // friendrequestuser should have tvName, profileImage, acceptButton, and rejectButton
            view = LayoutInflater.from(context).inflate(R.layout.friendrequestuser, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = requests.get(position);
        holder.tvName.setText(user.getUsername());

        if (user.getImageUrl() != null && !user.getImageUrl().equals("default")) {
            Picasso.get().load(user.getImageUrl()).into(holder.profileImage);
        }

        if (isSearchMode) {
            // SEARCH MODE: Show the user list and a 'Send' button if they aren't friends and no request is pending

            // Check if the current user is already friends or has already sent a request
            boolean hideSendButton = user.isFriend() || user.isHasSentRequest() || user.isHasReceivedRequest();

            if (holder.sendButton != null) {
                holder.sendButton.setVisibility(hideSendButton ? View.GONE : View.VISIBLE);

                if (holder.sendButton.getVisibility() == View.VISIBLE) {
                    holder.sendButton.setOnClickListener(v -> {
                        listener.onSendRequest(user);
                        // Optional: Give feedback and disable button immediately
                        Toast.makeText(context, "Request sent to " + user.getUsername(), Toast.LENGTH_SHORT).show();
                        holder.sendButton.setVisibility(View.GONE);
                    });
                }
            }

            // Hide request buttons in search mode
            if (holder.acceptButton != null) holder.acceptButton.setVisibility(View.GONE);
            if (holder.rejectButton != null) holder.rejectButton.setVisibility(View.GONE);

        } else {
            // FRIEND REQUEST MODE: Only display users who have sent a request to the current user

            // The list already only contains users with hasReceivedRequest = true
            if (holder.acceptButton != null) {
                holder.acceptButton.setVisibility(View.VISIBLE);
                holder.acceptButton.setOnClickListener(v -> listener.onAccept(user));
            }

            if (holder.rejectButton != null) {
                holder.rejectButton.setVisibility(View.VISIBLE);
                holder.rejectButton.setOnClickListener(v -> listener.onReject(user));
            }

            // Hide send button in request mode
            if (holder.sendButton != null) holder.sendButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView profileImage;
        // These can be null depending on the layout inflated (friendsendingui vs friendrequestuser)
        ImageView acceptButton, rejectButton, sendButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            profileImage = itemView.findViewById(R.id.profileImage);

            // Attempt to find all buttons, they will be null if the ID is not in the inflated layout
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            sendButton = itemView.findViewById(R.id.sendButton);
        }
    }
}