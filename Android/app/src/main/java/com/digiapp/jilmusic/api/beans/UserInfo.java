package com.digiapp.jilmusic.api.beans;

import com.google.gson.annotations.SerializedName;

public class UserInfo {

    @SerializedName("id")
    public int id;

    @SerializedName("is_active")
    public int is_active;

    @SerializedName("ends_with")
    public String ends_with;

    @SerializedName("username")
    public String username;

    @SerializedName("first_name")
    public String first_name;

    @SerializedName("last_name")
    public String last_name;

    @SerializedName("avatar")
    public String avatar;

    @SerializedName("language")
    public String language;

    @SerializedName("country")
    public String country;

    @SerializedName("subscription")
    public String subscription;

    @SerializedName("display_name")
    public String display_name;

}
