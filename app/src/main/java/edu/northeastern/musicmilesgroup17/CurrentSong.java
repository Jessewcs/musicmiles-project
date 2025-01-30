package edu.northeastern.musicmilesgroup17;

public class CurrentSong {
    private String title;
    private String artist;
    private String albumArt;
    private String spotifyUri;
    private long timestamp;

    public CurrentSong() {
    }

    public CurrentSong(String title, String artist, String albumArt, String spotifyUri) {
        this.title = title;
        this.artist = artist;
        this.albumArt = albumArt;
        this.spotifyUri = spotifyUri;
        this.timestamp = System.currentTimeMillis();
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbumArt() { return albumArt; }
    public void setAlbumArt(String albumArt) { this.albumArt = albumArt; }

    public String getSpotifyUri() { return spotifyUri; }
    public void setSpotifyUri(String spotifyUri) { this.spotifyUri = spotifyUri; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}