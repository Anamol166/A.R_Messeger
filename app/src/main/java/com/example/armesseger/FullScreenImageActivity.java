package com.example.armesseger;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class FullScreenImageActivity extends AppCompatActivity {
    public static final String IMAGE_URL = "image_url";
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_full_screen_image);
        ImageView iv = findViewById(R.id.fullscreen_image);
        String url = getIntent().getStringExtra(IMAGE_URL);
        if (url != null && !url.isEmpty()) Picasso.get().load(url).into(iv);
        iv.setOnClickListener(v -> finish());
    }
}
