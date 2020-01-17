/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digiapp.jilmusic.model;

import android.media.MediaMetadataRetriever;
import android.support.v4.media.MediaMetadataCompat;

import com.digiapp.jilmusic.AppObj;
import org.json.JSONException;
import org.json.JSONObject;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.utils.Core;
import com.digiapp.jilmusic.utils.LogHelper;
import com.digiapp.jilmusic.utils.MediaIDHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Utility class to get a list of MusicTrack's based on a server-side JSON
 * configuration.
 */
public class RemoteJSONSource implements MusicProviderSource {

    private static final String TAG = LogHelper.makeLogTag(RemoteJSONSource.class);

    protected static final String CATALOG_URL =
            "http://storage.googleapis.com/automotive-media/music.json";

    private static final String JSON_MUSIC = "music";
    private static final String JSON_TITLE = "title";
    private static final String JSON_ALBUM = "album";
    private static final String JSON_ARTIST = "artist";
    private static final String JSON_GENRE = "genre";
    private static final String JSON_SOURCE = "source";
    private static final String JSON_IMAGE = "image";
    private static final String JSON_TRACK_NUMBER = "trackNumber";
    private static final String JSON_TOTAL_TRACK_COUNT = "totalTrackCount";
    private static final String JSON_DURATION = "duration";
    private static final String JSON_LOC = "location";

    public static String ALL_GENRE = "ALL_GENRE";
    public static final String NOT_SPECIFIED = "not specified";

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        try {
            int slashPos = CATALOG_URL.lastIndexOf('/');
            String path = CATALOG_URL.substring(0, slashPos + 1);
            ArrayList<MediaMetadataCompat> tracks = new ArrayList<>();

            // read audio first
            File rootDirectory = MediaIDHelper.getMediaDirectory(AppObj.getGlobalContext());
            File[] fileList = rootDirectory.listFiles();
            for (File fl : fileList) {
                tracks.add(buildFromFile(fl.getAbsolutePath()));
            }

            // read video
            rootDirectory = MediaIDHelper.getVideoDirectory(AppObj.getGlobalContext());
            fileList = rootDirectory.listFiles();
            for (File fl : fileList) {
                try {
                    tracks.add(buildFromFile(fl.getAbsolutePath()));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            return tracks.iterator();
        } catch (JSONException e) {
            LogHelper.e(TAG, e, "Could not retrieve music list");
            throw new RuntimeException("Could not retrieve music list", e);
        }
    }

    static String getStringSafe(String source){
        return getStringSafe(source,NOT_SPECIFIED);
    }

    static String getStringSafe(String source,String replace){
        return source==null?NOT_SPECIFIED:source;
    }

    public static MediaMetadataCompat buildFromFile(String filePath,MusicTrack musicTrack) throws JSONException {

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(filePath);
        }catch (Exception ex){
            return null;
        }

        String title = getStringSafe(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),filePath);
        String album = getStringSafe(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        String artist = getStringSafe(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        String genre = getStringSafe(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),"no genre");
        String trackNumber = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)==null?
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS):
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_TITLE,musicTrack.name);
        jsonObject.put(JSON_ALBUM,musicTrack.album_name);
        jsonObject.put(JSON_ARTIST,musicTrack.album_name);

        jsonObject.put(JSON_GENRE,ALL_GENRE);
        jsonObject.put(JSON_SOURCE,musicTrack.localPath);
        if(musicTrack.localPicturePath!=null) {
            jsonObject.put(JSON_IMAGE, musicTrack.localPicturePath);
        }else{
            jsonObject.put(JSON_IMAGE, "");
        }
        jsonObject.put(JSON_TRACK_NUMBER,trackNumber);
        jsonObject.put(JSON_TOTAL_TRACK_COUNT,mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS));
        jsonObject.put(JSON_DURATION,mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        jsonObject.put(JSON_LOC,filePath);
        jsonObject.put(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,musicTrack.id);

        return buildFromJSON(jsonObject,filePath);
    }

    public static MediaMetadataCompat buildFromFile(String filePath) throws JSONException {

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(filePath);
        }catch (Exception ex){
            return null;
        }

        String title = getStringSafe(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),filePath);
        String album = getStringSafe(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        String artist = getStringSafe(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        String genre = getStringSafe(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),"no genre");
        String trackNumber = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)==null?
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS):
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_TITLE,getStringSafe(title));
        jsonObject.put(JSON_ALBUM,getStringSafe(album));
        jsonObject.put(JSON_ARTIST,getStringSafe(artist));
        jsonObject.put(JSON_GENRE,ALL_GENRE);
        jsonObject.put(JSON_SOURCE,"");
        jsonObject.put(JSON_IMAGE,"");
        jsonObject.put(JSON_TRACK_NUMBER,trackNumber);
        jsonObject.put(JSON_TOTAL_TRACK_COUNT,mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS));
        jsonObject.put(JSON_DURATION,mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        jsonObject.put(JSON_LOC,filePath);

        return buildFromJSON(jsonObject,filePath);
    }

    public static MediaMetadataCompat buildFromJSON(JSONObject json, String basePath) throws JSONException {
        String title = json.getString(JSON_TITLE);
        String album = json.getString(JSON_ALBUM);
        String artist = json.getString(JSON_ARTIST);
        String genre = json.getString(JSON_GENRE);
        String source = json.getString(JSON_SOURCE);
        String iconUrl = "";
        try {
            iconUrl = json.getString(JSON_IMAGE);
        }catch (Exception ex){}
        int trackNumber = 0;
        int totalTrackCount = 0;
        try {
            trackNumber = json.getInt(JSON_TRACK_NUMBER);
            totalTrackCount = json.getInt(JSON_TOTAL_TRACK_COUNT);
        }catch (Exception ex){}

        // int duration = json.getInt(JSON_DURATION) * 1000; // ms
        int duration = json.getInt(JSON_DURATION); // ms

        LogHelper.d(TAG, "Found music track: ", json);

        // Media is stored relative to JSON file
        /*if (!source.startsWith("http")) {
            source = basePath + source;
        }*/
        /*if (!iconUrl.startsWith("http")) {
            iconUrl = basePath + iconUrl;
        }*/
        // Since we don't have a unique ID in the server, we fake one using the hashcode of
        // the music source. In a real world app, this could come from the server.
        String id = json.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)==null
                ?String.valueOf(source.hashCode())
                :json.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);

        // Adding the music source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world music app, because
        // the session metadata can be accessed by notification listeners. This is done in this
        // sample for convenience only.
        //noinspection ResourceType
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, ALL_GENRE)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, iconUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,json.getString(JSON_LOC))
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, Core.getBitmapFromPath(iconUrl))
                .build();
    }

    /**
     * Download a JSON file from a server, parse the content and return the JSON
     * object.
     *
     * @return result JSONObject containing the parsed representation.
     */
    private JSONObject fetchJSONFromUrl(String urlString) throws JSONException {
        BufferedReader reader = null;
        try {
            URLConnection urlConnection = new URL(urlString).openConnection();
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return new JSONObject(sb.toString());
        } catch (JSONException e) {
            throw e;
        } catch (Exception e) {
            LogHelper.e(TAG, "Failed to parse the json for media list", e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
