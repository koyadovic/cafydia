package org.cafydia4.android.configdatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 7/08/14.
 */
public class ConfigurationTableFood {
    private static final String schema = "CREATE TABLE food(_id INTEGER PRIMARY KEY, name TEXT, type INTEGER, " +
            "favorite INTEGER, c_percent REAL, unit_weight REAL);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    }

}
