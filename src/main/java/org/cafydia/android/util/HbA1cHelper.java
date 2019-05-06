package org.cafydia.android.util;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import org.cafydia.android.configdatabase.ConfigurationDatabase;
import org.cafydia.android.core.GlucoseTest;
import org.cafydia.android.core.HbA1c;
import org.cafydia.android.core.Instant;
import org.cafydia.android.datadatabase.DataDatabase;
import org.cafydia.android.recommendations.MetabolicFrameworkState;
import org.cafydia.android.recommendations.MetabolicRhythm;

import java.util.ArrayList;


/**
 * Created by miguel on 6/06/14.
 */
public class HbA1cHelper {
    private HbA1c hbA1c;

    public static final String SHARED_PREFERENCES_HBA1C_KEY = "HbA1c_data";
    private final String HBA1C = "HbA1c";
    private final String NEED_TO_RECALCULATE = "need_to_recalculate";
    private DataDatabase mDatabase;
    private ConfigurationDatabase mConfigDatabase;
    private MetabolicFrameworkState mState;

    private Activity hostActivity = null;
    private Boolean needToRecalculate;

    private ArrayList<GlucoseTest> glucoseAfterMeals;
    GlucoseTest beforeBr = null, beforeLu = null, beforeDi = null;
    GlucoseTest afterBr = null, afterLu = null, afterDi = null;

    public HbA1cHelper(Activity activity){
        this.hostActivity = activity;
        SharedPreferences sp = activity.getSharedPreferences(SHARED_PREFERENCES_HBA1C_KEY, Context.MODE_PRIVATE);

        mConfigDatabase = new ConfigurationDatabase(hostActivity);
        mState = new MetabolicFrameworkState(hostActivity);

        int metabolicId = mState.getActivatedMetabolicRhythmId();
        MetabolicRhythm m = mConfigDatabase.getMetabolicRhythmByIdSimple(metabolicId);

        hbA1c = new HbA1c(metabolicId, m.getName());
        hbA1c.setPercentage(sp.getFloat(HBA1C, 0.0f));

        needToRecalculate = sp.getBoolean(NEED_TO_RECALCULATE, false);

        glucoseAfterMeals = new ArrayList<>();
    }

    public void recalculateHbA1c(){
        recalculateHbA1c(null);
    }

