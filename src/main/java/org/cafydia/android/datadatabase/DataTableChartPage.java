package org.cafydia.android.datadatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 3/12/14.
 */
public class DataTableChartPage {
    private static final String schema =
            "CREATE TABLE chart_pages(_id INTEGER PRIMARY KEY, order_number INTEGER, title TEXT, criteria_id INTEGER);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }


    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        switch (oldVersion){
            case 7:
                db.execSQL("ALTER TABLE chart_pages ADD COLUMN criteria_id INTEGER");

        }
    }
}
