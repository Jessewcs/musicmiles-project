package edu.northeastern.musicmilesgroup17;

public class SearchResult {
    public enum Type {
        SONG,
        USER
    }

    private String title;
    private String subtitle; // artist for songs, username for users
    private Type type;

    public SearchResult(String title, String subtitle, Type type) {
        this.title = title;
        this.subtitle = subtitle;
        this.type = type;
    }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public Type getType() { return type; }
}