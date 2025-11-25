package com.example.armesseger;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final ArrayList<MessageModel> messageList;
    private final String receiverImage;
    private static final int ITEM_SENDER = 1;
    private static final int ITEM_RECEIVER = 2;

    public MessageAdapter(Context context, ArrayList<MessageModel> messageList, String receiverImage) {
        this.context = context;
        this.messageList = messageList;
        this.receiverImage = receiverImage != null ? receiverImage : "";
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel message = messageList.get(position);
        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        return currentUid.equals(message.getSenderUid()) ? ITEM_SENDER : ITEM_RECEIVER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENDER) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.receiver_layout, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel message = messageList.get(position);

        if (holder instanceof SenderViewHolder) {
            SenderViewHolder senderHolder = (SenderViewHolder) holder;

            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                senderHolder.messageText.setVisibility(View.GONE);
                senderHolder.messageImage.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getImageUrl()).into(senderHolder.messageImage);

                senderHolder.messageImage.setOnClickListener(v -> {
                    Intent intent = new Intent(context, FullScreenImageActivity.class);
                    intent.putExtra(FullScreenImageActivity.IMAGE_URL, message.getImageUrl());
                    context.startActivity(intent);
                });
            } else {
                senderHolder.messageText.setVisibility(View.VISIBLE);
                senderHolder.messageImage.setVisibility(View.GONE);
                senderHolder.messageText.setText(message.getMessage());
            }

        } else if (holder instanceof ReceiverViewHolder) {
            ReceiverViewHolder receiverHolder = (ReceiverViewHolder) holder;

            if (!receiverImage.isEmpty())
                Picasso.get().load(receiverImage).placeholder(R.drawable.maledefault).into(receiverHolder.profileImage);
            else
                receiverHolder.profileImage.setImageResource(R.drawable.maledefault);

            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                receiverHolder.messageText.setVisibility(View.GONE);
                receiverHolder.messageImage.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getImageUrl()).into(receiverHolder.messageImage);

                receiverHolder.messageImage.setOnClickListener(v -> {
                    Intent intent = new Intent(context, FullScreenImageActivity.class);
                    intent.putExtra(FullScreenImageActivity.IMAGE_URL, message.getImageUrl());
                    context.startActivity(intent);
                });
            } else {
                receiverHolder.messageText.setVisibility(View.VISIBLE);
                receiverHolder.messageImage.setVisibility(View.GONE);
                receiverHolder.messageText.setText(message.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView messageImage;

        SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.sender_message_text);
            messageImage = itemView.findViewById(R.id.sender_message_image);
        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView messageText;
        ImageView messageImage;

        ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.receiver_profile_image);
            messageText = itemView.findViewById(R.id.receiver_message_text);
            messageImage = itemView.findViewById(R.id.receiver_message_image);
        }
    }
}
