package com.digiapp.jilmusic.dao;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.digiapp.jilmusic.beans.AlbumTrack;

import java.lang.reflect.Type;

public class Converter {

    @TypeConverter
    public static String fromAlbumTrack(AlbumTrack info){
        Gson gson = new Gson();
        String json = gson.toJson(info);
        return json;
    }

    @TypeConverter
    public AlbumTrack toAlbumTrack(String info){
        Gson gson = new Gson();
        String json = gson.toJson(info);
        Type type = new TypeToken<AlbumTrack>() {
        }.getType();

        return gson.fromJson(info,type);
    }

    @TypeConverter
    public static String fromStringArray(String[] info){
        Gson gson = new Gson();
        String json = gson.toJson(info);
        return json;
    }

    @TypeConverter
    public String[] toStringArray(String info){
        Gson gson = new Gson();
        String json = gson.toJson(info);
        Type type = new TypeToken<String[]>() {
        }.getType();

        return gson.fromJson(info,type);
    }
}
