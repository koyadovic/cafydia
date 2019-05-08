package org.cafydia4.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.cafydia4.android.R;

/**
 * Created by user on 12/09/14.
 *
 *  Internally Cafydia work with mg/dL and grams for weight.
 */

public class UnitChanger {
    private static final String KEY_UNITS_WEIGHT = "pref_key_units_weight";
    private static final String KEY_UNITS_GLUCOSE = "pref_key_units_glucose";

    private Context context;

    private int weightUnitSelection;
    private int glucoseUnitSelection;


    // constructor
    public UnitChanger(Context context){
        this.context = context;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String weight = sp.getString(KEY_UNITS_WEIGHT, "");
        String glucose = sp.getString(KEY_UNITS_GLUCOSE, "");

        if(!weight.equals("")){
            weightUnitSelection = Integer.parseInt(weight);
        }
        if(!glucose.equals("")){
            glucoseUnitSelection = Integer.parseInt(glucose);
        }
    }

    // units for weight
    public static Float gramsToPounds(Float grams){
        return grams /  453.59237f;
    }

    public static Float poundsToGrams(Float pounds){
        return pounds * 453.59237f;
    }

    public static Float gramsToOunces(Float grams){
        return grams / 28.3495231f;
    }

    public static Float ouncesToGrams(Float ounces){
        return ounces * 28.3495231f;
    }

    public static Float ouncesToPounds(Float ounces) {
        return ounces / 16.0f;
    }

    public static Float poundsToOunces(Float pounds){
        return pounds * 16.0f;
    }

    // units for glycemia tests
    public static Float mgdlTommoll(Float mgdl){
        return mgdl / 18.0182f;
    }
    public static Float mmollTomgdl(Float mmoll){
        return mmoll * 18.0182f;
    }

    public Integer getDecimalsForWeight(){
        switch(weightUnitSelection){
            case C.PREFERENCE_WEIGHT_POUNDS:
                return 3;
            case C.PREFERENCE_WEIGHT_OUNCES:
                return 2;
            default:
                return 1;
        }
    }

    public Integer getDecimalsForGlucose(){
        switch(weightUnitSelection){
            case C.PREFERENCE_GLUCOSE_MGDL:
                return 0;
            case C.PREFERENCE_GLUCOSE_MMOLL:
                return 1;
            default:
                return 1;
        }
    }

    // public methods
    public Float toUIFromInternalWeight(Float grams){
        switch(weightUnitSelection){
            case C.PREFERENCE_WEIGHT_GRAMS:
                return grams;

            case C.PREFERENCE_WEIGHT_POUNDS:
                return gramsToPounds(grams);

            case C.PREFERENCE_WEIGHT_OUNCES:
                return gramsToOunces(grams);

            default:
                return null;
        }

    }

    public Float toInternalWeightFromUI(Float selectedUnit){
        switch(weightUnitSelection){
            case C.PREFERENCE_WEIGHT_GRAMS:
                return selectedUnit;

            case C.PREFERENCE_WEIGHT_POUNDS:
                return poundsToGrams(selectedUnit);

            case C.PREFERENCE_WEIGHT_OUNCES:
                return ouncesToGrams(selectedUnit);

            default:
                return null;

        }
    }

    public Float toUIFromInternalGlucose(Float mgdl){
        switch(glucoseUnitSelection){
            case C.PREFERENCE_GLUCOSE_MGDL:
                return mgdl;

            case C.PREFERENCE_GLUCOSE_MMOLL:
                return mgdlTommoll(mgdl);

            default:
                return null;
        }
    }

    public String toUIFromInternalGlucoseString(Float mgdl){
        switch(glucoseUnitSelection){
            case C.PREFERENCE_GLUCOSE_MGDL:
                return MyRound.round(mgdl, 0).intValue() + "";

            case C.PREFERENCE_GLUCOSE_MMOLL:
                return MyRound.round(mgdlTommoll(mgdl), 1).toString();

            default:
                return null;
        }
    }

    public Float toInternalGlucoseFromUI(Float selectedUnit){
        switch(glucoseUnitSelection){
            case C.PREFERENCE_GLUCOSE_MGDL:
                return selectedUnit;

            case C.PREFERENCE_GLUCOSE_MMOLL:
                return mmollTomgdl(selectedUnit);

            default:
                return null;
        }
    }

    public String getStringUnitForWeightInTheUI(){
        return context != null ? context.getResources().getStringArray(R.array.preferences_units)[weightUnitSelection] : null;
    }
    public String getStringUnitForGlucose(){
        return context != null ? context.getResources().getStringArray(R.array.preferences_glucose)[glucoseUnitSelection] : null;
    }
    public String getStringUnitForWeightShort(){
        return context != null ? context.getResources().getStringArray(R.array.preferences_units_short)[weightUnitSelection] : null;
    }


}
