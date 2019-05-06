package org.cafydia.android.datadatabase;

import android.app.backup.BackupManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import org.cafydia.android.chartobjects.ChartPage;
import org.cafydia.android.chartobjects.ChartPageElement;
import org.cafydia.android.chartobjects.DataCollectionCriteria;
import org.cafydia.android.chartobjects.DataCollectionCriteriaInstant;
import org.cafydia.android.chartobjects.DataCollectionLabelRule;
import org.cafydia.android.chartobjects.GlucoseTestsCrossedMeals;
import org.cafydia.android.chartobjects.Label;
import org.cafydia.android.chartobjects.LabelRange;
import org.cafydia.android.chartobjects.StatisticalObject;
import org.cafydia.android.core.Annotation;
import org.cafydia.android.core.Food;
import org.cafydia.android.core.GlucoseTest;
import org.cafydia.android.core.HbA1c;
import org.cafydia.android.core.Instant;
import org.cafydia.android.core.Meal;
import org.cafydia.android.core.Snack;
import org.cafydia.android.util.C;

import java.util.ArrayList;

/**
 * Created by user on 19/08/14.
 */
public class DataDatabase {
    private Context mContext;

    private final SQLOpenHelperData mHelperData;
    private SQLiteDatabase mDatabase;
    private BackupManager mBackupManager;

    private static final String LOG_TAG = "DataDatabase-LOG-TAG";

    private boolean openedWritable = false;
    private boolean openedReadable = false;

    public static final Object[] dbLock = new Object[0];

    public DataDatabase(Context context){
        mHelperData = new SQLOpenHelperData(context);
        mBackupManager = new BackupManager(context);
        mContext = context;
    }

    public boolean openWritable(){
        if(!openedWritable) {
            openedWritable = true;
            openedReadable = false;
            mDatabase = mHelperData.getWritableDatabase();
            return true;
        } else {
            return false;
        }
    }

    public boolean openReadable(){
        if(openedWritable || openedReadable){
            return false;
        } else {
            mDatabase = mHelperData.getReadableDatabase();
            openedReadable = true;
            openedWritable = false;
            return true;
        }
    }

    public void close() {
        if(mDatabase != null) {
            mDatabase.close();
        }

        if(openedWritable) {
            mBackupManager.dataChanged();
            openedWritable = false;
        }
        else if(openedReadable) {
            openedReadable = false;
        }
    }

    //public Cursor query(String sql, String[] params ) { return mDatabase.rawQuery(sql, params); }



    public synchronized GlucoseTest getLastGlucoseTestAdded(){
        GlucoseTest g = null;
        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM glucose_tests ORDER BY datetime DESC LIMIT 1", null);

        if(cursor.moveToFirst()){
            g = buildGlucoseTestObject(cursor);
            cursor.close();
        }

        if(newlyOpened) close();
        return g;
    }

    public synchronized GlucoseTest getLastBeforeMealGlucoseTestAdded(int meal){
        GlucoseTest g = null;

        int time;

        switch (meal){
            case C.MEAL_BREAKFAST:
                time = C.GLUCOSE_TEST_BEFORE_BREAKFAST;
                break;

            case C.MEAL_LUNCH:
                time = C.GLUCOSE_TEST_BEFORE_LUNCH;
                break;

            case C.MEAL_DINNER:
                time = C.GLUCOSE_TEST_BEFORE_DINNER;
                break;

            default:
                return null;
        }

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM glucose_tests WHERE time=? ORDER BY datetime DESC LIMIT 1", new String[] { time + "" });

        if(cursor.moveToFirst()){
            g = buildGlucoseTestObject(cursor);
            cursor.close();
        }

        if(newlyOpened) close();

        return g;
    }

    public synchronized GlucoseTest getLastAfterMealGlucoseTestAdded(int meal){
        GlucoseTest g = null;

        int time;

        switch (meal){
            case C.MEAL_BREAKFAST:
                time = C.GLUCOSE_TEST_AFTER_BREAKFAST;
                break;

            case C.MEAL_LUNCH:
                time = C.GLUCOSE_TEST_AFTER_LUNCH;
                break;

            case C.MEAL_DINNER:
                time = C.GLUCOSE_TEST_AFTER_DINNER;
                break;

            default:
                return null;
        }

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM glucose_tests WHERE time=? ORDER BY datetime DESC LIMIT 1", new String[] { time + "" });

        if(cursor.moveToFirst()){
            g = buildGlucoseTestObject(cursor);
            cursor.close();
        }

        if(newlyOpened) close();

        return g;
    }





    public synchronized ArrayList<GlucoseTest> getGlucoseTestsByCriteria(DataCollectionCriteria criteria){
        ArrayList<GlucoseTest> glucoseTests = new ArrayList<>();

        String query = "SELECT * FROM glucose_tests WHERE ";

        Instant sinceInstant = criteria.getSinceInstant();
        Instant untilInstant = criteria.getUntilInstant();

        if(sinceInstant != null && untilInstant != null) {
            if(!(criteria.getSince().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE) && criteria.getSince().getData() > -1)){
                query += "datetime>'" + sinceInstant
                        .setTimeToTheMorning()
                        .getInternalDateTimeString() + "' AND ";
            }

            query += "datetime<'" + untilInstant
                    .increaseOneDay()
                    .setTimeToTheMorning()
                    .getInternalDateTimeString() + "' ";

            // day weeks
            if (criteria.getDayWeeksActivated().equals(C.DATA_COLLECTION_CRITERIA_ACTIVATED) && criteria.getDayWeeksInteger() > 0) {
                query += "AND day_week IN (";
                String days = "";
                for (int a = 0; a < 7; a++) {
                    if (criteria.collectOnDayWeek(a)) {
                        if (days.equals("")) {
                            days += Integer.toString(a);
                        } else {
                            days += "," + Integer.toString(a);
                        }
                    }
                }
                query += days + ") ";
            }


            // glucose time, before and after each meal
            if (criteria.getMealTimesActivated().equals(C.DATA_COLLECTION_CRITERIA_ACTIVATED) && criteria.getMealTimeInteger() > 0) {
                query += "AND time IN (";
                String times = "";
                for (int a = 0; a < 7; a++) {
                    if (criteria.collectOnMealTime(a)) {
                        if (times.equals("")) {
                            times += Integer.toString(a);
                        } else {
                            times += "," + Integer.toString(a);
                        }
                    }
                }
                query += times + ") ";
            }

            boolean firstIteration = true;

            // to check that not will be provided rules for include and exclude at the same time
            boolean include = false;
            boolean exclude = false;

            boolean allEmpty = true;

            ArrayList<DataCollectionLabelRule> rules = criteria.getLabelRules();
            if (rules != null) {
                for (DataCollectionLabelRule rule : rules) {
                    Label label = rule.getLabel();

                    if(label != null) {
                        if(label.getRangeCount() > 0) {
                            allEmpty = false;

                            if (rule.getAction() == C.LABEL_RULE_ACTION_INCLUDE) {
                                include = true;

                                if (firstIteration) {
                                    query += "AND (";
                                    firstIteration = false;
                                }
                            } else if (rule.getAction() == C.LABEL_RULE_ACTION_EXCLUDE) {
                                exclude = true;

                                if (firstIteration) {
                                    query += "AND NOT (";
                                    firstIteration = false;
                                }
                            }

                            if (include && exclude) {
                                // because including one label, this excludes all others and vice versa.
                                throw new IllegalArgumentException();
                            }

                            for (int a = 0; a < label.getRangeCount(); a++) {
                                if (!query.substring(query.length() - 1).equals("(")) {
                                    query += "OR ";
                                }
                                query += "(datetime>'" + label.getRangeAtIndex(a).getStartInTheMorning().getInternalDateTimeString() + "' ";
                                query += "AND datetime<'" + label.getRangeAtIndex(a).getEndIncreasedOneDayInTheMorning().getInternalDateTimeString() + "') ";
                            }

                        }
                    }
                }

                if(rules.size() > 0 && rules.get(0).getAction() == C.LABEL_RULE_ACTION_INCLUDE && allEmpty)
                    return glucoseTests;

                // si es false es porque entró en el bucle y hay que cerrar el paréntesis
                if (!firstIteration) query += ") ";
            }

            boolean newlyOpened = openReadable();

            Cursor cursor = mDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    //_id INTEGER PRIMARY KEY, datetime TEXT, time INTEGER, level INTEGER, metabolic_rhythm_id INTEGER, day_week INTEGER
                    glucoseTests.add(buildGlucoseTestObject(cursor));

                } while (cursor.moveToNext());

