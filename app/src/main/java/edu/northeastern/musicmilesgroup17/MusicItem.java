package edu.northeastern.musicmilesgroup17;

// Helper class for music items
public class MusicItem {
    private final String title;
    private final String artistName;
    private int artResId;
    private String previewURL;
    private String trackId;
    private String artURL;
    private final boolean isSong;

    public MusicItem(String title, int artResId, boolean isSong) {
        this.title = title;
        this.artResId = artResId;
        this.isSong = isSong;
        this.artistName = "No artist";
        this.trackId = null;
        this.previewURL = null;
    }

    public MusicItem(String title, String artURL, boolean isSong) {
        this.title = title;
        this.artURL = artURL;
        this.isSong = isSong;
        this.artistName = "No artist";
    }

    public MusicItem(String trackId, String title, String artistName, String previewUrl, String artURL, boolean isSong) {
        this.trackId = trackId;
        this.title = title;
        this.artURL = artURL;
        this.isSong = isSong;
        this.artistName = artistName;
        this.previewURL = previewUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getTrackId() {
        return trackId;
    }
    public String getPreviewUrl() {
        return previewURL;
    }

    public int getArtResId() {
        return artResId;
    }

    public boolean isSong() {
        return isSong;
    }

    public String getArtUrl() {return artURL;}

    public String getArtistName() {return artistName;}
}
