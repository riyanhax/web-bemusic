package com.digiapp.jilmusic.dashboardView.models;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.MainActivity;
import com.digiapp.jilmusic.R;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.notification.BaseNotificationItem;
import com.liulishuo.filedownloader.util.FileDownloadHelper;

public class NotificationItem extends BaseNotificationItem {

    private static String NOTIFICATION_CHANNEL_ID = "channel_id";
    private static String NOTIFICATION_CHANNEL_NAME = "offline";

    PendingIntent pendingIntent;
    NotificationCompat.Builder builder;

    public NotificationItem(int id, String title, String desc) {
        super(id, title, desc);
        Intent[] intents = new Intent[1];
        intents[0] = Intent.makeMainActivity(new ComponentName(AppObj.getGlobalContext(),
                MainActivity.class));

        this.pendingIntent = PendingIntent.getActivities(AppObj.getGlobalContext(), 0, intents,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new NotificationCompat.
                Builder(FileDownloadHelper.getAppContext(), NOTIFICATION_CHANNEL_ID);

        builder.setDefaults(Notification.DEFAULT_LIGHTS)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOngoing(true)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentTitle(getTitle())
                .setContentText(desc)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher);

    }

    @Override
    public void show(boolean statusChanged, int status, boolean isShowProgress) {

        String desc = getDesc();
        switch (status) {
            case FileDownloadStatus.pending:
                desc += " pending";
                break;
            case FileDownloadStatus.started:
                desc += " started";
                break;
            case FileDownloadStatus.progress:

                //double perc = (getSofar() / getTotal()) * 100;
                double perc = 0.0;
                if (getSofar() != 0) {
                    float result = (((float) getSofar()) / (float) (getTotal())) * 100f;
                    perc = Math.round(Math.abs(result));
                }

                desc += " " + String.valueOf(perc) + " % ";
                break;
            case FileDownloadStatus.retry:
                desc += " retry";
                break;
            case FileDownloadStatus.error:
                desc += " error";
                break;
            case FileDownloadStatus.paused:
                desc += " paused";
                break;
            case FileDownloadStatus.completed:
                desc += " completed";
                break;
            case FileDownloadStatus.warn:
                desc += " warn";
                break;
        }

        builder.setContentTitle(getTitle())
                .setContentText(desc);


        if (statusChanged) {
            builder.setTicker(desc);
        }

        NotificationManager notificationManager = getManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(notificationChannel);
        }

        builder.setProgress(getTotal(), getSofar(), !isShowProgress);
        notificationManager.notify(getId(), builder.build());

        if (status == FileDownloadStatus.completed) { // remove notification for completed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID);
            }
        }
    }
}
