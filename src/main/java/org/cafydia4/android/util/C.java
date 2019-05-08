package org.cafydia4.android.util;


import com.google.gson.reflect.TypeToken;

import org.cafydia4.android.chartobjects.DataCollectionLabelRule;
import org.cafydia4.android.core.Food;
import org.cafydia4.android.core.FoodBundle;
import org.cafydia4.android.core.Meal;
import org.cafydia4.android.recommendations.CorrectiveComplex;
import org.cafydia4.android.recommendations.CorrectiveSimple;
import org.cafydia4.android.recommendations.ModificationStart;
import org.cafydia4.android.recommendations.ModificationStartDot;

import java.lang.reflect.Type;
import java.util.ArrayList;

/*
 * App constants
 */
public class C {
    public static final String[] MEAL_STRING = {
            "breakfast",
            "lunch",
            "dinner"
    };

    public static final float ADJUST_GLUCOSE_MAX_LEVEL = 140f;

    public static final String PREPRANDIAL_FITTER_STATE_NOT_GENERATED_NOT_VERIFIED = "0";
    public static final String PREPRANDIAL_FITTER_STATE_GENERATING_NOT_VERIFIED = "1";
    public static final String PREPRANDIAL_FITTER_STATE_GENERATED_NOT_VERIFIED = "2";
    public static final String PREPRANDIAL_FITTER_STATE_GENERATED_VERIFYING = "3";
    public static final String PREPRANDIAL_FITTER_STATE_GENERATED_VERIFIED = "4";

    public static final String PREPRANDIAL_FITTER_MODE_AUTOMATIC = "auto";
    public static final String PREPRANDIAL_FITTER_MODE_FORCED = "forced";
    public static final String PREPRANDIAL_FITTER_MODE_REQUESTED = "requested";
    public static final String PREPRANDIAL_FITTER_MODE_DISABLED = "disabled";

    public static final int MEAL_BREAKFAST = 0;
    public static final int MEAL_LUNCH = 1;
    public static final int MEAL_DINNER = 2;

    public static final int SNACK_AFTER_BREAKFAST = 0;
    public static final int SNACK_AFTER_LUNCH = 1;
    public static final int SNACK_BEFORE_BED = 2;

    public static final int DAY_WEEK_MONDAY = 0;
    public static final int DAY_WEEK_TUESDAY = 1;
    public static final int DAY_WEEK_WEDNESDAY = 2;
    public static final int DAY_WEEK_THURSDAY = 3;
    public static final int DAY_WEEK_FRIDAY = 4;
    public static final int DAY_WEEK_SATURDAY = 5;
    public static final int DAY_WEEK_SUNDAY = 6;

    public static final int DOT_TYPE_PREPRANDIAL_INSULIN_GLOBAL = 0;
    public static final int DOT_TYPE_PREPRANDIAL_INSULIN_BREAKFAST = 1;
    public static final int DOT_TYPE_PREPRANDIAL_INSULIN_LUNCH = 2;
    public static final int DOT_TYPE_PREPRANDIAL_INSULIN_DINNER = 3;

    public static final int STARTING_TYPE_GLOBAL = 0;
    public static final int STARTING_TYPE_SPECIFIC = 1;

    public static final int DOT_TYPE_BASAL_INSULIN_BREAKFAST = 10;
    public static final int DOT_TYPE_BASAL_INSULIN_LUNCH = 11;
    public static final int DOT_TYPE_BASAL_INSULIN_DINNER = 12;

    public static final int CORRECTIVE_VISIBLE_YES = 1;
    public static final int CORRECTIVE_VISIBLE_NO = 0;
    public static final int CORRECTIVE_TYPE_SIMPLE = 0;
    public static final int CORRECTIVE_TYPE_COMPLEX = 1;
    public static final int CORRECTIVE_MODIFICATION_TYPE_NUMBER = 0;
    public static final int CORRECTIVE_MODIFICATION_TYPE_PERCENTAGE = 1;

    public static final int GLUCOSE_TEST_BEFORE_BREAKFAST = 0;
    public static final int GLUCOSE_TEST_AFTER_BREAKFAST = 1;
    public static final int GLUCOSE_TEST_BEFORE_LUNCH = 2;
    public static final int GLUCOSE_TEST_AFTER_LUNCH = 3;
    public static final int GLUCOSE_TEST_BEFORE_DINNER = 4;
    public static final int GLUCOSE_TEST_AFTER_DINNER = 5;
    public static final int GLUCOSE_TEST_IN_THE_NIGHT = 6;

    public static final int GLUCOSE_TEST_BEFORE = 0;
    public static final int GLUCOSE_TEST_AFTER = 1;
    public static final int GLUCOSE_TEST_NIGHT = 2;

