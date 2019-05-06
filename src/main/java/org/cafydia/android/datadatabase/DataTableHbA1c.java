package org.cafydia.android.datadatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 12/10/14.
 */
public class DataTableHbA1c {
    private static final String schema = "CREATE TABLE hba1c (_id INTEGER PRIMARY KEY, datetime TEXT, " +
            "metabolic_rhythm_id INTEGER, metabolic_rhythm_name TEXT, percentage REAL, mmol_mol REAL);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
