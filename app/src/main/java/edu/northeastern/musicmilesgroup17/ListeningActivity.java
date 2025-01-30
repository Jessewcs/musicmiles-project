package edu.northeastern.musicmilesgroup17;

import static com.spotify.sdk.android.auth.AccountsQueryParameters.CLIENT_ID;
import static com.spotify.sdk.android.auth.AccountsQueryParameters.REDIRECT_URI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListeningActivity extends AppCompatActivity {

    private ImageView albumCoverImageView;
    private TextView trackNameTextView, trackArtistTextView;
    private Button playPauseButton;

    private SpotifyAppRemote spotifyAppRemote;
    private String previewURL, trackName, trackArt, trackArtist, trackId;
    private String accessToken;
    private ExoPlayer exoPlayer;

    private boolean isPlaying = false; // Track play/pause state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen);

        // Initialize UI elements
        // Initialize views
        trackNameTextView = findViewById(R.id.songTitle);
        trackArtistTextView = findViewById(R.id.songArtist);
        playPauseButton = findViewById(R.id.playPauseButton);
        albumCoverImageView = findViewById(R.id.albumCover);

        // Retrieve access token from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("accessToken", null);

        Intent intent = getIntent();
        if (intent != null) {
            trackId = intent.getStringExtra("trackId");
            trackArt = intent.getStringExtra("trackArt");
            trackName = intent.getStringExtra("trackName");
            trackArtist = intent.getStringExtra("trackArtist");
            previewURL = intent.getStringExtra("previewUrl");
            // Set track details
            trackNameTextView.setText(trackName != null ? trackName : "Unknown Track");
            trackArtistTextView.setText(trackArtist != null ? trackArtist : "Unknown Artist");
            if (trackArt != null) {
                Glide.with(this).load(trackArt).into(albumCoverImageView);
            }
            // If no preview is available, show a toast
            if (previewURL == null) {
                Toast.makeText(this, "Preview audio not available for this song.", Toast.LENGTH_SHORT).show();
                disablePlayButton();
            }
        }

        // Check Spotify Premium status
        fetchSpotifyUserProfile();
    }

    private void playPreview(String previewUrl) {
        // Add the media item to ExoPlayer
        MediaItem mediaItem = MediaItem.fromUri(previewUrl);
        exoPlayer.setMediaItem(mediaItem);
        // Prepare and start playback
        exoPlayer.prepare();
        exoPlayer.play();
        isPlaying = true;
        playPauseButton.setText("Pause");
    }

    private void pausePreview() {
        if (isPlaying) {
            exoPlayer.pause();
            isPlaying = false;
            playPauseButton.setText("Play");
        }
    }

    private void disablePlayButton() {
        playPauseButton.setEnabled(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Release ExoPlayer when the activity is stopped
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }
    private void fetchSpotifyUserProfile() {
        SpotifyApi apiService = RetrofitClient.getInstance().create(SpotifyApi.class);
        Call<UserProfileResponse> call = apiService.getCurrentUserProfile("Bearer " + accessToken);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String product = response.body().getProduct();
                    boolean isPremiumUser = "premium".equalsIgnoreCase(product);
                    if (isPremiumUser) {
                        Log.d("ListeningActivity", "User is Spotify Premium. Starting remote playback.");
                        streamTrackWithSpotify();
                    } else if (previewURL != null) {
                        Log.d("ListeningActivity", "User is not Premium. Starting local preview playback.");
                        setupLocalPlayback();
                    } else {
                        Toast.makeText(ListeningActivity.this, "Preview not available for this track.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("ListeningActivity", "Failed to fetch user profile. Response code: " + response.code());
                    if (previewURL != null) {
                        setupLocalPlayback();
                    }
                }
            }
            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Log.e("ListeningActivity", "Failed to fetch user profile: " + t.getMessage());
                if (previewURL != null) {
                    setupLocalPlayback();
                }
            }
        });
    }
    private void streamTrackWithSpotify() {
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();
        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyRemote) {
                spotifyAppRemote = spotifyRemote;
                Log.d("ListeningActivity", "Connected to Spotify!");
                // Stream the track
                spotifyAppRemote.getPlayerApi().play("spotify:track:" + trackId);
                // Subscribe to player state for play/pause updates
                spotifyAppRemote.getPlayerApi()
                        .subscribeToPlayerState()
                        .setEventCallback(playerState -> {
                            isPlaying = !playerState.isPaused;
                            playPauseButton.setText(isPlaying ? "Pause" : "Play");
                        });
                // Set play/pause button behavior
                playPauseButton.setOnClickListener(v -> {
                    if (isPlaying) {
                        spotifyAppRemote.getPlayerApi().pause();
                        playPauseButton.setText("Play");
                    } else {
                        spotifyAppRemote.getPlayerApi().resume();
                        playPauseButton.setText("Pause");
                    }
                });
            }
            @Override
            public void onFailure(Throwable throwable) {
                Log.e("ListeningActivity", "Failed to connect to Spotify: " + throwable.getMessage());
                if (previewURL != null) {
                    setupLocalPlayback();
                } else {
                    Toast.makeText(ListeningActivity.this, "Remote playback unavailable.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void setupLocalPlayback() {
        // Initialize ExoPlayer
        exoPlayer = new ExoPlayer.Builder(this).build();
        // Configure play/pause button
        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                pausePreview();
            } else {
                playPreview(previewURL);
            }
        });
        if (previewURL != null) {
            playPreview(previewURL);
        } else {
            Toast.makeText(this, "Preview audio not available.", Toast.LENGTH_SHORT).show();
            disablePlayButton();
        }
    }
}