    public void recalculateHbA1c(Instant i) {
        mDatabase = new DataDatabase(hostActivity);

        if(i != null)
            hbA1c.setInstant(i);

        mDatabase.openReadable();

        if (hostActivity != null) {
            Cursor cursor = mDatabase.getCursorForGlucoseTestsBetweenInstants(
                    new Instant(-120).getInternalDateTimeString(),
                    new Instant(0).getInternalDateTimeString()
            );

            float total = 0.0f;
            float elements = 0.0f;

            Instant day = null;


            if (cursor.moveToFirst()) {
                GlucoseTest g;
                do {
                    int id = cursor.getInt(0);
                    String datetime = cursor.getString(1);
                    int time = cursor.getInt(2);
                    int level = cursor.getInt(3);
                    int metabolicRhythmId = cursor.getInt(4);

                    // creamos la glucemia
                    g = new GlucoseTest(id, datetime, time, level, metabolicRhythmId);

                    // primer día
                    if (day == null) {
                        day = new Instant().setInstant(g);
                        catalogGlucoseTest(g);
                    }

                    // nuevo día
                    else if (!g.getDay().equals(day.getDay())) {
                        Log.d("recalculate HbA1c", "Cambio de día, del " + day.getInternalDateString() + " al " + g.getInternalDateString());
                        // calcular lo que llevemos de total y elements
                        if (beforeBr != null) {
                            Log.d("recalculate HbA1c", "Hay glucosa antes del desayuno " + beforeBr.getGlucoseLevel().toString());
                            if (afterBr == null) {
                                afterBr = new GlucoseTest(0, C.GLUCOSE_TEST_AFTER_BREAKFAST, (int) getGlucoseAverageAfterMeals(), beforeBr.getMetabolicRhythmId());
                                afterBr.setInstant(beforeBr);
                                afterBr.increaseNMinutes(120);
                                Log.d("recalculate HbA1c", "Pero no hay glucosa para después del desayuno. Inventamos ésta: " + afterBr.getGlucoseLevel().toString());
                            }

                            total += getHbA1c(beforeBr.getGlucoseLevel()) * weight(beforeBr.getDaysPassedFromNow());
                            elements += weight(beforeBr.getDaysPassedFromNow());
                            Log.d("recalculate HbA1c", "sumamos la glucosa de antes del desayuno: " + total / elements + "%");

                            total += getHbA1c(afterBr.getGlucoseLevel()) * weight(afterBr.getDaysPassedFromNow());
                            elements += weight(afterBr.getDaysPassedFromNow());
                            Log.d("recalculate HbA1c", "sumamos la glucosa de después del desayuno: " + total / elements + "%");
                        }

                        if (beforeLu != null) {
                            Log.d("recalculate HbA1c", "Hay glucosa antes de la comida " + beforeLu.getGlucoseLevel().toString());
                            if (afterLu == null) {
                                afterLu = new GlucoseTest(0, C.GLUCOSE_TEST_AFTER_LUNCH, (int) getGlucoseAverageAfterMeals(), beforeLu.getMetabolicRhythmId());
                                afterLu.setInstant(beforeLu);
                                afterLu.increaseNMinutes(120);
                                Log.d("recalculate HbA1c", "Pero no hay glucosa para después de la Comida. Inventamos ésta: " + afterLu.getGlucoseLevel().toString());
                            }

                            total += getHbA1c(beforeLu.getGlucoseLevel()) * weight(beforeLu.getDaysPassedFromNow());
                            elements += weight(beforeLu.getDaysPassedFromNow());
                            Log.d("recalculate HbA1c", "sumamos la glucosa de antes de la comida: " + total / elements + "%");

                            total += getHbA1c(afterLu.getGlucoseLevel()) * weight(afterLu.getDaysPassedFromNow());
                            elements += weight(afterLu.getDaysPassedFromNow());
                            Log.d("recalculate HbA1c", "sumamos la glucosa de después de la comida: " + total / elements + "%");
                        }

                        if (beforeDi != null) {
                            Log.d("recalculate HbA1c", "Hay glucosa antes de la cena " + beforeDi.getGlucoseLevel().toString());
                            if (afterDi == null) {
                                afterDi = new GlucoseTest(0, C.GLUCOSE_TEST_AFTER_DINNER, (int) getGlucoseAverageAfterMeals(), beforeDi.getMetabolicRhythmId());
                                afterDi.setInstant(beforeDi);
                                afterDi.increaseNMinutes(120);
                                Log.d("recalculate HbA1c", "Pero no hay glucosa para después de la cena. Inventamos ésta: " + afterDi.getGlucoseLevel().toString());
                            }

                            total += getHbA1c(beforeDi.getGlucoseLevel()) * weight(beforeDi.getDaysPassedFromNow());
                            elements += weight(beforeDi.getDaysPassedFromNow());
                            Log.d("recalculate HbA1c", "sumamos la glucosa de antes de la cena: " + total / elements + "%");

                            total += getHbA1c(afterDi.getGlucoseLevel()) * weight(afterDi.getDaysPassedFromNow());
                            elements += weight(afterDi.getDaysPassedFromNow());
                            Log.d("recalculate HbA1c", "sumamos la glucosa de después de la cena: " + total / elements + "%");
                        }


                        // poner to do a null
                        beforeBr = null;
                        beforeLu = null;
                        beforeDi = null;
                        afterBr = null;
                        afterLu = null;
                        afterDi = null;

                        day.setInstant(g);

                        // catalog el nuevo glucose test
                        if (g.getGlucoseTime().equals(C.GLUCOSE_TEST_IN_THE_NIGHT)) {
                            total += getHbA1c(g.getGlucoseLevel()) * weight(g.getDaysPassedFromNow());
                            elements += weight(g.getDaysPassedFromNow());
                        } else {
                            catalogGlucoseTest(g);
                        }
                    } else {
                        if (g.getGlucoseTime().equals(C.GLUCOSE_TEST_IN_THE_NIGHT)) {
                            total += getHbA1c(g.getGlucoseLevel()) * weight(g.getDaysPassedFromNow());
                            elements += weight(g.getDaysPassedFromNow());
                        } else {
                            catalogGlucoseTest(g);
                        }
                    }

                    //total += getHbA1c(g.getGlucoseLevel()) * weight(g.getDaysPassedFromNow());
                    //elements += weight(g.getDaysPassedFromNow());

                } while (cursor.moveToNext());

                cursor.close();

                // calcular lo que llevemos de total y elements
                if (beforeBr != null) {
                    Log.d("recalculate HbA1c", "Hay glucosa antes del desayuno " + beforeBr.getGlucoseLevel().toString());
                    if (afterBr == null) {
                        afterBr = new GlucoseTest(0, C.GLUCOSE_TEST_AFTER_BREAKFAST, (int) getGlucoseAverageAfterMeals(), beforeBr.getMetabolicRhythmId());
                        afterBr.setInstant(beforeBr);
                        afterBr.increaseNMinutes(120);
                        Log.d("recalculate HbA1c", "Pero no hay glucosa para después del desayuno. Inventamos ésta: " + afterBr.getGlucoseLevel().toString());
                    }

                    total += getHbA1c(beforeBr.getGlucoseLevel()) * weight(beforeBr.getDaysPassedFromNow());
                    elements += weight(beforeBr.getDaysPassedFromNow());
                    Log.d("recalculate HbA1c", "sumamos la glucosa de antes del desayuno: " + total / elements + "%");

                    total += getHbA1c(afterBr.getGlucoseLevel()) * weight(afterBr.getDaysPassedFromNow());
                    elements += weight(afterBr.getDaysPassedFromNow());
                    Log.d("recalculate HbA1c", "sumamos la glucosa de después del desayuno: " + total / elements + "%");
                }

                if (beforeLu != null) {
                    Log.d("recalculate HbA1c", "Hay glucosa antes de la comida " + beforeLu.getGlucoseLevel().toString());
                    if (afterLu == null) {
                        afterLu = new GlucoseTest(0, C.GLUCOSE_TEST_AFTER_LUNCH, (int) getGlucoseAverageAfterMeals(), beforeLu.getMetabolicRhythmId());
                        afterLu.setInstant(beforeLu);
                        afterLu.increaseNMinutes(120);
                        Log.d("recalculate HbA1c", "Pero no hay glucosa para después de la Comida. Inventamos ésta: " + afterLu.getGlucoseLevel().toString());
                    }

                    total += getHbA1c(beforeLu.getGlucoseLevel()) * weight(beforeLu.getDaysPassedFromNow());
                    elements += weight(beforeLu.getDaysPassedFromNow());
                    Log.d("recalculate HbA1c", "sumamos la glucosa de antes de la comida: " + total / elements + "%");

                    total += getHbA1c(afterLu.getGlucoseLevel()) * weight(afterLu.getDaysPassedFromNow());
                    elements += weight(afterLu.getDaysPassedFromNow());
                    Log.d("recalculate HbA1c", "sumamos la glucosa de después de la comida: " + total / elements + "%");
                }

                if (beforeDi != null) {
                    Log.d("recalculate HbA1c", "Hay glucosa antes de la cena " + beforeDi.getGlucoseLevel().toString());
                    if (afterDi == null) {
                        afterDi = new GlucoseTest(0, C.GLUCOSE_TEST_AFTER_DINNER, (int) getGlucoseAverageAfterMeals(), beforeDi.getMetabolicRhythmId());
                        afterDi.setInstant(beforeDi);
                        afterDi.increaseNMinutes(120);
                        Log.d("recalculate HbA1c", "Pero no hay glucosa para después de la cena. Inventamos ésta: " + afterDi.getGlucoseLevel().toString());
                    }

                    total += getHbA1c(beforeDi.getGlucoseLevel()) * weight(beforeDi.getDaysPassedFromNow());
                    elements += weight(beforeDi.getDaysPassedFromNow());
                    Log.d("recalculate HbA1c", "sumamos la glucosa de antes de la cena: " + total / elements + "%");

                    total += getHbA1c(afterDi.getGlucoseLevel()) * weight(afterDi.getDaysPassedFromNow());
                    elements += weight(afterDi.getDaysPassedFromNow());
                    Log.d("recalculate HbA1c", "sumamos la glucosa de después de la cena: " + total / elements + "%");
                }

                mDatabase.close();
                hbA1c.setPercentage(elements > 0.0f ? total / elements : 0.0f);
            }
            setNeedToRecalculate(false);
            saveHbA1c();
        }
    }

