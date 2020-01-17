package com.digiapp.jilmusic.offlineView.views;

import com.digiapp.jilmusic.beans.MusicTrack;

import java.util.List;

public interface MainView {
    void showData(List<MusicTrack> data);

    void showLoading();

    void showEmpty();
}
