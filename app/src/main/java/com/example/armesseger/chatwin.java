package com.example.armesseger;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
public class chatwin extends AppCompatActivity {
    String receivername , receiveruid , receiverimage , senderuid , receiver_status;
    CircleImageView receiver_profile_image;
    TextView receiver_name;
    TextView receiverstatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatwin);
        receivername = getIntent().getStringExtra("username");
        receiveruid = getIntent().getStringExtra("userId");
        receiverimage = getIntent().getStringExtra("profile_image");
        receiver_status = getIntent().getStringExtra("status");

        receiver_profile_image = findViewById(R.id.profile_image);
        receiver_name = findViewById(R.id.header_name);
        receiverstatus = findViewById(R.id.status);
        Picasso.get().load(receiverimage).into(receiver_profile_image);
        receiver_name.setText(receivername);
        receiverstatus.setText(receiver_status);
    }
}