    private void saveHbA1c(){
        if(hostActivity != null) {
            SharedPreferences sp = hostActivity.getSharedPreferences(SHARED_PREFERENCES_HBA1C_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            editor.putFloat(HBA1C, hbA1c.getPercentage());
            editor.apply();

            if(hbA1c.getPercentage() > 0f){
                hbA1c.save(mDatabase);
            }

            new BackupManager(hostActivity).dataChanged();
        }
    }

    public void setNeedToRecalculate(boolean b){
        if(hostActivity != null) {
            SharedPreferences sp = hostActivity.getSharedPreferences(SHARED_PREFERENCES_HBA1C_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            editor.putBoolean(NEED_TO_RECALCULATE, b);
            editor.apply();

            new BackupManager(hostActivity).dataChanged();
        }
    }
    public Boolean isNeededToRecalculate(){
        return needToRecalculate;
    }

    public Float getHbA1cPercentage(){
        return hbA1c.getPercentage();
    }
    public Float getHbA1cMmolMol(){
        return hbA1c.getMmolMol();
    }

    private float weight(float daysPassed){
        return 1.0f / (float)(Math.pow(2.0, (Math.abs(daysPassed) / 30.0)));
    }

    public static float getHbA1c(float level){
        return ((level - 60.0f) / 31.0f) + 4.0f;
    }


    private void pushGlucoseTestAfterMeal(GlucoseTest g){
        glucoseAfterMeals.add(g);

        int b = glucoseAfterMeals.size();

        for(int a=0; a < b - 10; a++){
            glucoseAfterMeals.remove(0);
        }
    }

    private float getGlucoseAverageAfterMeals(){
        float total = 0;
        float elements = 0;

        if(glucoseAfterMeals.size() == 0){
            glucoseAfterMeals.add(new GlucoseTest(0, C.GLUCOSE_TEST_AFTER_BREAKFAST, 150, 1));
        }


        for(GlucoseTest g : glucoseAfterMeals){
            total += g.getGlucoseLevel();
            elements++;
        }

        return total / elements;
    }

    private void catalogGlucoseTest(GlucoseTest g){
        switch (g.getGlucoseTime()){
            case C.GLUCOSE_TEST_BEFORE_BREAKFAST:
                beforeBr = g;
                break;
            case C.GLUCOSE_TEST_BEFORE_LUNCH:
                beforeLu = g;
                break;
            case C.GLUCOSE_TEST_BEFORE_DINNER:
                beforeDi = g;
                break;
            case C.GLUCOSE_TEST_AFTER_BREAKFAST:
                afterBr = g;
                pushGlucoseTestAfterMeal(g);
                break;
            case C.GLUCOSE_TEST_AFTER_LUNCH:
                afterLu = g;
                pushGlucoseTestAfterMeal(g);
                break;
            case C.GLUCOSE_TEST_AFTER_DINNER:
                afterDi = g;
                pushGlucoseTestAfterMeal(g);
                break;
        }
    }
}
