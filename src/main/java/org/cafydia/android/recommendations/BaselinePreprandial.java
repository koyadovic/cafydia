package org.cafydia.android.recommendations;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import org.cafydia.android.R;
import org.cafydia.android.chartobjects.DataCollectionCriteria;
import org.cafydia.android.chartobjects.DataCollectionCriteriaInstant;
import org.cafydia.android.chartobjects.GlucoseTestsCrossedMeals;
import org.cafydia.android.core.Annotation;
import org.cafydia.android.core.GlucoseTest;
import org.cafydia.android.core.Instant;
import org.cafydia.android.core.Meal;
import org.cafydia.android.datadatabase.DataDatabase;
import org.cafydia.android.dialogfragments.DialogShowPreprandialFitState;
import org.cafydia.android.util.Averages;
import org.cafydia.android.util.C;
import org.cafydia.android.util.LinearFunction;
import org.cafydia.android.util.MyRound;
import org.cafydia.android.util.PolynomialFitter;
import org.cafydia.android.util.UnitChanger;

/**
 * Created by miguel on 17/06/14.
 *
 * La clase BaselinePreprandial es una clase que mediante funciones lineales es capaz de decirnos
 * qué dosis corresponde a qué cantidad de carbohidratos.
 */
public class BaselinePreprandial {

    private String mBreakfastState;
    private String mLunchState;
    private String mDinnerState;

    private Averages mAverages;
    private Handler mHandler = new Handler();

    private Float mBrM = 0.0f, mBrB = 0.0f;
    private Float mLuM = 0.0f, mLuB = 0.0f;
    private Float mDiM = 0.0f, mDiB = 0.0f;

    private Float mAverageBr = 0f, mAverageLu = 0f, mAverageDi = 0f;

    private Float newM, newB;

    private Context mContext = null;

    public static final String SHARED_PREFERENCES_FUNCTION_KEY = "dose_by_meal";

    public BaselinePreprandial(Context c) {
        mContext = c;

        if (mContext != null) {
            SharedPreferences sp = mContext.getSharedPreferences(SHARED_PREFERENCES_FUNCTION_KEY, Context.MODE_PRIVATE);

            mBrM = sp.getFloat("brm", 0);
            mBrB = sp.getFloat("brb", 0);
            mLuM = sp.getFloat("lum", 0);
            mLuB = sp.getFloat("lub", 0);
            mDiM = sp.getFloat("dim", 0);
            mDiB = sp.getFloat("dib", 0);

            mAverages = new Averages(mContext, new Averages.OnAveragesCalculatedListener() {
                @Override
                public void onAveragesCalculated() {
                    mHandler.post(mGetAveragesRunnable);
                }
            });


        }
    }

    private Runnable mGetAveragesRunnable = new Runnable() {
        @Override
        public void run() {
            mAverageBr = mAverages.getAvCarbohydrates(C.MEAL_BREAKFAST);
            mAverageLu = mAverages.getAvCarbohydrates(C.MEAL_LUNCH);
            mAverageDi = mAverages.getAvCarbohydrates(C.MEAL_DINNER);
        }
    };


    public boolean areAllFunctionsGenerated(){
        return mBrM != 0 && mLuM != 0 && mDiM != 0;
    }

    public boolean isFunctionGenerated(int meal) {
        switch(meal){
            case C.MEAL_BREAKFAST:
                return mBrM != null && mBrM != 0;
            case C.MEAL_LUNCH:
                return mLuM != null && mLuM != 0;
            case C.MEAL_DINNER:
                return mDiM != null && mDiM != 0;
        }
        return false;

    }

    public Float getMByMeal(int m) {
        switch(m){
            case C.MEAL_BREAKFAST:
                return mBrM;
            case C.MEAL_LUNCH:
                return mLuM;
            case C.MEAL_DINNER:
                return mDiM;
        }
        return null;

    }

    public Float getAverage(int m){
        switch(m){
            case C.MEAL_BREAKFAST:
                return mAverageBr;
            case C.MEAL_LUNCH:
                return mAverageLu;
            case C.MEAL_DINNER:
                return mAverageDi;
        }
        return 0f;
    }

    public Float getBByMeal(int m) {
        switch(m){
            case C.MEAL_BREAKFAST:
                return mBrB;
            case C.MEAL_LUNCH:
                return mLuB;
            case C.MEAL_DINNER:
                return mDiB;
        }
        return null;
    }

    public void setMByMeal(float m, int me) {
        switch(me){
            case C.MEAL_BREAKFAST:
                mBrM = m;
                break;
            case C.MEAL_LUNCH:
                mLuM = m;
                break;
            case C.MEAL_DINNER:
                mDiM = m;
                break;
        }
        save(me);
    }

