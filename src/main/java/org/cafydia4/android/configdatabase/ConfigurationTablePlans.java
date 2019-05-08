package org.cafydia4.android.configdatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 8/08/14.
 */
public class ConfigurationTablePlans {
    private static final String schema = "CREATE TABLE plans(_id INTEGER PRIMARY KEY, " +
    "name TEXT, description TEXT, metabolic_rhythm_id INTEGER, corrective_type INTEGER, " +
    "corrective_id INTEGER, triggers INTEGER);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
