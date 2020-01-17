package com.digiapp.jilmusic.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.notifications.WebViewNotificationManager;
import com.digiapp.jilmusic.onlinePlayerView.presenters.XWalkWebViewPresenter;
import com.digiapp.jilmusic.utils.LogHelper;

public class MediaCommandsReceiver extends BroadcastReceiver {

    private static final String AVRCP_PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
    private static final String AVRCP_META_CHANGED = "com.android.music.metachanged";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d("music_debug", "Received intent with action " + action);

        switch (action) {
            case WebViewNotificationManager.ACTION_PAUSE:
                XWalkWebViewPresenter.stopPlayback();
                break;
            case WebViewNotificationManager.ACTION_PLAY:
                XWalkWebViewPresenter.playPlayback();

                // sending to car audio
                MusicTrack musicTrack = WebViewNotificationManager.getCurrentMusicTrack();
                if(musicTrack!=null) {
                    try {
                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if(mBluetoothAdapter!=null
                                && mBluetoothAdapter.isEnabled()) {
                            Intent i = new Intent(AVRCP_PLAYSTATE_CHANGED);
                            i.putExtra("id", Long.valueOf(musicTrack.id));
                            i.putExtra("artist", musicTrack.artists[0]);
                            i.putExtra("album", musicTrack.album.name);
                            i.putExtra("track", musicTrack.name);
                            i.putExtra("playing", true);
                            i.putExtra("ListSize", 1);
                            i.putExtra("duration", musicTrack.duration);
                            i.putExtra("position", 0);
                            context.sendBroadcast(i);
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }

                break;
            case WebViewNotificationManager.ACTION_NEXT:
                XWalkWebViewPresenter.nextFromApp();
                break;
            case WebViewNotificationManager.ACTION_PREV:
                XWalkWebViewPresenter.prevFromApp();
                break;
            case WebViewNotificationManager.ACTION_CLOSE:
                Intent intent1 = new Intent(context, WebViewNotificationManager.class);
                context.stopService(intent1);
                break;
            default:
                LogHelper.w("music_debug", "Unknown intent ignored. Action=", action);
        }
    }
}
