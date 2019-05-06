package org.cafydia.android.datadatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 7/08/14.
 */
public class DataTableGlucoseTests {
    private static final String schema =
            "CREATE TABLE glucose_tests(_id INTEGER PRIMARY KEY, datetime TEXT, time INTEGER, level INTEGER, metabolic_rhythm_id INTEGER, day_week INTEGER);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
