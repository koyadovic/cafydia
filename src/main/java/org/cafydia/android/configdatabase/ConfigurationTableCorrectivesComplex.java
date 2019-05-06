package org.cafydia.android.configdatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 7/08/14.
 */
public class ConfigurationTableCorrectivesComplex {
    private static final String schema =
            "CREATE TABLE complex_correctives(_id INTEGER PRIMARY KEY, name TEXT, description TEXT, type INTEGER, " +
                    "metabolic_rhythm_id INTEGER, modification_type INTEGER, modification_breakfast REAL, " +
                    "modification_lunch REAL, modification_dinner REAL, visible INTEGER, triggers INTEGER);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
