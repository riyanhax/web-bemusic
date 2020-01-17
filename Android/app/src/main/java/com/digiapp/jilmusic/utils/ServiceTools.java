package com.digiapp.jilmusic.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.digiapp.jilmusic.AppObj;

import java.util.List;

public class ServiceTools {
    private static String LOG_TAG = ServiceTools.class.getName();

    public static boolean isServiceRunning(String serviceClassName){
        final ActivityManager activityManager = (ActivityManager) AppObj.getGlobalContext().getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }
}
