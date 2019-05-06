package org.cafydia.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cafydia.android.R;
import org.cafydia.android.activities.ActivityBaseline;
import org.cafydia.android.core.Meal;
import org.cafydia.android.datadatabase.DataDatabase;
import org.cafydia.android.genericdialogfragments.DialogConfirmation;
import org.cafydia.android.genericdialogfragments.DialogGetTextOrNumber;
import org.cafydia.android.recommendations.BaselineBasal;
import org.cafydia.android.recommendations.BaselinePreprandial;
import org.cafydia.android.tutorial.HelpFragmentBundle;
import org.cafydia.android.tutorial.Tutorial;
import org.cafydia.android.util.Averages;
import org.cafydia.android.util.C;
import org.cafydia.android.views.BaselineChartView;
import org.cafydia.android.views.ModificationZoneView;

/**
 * Created by user on 19/03/15.
 */
public class BaselineFragment extends Fragment {
    public static final String PREF_KEY = "pref_key_advanced_hide_sections_no_modification";

    public static final String MINIMUM_PREPRANDIAL_KEY = "minimum_dose_for_preprandial";
    public static final String MINIMUM_PREPRANDIAL_VALUE_BR = "minimum_dose_for_preprandial_breakfast";
    public static final String MINIMUM_PREPRANDIAL_VALUE_LU = "minimum_dose_for_preprandial_lunch";
    public static final String MINIMUM_PREPRANDIAL_VALUE_DI = "minimum_dose_for_preprandial_dinner";

    private int mMealTime;
    private Float mAverageCarbohydrates;
    private Meal mLastMeal;

    private BaselineChartView mBaselineChart;

    private ModificationZoneView mModificationZoneView;

    private TextView tvPreprandial, tvBasal;
    private EditText etNewPreprandial, etNewBasal;

    // for the minimum
    private LinearLayout lMinimum;
    private ImageView ivMinimum;
    private TextView tvMinimum;

    private boolean mAllDataOk = false;
    private boolean hideSectionsWithNoModification;

    private Averages mAverages;

    private HelpFragmentBundle mHelpFragmentBundle;

