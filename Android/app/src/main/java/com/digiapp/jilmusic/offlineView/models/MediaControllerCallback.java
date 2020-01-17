package com.digiapp.jilmusic.offlineView.models;

import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.digiapp.jilmusic.offlineView.presenters.MediaControllerPresenter;
import com.digiapp.jilmusic.utils.LogHelper;

public class MediaControllerCallback extends MediaControllerCompat.Callback {

    private static final String TAG = LogHelper.makeLogTag(MediaControllerCallback.class);
    MediaControllerPresenter mPresenter;

    public MediaControllerCallback(MediaControllerPresenter presenter){
        mPresenter = presenter;
    }

    @Override
    public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
        LogHelper.d(TAG, "Received playback state change to state ", state.getState());
        mPresenter.onPlaybackStateChanged(state);
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        LogHelper.d(TAG, "Received metadata state change to mediaId=",
                metadata.getDescription().getMediaId(),
                " song=", metadata.getDescription().getTitle());
        mPresenter.onMetadataChanged(metadata);
    }
}
