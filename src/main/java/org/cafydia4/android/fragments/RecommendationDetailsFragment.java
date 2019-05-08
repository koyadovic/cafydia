package org.cafydia4.android.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.cafydia4.android.R;
import org.cafydia4.android.activities.ActivityMealsSnacks;
import org.cafydia4.android.core.GlucoseTest;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.core.Meal;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.recommendations.BaselinePreprandial;
import org.cafydia4.android.recommendations.Corrective;
import org.cafydia4.android.recommendations.CorrectiveComplex;
import org.cafydia4.android.recommendations.CorrectiveSimple;
import org.cafydia4.android.recommendations.MetabolicFramework;
import org.cafydia4.android.util.Averages;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.MyRound;
import org.cafydia4.android.util.OnSwipeTouchListener;
import org.cafydia4.android.util.UnitChanger;
import org.cafydia4.android.views.BaselineChartView;
import org.cafydia4.android.views.CompoundCorrectiveView;
import org.cafydia4.android.views.ModificationStartView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by user on 11/03/15.
 */
public class RecommendationDetailsFragment  extends Fragment {
    private static MetabolicFramework framework;
    private BaselinePreprandial functions;
    private ArrayList<Corrective> correctivesVisibleOrActive;
    private GridLayout glCorrectives;
    private UnitChanger changer;
    private Activity hostActivity;
    private static Meal meal;
    private boolean hideSectionsWithNoModification;
    private static final String PREF_KEY = "pref_key_advanced_hide_sections_no_modification";
    private final int TIME_ANIMATION = C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS;
    private Float averageCarbohydrates;
    private GlucoseTest lastGlucoseTest;

    private TextView tvPreprandialBaseline, tvBasalBaseline, tvPreprandialBeginning, tvBasalBeginning,
            tvPreprandialCorrectives, tvPreprandialCorrectionFactor;

    private TextView tvFactorTarget, tvFactorCurrent, tvFactor, tvFactorModification;

    private TextView baselineBasalText, beginningsBasalText;

    private ImageView mIvToggleShow;
    private int mWidthFragment = -1;
    private int mWidthIvToggleShow = -1;


    private BaselineChartView baselineChart;
    private ModificationStartView preprandialBeginning, basalBeginning;

    private LinearLayout lBeginnings, lCorrectionFactor;
    private RelativeLayout lCorrectives;

    private RecommendationDetailsInterface mCallback;

    private boolean mIsPhone;
    private boolean mPanelVisible;
    private String mActionBarOldTitle;

    private Averages mAverages;

    private boolean correctionFactorAboveActivated = false;
    private boolean correctionFactorBelowActivated = false;
    private float correctionFactorAboveValue = 0f;
    private float correctionFactorBelowValue = 0f;


    public static RecommendationDetailsFragment newInstance(MetabolicFramework f, Meal m) {
        framework = f;
        meal = m;
        RecommendationDetailsFragment fr = new RecommendationDetailsFragment();

        return fr;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_recommendation_details, container, false);

        // because the orientation is fixed, by default in portrait == phone.
        mIsPhone = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        // because if it is a phone, the space is limited so by default the panel is hidden.
        mPanelVisible = !mIsPhone;

        hostActivity = getActivity();


        mCallback = (RecommendationDetailsInterface) hostActivity;
        if(framework == null) {
            framework = new MetabolicFramework(hostActivity);
        }

        searchViews(layout);

        functions = framework.getBaselinePreprandial();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(hostActivity);
        hideSectionsWithNoModification = sp.getBoolean(PREF_KEY, false);

        correctionFactorAboveActivated = sp.getBoolean("pref_key_correction_factor_above_activated", false);
        correctionFactorBelowActivated = sp.getBoolean("pref_key_correction_factor_below_activated", false);
        correctionFactorAboveValue = Float.parseFloat(sp.getString("pref_key_correction_factor_above", "100"));
        correctionFactorBelowValue = Float.parseFloat(sp.getString("pref_key_correction_factor_below", "100"));

        changer = new UnitChanger(hostActivity);

