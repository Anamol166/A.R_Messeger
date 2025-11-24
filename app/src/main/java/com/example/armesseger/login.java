package com.example.armesseger;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {
    Button button;
    EditText email, password;
    FirebaseAuth auth;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    TextView Signup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.Loginbutton);
        email = findViewById(R.id.editTextLogEmail);
        password = findViewById(R.id.editTextLogPassword);
        Signup = findViewById(R.id.textsignup);
        Signup.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, registration.class);
            startActivity(intent);
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = email.getText().toString();
                String Password = password.getText().toString();
                if (TextUtils.isEmpty(Email) || TextUtils.isEmpty(Password)) {
                    Toast.makeText(login.this, "Please enter email or password", Toast.LENGTH_LONG).show();
                } else if (!Email.matches(emailPattern)) {
                    Toast.makeText(login.this, "Please enter valid email", Toast.LENGTH_LONG).show();
                } else if (password.length() < 8) {
                    Toast.makeText(login.this, "Password must be at least 8 characters", Toast.LENGTH_LONG).show();
            } else {
                    auth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                try {
                                    Intent intent = new Intent(login.this, MainActivity.class);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(login.this, "Check your email or password", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(login.this,"Check your email or password", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }

        });
    }
}