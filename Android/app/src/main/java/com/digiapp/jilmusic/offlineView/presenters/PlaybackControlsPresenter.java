package com.digiapp.jilmusic.offlineView.presenters;

import android.support.v4.media.MediaMetadataCompat;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.dao.MusicRoomDatabase;
import com.digiapp.jilmusic.dao.MusicTrackDAO;
import com.digiapp.jilmusic.utils.Core;

public class PlaybackControlsPresenter {

    View mView;
    public PlaybackControlsPresenter(View view){
        this.mView = view;
    }

    public void updateByMediaMetadata(MediaMetadataCompat metadataCompat){
        String key = "android.media.metadata.MEDIA_URI";
        if (metadataCompat.getBundle().containsKey(key)) {
            final String id = Core.getMusicId(metadataCompat.getBundle().getString(key));

            new Thread(() -> {
                MusicTrackDAO musicTrackDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).musicTrackDAO();
                MusicTrack musicTrack = musicTrackDAO.getById(Integer.valueOf(id));

                if (musicTrack == null) {
                    return;
                }

                final String name = musicTrack.name.isEmpty() ? metadataCompat.getDescription().getTitle().toString() : musicTrack.name;
                final String album = musicTrack.album_name.isEmpty() ? metadataCompat.getDescription().getSubtitle().toString() : musicTrack.album_name;

                mView.setTitle(name);
                mView.setSubtitle(album);
                if (musicTrack.localPicturePath != null) {
                    mView.setAlbumPicture(musicTrack.localPicturePath);
                }
            }).start();
        } else {
            mView.setTitle(metadataCompat.getDescription().getTitle().toString());
            mView.setSubtitle(metadataCompat.getDescription().getSubtitle().toString());
        }

        int duration = (int) metadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        mView.setMaxDuration(duration);
    }

    public interface View{
        void setTitle(String title);
        void setSubtitle(String value);
        void setMaxDuration(int duration);
        void setAlbumPicture(String path);
    }
}