                cursor.close();
            }

            if(newlyOpened) close();
        }

        return glucoseTests;
    }

    public synchronized GlucoseTestsCrossedMeals getGlucoseTestsCrossedMealsByCriteria(DataCollectionCriteria criteria){
        String query = "SELECT * FROM meals WHERE ";

        Instant sinceInstant = criteria.getSinceInstant();
        Instant untilInstant = criteria.getUntilInstant();

        if(!(criteria.getSince().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE) && criteria.getSince().getData() > -1)){
            query += "datetime>'" + sinceInstant
                    .setTimeToTheMorning()
                    .getInternalDateTimeString() + "' AND ";
        }

        query += "datetime<'" + untilInstant
                .increaseOneDay()
                .setTimeToTheMorning()
                .getInternalDateTimeString() + "' ";

        // day weeks
        if(criteria.getDayWeeksInteger() > 0) {
            query += "AND day_week IN (";
            String days = "";
            for (int a = 0; a < 7; a++) {
                if (criteria.collectOnDayWeek(a)) {
                    if (days.equals("")) {
                        days += Integer.toString(a);
                    } else {
                        days += "," + Integer.toString(a);
                    }
                }
            }
            query += days + ") ";
        }

        // meals
        if(criteria.getMealTimeInteger() > 0) {
            query += "AND meal IN(";

            if(criteria.collectOnMealTime(C.GLUCOSE_TEST_AFTER_BREAKFAST) || criteria.collectOnMealTime(C.GLUCOSE_TEST_BEFORE_BREAKFAST)) {
                query += "'breakfast'";
            }

            if(criteria.collectOnMealTime(C.GLUCOSE_TEST_AFTER_LUNCH) || criteria.collectOnMealTime(C.GLUCOSE_TEST_BEFORE_LUNCH)){
                if (query.substring(query.length() - 1).equals("(")) {
                    query += "'lunch'";
                } else {
                    query += ",'lunch'";
                }
            }
            if(criteria.collectOnMealTime(C.GLUCOSE_TEST_AFTER_DINNER) ||
                    criteria.collectOnMealTime(C.GLUCOSE_TEST_BEFORE_DINNER) ||
                    criteria.collectOnMealTime(C.GLUCOSE_TEST_IN_THE_NIGHT)) {

                if (query.substring(query.length() - 1).equals("(")) {
                    query += "'dinner'";
                } else {
                    query += ",'dinner'";
                }
            }
            query += ") ";
        }

        boolean firstIteration = true;

        // to check that not will be provided rules for include and exclude at the same time
        boolean include = false;
        boolean exclude = false;

        ArrayList<DataCollectionLabelRule> rules = criteria.getLabelRules();
        if(rules != null) {

            for (DataCollectionLabelRule rule : rules) {
                Label label = rule.getLabel();

                if (label != null && label.getRangeCount() > 0) {

                    if (rule.getAction() == C.LABEL_RULE_ACTION_INCLUDE) {
                        include = true;

                        if (firstIteration) {
                            query += "AND (";
                            firstIteration = false;
                        }

                    } else if (rule.getAction() == C.LABEL_RULE_ACTION_EXCLUDE) {
                        exclude = true;

                        if (firstIteration) {
                            query += "AND NOT (";
                            firstIteration = false;
                        }
                    }

                    if (include && exclude) {
                        // because including one label, this excludes all others and vice versa.
                        throw new IllegalArgumentException();
                    }

                    for (int a = 0; a < label.getRangeCount(); a++) {
                        if (!query.substring(query.length() - 1).equals("(")) {
                            query += "OR ";
                        }
                        query += "(datetime>'" + label.getRangeAtIndex(a).getStartInTheMorning().getInternalDateTimeString() + "' ";
                        query += "AND datetime<'" + label.getRangeAtIndex(a).getEndIncreasedOneDayInTheMorning().getInternalDateTimeString() + "') ";
                    }

                }
            }
            // si es false es porque entró en el bucle y hay que cerrar el paréntesis
            if (!firstIteration) query += ") ";

        }

        boolean newlyOpened = openReadable();
        Cursor cursor = mDatabase.rawQuery(query, null);

        ArrayList<Meal> meals = new ArrayList<>();

        if(cursor.moveToFirst()) {
            do {
                meals.add(buildMealObject(cursor));
            }while(cursor.moveToNext());
        }

        if(newlyOpened) close();

        GlucoseTestsCrossedMeals testsCrossedMeals = new GlucoseTestsCrossedMeals();

        if(meals.size() > 0) {
            newlyOpened = openReadable();

            for(Meal m : meals) {
                String date = m.getInternalDateString();
                String dateTomorrow = new Instant(m.toDate().getTime()).increaseOneDay().getInternalDateString();

                String q = "";
                boolean night = false;

                if(m.getMealTime().equals(C.MEAL_DINNER) && criteria.collectOnMealTime(C.GLUCOSE_TEST_IN_THE_NIGHT)) {
                    night = true;
                    q = "SELECT * FROM glucose_tests WHERE (datetime LIKE ? AND time IN (6)) OR (datetime LIKE ? ";

                } else {
                    q = "SELECT * FROM glucose_tests WHERE datetime LIKE ? ";
                }

                ArrayList<Integer> ts = new ArrayList<>();

                switch (m.getMealTime()) {
                    case C.MEAL_BREAKFAST:
                        if(criteria.collectOnMealTime(C.GLUCOSE_TEST_AFTER_BREAKFAST)) {
                            ts.add(C.GLUCOSE_TEST_AFTER_BREAKFAST);
                        }
                        if(criteria.collectOnMealTime(C.GLUCOSE_TEST_BEFORE_BREAKFAST)) {
                            ts.add(C.GLUCOSE_TEST_BEFORE_BREAKFAST);
                        }
                        break;

                    case C.MEAL_LUNCH:
                        if(criteria.collectOnMealTime(C.GLUCOSE_TEST_AFTER_LUNCH)) {
                            ts.add(C.GLUCOSE_TEST_AFTER_LUNCH);
                        }
                        if(criteria.collectOnMealTime(C.GLUCOSE_TEST_BEFORE_LUNCH)) {
                            ts.add(C.GLUCOSE_TEST_BEFORE_LUNCH);
                        }
                        break;

                    case C.MEAL_DINNER:
                        if(criteria.collectOnMealTime(C.GLUCOSE_TEST_AFTER_DINNER)) {
                            ts.add(C.GLUCOSE_TEST_AFTER_DINNER);
                        }
                        if(criteria.collectOnMealTime(C.GLUCOSE_TEST_BEFORE_DINNER)) {
                            ts.add(C.GLUCOSE_TEST_BEFORE_DINNER);
                        }
                        break;
                }

                if(criteria.getMealTimeInteger() > 0) {
                    q += "AND time IN (";
                    String times = "";
                    for (int a = 0; a < 6; a++) {
                        if (criteria.collectOnMealTime(a) && ts.contains(a)) {
                            if (times.equals("")) {
                                times += Integer.toString(a);
                            } else {
                                times += "," + Integer.toString(a);
                            }
                        }
                    }
                    q += times + ") ";
                }

                Cursor c2;


                if(night) {
                    q += ") ";

                    c2 = mDatabase.rawQuery(q, new String[] { dateTomorrow + "%", date + "%" });
                } else {
                    c2 = mDatabase.rawQuery(q, new String[] { date + "%" });
                }

                if(c2.moveToFirst()) {
                    testsCrossedMeals.newElement();
                    testsCrossedMeals.getCurrentElement().setMeal(m);
                    do {
                        GlucoseTest test = buildGlucoseTestObject(c2);

                        if((test.getGlucoseTime().equals(C.GLUCOSE_TEST_AFTER_BREAKFAST) && m.getMealTime().equals(C.MEAL_BREAKFAST)) ||
                                (test.getGlucoseTime().equals(C.GLUCOSE_TEST_AFTER_LUNCH)  && m.getMealTime().equals(C.MEAL_LUNCH))||
                                (test.getGlucoseTime().equals(C.GLUCOSE_TEST_AFTER_DINNER) && m.getMealTime().equals(C.MEAL_DINNER)) ||
                                (test.getGlucoseTime().equals(C.GLUCOSE_TEST_IN_THE_NIGHT)  && m.getMealTime().equals(C.MEAL_DINNER))) {

                            testsCrossedMeals.getCurrentElement().setGlucoseTestAfterMeal(test);
                        }
                        else if((test.getGlucoseTime().equals(C.GLUCOSE_TEST_BEFORE_BREAKFAST) && m.getMealTime().equals(C.MEAL_BREAKFAST)) ||
                                (test.getGlucoseTime().equals(C.GLUCOSE_TEST_BEFORE_LUNCH) && m.getMealTime().equals(C.MEAL_LUNCH)) ||
                                (test.getGlucoseTime().equals(C.GLUCOSE_TEST_BEFORE_DINNER) && m.getMealTime().equals(C.MEAL_DINNER))){

                            testsCrossedMeals.getCurrentElement().setGlucoseTestBeforeMeal(test);
                        }

                    }while(c2.moveToNext());
                }
            }

            if(newlyOpened) close();
        }

        return testsCrossedMeals;
    }

    public synchronized Meal getLastMealAdded(int mealTime){
        Meal lastMeal = null;
        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM meals WHERE meal=? ORDER BY datetime DESC LIMIT 1", new String[] {
                C.MEAL_STRING[mealTime]
        });

        if(cursor.moveToFirst()){
            lastMeal = buildMealObject(cursor);
            cursor.close();
        }
        if(newlyOpened) close();

        return lastMeal;
    }

    public synchronized Meal getMealById(Integer id) {
        boolean newlyOpened = openReadable();

        Meal m = null;

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM meals WHERE _id=?",
                new String[] {
                        id.toString()
                });

        if (cursor.moveToFirst()) {
            m = buildMealObject(cursor);
            cursor.close();
        }
        if(newlyOpened) close();
        return m;
    }

    public synchronized ArrayList<Meal> getArrayListWithLastMeals(Integer m, Integer number){
        boolean newlyOpened = openReadable();
        ArrayList<Meal> amdb = new ArrayList<>();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM meals WHERE meal=? ORDER BY datetime DESC LIMIT ?",
                new String[] {
                        C.MEAL_STRING[m],
                        number.toString()
                });

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {

                    amdb.add(buildMealObject(cursor));

                }while(cursor.moveToNext());

                cursor.close();
            }
        }
        if(newlyOpened) close();

        return amdb;
    }

    public synchronized Cursor getCursorForGlucoseTestsBetweenInstants(String start, String end){
        return mDatabase.rawQuery("SELECT * FROM glucose_tests WHERE datetime>? AND datetime<? ORDER BY datetime", new String[] {
                start,
                end
        });
    }


    /******************************************************************************
     * Methods related to insert, update and delete Cafydia objects onto database *
     ******************************************************************************/

    // methods related to Food
    public synchronized void insertFood(Food food){
        boolean newlyOpened = openWritable();
        mDatabase.execSQL("INSERT INTO food (name, type, favorite, c_percent, unit_weight) VALUES (?, ?, ?, ?, ?);",
                new String[]{
                        food.getName(),
                        food.getType().toString(),
                        food.getFavorite().toString(),
                        food.getCPercent().toString(),
                        food.getWeightPerUnitInGrams().toString()
                });
        if(newlyOpened) close();

    }
    public synchronized void updateFood(Food food){
        if (food.getId() == 0) { return; }
        boolean newlyOpened = openWritable();
        mDatabase.execSQL("UPDATE food SET name=?, type=?, favorite=?, c_percent=?, unit_weight=? WHERE _id=?",
                new String[]{
                        food.getName(),
                        food.getType().toString(),
                        food.getFavorite().toString(),
                        food.getCPercent().toString(),
                        food.getWeightPerUnitInGrams().toString(),
                        food.getId().toString()
                });
        if(newlyOpened) close();
    }
    public synchronized void deleteFood(Food food){
        if (food.getId() == 0) { return; }
        boolean newlyOpened = openWritable();
        mDatabase.execSQL("DELETE FROM food WHERE _id=?",
                new String[]{
                        food.getId().toString()
                });

        if(newlyOpened) close();
    }

    // methods related to glucose tests
    public synchronized void insertGlucoseTest(GlucoseTest glucoseTest) {
        if (glucoseTest.getGlucoseId() != 0) { return; }
        boolean newlyOpened = openWritable();
        mDatabase.execSQL("INSERT INTO glucose_tests (datetime, time, level, metabolic_rhythm_id, day_week) VALUES (?, ?, ?, ?, ?);",
                new String[]{
                        glucoseTest.getInternalDateTimeString(),
                        glucoseTest.getGlucoseTime().toString(),
                        glucoseTest.getGlucoseLevel().toString(),
                        glucoseTest.getMetabolicRhythmId().toString(),
                        glucoseTest.getDayOfWeek().toString()
                });
        if(newlyOpened) close();
    }
    public synchronized void updateGlucoseText(GlucoseTest glucoseTest){
        if (glucoseTest.getGlucoseId() == 0) { return; }
        boolean newlyOpened = openWritable();
        mDatabase.execSQL("UPDATE glucose_tests SET datetime=?, time=?, level=?, metabolic_rhythm_id=?, day_week=? WHERE _id=?",
                new String[] {
                        glucoseTest.getInternalDateTimeString(),
                        glucoseTest.getGlucoseTime().toString(),
                        glucoseTest.getGlucoseLevel().toString(),
                        glucoseTest.getMetabolicRhythmId().toString(),
                        glucoseTest.getDayOfWeek().toString(),
                        glucoseTest.getGlucoseId().toString()

                });
        if(newlyOpened) close();

    }
    public synchronized void deleteGlucoseTest(GlucoseTest glucoseTest) {
        if (glucoseTest.getGlucoseId() == 0) { return; }
        boolean newlyOpened = openWritable();
        mDatabase.execSQL("DELETE FROM glucose_tests WHERE _id=?",
                new String[]{
                        glucoseTest.getGlucoseId().toString()
                });
        if(newlyOpened) close();
    }

    // methods related to meals
    public synchronized void insertMeal(Meal meal) {
        if (meal.getMealId() != 0) { return; }
        boolean newlyOpened = openWritable();

        mDatabase.execSQL("INSERT INTO meals (" +
                        "datetime, meal, carbohydrates, metabolic_rhythm_id, metabolic_rhythm_name, baseline_preprandial, baseline_basal, metabolic_preprandial, " +
        "metabolic_basal, correction_factor_preprandial, correctives_preprandial, correctives_name, food_selected_json, day_week, final_preprandial) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new String[]{
                        meal.getInternalDateTimeString(),
                        meal.getMealString(),
                        meal.getMealCarbohydrates().toString(),
                        meal.getMetabolicRhythmId().toString(),
                        meal.getMetabolicRhythmName(),
                        meal.getBaselinePreprandial().toString(),
                        meal.getBaselineBasal().toString(),
                        meal.getBeginningPreprandial().toString(),
                        meal.getBeginningBasal().toString(),
                        meal.getCorrectionFactorPreprandial().toString(),
                        meal.getCorrectivesPreprandial().toString(),
                        meal.getCorrectivesAppliedName(),
                        meal.getFoodSelectedToJson(),
                        meal.getDayOfWeek().toString(),
                        meal.getFinalPreprandialDose().toString()
                });
        if(newlyOpened) close();
    }
    public synchronized void updateMeal(Meal meal) {
        if (meal.getMealId() == 0) { return; }
        boolean newlyOpened = openWritable();
        mDatabase.execSQL("UPDATE meals SET datetime=?, meal=?, carbohydrates=?, metabolic_rhythm_id=?, " +
                "metabolic_rhythm_name=?, baseline_preprandial=?, baseline_basal=?, metabolic_preprandial=?, "+
                "metabolic_basal=?, correction_factor_preprandial=?, correctives_preprandial=?, correctives_name=?, food_selected_json=?, day_week=?, final_preprandial=? WHERE _id=?",
                new String[]{
                        meal.getInternalDateTimeString(),
                        meal.getMealString(),
                        meal.getMealCarbohydrates().toString(),
                        meal.getMetabolicRhythmId().toString(),
                        meal.getMetabolicRhythmName(),
                        meal.getBaselinePreprandial().toString(),
                        meal.getBaselineBasal().toString(),
                        meal.getBeginningPreprandial().toString(),
                        meal.getBeginningBasal().toString(),
                        meal.getCorrectionFactorPreprandial().toString(),
                        meal.getCorrectivesPreprandial().toString(),
                        meal.getCorrectivesAppliedName(),
                        meal.getFoodSelectedToJson(),
                        meal.getDayOfWeek().toString(),
                        meal.getFinalPreprandialDose().toString(),
                        meal.getMealId().toString()
                });
        if(newlyOpened) close();
    }
    public synchronized void deleteMeal(Meal meal) {
        if (meal.getMealId() == 0) { return; }
        boolean newlyOpened = openWritable();
        mDatabase.execSQL("DELETE FROM meals WHERE _id=?",
                new String[]{
                        meal.getMealId().toString()
                });
        if(newlyOpened) close();
    }


    /*
     * Related to HbA1c
     */
    public synchronized ArrayList<HbA1c> getHbA1cArrayListSinceInstant(Instant instant){
        ArrayList<HbA1c> result = new ArrayList<HbA1c>();

        boolean newlyOpened = openReadable();
        Cursor cursor = mDatabase.rawQuery("SELECT * from hba1c WHERE datetime>? ORDER BY datetime DESC", new String[] {
                instant.getInternalDateTimeString()
        });

        if(cursor.moveToFirst()) {
            do {
                result.add(buildHbA1cObject(cursor));

            }while(cursor.moveToNext());

            cursor.close();
        }
        if(newlyOpened) close();

        return result;
    }

    public synchronized void insertHbA1c(HbA1c h){
        boolean newlyOpened = openWritable();
        mDatabase.execSQL("INSERT INTO hba1c(datetime, metabolic_rhythm_id, metabolic_rhythm_name, percentage, mmol_mol) VALUES (?, ?, ?, ?, ?)",
                new String[]{
                        h.getInternalDateTimeString(),
                        h.getMetabolicRhythmId().toString(),
                        h.getMetabolicRhythmName(),
                        h.getPercentage().toString(),
                        h.getMmolMol().toString()
                });
        if(newlyOpened) close();
    }
    public synchronized void updateHbA1c(HbA1c h){
        boolean newlyOpened = openWritable();
        mDatabase.execSQL("UPDATE hba1c SET datetime=?, metabolic_rhythm_id=?, metabolic_rhythm_name=?, percentage=?, mmol_mol=? WHERE _id=?",
                new String[]{
                        h.getInternalDateTimeString(),
                        h.getMetabolicRhythmId().toString(),
                        h.getMetabolicRhythmName(),
                        h.getPercentage().toString(),
                        h.getMmolMol().toString(),
                        h.getId().toString()
                });
        if(newlyOpened) close();
    }
    public synchronized void deleteHbA1c(HbA1c h){
        boolean newlyOpened = openWritable();
        mDatabase.execSQL("DELETE FROM hba1c WHERE _id=?",
                new String[] {
                        h.getId().toString()
                });
        if(newlyOpened) close();

    }


    /*
     * Related to Snack
     */
    public synchronized void insertSnack(Snack s){
        Gson gson = new Gson();
        boolean newlyOpened = openWritable();

        mDatabase.execSQL("INSERT INTO snacks(datetime, snack, carbohydrates, metabolic_rhythm_id, metabolic_rhythm_name, food_selected_json, day_week) VALUES (?, ?, ?, ?, ?, ?, ?)",
                new String[] {
                        s.getInternalDateTimeString(),
                        s.getSnack().toString(),
                        s.getFoodSelected().getTotalCarbohydratesInGrams().toString(),
                        s.getMetabolicRhythmId().toString(),
                        s.getMetabolicRhythmName(),
                        gson.toJson(s.getFoodSelected()),
                        s.getDayOfWeek().toString()
                });
        if(newlyOpened) close();

    }
    public synchronized void updateSnack(Snack s){
        Gson gson = new Gson();
        boolean newlyOpened = openWritable();

        mDatabase.execSQL("UPDATE snacks SET datetime=?, snack=?, carbohydrates=?, metabolic_rhythm_id=?, metabolic_rhythm_name=?, food_selected_json=?, day_week=? WHERE _id=?",
                new String[] {
                        s.getInternalDateTimeString(),
                        s.getSnack().toString(),
                        s.getFoodSelected().getTotalCarbohydratesInGrams().toString(),
                        s.getMetabolicRhythmId().toString(),
                        s.getMetabolicRhythmName(),
                        gson.toJson(s.getFoodSelected()),
                        s.getDayOfWeek().toString(),
                        s.getId().toString()
                });
        if(newlyOpened) close();
    }
    public synchronized void deleteSnack(Snack s){
        boolean newlyOpened = openWritable();

        mDatabase.execSQL("DELETE FROM snacks WHERE _id=?", new String[] { s.getId().toString() });

        if(newlyOpened) close();
    }

    /*
     * Related to annotations
     */

    public synchronized ArrayList<Annotation> getAnnotationsByOrderNumberScopeXLowAndXHigh(int scope, float xLow, float xHigh){
        ArrayList<Annotation> annotations = new ArrayList<>();

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM annotations WHERE (scope=? OR scope=?) ORDER BY datetime DESC", new String[] {
                Integer.toString(C.ANNOTATION_SCOPE_GLOBAL),
                Integer.toString(scope)
        });

        if(cursor.moveToFirst()){
            int n = 1;
            do {

                Annotation a = buildAnnotationObject(cursor);
                a.setNumber(n);

                // sólo se les asigna número a aquellas que se encuentren entre el xhigh o xlow
                if(a.getDaysPassedFromNow() <= xHigh && a.getDaysPassedFromNow() >= xLow) {
                    annotations.add(a);
                    n++;
                } else {
                    a.setNumber(0);
                    annotations.add(a);
                }

            } while(cursor.moveToNext());
            cursor.close();
        }
        if(newlyOpened) close();

        return annotations;

    }

    public synchronized ArrayList<Annotation> getAllAnnotations(){
        ArrayList<Annotation> annotations = new ArrayList<>();

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM annotations", null);

        if(cursor.moveToFirst()){

            do {
                Annotation a = buildAnnotationObject(cursor);
                a.setNumber(0);
                annotations.add(a);

            } while(cursor.moveToNext());
            cursor.close();
        }
        if(newlyOpened) close();

        return annotations;

    }
    public synchronized ArrayList<Annotation> getAllAnnotationsByScope(int scope){
        ArrayList<Annotation> annotations = new ArrayList<>();

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM annotations WHERE scope=?", new String[]{
                scope + ""
        });

        if(cursor.moveToFirst()){
            do {
                Annotation a = buildAnnotationObject(cursor);
                a.setNumber(0);
                annotations.add(a);

            } while(cursor.moveToNext());
            cursor.close();
        }
        if(newlyOpened) close();

        return annotations;

    }

    public synchronized Annotation getAnnotationById(Integer id){
        Annotation a = null;

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM annotations WHERE _id=?", new String[]{id.toString()});

        if(cursor.moveToFirst()){

            a = buildAnnotationObject(cursor);

            cursor.close();
        }

        if(newlyOpened) close();

        return a;
    }
    public synchronized void insertAnnotation(Annotation a){
        if(a.getId() != 0) return;
        boolean newlyOpened = openWritable();

        mDatabase.execSQL("INSERT INTO annotations(datetime, created_by, scope, annotation) VALUES (?, ?, ?, ?);", new String[] {
                a.getInternalDateString(),
                a.getCreatedBy().toString(),
                a.getOrderNumberScope().toString(),
                a.getAnnotation()
        });

        if(newlyOpened) close();
    }

    public synchronized void updateAnnotation(Annotation a){
        if(a.getId() == 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("UPDATE annotations SET datetime=?, created_by=?, scope=?, annotation=? WHERE _id=?", new String[] {
                a.getInternalDateString(),
                a.getCreatedBy().toString(),
                a.getOrderNumberScope().toString(),
                a.getAnnotation(),
                a.getId().toString()
        });

        if(newlyOpened) close();
    }

    public synchronized void deleteAnnotation(Annotation a){
        if(a.getId() == 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("DELETE FROM annotations WHERE _id=?", new String[] {
                a.getId().toString()
        });

        if(newlyOpened) close();
    }


    public synchronized void cleanChartsTables(){
        boolean newlyOpened = openWritable();

        mDatabase.execSQL("DELETE FROM chart_pages");
        mDatabase.execSQL("DELETE FROM chart_page_elements");
        mDatabase.execSQL("DELETE FROM data_collection_criteria");
        mDatabase.execSQL("DELETE FROM data_collection_criteria_label_rules");
        mDatabase.execSQL("DELETE FROM statistical_objects");


        if(newlyOpened) close();
    }

    ////////////////////////////////////////////

    // Related to Charts Pages

    ////////////////////////////////////////////

    public synchronized ArrayList<ChartPage> getChartPagesOrderedByScope(){
        ArrayList<ChartPage> pages = new ArrayList<ChartPage>();

        boolean newlyOpened = openReadable();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM chart_pages ORDER BY order_number ASC;", null);
        if(cursor.moveToFirst()){
            do {
                int id = cursor.getInt(0);
                int orderNumber = cursor.getInt(1);
                String title = cursor.getString(2);
                int criteriaId = cursor.getInt(3);
                DataCollectionCriteria criteria = getDataCollectionCriteriaById(criteriaId);

                ChartPage page = new ChartPage(id, orderNumber, title, criteria);

                pages.add(page);
            } while (cursor.moveToNext());
            cursor.close();
        }
        if(newlyOpened) close();

        return pages;
    }

    public synchronized ChartPage getChartPageById(Integer id){

        ChartPage page = null;


        boolean newlyOpened = openReadable();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM chart_pages WHERE _id=?;", new String[] {
                id.toString()
        });

        if(cursor.moveToFirst()){
            int pageId = cursor.getInt(0);
            int orderNumber = cursor.getInt(1);
            String title = cursor.getString(2);
            int criteriaId = cursor.getInt(3);
            DataCollectionCriteria criteria = getDataCollectionCriteriaById(criteriaId);

            page = new ChartPage(pageId, orderNumber, title, criteria);

            cursor.close();
        }
        if(newlyOpened) close();

        return page;
    }

    public synchronized void setChartPageDatabaseId(ChartPage page){
        boolean newlyOpened = openReadable();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM chart_pages WHERE order_number=?", new String[] {
                page.getPagerScopePosition().toString()
        });

        if(cursor.moveToFirst()){
            int id = cursor.getInt(0);
            page.setId(id);

            cursor.close();
        }

        if(newlyOpened) close();
    }

    public synchronized void insertChartPage(ChartPage page){
        if(page.getId() != 0) return;

        boolean newlyOpened = openWritable();
        mDatabase.execSQL("INSERT INTO chart_pages(order_number, title, criteria_id) VALUES (?, ?, ?);", new String[] {
                page.getPagerScopePosition().toString(),
                page.getTitle(),
                page.getCriteria() != null ? page.getCriteria().getId().toString() : "0"
        });
        if(newlyOpened) close();
    }

    public synchronized void updateChartPage(ChartPage page){
        if(page.getId() == 0) return;

        boolean newlyOpened = openWritable();
        mDatabase.execSQL("UPDATE chart_pages SET order_number=?, title=?, criteria_id=? WHERE _id=?", new String[] {
                page.getPagerScopePosition().toString(),
                page.getTitle(),
                page.getCriteria() != null ? page.getCriteria().getId().toString() : "0",
                page.getId().toString()
        });

        if(newlyOpened) close();
    }

    public synchronized void deleteChartPage(ChartPage page){

        if(page.getId() == 0) return;
        boolean newlyOpened = openWritable();

        // eliminar todas las anotaciones cuyo scope sea igual al order number del page
        mDatabase.execSQL("DELETE FROM annotations WHERE scope=?", new String[] {
                page.getPagerScopePosition().toString()
        });

        ArrayList<ChartPageElement> elements = getChartPageElementsByChartPageId(page.getId());
        for(ChartPageElement element : elements){
            if(element.getCriteria() != null) {
                deleteDataCollectionLabelRuleByCriteriaId(element.getCriteria().getId());
                deleteDataCollectionCriteria(element.getCriteria());
            }
            deleteChartPageElement(element);
        }

        if(page.getCriteria() != null) {
            deleteDataCollectionCriteria(page.getCriteria());
            deleteDataCollectionLabelRuleByCriteriaId(page.getCriteria().getId());
        }

        // eliminar la propia página
        mDatabase.execSQL("DELETE FROM chart_pages WHERE _id=?", new String[] {
                page.getId().toString()
        });

        if(newlyOpened) close();
    }
    //////////////////////////////////////////////
    //////////////////////////////////////////////
    //////////////////////////////////////////////



    ////////////////////////////////////////////

    // Related to Chart Page Elements

    ////////////////////////////////////////////

    public synchronized ArrayList<ChartPageElement> getChartPageElementsByChartPageId(Integer pageId){
        ArrayList<ChartPageElement> chartPageElements = new ArrayList<>();

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM chart_page_elements WHERE chart_page_id=? ORDER BY _id ASC", new String[]{
                pageId.toString()
        });

        if(cursor.moveToFirst()){
            do {
                int id = cursor.getInt(0);
                int type = cursor.getInt(1);

                String textHeader = cursor.getString(3);

                int criteriaId;
                int statisticalId;


                ChartPageElement chartPageElement = new ChartPageElement(id, type, pageId, textHeader);

                // si el texto es "" es porque no se trata de una cabecera, sino de una gráfica
                // hay que recoger el criterio y el objeto estadístico.
                if(textHeader.equals("")){
                    criteriaId = cursor.getInt(4);
                    statisticalId = cursor.getInt(5);

                    if(criteriaId > 0) {
                        chartPageElement.setCriteria(getDataCollectionCriteriaById(criteriaId));
                    }

                    if(statisticalId > 0) {
                        chartPageElement.setStatisticalObject(getStatisticalObjectById(statisticalId));
                    }
                }

                chartPageElements.add(chartPageElement);

            } while (cursor.moveToNext());
            cursor.close();
        }

        if(newlyOpened) close();

        return chartPageElements;
    }
    public synchronized void insertChartPageElement(ChartPageElement chartPageElement){
        if(chartPageElement.getId() != 0) return;

        boolean newlyOpened = openWritable();

        //_id INTEGER PRIMARY KEY, type INTEGER, chart_page_id INTEGER, text_header TEXT, criteria_id INTEGER, statistical_object_id INTEGER

        mDatabase.execSQL("INSERT INTO chart_page_elements (type, chart_page_id, text_header, criteria_id, statistical_object_id) VALUES (?, ?, ?, ?, ?);", new String[] {
                chartPageElement.getType().toString(),
                chartPageElement.getChartPageId().toString(),
                chartPageElement.getTextHeader(),
                chartPageElement.getCriteria() != null ? chartPageElement.getCriteria().getId().toString() : "0",
                chartPageElement.getStatisticalObject() != null ? chartPageElement.getStatisticalObject().getId().toString() : "0"
        });

        if(newlyOpened) close();
    }

    public synchronized void updateChartPageElement(ChartPageElement chartPageElement){
        if(chartPageElement.getId() == 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("UPDATE chart_page_elements SET type=?, chart_page_id=?, text_header=?, criteria_id=?, statistical_object_id=? WHERE _id=?", new String[] {
                chartPageElement.getType().toString(),
                chartPageElement.getChartPageId().toString(),
                chartPageElement.getTextHeader(),
                chartPageElement.getCriteria() != null ? chartPageElement.getCriteria().getId().toString() : "0",
                chartPageElement.getStatisticalObject() != null ? chartPageElement.getStatisticalObject().getId().toString() : "0",
                chartPageElement.getId().toString()
        });

        if(newlyOpened) close();
    }

    public synchronized void deleteChartPageElement(ChartPageElement chartPageElement){
        if(chartPageElement.getId() == 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("DELETE FROM chart_page_elements WHERE _id=?", new String[] {
                chartPageElement.getId().toString()
        });


        if(chartPageElement.getStatisticalObject() != null) {
            deleteStatisticalObject(chartPageElement.getStatisticalObject());
        }

        if(chartPageElement.getCriteria() != null && chartPageElement.getCriteria().getId() > 0){
            deleteDataCollectionCriteria(chartPageElement.getCriteria());
        }

        if(newlyOpened) close();
    }

    public synchronized void deleteChartPageElementsByChartPageId(Integer id){
        boolean newlyOpened = openWritable();

        mDatabase.execSQL("DELETE FROM chart_page_elements WHERE chart_page_id=?", new String[] {
                id.toString()
        });

        if(newlyOpened) close();
    }

    public synchronized ChartPageElement getLastChartPageElementAdded(){
        boolean newlyOpened = openReadable();

        ChartPageElement element = null;

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM chart_page_elements ORDER BY _id DESC LIMIT 1", null);

        if(cursor.moveToFirst()){
            int id = cursor.getInt(0);
            int type = cursor.getInt(1);
            int chartPageId = cursor.getInt(2);
            String text = cursor.getString(3);

            element = new ChartPageElement(id, type, chartPageId, text);

            // si el texto es "" es porque no se trata de una cabecera, sino de una gráfica
            // hay que recoger el criterio y el objeto estadístico.
            if(text.equals("")){
                int criteriaId = cursor.getInt(4);
                int statisticalId = cursor.getInt(5);

                if(criteriaId > 0) {
                    element.setCriteria(getDataCollectionCriteriaById(criteriaId));
                }

                if(statisticalId > 0) {
                    element.setStatisticalObject(getStatisticalObjectById(statisticalId));
                }
            }


            cursor.close();

        }

        if(newlyOpened) close();

        return element;
    }
    public synchronized ChartPageElement getChartPageElementById(Integer id){
        if(id.equals(0))
            return null;

        boolean newlyOpened = openReadable();

        ChartPageElement element = null;

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM chart_page_elements WHERE _id=?", new String[] {
                id.toString()
        });

        if(cursor.moveToFirst()){
            int type = cursor.getInt(1);
            int chartPageId = cursor.getInt(2);
            String text = cursor.getString(3);

            element = new ChartPageElement(id, type, chartPageId, text);

            // si el texto es "" es porque no se trata de una cabecera, sino de una gráfica
            // hay que recoger el criterio y el objeto estadístico.
            if(text.equals("")){
                int criteriaId = cursor.getInt(4);
                int statisticalId = cursor.getInt(5);

                if(criteriaId > 0) {
                    element.setCriteria(getDataCollectionCriteriaById(criteriaId));
                }

                if(statisticalId > 0) {
                    element.setStatisticalObject(getStatisticalObjectById(statisticalId));
                }
            }


            cursor.close();

        }

        if(newlyOpened) close();

        return element;
    }
