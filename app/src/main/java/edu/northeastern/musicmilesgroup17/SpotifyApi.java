package edu.northeastern.musicmilesgroup17;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SpotifyApi {
    @GET("v1/tracks/{id}")
    Call<TrackLocal> getTrack(
            @Path("id") String trackId,
            @Header("Authorization") String authHeader
    );

    @GET("v1/artists/{id}")
    Call<ArtistLocal> getArtist(
            @Path("id") String artistId,
            @Header("Authorization") String authHeader
    );

    @GET("v1/albums/{id}")
    Call<AlbumLocal> getAlbum(
            @Path("id") String albumId,
            @Header("Authorization") String authHeader
    );

    @GET("v1/playlists/{playlist_id}")
    Call<Playlist> getPlaylist(
            @Path("playlist_id") String playlistId,
            @Header("Authorization") String authHeader
    );

    @GET("v1/me/top/tracks")
    Call<TopTracksResponse> getTopTracks(
            @Header("Authorization") String authHeader,
            @Query("market") String market
    );

    @GET("v1/search")
    Call<SearchResponse> searchTracks(
            @Header("Authorization") String authorization,
            @Query("q") String query,
            @Query("type") String type,
            @Query("limit") int limit
    );

    @GET("v1/me")
    Call<UserProfileResponse> getCurrentUserProfile(@Header("Authorization") String authorization);


    @GET("v1/tracks/{id}")
    Call<TrackDetailsResponse> getTrackDetails(
            @Header("Authorization") String authorization,
            @Path("id") String trackId,
            @Query("market") String market
    );

    @POST("api/token")
    Call<TokenResponse> refreshToken(
            @Header("Authorization") String authHeader,
            @Body RequestBody body
    );

}

