package org.cafydia.android.recommendations;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.cafydia.android.R;
import org.cafydia.android.core.Annotation;
import org.cafydia.android.core.Meal;
import org.cafydia.android.util.C;

/**
 * Created by user on 21/09/14.
 */
public class BaselineBasal {
    public static final String SHARED_PREFERENCES_INSULIN_PATTERN = "insulin_pattern";
    private static final String BASAL_DOSE_BREAKFAST = "basal_dose_br";
    private static final String BASAL_DOSE_LUNCH = "basal_dose_lu";
    private static final String BASAL_DOSE_DINNER = "basal_dose_di";
    private static final String BASAL_NAME_BREAKFAST = "basal_name_br";
    private static final String BASAL_NAME_LUNCH = "basal_name_lu";
    private static final String BASAL_NAME_DINNER = "basal_name_di";
    private static final String BASAL_BREAKFAST_ACTIVATED = "basal_breakfast_activated";
    private static final String BASAL_LUNCH_ACTIVATED = "basal_lunch_activated";
    private static final String BASAL_DINNER_ACTIVATED = "basal_dinner_activated";

    private static final String PREPRANDIAL_NAME_BREAKFAST = "preprandial_name_br";
    private static final String PREPRANDIAL_NAME_LUNCH = "preprandial_name_lu";
    private static final String PREPRANDIAL_NAME_DINNER = "preprandial_name_di";


    private Context mContext;

    private Float basalDoseBreakfast;
    private Float basalDoseLunch;
    private Float basalDoseDinner;

    private Float basalDoseBreakfastOld;
    private Float basalDoseLunchOld;
    private Float basalDoseDinnerOld;

    private Boolean basalBreakfastActivatedOld;
    private Boolean basalLunchActivatedOld;
    private Boolean basalDinnerActivatedOld;


    private String basalNameBreakfast;
    private String basalNameLunch;
    private String basalNameDinner;
    private String preprandialNameBreakfast;
    private String preprandialNameLunch;
    private String preprandialNameDinner;

    private Boolean basalBreakfastActivated;
    private Boolean basalLunchActivated;
    private Boolean basalDinnerActivated;


    public BaselineBasal(Context c) {
        SharedPreferences sp = c.getSharedPreferences(SHARED_PREFERENCES_INSULIN_PATTERN, Context.MODE_PRIVATE);
        basalDoseBreakfast = sp.getFloat(BASAL_DOSE_BREAKFAST, 0.0f);
        basalDoseLunch = sp.getFloat(BASAL_DOSE_LUNCH, 0.0f);
        basalDoseDinner = sp.getFloat(BASAL_DOSE_DINNER, 0.0f);

        basalDoseBreakfastOld = basalDoseBreakfast;
        basalDoseLunchOld = basalDoseLunch;
        basalDoseDinnerOld = basalDoseDinner;

        basalNameBreakfast = sp.getString(BASAL_NAME_BREAKFAST, "");
        basalNameLunch = sp.getString(BASAL_NAME_LUNCH, "");
        basalNameDinner = sp.getString(BASAL_NAME_DINNER, "");

        preprandialNameBreakfast = sp.getString(PREPRANDIAL_NAME_BREAKFAST, "");
        preprandialNameLunch = sp.getString(PREPRANDIAL_NAME_LUNCH, "");
        preprandialNameDinner = sp.getString(PREPRANDIAL_NAME_DINNER, "");

        basalBreakfastActivated = sp.getBoolean(BASAL_BREAKFAST_ACTIVATED, false);
        basalLunchActivated = sp.getBoolean(BASAL_LUNCH_ACTIVATED, false);
        basalDinnerActivated = sp.getBoolean(BASAL_DINNER_ACTIVATED, false);

        basalBreakfastActivatedOld = basalBreakfastActivated;
        basalLunchActivatedOld = basalLunchActivated;
        basalDinnerActivatedOld = basalDinnerActivated;

        this.mContext = c;
    }