    public static final int METABOLIC_RHYTHM_STATE_ENABLED = 1;
    public static final int METABOLIC_RHYTHM_STATE_DISABLED = 0;

    public static final int LINEAR_FUNCTIONS_MODIFICATION_BY_ITS_INDEX = 0;
    public static final int LINEAR_FUNCTIONS_MODIFICATION_BY_THE_LEFT = 1;
    public static final int LINEAR_FUNCTIONS_MODIFICATION_BY_THE_RIGHT = 2;

    public static final int FOOD_FAVORITE_YES = 1;
    public static final int FOOD_FAVORITE_NO = 0;

    public static final int FOOD_TYPE_COMPLEX = 1;
    public static final int FOOD_TYPE_SIMPLE = 0;

    public static final int FOR_CORRECTION_FACTOR_INSULIN_SYNTHETIC = 1800;

    public static final float HBA1C_TOP_VERY_GOOD = 6.2f;
    public static final float HBA1C_TOP_GOOD = 7.0f;
    public static final float HBA1C_TOP_REGULAR = 8.5f;
    public static final float HBA1C_TOP_BAD = 10f;

    public static final int FOOD_FRAGMENT_POSITION_NONE = -1;
    public static final int FOOD_FRAGMENT_POSITION_FOOD = 0;
    public static final int FOOD_FRAGMENT_POSITION_FAVORITE_FOOD = 1;
    public static final int FOOD_FRAGMENT_POSITION_COMPLEX_FOOD = 2;
    public static final int FOOD_FRAGMENT_POSITION_SEARCH = 3;

    public static final int FOOD_FRAGMENT_FRAME_LAYOUT_NONE = -1;
    public static final int FOOD_FRAGMENT_FRAME_LAYOUT_NO_FOOD_ADDED = 0;
    public static final int FOOD_FRAGMENT_FRAME_LAYOUT_NO_RESULTS = 1;
    public static final int FOOD_FRAGMENT_FRAME_LAYOUT_SEARCHING = 2;
    public static final int FOOD_FRAGMENT_FRAME_LAYOUT_NO_SEARCH_MADE = 3;
    public static final int FOOD_FRAGMENT_FRAME_LAYOUT_NO_INTERNET_CONNECTION = 4;
    public static final int FOOD_FRAGMENT_FRAME_LAYOUT_BEDCA_DOWN = 5;

    public static final int FOOD_ACTION_TYPE_FAVORITE_YES = 0;
    public static final int FOOD_ACTION_TYPE_FAVORITE_NO = 1;
    public static final int FOOD_ACTION_TYPE_DELETE = 2;
    public static final int FOOD_ACTION_TYPE_ADD_FOOD = 3;
    public static final int FOOD_ACTION_TYPE_EDIT_FOOD = 4;

    public static final int FOOD_SELECTED_ACTION_TYPE_EDIT_SELECTION = 0;
    public static final int FOOD_SELECTED_ACTION_TYPE_REMOVE_SELECTION = 1;

    public static final int PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS = 300;

    public static final int PREFERENCE_WEIGHT_GRAMS = 0;
    public static final int PREFERENCE_WEIGHT_POUNDS = 1;
    public static final int PREFERENCE_WEIGHT_OUNCES = 2;

    public static final int PREFERENCE_GLUCOSE_MGDL = 0;
    public static final int PREFERENCE_GLUCOSE_MMOLL = 1;

    //
    // METABOLIC RHYTHM ACTIVITY AND FRAGMENTS
    //
    public static final int METABOLIC_ACTIVITY_STATE_LIST = 0;
    public static final int METABOLIC_ACTIVITY_STATE_DETAILS = 1;
    public static final int METABOLIC_ACTIVITY_STATE_BEGINNINGS_LIST = 2;
    public static final int METABOLIC_ACTIVITY_STATE_CORRECTIVES_LIST = 3;
    public static final int METABOLIC_ACTIVITY_STATE_BEGINNINGS_DETAILS_PREPRANDIAL = 4;
    public static final int METABOLIC_ACTIVITY_STATE_BEGINNINGS_DETAILS_BASAL = 5;
    public static final int METABOLIC_ACTIVITY_STATE_CORRECTIVES_DETAILS = 6;

    public static final int METABOLIC_DETAILS_GO_TO_BEGINNINGS = 0;
    public static final int METABOLIC_DETAILS_GO_TO_CORRECTIVES = 1;

    public static final int METABOLIC_BEGINNINGS_LIST_ELEMENT_CLICKED_PREPRANDIAL = 0;
    public static final int METABOLIC_BEGINNINGS_LIST_ELEMENT_CLICKED_BASAL = 1;

