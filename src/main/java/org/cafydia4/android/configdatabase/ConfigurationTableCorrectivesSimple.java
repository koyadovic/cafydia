package org.cafydia4.android.configdatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 7/08/14.
 */
public class ConfigurationTableCorrectivesSimple {
    private static final String schema =
            "CREATE TABLE simple_correctives(_id INTEGER PRIMARY KEY, name TEXT, description TEXT, type INTEGER, " +
                    "metabolic_rhythm_id INTEGER, modification_type INTEGER, modification REAL, visible INTEGER, triggers INTEGER);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
