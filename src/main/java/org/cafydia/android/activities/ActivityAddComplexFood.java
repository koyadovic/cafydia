package org.cafydia.android.activities;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.cafydia.android.R;
import org.cafydia.android.configdatabase.ConfigurationDatabase;
import org.cafydia.android.core.Food;
import org.cafydia.android.core.FoodBundle;
import org.cafydia.android.dialogfragments.CafydiaAlertDialog;
import org.cafydia.android.dialogfragments.DialogFoodSelected;
import org.cafydia.android.fragments.FoodFragment;
import org.cafydia.android.fragments.FoodSelectedFragment;
import org.cafydia.android.tutorial.HelpFragmentBundle;
import org.cafydia.android.tutorial.Tutorial;
import org.cafydia.android.util.C;
import org.cafydia.android.util.MyRound;
import org.cafydia.android.util.MyToast;
import org.cafydia.android.util.UnitChanger;
import org.cafydia.android.util.ViewUtil;
import org.cafydia.android.views.EditTextWeight;

/**
 * Created by user on 3/09/14.
 */
public class ActivityAddComplexFood extends  FragmentActivity implements FoodFragment.FoodFragmentInterface,
        DialogFoodSelected.OnFoodFinallySelectedListener,
        FoodSelectedFragment.OnFoodSelectedFragmentListener {

    // the two panels for food and selectedfood
    private FoodFragment mFoodPanel;
    private FoodSelectedFragment mSelectedPanel;

    // the food bundle where will be stored food selected
    private FoodBundle mFoodBundle;

    // final complex food
    private Food mComplexFood;

    // UI elements
    private EditText mEtName;
    //private EditText mEtWeight;
    private EditTextWeight mEtWeight;

    //private TextView mTvTotalCarbohydrates;
    private TextView mTvCarbohydratePercent;

    private FrameLayout lShadow;

    // for units changes
    private UnitChanger mChange;

    // to manage el done button
    private boolean mAllDataOk = false;

    private HelpFragmentBundle mHelpFragmentBundle;

    private void setOrientation(){
        if (getResources().getBoolean(R.bool.phone)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

   /*
    * TO MANAGE LIFE CYCLE
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_complex_food);

        mHelpFragmentBundle = new HelpFragmentBundle(this, R.id.help);

        mChange = new UnitChanger(this);

        // find the elements in the layout
        mEtName = (EditText) findViewById(R.id.etName);
        mEtWeight = (EditTextWeight) findViewById(R.id.totalWeight);

        mTvCarbohydratePercent = (TextView) findViewById(R.id.tvCarbohydratePercent);

        lShadow = (FrameLayout) findViewById(R.id.lShadow);

        // This is to catch onClick events when the layout is shown
        lShadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // foo
            }
        });

        // set the text watcher
        mEtName.addTextChangedListener(nameTextWatcher);
        mEtWeight.addTextChangedListener(weightTextWatcher);


        if(savedInstanceState == null) {
            mFoodPanel = FoodFragment.newInstance();
            mSelectedPanel = FoodSelectedFragment.newInstance();

            mFoodBundle = new FoodBundle();
            mComplexFood = new Food();
            mComplexFood.setType(C.FOOD_TYPE_COMPLEX);

        } else {
            String fb = savedInstanceState.getString("food_bundle");
            String cf = savedInstanceState.getString("complex_food");

            mFoodBundle = new Gson().fromJson(fb, C.TYPE_TOKEN_TYPE_FOOD_BUNDLE);
            mComplexFood = new Gson().fromJson(cf, C.TYPE_TOKEN_TYPE_FOOD);

            mFoodPanel = FoodFragment.newInstance();
            mSelectedPanel = FoodSelectedFragment.newInstance(fb);

            mEtName.setText(savedInstanceState.getString("complex_food_name"));
            mEtWeight.setWeightInGrams(savedInstanceState.getFloat("complex_food_weight"));

            mTvCarbohydratePercent.setText(savedInstanceState.getString("carbohydrate_percent"));
        }

        // show the panels
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.leftPanel, mFoodPanel);
        transaction.replace(R.id.rightPanel, mSelectedPanel);
        transaction.show(mFoodPanel);
        transaction.show(mSelectedPanel);
        transaction.commit();

        if(getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);


        Tutorial.AddComplexFood.aboutAddComplexFoodActivity(mHelpFragmentBundle);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("food_bundle", new Gson().toJson(mFoodBundle, C.TYPE_TOKEN_TYPE_FOOD_BUNDLE));
        savedInstanceState.putString("complex_food", new Gson().toJson(mComplexFood, C.TYPE_TOKEN_TYPE_FOOD));

        savedInstanceState.putString("complex_food_name", mEtName.getText().toString());
        savedInstanceState.putFloat("complex_food_weight", mEtWeight.getWeightInGrams());
        savedInstanceState.putString("carbohydrate_percent", mTvCarbohydratePercent.getText().toString());
    }


    @Override
    public void onPause(){
        super.onPause();

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();


        if(mFoodPanel != null) {
            mFoodPanel.hidePanel();
            if(mFoodPanel.isHidden()){
                transaction.show(mFoodPanel);
            }
        }

        if(mSelectedPanel != null) {
            mSelectedPanel.hidePanel();
            if(mSelectedPanel.isHidden()){
                transaction.show(mSelectedPanel);
            }
        }

        if(!transaction.isEmpty())
            transaction.commit();

        ViewUtil.makeViewGone(lShadow);
    }

    @Override
    public void onFoodSelectedFragmentResumed(){

    }


    TextWatcher weightTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            updateCarbohydratePercent();
            checkIfButtonDoneShouldBeVisible();
        }
    };

    TextWatcher nameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            checkIfButtonDoneShouldBeVisible();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_complex_food_menu, menu);

        menu.findItem(R.id.done).setEnabled(mAllDataOk);
        menu.findItem(R.id.done).setVisible(mAllDataOk);

        return super.onCreateOptionsMenu(menu);
    }

    private void checkIfButtonDoneShouldBeVisible(){
        boolean result = false;
        if(!mEtName.getText().toString().equals("") && mEtWeight.getWeightInGrams() != 0.0f && mFoodBundle.getFoodSize() > 0) {
            Float p = 100.0f / (mEtWeight.getWeightInGrams() / mFoodBundle.getTotalCarbohydratesInGrams());

            result = p > 0.0 && p <= 100.0;
        }

        if(result != mAllDataOk) {
            mAllDataOk = result;
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.done:
                String name = mEtName.getText().toString();

                CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                        this,
                        getResources().getColor(R.color.colorCafydiaDefault),
                        null,
                        R.string.complex_food_activity_dialog_title
                );

                builder.setMessage(getString(R.string.complex_food_activity_dialog_message));

                final String n = name;
                final FragmentActivity activity = this;

                builder.setPositiveButton(getString(R.string.complex_food_activity_dialog_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mComplexFood.setName(n);
                        ConfigurationDatabase db = new ConfigurationDatabase(getBaseContext());
                        mComplexFood.save(db);

                        new MyToast(getApplicationContext(), getString(R.string.complex_food_activity_new_food_added));
                        activity.finish();
                    }
                });
                builder.setNegativeButton(getString(R.string.complex_food_activity_dialog_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }



    private void updateCarbohydratePercent(){
        if(mEtWeight.getWeightInGrams() == 0.0f){
            mTvCarbohydratePercent.setText("0.0%");
            mComplexFood.setCPercent(0.0f);
        }
        else if(mFoodBundle.getFoodSize() == 0){
            mTvCarbohydratePercent.setText("0.0%");
            mComplexFood.setCPercent(0.0f);
        }
        else {
            Float weight = mEtWeight.getWeightInGrams();
            Float carb = mFoodBundle.getTotalCarbohydratesInGrams();
            Float cPerc = 100.0f / (weight / carb);
            if(cPerc > 0.0 && cPerc <= 100.0){
                mTvCarbohydratePercent.setText(MyRound.round(cPerc).toString() + "%");
                mComplexFood.setCPercent(cPerc);
            } else {
                mTvCarbohydratePercent.setText("0.0%");
            }
        }

    }



    /*
     * INTERFACES IMPLEMENTED
     */
    @Override
    public void onFoodClicked(Food food){
        FragmentManager fm = getSupportFragmentManager();
        Gson gson = new Gson();
        DialogFoodSelected.newInstance(gson.toJson(food)).show(fm, "food_selecting_dialog");
    }

    @Override
    public void onFoodFinallySelected(Food food){
        mSelectedPanel.addFoodSelected(food);
        updateUI();
    }

    @Override
    public void onSelectedFoodEdited(Food food){
        updateUI();

        FragmentManager fm = getSupportFragmentManager();
        Gson gson = new Gson();
        DialogFoodSelected.newInstance(gson.toJson(food)).show(fm, "food_selecting_dialog");
    }

    @Override
    public void onSelectedFoodRemoved(Food food){
        updateUI();
    }

    private void updateUI(){
        mFoodBundle = mSelectedPanel.getFoodBundle();

        checkIfButtonDoneShouldBeVisible();
        updateCarbohydratePercent();
    }



    private void disableNameEditText(){
        mEtName.setEnabled(false);
    }
    private void disableWeightEditText(){
        mEtWeight.setEnabled(false);
        mEtWeight.removeTextChangedListener(weightTextWatcher);
    }
    private void enableNameEditText(){
        mEtName.setEnabled(true);
    }
    private void enableWeightEditText(){
        mEtWeight.setEnabled(true);
        mEtWeight.addTextChangedListener(weightTextWatcher);
    }


   /*
    * FOR HIDE AND SHOW OPERATION
    */
    @Override
    public void onRequestedHideFoodSelectedPanel(){
        enableNameEditText();
        enableWeightEditText();

        ViewUtil.makeViewInvisibleAnimatedly(lShadow, C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS);

        mSelectedPanel.hidePanel();
        getSupportFragmentManager().beginTransaction().show(mFoodPanel).commit();
    }
    @Override
    public void onRequestedShowFoodSelectedPanel(){
        disableNameEditText();
        disableWeightEditText();

        ViewUtil.makeViewVisibleAnimatedly(lShadow, C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS);

        mFoodPanel.hidePanel();
        getSupportFragmentManager().beginTransaction().hide(mFoodPanel).commit();
        mSelectedPanel.showPanel();
    }

    @Override
    public void onRequestedHideFoodPanel(){
        enableNameEditText();
        enableWeightEditText();

        ViewUtil.makeViewInvisibleAnimatedly(lShadow, C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS);

        mFoodPanel.hidePanel();
        getSupportFragmentManager().beginTransaction().show(mSelectedPanel).commit();
    }
    @Override
    public void onRequestedShowFoodPanel(){
        Tutorial.AddComplexFood.aboutFoodPanel(mHelpFragmentBundle);

        disableNameEditText();
        disableWeightEditText();

        ViewUtil.makeViewVisibleAnimatedly(lShadow, C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS);

        mSelectedPanel.hidePanel();
        getSupportFragmentManager().beginTransaction().hide(mSelectedPanel).commit();
        mFoodPanel.showPanel();
    }

}