    public void setBByMeal(float b, int me) {
        switch(me){
            case C.MEAL_BREAKFAST:
                mBrB = b;
                break;
            case C.MEAL_LUNCH:
                mLuB = b;
                break;
            case C.MEAL_DINNER:
                mDiB = b;
                break;
        }
        save(me);
    }

    private void save(int meal){
        if (mContext != null) {
            SharedPreferences sp = mContext.getSharedPreferences(SHARED_PREFERENCES_FUNCTION_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            editor.putFloat("brm", mBrM);
            editor.putFloat("brb", mBrB);
            editor.putFloat("lum", mLuM);
            editor.putFloat("lub", mLuB);
            editor.putFloat("dim", mDiM);
            editor.putFloat("dib", mDiB);

            editor.apply();

            Fitter.preprandialBaselineChange(mContext, meal);

            new BackupManager(mContext).dataChanged();
        }

    }

    private boolean modifyFunctionParameters(Meal m, float newDose, int typeOfModification) {
        if(m == null) return false;

        float oldDose = m.getTotalPreprandialDose();

        float diffDoseModification = newDose - oldDose;

        float minCarb = 0.0f;
        float maxCarb = 0.0f;
        float minDose = 0.0f;
        float maxDose = 0.0f;

        float mParam = getMByMeal(m.getMealTime());
        float bParam = getBByMeal(m.getMealTime());

        switch (typeOfModification) {
            case C.LINEAR_FUNCTIONS_MODIFICATION_BY_ITS_INDEX:
                minCarb = m.getMealCarbohydrates() / 2.0f;
                maxCarb = m.getMealCarbohydrates() * 2.0f;

                minDose = (minCarb * mParam) + bParam;
                maxDose = (maxCarb * mParam) + bParam;

                minDose += diffDoseModification;
                maxDose += diffDoseModification;
                break;
            case C.LINEAR_FUNCTIONS_MODIFICATION_BY_THE_LEFT:
                minCarb = m.getMealCarbohydrates();

                if (getAverage(m.getMealTime()) != 0) {
                    maxCarb = getAverage(m.getMealTime()) * 1.5f;
                } else {
                    maxCarb = m.getMealCarbohydrates() * 2.5f;
                }

                minDose = (minCarb * mParam) + bParam;
                maxDose = (maxCarb * mParam) + bParam;

                minDose += diffDoseModification;
                break;
            case C.LINEAR_FUNCTIONS_MODIFICATION_BY_THE_RIGHT:

                if (getAverage(m.getMealTime()) != 0f) {
                    minCarb = getAverage(m.getMealTime()) / 3f;
                } else {
                    minCarb = m.getMealCarbohydrates() / 3.0f;
                }

                maxCarb = m.getMealCarbohydrates();

                minDose = (minCarb * mParam) + bParam;
                maxDose = (maxCarb * mParam) + bParam;

                maxDose += diffDoseModification;
                break;
        }

        newM = (maxDose - minDose) / (maxCarb - minCarb);
        newB = minDose - (minCarb * newM);

        return newM >= 0.0;
    }



    public Float getPreprandialBaselineDoseForMeal(Meal meal){
        Float m, b;
        Float doseRecommendation;

        m = getMByMeal(meal.getMealTime());
        b = getBByMeal(meal.getMealTime());

        if(m != null) {
            doseRecommendation = (meal.getMealCarbohydrates() * m) + b;
            return doseRecommendation;
        } else {
            return null;
        }
    }

    public Float getPreprandialBaselineDoseForCarbs(int mealTime, float carbs){
        Float m, b;
        Float doseRecommendation;

        m = getMByMeal(mealTime);
        b = getBByMeal(mealTime);

        if(m != null) {
            doseRecommendation = (carbs * m) + b;
            return doseRecommendation;
        } else {
            return null;
        }
    }



    /*
     * lo primero es llamar a processModification con el last meal, la nueva dosis y el tipo de modificación
     * y recoger el boolean.
     *
     * Si éste es correcto, entonces se pueden recuperar M y B con getNewM y getNewB para mostrarlos en el chart.
     *
     * Si al final el usuario acepta cambiar definitivamente la dosis, tendrá que llamarse al método  saveNewParameters(Meal, newDose)
     */

    public boolean processModification(Meal meal, Float newDose, int typeOfModification){
        boolean ok = modifyFunctionParameters(meal, newDose, typeOfModification);

        if(! ok){
            newM = null;
            newB = null;
        }

        return ok;
    }

    public Float getNewB() {
        return newB;
    }

    public Float getNewM() {
        return newM;
    }

    public void saveNewParameters(Meal meal, float newDose){
        if(newM != null && newM > 0) {

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean activated = sp.getBoolean("pref_automatic_annotations_preprandial_baseline", true);

            if (activated) {
                // automatic annotation
                String text = mContext.getString(R.string.automatic_annotation_preprandial_baseline_change);
                text += ". " + mContext.getResources().getStringArray(R.array.time_meal)[meal.getMealTime()] + ": ";
                text += mContext.getString(R.string.automatic_annotation_old) + " m=" + getMByMeal(meal.getMealTime()).toString();
                text += ", b=" + getBByMeal(meal.getMealTime()).toString();
                text += ", " + mContext.getString(R.string.automatic_annotation_new) + " m=" + newM.toString();
                text += ", b=" + newB.toString();
                Annotation.saveCafydiaAutomaticAnnotation(mContext, text);
            }

            setMByMeal(newM, meal.getMealTime());
            setBByMeal(newB, meal.getMealTime());

            sp = mContext.getSharedPreferences(SHARED_PREFERENCES_FUNCTION_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putFloat("brm", mBrM);
            editor.putFloat("brb", mBrB);
            editor.putFloat("lum", mLuM);
            editor.putFloat("lub", mLuB);
            editor.putFloat("dim", mDiM);
            editor.putFloat("dib", mDiB);
            editor.apply();
        }

        new BackupManager(mContext).dataChanged();

        DataDatabase db = new DataDatabase(mContext);
        newDose -= meal.getBeginningPreprandial();
        newDose -= meal.getCorrectionFactorPreprandial();
        newDose -= meal.getCorrectivesPreprandial();

        meal.setBaselinePreprandial(newDose);
        meal.setFinalPreprandialDose(meal.getTotalPreprandialDose());
        meal.save(db);


    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    public static class CalculateFromDatabase {
        public static final String CALCULATE_FROM_DATABASE_TAG = "calculate_from_database";
        DataDatabase mDatabase;
        Float m = null, b = null;

        private CorrectionFactor mCorrectionFactor;
        private MetabolicFramework mFramework;

        private float getCorrectedDose(float userDose, float glucoseBefore, float glucoseAfter){
            Float cf = mCorrectionFactor.getCorrectionFactor();

            if(cf != null) {
                return userDose + ((glucoseAfter - glucoseBefore - C.ADJUST_GLUCOSE_MAX_LEVEL + 100) / cf);
            } else {
                return userDose;
            }

        }

        public static String getMessage(Context c, int meal){
            SharedPreferences sp = c.getSharedPreferences(CALCULATE_FROM_DATABASE_TAG, Context.MODE_PRIVATE);
            String m = c.getResources().getStringArray(R.array.time_meal)[meal].toLowerCase();
            return sp.getString(m + "_message", "");
        }

        /*
         * Aquí se llama tres veces desde el MetabolicFramework una vez para cada una de las comidas
         */
        public CalculateFromDatabase(MetabolicFramework f, int meal){
            mFramework = f;

            mCorrectionFactor = mFramework.getCorrectionFactor();
            mDatabase = mFramework.getDataDatabase();

            DataCollectionCriteria criteria = new DataCollectionCriteria(mFramework.getContext());

            criteria.setSince(
                    new DataCollectionCriteriaInstant(
                            mFramework.getContext(),
                            mFramework.getDataDatabase(),
                            C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE,
                            -120
                    )
            );

            switch (meal){
                case C.MEAL_BREAKFAST:
                    criteria.setCollectOnGlucoseTime(C.GLUCOSE_TEST_AFTER_BREAKFAST, true);
                    criteria.setCollectOnGlucoseTime(C.GLUCOSE_TEST_BEFORE_BREAKFAST, true);
                    break;
                case C.MEAL_LUNCH:
                    criteria.setCollectOnGlucoseTime(C.GLUCOSE_TEST_AFTER_LUNCH, true);
                    criteria.setCollectOnGlucoseTime(C.GLUCOSE_TEST_BEFORE_LUNCH, true);
                    break;
                case C.MEAL_DINNER:
                    criteria.setCollectOnGlucoseTime(C.GLUCOSE_TEST_AFTER_DINNER, true);
                    criteria.setCollectOnGlucoseTime(C.GLUCOSE_TEST_BEFORE_DINNER, true);
                    break;
            }


            GlucoseTestsCrossedMeals glucosesMeals = mDatabase.getGlucoseTestsCrossedMealsByCriteria(criteria);

            GlucoseTestsCrossedMeals data = new GlucoseTestsCrossedMeals();

            for(int a = glucosesMeals.getCount() - 1; a >= 0; a --){
                GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal one = glucosesMeals.getItem(a);

                if(one.getGlucoseTestAfterMeal() != null && one.getGlucoseTestBeforeMeal() != null){
                    data.addNewElement(one);
                }
            }

            if(data.getCount() < 2) {
                // habría que guardar un mensaje comentando que hay que hacer una comida completa tal
                // el mensaje puede ser cogido por el diálogo de generate and adjust y por la actividad de las comidas.

                Fitter.changeState(mFramework.getContext(), meal, C.PREPRANDIAL_FITTER_STATE_GENERATING_NOT_VERIFIED);

                SharedPreferences sp = mFramework.getContext().getSharedPreferences(CALCULATE_FROM_DATABASE_TAG, Context.MODE_PRIVATE);

                Resources r = mFramework.getContext().getResources();
                String s = r.getString(R.string.baseline_preprandial_still_need1);

                int n = 2 - data.getCount();

                s += " " + n + " " + r.getString(R.string.baseline_preprandial_still_need2) + " ";
                s += r.getStringArray(R.array.time_meal)[meal].toLowerCase();

                SharedPreferences.Editor editor = sp.edit();
                editor.putString(r.getStringArray(R.array.time_meal)[meal].toLowerCase() + "_message", s);
                editor.commit();

                return;

            }

            PolynomialFitter dosesCorrected = new PolynomialFitter(1);

            float minc = 0f, maxc = 0f;

            GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal m;
            for(int a = 0; a < data.getCount(); a++){

                float c, d, lb, la;

                m = data.getItem(a);

                lb = m.getGlucoseTestBeforeMeal().getGlucoseLevel();
                la = m.getGlucoseTestAfterMeal().getGlucoseLevel();
                c = m.getMeal().getMealCarbohydrates();
                d = getCorrectedDose(m.getMeal().getFinalPreprandialDose(), lb, la);

                dosesCorrected.addPoint(c, d);

                // aquí hay que guardar la comida con la dosis corregida que nos proporcionó el usuario y las glucosas corregidas
                if(d != m.getMeal().getFinalPreprandialDose()) {
                    m.getMeal().setBaselinePreprandial(d);
                    m.getMeal().setFinalPreprandialDose(d);
                    m.getMeal().save(mFramework.getDataDatabase());

                    m.getGlucoseTestAfterMeal().setGlucoseLevel((int) C.ADJUST_GLUCOSE_MAX_LEVEL);
                    m.getGlucoseTestAfterMeal().save(mFramework.getDataDatabase());

                    m.getGlucoseTestBeforeMeal().setGlucoseLevel(100);
                    m.getGlucoseTestBeforeMeal().save(mFramework.getDataDatabase());
                }

                if(minc == 0f || c < minc) {
                    minc = c;
                }

                if(maxc == 0f || c > maxc) {
                    maxc = c;
                }

            }

            if(maxc - minc < 15) {
                Fitter.changeState(mFramework.getContext(), meal, C.PREPRANDIAL_FITTER_STATE_GENERATING_NOT_VERIFIED);

                // habría que guardar un mensaje comentando que hay que hacer una comida completa tal
                // el mensaje puede ser cogido por el diálogo de generate and adjust y por la actividad de las comidas.
                UnitChanger changer = new UnitChanger(mFramework.getContext());

                int l = (int) maxc - 16;
                int h = (int) minc + 16;
                String sl = MyRound.round(changer.toUIFromInternalWeight((float) l), changer.getDecimalsForWeight()) + changer.getStringUnitForWeightShort();
                String sh = MyRound.round(changer.toUIFromInternalWeight((float) h), changer.getDecimalsForWeight()) + changer.getStringUnitForWeightShort();

                SharedPreferences sp = mFramework.getContext().getSharedPreferences(CALCULATE_FROM_DATABASE_TAG, Context.MODE_PRIVATE);
                Resources r = mFramework.getContext().getResources();

                String me = r.getStringArray(R.array.time_meal)[meal].toLowerCase();
                String s = r.getString(R.string.baseline_preprandial_fit_still_need1) + " " + me + " ";
                s += r.getString(R.string.baseline_preprandial_fit_still_need2) + " " + sh + " ";
                s += r.getString(R.string.baseline_preprandial_fit_still_need3) + " " + sl + " ";
                s += r.getString(R.string.baseline_preprandial_fit_still_need4);

                SharedPreferences.Editor editor = sp.edit();
                editor.putString(me + "_message", s);
                editor.apply();

            } else {

                // aquí hay más de una comida completa, glucosas antes y después separadas por 15gr de HdC o más.

                PolynomialFitter.Polynomial dosesCorrectedPolynomial = dosesCorrected.getBestFit();

                BaselinePreprandial preprandial = mFramework.getBaselinePreprandial();

                float lowCarb = 30;
                float highCarb = 60;

                float correctedLowDose = (float) dosesCorrectedPolynomial.getY(lowCarb);
                float correctedHighDose = (float) dosesCorrectedPolynomial.getY(highCarb);

                float newM = LinearFunction.getM(lowCarb, highCarb, correctedLowDose, correctedHighDose);
                float newB = LinearFunction.getB(lowCarb, highCarb, correctedLowDose, correctedHighDose);

                preprandial.setMByMeal(newM, meal);
                preprandial.setBByMeal(newB, meal);

                Fitter.changeState(mFramework.getContext(), meal, C.PREPRANDIAL_FITTER_STATE_GENERATED_NOT_VERIFIED);

            }
        }

        public Float getM(){
            return m;
        }

        public Float getB(){
            return b;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    public static class Fitter {
        public static final String BASELINE_PREPRANDIAL_FITTER_TAG = "validate_baseline_preprandial";

        private CorrectionFactor mCorrectionFactor;
        private PolynomialFitter mFitter;

        private long mLastPreprandialChange = 0;
        private long mMaxDaysWithoutAdjust = 0;

        private String mState;
        private String mMode;

        private Context mContext;
        private MetabolicFramework mFramework;
        private DataDatabase mDatabase;

        private int mMealTime;

        // states
        private boolean mReadyToWork = false;
        private boolean mPendingToWork = false;

        public static boolean isSuccess(Context c, int meal){
            SharedPreferences sp = c.getSharedPreferences(BASELINE_PREPRANDIAL_FITTER_TAG, Context.MODE_PRIVATE);
            String mealString = C.MEAL_STRING[meal].toLowerCase();
            return sp.getBoolean(mealString + "_success", false);

        }

        public static String getSuccessMessage(Context c, int meal){
            SharedPreferences sp = c.getSharedPreferences(BASELINE_PREPRANDIAL_FITTER_TAG, Context.MODE_PRIVATE);
            String mealString = C.MEAL_STRING[meal].toLowerCase();
            return sp.getString(mealString + "_success_message", "");
        }

        private void init(){
            SharedPreferences sp = mContext.getSharedPreferences(BASELINE_PREPRANDIAL_FITTER_TAG, Context.MODE_PRIVATE);
            String mealString = C.MEAL_STRING[mMealTime];

            mState = sp.getString(mealString + "_state", C.PREPRANDIAL_FITTER_STATE_NOT_GENERATED_NOT_VERIFIED);
            mMode = sp.getString(mealString + "_mode", C.PREPRANDIAL_FITTER_MODE_FORCED);

            mLastPreprandialChange = sp.getLong(mealString + "_last_preprandial_change", 0);
            if(mLastPreprandialChange == 0){
                preprandialBaselineChange(mContext, mMealTime);
            }

            mCorrectionFactor = new CorrectionFactor(mContext, new MetabolicFramework(mContext));
            mFitter = new PolynomialFitter(1);


            // desde aquí pegado
            mCorrectionFactor = mFramework.getCorrectionFactor();
            mDatabase = mFramework.getDataDatabase();
        }

        public Fitter(Context c, GlucoseTest g){
            mReadyToWork = false;
            mContext = c;

            switch (g.getGlucoseTime()){
                case C.GLUCOSE_TEST_BEFORE_BREAKFAST:
                case C.GLUCOSE_TEST_AFTER_BREAKFAST:
                    mMealTime = C.MEAL_BREAKFAST;
                    break;
                case C.GLUCOSE_TEST_BEFORE_LUNCH:
                case C.GLUCOSE_TEST_AFTER_LUNCH:
                    mMealTime = C.MEAL_LUNCH;
                    break;
                case C.GLUCOSE_TEST_BEFORE_DINNER:
                case C.GLUCOSE_TEST_AFTER_DINNER:
                    mMealTime = C.MEAL_DINNER;
                    break;
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFramework = new MetabolicFramework(mContext, new MetabolicFramework.OnMetabolicFrameworkLoadedListener() {
                        @Override
                        public void onLoadFinished() {
                            mReadyToWork = true;

                            if(mPendingToWork)
                                mHandler.post(mWork);
                        }
                    });
                }
            });

        }

        public Fitter(Context c, int mealTime){
            mReadyToWork = false;
            mContext = c;
            mMealTime = mealTime;



            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFramework = new MetabolicFramework(mContext, new MetabolicFramework.OnMetabolicFrameworkLoadedListener() {
                        @Override
                        public void onLoadFinished() {
                            mReadyToWork = true;

                            if(mPendingToWork)
                                mHandler.post(mWork);

                        }
                    });
                }
            });
        }

        private Handler mHandler = new Handler();

        // esto corre siempre que añadimos una glucosa

        private static void log(String s){
            Log.d("fitter-log-message", s);
        }

        private Runnable mWork = new Runnable() {
            @Override
            public void run() {

                init();

                if(mMode.equals(C.PREPRANDIAL_FITTER_MODE_DISABLED)) {
                    log("fitter disabled");
                    return;
                }

                else if (mMode.equals(C.PREPRANDIAL_FITTER_MODE_AUTOMATIC) && (mLastPreprandialChange + (mMaxDaysWithoutAdjust * 24 * 60 * 60 * 1000)) > System.currentTimeMillis()) {
                    Fitter.changeState(mFramework.getContext(), mMealTime, C.PREPRANDIAL_FITTER_STATE_GENERATED_VERIFIED);
                    showDialog();
                    return;
                }

                DataCollectionCriteria criteria = new DataCollectionCriteria(mFramework.getContext());
                log("creamos el criterio");
                criteria.setSince(
                        new DataCollectionCriteriaInstant(
                                mFramework.getContext(),
                                mFramework.getDataDatabase(),
                                C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE,
                                new Instant(mLastPreprandialChange).getDaysPassedFromNow().intValue()
                        )
                );

                switch (mMealTime) {
                    case C.MEAL_BREAKFAST:
                        criteria.setCollectOnGlucoseTime(C.GLUCOSE_TEST_AFTER_BREAKFAST, true);
                        break;
                    case C.MEAL_LUNCH:
                        criteria.setCollectOnGlucoseTime(C.GLUCOSE_TEST_AFTER_LUNCH, true);
                        break;
                    case C.MEAL_DINNER:
                        criteria.setCollectOnGlucoseTime(C.GLUCOSE_TEST_AFTER_DINNER, true);
                        break;
                }

                log("recogemos los datos");
                GlucoseTestsCrossedMeals glucosesMeals = mDatabase.getGlucoseTestsCrossedMealsByCriteria(criteria);

                GlucoseTestsCrossedMeals midweek = new GlucoseTestsCrossedMeals();
                GlucoseTestsCrossedMeals weekend = new GlucoseTestsCrossedMeals();
                GlucoseTestsCrossedMeals week = new GlucoseTestsCrossedMeals();

                for (int a = glucosesMeals.getCount() - 1; a >= 0; a--) {
                    GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal one = glucosesMeals.getItem(a);

                    if (one.getGlucoseTestAfterMeal() != null) {
                        if(mMode.equals(C.PREPRANDIAL_FITTER_MODE_AUTOMATIC)) {
                            week.addNewElement(one);

                        } else {
                            if (one.getMeal().getDayOfWeek() == 5 || one.getMeal().getDayOfWeek() == 6) {
                                weekend.addNewElement(one);
                            } else {
                                midweek.addNewElement(one);
                            }
                        }
                    }
                }

                int max = midweek.getCount() < weekend.getCount() ? midweek.getCount() : weekend.getCount();

                if(mMode.equals(C.PREPRANDIAL_FITTER_MODE_FORCED) || mMode.equals(C.PREPRANDIAL_FITTER_MODE_REQUESTED)){
                    log("el modo es forzado o solicitado");
                    if(max < 2){
                        log("los datos son menores a 2");
                        Fitter.changeState(mFramework.getContext(), mMealTime, C.PREPRANDIAL_FITTER_STATE_GENERATED_VERIFYING);

                        String mealString = C.MEAL_STRING[mMealTime].toLowerCase();

                        SharedPreferences sp = mContext.getSharedPreferences(BASELINE_PREPRANDIAL_FITTER_TAG, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();

                        String s;

                        Resources r = mFramework.getContext().getResources();
                        s = r.getString(R.string.baseline_preprandial_still_need1);

                        if(midweek.getCount() > 1 && weekend.getCount() < 2){
                            // faltan en fin de semana
                            int n = 2 - weekend.getCount();
                            s += " " + n + " " + r.getString(R.string.baseline_preprandial_still_need2) + " ";
                            s += r.getStringArray(R.array.time_meal)[mMealTime].toLowerCase() + " ";
                            s += r.getString(R.string.baseline_preprandial_still_need3_weekend);
                        }
                        else if(midweek.getCount() < 2 && weekend.getCount() > 1){
                            // faltan entresemana
                            int n = 2 - midweek.getCount();
                            s += " " + n + " " + r.getString(R.string.baseline_preprandial_still_need2) + " ";
                            s += r.getStringArray(R.array.time_meal)[mMealTime].toLowerCase() + " ";
                            s += r.getString(R.string.baseline_preprandial_still_need3_midweek);

                        } else {
                            // faltan entresemana y también en fin de semana
                            int nm = 2 - midweek.getCount();
                            int nw = 2 - weekend.getCount();

                            s += " " + nm + " " + r.getString(R.string.baseline_preprandial_still_need2) + " ";
                            s += r.getStringArray(R.array.time_meal)[mMealTime].toLowerCase() + " ";
                            s += r.getString(R.string.baseline_preprandial_still_need3_midweek) + " ";
                            s += r.getString(R.string.baseline_preprandial_still_need3_and) + " ";
                            s += nw + " " + r.getString(R.string.baseline_preprandial_still_need3_weekend) + " ";
                            s += r.getString(R.string.baseline_preprandial_still_need4);
                        }

                        editor.putBoolean(mealString + "_success", false);
                        editor.putString(mealString + "_success_message", s);

                        log("establecemos el siguiente mensaje: " + s);

                        editor.commit();

                        return;
                    }
                }

                else if(mMode.equals(C.PREPRANDIAL_FITTER_MODE_AUTOMATIC) && week.getCount() < 2){
                    log("el modo es automático y los datos son menores a 2");
                    Fitter.changeState(mFramework.getContext(), mMealTime, C.PREPRANDIAL_FITTER_STATE_GENERATED_VERIFYING);

                    String mealString = C.MEAL_STRING[mMealTime].toLowerCase();

                    SharedPreferences sp = mContext.getSharedPreferences(BASELINE_PREPRANDIAL_FITTER_TAG, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();

                    String s;

                    Resources r = mFramework.getContext().getResources();
                    s = r.getString(R.string.baseline_preprandial_still_need1);

                    int n = 2 - max;

                    s += " " + n + " " + r.getString(R.string.baseline_preprandial_still_need2) + " ";
                    s += r.getStringArray(R.array.time_meal)[mMealTime].toLowerCase();

                    editor.putBoolean(mealString + "_success", false);
                    editor.putString(mealString + "_success_message", s);

                    log("añadimos el siguiente mensaje: " + s);

                    editor.commit();

                    return;
                }

                PolynomialFitter fitterAfter = new PolynomialFitter(1);

                if(mMode.equals(C.PREPRANDIAL_FITTER_MODE_AUTOMATIC)){
                    for(int a = 0; a < week.getCount(); a++){
                        float c, l;

                        c = week.getItem(a).getMeal().getMealCarbohydrates();
                        l = week.getItem(a).getGlucoseTestAfterMeal().getGlucoseLevel();

                        fitterAfter.addPoint(c, l);
                    }

                } else {
                    for (int a = 0; a < max; a++) {

                        float c, l;

                        c = midweek.getItem(a).getMeal().getMealCarbohydrates();
                        l = midweek.getItem(a).getGlucoseTestAfterMeal().getGlucoseLevel();

                        fitterAfter.addPoint(c, l);

                        c = weekend.getItem(a).getMeal().getMealCarbohydrates();
                        l = weekend.getItem(a).getGlucoseTestAfterMeal().getGlucoseLevel();

                        fitterAfter.addPoint(c, l);
                    }
                }

                PolynomialFitter.Polynomial polynomial = fitterAfter.getBestFit();

                BaselinePreprandial preprandial = mFramework.getBaselinePreprandial();

                float lowCarb = 30;
                float highCarb = 60;

                float userLowDose = preprandial.getPreprandialBaselineDoseForCarbs(mMealTime, lowCarb);
                float userHighDose = preprandial.getPreprandialBaselineDoseForCarbs(mMealTime, highCarb);

                float adjustedLowDose = (((float) polynomial.getY(lowCarb) - C.ADJUST_GLUCOSE_MAX_LEVEL) / mCorrectionFactor.getCorrectionFactor()) + userLowDose;
                float adjustedHighDose = (((float) polynomial.getY(highCarb) - C.ADJUST_GLUCOSE_MAX_LEVEL) / mCorrectionFactor.getCorrectionFactor()) + userHighDose;

                float oldM = preprandial.getMByMeal(mMealTime);
                float oldB = preprandial.getBByMeal(mMealTime);

                float newM = LinearFunction.getM(lowCarb, highCarb, adjustedLowDose, adjustedHighDose);
                float newB = LinearFunction.getB(lowCarb, highCarb, adjustedLowDose, adjustedHighDose);

                Fitter.changeState(mFramework.getContext(), mMealTime, C.PREPRANDIAL_FITTER_STATE_GENERATED_VERIFIED);

                if(mMode.equals(C.PREPRANDIAL_FITTER_MODE_FORCED) || mMode.equals(C.PREPRANDIAL_FITTER_MODE_REQUESTED)){
                    log("mostrando el dialogo para el modo forzado o solicitado");
                    showDialog(oldM, oldB, newM, newB);

                    //preprandial.setMByMeal(newM, mMealTime);
                    //preprandial.setBByMeal(newB, mMealTime);

                } else {
                    log("mostrando el dialogo para todo lo demás");
                    float meanM, meanB;

                    meanM = (oldM + newM) / 2f;
                    meanB = (oldB + newB) / 2f;

                    showDialog(oldM, oldB, meanM, meanB);

                    //preprandial.setMByMeal(meanM, mMealTime);
                    //preprandial.setBByMeal(meanB, mMealTime);

                }



                mPendingToWork = false;

            }
        };

        public void fit(){
            if(mReadyToWork)
                mHandler.post(mWork);
            else
                mPendingToWork = true;
        }

        private void showDialog(){
            showDialog(null, null, null, null);
        }

        private void showDialog(Float oldM, Float oldB, Float newM, Float newB){
            try {
                final Activity a = (Activity) mContext;
                DialogShowPreprandialFitState.newInstance(mMealTime, oldM, oldB, newM, newB).show(a.getFragmentManager(), null);

            }catch (Exception e){
                e.printStackTrace();
            }

        }

        public static void onAllBaselinesGenerated(Context c){
            log("onAllBaselinesGenerated called");
            // aquí tendríamos que ver si las funciones acaban de ser generadas para aplicar sobre ellas
            // las modificaciones pertinentes para corregir toda desviación debido a malas introducciones por parte de los usuarios.

            Fitter fitterBreakfast = new Fitter(c, C.MEAL_BREAKFAST);
            Fitter fitterLunch = new Fitter(c, C.MEAL_LUNCH);
            Fitter fitterDinner = new Fitter(c, C.MEAL_DINNER);

            fitterBreakfast.changeMode(C.PREPRANDIAL_FITTER_MODE_FORCED);
            fitterLunch.changeMode(C.PREPRANDIAL_FITTER_MODE_FORCED);
            fitterDinner.changeMode(C.PREPRANDIAL_FITTER_MODE_FORCED);

            fitterBreakfast.changeState(C.PREPRANDIAL_FITTER_STATE_GENERATED_NOT_VERIFIED);
            fitterLunch.changeState(C.PREPRANDIAL_FITTER_STATE_GENERATED_NOT_VERIFIED);
            fitterDinner.changeState(C.PREPRANDIAL_FITTER_STATE_GENERATED_NOT_VERIFIED);

        }

        public static void preprandialBaselineChange(Context c, int mealTime){
            log("change-fitter-preprandial-date" + new Instant(System.currentTimeMillis()).getUserDateString());
            SharedPreferences sp = c.getSharedPreferences(BASELINE_PREPRANDIAL_FITTER_TAG, Context.MODE_PRIVATE);
            String mealString = C.MEAL_STRING[mealTime];
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(mealString + "_last_preprandial_change", System.currentTimeMillis());
            editor.commit();

        }

        public void changeMode(String mode){
            log("change-fitter-mode " + mode);
            SharedPreferences sp = mContext.getSharedPreferences(BASELINE_PREPRANDIAL_FITTER_TAG, Context.MODE_PRIVATE);
            String mealString = C.MEAL_STRING[mMealTime];
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(mealString + "_mode", mode);
            editor.commit();
        }

        public static void changeMode(Context c, int mealTime, String mode){
            log("change-fitter-mode " + mode);
            SharedPreferences sp = c.getSharedPreferences(BASELINE_PREPRANDIAL_FITTER_TAG, Context.MODE_PRIVATE);
            String mealString = C.MEAL_STRING[mealTime];
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(mealString + "_mode", mode);
            editor.commit();

        }

        public static String getMode(Context c, int mealTime){
            SharedPreferences sp = c.getSharedPreferences(BASELINE_PREPRANDIAL_FITTER_TAG, Context.MODE_PRIVATE);
            String mealString = C.MEAL_STRING[mealTime];
            return sp.getString(mealString + "_mode", C.PREPRANDIAL_FITTER_MODE_FORCED);

        }

        public void changeState(String state){
            log("change-fitter-state " + state);
            SharedPreferences sp = mContext.getSharedPreferences(BASELINE_PREPRANDIAL_FITTER_TAG, Context.MODE_PRIVATE);
            String mealString = C.MEAL_STRING[mMealTime];
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(mealString + "_state", state);
            editor.commit();

        }

        public static void changeState(Context c, int mealTime, String state){
            log("change-fitter-state " + state);
            SharedPreferences sp = c.getSharedPreferences(BASELINE_PREPRANDIAL_FITTER_TAG, Context.MODE_PRIVATE);
            String mealString = C.MEAL_STRING[mealTime];
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(mealString + "_state", state);
            editor.commit();
        }

    }
}
