package com.digiapp.jilmusic.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
@SuppressLint("OverrideAbstract")
public class NotificationListener extends NotificationListenerService {
    public NotificationListener() {
    }

    // Helper method to check if our notification listener is enabled. In order to get active media
    // sessions, we need an enabled notification listener component.
    public static boolean isEnabled(Context context) {
        return NotificationManagerCompat
                .getEnabledListenerPackages(context)
                .contains(context.getPackageName());
    }
}
