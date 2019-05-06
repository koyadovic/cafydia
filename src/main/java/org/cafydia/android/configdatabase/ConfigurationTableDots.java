package org.cafydia.android.configdatabase;

import android.database.sqlite.SQLiteDatabase;

import org.cafydia.android.util.C;

import java.util.ArrayList;

/**
 * Created by user on 7/08/14.
 */
public class ConfigurationTableDots {
    private static final String schema = "CREATE TABLE dots(_id INTEGER PRIMARY KEY, type INTEGER, " +
    "metabolic_rhythm_id INTEGER, x REAL, y REAL);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);

        // we add a default dots of 0.0 modification at the third day.
        ArrayList<Integer> types = new ArrayList<Integer>();
        types.add(C.DOT_TYPE_PREPRANDIAL_INSULIN_GLOBAL);
        types.add(C.DOT_TYPE_PREPRANDIAL_INSULIN_BREAKFAST);
        types.add(C.DOT_TYPE_PREPRANDIAL_INSULIN_LUNCH);
        types.add(C.DOT_TYPE_PREPRANDIAL_INSULIN_DINNER);
        types.add(C.DOT_TYPE_BASAL_INSULIN_BREAKFAST);
        types.add(C.DOT_TYPE_BASAL_INSULIN_LUNCH);
        types.add(C.DOT_TYPE_BASAL_INSULIN_DINNER);

        for(Integer type : types){
            db.execSQL("INSERT INTO dots(type, metabolic_rhythm_id, x, y) VALUES (?, ?, ?, ?)",
                    new String[] {
                            type.toString(),
                            "1",
                            "3",
                            "0.0"
                    });

        }
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
