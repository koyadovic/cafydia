package org.cafydia4.android.tutorial;

import android.content.Context;
import android.content.SharedPreferences;

import org.cafydia4.android.R;

/**
 * Created by user on 6/05/15.
 */
public class Tutorial {

    public static class Main {
        public static void aboutApp(HelpFragmentBundle helpBundle){
            helpBundle.addHelpFragment("main_easy_1", R.layout.help_fragment_easy_main_1);
            //helpBundle.addHelpFragment("main_easy_2", R.layout.help_fragment_easy_main_2);
            helpBundle.addHelpFragment("main_easy_6", R.layout.help_fragment_easy_main_6);
            helpBundle.start(mHelpFragmentBundleListener);
        }
    }

    //
    // Meals and Snacks Activity
    //
    public static class MealsSnacks {
        public static void aboutMeal(HelpFragmentBundle helpBundle){
            helpBundle.addHelpFragment("meals_snacks_easy_1", R.layout.help_fragment_easy_meals_1);
            helpBundle.addHelpFragment("meals_snacks_easy_2", R.layout.help_fragment_easy_meals_2);
            helpBundle.addHelpFragment("meals_snacks_easy_3", R.layout.help_fragment_easy_meals_3);
            helpBundle.addHelpFragment("meals_snacks_easy_3_save_meal", R.layout.help_fragment_easy_meals_3_save_meal);
            helpBundle.start(mHelpFragmentBundleListener);
        }

        public static void aboutSnack(HelpFragmentBundle helpBundle){
            helpBundle.addHelpFragment("meals_snacks_easy_1", R.layout.help_fragment_easy_meals_1);
            helpBundle.addHelpFragment("meals_snacks_easy_4", R.layout.help_fragment_easy_meals_4);
            helpBundle.addHelpFragment("meals_snacks_easy_5", R.layout.help_fragment_easy_meals_5);
            helpBundle.addHelpFragment("meals_snacks_easy_6_snacks_1", R.layout.help_fragment_easy_meals_6_snacks_1);
            helpBundle.addHelpFragment("meals_snacks_easy_6_snacks_2", R.layout.help_fragment_easy_meals_6_snacks_2);
            helpBundle.addHelpFragment("meals_snacks_easy_6_snacks_3", R.layout.help_fragment_easy_meals_6_snacks_3);
            helpBundle.start(mHelpFragmentBundleListener);

        }

        public static void aboutFoodPanel(HelpFragmentBundle helpBundle){
            helpBundle.addHelpFragment("meals_snacks_food_panel_5", R.layout.help_fragment_easy_food_panel_5);
            helpBundle.addHelpFragment("meals_snacks_food_panel_6", R.layout.help_fragment_easy_food_panel_6);
            helpBundle.addHelpFragment("meals_snacks_food_panel_7", R.layout.help_fragment_easy_food_panel_7);
            helpBundle.start(mHelpFragmentBundleListener);
        }

        public static void aboutRecommendationDetailsPanel(HelpFragmentBundle helpBundle){
            helpBundle.addHelpFragment("recommendations_easy_1", R.layout.help_fragment_easy_recommendations_1);
            helpBundle.addHelpFragment("recommendations_easy_2", R.layout.help_fragment_easy_recommendations_2);
            helpBundle.addHelpFragment("recommendations_easy_3", R.layout.help_fragment_easy_recommendations_3);
            helpBundle.addHelpFragment("recommendations_easy_4", R.layout.help_fragment_easy_recommendations_4);
            helpBundle.addHelpFragment("recommendations_easy_5", R.layout.help_fragment_easy_recommendations_5);
            helpBundle.addHelpFragment("recommendations_easy_6", R.layout.help_fragment_easy_recommendations_6);
            helpBundle.addHelpFragment("meals_snacks_recommendation_panel_1", R.layout.help_fragment_easy_recommendation_panel_1);
            helpBundle.start(mHelpFragmentBundleListener);
        }
    }


    //
    // Baseline Activity
    //
    public static class Baseline {
        public static void aboutBaseline(HelpFragmentBundle helpBundle){
            helpBundle.addHelpFragment("recommendations_easy_1", R.layout.help_fragment_easy_recommendations_1);
            helpBundle.addHelpFragment("recommendations_easy_2", R.layout.help_fragment_easy_recommendations_2);
            helpBundle.addHelpFragment("recommendations_easy_3", R.layout.help_fragment_easy_recommendations_3);
            helpBundle.addHelpFragment("recommendations_easy_4", R.layout.help_fragment_easy_recommendations_4);
            helpBundle.addHelpFragment("recommendations_easy_5", R.layout.help_fragment_easy_recommendations_5);
            helpBundle.addHelpFragment("recommendations_easy_6", R.layout.help_fragment_easy_recommendations_6);
            helpBundle.addHelpFragment("baseline_easy_1", R.layout.help_fragment_easy_baseline_1);
            helpBundle.addHelpFragment("baseline_easy_2", R.layout.help_fragment_easy_baseline_2);
            helpBundle.start(mHelpFragmentBundleListener);
        }

