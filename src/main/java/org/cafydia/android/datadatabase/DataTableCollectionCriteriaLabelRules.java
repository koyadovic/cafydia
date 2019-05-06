package org.cafydia.android.datadatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 29/01/15.
 */
public class DataTableCollectionCriteriaLabelRules {

    // now version 8, old 7

    private static final String schema =
            "CREATE TABLE data_collection_criteria_label_rules (_id INTEGER PRIMARY KEY, data_collection_criteria_id INTEGER, action INTEGER, label_id INTEGER);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        switch (oldVersion){
            case 7:
                onCreate(db);

        }
    }
}
