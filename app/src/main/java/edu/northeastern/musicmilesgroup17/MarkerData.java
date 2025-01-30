package edu.northeastern.musicmilesgroup17;

import com.google.android.gms.maps.model.Marker;

public class MarkerData {
    private String username;
    private String fullName;
    private Marker marker;
    private CurrentSong currentSong;

    public MarkerData(String username, String fullName, Marker marker, CurrentSong currentSong) {
        this.username = username;
        this.fullName = fullName;
        this.marker = marker;
        this.currentSong = currentSong;
    }

    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public Marker getMarker() { return marker; }
    public void setMarker(Marker marker) { this.marker = marker; }
    public CurrentSong getCurrentSong() { return currentSong; }
    public void setCurrentSong(CurrentSong currentSong) { this.currentSong = currentSong; }
}