        restoreInstanceState(savedInstanceState);

        mIvToggleShow = (ImageView) layout.findViewById(R.id.ivToggleShow);
        if(mIvToggleShow != null) {
            mIvToggleShow.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {

                @Override
                public void onSwipeRight() {
                    mCallback.onRequestedHideRecommendationDetailsPanel();
                }

                @Override
                public void onSwipeLeft() {
                    mCallback.onRequestedShowRecommendationDetailsPanel();
                }
            });
        }

        DataDatabase db = framework.getDataDatabase();
        lastGlucoseTest = db.getLastGlucoseTestAdded();

        refreshAverages();


        return layout;
    }

    private void refreshAverages(){
        if(getActivity() != null)
            mAverages = ((ActivityMealsSnacks) getActivity()).getAverages();

        if(meal != null && mAverages != null) {
            averageCarbohydrates = mAverages.getAvCarbohydrates(meal.getMealTime());
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        // this code is to measure the width for show and hide operations
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mWidthFragment = metrics.widthPixels;
        mWidthIvToggleShow = (int) (metrics.density * 48);

        // if is a phone, the panel start hidden
        if(mIsPhone && !mPanelVisible) {

            final RelativeLayout.LayoutParams params = getView() != null ? (RelativeLayout.LayoutParams) getView().getLayoutParams() : null;
            if (params != null) {
                params.leftMargin = mWidthFragment - mWidthIvToggleShow;
                params.rightMargin = -mWidthFragment + mWidthIvToggleShow;

                getView().setLayoutParams(params);
                getView().invalidate();
            }
        }

    }

    // to save and restore the state
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        Gson gson = new GsonBuilder().serializeNulls().create();

        savedInstanceState.putInt("meal_time", meal.getMealTime());
        ArrayList<CorrectiveSimple> simples = new ArrayList<>();
        ArrayList<CorrectiveComplex> complexes = new ArrayList<>();

        for(int i = 0; i < glCorrectives.getChildCount(); i++){
            if(glCorrectives.getChildAt(i) instanceof CompoundCorrectiveView){
                if(((CompoundCorrectiveView) glCorrectives.getChildAt(i)).getCorrective().getType().equals(C.CORRECTIVE_TYPE_SIMPLE)){
                    simples.add((CorrectiveSimple) ((CompoundCorrectiveView) glCorrectives.getChildAt(i)).getCorrective());
                } else {
                    complexes.add((CorrectiveComplex) ((CompoundCorrectiveView) glCorrectives.getChildAt(i)).getCorrective());
                }

            }
        }
        savedInstanceState.putString("correctives_simple", gson.toJson(simples));
        savedInstanceState.putString("correctives_complex", gson.toJson(complexes));


    }



    private void restoreInstanceState(Bundle savedInstanceState){

        if(meal != null) {
            // Cafydia objects
            if (savedInstanceState != null) {
                Gson gson = new GsonBuilder().serializeNulls().create();

                // correctives
                correctivesVisibleOrActive = new ArrayList<>();
                ArrayList<CorrectiveSimple> simples = gson.fromJson(savedInstanceState.getString("correctives_simple"), C.TYPE_TOKEN_TYPE_ARRAY_LIST_CORRECTIVE_SIMPLE);
                ArrayList<CorrectiveComplex> complexes = gson.fromJson(savedInstanceState.getString("correctives_complex"), C.TYPE_TOKEN_TYPE_ARRAY_LIST_CORRECTIVE_COMPLEX);
                correctivesVisibleOrActive.addAll(simples);
                correctivesVisibleOrActive.addAll(complexes);
                Collections.sort(correctivesVisibleOrActive, new CorrectiveComparator());

                buildCorrectiveViewGroup();

            } else {
                reloadCorrectives(meal);
            }
        }
    }

    public void refreshLastGlucoseTestAdded(){
        lastGlucoseTest = new DataDatabase(getActivity()).getLastGlucoseTestAdded();
    }

    private void buildCorrectiveViewGroup(){
        glCorrectives.removeAllViewsInLayout();
        for(Corrective c : correctivesVisibleOrActive){
            CompoundCorrectiveView correctiveView = new CompoundCorrectiveView(getActivity());
            correctiveView.setCorrective(c);
            correctiveView.setMeal(meal);
            correctiveView.registerActivityForCallBackMethod(getActivity());
            glCorrectives.addView(correctiveView);
        }

    }

    private class CorrectiveComparator implements Comparator<Corrective> {
        public int compare(Corrective left, Corrective right) {
            return left.getName().compareTo(right.getName());
        }
    }

    public void reloadCorrectives(Meal m){
        if(m != null) {
            meal = m;

        }

        ArrayList<Corrective> correctives = framework.getConfigDatabase().getCorrectivesSorted(framework.getEnabledMetabolicRhythm().getId());

        Collections.sort(correctives, new CorrectiveComparator());

        correctivesVisibleOrActive = new ArrayList<>();

        for (Corrective c : correctives) {
            if (c.applies(meal) || c.getVisible().equals(C.CORRECTIVE_VISIBLE_YES)) {
                correctivesVisibleOrActive.add(c);
            }
        }

        buildCorrectiveViewGroup();
    }


    private void searchViews(View layout) {
        // baseline
        baselineChart = (BaselineChartView) layout.findViewById(R.id.baselineChart);

        // metabolic rhythms
        preprandialBeginning = (ModificationStartView) layout.findViewById(R.id.preprandialBeginning);
        basalBeginning = (ModificationStartView) layout.findViewById(R.id.basalBeginning);

        // correctives
        glCorrectives = (GridLayout) layout.findViewById(R.id.glCorrectives);

        // individual sections detail
        tvPreprandialBaseline = (TextView) layout.findViewById(R.id.tvPreprandialBaseline);
        tvBasalBaseline = (TextView) layout.findViewById(R.id.tvBasalBaseline);
        tvPreprandialBeginning = (TextView) layout.findViewById(R.id.tvPreprandialBeginning);
        tvBasalBeginning = (TextView) layout.findViewById(R.id.tvBasalBeginning);
        tvPreprandialCorrectives = (TextView) layout.findViewById(R.id.tvPreprandialCorrectives);
        tvPreprandialCorrectionFactor = (TextView) layout.findViewById(R.id.tvPreprandialCorrectionFactor);

        tvFactorTarget = (TextView) layout.findViewById(R.id.tvFactorTarget);
        tvFactorCurrent = (TextView) layout.findViewById(R.id.tvFactorCurrent);
        tvFactor = (TextView) layout.findViewById(R.id.tvFactor);
        tvFactorModification = (TextView) layout.findViewById(R.id.tvFactorModification);

        // basal titles
        baselineBasalText = (TextView) layout.findViewById(R.id.baselineBasalText);
        beginningsBasalText = (TextView) layout.findViewById(R.id.beginningsBasalText);

        // layouts
        lBeginnings = (LinearLayout) layout.findViewById(R.id.lBeginnings);
        lCorrectives = (RelativeLayout) layout.findViewById(R.id.lCorrectives);
        lCorrectionFactor = (LinearLayout) layout.findViewById(R.id.lCorrectionFactor);
    }


    /////////////////////////////////////////////////////////////////////////////////////
    // Para pillar los correctivos y para actualizar el fragment con el meal configurado.
    /////////////////////////////////////////////////////////////////////////////////////

    public ArrayList<Corrective> getCorrectivesEnabled(){
        ArrayList<Corrective> cEnabled = new ArrayList<>();

        if(correctivesVisibleOrActive != null) {
            for (Corrective c : correctivesVisibleOrActive) {
                if (c.getTemporalState()) {
                    cEnabled.add(c);
                }
            }
        }

        return cEnabled;
    }

    public void refreshUIWithMeal(Meal meal) {
        refreshAverages();

        if(framework != null && meal != null && baselineChart != null && mAverages != null) {

            baselineChart.setAverageCarbohydrates(averageCarbohydrates);
        }

        if(meal != null && baselineChart != null && preprandialBeginning != null && basalBeginning != null && getActivity() != null) {

            baselineChart.setMainLineParameters(functions.getMByMeal(meal.getMealTime()), functions.getBByMeal(meal.getMealTime()));
            baselineChart.setMealToDraw(meal);
            baselineChart.setAverageCarbohydrates(averageCarbohydrates);
            baselineChart.refresh();

            Instant startDate = new Instant(framework.getState().getActivatedMetabolicRhythmStartDate()).setTimeToTheStartOfTheDay();

            preprandialBeginning.setMetabolicRhythmStartType(framework.getEnabledMetabolicRhythm().getStartingPreprandialType());
            preprandialBeginning.setMetabolicStateStartType(framework.getState().getStartingPreprandialType());
            preprandialBeginning.setStartDate(startDate);
            preprandialBeginning.setDaysPassed(new Instant().setTimeToTheStartOfTheDay().getDaysPassedFromInstant(startDate));

            // si la basal no está activada, no muestra la modificación del ritmo metabólico
            if(!framework.getBaselineBasal().isBasalActivated(meal.getMealTime())) {
                basalBeginning.setVisibility(View.GONE);
            } else {
                basalBeginning.setVisibility(View.VISIBLE);

                basalBeginning.setMetabolicRhythmStartType(C.STARTING_TYPE_SPECIFIC);
                basalBeginning.setMetabolicStateStartType(C.STARTING_TYPE_SPECIFIC);

                basalBeginning.setStartDate(startDate);
                basalBeginning.setDaysPassed(new Instant().setTimeToTheStartOfTheDay().getDaysPassedFromInstant(startDate));
            }



            switch(meal.getMealTime()){
                case C.MEAL_BREAKFAST:
                    preprandialBeginning.setStart(framework.getEnabledPreprandialStartForBreakfast());
                    basalBeginning.setStart(framework.getEnabledBasalStartForBreakfast());
                    break;
                case C.MEAL_LUNCH:
                    preprandialBeginning.setStart(framework.getEnabledPreprandialStartForLunch());
                    basalBeginning.setStart(framework.getEnabledBasalStartForLunch());
                    break;
                case C.MEAL_DINNER:
                    preprandialBeginning.setStart(framework.getEnabledPreprandialStartForDinner());
                    basalBeginning.setStart(framework.getEnabledBasalStartForDinner());
                    break;
            }


            // baseline
            tvPreprandialBaseline.setText((meal.getBaselinePreprandial() > 0.0f ? "+" : "") + MyRound.round(meal.getBaselinePreprandial()).toString());
            if (meal.getBaselinePreprandial() > 0.0f) {
                tvPreprandialBaseline.setTextColor(getResources().getColor(R.color.colorValueAppliedMealMenu));
                tvPreprandialBaseline.setTypeface(null, Typeface.BOLD);
            } else {
                tvPreprandialBaseline.setTypeface(null, Typeface.NORMAL);
                tvPreprandialBaseline.setTextColor(Color.parseColor("#EE444444")); // provisional
            }

            tvBasalBaseline.setText((meal.getBaselineBasal() > 0.0f ? "+" : "") + MyRound.round(meal.getBaselineBasal()).toString());
            if (meal.getBaselineBasal() > 0.0f) {
                tvBasalBaseline.setTextColor(getResources().getColor(R.color.colorValueAppliedMealMenu));
                tvBasalBaseline.setTypeface(null, Typeface.BOLD);
            } else {
                tvBasalBaseline.setTypeface(null, Typeface.NORMAL);
                tvBasalBaseline.setTextColor(Color.parseColor("#EE444444")); // provisional
            }

            // ocultamos lo basal si no está activado
            if(!framework.getBaselineBasal().isBasalActivated(meal.getMealTime())) {
                tvBasalBaseline.setVisibility(View.GONE);
                baselineBasalText.setVisibility(View.GONE);
            } else {
                tvBasalBaseline.setVisibility(View.VISIBLE);
                baselineBasalText.setVisibility(View.VISIBLE);
            }

            // Metabolic Rhythm
            tvPreprandialBeginning.setText((meal.getBeginningPreprandial() > 0.0f ? "+" : "") + MyRound.round(meal.getBeginningPreprandial()).toString());
            if (meal.getBeginningPreprandial() != 0.0f) {
                tvPreprandialBeginning.setTextColor(getResources().getColor(R.color.colorValueAppliedMealMenu));
                tvPreprandialBeginning.setTypeface(null, Typeface.BOLD);
            } else {
                tvPreprandialBeginning.setTypeface(null, Typeface.NORMAL);
                tvPreprandialBeginning.setTextColor(Color.parseColor("#EE444444")); // provisional
            }

            tvBasalBeginning.setText((meal.getBeginningBasal() > 0.0f ? "+" : "") + MyRound.round(meal.getBeginningBasal()).toString());
            if (meal.getBeginningBasal() != 0.0f) {
                tvBasalBeginning.setTextColor(getResources().getColor(R.color.colorValueAppliedMealMenu));
                tvBasalBeginning.setTypeface(null, Typeface.BOLD);
            } else {
                tvBasalBeginning.setTypeface(null, Typeface.NORMAL);
                tvBasalBeginning.setTextColor(Color.parseColor("#EE444444")); // provisional
            }

            // ocultamos lo basal si no está activado
            if(!framework.getBaselineBasal().isBasalActivated(meal.getMealTime())) {
                beginningsBasalText.setVisibility(View.GONE);
                tvBasalBeginning.setVisibility(View.GONE);
            } else {
                beginningsBasalText.setVisibility(View.VISIBLE);
                tvBasalBeginning.setVisibility(View.VISIBLE);
            }

            // to hide or show metabolic layout
            if (hideSectionsWithNoModification && meal.getBeginningBasal() == 0.0f && meal.getBeginningPreprandial() == 0.0f) {
                lBeginnings.setVisibility(View.GONE);
            } else {
                lBeginnings.setVisibility(View.VISIBLE);
            }


            // correctives
            tvPreprandialCorrectives.setText((meal.getCorrectivesPreprandial() > 0.0f ? "+" : "") + MyRound.round(meal.getCorrectivesPreprandial()).toString());
            if (meal.getCorrectivesPreprandial() != 0.0f) {
                tvPreprandialCorrectives.setTextColor(getResources().getColor(R.color.colorValueAppliedMealMenu));
                tvPreprandialCorrectives.setTypeface(null, Typeface.BOLD);
            } else {
                tvPreprandialCorrectives.setTypeface(null, Typeface.NORMAL);
                tvPreprandialCorrectives.setTextColor(Color.parseColor("#EE444444")); // provisional
            }

            // to hide or show correctives layout
            if (correctivesVisibleOrActive != null && correctivesVisibleOrActive.size() == 0 && hideSectionsWithNoModification) {
                lCorrectives.setVisibility(View.GONE);
            } else {
                lCorrectives.setVisibility(View.VISIBLE);
            }

            /*
             * Correction Factor Modification
             */
            tvPreprandialCorrectionFactor.setText(meal.getCorrectionFactorPreprandial() != null ? (meal.getCorrectionFactorPreprandial() > 0.0f ? "+" : "") + MyRound.round(meal.getCorrectionFactorPreprandial()).toString() : "+0.0");

            if (meal.getCorrectionFactorPreprandial() != null && meal.getCorrectionFactorPreprandial() != 0.0f) {
                tvPreprandialCorrectionFactor.setTextColor(getResources().getColor(R.color.colorValueAppliedMealMenu));
                tvPreprandialCorrectionFactor.setTypeface(null, Typeface.BOLD);
                lCorrectionFactor.setVisibility(View.VISIBLE);
            } else {
                tvPreprandialCorrectionFactor.setTypeface(null, Typeface.NORMAL);
                tvPreprandialCorrectionFactor.setTextColor(Color.parseColor("#EE444444")); // provisional
                if (hideSectionsWithNoModification) {
                    lCorrectionFactor.setVisibility(View.GONE);
                }
            }

            if (meal.getCorrectionFactorPreprandial() != null && meal.getCorrectionFactorPreprandial() != 0.0f) {
                if(lastGlucoseTest.getGlucoseLevel() > correctionFactorAboveValue) {
                    tvFactorTarget.setText(MyRound.round(changer.toUIFromInternalGlucose(correctionFactorAboveValue)).toString() + changer.getStringUnitForGlucose());
                }
                else if(lastGlucoseTest.getGlucoseLevel() < correctionFactorBelowValue) {
                    tvFactorTarget.setText(MyRound.round(changer.toUIFromInternalGlucose(correctionFactorBelowValue)).toString() + changer.getStringUnitForGlucose());
                }
                tvFactorCurrent.setText(MyRound.round(changer.toUIFromInternalGlucose(lastGlucoseTest.getGlucoseLevel().floatValue())).toString() + changer.getStringUnitForGlucose());
                tvFactor.setText(framework.getCorrectionFactor().getCorrectionFactor().toString());

                if(meal.getCorrectionFactorPreprandial() > 0) {
                    tvFactorModification.setText("+" + meal.getCorrectionFactorPreprandial().toString());
                } else {
                    tvFactorModification.setText(meal.getCorrectionFactorPreprandial().toString());
                }
            }
        }

    }

        /*
     * ANIMATIONS TO OPEN AND CLOSE FOOD PANEL
     */

    public void showPanel(){
        if(!mPanelVisible && mIvToggleShow != null) {
            mPanelVisible = true;

            ActionBar actionBar = getActivity().getActionBar();
            if(actionBar != null) {
                mActionBarOldTitle = actionBar.getTitle().toString();
                actionBar.setTitle(getString(R.string.recommendation_details_fragment_actionbar_title));

            }

            Animation showAnimation = new Animation() {

                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    super.applyTransformation(interpolatedTime, t);
                    RelativeLayout.LayoutParams params = getView() != null ? (RelativeLayout.LayoutParams) getView().getLayoutParams() : null;
                    if (params != null) {
                        params.leftMargin = (int) ((mWidthFragment - mWidthIvToggleShow) * (1.0 - interpolatedTime));
                        params.rightMargin = (int) ((-mWidthFragment + mWidthIvToggleShow) * (1.0 - interpolatedTime));

                        getView().setLayoutParams(params);
                        getView().bringToFront();
                    }
                }
            };

            RelativeLayout.LayoutParams params = getView() != null ? (RelativeLayout.LayoutParams) getView().getLayoutParams() : null;
            if (params != null) {
                showAnimation.setDuration(TIME_ANIMATION);

                getView().startAnimation(showAnimation);
            }
        }
    }

    public void hidePanel(){
        if(mPanelVisible && mIvToggleShow != null) {
            mPanelVisible = false;

            ActionBar actionBar = getActivity().getActionBar();
            if(actionBar != null && mActionBarOldTitle != null && !mActionBarOldTitle.equals("")) {
                actionBar.setTitle(mActionBarOldTitle);
            }

            Animation hideAnimation = new Animation() {
                int ivWidth = mIvToggleShow.getWidth();

                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    super.applyTransformation(interpolatedTime, t);

                    RelativeLayout.LayoutParams params = getView() != null ? (RelativeLayout.LayoutParams) getView().getLayoutParams() : null;
                    if (params != null) {


                        params.leftMargin = (int) ((mWidthFragment - mWidthIvToggleShow) * interpolatedTime);
                        params.rightMargin = (int) ((-mWidthFragment + mWidthIvToggleShow) * interpolatedTime);

                        getView().setLayoutParams(params);
                    }
                }
            };

            RelativeLayout.LayoutParams params = getView() != null ? (RelativeLayout.LayoutParams) getView().getLayoutParams() : null;
            if (params != null) {
                hideAnimation.setDuration(TIME_ANIMATION);
                getView().startAnimation(hideAnimation);
            }
        }
    }

    public boolean isPanelVisible() {
        return mPanelVisible;
    }

    public interface RecommendationDetailsInterface {
        void onRequestedHideRecommendationDetailsPanel();
        void onRequestedShowRecommendationDetailsPanel();
    }

}
