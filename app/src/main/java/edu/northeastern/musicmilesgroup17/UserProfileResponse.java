package edu.northeastern.musicmilesgroup17;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {
    private String display_name;
    private String email;
    @SerializedName("product") // Maps to the "product" field in the JSON response
    private String product;

    @SerializedName("country")
    private String country;

    public String getDisplayName() {
        return display_name;
    }

    public String getEmail() {
        return email;
    }

    public String getProduct() {
        return product;
    }

    public String getCountry() {
        return country;
    }
}
