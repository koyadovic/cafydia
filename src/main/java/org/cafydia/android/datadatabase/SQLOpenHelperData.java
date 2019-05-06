package org.cafydia.android.datadatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by user on 19/08/14.
 */
public class SQLOpenHelperData extends android.database.sqlite.SQLiteOpenHelper  {
    public static final String DATABASE_NAME = "data";
    private static final int VERSION = 11;

    public SQLOpenHelperData(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DataTableMeals.onCreate(db);
        DataTableGlucoseTests.onCreate(db);
        DataTableSnacks.onCreate(db);
        DataTableHbA1c.onCreate(db);

        DataTableAnnotations.onCreate(db);

        DataTableChartPage.onCreate(db);
        DataTableChartPageElements.onCreate(db);

        DataTableLabel.onCreate(db);
        DataTableLabelRange.onCreate(db);

        DataTableCollectionCriteria.onCreate(db);
        DataTableCollectionCriteriaLabelRules.onCreate(db);
        DataTableStatisticalObjects.onCreate(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DataTableMeals.onUpgrade(db, oldVersion, newVersion);
        DataTableGlucoseTests.onUpgrade(db, oldVersion, newVersion);
        DataTableSnacks.onUpgrade(db, oldVersion, newVersion);
        DataTableHbA1c.onUpgrade(db, oldVersion, newVersion);

        DataTableAnnotations.onUpgrade(db, oldVersion, newVersion);

        DataTableChartPage.onUpgrade(db, oldVersion, newVersion);
        DataTableChartPageElements.onUpgrade(db, oldVersion, newVersion);

        DataTableLabel.onUpgrade(db, oldVersion, newVersion);
        DataTableLabelRange.onUpgrade(db, oldVersion, newVersion);


        DataTableCollectionCriteria.onUpgrade(db, oldVersion, newVersion);
        DataTableCollectionCriteriaLabelRules.onUpgrade(db, oldVersion, newVersion);
        DataTableStatisticalObjects.onUpgrade(db, oldVersion, newVersion);
    }

}
