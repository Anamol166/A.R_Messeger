package com.example.armesseger;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class FullScreenImageActivity extends AppCompatActivity {

    public static final String IMAGE_URL = "image_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        ImageView fullScreenImage = findViewById(R.id.fullscreen_image);

        String imageUrl = getIntent().getStringExtra(IMAGE_URL);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(fullScreenImage);
        }

        fullScreenImage.setOnClickListener(v -> finish());
    }
}
