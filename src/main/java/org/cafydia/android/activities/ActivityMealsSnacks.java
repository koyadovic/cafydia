package org.cafydia.android.activities;

import android.app.ActionBar;
import android.app.NotificationManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.cafydia.android.R;
import org.cafydia.android.adapters.FoodSelectedAdapter;
import org.cafydia.android.core.Food;
import org.cafydia.android.core.FoodBundle;
import org.cafydia.android.core.Instant;
import org.cafydia.android.core.Meal;
import org.cafydia.android.core.Snack;
import org.cafydia.android.dialogfragments.CafydiaAlertDialog;
import org.cafydia.android.dialogfragments.DialogAddGlucose;
import org.cafydia.android.dialogfragments.DialogFoodSelected;
import org.cafydia.android.dialogfragments.DialogMealFinished;
import org.cafydia.android.fragments.BaselineFragment;
import org.cafydia.android.fragments.FoodFragment;
import org.cafydia.android.fragments.RecommendationDetailsFragment;
import org.cafydia.android.recommendations.Corrective;
import org.cafydia.android.recommendations.MetabolicFramework;
import org.cafydia.android.tutorial.HelpFragmentBundle;
import org.cafydia.android.tutorial.Tutorial;
import org.cafydia.android.util.Averages;
import org.cafydia.android.util.C;
import org.cafydia.android.util.HbA1cHelper;
import org.cafydia.android.util.MyRound;
import org.cafydia.android.util.MyToast;
import org.cafydia.android.util.NearestTime;
import org.cafydia.android.util.UnitChanger;
import org.cafydia.android.util.ViewUtil;
import org.cafydia.android.views.CompoundCorrectiveView;
import org.cafydia.android.views.EditTextWeight;

import java.util.ArrayList;

/**
 * Created by miguel on 2/07/14.
 */
