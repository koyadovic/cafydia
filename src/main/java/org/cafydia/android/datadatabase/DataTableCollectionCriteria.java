package org.cafydia.android.datadatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 4/02/15.
 */
public class DataTableCollectionCriteria {
    // now version 8, old 7

    private static final String schema =
            "CREATE TABLE data_collection_criteria (_id INTEGER PRIMARY KEY, since_type INTEGER, since INTEGER, until_type INTEGER, until INTEGER, day_weeks_activated INTEGER, day_weeks INTEGER, meals_activated INTEGER, meals INTEGER);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        switch (oldVersion){
            case 9:
                db.execSQL("DROP TABLE data_collection_criteria");
                onCreate(db);

        }
    }

}
