package edu.northeastern.musicmilesgroup17;

import static com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "c300b742f15643d8b1d70998dc0a4e02";
    private static final String REDIRECT_URI = "musicmiles://callback/";

    private SpotifyApi spotifyApiService;
    private String accessToken;
    private String refreshToken;
    private static final String DEFAULT_MARKET = "US";

    private DatabaseReference usersRef;
    private String username, password;
    private String userFullName;

    private SearchAdapter searchAdapter;
    private RecyclerView searchResultsRecyclerView;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private ScrollView mainScrollView;
    private ProgressBar loadingIndicator;
    private GridLayout musicGrid;

    private BottomNavigationView bottomNavigationView;
    private LinearLayout mainContent;
    private LinearLayout libraryContent;
    private LinearLayout friendsContent;

    private RecyclerView friendsRecyclerView;
    private UserAdapter userAdapter;
    // Store friends as a map of username -> full name
    private final Map<String, String> friendsMap = new HashMap<>();

    private RecyclerView libraryRecyclerView;
    private LibraryAdapter libraryAdapter;
    private List<CurrentSong> currentSongs = new ArrayList<>();

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    // temp sample music items
    private List<MusicItem> musicItems = new ArrayList<>();

    boolean isSpotifyLogin;

    private LinearLayout mapContent;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private GoogleMap googleMap;
    private boolean initialLocationSet = false;

    private Map<String, MarkerData> friendMarkers = new HashMap<>();
    private DatabaseReference locationRef;
    private ValueEventListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d("HomeActivity", "HomeActivity started successfully.");

        mapContent = findViewById(R.id.mapContent);
        mapContent.setVisibility(View.GONE);  // hide at first

        isSpotifyLogin = getIntent().getBooleanExtra("isSpotifyLogin", false);

        // Initialize UI elements
        musicGrid = findViewById(R.id.musicGrid);

        spotifyApiService = RetrofitClient.getInstance().create(SpotifyApi.class);

        // Get Firebase reference to users node
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Get the logged-in username from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        username = prefs.getString("username", null);
        password = prefs.getString("password", null);
        accessToken = prefs.getString("accessToken", null);
        refreshToken = prefs.getString("refreshToken", null);

        if (username == null && accessToken == null) {
            // Redirect to login if no username or spotify account is found
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (isSpotifyLogin) {
            // Fetch Spotify user-specific data
            try {
                fetchTopSongsAndAlbums();
                fetchUserProfile();
            } catch (Exception error) {
                ensureValidToken();
            }
        } else {
            // Load default music data
            initializeDummyMusicItems();
            populateMusicGrid(musicItems);
        }

        // initialize ProgressBar
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // initialize bottom nav
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // initialize content
        mainContent = findViewById(R.id.mainContent);
        libraryContent = findViewById(R.id.libraryContent);
        friendsContent = findViewById(R.id.friendsContent);

        // hide these first
        libraryContent.setVisibility(View.GONE);
        friendsContent.setVisibility(View.GONE);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                mainContent.setVisibility(View.VISIBLE);
                libraryContent.setVisibility(View.GONE);
                friendsContent.setVisibility(View.GONE);
                mapContent.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.navigation_library) {
                mainContent.setVisibility(View.GONE);
                libraryContent.setVisibility(View.VISIBLE);
                friendsContent.setVisibility(View.GONE);
                mapContent.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.navigation_friends) {
                mainContent.setVisibility(View.GONE);
                libraryContent.setVisibility(View.GONE);
                friendsContent.setVisibility(View.VISIBLE);
                mapContent.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.navigation_map) {
                mainContent.setVisibility(View.GONE);
                libraryContent.setVisibility(View.GONE);
                friendsContent.setVisibility(View.GONE);
                mapContent.setVisibility(View.VISIBLE);
                initializeMap();
                return true;
            }
            return false;
        });

        // Friends content
        setupFriendsView();

        // Library
        setupLibraryView();

        // Menu
        setupNavigationDrawer();

        // Profile temp fpr now
        setupProfileButton();

        if (isSpotifyLogin) {
            ensureValidToken();
            setupHomeSearchSpotify();
        } else {
            // Search for sample songs for now and users?
            setupHomeSearch();
        }

    }


    private void initializeMap() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                this.googleMap = googleMap;

                // map settings
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);

                enableMyLocation();
                setupFriendMarkers();
            });
        }

        // location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateMapLocation(location);
                }
            }
        };
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                startLocationUpdates();
            }
        } else {
            // perms not granted so request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(10000) // Update location every 10 seconds
                .setFastestInterval(5000) // Fastest update interval
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());

        // last known location and move camera
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, this::updateMapLocation);
    }

    private void updateMapLocation(Location location) {
        if (location != null && googleMap != null) {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            // move camera on first location fix
            if (!initialLocationSet) {
                // 6f is state
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 6f));
                initialLocationSet = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission is required for this feature",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop location updates when app is in background
        if (fusedLocationClient != null && locationCallback != null) {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback);
            } catch (Exception e) {
                Log.e("HomeActivity", "Error removing location updates", e);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Resume location updates when app is in foreground
        if (fusedLocationClient != null &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            try {
                startLocationUpdates();
            } catch (Exception e) {
                Log.e("HomeActivity", "Error starting location updates", e);
            }
        }
    }

    private void setupFriendMarkers() {
        if (googleMap == null) return;

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null; // default window frame
            }

            @Override
            public View getInfoContents(Marker marker) {
                View view = getLayoutInflater().inflate(R.layout.marker_info_window, null);
                MarkerData data = friendMarkers.get(marker.getId());

                if (data != null) {
                    TextView friendName = view.findViewById(R.id.friend_name);
                    TextView songInfo = view.findViewById(R.id.song_info);
                    TextView timestamp = view.findViewById(R.id.timestamp);

                    friendName.setText(data.getFullName());

                    CurrentSong song = data.getCurrentSong();
                    if (song != null) {
                        songInfo.setText(String.format("%s - %s",
                                song.getTitle(), song.getArtist()));
                        timestamp.setText("Started: " +
                                new SimpleDateFormat("HH:mm", Locale.getDefault())
                                        .format(new Date(song.getTimestamp())));
                    } else {
                        songInfo.setText("Not listening to music");
                        timestamp.setVisibility(View.GONE);
                    }
                }
                return view;
            }
        });

        // listening for friend locations
        setupLocationListener();
    }

    private void setupLocationListener() {
        locationRef = FirebaseDatabase.getInstance().getReference("users");

        locationListener = locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String friendUsername = userSnapshot.getKey();

                    // Skip curr user
                    if (friendUsername.equals(username)) continue;

                    DataSnapshot locationSnapshot = userSnapshot.child("location");
                    if (locationSnapshot.exists()) {
                        double lat = locationSnapshot.child("latitude").getValue(Double.class);
                        double lng = locationSnapshot.child("longitude").getValue(Double.class);
                        String fullName = userSnapshot.child("name").getValue(String.class);

                        // current song info IF available
                        CurrentSong currentSong = null;
                        DataSnapshot songSnapshot = userSnapshot.child("currentSong");
                        if (songSnapshot.exists()) {
                            currentSong = songSnapshot.getValue(CurrentSong.class);
                        }

                        updateFriendMarker(friendUsername, fullName,
                                new LatLng(lat, lng), currentSong);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeActivity", "Failed to read friend locations", error.toException());
            }
        });
    }

    private void updateFriendMarker(String friendUsername, String fullName,
                                    LatLng position, CurrentSong song) {

        if (googleMap == null) return;

        MarkerData existingData = null;
        for (MarkerData data : friendMarkers.values()) {
            if (data.getUsername().equals(friendUsername)) {
                existingData = data;
                break;
            }
        }

        if (existingData != null) {
            // Update existing marker
            existingData.getMarker().setPosition(position);
            existingData.setCurrentSong(song);
            existingData.getMarker().showInfoWindow();
        } else {
            // Create new marker
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(fullName)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            if (marker != null) {
                friendMarkers.put(marker.getId(),
                        new MarkerData(friendUsername, fullName, marker, song));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (googleMap != null) {
            googleMap.clear();
            googleMap = null;
        }
        if (locationListener != null && locationRef != null) {
            try {
                locationRef.removeEventListener(locationListener);
            } catch (Exception e) {
                Log.e("HomeActivity", "Error removing location listener", e);
            }
        }
        // Clean up location resources
        if (fusedLocationClient != null && locationCallback != null) {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback);
            } catch (Exception e) {
                Log.e("HomeActivity", "Error removing location updates", e);
            }
        }
        fusedLocationClient = null;
        locationCallback = null;
    }

    private void initializeDummyMusicItems() {
        musicItems.add(new MusicItem("New Recommended Song", R.drawable.sample_music_art, true));
        musicItems.add(new MusicItem("New Recommended Song 2", R.drawable.sample_music_art, true));
        musicItems.add(new MusicItem("My Playlists", R.drawable.sample_music_art, false));
        musicItems.add(new MusicItem("Song 1", R.drawable.sample_music_art, true));
        musicItems.add(new MusicItem("Song 2", R.drawable.sample_music_art, true));
        musicItems.add(new MusicItem("Beach Playlist", R.drawable.sample_music_art, false));
    }

    private void fetchTopSongsAndAlbums() {
        // Fetch Top Tracks
        spotifyApiService.getTopTracks("Bearer " + accessToken, DEFAULT_MARKET)
                .enqueue(new Callback<TopTracksResponse>() {
                    @Override
                    public void onResponse(Call<TopTracksResponse> call, Response<TopTracksResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<TrackLocal> tracks = response.body().getItems();
                            List<MusicItem> musicItems = new ArrayList<>();

                            for (TrackLocal track : tracks) {
                                String title = track.getName();
                                String artistName = track.getArtists().get(0).getName();
                                String artUrl = track.getAlbum().getImages().get(0).getUrl();
                                String trackId = track.getId();
                                String previewUrl = track.getPreviewUrl();
                                musicItems.add(new MusicItem(trackId, title, artistName, previewUrl, artUrl, true));
                            }

                            // Populate the music grid
                            populateMusicGrid(musicItems);
                        }
                    }

                    @Override
                    public void onFailure(Call<TopTracksResponse> call, Throwable t) {
                        Toast.makeText(HomeActivity.this, "Failed to load Spotify data", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setupLibraryView() {
        libraryRecyclerView = findViewById(R.id.libraryRecyclerView);
        libraryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Example sample songs for now
        currentSongs.add(new CurrentSong("Song 1", "Artist 1", "Art", "3:45"));
        currentSongs.add(new CurrentSong("Song 2", "Artist 2","Art", "4:20"));
        currentSongs.add(new CurrentSong("Song 3", "Artist 3","Art", "3:15"));

        libraryAdapter = new LibraryAdapter(currentSongs);
        libraryRecyclerView.setAdapter(libraryAdapter);
    }

    private void setupFriendsView() {
        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(friendsMap);
        friendsRecyclerView.setAdapter(userAdapter);

        SearchView friendsSearchView = findViewById(R.id.friendsSearchView);
        MaterialCardView searchCard = findViewById(R.id.friendsSearchCard);
        searchCard.setOnClickListener(v -> {
            friendsSearchView.requestFocus();
            friendsSearchView.setIconified(false);
        });
        friendsSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (userAdapter != null) {
                    userAdapter.filter(newText);
                }
                return true;
            }
        });

        // get users from Firebase
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendsMap.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userName = userSnapshot.getKey();
                    String fullName = userSnapshot.child("name").getValue(String.class);
                    if (userName != null && !userName.equals(username) && fullName != null) {
                        friendsMap.put(userName, fullName);
                    }
                }
                userAdapter.updateData(friendsMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateMusicGrid(List<MusicItem> musicItems) {
        musicGrid.removeAllViews();
        for (MusicItem item : musicItems) {
            // Create a FrameLayout for each block
            FrameLayout musicBlock = (FrameLayout) getLayoutInflater().inflate(R.layout.music_block, null);

            // Set the music art
            ImageView musicArt = musicBlock.findViewById(R.id.musicArt);
            if (item.getArtUrl() != null) {
                Glide.with(this).load(item.getArtUrl()).into(musicArt);
            } else {
                musicArt.setImageResource(item.getArtResId());
            }

            // Set the music title
            TextView musicTitle = musicBlock.findViewById(R.id.musicTitle);
            musicTitle.setText(item.getTitle());

            // Handle click event
            musicBlock.setOnClickListener(v -> {
                if (item.isSong()) {
                    // Navigate to the ListeningActivity
                    Intent intent = new Intent(HomeActivity.this, ListeningActivity.class);
                    intent.putExtra("trackId", item.getTrackId());
                    intent.putExtra("trackName", item.getTitle());
                    intent.putExtra("trackArtist", item.getArtistName());
                    intent.putExtra("trackArt", item.getArtUrl());
                    intent.putExtra("previewUrl", item.getPreviewUrl());
                    Log.d("HomeActivity", "Track ID: " + item.getTrackId());
                    Log.d("HomeActivity", "Track Name: " + item.getTitle());
                    Log.d("HomeActivity", "Track Artist: " + item.getArtistName());
                    Log.d("HomeActivity", "Track Art URL: " + item.getArtUrl());
                    Log.d("HomeActivity", "Preview URL: " + item.getPreviewUrl());
                    startActivity(intent);
                } else {
                    // Navigate to the PlaylistActivity
                    Intent intent = new Intent(HomeActivity.this, PlaylistActivity.class);
                    intent.putExtra("playlistTitle", item.getTitle());
                    startActivity(intent);
                }
            });

            // Add the block to the GridLayout
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0; // Use column weight to distribute evenly
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1);
            params.setMargins(16, 16, 16, 16);

            musicGrid.addView(musicBlock, params);
        }
    }

    private void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.black));

        // header with user info
        View headerView = navigationView.getHeaderView(0);
        TextView headerFullName = headerView.findViewById(R.id.headerFullName);
        TextView headerUsername = headerView.findViewById(R.id.headerUsername);

        if (!isSpotifyLogin) {
            // user data from Firebase
            usersRef.child(username).get().addOnSuccessListener(snapshot -> {
                String fullName = snapshot.child("name").getValue(String.class);
                headerFullName.setText(fullName);
                headerUsername.setText("@" + username);
            });
        } else {
            headerFullName.setText(userFullName);
            headerUsername.setText("Spotify User");
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setupHomeSearch() {
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainScrollView = findViewById(R.id.mainScrollView);

        searchAdapter = new SearchAdapter(result -> {
            if (result.getType() == SearchResult.Type.SONG) {
                // Handle song click
                Intent intent = new Intent(HomeActivity.this, ListeningActivity.class);
                intent.putExtra("musicTitle", result.getTitle());
                startActivity(intent);
            } else {
                // Handle user click
                Toast.makeText(this, "User: " + result.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        searchResultsRecyclerView.setAdapter(searchAdapter);

        SearchView homeSearchView = findViewById(R.id.homeSearchView);
        MaterialCardView homeSearchCard = findViewById(R.id.homeSearchCard);
        homeSearchCard.setOnClickListener(v -> {
            homeSearchView.requestFocus();
            homeSearchView.setIconified(false);
        });
        if (homeSearchView != null) {
            homeSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.isEmpty()) {
                        searchResultsRecyclerView.setVisibility(View.GONE);
                        mainScrollView.setVisibility(View.VISIBLE);
                    } else {
                        searchResultsRecyclerView.setVisibility(View.VISIBLE);
                        mainScrollView.setVisibility(View.GONE);
                        performSearch(newText);
                    }
                    return true;
                }
            });
        }
    }

    private void performSearch(String query) {
        List<SearchResult> results = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        // Search users
        for (Map.Entry<String, String> entry : friendsMap.entrySet()) {
            String username = entry.getKey();
            String fullName = entry.getValue();
            if (username.toLowerCase().contains(lowerCaseQuery) ||
                    fullName.toLowerCase().contains(lowerCaseQuery)) {
                results.add(new SearchResult(fullName, "@" + username, SearchResult.Type.USER));
            }
        }

        // Search DUMMY songs for now..
        for (MusicItem item : musicItems) {
            if (item.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                results.add(new SearchResult(item.getTitle(), "Sample Artist", SearchResult.Type.SONG));
            }
        }
        searchAdapter.updateResults(results);
    }

    // Temp Profile icon click setup only shows logout will work for now
    private void setupProfileButton() {
        ImageView profileImage = findViewById(R.id.profileImage);
        profileImage.setOnClickListener(v -> showProfileDialog());
    }

    private void showProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.profile_dialog, null);
        builder.setView(dialogView);

        TextView fullNameText = dialogView.findViewById(R.id.profileFullName);
        TextView usernameText = dialogView.findViewById(R.id.profileUsername);
        Button logoutButton = dialogView.findViewById(R.id.logoutButton);

        if (!isSpotifyLogin) {
            usersRef.child(username).get().addOnSuccessListener(snapshot -> {
                String fullName = snapshot.child("name").getValue(String.class);
                fullNameText.setText(fullName);
                usernameText.setText("@" + username);
            });
        } else {
            fullNameText.setText(userFullName);
            usernameText.setText("Spotify User");
        }

        AlertDialog dialog = builder.create();

        // logout button
        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();

            // Go back to login
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupHomeSearchSpotify() {
        // Set up the search bar
        SearchView homeSearchView = findViewById(R.id.homeSearchView);
        MaterialCardView homeSearchCard = findViewById(R.id.homeSearchCard);

        // Focus the search bar when the search card is clicked
        homeSearchCard.setOnClickListener(v -> {
            homeSearchView.requestFocus();
            homeSearchView.setIconified(false);
        });

        homeSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query == null || query.trim().isEmpty()) {
                    Toast.makeText(HomeActivity.this, "Please enter a valid search query", Toast.LENGTH_SHORT).show();
                    return false;
                }
                performSearchSpotify(query.trim());
                // Hide the keyboard
                hideKeyboard();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    musicGrid.removeAllViews();
                    // If search is cleared, refresh to top tracks
                    fetchTopSongsAndAlbums();
                } else {
                    // Perform search for the new query
                    performSearchSpotify(newText);
                }
                return true;
            }
        });
    }

    private void performSearchSpotify(String query) {
        // Show loading indicator
        loadingIndicator.setVisibility(View.VISIBLE);
        executorService.execute(() -> {
            try {
                // Make the API call in the background
                Response<SearchResponse> response = spotifyApiService
                        .searchTracks("Bearer " + accessToken, query, "track", 20)
                        .execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<TrackLocal> tracks = response.body().getTracks().getItems();
                    List<MusicItem> musicItems = new ArrayList<>();

                    for (TrackLocal track : tracks) {
                        String title = track.getName();
                        String artistName = track.getArtists().get(0).getName();
                        String artUrl = track.getAlbum().getImages().get(0).getUrl();
                        String trackId = track.getId();
                        String previewUrl = track.getPreviewUrl();
                        musicItems.add(new MusicItem(trackId, title, artistName, previewUrl, artUrl, true));
                    }

                    // Post results back to the main thread
                    uiHandler.post(() -> {
                        populateMusicGrid(musicItems);
                        loadingIndicator.setVisibility(View.GONE); // Hide loading indicator
                    });
                } else {
                    // Handle unsuccessful responses
                    uiHandler.post(() -> {
                        Toast.makeText(HomeActivity.this, "No results found.", Toast.LENGTH_SHORT).show();
                        loadingIndicator.setVisibility(View.GONE);
                    });
                }
            } catch (Exception e) {
                // Handle failures
                uiHandler.post(() -> {
                    Toast.makeText(HomeActivity.this, "Search failed. Please try again.", Toast.LENGTH_SHORT).show();
                    loadingIndicator.setVisibility(View.GONE);
                });
            }
        });
    }

    private void fetchUserProfile() {
        spotifyApiService.getCurrentUserProfile("Bearer " + accessToken)
                .enqueue(new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UserProfileResponse userProfile = response.body();
                            userFullName = userProfile.getDisplayName();

                        } else {
                            Log.e("SpotifyUser", "Failed to fetch user profile. Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                        Log.e("SpotifyUser", "Error fetching user profile: " + t.getMessage(), t);
                    }
                });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void refreshAccessToken(String refreshToken) {
        String clientId = "c300b742f15643d8b1d70998dc0a4e02";
        String clientSecret = "1b2255d5e44b4011addc7c8d9b2c178c";

        // Encode clientId:clientSecret in Base64
        String authHeader = "Basic " + Base64.encodeToString(
                (clientId + ":" + clientSecret).getBytes(), Base64.NO_WRAP);

        // Create request body
        RequestBody body = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build();

        SpotifyApi authApi = RetrofitClient.getInstance()
                .create(SpotifyApi.class);

        authApi.refreshToken(authHeader, body).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String newAccessToken = response.body().getAccessToken();
                    String newRefreshToken = response.body().getRefreshToken();
                    Log.d("SpotifyAuth", "Access Token Refreshed: " + newAccessToken);

                    // Save the new access token
                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    prefs.edit().putString("accessToken", newAccessToken).apply();

                    // Retry the original API call with the new token if needed
                } else {
                    Log.e("SpotifyAuth", "Failed to refresh token. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Log.e("SpotifyAuth", "Error refreshing token: " + t.getMessage(), t);
            }
        });
    }

    private boolean ensureValidToken() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String refreshToken = prefs.getString("refreshToken", null);
        if (refreshToken != null) {
            refreshAccessToken(refreshToken);
            return true;
        } else {
            Log.e("SpotifyAuth", "No refresh token available. Re-authenticate required.");
            AuthorizationRequest.Builder builder =
                    new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
            builder.setScopes(new String[]{"user-top-read", "user-read-private"});
            builder.setShowDialog(true);
            AuthorizationRequest request = builder.build();

            AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
            return false;
        }


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

                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("name", username);
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

                        } else {
                            Toast.makeText(this, "Spotify refreshing Failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                case ERROR:
                    Toast.makeText(this, "Spotify refresh failed.", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    Toast.makeText(this, "Spotify refresh cancelled.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

}






