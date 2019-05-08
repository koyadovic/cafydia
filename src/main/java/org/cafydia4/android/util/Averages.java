package org.cafydia4.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.cafydia4.android.core.Instant;
import org.cafydia4.android.core.Meal;
import org.cafydia4.android.datadatabase.DataDatabase;

import java.util.ArrayList;

/**
 * Created by user on 18/05/15.
 */
public class Averages {
    private Float mAvCarbohydratesBreakfast = null;
    private Float mAvCarbohydratesLunch = null;
    private Float mAvCarbohydratesDinner = null;

    private Float mAvMinutesPassedFromMidnightBreakfast = null;
    private Float mAvMinutesPassedFromMidnightLunch = null;
    private Float mAvMinutesPassedFromMidnightDinner = null;

    private Float mAvPreprandialDoseBreakfast;
    private Float mAvPreprandialDoseLunch;
    private Float mAvPreprandialDoseDinner;

    private Float mAvBasalDoseBreakfast;
    private Float mAvBasalDoseLunch;
    private Float mAvBasalDoseDinner;

    private boolean mNowInBreakfastRange = true;
    private boolean mNowInLunchRange = true;
    private boolean mNowInDinnerRange = true;

    private OnAveragesCalculatedListener mCallback;
    private Context mContext;
    private int mDays;

    public static interface OnAveragesCalculatedListener {
        public void onAveragesCalculated();
    }

    public void setOnAveragesCalculatedListener(OnAveragesCalculatedListener callback){
        mCallback = callback;
    }

    public Averages (Context c, int n, OnAveragesCalculatedListener callback){
        mCallback = callback;
        mContext = c;
        mDays = n;

        if(mCallback == null){
            calculateAverages();
        } else {
            new CalculateAveragesAsyncTask().execute();
        }

    }

    public Averages(Context c, OnAveragesCalculatedListener callback){
        this(c, 14, callback);
    }

    public Averages(Context c, int n){
        this(c, n, null);
    }

    public Averages(Context c){
        this(c, 14, null); // two weeks by default
    }

    private Float calculateAverageCarbohydrates(ArrayList<Meal> meals){
        float n = 0f;
        float t = 0f;

        for(Meal m : meals){
            t += m.getMealCarbohydrates();
            n ++;
        }

        return n > 0 ? t / n : null;
    }

    private Float calculateAverageMinutesPassedFromMidnight(ArrayList<Meal> meals){
        float n = 0f;
        float t = 0f;

        for(Meal m : meals){
            t += m.getMinutesPassedFromMidnight();
            n ++;
        }

        return n > 0 ? t / n : null;
    }

    private Float calculateAveragePreprandialDose(ArrayList<Meal> meals){
        float n = 0f;
        float t = 0f;

        for(Meal m : meals){
            t += m.getFinalPreprandialDose();
            n ++;
        }

        return n > 0 ? t / n : null;
    }
    private Float calculateAverageBasalDose(ArrayList<Meal> meals){
        float n = 0f;
        float t = 0f;

        for(Meal m : meals){
            t += m.getTotalBasalDose();
            n ++;
        }

        return n > 0 ? t / n : null;
    }

    // getters
    public Float getAvCarbohydratesBreakfast() {
        return mAvCarbohydratesBreakfast;
    }

    public Float getAvCarbohydratesLunch() {
        return mAvCarbohydratesLunch;
    }

    public Float getAvCarbohydratesDinner() {
        return mAvCarbohydratesDinner;
    }

    public Float getAvCarbohydrates(Integer meal){
        switch (meal){
            case C.MEAL_BREAKFAST:
                return mAvCarbohydratesBreakfast;
            case C.MEAL_LUNCH:
                return mAvCarbohydratesLunch;
            case C.MEAL_DINNER:
                return mAvCarbohydratesDinner;
            default:
                return null;
        }
    }

    public Float getAvMinutesPassedFromMidnightBreakfast() {
        return mAvMinutesPassedFromMidnightBreakfast;
    }

    public Float getAvMinutesPassedFromMidnightLunch() {
        return mAvMinutesPassedFromMidnightLunch;
    }

