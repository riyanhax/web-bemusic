package com.digiapp.jilmusic.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.dashboardView.views.DashboardActivity;
import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.receivers.MediaCommandsReceiver;
import com.digiapp.jilmusic.media.WebMediaChanged;
import com.digiapp.jilmusic.utils.DiskCacheHelper;
import com.digiapp.jilmusic.utils.ResourceHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.InputStream;

public class WebViewNotificationManager extends IntentService {

    private static final String TAG = "audio_debug";
    private static final String CHANNEL_ID = "com.example.android.uamp.MUSIC_CHANNEL_ID";
    private static final int NOTIFICATION_ID = 412;

    private int mNotificationColor;

    private Context mContext;
    AudioManager mAudioManager;

    private NotificationManager mNotificationManager;
    private PendingIntent mPauseIntent;
    private PendingIntent mPlayIntent;
    private PendingIntent mNextIntent;
    private PendingIntent mPrevIntent;
    private PendingIntent mCloseIntent;
    private static final int REQUEST_CODE = 0;

    public static final String ACTION_PAUSE = "com.example.android.webviewmediamanager.pause";
    public static final String ACTION_PLAY = "com.example.android.webviewmediamanager.play";
    public static final String ACTION_NEXT = "com.example.android.webviewmediamanager.next";
    public static final String ACTION_PREV = "com.example.android.webviewmediamanager.prev";
    public static final String ACTION_CLOSE = "com.example.android.webviewmediamanager.close";

    private static MusicTrack mCurrentMusicTrack;
    public static String mCurrentAction;

    public WebViewNotificationManager() {
        super(TAG);
    }

    public static MusicTrack getCurrentMusicTrack() {
        return mCurrentMusicTrack;
    }

    @Override
    public void onDestroy() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
        EventBus.getDefault().unregister(this);

        if (mCurrentAction != null
                && mCurrentAction.equalsIgnoreCase("play")) {
            try {
                mPauseIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    public void onEvent(WebMediaChanged event) {
        mCurrentMusicTrack = event.getMusicTrack();
        mCurrentAction = event.getAction();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            beginNotificationBuilder();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    public void onEvent(String action) {
        if (action.equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)) {

            // restart manager
            if (mCurrentAction != null) {
                if (mCurrentAction.equalsIgnoreCase("play")) {
                    // restart player anyway
                    try {
                        mPlayIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }

                // in case of pause we need to recall pause intent
                if (mCurrentAction.equalsIgnoreCase("pause")) {
                    // restart player anyway
                    try {
                        mPauseIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void beginNotificationBuilder() {
        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext, CHANNEL_ID);
        Bitmap art = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.ic_default_art);

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_view_small);
        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher);
        if (mCurrentMusicTrack != null) {
            contentView.setTextViewText(R.id.title, mCurrentMusicTrack.name.toString());
            try {
                if(mCurrentMusicTrack.artists!=null && mCurrentMusicTrack.artists.length>0){
                    contentView.setTextViewText(R.id.artist, mCurrentMusicTrack.artists[0]);
                }else{
                    contentView.setTextViewText(R.id.artist, mCurrentMusicTrack.album_name);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                contentView.setTextViewText(R.id.artist, "-no data-");
            }

        }
        if (mCurrentAction != null) {
            if (mCurrentAction.equalsIgnoreCase("play")) {
                contentView.setImageViewResource(R.id.play_pause, R.drawable.ic_media_pause_dark);
                contentView.setOnClickPendingIntent(R.id.play_pause, mPauseIntent);
            } else if (mCurrentAction.equalsIgnoreCase("pause")) {
                contentView.setImageViewResource(R.id.play_pause, R.drawable.uamp_ic_play_arrow_white_48dp);
                contentView.setOnClickPendingIntent(R.id.play_pause, mPlayIntent);
            }
        }

        contentView.setOnClickPendingIntent(R.id.prev, mPrevIntent);
        contentView.setOnClickPendingIntent(R.id.next, mNextIntent);
        contentView.setOnClickPendingIntent(R.id.close, mCloseIntent);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, DashboardActivity.class), PendingIntent.FLAG_IMMUTABLE);

        Log.d("debug", "notification_created WebMediaNotificationManager");

        notificationBuilder
                .setColor(mNotificationColor)
                .setSmallIcon(R.drawable.ic_notification)
                .setContent(contentView)
                .setContentIntent(contentIntent)
                .setCustomBigContentView(contentView)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getString(R.string.play_from_jilmusic))
                .setContentText(getString(R.string.exlpand_controls))
                .setStyle(new NotificationCompat.BigTextStyle())
                .setLargeIcon(art);

        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;

        // download picture
        new Thread(() -> {
            if(mCurrentMusicTrack!=null) {
                String picPath = mCurrentMusicTrack.album.image;
                if (picPath != null) {
                    InputStream inputStream = DiskCacheHelper.readFromCacheSync(picPath);
                    if (inputStream != null) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap badge = BitmapFactory.decodeStream(inputStream, null, options);
                        contentView.setImageViewBitmap(R.id.image, badge);
                        mNotificationManager.notify(NOTIFICATION_ID, notification);
                    }
                }
            }
        }).start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, notification); // todo
        }
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        mContext = AppObj.getGlobalContext();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mNotificationColor = ResourceHelper.getThemeColor(mContext, R.attr.colorPrimary,
                Color.DKGRAY);

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        Intent mediaPlayerReceiver = new Intent(this, MediaCommandsReceiver.class);
        mediaPlayerReceiver.setAction(ACTION_PAUSE);
        mPauseIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, mediaPlayerReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlay = new Intent(this, MediaCommandsReceiver.class);
        intentPlay.setAction(ACTION_PLAY);
        mPlayIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentNext = new Intent(this, MediaCommandsReceiver.class);
        intentNext.setAction(ACTION_NEXT);
        mNextIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPrev = new Intent(this, MediaCommandsReceiver.class);
        intentPrev.setAction(ACTION_PREV);
        mPrevIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentClose = new Intent(this, MediaCommandsReceiver.class);
        intentClose.setAction(ACTION_CLOSE);
        mCloseIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intentClose, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            beginNotificationBuilder();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);
    }

    /**
     * Creates Notification Channel. This is required in Android O+ to display notifications.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {

        if (mNotificationManager !=null && mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID,
                            mContext.getString(R.string.notification_channel),
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription(
                    mContext.getString(R.string.notification_channel_description));

            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

}
