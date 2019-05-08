package org.cafydia4.android.datadatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 8/12/14.
 */
public class DataTableLabel {
    private static final String schema =
            "CREATE TABLE labels(_id INTEGER PRIMARY KEY, title TEXT, color INTEGER);";

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