    public void saveOnly(){
        SharedPreferences sp = mContext.getSharedPreferences(SHARED_PREFERENCES_INSULIN_PATTERN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(BASAL_DOSE_BREAKFAST, basalDoseBreakfast);
        editor.putFloat(BASAL_DOSE_LUNCH, basalDoseLunch);
        editor.putFloat(BASAL_DOSE_DINNER, basalDoseDinner);

        editor.putString(BASAL_NAME_BREAKFAST, basalNameBreakfast);
        editor.putString(BASAL_NAME_LUNCH, basalNameLunch);
        editor.putString(BASAL_NAME_DINNER, basalNameDinner);

        editor.putString(PREPRANDIAL_NAME_BREAKFAST, preprandialNameBreakfast);
        editor.putString(PREPRANDIAL_NAME_LUNCH, preprandialNameLunch);
        editor.putString(PREPRANDIAL_NAME_DINNER, preprandialNameDinner);

        editor.putBoolean(BASAL_BREAKFAST_ACTIVATED, basalBreakfastActivated);
        editor.putBoolean(BASAL_LUNCH_ACTIVATED, basalLunchActivated);
        editor.putBoolean(BASAL_DINNER_ACTIVATED, basalDinnerActivated);

        editor.apply();
    }

    public void save(){
        saveOnly();

        new BackupManager(mContext).dataChanged();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean activated = sp.getBoolean("pref_automatic_annotations_basal_baseline", true);

        if(activated) {

            String text = "";

            if (!getBasalDoseBreakfastOld().equals(getBasalDoseBreakfast()) ||
                    (wasBasalBreakfastActivatedOld() != isBasalBreakfastActivated())) {
                text += mContext.getString(R.string.automatic_annotation_basal_baseline_change) + ": ";

                text += mContext.getResources().getStringArray(R.array.time_meal)[0] + " ";
                if (wasBasalBreakfastActivatedOld()) {
                    text += mContext.getString(R.string.automatic_annotation_old) + ": " + getBasalDoseBreakfastOld().toString() + ", ";
                } else {
                    text += mContext.getString(R.string.automatic_annotation_old) + ": 0.0, ";
                }

                if (isBasalBreakfastActivated()) {
                    text += mContext.getString(R.string.automatic_annotation_new) + ": " + getBasalDoseBreakfast().toString();
                } else {
                    text += mContext.getString(R.string.automatic_annotation_new) + ": 0.0";
                }
            }

            if (!getBasalDoseLunchOld().equals(getBasalDoseLunch()) ||
                    (wasBasalLunchActivatedOld() != isBasalLunchActivated())) {
                if (text.equals("")) {
                    text += mContext.getString(R.string.automatic_annotation_basal_baseline_change) + ":";
                }
                text += " " + mContext.getResources().getStringArray(R.array.time_meal)[1] + " ";
                if (wasBasalLunchActivatedOld()) {
                    text += mContext.getString(R.string.automatic_annotation_old) + ": " + getBasalDoseLunchOld().toString() + ", ";
                } else {
                    text += mContext.getString(R.string.automatic_annotation_old) + ": 0.0, ";
                }

                if (isBasalLunchActivated()) {
                    text += mContext.getString(R.string.automatic_annotation_new) + ": " + getBasalDoseLunch().toString();
                } else {
                    text += mContext.getString(R.string.automatic_annotation_new) + ": 0.0";
                }
            }

            if (!getBasalDoseDinnerOld().equals(getBasalDoseDinner()) ||
                    (wasBasalDinnerActivatedOld() != isBasalDinnerActivated())) {

                if (text.equals("")) {
                    text += mContext.getString(R.string.automatic_annotation_basal_baseline_change) + ":";
                }
                text += " " + mContext.getResources().getStringArray(R.array.time_meal)[2] + " ";

                if (wasBasalDinnerActivatedOld()) {
                    text += mContext.getString(R.string.automatic_annotation_old) + ": " + getBasalDoseDinnerOld().toString() + ", ";
                } else {
                    text += mContext.getString(R.string.automatic_annotation_old) + ": 0.0, ";
                }

                if (isBasalDinnerActivated()) {
                    text += mContext.getString(R.string.automatic_annotation_new) + ": " + getBasalDoseDinner().toString();
                } else {
                    text += mContext.getString(R.string.automatic_annotation_new) + ": 0.0";
                }

            }
            // automatic annotation
            if (!text.equals("")) {
                Annotation.saveCafydiaAutomaticAnnotation(mContext, text);
            }
        }
    }

    public Float getBasalDose(Meal m){
        switch(m.getMealTime()){
            case C.MEAL_BREAKFAST:
                return basalDoseBreakfast;
            case C.MEAL_LUNCH:
                return basalDoseLunch;
            case C.MEAL_DINNER:
                return basalDoseDinner;
            default:
                return null;
        }
    }
    public Float getBasalDose(int mealTime){
        switch(mealTime){
            case C.MEAL_BREAKFAST:
                return basalDoseBreakfast;
            case C.MEAL_LUNCH:
                return basalDoseLunch;
            case C.MEAL_DINNER:
                return basalDoseDinner;
            default:
                return null;
        }
    }

    public Float getBasalDoseBreakfast() {
        return basalDoseBreakfast;
    }

    public void setBasalDoseBreakfast(Float basalDoseBreakfast) {
        this.basalDoseBreakfast = basalDoseBreakfast;
    }

    public Float getBasalDoseLunch() {
        return basalDoseLunch;
    }

    public void setBasalDoseLunch(Float basalDoseLunch) {
        this.basalDoseLunch = basalDoseLunch;
    }

    public Float getBasalDoseDinner() {
        return basalDoseDinner;
    }

    public void setBasalDoseDinner(Float basalDoseDinner) {
        this.basalDoseDinner = basalDoseDinner;
    }

    public String getPreprandialNameDinner() {
        return preprandialNameDinner;
    }

    public void setPreprandialNameDinner(String preprandialNameDinner) {
        this.preprandialNameDinner = preprandialNameDinner;
    }

    public String getPreprandialNameLunch() {
        return preprandialNameLunch;
    }

    public void setPreprandialNameLunch(String preprandialNameLunch) {
        this.preprandialNameLunch = preprandialNameLunch;
    }

    public String getPreprandialNameBreakfast() {
        return preprandialNameBreakfast;
    }

    public void setPreprandialNameBreakfast(String preprandialNameBreakfast) {
        this.preprandialNameBreakfast = preprandialNameBreakfast;
    }

    public String getBasalNameDinner() {
        return basalNameDinner;
    }

    public void setBasalNameDinner(String basalNameDinner) {
        this.basalNameDinner = basalNameDinner;
    }

    public String getBasalNameLunch() {
        return basalNameLunch;
    }

    public void setBasalNameLunch(String basalNameLunch) {
        this.basalNameLunch = basalNameLunch;
    }

    public String getBasalNameBreakfast() {
        return basalNameBreakfast;
    }

    public void setBasalNameBreakfast(String basalNameBreakfast) {
        this.basalNameBreakfast = basalNameBreakfast;
    }

    public Boolean isBasalActivated(int meal) {
        switch (meal){

            case C.MEAL_BREAKFAST:
                return isBasalBreakfastActivated();

            case C.MEAL_LUNCH:
                return isBasalLunchActivated();

            case C.MEAL_DINNER:
                return isBasalDinnerActivated();

            default:
                return false;
        }
    }

    public Boolean isBasalBreakfastActivated() {
        return basalBreakfastActivated;
    }

    public void setBasalBreakfastActivated(Boolean basalBreakfastActivated) {
        this.basalBreakfastActivated = basalBreakfastActivated;
    }

    public Boolean isBasalLunchActivated() {
        return basalLunchActivated;
    }

    public void setBasalLunchActivated(Boolean basalLunchActivated) {
        this.basalLunchActivated = basalLunchActivated;
    }

    public Boolean isBasalDinnerActivated() {
        return basalDinnerActivated;
    }

    public void setBasalDinnerActivated(Boolean basalDinnerActivated) {
        this.basalDinnerActivated = basalDinnerActivated;
    }

    // for the original doses
    public Float getBasalDoseBreakfastOld() {
        return basalDoseBreakfastOld;
    }

    public Float getBasalDoseLunchOld() {
        return basalDoseLunchOld;
    }

    public Float getBasalDoseDinnerOld() {
        return basalDoseDinnerOld;
    }

    public Boolean wasBasalBreakfastActivatedOld() {
        return basalBreakfastActivatedOld;
    }

    public Boolean wasBasalLunchActivatedOld() {
        return basalLunchActivatedOld;
    }

    public Boolean wasBasalDinnerActivatedOld() {
        return basalDinnerActivatedOld;
    }
}
