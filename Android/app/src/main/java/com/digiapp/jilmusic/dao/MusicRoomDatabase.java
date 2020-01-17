package com.digiapp.jilmusic.dao;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.beans.SelectionItems;
import com.digiapp.jilmusic.beans.SelectionList;

import static com.digiapp.jilmusic.dao.Migrations.MIGRATION_2_3;

@Database(entities = {MusicTrack.class, SelectionList.class, SelectionItems.class},
        version = 3,
        exportSchema = false)
public abstract class MusicRoomDatabase extends RoomDatabase {

    private static MusicRoomDatabase INSTANCE;

    public static MusicRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MusicRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MusicRoomDatabase.class, "music_database")
                            .addMigrations(MIGRATION_2_3)
                            .allowMainThreadQueries() // FIXME
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract MusicTrackDAO musicTrackDAO();
    public abstract SelectionsDAO selectionsDAO();
}
