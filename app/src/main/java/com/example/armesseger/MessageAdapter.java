package com.example.armesseger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        // Ensuring receiverImage is never null
        this.receiverImage = receiverImage != null ? receiverImage : "";
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel message = messageList.get(position);
        // Using a local variable for current user ID check for clarity
        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        if (currentUid.equals(message.getSenderUid())) {
            return ITEM_SENDER;
        } else {
            return ITEM_RECEIVER;
        }
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
            ((SenderViewHolder) holder).messageText.setText(message.getMessage());
        } else if (holder instanceof ReceiverViewHolder) {
            ReceiverViewHolder receiverHolder = (ReceiverViewHolder) holder;
            receiverHolder.messageText.setText(message.getMessage());

            if (!receiverImage.isEmpty()) {
                Picasso.get().load(receiverImage).placeholder(R.drawable.maledefault).into(receiverHolder.profileImage);
            } else {
                receiverHolder.profileImage.setImageResource(R.drawable.maledefault);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.sender_message_text);
        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView messageText;

        ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.receiver_profile_image);
            messageText = itemView.findViewById(R.id.receiver_message_text);
        }
    }
}