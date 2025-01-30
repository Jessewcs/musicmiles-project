package edu.northeastern.musicmilesgroup17;

import com.spotify.protocol.types.Album;
import com.spotify.protocol.types.Artist;
import androidx.annotation.NonNull;

import java.util.List;

public class TrackLocal {
    private String name;
    private AlbumLocal album;
    private List<ArtistLocal> artists;
    private String id;
    private String preview_url;

    public String getName() {
        return name;
    }

    public AlbumLocal getAlbum() {
        return album;
    }

    public List<ArtistLocal> getArtists() {
        return artists;
    }

    public String getPreviewUrl() { return preview_url;
    }
    public String getId() { return id;
    }
    @Override
    public String toString() {
        return "TrackLocal{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", artists=" + artists +
                ", album=" + album +
                ", previewUrl='" + preview_url + '\'' +
                '}';
    }
}
