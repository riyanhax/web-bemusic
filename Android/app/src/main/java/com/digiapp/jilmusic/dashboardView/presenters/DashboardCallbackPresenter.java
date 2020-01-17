package com.digiapp.jilmusic.dashboardView.presenters;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.CallbackListener;
import com.digiapp.jilmusic.api.beans.UserInfo;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.media.WebMediaChanged;
import com.digiapp.jilmusic.notifications.WebViewNotificationManager;
import com.digiapp.jilmusic.utils.Core;
import com.digiapp.jilmusic.utils.ServiceTools;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

public class DashboardCallbackPresenter implements CallbackListener {
    DashboardPresenter mDashboardPresenter;
    Context mContext = AppObj.getGlobalContext();

    public DashboardCallbackPresenter(DashboardPresenter presenter){
        mDashboardPresenter = presenter;
    }

    @Override
    public void onCustomCallback(Type type, Object... objects) {
        Log.d("debug","onCustomCallback " + type);

        if (objects.length > 0) {

            switch (type) {
                case TRACK_TO_PLAY:

                    // clear audio focus anyway
                    AudioManager mAudioManager =
                            (AudioManager) AppObj.getGlobalContext().getSystemService(Context.AUDIO_SERVICE);
                    mAudioManager.requestAudioFocus(
                            null,
                            AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN);
                    try {
                        Gson gson = new Gson();
                        String track_to_mobile = objects[0].toString();

                        JSONObject jsonObject = new JSONObject(track_to_mobile);
                        String action = jsonObject.getString("action");
                        MusicTrack musicTrack = gson.fromJson(jsonObject.getJSONObject("track").toString(), MusicTrack.class);

                        // check if service running
                        if (!ServiceTools.isServiceRunning(WebViewNotificationManager.class.getName())) {
                            Intent msgIntent = new Intent(mContext, WebViewNotificationManager.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                mContext.startForegroundService(msgIntent);
                            } else {
                                mContext.startService(msgIntent);
                            }
                        }

                        // update notification
                        new Thread(() -> EventBus.getDefault().postSticky(new WebMediaChanged(action, musicTrack))).start();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    break;
                case GET_TRACK_OBJECT:
                    try {
                        try {
                            mDashboardPresenter.getTrackObj(objects[0].toString());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;

                case USER_LOGOUT:
                    Core.setUserLogged(mContext, false);
                    mDashboardPresenter.mView.updateUserUI(null);
                    break;

                case USER_LOGIN:
                case USER_REGISTER:

                    Gson gson = new Gson();
                    try {
                        UserInfo userInfo = gson.fromJson(objects[0].toString(), UserInfo.class);

                        Core.setUserLogged(mContext, true);
                        Core.setUserInfo(mContext, userInfo);
                        // updateUserUI(userInfo);
                        mDashboardPresenter.checkUserInfo();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    break;
                case SHARE:
                    try {
                        JSONObject data = new JSONObject(objects[0].toString());
                        String shareUrl = data.getString("shareUrl");

                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setType("*/*");
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
                        mContext.startActivity(Intent.createChooser(sharingIntent, "Share"));

                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                    break;
            }
        }
    }

}
