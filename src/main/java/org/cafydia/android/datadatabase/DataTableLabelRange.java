package org.cafydia.android.datadatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 8/12/14.
 */
public class DataTableLabelRange {

    private static final String schema =
            "CREATE TABLE label_ranges(_id INTEGER PRIMARY KEY, label_id INTEGER, start INTEGER, end INTEGER);";


    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        switch (oldVersion){
            case 1:
            case 2:
            case 3:
                onCreate(db);
        }
    }

}
