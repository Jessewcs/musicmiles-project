package edu.northeastern.musicmilesgroup17;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference usersRef;
    private String username;
    private EditText editFullName;
    private TextView profileUsername;
    private ImageView profileImage;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // username
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        username = prefs.getString("username", "");

        editFullName = findViewById(R.id.editFullName);
        profileUsername = findViewById(R.id.profileUsername);
        profileImage = findViewById(R.id.profileImage);
        saveButton = findViewById(R.id.saveProfileButton);

        profileUsername.setText("@" + username);

        loadUserData();

        saveButton.setOnClickListener(v -> saveChanges());

        // Setup the profile image click (for future implementation) if we have time
        profileImage.setOnClickListener(v -> {
            Toast.makeText(this, "Profile picture update coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserData() {
        usersRef.child(username).get().addOnSuccessListener(snapshot -> {
            String fullName = snapshot.child("name").getValue(String.class);
            if (fullName != null) {
                editFullName.setText(fullName);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load user data!", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveChanges() {
        String newName = editFullName.getText().toString().trim();

        if (newName.isEmpty()) {
            editFullName.setError("Name cannot be empty!");
            return;
        }

        // updates name in Firebase
        usersRef.child(username).child("name").setValue(newName)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update profile!", Toast.LENGTH_SHORT).show();
                });
    }
}