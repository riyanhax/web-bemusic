package com.digiapp.jilmusic.onlinePlayerView.models;

import com.digiapp.jilmusic.CallbackListener;

public class WebAppInterface {

    CallbackListener mCallbackListener;

    public WebAppInterface(CallbackListener callbackListener){
        mCallbackListener = callbackListener;
    }

    @org.xwalk.core.JavascriptInterface
    public void getTrackObj(String track_to_mobile) {
        if (mCallbackListener != null) {
            if (!"undefined".equalsIgnoreCase(track_to_mobile)) {
                mCallbackListener.onCustomCallback(CallbackListener.Type.GET_TRACK_OBJECT, track_to_mobile);
            }
        }
    }

    @org.xwalk.core.JavascriptInterface
    public void trackToPlay(String track_to_mobile) {
        if (mCallbackListener != null) {
            if (!"undefined".equalsIgnoreCase(track_to_mobile)) {
                mCallbackListener.onCustomCallback(CallbackListener.Type.TRACK_TO_PLAY, track_to_mobile);
            }
        }
    }

    @org.xwalk.core.JavascriptInterface
    public void userRegister(String user) {
        if (mCallbackListener != null) {
            if (!"undefined".equalsIgnoreCase(user)) {
                mCallbackListener.onCustomCallback(CallbackListener.Type.USER_REGISTER, user);
            }
        }
    }

    @org.xwalk.core.JavascriptInterface
    public void shareMediaItem(String data) {
        if (mCallbackListener != null) {
            if (!"undefined".equalsIgnoreCase(data)) {
                mCallbackListener.onCustomCallback(CallbackListener.Type.SHARE, data);
            }
        }
    }

    @org.xwalk.core.JavascriptInterface
    public void userLogin(String user) {
        if (mCallbackListener != null) {
            if (!"undefined".equalsIgnoreCase(user)) {
                mCallbackListener.onCustomCallback(CallbackListener.Type.USER_LOGIN, user);
            }
        }
    }

    @org.xwalk.core.JavascriptInterface
    public void userLogout(String user) {
        if (mCallbackListener != null) {
            if (!"undefined".equalsIgnoreCase(user)) {
                mCallbackListener.onCustomCallback(CallbackListener.Type.USER_LOGOUT, user);
            }
        }
    }
}
