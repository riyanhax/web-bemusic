package com.digiapp.jilmusic.offlineView.presenters;

import android.content.ComponentName;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.MusicService;
import com.digiapp.jilmusic.offlineView.views.MediaBrowserProvider;
import com.digiapp.jilmusic.utils.LogHelper;

public class MediaBrowserPresenter implements MediaBrowserProvider {

    MediaBrowserCompat mMediaBrowser;
    View mView;

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnectionFailed() {
                    super.onConnectionFailed();
                    mView.onError("MediaControllerCompat onConnectionFailed");
                }

                @Override
                public void onConnectionSuspended() {
                    super.onConnectionSuspended();

                    mView.onError("MediaControllerCompat onConnectionSuspended");
                }

                @Override
                public void onConnected() {

                    try {
                        MediaControllerCompat mediaController = new MediaControllerCompat(
                                AppObj.getGlobalContext(), mMediaBrowser.getSessionToken());
                        if(mediaController==null){
                            mView.onError("MediaControllerCompat is null");
                            return;
                        }
                        mView.setMediaController(mediaController);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        mView.onError(e.getMessage());
                        return;
                    }

                    mView.onMediaBrowserConnected(mMediaBrowser.getSessionToken());
                }
            };

    public MediaBrowserPresenter(@NonNull View view){
        this.mView = view;
    }

    public void connectMediaBrowser(){
        mMediaBrowser = new MediaBrowserCompat(AppObj.getGlobalContext(),
                new ComponentName(AppObj.getGlobalContext(), MusicService.class), mConnectionCallback, null);
        mMediaBrowser.connect();
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    public interface View{
        void onMediaBrowserConnected(MediaSessionCompat.Token token);
        void setMediaController(MediaControllerCompat mediaControllerCompat);
        void onError(String message);
    }


    public void onStop() {
        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
        }
    }
}