    public Float getAvMinutesPassedFromMidnightDinner() {
        return mAvMinutesPassedFromMidnightDinner;
    }

    public Float getAvPreprandialDoseBreakfast() {
        return mAvPreprandialDoseBreakfast;
    }

    public Float getAvPreprandialDoseLunch() {
        return mAvPreprandialDoseLunch;
    }

    public Float getAvPreprandialDoseDinner() {
        return mAvPreprandialDoseDinner;
    }

    public Float getAvBasalDoseBreakfast() {
        return mAvBasalDoseBreakfast;
    }

    public Float getAvBasalDoseLunch() {
        return mAvBasalDoseLunch;
    }

    public Float getAvBasalDoseDinner() {
        return mAvBasalDoseDinner;
    }

    public boolean isNowInBreakfastRange() {
        return mNowInBreakfastRange;
    }

    public boolean isNowInLunchRange() {
        return mNowInLunchRange;
    }

    public boolean isNowInDinnerRange() {
        return mNowInDinnerRange;
    }

    private void calculateAverages(){
        DataDatabase db = new DataDatabase(mContext);

        ArrayList<Meal> br = db.getArrayListWithLastMeals(C.MEAL_BREAKFAST, mDays);
        ArrayList<Meal> lu = db.getArrayListWithLastMeals(C.MEAL_LUNCH, mDays);
        ArrayList<Meal> di = db.getArrayListWithLastMeals(C.MEAL_DINNER, mDays);

        // average carbohydrates
        mAvCarbohydratesBreakfast = calculateAverageCarbohydrates(br);
        mAvCarbohydratesLunch = calculateAverageCarbohydrates(lu);
        mAvCarbohydratesDinner = calculateAverageCarbohydrates(di);

        // average hours
        mAvMinutesPassedFromMidnightBreakfast = calculateAverageMinutesPassedFromMidnight(br);
        mAvMinutesPassedFromMidnightLunch = calculateAverageMinutesPassedFromMidnight(lu);
        mAvMinutesPassedFromMidnightDinner = calculateAverageMinutesPassedFromMidnight(di);

        // average preprandial dose
        mAvPreprandialDoseBreakfast = calculateAveragePreprandialDose(br);
        mAvPreprandialDoseLunch = calculateAveragePreprandialDose(lu);
        mAvPreprandialDoseDinner = calculateAveragePreprandialDose(di);

        // average basal dose
        mAvBasalDoseBreakfast = calculateAverageBasalDose(br);
        mAvBasalDoseLunch = calculateAverageBasalDose(lu);
        mAvBasalDoseDinner = calculateAverageBasalDose(di);

        // is now in range
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int minutesOfRange = Integer.parseInt(sp.getString("pref_meal_hours_choose_minutes_of_range", "60"));
        int halfMinutesOfRange = minutesOfRange / 2;

        int minutesNow = new Instant().getMinutesPassedFromMidnight();

        mNowInBreakfastRange = mAvMinutesPassedFromMidnightBreakfast != null && minutesNow >= mAvMinutesPassedFromMidnightBreakfast - halfMinutesOfRange && minutesNow <= mAvMinutesPassedFromMidnightBreakfast + halfMinutesOfRange;
        mNowInLunchRange = mAvMinutesPassedFromMidnightLunch != null && minutesNow >= mAvMinutesPassedFromMidnightLunch - halfMinutesOfRange && minutesNow <= mAvMinutesPassedFromMidnightLunch + halfMinutesOfRange;
        mNowInDinnerRange = mAvMinutesPassedFromMidnightDinner != null && minutesNow >= mAvMinutesPassedFromMidnightDinner - halfMinutesOfRange && minutesNow <= mAvMinutesPassedFromMidnightDinner + halfMinutesOfRange;

    }




    private class CalculateAveragesAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        protected void onPreExecute(){

        }

        protected Boolean doInBackground(Void... params){
            calculateAverages();
            return true;
        }

        protected void onPostExecute(Boolean result){
            if(mCallback != null)
                mCallback.onAveragesCalculated();
        }
    }
}
