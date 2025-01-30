package edu.northeastern.musicmilesgroup17;

public class UserLocationData {
    private double latitude;
    private double longitude;
    private CurrentSong currentSong;
    private boolean isListening;
    private long lastUpdated;

    public UserLocationData() {
    }

    public UserLocationData(double latitude, double longitude, CurrentSong currentSong,
                            boolean isListening) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentSong = currentSong;
        this.isListening = isListening;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and setters
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public CurrentSong getCurrentSong() { return currentSong; }
    public void setCurrentSong(CurrentSong currentSong) { this.currentSong = currentSong; }

    public boolean isListening() { return isListening; }
    public void setListening(boolean listening) { isListening = listening; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}