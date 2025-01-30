package edu.northeastern.musicmilesgroup17;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginButton, createAccountButton;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", null);
        String password = prefs.getString("password", null);
        String accessToken = prefs.getString("accessToken", null);

        if (username != null && password != null) {
            // Auto-login if credentials are stored
            navigateToHome(accessToken != null, accessToken);
            return;
        }

        setContentView(R.layout.activity_main);

        // Initialize UI elements
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        createAccountButton = findViewById(R.id.create_account_button);

        // Firebase reference
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            String usernameIn = usernameInput.getText().toString().trim();
            String passwordIn = passwordInput.getText().toString().trim();
            if (!usernameIn.isEmpty() && !passwordIn.isEmpty()) {
                validateUser(usernameIn, passwordIn);
            } else {
                showToast("Please enter a username and password");
            }
        });

        // Handle create account button click
        createAccountButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CreateUserActivity.class)));
    }

    private void validateUser(String username, String password) {
        Log.d("Login", "Validating user: " + username);

        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String savedPassword = snapshot.child("password").getValue(String.class);
                    String savedAccessToken = snapshot.child("accessToken").getValue(String.class);

                    if (savedPassword != null && savedPassword.equals(password)) {
                        Log.d("Login", "Password matched for user: " + username);
                        saveUserToPreferences(username, password, savedAccessToken);
                        navigateToHome(savedAccessToken != null, savedAccessToken);
                    } else {
                        showToast("Invalid password. Please try again.");
                    }
                } else {
                    showToast("User does not exist. Please create an account.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database error: " + error.getMessage());
                showToast("Error accessing database. Please try again.");
            }
        });
    }

    private void saveUserToPreferences(String username, String password, String accessToken) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        if (accessToken != null) {
            editor.putString("accessToken", accessToken);
        }
        editor.apply();
    }

    private void navigateToHome(boolean isSpotifyLogin, String accessToken) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra("isSpotifyLogin", isSpotifyLogin);
        if (accessToken != null) {
            intent.putExtra("accessToken", accessToken);
        }
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
