package com.digiapp.jilmusic;

public interface CallbackListener {

    public enum Type{
        GET_TRACK_OBJECT,
        USER_REGISTER,
        USER_LOGIN,
        USER_LOGOUT,
        TRACK_TO_PLAY,
        SHARE
    }

    public void onCustomCallback(Type type,Object...params);
}
