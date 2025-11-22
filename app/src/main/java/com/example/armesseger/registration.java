package com.example.armesseger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class registration extends AppCompatActivity {

    EditText username, email, password, confirm_password;
    Button Signup;
    CircleImageView profile;
    TextView login;

    FirebaseAuth auth;
    FirebaseDatabase database;
    Uri imageurl;
    String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Signup = findViewById(R.id.button_signup);
        username = findViewById(R.id.input_username);
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        confirm_password = findViewById(R.id.input_password_confirm);
        profile = findViewById(R.id.profilerg);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        initCloudinary();

        profile.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
        });

        Signup.setOnClickListener(v -> signupUser());
    }

    private void initCloudinary() {
        HashMap config = new HashMap();
        config.put("cloud_name", "dxy0ywfqp");
        config.put("api_key", "974368724742381");
        config.put("api_secret", "m86UxhYefgUyOm-u69eTZfSDjPE");

        try {
            MediaManager.init(this, config);
        } catch (Exception e) {}
    }

    private void signupUser() {

        String name = username.getText().toString();
        String mail = email.getText().toString();
        String pass = password.getText().toString();
        String cpass = confirm_password.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(mail) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(cpass)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mail.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
            Toast.makeText(this, "Invalid email!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(cpass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String id = task.getResult().getUser().getUid();
                        DatabaseReference reference = database.getReference().child("users").child(id);

                        if (imageurl != null) {
                            uploadToCloudinary(imageurl, name, mail, reference, pass);
                        } else {
                            // DEFAULT IMAGE + STATUS FIELD
                            HashMap<String, Object> user = new HashMap<>();
                            user.put("username", name);
                            user.put("email", mail);
                            user.put("password", pass);
                            user.put("imageUrl", "https://res.cloudinary.com/dxy0ywfqp/image/upload/v1763741711/1000115750_jpivhz.png");   // <--- default picture
                            user.put("status", "offline");      // <--- status added

                            saveUserToDB(reference, user);
                        }

                    } else {
                        Toast.makeText(this, "Signup failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadToCloudinary(Uri imageUri, String name, String mail,
                                    DatabaseReference reference, String pass) {

        MediaManager.get().upload(imageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) { }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        image = resultData.get("secure_url").toString();

                        HashMap<String, Object> user = new HashMap<>();
                        user.put("username", name);
                        user.put("email", mail);
                        user.put("password", pass);
                        user.put("imageUrl", image);
                        user.put("status", "offline"); // default OFFLINE

                        saveUserToDB(reference, user);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(registration.this, "Image Upload Failed!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { }
                }).dispatch();
    }

    private void saveUserToDB(DatabaseReference reference, HashMap<String, Object> user) {
        reference.setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                startActivity(new Intent(registration.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && data != null) {
            imageurl = data.getData();
            profile.setImageURI(imageurl);
        }
    }
}
