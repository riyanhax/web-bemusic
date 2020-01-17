package com.digiapp.jilmusic.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.digiapp.jilmusic.beans.MusicTrack;

import java.util.List;

@Dao
public interface MusicTrackDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MusicTrack musicTrack);

    @Query("DELETE FROM music_track")
    void deleteAll();

    @Query("DELETE FROM music_track WHERE id = (:id)")
    void deleteById(int id);

    @Query("SELECT * from music_track ORDER BY id ASC")
    List<MusicTrack> getAllMusicTracks();

    @Query("SELECT * from music_track WHERE id = (:id)")
    MusicTrack getById(int id);

    @Query("SELECT * from music_track WHERE favorite = 1")
    List<MusicTrack> getAllFavorite();

}
