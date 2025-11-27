package com.example.armesseger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendSelectAdapter extends RecyclerView.Adapter<FriendSelectAdapter.ViewHolder> {

    public interface OnFriendClickListener {
        void onFriendClick(String friendId);
    }

    private ArrayList<Users> users;
    private Context context;
    private OnFriendClickListener listener;

    public FriendSelectAdapter(ArrayList<Users> users, Context context, OnFriendClickListener listener) {
        this.users = users;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendSelectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.friend_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendSelectAdapter.ViewHolder holder, int position) {
        Users user = users.get(position);

        holder.friendName.setText(user.getUsername());

        if (user.getImageUrl() == null || user.getImageUrl().equals("default") || user.getImageUrl().equals("no_image")) {
            holder.friendProfileImage.setImageResource(R.drawable.maledefault);
        } else {
            Picasso.get()
                    .load(user.getImageUrl())
                    .placeholder(R.drawable.maledefault)
                    .into(holder.friendProfileImage);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendClick(user.getUid());
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView friendProfileImage;
        TextView friendName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            friendProfileImage = itemView.findViewById(R.id.friendProfileImage);
            friendName = itemView.findViewById(R.id.friendName);
        }
    }
}
