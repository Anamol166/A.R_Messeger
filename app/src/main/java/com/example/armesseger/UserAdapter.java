package com.example.armesseger;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    MainActivity mainActivity;
    ArrayList<Users> usersArrayList;

    public UserAdapter(MainActivity mainActivity, ArrayList<Users> usersArrayList) {
        this.mainActivity = mainActivity;
        this.usersArrayList = usersArrayList;
    }
    public void updateList(ArrayList<Users> list) {
        this.usersArrayList = list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mainActivity)
                .inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = usersArrayList.get(position);
        holder.tvName.setText(user.getUsername());

        String status = user.getStatus();
        if (status == null) status = "offline";

        holder.tvStatus.setText(status);
        if ("online".equals(status)) {
            holder.tvStatus.setTextColor(Color.GREEN);
        } else {
            holder.tvStatus.setTextColor(Color.GRAY);
        }

        if (user.getImageUrl() == null ||
                user.getImageUrl().equals("default") ||
                user.getImageUrl().equals("no_image")) {

            holder.profileImage.setImageResource(R.drawable.maledefault);

        } else {
            Picasso.get()
                    .load(user.getImageUrl())
                    .placeholder(R.drawable.maledefault)
                    .into(holder.profileImage);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mainActivity, chatwin.class);
            intent.putExtra("userId", user.getUid());
            intent.putExtra("username", user.getUsername());
            intent.putExtra("status",user.getStatus());
            intent.putExtra("profile_image", user.getImageUrl());
            mainActivity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView tvName, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}