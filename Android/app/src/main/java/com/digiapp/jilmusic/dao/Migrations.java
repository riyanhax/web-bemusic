package com.digiapp.jilmusic.dao;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;

/**
 * Created by artembogomaz on 3/13/2018.
 */

public class Migrations {
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

            database.execSQL("" +
                    "" +
                    " ALTER TABLE 'music_track' ADD COLUMN 'isVideo' INTEGER NOT NULL DEFAULT 0" +
                    "");
            database.setVersion(3);
        }
    };

}
