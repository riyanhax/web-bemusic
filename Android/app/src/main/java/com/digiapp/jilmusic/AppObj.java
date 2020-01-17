package com.digiapp.jilmusic;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.digiapp.jilmusic.utils.PreferencesHelper;
import com.facebook.cache.common.CacheErrorLogger;
import com.facebook.cache.common.CacheEvent;
import com.facebook.cache.common.CacheEventListener;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.liulishuo.filedownloader.FileDownloader;

import com.digiapp.jilmusic.dao.MusicRoomDatabase;

import org.xwalk.core.XWalkApplication;
import org.xwalk.core.XWalkPreferences;

import java.io.File;


public class AppObj extends Application {

    public static Context mContext;
    protected static boolean isConversionSupported = false;

    private static DiskCacheConfig getDefaultMainDiskCacheConfig(final Context context) {
        return DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPathSupplier(
                        new Supplier<File>() {
                            @Override
                            public File get() {
                                return context.getApplicationContext().getCacheDir();
                            }
                        })
                .setBaseDirectoryName("fresco_cache")
                .setMaxCacheSize(400 * ByteConstants.MB)
                .setMaxCacheSizeOnLowDiskSpace(150 * ByteConstants.MB)
                .setMaxCacheSizeOnVeryLowDiskSpace(20 * ByteConstants.MB)
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        int nightMode = PreferencesHelper.getModeNight(this);
        AppCompatDelegate.setDefaultNightMode(nightMode);

        FileDownloader.setupOnApplicationOnCreate(this);

        mContext = this;

        Log.d("cache_path",getApplicationContext().getCacheDir().getAbsolutePath());

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setDownsampleEnabled(true)
                .setMainDiskCacheConfig(getDefaultMainDiskCacheConfig(this))
                .build();
        try {
            Fresco.initialize(this, config);
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public static boolean isConversionSupported(){
        return isConversionSupported;
    }

    public static Context getGlobalContext(){
        return mContext;
    }

    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}