    private Handler mHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_baseline_for_meal, container, false);

        mHelpFragmentBundle = new HelpFragmentBundle(getActivity(), R.id.help);

        mHandler = new Handler();

        mAverages = getAverages();

        searchViews(layout);

        restoreInstanceState(savedInstanceState);

        //
        // here we update the UI
        //
        if(getPreprandial() != null){
            refreshUI();
        } else {
            mHandlerUI.post(updateUI);
        }

        getAverageHandler.post(getAverageRunnable);

        mBaselineChart.refresh();

        mModificationZoneView.setVisibility(View.GONE);

        updateMinimumPreprandialInUI();

        //
        // Listeners
        //
        etNewPreprandial.addTextChangedListener(mPreprandialListener);
        etNewBasal.addTextChangedListener(mBasalListener);

        mModificationZoneView.setOnModificationZoneChangeListener(new ModificationZoneView.OnModificationZoneChangeListener() {
            @Override
            public void onModificationChange(int typeOfModification) {
                if (isPreprandialOk()) {
                    updateModificationInChart();
                }
            }
        });

        lMinimum.setOnClickListener(mMinimumListener);

        return layout;
    }

    private Handler mHandlerUI = new Handler();
    private Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            mHandlerUI.removeCallbacks(updateUI);

            refreshUI();

            if(getPreprandial() == null){
                mHandlerUI.postDelayed(updateUI, 50);
            }
        }
    };



    private void refreshUI(){
        mBaselineChart.setMealToDraw(mLastMeal);
        mBaselineChart.setAverageCarbohydrates(mAverageCarbohydrates);

        if(getPreprandial() != null) {
            mBaselineChart.setMainLineParameters(getPreprandial().getMByMeal(mMealTime), getPreprandial().getBByMeal(mMealTime));
        }

        tvPreprandial.setText(mLastMeal != null ? mLastMeal.getTotalPreprandialDose().toString() : "0.0");

        if(getBasal() != null) {
            tvBasal.setText(getBasal().getBasalDose(mMealTime) != null ? Integer.toString(getBasal().getBasalDose(mMealTime).intValue()) : "0");
        }

        mBaselineChart.white();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateAllDataOk();
                getActivity().invalidateOptionsMenu();
            }
        });
    }

    public Meal getLastMeal() {
        return mLastMeal;
    }

    private Runnable getAverageRunnable = new Runnable() {

        private int n = 0;

        @Override
        public void run() {
            getAverageHandler.removeCallbacks(getAverageRunnable);

            mAverageCarbohydrates = getAverages() != null ? getAverages().getAvCarbohydrates(mMealTime) : null;

            if(mLastMeal != null) {
                if(mAverageCarbohydrates != null) {

                    if (mLastMeal.getMealCarbohydrates() > mAverageCarbohydrates) {
                        mModificationZoneView.mealAboveAverage();

                    } else if (mLastMeal.getMealCarbohydrates() <= mAverageCarbohydrates) {
                        mModificationZoneView.mealBelowAverage();
                    }

                    refreshUI();
                    mBaselineChart.refresh();

                    getAverageHandler.removeCallbacks(getAverageRunnable);

                } else {
                    mModificationZoneView.mealWithoutAverage();
                    //refreshUI();
                    //mBaselineChart.refresh();

                    if(n < 200) {
                        getAverageHandler.postDelayed(getAverageRunnable, 50);
                        n ++;
                    }
                }

            } else {
                DataDatabase db = getDataDatabase();
                mLastMeal = db != null ? db.getLastMealAdded(mMealTime) : null;

                if(n < 200) {
                    getAverageHandler.postDelayed(getAverageRunnable, 50);
                    n ++;
                }
            }
        }
    };

    private Handler getAverageHandler = new Handler();



    private void updateModificationInChart(){
        if(isPreprandialOk()) {
            mBaselineChart.setSecondaryLineParameters(getPreprandial().getNewM(), getPreprandial().getNewB());
            mModificationZoneView.setVisibility(View.VISIBLE);

            Tutorial.Baseline.aboutModificationZones(mHelpFragmentBundle);
        } else {
            mBaselineChart.setSecondaryLineParameters(null, null);
            mModificationZoneView.setVisibility(View.GONE);
        }
        mBaselineChart.refresh();

    }

    //
    // When in the activity detects a click on done action button, here we save changes
    //
    public void saveChanges(){
        // preprandial
        if(mLastMeal != null && !etNewPreprandial.getText().toString().equals("")) {
            getPreprandial().saveNewParameters(mLastMeal, Float.parseFloat(etNewPreprandial.getText().toString()));
        }

        // basal
        String t = etNewBasal.getText().toString();
        if(!t.equals("")) {
            Float basalDose = Float.parseFloat(t);

            if(basalDose > 0) {
                switch (mMealTime) {
                    case C.MEAL_BREAKFAST:
                        getBasal().setBasalBreakfastActivated(true);
                        getBasal().setBasalDoseBreakfast(Float.parseFloat(t));
                        break;

                    case C.MEAL_LUNCH:
                        getBasal().setBasalLunchActivated(true);
                        getBasal().setBasalDoseLunch(Float.parseFloat(t));
                        break;

                    case C.MEAL_DINNER:
                        getBasal().setBasalDinnerActivated(true);
                        getBasal().setBasalDoseDinner(Float.parseFloat(t));
                        break;
                }

            } else {

                switch (mMealTime) {
                    case C.MEAL_BREAKFAST:
                        getBasal().setBasalBreakfastActivated(false);
                        getBasal().setBasalDoseBreakfast(0f);
                        break;

                    case C.MEAL_LUNCH:
                        getBasal().setBasalLunchActivated(false);
                        getBasal().setBasalDoseLunch(0f);
                        break;

                    case C.MEAL_DINNER:
                        getBasal().setBasalDinnerActivated(false);
                        getBasal().setBasalDoseDinner(0f);
                        break;
                }

            }
            getBasal().save();
        }

        etNewPreprandial.removeTextChangedListener(mPreprandialListener);
        etNewPreprandial.setText("");
        etNewPreprandial.addTextChangedListener(mPreprandialListener);

        etNewBasal.removeTextChangedListener(mBasalListener);
        etNewBasal.setText("");
        etNewBasal.addTextChangedListener(mBasalListener);

        mBaselineChart.setMainLineParameters(getPreprandial().getMByMeal(mMealTime), getPreprandial().getBByMeal(mMealTime));
        mBaselineChart.setSecondaryLineParameters(null, null);

        if(mLastMeal != null) {
            mLastMeal = new DataDatabase(getActivity()).getMealById(mLastMeal.getMealId());
        }

        mBaselineChart.setMealToDraw(mLastMeal);
        mBaselineChart.refresh();

        tvPreprandial.setText(mLastMeal != null ? mLastMeal.getTotalPreprandialDose().toString() : "0.0");
        tvBasal.setText(getBasal().getBasalDose(mMealTime) != null ? Integer.toString(getBasal().getBasalDose(mMealTime).intValue()) : "0");

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateAllDataOk();
                getActivity().invalidateOptionsMenu();
            }
        });

    }

    public boolean isAllDataOk(){
        return mAllDataOk;
    }

    public boolean hasLastMeal(){
        return mLastMeal != null;
    }


    //
    // Search Views and Restore the Instance State
    //
    private void restoreInstanceState(Bundle savedInstanceState) {
        Activity a = getActivity();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(a);
        hideSectionsWithNoModification = sp.getBoolean(PREF_KEY, true);

        mMealTime = getArguments().getInt("meal");

        DataDatabase db = getDataDatabase();


        mLastMeal = db != null ? db.getLastMealAdded(mMealTime) : null;

    }

    private void searchViews(View layout){
        mBaselineChart = (BaselineChartView) layout.findViewById(R.id.baselineChart);

        mModificationZoneView = (ModificationZoneView) layout.findViewById(R.id.modificationZoneView);

        tvPreprandial = (TextView) layout.findViewById(R.id.tvPreprandial);
        tvBasal = (TextView) layout.findViewById(R.id.tvBasal);

        etNewPreprandial = (EditText) layout.findViewById(R.id.etNewPreprandial);
        etNewBasal = (EditText) layout.findViewById(R.id.etNewBasal);

        lMinimum = (LinearLayout) layout.findViewById(R.id.lMinimum);
        ivMinimum = (ImageView) layout.findViewById(R.id.ivMinimum);
        tvMinimum = (TextView) layout.findViewById(R.id.tvMinimum);
    }

    private void updateAllDataOk(){
        mAllDataOk = isPreprandialOk() | isBasalOk();
    }

    private boolean isPreprandialOk(){
        if(etNewPreprandial.getText().toString().equals("")) return false;

        int typeOfModification = mModificationZoneView.getTypeOfModification();
        float newDose = Float.parseFloat(etNewPreprandial.getText().toString());

        return getPreprandial().processModification(mLastMeal, newDose, typeOfModification);
    }

    private boolean isBasalOk(){
        return !etNewBasal.getText().toString().equals("") && !tvBasal.getText().toString().equals(etNewBasal.getText().toString());
    }

    private void updateMinimumPreprandialInUI(){
        SharedPreferences sp = getActivity().getSharedPreferences(MINIMUM_PREPRANDIAL_KEY, Context.MODE_PRIVATE);

        String value = "";
        switch (mMealTime) {
            case C.MEAL_BREAKFAST:
                value = MINIMUM_PREPRANDIAL_VALUE_BR;
                break;
            case C.MEAL_LUNCH:
                value = MINIMUM_PREPRANDIAL_VALUE_LU;
                break;
            case C.MEAL_DINNER:
                value = MINIMUM_PREPRANDIAL_VALUE_DI;
                break;
        }

        if(!value.equals("")) {
            Float min = sp.getFloat(value, 0.0f);
            tvMinimum.setText(min.toString());
            if (min != 0.0) {
                ivMinimum.setBackgroundResource(R.color.preprandialLight);
            } else {
                ivMinimum.setBackgroundColor(0xFFFFFFFF);
            }
        }

    }


    //
    // Listeners
    //
    private TextWatcher mPreprandialListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateModificationInChart();
            updateAllDataOk();
            getActivity().invalidateOptionsMenu();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher mBasalListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateAllDataOk();
            getActivity().invalidateOptionsMenu();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private View.OnClickListener mMinimumListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DialogConfirmation.newInstance(
                    "establish_minimum",
                    mConfirmListener,
                    R.string.activity_baseline_confirmation_minimum_title,
                    R.string.activity_baseline_confirmation_minimum_message,
                    null
            ).show(getActivity().getFragmentManager(), null);
        }
    };

    private DialogConfirmation.OnConfirmListener mConfirmListener = new DialogConfirmation.OnConfirmListener() {
        @Override
        public void onConfirmPerformed(String tag, boolean confirmation, Object object) {
            if(tag.equals("establish_minimum")){
                if(confirmation) {
                    DialogGetTextOrNumber.newInstance(
                            "establish_minimum",
                            getString(R.string.activity_baseline_establish_minimum_title),
                            null,
                            mTextIntroducedListener,
                            true
                    ).show(getActivity().getFragmentManager(), null);
                }
            }
        }
    };

    private DialogGetTextOrNumber.OnTextIntroducedListener mTextIntroducedListener = new DialogGetTextOrNumber.OnTextIntroducedListener() {
        @Override
        public void onTextIntroduced(String tag, String text, View targetView) {
            if(tag.equals("establish_minimum")) {

                if(text.equals("")) {
                    text = "0.0";
                }

                Float min = Float.parseFloat(text);
                SharedPreferences sp = getActivity().getSharedPreferences(MINIMUM_PREPRANDIAL_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();

                String value = "";
                switch (mMealTime) {
                    case C.MEAL_BREAKFAST:
                        value = MINIMUM_PREPRANDIAL_VALUE_BR;
                        break;
                    case C.MEAL_LUNCH:
                        value = MINIMUM_PREPRANDIAL_VALUE_LU;
                        break;
                    case C.MEAL_DINNER:
                        value = MINIMUM_PREPRANDIAL_VALUE_DI;
                        break;
                }

                if(!value.equals("")) {
                    editor.putFloat(value, min);
                    editor.apply();
                }


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateMinimumPreprandialInUI();
                    }
                }, 200);
            }
        }
    };

    private Averages getAverages(){
        if(getActivity() == null)
            return null;

        return ((ActivityBaseline) getActivity()).getAverages();
    }

    public BaselineBasal getBasal() {
        if(getActivity() == null)
            return null;

        return ((ActivityBaseline) getActivity()).getBasal();
    }

    public BaselinePreprandial getPreprandial() {
        if(getActivity() == null)
            return null;

        return ((ActivityBaseline) getActivity()).getPreprandial();
    }

    public DataDatabase getDataDatabase() {
        if(getActivity() == null)
            return null;

        return ((ActivityBaseline) getActivity()).getDataDatabase();
    }


}
