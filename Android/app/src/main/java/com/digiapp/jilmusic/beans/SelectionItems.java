package com.digiapp.jilmusic.beans;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "selections_items")
public class SelectionItems {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public long id;

    @ColumnInfo(name = "selectionlist_id")
    public String selection_id;

    @ColumnInfo(name = "order")
    public int order;

    @ColumnInfo(name = "track_id")
    public int track_id;

    public SelectionItems(int track_id){
        id = System.currentTimeMillis();
        order = 0;
        this.track_id = track_id;
    }
}
