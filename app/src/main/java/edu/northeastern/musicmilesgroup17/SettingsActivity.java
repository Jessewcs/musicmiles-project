package edu.northeastern.musicmilesgroup17;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference usersRef;
    private String username;
    private SharedPreferences prefs;

    private Switch privateAccountSwitch;
    private Switch friendActivitySwitch;
    private Switch newSongsSwitch;
    private Switch downloadOverWifiSwitch;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        username = prefs.getString("username", "");

        privateAccountSwitch = findViewById(R.id.privateAccountSwitch);
        friendActivitySwitch = findViewById(R.id.friendActivitySwitch);
        newSongsSwitch = findViewById(R.id.newSongsSwitch);
        downloadOverWifiSwitch = findViewById(R.id.downloadOverWifiSwitch);
        logoutButton = findViewById(R.id.logoutButton);

        loadSettings();
        setupListeners();
    }

    private void loadSettings() {
        // Load saved settings
        privateAccountSwitch.setChecked(prefs.getBoolean("private_account", false));
        friendActivitySwitch.setChecked(prefs.getBoolean("friend_activity", true));
        newSongsSwitch.setChecked(prefs.getBoolean("new_songs", true));
        downloadOverWifiSwitch.setChecked(prefs.getBoolean("wifi_only", true));
    }

    private void setupListeners() {
        // Save settings when switched
        privateAccountSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveSettings("private_account", isChecked));

        friendActivitySwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveSettings("friend_activity", isChecked));

        newSongsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveSettings("new_songs", isChecked));

        downloadOverWifiSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveSettings("wifi_only", isChecked));

        // logout
        logoutButton.setOnClickListener(v -> logout());
    }

    private void saveSettings(String key, boolean value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Go back
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}