    public static final int CORRECTIVES_LIST = 0;
    public static final int CORRECTIVES_DETAILS = 1;


    public static final int DIALOG_DOT_SELECTOR_ACTION_EDIT = 0;
    public static final int DIALOG_DOT_SELECTOR_ACTION_DELETE = 1;

    public static final int METABOLIC_FRAMEWORK_ERROR_ALL_OK = 0;
    public static final int METABOLIC_FRAMEWORK_ERROR_METABOLIC_RHYTHM_PLANED = 1;
    //public static final int METABOLIC_FRAMEWORK_ERROR_METABOLIC_FRAMEWORK_BUSY = 2;

    public static final int PAGER_SCOPE_HBA1C_POSITION = 0;

    public static final int ANNOTATION_SCOPE_GLOBAL = -1;
    public static final int ANNOTATION_SCOPE_HBA1C = 0;

    public static final int ANNOTATION_CREATED_BY_CAFYDIA = 0;
    public static final int ANNOTATION_CREATED_BY_USER = 1;

    public static final int CHART_ACTIVITY_ELEMENT_PAGE = 0;
    public static final int CHART_ACTIVITY_ELEMENT_TEXT = 1;
    public static final int CHART_ACTIVITY_ELEMENT_CHART = 2;
    public static final int CHART_ACTIVITY_ELEMENT_ANNOTATION = 3;
    public static final int CHART_ACTIVITY_ELEMENT_LABEL = 4;

    // en consonancia con el String-Array chart_types de arrays.xml + 1
    public static final int CHART_PAGE_ELEMENT_TYPE_TEXT = 0;
    public static final int CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS = 1;
    public static final int CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS = 2;
    public static final int CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES = 3;
    public static final int CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE = 4;
    public static final int CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_MEAL_HOUR = 5;
    //public static final int CHART_PAGE_ELEMENT_TYPE_GENERAL_STATS = 6;

    public static final int DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE = 0; // -5
    public static final int DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ABSOLUTE = 1; // long
    public static final int DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION = 2; // annotation id from annotations table

    public static final int DATA_COLLECTION_CRITERIA_NO_ACTIVATED = 0;
    public static final int DATA_COLLECTION_CRITERIA_ACTIVATED = 1;

    public static final int LABEL_RULE_ACTION_EXCLUDE = 0;
    public static final int LABEL_RULE_ACTION_INCLUDE = 1;

    public static final int LABEL_TEXT_COLOR_BLACK = 1;

    public static final int STATISTICAL_GRID = 0b100000000;
    public static final int STATISTICAL_GLUCOSE_TESTS = 0b010000000;
    public static final int STATISTICAL_MINIMUM = 0b001000000;
    public static final int STATISTICAL_MAXIMUM = 0b000100000;
    public static final int STATISTICAL_MEAN = 0b000010000;
    public static final int STATISTICAL_MEDIAN = 0b000001000;
    public static final int STATISTICAL_LINEAR_REGRESSION = 0b000000100;
    public static final int STATISTICAL_POLYNOMIAL_REGRESSION_GRADE2 = 0b000000010;
    public static final int STATISTICAL_POLYNOMIAL_REGRESSION_GRADE3 = 0b000000001;


    public static final Type TYPE_TOKEN_TYPE_FOOD_BUNDLE = new TypeToken<FoodBundle>() {}.getType();
    public static final Type TYPE_TOKEN_TYPE_ARRAY_LIST_FOOD = new TypeToken<ArrayList<Food>>() {}.getType();
    public static final Type TYPE_TOKEN_TYPE_FOOD = new TypeToken<Food>() {}.getType();
    public static final Type TYPE_TOKEN_TYPE_MODIFICATION_START_DOT = new TypeToken<ModificationStartDot>() {}.getType();
    public static final Type TYPE_TOKEN_TYPE_MODIFICATION_START = new TypeToken<ModificationStart>() {}.getType();
    public static final Type TYPE_TOKEN_TYPE_MEAL = new TypeToken<Meal>() {}.getType();

    public static final Type TYPE_TOKEN_TYPE_ARRAY_LIST_CORRECTIVE_SIMPLE = new TypeToken<ArrayList<CorrectiveSimple>>() {}.getType();
    public static final Type TYPE_TOKEN_TYPE_ARRAY_LIST_CORRECTIVE_COMPLEX = new TypeToken<ArrayList<CorrectiveComplex>>() {}.getType();
    public static final Type TYPE_TOKEN_TYPE_ARRAY_LIST_DATA_COLLECTION_LABEL_RULE = new TypeToken<ArrayList<DataCollectionLabelRule>>() {}.getType();


}
