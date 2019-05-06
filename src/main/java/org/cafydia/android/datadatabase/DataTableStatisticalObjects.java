package org.cafydia.android.datadatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 4/02/15.
 */
public class DataTableStatisticalObjects {
    private static final String schema = "CREATE TABLE statistical_objects (_id INTEGER PRIMARY KEY, chart_page_element_id INTEGER, configuration INTEGER);";

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
