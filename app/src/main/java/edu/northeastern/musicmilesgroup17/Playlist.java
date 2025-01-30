package edu.northeastern.musicmilesgroup17;


import java.util.List;

public class Playlist {
    private String id;
    private String name;
    private String description;
    private List<Image> images; // Cover art images
    private Tracks tracks; // Nested class for playlist tracks

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Image> getImages() {
        return images;
    }

    public Tracks getTracks() {
        return tracks;
    }

    // Inner class for tracks
    public static class Tracks {
        private List<TrackItem> items;

        public List<TrackItem> getItems() {
            return items;
        }

        // Inner class for individual track items
        public static class TrackItem {
            private TrackLocal track;

            public TrackLocal getTrack() {
                return track;
            }
        }
    }
}

