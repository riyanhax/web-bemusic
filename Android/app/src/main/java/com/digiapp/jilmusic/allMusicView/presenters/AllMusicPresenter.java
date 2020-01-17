package com.digiapp.jilmusic.allMusicView.presenters;

import com.digiapp.jilmusic.offlineView.presenters.MusicTrackViewPresenter;

public class AllMusicPresenter extends MusicTrackViewPresenter {
    @Override
    protected void updateView() {
        super.updateView();

        view().setMenuVisible(true);
    }
}
