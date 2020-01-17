package com.digiapp.jilmusic.favouritesView.models;

import android.app.Activity;
import android.content.Context;

import com.digiapp.jilmusic.offlineView.models.AbstractAdapter;
import com.digiapp.jilmusic.offlineView.presenters.MusicTrackViewPresenter;
import com.digiapp.jilmusic.favouritesView.presenters.FavouritesPresenter;

public class FavouritesAdapter extends AbstractAdapter {

    protected Activity mActivity;
    public FavouritesAdapter(Activity activity){
        mActivity = activity;
    }
    @Override
    public MusicTrackViewPresenter getPresenter() {
        return new FavouritesPresenter();
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

}
