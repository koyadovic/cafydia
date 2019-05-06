package org.cafydia.android.datadatabase;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 7/08/14.
 */
public class DataTableMeals {
    private static final String schema = "CREATE TABLE meals(_id INTEGER PRIMARY KEY, datetime TEXT, " +
            "meal TEXT, carbohydrates REAL, metabolic_rhythm_id INTEGER, metabolic_rhythm_name TEXT, " +
            "baseline_preprandial REAL, baseline_basal REAL, metabolic_preprandial REAL, " +
            "metabolic_basal REAL, correction_factor_preprandial REAL, correctives_preprandial REAL, " +
            "correctives_name TEXT, food_selected_json TEXT, day_week INTEGER, final_preprandial REAL);";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        if(oldVersion < 11) {
            db.execSQL("ALTER TABLE meals ADD COLUMN final_preprandial REAL");
        }

    }
}
