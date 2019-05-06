package org.cafydia.android.datadatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 12/10/14.
 */
public class DataTableSnacks {
    private static final String schema = "CREATE TABLE snacks (_id INTEGER PRIMARY KEY, datetime TEXT, " +
            "snack INTEGER, carbohydrates REAL, metabolic_rhythm_id INTEGER, metabolic_rhythm_name TEXT," +
            "food_selected_json TEXT, day_week INTEGER);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