public class ActivityMealsSnacks extends FragmentActivity implements
        FoodFragment.FoodFragmentInterface,
        DialogFoodSelected.OnFoodFinallySelectedListener,
        DialogAddGlucose.GlucoseTestAdded,
        RecommendationDetailsFragment.RecommendationDetailsInterface,
        CompoundCorrectiveView.OnSwitchChangeListener {

    // the two panels for food and selected food
    private FoodFragment mFoodPanel;
    private RecommendationDetailsFragment mRecommendationPanel;

    // cafydia objects
    private MetabolicFramework framework;
    private UnitChanger changer;
    private BackupManager mBackupManager;
    private FoodBundle foodBundle;

    // UI objects
    private Spinner spinnerMealOrSnack;
    private int spinnerPosition = 0;
    private LinearLayout lFoodSelected;
    private ListView lvFoodSelected;
    private Switch sMealOrSnack;
    private TextView tvPreprandialRecommendation, tvBasalRecommendation, tvSnackCarbohydratesTotal;


    //private EditText etSnackCarbohydrates;
    private EditTextWeight snackCarbohydratesEditText;

    private LinearLayout lSnackCarbohydratesLayout, lPreprandialLayout, lBasalLayout;
    private FrameLayout lNoFoodSelected;
    private FrameLayout lShadow;
    private FoodSelectedAdapter foodSelectedAdapter;
    private RelativeLayout rlMasterLayout;
    private FrameLayout flLoading;

    // MEAL
    private boolean mAllDataOk = false;
    private Float mMinPreprandialDose = 0.0f;

    private Averages mAverages;

    // SNACK
    public static final String SHARED_PREFERENCES_SNACK_VALUES = "snack_values";
    private Float[] mCarbohydrateValues = new Float[4];

    private HelpFragmentBundle mHelpBundle;

    private Handler mActivityHandler = new Handler();


    private void setOrientation() {
        if (getResources().getBoolean(R.bool.phone)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_meals);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.colorActivityBackground2));

        searchUIElements();
        setListeners();
        restoreInstanceState(savedInstanceState);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();

        ActionBar action = getActionBar();
        if (action != null) {
            action.setDisplayShowTitleEnabled(false);

            View actionBarView = getLayoutInflater().inflate(R.layout.meal_or_snack_actionbar, null);
            action.setCustomView(actionBarView);
            action.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); // | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME

            // Get the action view used in your toggleservice item
            sMealOrSnack = (Switch) actionBarView.findViewById(R.id.sRecommendation);

        }

        mHelpBundle = new HelpFragmentBundle(ActivityMealsSnacks.this, R.id.help);

        rlMasterLayout.setVisibility(View.INVISIBLE);

        mAverages = new Averages(this, new Averages.OnAveragesCalculatedListener() {
            @Override
            public void onAveragesCalculated() {
                mClockHandler.removeCallbacks(mClockRunnable);
                mClockHandler.post(mClockRunnable);

                mActivityHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // For Snacks
                        SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES_SNACK_VALUES, Context.MODE_PRIVATE);
                        mCarbohydrateValues[0] = sp.getFloat("snack_after_breakfast", 0.0f);
                        mCarbohydrateValues[1] = sp.getFloat("snack_after_lunch", 0.0f);
                        mCarbohydrateValues[2] = sp.getFloat("snack_before_bed", 0.0f);
                        mCarbohydrateValues[3] = 0.0f;

                        // for the backups
                        mBackupManager = new BackupManager(ActivityMealsSnacks.this);

                        framework = new MetabolicFramework(ActivityMealsSnacks.this,
                                new MetabolicFramework.OnMetabolicFrameworkLoadedListener() {

                            @Override
                            public void onLoadFinished() {

                                // meal is true
                                boolean nearestIsAMeal = NearestTime.isSomeMealNearestThanSomeSnack();

                                if (nearestIsAMeal) {
                                    sMealOrSnack.setChecked(true);
                                    setUIToMeal();
                                } else {
                                    sMealOrSnack.setChecked(false);
                                    setUIToSnack();
                                }
                                processModificationOnFoodBundleOrCorrectives();

                                mClockHandler.post(mClockRunnable);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ViewUtil.makeViewVisibleAnimatedly(rlMasterLayout);
                                        flLoading.setVisibility(View.GONE);
                                    }
                                }, 400);


                                sMealOrSnack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if (isChecked) {
                                            setUIToMeal();
                                        } else {
                                            setUIToSnack();
                                        }

                                        mClockHandler.post(mClockRunnable);

                                        rlMasterLayout.setVisibility(View.INVISIBLE);

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                ViewUtil.makeViewVisibleAnimatedly(rlMasterLayout);
                                            }
                                        }, 400);
                                    }
                                });


                            }
                        });
                    }
                });
            }
        });

    }

    public Averages getAverages(){
        return mAverages;
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    // to save and restore the state
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        Gson gson = new Gson();

        // food bundle
        savedInstanceState.putString("food_bundle", gson.toJson(foodBundle));
    }

    @Override
    public void onPause() {
        super.onPause();

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();


        if (mFoodPanel != null) {
            mFoodPanel.hidePanel();

            if (mFoodPanel.isHidden()) {
                transaction.show(mFoodPanel);
            }
        }

        if (mRecommendationPanel != null) {
            mRecommendationPanel.hidePanel();
            if (mRecommendationPanel.isHidden()) {
                transaction.show(mRecommendationPanel);
            }
        }

        if (!transaction.isEmpty())
            transaction.commit();

        ViewUtil.makeViewGone(lShadow);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        Gson gson = new Gson();

        foodSelectedAdapter = new FoodSelectedAdapter(this);

        if (savedInstanceState != null) {
            // food bundle
            String f = savedInstanceState.getString("food_bundle", "");
            if (!f.equals("")) {
                foodBundle = gson.fromJson(f, C.TYPE_TOKEN_TYPE_FOOD_BUNDLE);
            }

        } else {
            if (foodBundle == null) {
                foodBundle = new FoodBundle();
            }

        }

        if (changer == null) {
            changer = new UnitChanger(this);
        }

        foodSelectedAdapter.setFoodSelected(foodBundle.getFoods());
        lvFoodSelected.setAdapter(foodSelectedAdapter);

        processModificationOnFoodBundleOrCorrectives();
    }


    private void searchUIElements() {
        spinnerMealOrSnack = (Spinner) findViewById(R.id.spinnerMealOrSnack);

        rlMasterLayout = (RelativeLayout) findViewById(R.id.rlMasterLayout);
        flLoading = (FrameLayout) findViewById(R.id.flLoading);

        lFoodSelected = (LinearLayout) findViewById(R.id.lFoodSelected);
        lvFoodSelected = (ListView) findViewById(R.id.lvFoodSelected);
        registerForContextMenu(lvFoodSelected);

        tvPreprandialRecommendation = (TextView) findViewById(R.id.tvPreprandialRecommendation);
        tvBasalRecommendation = (TextView) findViewById(R.id.tvBasalRecommendation);
        tvSnackCarbohydratesTotal = (TextView) findViewById(R.id.tvSnackCarbohydratesTotal);

        snackCarbohydratesEditText = (EditTextWeight) findViewById(R.id.snackCarbohydratesEditText);
        snackCarbohydratesEditText.white();

        lSnackCarbohydratesLayout = (LinearLayout) findViewById(R.id.lSnackCarbohydratesLayout);
        lPreprandialLayout = (LinearLayout) findViewById(R.id.lPreprandialLayout);
        lBasalLayout = (LinearLayout) findViewById(R.id.lBasalLayout);
        lNoFoodSelected = (FrameLayout) findViewById(R.id.lNoFoodSelected);

        ivClock = (ImageView) findViewById(R.id.ivClock);
        tvTimeCounter = (TextView) findViewById(R.id.tvTimeCounter);

        lShadow = (FrameLayout) findViewById(R.id.lShadow);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;


        switch (v.getId()) {
            case R.id.lvFoodSelected:
                Food contextFood = foodSelectedAdapter.getItem(info.position);

                // todo hay que apañar esta puta mierda
                // todo hay que apañar esta puta mierda
                // todo hay que apañar esta puta mierda
                // todo hay que apañar esta puta mierda
                String n = contextFood.getName().length() > 10 ? contextFood.getName().substring(0, 10) : contextFood.getName();
                n = n + " (" + toUIFromInternalWeight(contextFood.getWeightInGrams()) + changer.getStringUnitForWeightShort() + ")";

                LayoutInflater i = getLayoutInflater();
                View header = i.inflate(R.layout.cafydia_alert_dialog_custom_title, null);

                TextView title = (TextView) header.findViewById(R.id.tvTitle);
                title.setText(n);
                title.setTextColor(getResources().getColor(R.color.colorCafydiaDefault));
                header.findViewById(R.id.line).setBackgroundColor(getResources().getColor(R.color.colorCafydiaDefault));


                //menu.setHeaderView(header);


                inflater.inflate(R.menu.fragment_selected_food_context_menu, menu);
                break;
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        Food contextFood;
        switch (item.getItemId()) {

            case R.id.edit_selection:
                contextFood = foodSelectedAdapter.getItem(info.position);
                actionSelected(C.FOOD_SELECTED_ACTION_TYPE_EDIT_SELECTION, contextFood);
                return true;

            case R.id.remove_selection:
                contextFood = foodSelectedAdapter.getItem(info.position);
                actionSelected(C.FOOD_SELECTED_ACTION_TYPE_REMOVE_SELECTION, contextFood);
                return true;

        }


        return super.onContextItemSelected(item);
    }

    /*
     * Dialog to agree with contextual action selected
     */
    private void actionSelected(final int type, final Food food) {
        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                this,
                getResources().getColor(R.color.colorCafydiaDefault),
                R.drawable.ic_action_warning,
                getResources().getStringArray(R.array.selected_food_dialog_agree_titles)[type]
        );

        builder.setMessage(getResources().getStringArray(R.array.selected_food_dialog_agree_messages)[type]);

        builder.setPositiveButton(getResources().getString(R.string.food_selected_dialog_button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                switch (type) {
                    case C.FOOD_SELECTED_ACTION_TYPE_EDIT_SELECTION:
                        foodSelectedAdapter.removeFood(food);

                        onSelectedFoodEdited(food);
                        break;
                    case C.FOOD_SELECTED_ACTION_TYPE_REMOVE_SELECTION:
                        foodSelectedAdapter.removeFood(food);

                        onSelectedFoodRemoved(food);
                        break;
                }

            }
        });

        builder.setNegativeButton(getResources().getString(R.string.food_selected_dialog_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }


    ///////////////////////////////////////////////////////
    ///////////
    /////////// MEAL
    ///////////
    ///////////////////////////////////////////////////////

    private void refreshUIWithMeal(Meal m) {
        // ONLY ON MEALS
        if (sMealOrSnack != null && sMealOrSnack.isChecked()) {
            if (mRecommendationPanel != null) {
                mRecommendationPanel.refreshUIWithMeal(m);

            }

            if (m.getBaselineBasal() == null || m.getBaselineBasal() == 0.0f) {
                lBasalLayout.setVisibility(View.GONE);
            } else {
                lBasalLayout.setVisibility(View.VISIBLE);
            }

            if (framework.getBaselinePreprandial().isFunctionGenerated(m.getMealTime())) {

                if (foodBundle.getFoodSize() != 0 && mMinPreprandialDose != 0.0f && m.getTotalPreprandialDose() < mMinPreprandialDose) {
                    tvPreprandialRecommendation.setTextColor(getResources().getColor(R.color.preprandialLight));
                    tvPreprandialRecommendation.setText(mMinPreprandialDose.toString());
                } else {
                    tvPreprandialRecommendation.setTextColor(0xFFEEEEEE);
                    tvPreprandialRecommendation.setText(MyRound.round(m.getTotalPreprandialDose()).toString());
                }
            } else {
                tvPreprandialRecommendation.setTextColor(0xFFEEEEEE);
                tvPreprandialRecommendation.setText(MyRound.round(m.getTotalPreprandialDose()).toString());
            }

            if(m.getTotalBasalDose() != null)
                tvBasalRecommendation.setText(MyRound.round(m.getTotalBasalDose()).toString());
        }

        // BOTH MEAL AND SNACKS
        if (foodBundle.getFoodSize() > 0) {
            lNoFoodSelected.setVisibility(View.GONE);
            lFoodSelected.setVisibility(View.VISIBLE);
        } else {
            lNoFoodSelected.setVisibility(View.VISIBLE);
            lFoodSelected.setVisibility(View.GONE);
        }


    }

    private AdapterView.OnItemSelectedListener mealOrSnackTimeListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            flLoading.setVisibility(View.VISIBLE);

            spinnerPosition = position;


            if (sMealOrSnack.isChecked()) {
                // MEAL

                Meal m = new Meal(spinnerPosition,
                        framework.getEnabledMetabolicRhythm().getId(),
                        framework.getEnabledMetabolicRhythm().getName());

                SharedPreferences sp = getSharedPreferences(BaselineFragment.MINIMUM_PREPRANDIAL_KEY, Context.MODE_PRIVATE);
                String value = "";
                switch (m.getMealTime()) {
                    case C.MEAL_BREAKFAST:
                        value = BaselineFragment.MINIMUM_PREPRANDIAL_VALUE_BR;
                        break;
                    case C.MEAL_LUNCH:
                        value = BaselineFragment.MINIMUM_PREPRANDIAL_VALUE_LU;
                        break;
                    case C.MEAL_DINNER:
                        value = BaselineFragment.MINIMUM_PREPRANDIAL_VALUE_DI;
                        break;
                }

                mMinPreprandialDose = 0.0f;
                if (!value.equals("")) {
                    mMinPreprandialDose = sp.getFloat(value, 0.0f);
                }


                // because here is needed correctives are reloaded we must create a temporally meal object
                // retrieve correctives, and then call to getMealResultant method.

                if (mRecommendationPanel != null) {
                    mRecommendationPanel.reloadCorrectives(m);
                }

                m = getMealResultant();

                refreshUIWithMeal(m);

            } else {
                // SNACK
                snackCarbohydratesEditText.setWeightInGrams(mCarbohydrateValues[position]);
            }

            mClockHandler.post(mClockRunnable);

            ViewUtil.makeViewInvisibleAnimatedly(flLoading);

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void setUIToMeal() {

        Tutorial.MealsSnacks.aboutMeal(mHelpBundle);

        lSnackCarbohydratesLayout.setVisibility(View.GONE);
        lPreprandialLayout.setVisibility(View.VISIBLE);
        //lBasalLayout.setVisibility(View.VISIBLE);

        snackCarbohydratesEditText.removeTextChangedListener(etWatcher);

        spinnerMealOrSnack.setOnItemSelectedListener(mealOrSnackTimeListener);


        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(ActivityMealsSnacks.this, R.array.time_meal, android.R.layout.simple_spinner_dropdown_item);
        spinnerMealOrSnack.setAdapter(spinnerAdapter);
        int pos = NearestTime.getNearestMeal();
        spinnerMealOrSnack.setSelection(pos);
        spinnerPosition = pos;

        if (getActionBar() != null) {
            getActionBar().setTitle(R.string.meals_activity_label);
        }

        refreshPanels();

        invalidateOptionsMenu();

    }

    private void setListeners() {

        // This is to catch onClick events when the layout is shown
        lShadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // foo
            }
        });


    }


    private void refreshPanels() {
        // show the two panels
        FragmentManager manager = getSupportFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();

        mFoodPanel = (FoodFragment) manager.findFragmentByTag("food_panel");
        if (mFoodPanel == null) {
            mFoodPanel = FoodFragment.newInstance();
            transaction.replace(R.id.leftPanel, mFoodPanel, "food_panel");
        } else {
            if (mFoodPanel.isHidden()) {
                transaction.show(mFoodPanel);
            } else if (mFoodPanel.isDetached()) {
                transaction.attach(mFoodPanel);
            }
        }

        if (sMealOrSnack != null && sMealOrSnack.isChecked()) {
            mRecommendationPanel = (RecommendationDetailsFragment) manager.findFragmentByTag("recommendation_panel");

            if (mRecommendationPanel == null) {
                mRecommendationPanel = RecommendationDetailsFragment.newInstance(framework, getMealResultant());
                transaction.replace(R.id.rightPanel, mRecommendationPanel, "recommendation_panel");

                if (mFoodPanel.isPanelVisible()) {
                    transaction.hide(mRecommendationPanel);
                }

            } else {
                if (mRecommendationPanel.isHidden() && !mFoodPanel.isPanelVisible()) {
                    transaction.show(mRecommendationPanel);

                } else if (mRecommendationPanel.isDetached()) {
                    transaction.attach(mRecommendationPanel);

                }
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mRecommendationPanel != null) {
                        mRecommendationPanel.refreshUIWithMeal(getMealResultant());
                    }
                }
            }, 1000);


        } else {
            if (mRecommendationPanel != null) {
                mRecommendationPanel.hidePanel();

                if (!mFoodPanel.isPanelVisible())
                    ViewUtil.makeViewInvisibleAnimatedly(lShadow, C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getSupportFragmentManager().beginTransaction().hide(mRecommendationPanel).commit();

                        if (!mFoodPanel.isPanelVisible()) {
                            ViewUtil.makeViewInvisibleAnimatedly(lShadow, C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS);
                            mRecommendationPanel.hidePanel();
                        }

                        if (mFoodPanel.isHidden())

                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .show(mFoodPanel)
                                    .commit();

                    }
                }, C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS);

            }

        }

        if (!transaction.isEmpty())
            transaction.commit();

    }


    @Override
    public void onGlucoseTestAdded() {

        recalculateHbA1c();

        Meal m = getMealResultant();

        if (mRecommendationPanel != null)
            mRecommendationPanel.refreshLastGlucoseTestAdded();

        refreshUIWithMeal(m);
    }


    ///////////////////////////////////////////////////////
    ///////////
    /////////// SNACK
    ///////////
    ///////////////////////////////////////////////////////

    private void setUIToSnack() {

        Tutorial.MealsSnacks.aboutSnack(mHelpBundle);

        lSnackCarbohydratesLayout.setVisibility(View.VISIBLE);
        lPreprandialLayout.setVisibility(View.GONE);
        lBasalLayout.setVisibility(View.GONE);

        snackCarbohydratesEditText.getInternalEditTextReference().setCursorVisible(false);
        snackCarbohydratesEditText.addTextChangedListener(etWatcher);

        spinnerMealOrSnack.setOnItemSelectedListener(mealOrSnackTimeListener);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(ActivityMealsSnacks.this, R.array.snack_times, android.R.layout.simple_spinner_dropdown_item);
        spinnerMealOrSnack.setAdapter(spinnerAdapter);

        int pos = NearestTime.getNearestSnack();
        spinnerMealOrSnack.setSelection(pos);
        spinnerPosition = pos;

        if (getActionBar() != null) {
            getActionBar().setTitle(R.string.meals_activity_label_snack);
        }

        refreshPanels();

        invalidateOptionsMenu();

    }

    private TextWatcher etWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (foodBundle != null) {
                if (!s.toString().equals("")) {
                    mCarbohydrateValues[spinnerPosition] = Float.parseFloat(s.toString());
                } else {
                    mCarbohydrateValues[spinnerPosition] = 0.0f;
                }

                SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES_SNACK_VALUES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor;
                switch (spinnerPosition) {
                    case 0:
                        sp = getSharedPreferences(SHARED_PREFERENCES_SNACK_VALUES, Context.MODE_PRIVATE);
                        editor = sp.edit();
                        editor.putFloat("snack_after_breakfast", mCarbohydrateValues[0]);
                        editor.apply();

                        mBackupManager.dataChanged();

                        break;
                    case 1:
                        editor = sp.edit();
                        editor.putFloat("snack_after_lunch", mCarbohydrateValues[1]);
                        editor.apply();

                        mBackupManager.dataChanged();
                        break;
                    case 2:
                        editor = sp.edit();
                        editor.putFloat("snack_before_bed", mCarbohydrateValues[2]);
                        editor.apply();

                        mBackupManager.dataChanged();
                        break;
                }
                checkIfButtonDoneShouldBeVisible();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    /*
     * PANELS INTERFACES IMPLEMENTED
     */


    @Override
    public void onFoodClicked(Food food) {
        FragmentManager fm = getSupportFragmentManager();
        Gson gson = new Gson();

        if (sMealOrSnack.isChecked()) {
            // MEAL
            DialogFoodSelected.newInstance(gson.toJson(food)).show(fm, "food_selecting_dialog");
        } else {

            //SNACK
            if (snackCarbohydratesEditText.getWeightInGrams() == 0.0f) {
                new MyToast(ActivityMealsSnacks.this, getString(R.string.snack_activity_no_quantity));
            } else {
                Float topCarbGrams = changer.toInternalWeightFromUI(mCarbohydrateValues[spinnerPosition]) - foodBundle.getTotalCarbohydratesInGrams();
                Float topWeightValue = topCarbGrams / (food.getCPercent() / 100.0f);
                int topUnits = (int) (topWeightValue / food.getWeightPerUnitInGrams());


                if (food.getWeightPerUnitInGrams() == 0.0) {
                    if (topCarbGrams.intValue() == 0) {
                        // ya has llegado al límite.
                        new MyToast(ActivityMealsSnacks.this, getString(R.string.snack_activity_limit_reached));
                    }
                    food.setWeight(topWeightValue);

                    DialogFoodSelected.newInstance(gson.toJson(food), topWeightValue)
                            .show(fm, "food_selecting_dialog");
                } else {
                    if (topUnits == 0) {
                        // ya has llegado al límite.
                        new MyToast(ActivityMealsSnacks.this, getString(R.string.snack_activity_limit_reached));

                    }
                    food.setWeight(topWeightValue);

                    DialogFoodSelected.newInstance(gson.toJson(food), topWeightValue)
                            .show(fm, "food_selecting_dialog");
                }

            }
        }
    }

    @Override
    public void onFoodFinallySelected(Food food) {
        foodAdded(food);
        processModificationOnFoodBundleOrCorrectives();
    }

    private void onSelectedFoodRemoved(Food food) {
        foodRemoved(food);
        processModificationOnFoodBundleOrCorrectives();
    }

    private void onSelectedFoodEdited(Food food) {
        foodRemoved(food);

        FragmentManager fm = getSupportFragmentManager();
        Gson gson = new Gson();

        if (sMealOrSnack.isChecked()) {
            // MEAL
            DialogFoodSelected.newInstance(gson.toJson(food)).show(fm, "food_selecting_dialog");
        } else {
            // SNACK
            Float topCarbGrams = changer.toInternalWeightFromUI(mCarbohydrateValues[spinnerPosition]) - foodBundle.getTotalCarbohydratesInGrams();
            Float topWeightValue = topCarbGrams / (food.getCPercent() / 100.0f);

            DialogFoodSelected.newInstance(gson.toJson(food), topWeightValue).show(fm, "food_selecting_dialog");

        }

    }

    private void foodRemoved(Food food) {
        //foodBundle.removeFood(food);
        foodSelectedAdapter.removeFood(food);
        processModificationOnFoodBundleOrCorrectives();
    }

    private void foodAdded(Food food) {
        //foodBundle.addFood(food);
        foodSelectedAdapter.addFood(food);
        processModificationOnFoodBundleOrCorrectives();
    }

    private String toUIFromInternalWeight(float f) {
        return MyRound.round(changer.toUIFromInternalWeight(f), changer.getDecimalsForWeight()).toString();
    }

    private void processModificationOnFoodBundleOrCorrectives() {
        tvSnackCarbohydratesTotal.setText("Total: " + toUIFromInternalWeight(foodBundle.getTotalCarbohydratesInGrams()) + " " + changer.getStringUnitForWeightShort());

        if (foodBundle.getTotalCarbohydratesInGrams() == 0) {
            tvSnackCarbohydratesTotal.setVisibility(View.INVISIBLE);
        } else {
            tvSnackCarbohydratesTotal.setVisibility(View.VISIBLE);
        }

        Meal meal = getMealResultant();
        refreshUIWithMeal(meal);

        checkIfButtonDoneShouldBeVisible();
    }

    @Override
    public void onSwitchChange(Corrective corrective) {
        processModificationOnFoodBundleOrCorrectives();
    }


    private Meal getMealResultant() {
        if (framework == null)
            return null;

        Meal meal = new Meal(spinnerPosition,
                framework.getEnabledMetabolicRhythm().getId(),
                framework.getEnabledMetabolicRhythm().getName());

        if (foodBundle == null) {
            meal.setFoodSelected(new ArrayList<Food>());

        } else {

            meal.setFoodSelected(foodBundle.getFoods().getFoodArrayList());
        }


        if (mRecommendationPanel != null) {

            framework.fillRecommendationData(meal, mRecommendationPanel.getCorrectivesEnabled());
        }

        return meal;
    }


    /////////////////////////////////////
    //
    // FOR HIDE AND SHOW OPERATION
    //
    /////////////////////////////////////

    @Override
    public void onRequestedHideRecommendationDetailsPanel() {
        mRecommendationPanel.hidePanel();
        ViewUtil.makeViewInvisibleAnimatedly(lShadow, C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS);
        getSupportFragmentManager().beginTransaction().show(mFoodPanel).commit();
    }

    @Override
    public void onRequestedShowRecommendationDetailsPanel() {
        Tutorial.MealsSnacks.aboutRecommendationDetailsPanel(mHelpBundle);

        mFoodPanel.hidePanel();
        ViewUtil.makeViewVisibleAnimatedly(lShadow, C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS);
        getSupportFragmentManager().beginTransaction().hide(mFoodPanel).commit();
        mRecommendationPanel.showPanel();
    }

    @Override
    public void onRequestedHideFoodPanel() {
        mFoodPanel.hidePanel();
        ViewUtil.makeViewInvisibleAnimatedly(lShadow, C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS);

        if (sMealOrSnack.isChecked()) {
            getSupportFragmentManager().beginTransaction().show(mRecommendationPanel).commit();
        }
    }

    @Override
    public void onRequestedShowFoodPanel() {
        Tutorial.MealsSnacks.aboutFoodPanel(mHelpBundle);

        mFoodPanel.showPanel();
        ViewUtil.makeViewVisibleAnimatedly(lShadow, C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS);

        if (sMealOrSnack.isChecked()) {
            mRecommendationPanel.hidePanel();
            getSupportFragmentManager().beginTransaction().hide(mRecommendationPanel).commit();
        }
    }


    /////////////////////////////////////
    //
    // TO RECALCULATE HBA1C FROM DATABASE
    //
    /////////////////////////////////////

    private class RecalculateHbA1c extends AsyncTask<Void, Integer, Boolean> {
        private HbA1cHelper h;

        protected Boolean doInBackground(Void... params) {
            h = new HbA1cHelper(ActivityMealsSnacks.this);
            h.recalculateHbA1c();
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                if (h.getHbA1cPercentage() == 0.0) {
                    // no data
                    new MyToast(ActivityMealsSnacks.this, getString(R.string.main_activity_status_hba1c_no_data), Toast.LENGTH_LONG);
                } else {
                    if (h.getHbA1cPercentage() <= C.HBA1C_TOP_VERY_GOOD) {
                        new MyToast(ActivityMealsSnacks.this, getString(R.string.main_activity_status_hba1c_very_good), Toast.LENGTH_LONG);
                    } else if (h.getHbA1cPercentage() <= C.HBA1C_TOP_GOOD) {
                        new MyToast(ActivityMealsSnacks.this, getString(R.string.main_activity_status_hba1c_good), Toast.LENGTH_LONG);
                    } else if (h.getHbA1cPercentage() <= C.HBA1C_TOP_REGULAR) {
                        new MyToast(ActivityMealsSnacks.this, getString(R.string.main_activity_status_hba1c_not_quite_well), Toast.LENGTH_LONG);
                    } else if (h.getHbA1cPercentage() <= C.HBA1C_TOP_BAD) {
                        new MyToast(ActivityMealsSnacks.this, getString(R.string.main_activity_status_hba1c_bad), Toast.LENGTH_LONG);
                    } else {
                        new MyToast(ActivityMealsSnacks.this, getString(R.string.main_activity_status_hba1c_very_bad), Toast.LENGTH_LONG);
                    }
                }
            }
        }
    }

    private void recalculateHbA1c() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HbA1cHelper mH = new HbA1cHelper(ActivityMealsSnacks.this);
                if (mH.isNeededToRecalculate()) {
                    new RecalculateHbA1c().execute();
                }
            }
        }, 1500);
    }


    /////////////////////////////////////////////
    //
    // ACTION BAR OPTIONS
    //
    /////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_meals, menu);

        menu.findItem(R.id.done).setEnabled(mAllDataOk);
        menu.findItem(R.id.done).setVisible(mAllDataOk);

        // sólo en comidas
        menu.findItem(R.id.take_average_meal).setVisible(sMealOrSnack.isChecked());

        return super.onCreateOptionsMenu(menu);
    }

    private void checkIfButtonDoneShouldBeVisible() {
        boolean result;

        if (sMealOrSnack != null && sMealOrSnack.isChecked()) {
            // MEAL
            Meal meal = getMealResultant();

            result = foodBundle.getFoodSize() > 0;

        } else {
            // SNACK
            result = snackCarbohydratesEditText.getWeightInGrams() != 0.0f &&
                    foodBundle.getTotalCarbohydratesInGrams() > 0.0f;
        }


        if (result != mAllDataOk) {
            mAllDataOk = result;
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        android.app.FragmentManager fm = getFragmentManager();
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.done:
                if (sMealOrSnack.isChecked()) {

                    if (strictMode && !inRange) {
                        new MyToast(this, getString(R.string.meals_activity_strict_mode_activated_not_in_range));
                        return true;
                    }

                    Meal finalMeal = getMealResultant();

                    if(finalMeal != null) {

                        if (finalMeal.getTotalPreprandialDose() < mMinPreprandialDose) {
                            finalMeal.setFinalPreprandialDose(mMinPreprandialDose);
                        } else {
                            finalMeal.setFinalPreprandialDose(finalMeal.getTotalPreprandialDose());
                        }
                    }

                    DialogMealFinished.newInstance(finalMeal, framework).show(fm, "dialog_meal_finished");
                } else {

                    CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                            this,
                            getResources().getColor(R.color.colorCafydiaDefault),
                            R.drawable.ic_action_warning,
                            R.string.snack_activity_dialog_title
                    );
                    builder.setMessage(getString(R.string.snack_activity_dialog_message));

                    builder.setPositiveButton(getString(R.string.snack_activity_dialog_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Snack snack = new Snack(spinnerPosition, framework.getEnabledMetabolicRhythm().getId(), framework.getEnabledMetabolicRhythm().getName());
                            snack.setFoodSelected(foodBundle);
                            snack.save(framework.getDataDatabase());

                            finish();
                        }
                    });
                    builder.setNegativeButton(getString(R.string.snack_activity_dialog_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                }
                return true;

            case R.id.glucose:
                DialogAddGlucose.newInstance().show(fm, "glucose_dialog_fragment");
                return true;

            case R.id.add_carbs:

                DialogFoodSelected.newInstance(
                        new Gson().toJson(new Food(0, getString(R.string.meals_activity_carbohydrates_food), C.FOOD_TYPE_SIMPLE, C.FOOD_FAVORITE_NO, 100f))
                ).show(getSupportFragmentManager(), "food_selecting_dialog");

                return true;

            case R.id.take_average_meal:
                Meal m = getMealResultant();

                if(m != null) {
                    if(mAverages.getAvCarbohydrates(m.getMealTime()) != null) {
                        Food food = new Food(0, getString(R.string.meals_activity_carbohydrates_food), C.FOOD_TYPE_SIMPLE, C.FOOD_FAVORITE_NO, 100f);
                        food.setWeight(mAverages.getAvCarbohydrates(m.getMealTime()));
                        foodAdded(food);
                    } else {
                        new MyToast(this, getString(R.string.meals_activity_average_not_calculated_yet));
                    }
                }

                return true;


        }
        return super.onOptionsItemSelected(item);
    }


    // MEAL HOUR
    private ImageView ivClock;
    private TextView tvTimeCounter;
    private Boolean mClockActivated = null;
    private Instant mRangesInstant;
    private Integer mRange = null;

    private Handler mClockHandler = new Handler();
    private Runnable mClockRunnable = new Runnable() {
        @Override
        public void run() {
            mClockHandler.removeCallbacks(mClockRunnable);

            if (mMissing == null)
                mMissing = getString(R.string.meal_hour_limit_missing);

            if (mMinutes == null)
                mMinutes = getString(R.string.meal_hour_limit_minutes);

            if (mHours == null)
                mHours = getString(R.string.meal_hour_limit_hours);

            if (mClockActivated == null || mRange == null || strictMode == null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ActivityMealsSnacks.this);

                mClockActivated = sp.getBoolean("pref_meal_hours_notify_if_correct_meal_hour", true);
                mRange = Integer.parseInt(sp.getString("pref_meal_hours_choose_minutes_of_range", "60"));
                strictMode = sp.getBoolean("pref_meal_hours_notify_strict_mode", false);
            }

            mRangesInstant = new Instant();

            updateClockCounter();

            mClockHandler.postDelayed(mClockRunnable, 20 * 1000);
        }
    };

    private String mMissing = null;
    private String mMinutes = null;
    private String mHours = null;

    private boolean inRange = false;
    private Boolean strictMode = null;

    private void updateClockCounter() {
        if (sMealOrSnack != null && mClockActivated != null && sMealOrSnack.isChecked() && mClockActivated) {
            // meal
            Meal mealResultant = getMealResultant();

            long lowLimit, highLimit;
            long average = 0;

            switch (mealResultant.getMealTime()) {
                case C.MEAL_BREAKFAST:
                    if (mAverages.getAvMinutesPassedFromMidnightBreakfast() == null) {
                        ivClock.setImageResource(R.drawable.ic_clock_disabled);
                        tvTimeCounter.setText(getString(R.string.meal_hour_limit_disabled));
                        inRange = true;
                        return;
                    }

                    average = mRangesInstant.setTimeToTheStartOfTheDay().increaseNMinutes(mAverages.getAvMinutesPassedFromMidnightBreakfast().intValue()).toDate().getTime();
                    break;

                case C.MEAL_LUNCH:
                    if (mAverages.getAvMinutesPassedFromMidnightLunch() == null) {
                        ivClock.setImageResource(R.drawable.ic_clock_disabled);
                        tvTimeCounter.setText(getString(R.string.meal_hour_limit_disabled));
                        inRange = true;
                        return;
                    }

                    average = mRangesInstant.setTimeToTheStartOfTheDay().increaseNMinutes(mAverages.getAvMinutesPassedFromMidnightLunch().intValue()).toDate().getTime();
                    break;

                case C.MEAL_DINNER:
                    if (mAverages.getAvMinutesPassedFromMidnightDinner() == null) {
                        ivClock.setImageResource(R.drawable.ic_clock_disabled);
                        tvTimeCounter.setText(getString(R.string.meal_hour_limit_disabled));
                        inRange = true;
                        return;
                    }

                    average = mRangesInstant.setTimeToTheStartOfTheDay().increaseNMinutes(mAverages.getAvMinutesPassedFromMidnightDinner().intValue()).toDate().getTime();
                    break;
            }

            int minutesMissing;

            lowLimit = average - ((mRange / 2) * 60 * 1000);
            highLimit = average + ((mRange / 2) * 60 * 1000);

            if (System.currentTimeMillis() < lowLimit) {
                ivClock.setImageResource(R.drawable.ic_clock_out_of_range);
                minutesMissing = (int) Math.abs((System.currentTimeMillis() - lowLimit) / (1000 * 60));
                inRange = false;

            } else if (System.currentTimeMillis() < highLimit) {
                ivClock.setImageResource(R.drawable.ic_clock_in_range);
                minutesMissing = (int) Math.abs((System.currentTimeMillis() - highLimit) / (1000 * 60));
                inRange = true;

            } else {
                ivClock.setImageResource(R.drawable.ic_clock_out_of_range);
                minutesMissing = (int) (((lowLimit + (24 * 60 * 60 * 1000)) - System.currentTimeMillis()) / (1000 * 60));
                inRange = false;

            }

            if ((minutesMissing / 60) > 0) {
                tvTimeCounter.setText(mMissing + " " + (minutesMissing / 60) + " " + mHours + " " + (minutesMissing % 60) + " " + mMinutes);

            } else {
                tvTimeCounter.setText(mMissing + " " + (minutesMissing % 60) + " " + mMinutes);
            }

        } else {

            // snack
            ivClock.setImageResource(R.drawable.ic_clock_disabled);
            tvTimeCounter.setText(getString(R.string.meal_hour_limit_disabled));
            inRange = true;
        }
    }


}