        public static void aboutModificationZones(HelpFragmentBundle helpBundle) {
            helpBundle.addHelpFragment("baseline_easy_3", R.layout.help_fragment_easy_baseline_3);
            helpBundle.start(mHelpFragmentBundleListener);
        }
    }

    //
    // Metabolic Rhythms Activity
    //
    public static class MetabolicRhythms {
        public static void aboutMetabolicRhythmsList(HelpFragmentBundle helpBundle){
            helpBundle.addHelpFragment("metabolic_rhythms_easy_1", R.layout.help_fragment_easy_metabolic_rhythms_1);
            helpBundle.addHelpFragment("metabolic_rhythms_easy_1_more_more", R.layout.help_fragment_easy_metabolic_rhythms_1_more_more);
            helpBundle.start(mHelpFragmentBundleListener);
        }

        public static void aboutMetabolicRhythmsDetails(HelpFragmentBundle helpBundle){
            helpBundle.addHelpFragment("metabolic_rhythms_easy_1", R.layout.help_fragment_easy_metabolic_rhythms_1);
            helpBundle.addHelpFragment("metabolic_rhythms_easy_1_more_more", R.layout.help_fragment_easy_metabolic_rhythms_1_more_more);
            helpBundle.addHelpFragment("metabolic_rhythms_easy_2", R.layout.help_fragment_easy_metabolic_rhythms_2);
            helpBundle.addHelpFragment("metabolic_rhythms_easy_3", R.layout.help_fragment_easy_metabolic_rhythms_3);
            helpBundle.start(mHelpFragmentBundleListener);
        }
    }


    //
    // Correctives
    //
    public static class Correctives {
        public static void aboutCorrectivesList(HelpFragmentBundle helpBundle) {
            helpBundle.addHelpFragment("correctives_easy_list_1", R.layout.help_fragment_easy_correctives_list_1);
            helpBundle.addHelpFragment("correctives_easy_list_2", R.layout.help_fragment_easy_correctives_list_2);
            helpBundle.start(mHelpFragmentBundleListener);
        }

        public static void aboutCorrectivesDetails(HelpFragmentBundle helpBundle) {
            helpBundle.start(mHelpFragmentBundleListener);
        }

    }

    //
    // Beginnings
    //
    public static class Beginnings {
        public static void aboutBeginningsList(HelpFragmentBundle helpBundle) {
            helpBundle.start(mHelpFragmentBundleListener);
        }

        public static void aboutBeginningsDetails(HelpFragmentBundle helpBundle) {
            helpBundle.addHelpFragment("beginnings_easy_details_1", R.layout.help_fragment_easy_beginnings_details_1);
            helpBundle.addHelpFragment("beginnings_easy_details_2", R.layout.help_fragment_easy_beginnings_details_2);
            helpBundle.addHelpFragment("beginnings_easy_details_3", R.layout.help_fragment_easy_beginnings_details_3);
            helpBundle.start(mHelpFragmentBundleListener);
        }

    }

    //
    // Build your charts Activity
    //
    public static class BuildYourCharts {
        public static void aboutChartsActivity(HelpFragmentBundle helpBundle) {
            helpBundle.addHelpFragment("charts_general_easy_1", R.layout.help_fragment_easy_charts_general_1);
            helpBundle.addHelpFragment("charts_general_easy_2", R.layout.help_fragment_easy_charts_general_2);
            helpBundle.addHelpFragment("charts_general_easy_4", R.layout.help_fragment_easy_charts_general_4);
            helpBundle.addHelpFragment("charts_general_easy_3", R.layout.help_fragment_easy_charts_general_3);
            helpBundle.start(mHelpFragmentBundleListener);
        }

        public static void aboutAnnotationsAndLabels(HelpFragmentBundle helpBundle) {
            helpBundle.addHelpFragment("charts_panel_easy_1", R.layout.help_fragment_easy_charts_panel_1);
            helpBundle.addHelpFragment("charts_panel_easy_2", R.layout.help_fragment_easy_charts_panel_2);
            helpBundle.start(mHelpFragmentBundleListener);
        }

        public static void aboutNewPageCreated(HelpFragmentBundle helpBundle) {
            helpBundle.addHelpFragment("charts_new_page_created_1", R.layout.help_fragment_easy_charts_new_page_created_1);
            helpBundle.addHelpFragment("charts_new_page_created_2", R.layout.help_fragment_easy_charts_new_page_created_2);
            helpBundle.start(mHelpFragmentBundleListener);
        }

