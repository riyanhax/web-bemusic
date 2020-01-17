package com.digiapp.jilmusic.media;

import com.digiapp.jilmusic.beans.MusicTrack;

public class WebMediaChanged {
    private String action;
    private MusicTrack musicTrack;

    public WebMediaChanged(String action, MusicTrack musicTrack) {
        this.action = action;
        this.musicTrack = musicTrack;
    }

    public String getAction() {
        return action;
    }

    public MusicTrack getMusicTrack() {
        return musicTrack;
    }
}
