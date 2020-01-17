package com.digiapp.jilmusic.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

public class PreferencesHelper {

    public static final String PREFERENCES_FILE_NAME = "default_preferences";

    public static final String MODE_NIGHT_KEY = "mode_night";
    public static final int MODE_NIGHT_DEFAULT = AppCompatDelegate.MODE_NIGHT_NO;

    @AppCompatDelegate.NightMode
    public static int getModeNight(Context context) {
        return context.getSharedPreferences(PREFERENCES_FILE_NAME, 0)
                .getInt(MODE_NIGHT_KEY, MODE_NIGHT_DEFAULT);
    }

    @SuppressLint("ApplySharedPref")
    public static void setModeNight(Context context, @AppCompatDelegate.NightMode int modeNight) {
        context.getApplicationContext().getSharedPreferences(PREFERENCES_FILE_NAME, 0)
                .edit()
                .putInt(MODE_NIGHT_KEY, modeNight)
                .commit();
    }
}