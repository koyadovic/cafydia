package org.cafydia.android.datadatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 26/11/14.
 */
public class DataTableAnnotations {
    private static final String schema =
            "CREATE TABLE annotations(_id INTEGER PRIMARY KEY, datetime TEXT, created_by INTEGER, scope INTEGER, annotation TEXT);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        switch (oldVersion){
            case 1:
                // porque en la versión 1 aún no existía la tabla
                onCreate(db);
            case 2:
                db.execSQL("DROP TABLE annotations;");
                onCreate(db);

        }
    }
}
