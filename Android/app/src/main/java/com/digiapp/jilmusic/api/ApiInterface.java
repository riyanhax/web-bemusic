package com.digiapp.jilmusic.api;

import com.google.gson.JsonObject;

import com.digiapp.jilmusic.api.beans.UserInfo;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by artembogomaz on 11/19/2016.
 */

public interface ApiInterface {
    @Headers("Content-Type: application/json")
    @GET("subscription")
    Call<UserInfo> subscription(@Query("user_id") int user_id);
}
