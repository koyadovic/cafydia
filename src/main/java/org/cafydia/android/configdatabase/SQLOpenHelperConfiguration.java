package org.cafydia.android.configdatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by usuario on 5/03/14.
 */
public class SQLOpenHelperConfiguration extends android.database.sqlite.SQLiteOpenHelper {
    public static final String DATABASE_NAME = "configuration";
    private static final int VERSION = 1;

    public SQLOpenHelperConfiguration(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ConfigurationTableFood.onCreate(db);
        ConfigurationTableMetabolicRhythms.onCreate(db);
        ConfigurationTableCorrectivesSimple.onCreate(db);
        ConfigurationTableCorrectivesComplex.onCreate(db);
        ConfigurationTableDots.onCreate(db);
        ConfigurationTablePlans.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ConfigurationTableFood.onUpgrade(db, oldVersion, newVersion);
        ConfigurationTableMetabolicRhythms.onUpgrade(db, oldVersion, newVersion);
        ConfigurationTableCorrectivesSimple.onUpgrade(db, oldVersion, newVersion);
        ConfigurationTableCorrectivesComplex.onUpgrade(db, oldVersion, newVersion);
        ConfigurationTableDots.onUpgrade(db, oldVersion, newVersion);
        ConfigurationTablePlans.onUpgrade(db, oldVersion, newVersion);
    }

}