        public static void aboutClickOrLongClickOnChart(HelpFragmentBundle helpBundle) {
            helpBundle.addHelpFragment("charts_click_or_long_click_1", R.layout.help_fragment_easy_charts_click_or_long_click_1);
            helpBundle.addHelpFragment("charts_click_or_long_click_2", R.layout.help_fragment_easy_charts_click_or_long_click_2);
            helpBundle.start(mHelpFragmentBundleListener);
        }


    }

    //
    // Add complex food Activity
    //
    public static class AddComplexFood {
        public static void aboutAddComplexFoodActivity(HelpFragmentBundle helpBundle) {
            helpBundle.addHelpFragment("complex_food_easy_intro_1", R.layout.help_fragment_easy_complex_food_intro_1);
            helpBundle.addHelpFragment("complex_food_easy_intro_2", R.layout.help_fragment_easy_complex_food_intro_2);
            helpBundle.addHelpFragment("complex_food_easy_intro_3", R.layout.help_fragment_easy_complex_food_intro_3);
            helpBundle.addHelpFragment("complex_food_easy_1", R.layout.help_fragment_easy_complex_food_1);
            helpBundle.start(mHelpFragmentBundleListener);
        }


        public static void aboutFoodPanel(HelpFragmentBundle helpBundle){
            helpBundle.addHelpFragment("meals_snacks_food_panel_5", R.layout.help_fragment_easy_food_panel_5);
            helpBundle.addHelpFragment("meals_snacks_food_panel_6", R.layout.help_fragment_easy_food_panel_6);
            helpBundle.addHelpFragment("meals_snacks_food_panel_7", R.layout.help_fragment_easy_food_panel_7);
            helpBundle.start(mHelpFragmentBundleListener);
        }

    }


    //
    // To activate and deactivate the tutorial
    //
    private static final String[] mFragmentTags = new String[] {
            "meals_snacks_easy_1",
            "meals_snacks_easy_2",
            "meals_snacks_easy_3",
            "meals_snacks_easy_3_save_meal",
            "meals_snacks_easy_4",
            "meals_snacks_easy_5",
            "meals_snacks_easy_6_snacks_1",
            "meals_snacks_easy_6_snacks_2",
            "meals_snacks_easy_6_snacks_3",
            "meals_snacks_food_panel_1",
            "meals_snacks_food_panel_2",
            "meals_snacks_food_panel_3",
            "meals_snacks_food_panel_4",
            "meals_snacks_food_panel_5",
            "meals_snacks_food_panel_6",
            "meals_snacks_food_panel_7",
            "meals_snacks_recommendation_panel_1",
            "baseline_easy_1",
            "baseline_easy_2",
            "baseline_easy_3",
            "metabolic_rhythms_easy_1",
            "metabolic_rhythms_easy_1_more_more",
            "metabolic_rhythms_easy_2",
            "metabolic_rhythms_easy_3",
            "correctives_easy_list_1",
            "correctives_easy_list_2",
            "beginnings_easy_details_1",
            "beginnings_easy_details_2",
            "beginnings_easy_details_3",
            "complex_food_easy_intro_1",
            "complex_food_easy_intro_2",
            "complex_food_easy_intro_3",
            "complex_food_easy_1",
            "charts_general_easy_1",
            "charts_general_easy_2",
            "charts_general_easy_3",
            "charts_general_easy_4",
            "charts_panel_easy_1",
            "charts_panel_easy_2",
            "main_easy_1",
            "main_easy_6",
            "charts_new_page_created_1",
            "charts_new_page_created_2",
            "charts_click_or_long_click_1",
            "charts_click_or_long_click_2",
            "recommendations_easy_1",
            "recommendations_easy_2",
            "recommendations_easy_3",
            "recommendations_easy_4",
            "recommendations_easy_5",
            "recommendations_easy_6"

    };

    public static void tutorialOn(Context c){
        setTutorial(c, true);
    }

    public static void tutorialOff(Context c){
        setTutorial(c, false);
    }

    private static void setTutorial(Context c, boolean activated){
        SharedPreferences sp = c.getSharedPreferences(HelpFragment.HELP_FRAGMENTS_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        for(String tag : mFragmentTags) {
            editor.putBoolean(tag, activated);
        }

        editor.apply();
    }


    //
    // The interface
    //

    private static HelpFragmentBundle.HelpFragmentBundleListener mHelpFragmentBundleListener = new HelpFragmentBundle.HelpFragmentBundleListener() {
        @Override
        public void onHelpFragmentBundleFinished() {
            if(mCallback != null) {
                mCallback.onTutorialHelpFragmentBundleFinished();
                mCallback = null;
            }
        }
    };

    private static TutorialListener mCallback = null;

    public static void setTutorialListener(TutorialListener callback) {
        mCallback = callback;
    }

    public interface TutorialListener {
        void onTutorialHelpFragmentBundleFinished();
    }

}
