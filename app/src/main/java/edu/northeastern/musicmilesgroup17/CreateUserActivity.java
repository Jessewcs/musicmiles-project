package edu.northeastern.musicmilesgroup17;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.util.HashMap;
import java.util.Map;

public class CreateUserActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "c300b742f15643d8b1d70998dc0a4e02";
    private static final String REDIRECT_URI = "musicmiles://callback/";

    private EditText usernameInput, fullNameInput, passwordInput;
    private Button createButton, backButton, spotifyRegisterButton;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        // Initialize UI elements
        usernameInput = findViewById(R.id.username_input);
        fullNameInput = findViewById(R.id.full_name_input);
        passwordInput = findViewById(R.id.password_input);
        createButton = findViewById(R.id.create_button);
        spotifyRegisterButton = findViewById(R.id.spotifyLoginButton);
        backButton = findViewById(R.id.back_button);

        // Initialize Firebase Database reference
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Handle Create Account button click (Normal registration)
        createButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String fullName = fullNameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!username.isEmpty() && !fullName.isEmpty() && !password.isEmpty()) {
                createUserAccount(username, fullName, password, null, null);
            } else {
                Toast.makeText(CreateUserActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Spotify Registration button click
        spotifyRegisterButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String fullName = fullNameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!username.isEmpty() && !fullName.isEmpty() && !password.isEmpty()) {
                initiateSpotifyLogin(username, fullName, password);
            } else {
                Toast.makeText(CreateUserActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Back button click
        backButton.setOnClickListener(v -> finish());
    }

    private void initiateSpotifyLogin(String username, String fullName, String password) {
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-top-read", "user-read-private"});
        builder.setShowDialog(true);
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);

        // Save temporary registration details to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("temp_username", username)
                .putString("temp_fullName", fullName)
                .putString("temp_password", password)
                .apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                case TOKEN:
                    String accessToken = response.getAccessToken();
                    String refreshToken = response.getAccessToken();
                    Log.d("SpotifyAuth", "Access Token: " + accessToken);

                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    String username = prefs.getString("temp_username", null);
                    String fullName = prefs.getString("temp_fullName", null);
                    String password = prefs.getString("temp_password", null);

                    if (username != null && fullName != null && password != null) {
                        createUserAccount(username, fullName, password, accessToken, refreshToken);
                    }
                    break;

                case ERROR:
                    Toast.makeText(this, "Spotify registration failed.", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    Toast.makeText(this, "Spotify registration cancelled.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void createUserAccount(String username, String fullName, String password, String accessToken, String refreshToken) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", fullName);
        userMap.put("password", password);
        userMap.put("accessToken", accessToken);
        userMap.put("refreshToken", refreshToken);

        // Save user to Firebase Realtime Database
        usersRef.child(username).setValue(userMap, (error, ref) -> {
            if (error == null) {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putString("username", username)
                        .putString("password", password)
                        .putString("accessToken", accessToken)
                        .putString("refreshToken", refreshToken)
                        .apply();

                Intent intent = new Intent(CreateUserActivity.this, HomeActivity.class);
                intent.putExtra("isSpotifyLogin", accessToken != null);
                if (accessToken != null) {
                    intent.putExtra("accessToken", accessToken);
                    intent.putExtra("accessToken", refreshToken);
                }
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(CreateUserActivity.this, "Failed to create account", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error creating account: " + error.getMessage());
            }
        });
    }
}
