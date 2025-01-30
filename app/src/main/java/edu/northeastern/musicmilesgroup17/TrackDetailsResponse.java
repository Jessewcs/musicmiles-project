
package edu.northeastern.musicmilesgroup17;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class TrackDetailsResponse {
    @SerializedName("preview_url")
    private String previewUrl;
    @SerializedName("album")
    private Album album;
    // Getter for preview_url
    public String getPreviewUrl() {
        return previewUrl;
    }
    // Getter for album
    public Album getAlbum() {
        return album;
    }
    // Nested Album class
    public static class Album {
        @SerializedName("images")
        private List<Image> images;
        public List<Image> getImages() {
            return images;
        }
    }
    // Nested Image class
    public static class Image {
        @SerializedName("url")
        private String url;
        public String getUrl() {
            return url;
        }
    }
}