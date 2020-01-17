package com.digiapp.jilmusic.offlineView.models;

import android.widget.SeekBar;

import com.digiapp.jilmusic.offlineView.presenters.MediaControllerPresenter;

public class SeekBarChangeMediaListener implements SeekBar.OnSeekBarChangeListener {
    MediaControllerPresenter mPresenter;

    public SeekBarChangeMediaListener(MediaControllerPresenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // mStart.setText(DateUtils.formatElapsedTime(progress / 1000));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mPresenter.stopSeekbarUpdate();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mPresenter.seekTo(seekBar.getProgress());
    }
}
