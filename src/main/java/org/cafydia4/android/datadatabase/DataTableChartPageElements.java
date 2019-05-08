package org.cafydia4.android.datadatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 3/12/14.
 */
public class DataTableChartPageElements {
    private static final String schema =
            "CREATE TABLE chart_page_elements(_id INTEGER PRIMARY KEY, type INTEGER, chart_page_id INTEGER, text_header TEXT, criteria_id INTEGER, statistical_object_id INTEGER);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }


    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        switch (oldVersion){
            case 7:
                db.execSQL("DROP TABLE charts");
                onCreate(db);

        }
    }

}
