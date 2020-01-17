package com.digiapp.jilmusic.beans;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.R;

@Entity(tableName = "selections")
public class SelectionList extends Object {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "order")
    public int order;

    @ColumnInfo(name = "parentId")
    public long parentId;

    @ColumnInfo(name = "track_id")
    public int track_id;

    public SelectionList(){
        // id = System.currentTimeMillis();
        name = AppObj.getGlobalContext().getString(R.string.text_no_name);
        order = 0;
        parentId = -1;
        track_id = -1;
    }

    public SelectionList(SelectionList selectionList){
        // id = System.currentTimeMillis();
        name = AppObj.getGlobalContext().getString(R.string.text_no_name);
        order = 0;
        parentId = selectionList.id;
        track_id = -1;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((SelectionList) o).id;
    }

    @Override
    public int hashCode() {
        return (int) id;
    }
}
