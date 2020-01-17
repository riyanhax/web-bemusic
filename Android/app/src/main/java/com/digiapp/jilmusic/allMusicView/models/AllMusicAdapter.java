package com.digiapp.jilmusic.allMusicView.models;

import android.app.Activity;
import android.content.Context;

import com.digiapp.jilmusic.offlineView.models.AbstractAdapter;
import com.digiapp.jilmusic.offlineView.presenters.MusicTrackViewPresenter;
import com.digiapp.jilmusic.allMusicView.presenters.AllMusicPresenter;

public class AllMusicAdapter extends AbstractAdapter {
    private Activity mActivity;

    public AllMusicAdapter(Activity activity) {
        mActivity = activity;
    }

    @Override
    public MusicTrackViewPresenter getPresenter() {
        return new AllMusicPresenter();
    }

    @Override
    public Context getContext() {
        return mActivity;
    }
}
