package edu.northeastern.musicmilesgroup17;

public class TokenResponse {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private int expires_in;

    public String getAccessToken() {
        return access_token;
    }

    public String getTokenType() {
        return token_type;
    }

    public int getExpiresIn() {
        return expires_in;
    }

    public String getRefreshToken() {
        return refresh_token;
    }
}

