package org.cafydia4.android.configdatabase;

import android.database.sqlite.SQLiteDatabase;

import org.cafydia4.android.util.C;

import java.util.Date;

/**
 * Created by user on 7/08/14.
 */
public class ConfigurationTableMetabolicRhythms {
    private static final String schema =
            "CREATE TABLE metabolic_rhythms(_id INTEGER PRIMARY KEY, name TEXT, description TEXT," +
            "start_mandatory_type INTEGER, state INTEGER, start_date TEXT, end_date TEXT);";


    public static void onCreate(SQLiteDatabase db){
        db.execSQL(schema);

        // we add the default metabolic rhythm
        db.execSQL("INSERT INTO metabolic_rhythms (_id, name, description, start_mandatory_type, state, start_date, end_date) VALUES " +
        "('1', 'Default', '', ?, ?, ?, '')",
                new String[] {
                        ((Integer) C.STARTING_TYPE_GLOBAL).toString(),
                        ((Integer) C.METABOLIC_RHYTHM_STATE_ENABLED).toString(),
                        ((Long) new Date().getTime()).toString()
                });
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
