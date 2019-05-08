package org.cafydia4.android.recommendations;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import org.cafydia4.android.configdatabase.ConfigurationDatabase;
import org.cafydia4.android.core.GlucoseTest;
import org.cafydia4.android.core.Meal;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.util.Averages;
import org.cafydia4.android.util.C;

import java.util.ArrayList;

public class CorrectionFactor {
    private Integer mDefaultType = C.FOR_CORRECTION_FACTOR_INSULIN_SYNTHETIC;
    private Float mTotalInsulin = 0.0f;
    private Handler mHandler = new Handler();

    private Averages mAverages;
    private BaselinePreprandial mPreprandial;
    private BaselineBasal mBasal;
    private ConfigurationDatabase mConfigurationDatabase;
    private DataDatabase mDataDatabase;
    private MetabolicFramework mFramework;

    private Meal mBreakfast;
    private Meal mLunch;
    private Meal mDinner;


    private float correctionFactorAboveValue = 0f;
    private float correctionFactorBelowValue = 0f;

    public CorrectionFactor(Context activity, MetabolicFramework framework){

        mFramework = framework;

        mPreprandial = mFramework.getBaselinePreprandial();
        mBasal = mFramework.getBaselineBasal();
        mDataDatabase = mFramework.getDataDatabase();
        mConfigurationDatabase = mFramework.getConfigDatabase();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        correctionFactorAboveValue = Float.parseFloat(sp.getString("pref_key_correction_factor_above", "100"));
        correctionFactorBelowValue = Float.parseFloat(sp.getString("pref_key_correction_factor_below", "100"));

        int metabolicId = framework.getState().getActivatedMetabolicRhythmId();
        String metabolicName = framework.getEnabledMetabolicRhythm().getName();

        mBreakfast = new Meal(C.MEAL_BREAKFAST, metabolicId, metabolicName);
        mLunch = new Meal(C.MEAL_LUNCH, metabolicId, metabolicName);
        mDinner = new Meal(C.MEAL_DINNER, metabolicId, metabolicName);

        mAverages = new Averages(activity);
        mHandler.post(mCalculateRunnable);

    }

    private Runnable mCalculateRunnable = new Runnable() {
        @Override
        public void run() {
            int metabolicId = mFramework.getState().getActivatedMetabolicRhythmId();

            Float carbBr = mAverages.getAvCarbohydrates(C.MEAL_BREAKFAST);
            Float carbLu = mAverages.getAvCarbohydrates(C.MEAL_LUNCH);
            Float carbDi = mAverages.getAvCarbohydrates(C.MEAL_DINNER);

            if (mPreprandial.getMByMeal(mBreakfast.getMealTime()) != null &&
                    mPreprandial.getMByMeal(mLunch.getMealTime()) != null &&
                    mPreprandial.getMByMeal(mDinner.getMealTime()) != null &&
                    mPreprandial.getMByMeal(mBreakfast.getMealTime()) != 0.0f &&
                    mPreprandial.getMByMeal(mLunch.getMealTime()) != 0.0f &&
                    mPreprandial.getMByMeal(mDinner.getMealTime()) != 0.0f &&

                    carbBr != null && carbLu != null & carbDi != null) {


                mBreakfast.setMealCarbohydrates(carbBr);
                mLunch.setMealCarbohydrates(carbLu);
                mDinner.setMealCarbohydrates(carbDi);


                ArrayList<Corrective> correctives = mConfigurationDatabase.getCorrectivesSorted(metabolicId);

                ArrayList<Corrective> cBreakfast = new ArrayList<Corrective>();
                ArrayList<Corrective> cLunch = new ArrayList<Corrective>();
                ArrayList<Corrective> cDinner = new ArrayList<Corrective>();

                for (Corrective corrective : correctives){
                    if(corrective.applies(mBreakfast)){
                        corrective.setTemporalState(true);
                        cBreakfast.add(corrective);
                    }
                    else if(corrective.applies(mLunch)){
                        corrective.setTemporalState(true);
                        cLunch.add(corrective);
                    }
                    else if(corrective.applies(mDinner)) {
                        corrective.setTemporalState(true);
                        cDinner.add(corrective);
                    }
                }

                mFramework.fillRecommendationData(mBreakfast, cBreakfast);
                mFramework.fillRecommendationData(mLunch, cLunch);
                mFramework.fillRecommendationData(mDinner, cDinner);


                addInsulinDose(mBreakfast.getTotalPreprandialDose());
                addInsulinDose(mLunch.getTotalPreprandialDose());
                addInsulinDose(mDinner.getTotalPreprandialDose());

                addInsulinDose(mBreakfast.getTotalBasalDose());
                addInsulinDose(mLunch.getTotalBasalDose());
                addInsulinDose(mDinner.getTotalBasalDose());

            } else {

                if(mAverages.getAvPreprandialDoseBreakfast() != null &&
                        mAverages.getAvPreprandialDoseLunch() != null &&
                        mAverages.getAvPreprandialDoseDinner() != null) {

                    addInsulinDose(mAverages.getAvPreprandialDoseBreakfast());
                    addInsulinDose(mAverages.getAvPreprandialDoseLunch());
                    addInsulinDose(mAverages.getAvPreprandialDoseDinner());
                    addInsulinDose(mAverages.getAvBasalDoseBreakfast());
                    addInsulinDose(mAverages.getAvBasalDoseLunch());
                    addInsulinDose(mAverages.getAvBasalDoseDinner());
                }

            }
        }
    };

    public Float getCorrectionFactor(){

        if(mTotalInsulin != 0.0){
            return mDefaultType / mTotalInsulin;
        } else {
            return null;
        }
    }

    public Float getModificationByGlucose(GlucoseTest g){
        if (g != null && getCorrectionFactor() != null ) {
            if(g.getGlucoseLevel() > correctionFactorAboveValue) {
                return (g.getGlucoseLevel() - correctionFactorAboveValue) / getCorrectionFactor();
            }
            else if(g.getGlucoseLevel() < correctionFactorBelowValue) {
                return (g.getGlucoseLevel() - correctionFactorBelowValue) / getCorrectionFactor();
            }

        }
        return 0.0f;
    }

    private void addInsulinDose(Float dose){
        if(dose != null)
            mTotalInsulin += dose;
    }
}