/*
    public synchronized ChartPage getLastChartPageAdded(){
        boolean newlyOpened = openReadable();

        ChartPage page = null;

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM chart_pages ORDER BY _id DESC LIMIT 1", null);

        if(cursor.moveToFirst()){
            // _id INTEGER PRIMARY KEY, order_number INTEGER, title TEXT, criteria_id INTEGER
            int id = cursor.getInt(0);
            int orderNumber = cursor.getInt(1);
            String title = cursor.getString(2);
            int criteriaId = cursor.getInt(3);

            page = new ChartPage(id, orderNumber, title, getDataCollectionCriteriaById(criteriaId));

            cursor.close();

        }

        if(newlyOpened) close();

        return page;
    }
*/

    /////////////////////////////////////////////////////////////////////////////////////////

    // Related to Data Collection Criteria

    /////////////////////////////////////////////////////////////////////////////////////////

    public synchronized void insertDataCollectionCriteria(DataCollectionCriteria criteria){
        if(criteria.getId() != 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("INSERT INTO data_collection_criteria (since_type, since, until_type, until, day_weeks_activated, day_weeks, meals_activated, meals) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", new String[] {
                criteria.getSince().getType().toString(),
                criteria.getSince().getData().toString(),
                criteria.getUntil().getType().toString(),
                criteria.getUntil().getData().toString(),
                criteria.getDayWeeksActivated().toString(),
                criteria.getDayWeeksInteger().toString(),
                criteria.getMealTimesActivated().toString(),
                criteria.getMealTimeInteger().toString()
        });

        if(newlyOpened) close();
    }

    public synchronized void updateDataCollectionCriteria(DataCollectionCriteria criteria){
        if(criteria.getId() == 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("UPDATE data_collection_criteria SET since_type=?, since=?, until_type=?, until=?, day_weeks_activated=?, day_weeks=?, meals_activated=?, meals=? WHERE _id=?",
                new String[]{
                        criteria.getSince().getType().toString(),
                        criteria.getSince().getData().toString(),
                        criteria.getUntil().getType().toString(),
                        criteria.getUntil().getData().toString(),
                        criteria.getDayWeeksActivated().toString(),
                        criteria.getDayWeeksInteger().toString(),
                        criteria.getMealTimesActivated().toString(),
                        criteria.getMealTimeInteger().toString(),
                        criteria.getId().toString()
                });

        if(newlyOpened) close();
    }

    public synchronized void deleteDataCollectionCriteria(DataCollectionCriteria criteria) {
        if(criteria.getId() == 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("DELETE FROM data_collection_criteria WHERE _id=?", new String[] {
                criteria.getId().toString()
        });

        for(DataCollectionLabelRule rule : criteria.getLabelRules()) {
            deleteDataCollectionLabelRule(rule);
        }

        if(newlyOpened) close();
    }

    public synchronized DataCollectionCriteria getDataCollectionCriteriaById(int id){
        DataCollectionCriteria criteria = null;
        if(id > 0){
            boolean newlyOpened = openReadable();

            Cursor cursor = mDatabase.rawQuery("SELECT * FROM data_collection_criteria WHERE _id=?",
                    new String[]{
                            Integer.toString(id)
                    });

            if(cursor.moveToFirst()){
                //_id INTEGER PRIMARY KEY, since INTEGER, until INTEGER, day_weeks INTEGER, meals INTEGER

                int sinceType = cursor.getInt(1);
                long since = cursor.getLong(2);
                int untilType = cursor.getInt(3);
                long until = cursor.getLong(4);
                int dayWeeksActivated = cursor.getInt(5);
                int dayWeeks = cursor.getInt(6);
                int mealsActivated = cursor.getInt(7);
                int meals = cursor.getInt(8);

                DataCollectionCriteriaInstant sin = new DataCollectionCriteriaInstant(mContext, this, sinceType, since);
                DataCollectionCriteriaInstant unt = new DataCollectionCriteriaInstant(mContext, this, untilType, until);

                criteria = new DataCollectionCriteria(id, sin, unt, dayWeeksActivated, dayWeeks, mealsActivated, meals);

                cursor.close();

                cursor = mDatabase.rawQuery("SELECT * FROM data_collection_criteria_label_rules WHERE data_collection_criteria_id=?",
                        new String[] {
                                Integer.toString(id)
                        });

                if(cursor.moveToFirst()){
                    do {
                        //_id INTEGER PRIMARY KEY, data_collection_criteria_id INTEGER, action INTEGER, label_id INTEGER

                        int i = cursor.getInt(0);
                        int action = cursor.getInt(2);
                        int labelId = cursor.getInt(3);

                        criteria.addLabelRule(new DataCollectionLabelRule(i, id, action, getLabelById(labelId)));

                    } while (cursor.moveToNext());

                    cursor.close();
                }
            }

            if(newlyOpened) close();

        }

        return criteria;
    }

    public synchronized DataCollectionCriteria getLastDataCollectionCriteriaAdded(){
        DataCollectionCriteria c = null;

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM data_collection_criteria ORDER BY _id DESC LIMIT 1", null);

        if(cursor.moveToFirst()){
            //_id INTEGER PRIMARY KEY, since INTEGER, until INTEGER, day_weeks INTEGER, meals INTEGER
            int id = cursor.getInt(0);
            int sinceType = cursor.getInt(1);
            long since = cursor.getLong(2);
            int untilType = cursor.getInt(3);
            long until = cursor.getLong(4);
            int dayWeeksActivated = cursor.getInt(5);
            int dayWeeks = cursor.getInt(6);
            int mealsActivated = cursor.getInt(7);
            int meals = cursor.getInt(8);

            DataCollectionCriteriaInstant sin = new DataCollectionCriteriaInstant(mContext, this, sinceType, since);
            DataCollectionCriteriaInstant unt = new DataCollectionCriteriaInstant(mContext, this, untilType, until);


            c = new DataCollectionCriteria(id, sin, unt, dayWeeksActivated, dayWeeks, mealsActivated, meals);

            cursor.close();

            cursor = mDatabase.rawQuery("SELECT * FROM data_collection_criteria_label_rules WHERE data_collection_criteria_id=?",
                    new String[] {
                            Integer.toString(id)
                    });

            if(cursor.moveToFirst()){
                do {
                    //_id INTEGER PRIMARY KEY, data_collection_criteria_id INTEGER, action INTEGER, label_id INTEGER

                    int i = cursor.getInt(0);
                    int action = cursor.getInt(2);
                    int labelId = cursor.getInt(3);

                    c.addLabelRule(new DataCollectionLabelRule(i, id, action, getLabelById(labelId)));

                } while (cursor.moveToNext());

                cursor.close();
            }
        }

        if(newlyOpened) close();

        return c;
    }

    public synchronized StatisticalObject getLastStatisticalObjectAdded(){
        StatisticalObject o = null;

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM statistical_objects ORDER BY _id DESC LIMIT 1", null);

        if(cursor.moveToFirst()){
            //_id INTEGER PRIMARY KEY, chart_page_element_id INTEGER, configuration INTEGER
            int id = cursor.getInt(0);
            int chartPageElementId = cursor.getInt(1);
            int configuration = cursor.getInt(2);

            o = new StatisticalObject(id, chartPageElementId, configuration);

            cursor.close();
        }

        if(newlyOpened) close();

        return o;
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    // Related to Data Collection Label Rule

    /////////////////////////////////////////////////////////////////////////////////////////

    public synchronized void insertDataCollectionLabelRule(DataCollectionLabelRule rule){
        if(rule.getId() != 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("INSERT INTO data_collection_criteria_label_rules (data_collection_criteria_id, action, label_id) VALUES (?, ?, ?)",
                new String[] {
                        rule.getCriteriaId().toString(),
                        rule.getAction().toString(),
                        rule.getLabel().getId().toString()
                });
        if(newlyOpened) close();
    }

    public synchronized void updateDataCollectionLabelRule(DataCollectionLabelRule rule) {
        if(rule.getId() == 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("UPDATE data_collection_criteria_label_rules SET data_collection_criteria_id=?, action=?, label_id=? WHERE _id=?",
                new String[] {
                        rule.getCriteriaId().toString(),
                        rule.getAction().toString(),
                        rule.getLabel().getId().toString(),
                        rule.getId().toString()
                });
        if(newlyOpened) close();
    }

    public synchronized void deleteDataCollectionLabelRule(DataCollectionLabelRule rule){

        if(rule.getId() == 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("DELETE FROM data_collection_criteria_label_rules WHERE _id=?", new String[] {
                rule.getId().toString()
        });

        if(newlyOpened) close();
    }

    public synchronized void deleteDataCollectionLabelRuleByCriteriaId(Integer criteriaId){

        if(criteriaId == 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("DELETE FROM data_collection_criteria_label_rules WHERE data_collection_criteria_id=?", new String[] {
                criteriaId.toString()
        });

        if(newlyOpened) close();
    }

    public synchronized DataCollectionLabelRule getLastDataCollectionLabelRuleAdded(){
        DataCollectionLabelRule r = null;

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM data_collection_criteria_label_rules ORDER BY _id DESC LIMIT 1", null);

        if(cursor.moveToFirst()){
            //_id INTEGER PRIMARY KEY, since INTEGER, until INTEGER, day_weeks INTEGER, meals INTEGER
            int id = cursor.getInt(0);
            int criteriaId = cursor.getInt(1);
            int action = cursor.getInt(2);
            int labelId = cursor.getInt(3);
            r = new DataCollectionLabelRule(id, criteriaId, action, getLabelById(labelId));

            cursor.close();

        }

        if(newlyOpened) close();

        return r;
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    // Related to Statistical Objects

    /////////////////////////////////////////////////////////////////////////////////////////

    public synchronized void insertStatisticalObject(StatisticalObject object) {
        if(object.getId() != 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("INSERT INTO statistical_objects (chart_page_element_id, configuration) VALUES (?, ?)",
                new String[] {
                        object.getChartPageElementId().toString(),
                        object.getConfigurationInteger().toString()
                });

        if(newlyOpened) close();
    }

    public synchronized void updateStatisticalObject(StatisticalObject object) {
        if(object.getId() == 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("UPDATE statistical_objects SET chart_page_element_id=?, configuration=? WHERE _id=?",
                new String[] {
                        object.getChartPageElementId().toString(),
                        object.getConfigurationInteger().toString(),
                        object.getId().toString()
                });

        if(newlyOpened) close();
    }

    public synchronized void deleteStatisticalObject(StatisticalObject object){
        if(object == null || object.getId() == 0) return;

        boolean newlyOpened = openWritable();

        mDatabase.execSQL("DELETE FROM statistical_objects WHERE _id=?", new String[] { object.getId().toString() });

        if(newlyOpened) close();
    }

    public synchronized StatisticalObject getStatisticalObjectById(int statisticalId){

        StatisticalObject object = null;

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM statistical_objects WHERE _id=?",
                new String[] {
                        Integer.toString(statisticalId)
                });

        if(cursor.moveToFirst()){
            // _id INTEGER PRIMARY KEY, chart_page_element_id INTEGER, configuration INTEGER
            int chartPageElementId = cursor.getInt(1);
            int configuration = cursor.getInt(2);

            object = new StatisticalObject(statisticalId, chartPageElementId, configuration);

            cursor.close();
        }

        if(newlyOpened) close();

        return object;
    }

    public synchronized StatisticalObject getStatisticalObjectByChartPageElementId(int chartPageElementId){

        StatisticalObject object = null;

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM statistical_objects WHERE chart_page_element_id=?",
                new String[] {
                        Integer.toString(chartPageElementId)
                });

        if(cursor.moveToFirst()){
            // _id INTEGER PRIMARY KEY, chart_page_element_id INTEGER, configuration INTEGER
            int id = cursor.getInt(0);
            int configuration = cursor.getInt(2);

            object = new StatisticalObject(id, chartPageElementId, configuration);

            cursor.close();
        }

        if(newlyOpened) close();

        return object;
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    // Related to labels

    /////////////////////////////////////////////////////////////////////////////////////////


    public synchronized Label getLastLabelAdded(){
        Label l = null;

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM labels ORDER BY _id DESC LIMIT 1", null);

        if(cursor.moveToFirst()){
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            int color = cursor.getInt(2);

            l = new Label(id, name, color);

            cursor.close();
        }

        if(newlyOpened) close();

        return l;
    }

    public synchronized ArrayList<Label> getLabels(){
        ArrayList<Label> labels = new ArrayList<Label>();
        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM labels", null);
        if(cursor.moveToFirst()){
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                int color = cursor.getInt(2);

                Label l = new Label(id, title, color);

                Cursor cursor2 = mDatabase.rawQuery("SELECT * FROM label_ranges WHERE label_id=?", new String[] {
                        l.getId().toString()
                });

                if(cursor2.moveToFirst()){
                    do {
                        int rid = cursor2.getInt(0);
                        int rLabelId = cursor2.getInt(1);
                        long rStart = cursor2.getLong(2);
                        long rEnd = cursor2.getLong(3);

                        LabelRange range = new LabelRange(rid, rLabelId, rStart, rEnd);

                        l.addRange(range);

                    } while(cursor2.moveToNext());
                    cursor2.close();
                }

                labels.add(l);

            } while(cursor.moveToNext());
            cursor.close();
        }

        if(newlyOpened) close();

        return labels;
    }

    public synchronized ArrayList<LabelRange> getLabelRangesByLabelId(Integer id){
        ArrayList<LabelRange> ranges = new ArrayList<>();

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM label_ranges WHERE label_id=?", new String[] {
                id.toString()
        });


        if(cursor.moveToFirst()){
            do {
                int rid = cursor.getInt(0);
                int rLabelId = cursor.getInt(1);
                long rStart = cursor.getLong(2);
                long rEnd = cursor.getLong(3);

                LabelRange range = new LabelRange(rid, rLabelId, rStart, rEnd);

                ranges.add(range);

            } while(cursor.moveToNext());
            cursor.close();
        }

        if(newlyOpened) close();

        return ranges;

    }

    public synchronized Label getLabelById(int id){
        Label l = null;

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM labels WHERE _id=?",
                new String[] {
                        Integer.toString(id)
                });
        if(cursor.moveToFirst()){
            do {
                String title = cursor.getString(1);
                int color = cursor.getInt(2);

                l = new Label(id, title, color);

                l.setRanges(getLabelRangesByLabelId(l.getId()));

            } while(cursor.moveToNext());
            cursor.close();
        }

        if(newlyOpened) close();

        return l;

    }

    public synchronized  Label getLabelByTitle(String s){
        if (s.equals("")) {
            return null;
        }

        Label l = null;

        boolean newlyOpened = openReadable();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM labels WHERE title=?", new String[] { s });
        if(cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            int color = cursor.getInt(2);

            l = new Label(id, s, color);

            l.setRanges(getLabelRangesByLabelId(l.getId()));

            cursor.close();
        }

        if(newlyOpened) close();

        return l;
    }

    public synchronized void insertLabel(Label label){
        if(label.getId() != 0) return;

        boolean newlyOpened = openWritable();
        mDatabase.execSQL("INSERT INTO labels(title, color) VALUES(?, ?);", new String[] {
                label.getTitle(),
                label.getColor().toString()
        });

        if(newlyOpened) close();
    }
    public synchronized void updateLabel(Label label){
        if(label.getId() == 0) return;

        boolean newlyOpened = openWritable();
        mDatabase.execSQL("UPDATE labels SET title=?, color=? WHERE _id=?", new String[] {
                label.getTitle(),
                label.getColor().toString(),
                label.getId().toString()
        });

        if(newlyOpened) close();
    }

    public synchronized void deleteLabel(Label label){
        if(label.getId() == 0) return;

        boolean newlyOpened = openWritable();
        mDatabase.execSQL("DELETE FROM labels WHERE _id=?", new String[] {
                label.getId().toString()
        });

        mDatabase.execSQL("DELETE FROM label_ranges WHERE label_id=?", new String[] {
                label.getId().toString()
        });

        if(newlyOpened) close();
    }

    public synchronized void insertLabelRange(LabelRange range){
        if(range.getId() != 0) return;

        boolean newlyOpened = openWritable();
        mDatabase.execSQL("INSERT INTO label_ranges(label_id, start, end) VALUES (?, ?, ?);", new String[] {
                range.getLabelId().toString(),
                Long.toString(range.getStart().toDate().getTime()),
                Long.toString(range.getEnd().toDate().getTime())
        });
        if(newlyOpened) close();
    }

    public synchronized void updateLabelRange(LabelRange range){
        if(range.getId() == 0) return;

        boolean newlyOpened = openWritable();
        mDatabase.execSQL("UPDATE label_ranges SET label_id=?, start=?, end=? WHERE _id=?", new String[] {
                range.getLabelId().toString(),
                Long.toString(range.getStart().toDate().getTime()),
                Long.toString(range.getEnd().toDate().getTime()),
                range.getId().toString()
        });

        if(newlyOpened) close();
    }

    public synchronized void deleteLabelRange(LabelRange range){
        if(range.getId() == 0) return;

        boolean newlyOpened = openWritable();
        mDatabase.execSQL("DELETE FROM label_ranges WHERE _id=?", new String[] {
                range.getId().toString()
        });
        if(newlyOpened) close();
    }









    ////
    // o construct objects from cursors
    ////


    private DataCollectionCriteria buildDataCollectionCriteriaObject(Cursor cursor){

        int id = cursor.getInt(0);
        int sinceType = cursor.getInt(1);
        long since = cursor.getLong(2);
        int untilType = cursor.getInt(3);
        long until = cursor.getLong(4);
        int dayWeeksActivated = cursor.getInt(5);
        int dayWeeks = cursor.getInt(6);
        int mealsActivated = cursor.getInt(7);
        int meals = cursor.getInt(8);

        DataCollectionCriteriaInstant sin = new DataCollectionCriteriaInstant(mContext, this, sinceType, since);
        DataCollectionCriteriaInstant unt = new DataCollectionCriteriaInstant(mContext, this, untilType, until);

        return new DataCollectionCriteria(id, sin, unt, dayWeeksActivated, dayWeeks, mealsActivated, meals);
    }


    private GlucoseTest buildGlucoseTestObject(Cursor cursor) {
        int id = cursor.getInt(0);
        String datetime = cursor.getString(1);
        int time = cursor.getInt(2);
        int level = cursor.getInt(3);
        int metabolicRhythmId = cursor.getInt(4);

        return new GlucoseTest(id, datetime, time, level, metabolicRhythmId);
    }


    private Meal buildMealObject(Cursor cursor) {

        int _id = cursor.getInt(0);
        String datetime = cursor.getString(1);
        String me = cursor.getString(2);
        int meal;

        if (me.equals(C.MEAL_STRING[C.MEAL_BREAKFAST])) {
            meal = C.MEAL_BREAKFAST;
        } else if (me.equals(C.MEAL_STRING[C.MEAL_LUNCH])) {
            meal = C.MEAL_LUNCH;
        } else {
            meal = C.MEAL_DINNER;
        }

        float carb = cursor.getFloat(3);
        int metabolicId = cursor.getInt(4);
        String metabolicName = cursor.getString(5);
        float baselinePreprandial = cursor.getFloat(6);
        float baselineBasal = cursor.getFloat(7);
        float metabolicPreprandial = cursor.getFloat(8);
        float metabolicBasal = cursor.getFloat(9);
        float correctionFactor = cursor.getFloat(10);
        float correctives = cursor.getFloat(11);
        String correctivesName = cursor.getString(12);
        String foodSel = cursor.getString(13);
        float finalPreprandial = cursor.getFloat(15);

        return new Meal(_id, datetime, meal, carb, metabolicId, metabolicName, baselinePreprandial, baselineBasal, metabolicPreprandial, metabolicBasal, correctionFactor, correctives, correctivesName, foodSel, finalPreprandial);
    }

    private HbA1c buildHbA1cObject(Cursor cursor) {
        int id = cursor.getInt(0);
        String datetime = cursor.getString(1);
        int metabolicId = cursor.getInt(2);
        String metabolicName = cursor.getString(3);
        float percentage = cursor.getFloat(4);
        float mmolMol = cursor.getFloat(5);

        return new HbA1c(id, datetime, metabolicId, metabolicName, percentage, mmolMol);
    }

    private Annotation buildAnnotationObject(Cursor cursor) {
        int id = cursor.getInt(0);
        String dateString = cursor.getString(1);
        int createdBy = cursor.getInt(2);
        int s = cursor.getInt(3);
        String annotation = cursor.getString(4);

        return new Annotation(id, dateString, createdBy, s, annotation);
    }
}
