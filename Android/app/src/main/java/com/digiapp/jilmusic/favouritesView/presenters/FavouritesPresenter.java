package com.digiapp.jilmusic.favouritesView.presenters;

import com.digiapp.jilmusic.offlineView.presenters.MusicTrackViewPresenter;

public class FavouritesPresenter extends MusicTrackViewPresenter {
    @Override
    public void updateView() {
        super.updateView();

        view().setMenuVisible(false);
    }
}
