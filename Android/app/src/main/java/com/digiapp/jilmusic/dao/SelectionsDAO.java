package com.digiapp.jilmusic.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.beans.SelectionItems;
import com.digiapp.jilmusic.beans.SelectionList;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface SelectionsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSelectionList(SelectionList selectionList);

    @Query("DELETE FROM selections WHERE id=(:id)")
    void deleteById(long id);

    @Query("DELETE FROM selections")
    void deleteAllSelections();

    @Query("DELETE FROM selections WHERE track_id = (:track_id)")
    void deleteSelectionByTrack(long track_id);

    @Query("DELETE FROM selections WHERE parentId = (:parentId) AND track_id = (:track_id)")
    void deleteSelectionByTrackAndParent(long parentId,long track_id);

    @Query("SELECT* FROM selections WHERE parentId = -1") // only ROOT
    List<SelectionList> getAllSelections();

    @Query("SELECT* FROM selections WHERE parentId = (:id)") // parent
    List<SelectionList> getByParent(long id);

    @Query("SELECT* FROM selections WHERE id = (:id)")
    SelectionList getById(long id);

    @Query("SELECT* FROM selections WHERE parentId = (:parentId) AND track_id = (:track_id)")
    SelectionList getByTrackIdInParent(long parentId,long track_id);

}
