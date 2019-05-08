package org.cafydia4.android.activities;


import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.cafydia4.android.R;
import org.cafydia4.android.chartobjects.DataCollectionCriteria;
import org.cafydia4.android.chartobjects.DataCollectionCriteriaInstant;
import org.cafydia4.android.core.GlucoseTest;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.dialogfragments.DialogAddGlucose;
import org.cafydia4.android.dialogfragments.DialogMessage;
import org.cafydia4.android.recommendations.MetabolicFramework;
import org.cafydia4.android.tutorial.HelpFragmentBundle;
import org.cafydia4.android.tutorial.Tutorial;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.HbA1cHelper;
import org.cafydia4.android.util.MyRound;
import org.cafydia4.android.util.MyToast;
import org.cafydia4.android.util.NearestTime;
import org.cafydia4.android.views.GlucoseRingChart;
import org.cafydia4.android.views.HbA1cImageView;

import java.util.ArrayList;
import java.util.LinkedHashMap;


public class ActivityMain extends FragmentActivity implements DialogAddGlucose.GlucoseTestAdded {

    private HbA1cImageView mFace;
    private TextView mPercentage, mMmolMol;
    private DrawerLayout mDrawerLayout;
    private LinearLayout mLeftDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    private GlucoseRingChart mBreakfastRing, mLunchRing, mDinnerRing;

    private HbA1cHelper mH;

    private ExpandableListView eListView;
    private OptionsExpandableAdapter mAdapterMainMenu;

    private boolean mAnimate = true;
    private boolean mWelcomeMessageShowed = false;


    private boolean isTablet;

    private HelpFragmentBundle mHelpFragmentBundle;

    private MetabolicFramework framework;

    private void setOrientation(){
        if (getResources().getBoolean(R.bool.phone)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isTablet = false;
        } else {
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            isTablet = true;
        }
    }

    /*
     * TO MANAGE LIFE CYCLE
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        mHelpFragmentBundle = new HelpFragmentBundle(this, R.id.help);

        mFace = (HbA1cImageView) findViewById(R.id.imageViewFace);
        mPercentage = (TextView) findViewById(R.id.textViewPercentage);
        mMmolMol = (TextView) findViewById(R.id.textViewMmolMol);

        mBreakfastRing = (GlucoseRingChart) findViewById(R.id.breakfastRing);
        mLunchRing = (GlucoseRingChart) findViewById(R.id.lunchRing);
        mDinnerRing = (GlucoseRingChart) findViewById(R.id.dinnerRing);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mLeftDrawer = (LinearLayout) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.action_dialog_button_cancel,
                R.string.action_dialog_button_cancel
        ) {

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_DRAGGING) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ActivityMain.this);
                    SharedPreferences.Editor e = sp.edit();
                    e.putBoolean("know_navigation_drawer", true);
                    e.apply();
                }
            }
        };


        initializeMainMenu();

        checkIfItsNeededTheUpdate();

        if(getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        new MetabolicFramework(this);

        Tutorial.setTutorialListener(new Tutorial.TutorialListener() {
            @Override
            public void onTutorialHelpFragmentBundleFinished() {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ActivityMain.this);
                boolean known = sp.getBoolean("know_navigation_drawer", false);

                if (!known) {
                    mDrawerLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDrawerLayout.openDrawer(mLeftDrawer);
                        }
                    }, 500);
                }

            }
        });

        Tutorial.Main.aboutApp(mHelpFragmentBundle);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onPostResume(){
        super.onPostResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public void onGlucoseTestAdded(){
        checkIfItsNeededTheUpdate();
    }


    /*
     * TOGGLE FLOATING MENU VISIBILITY
     */

    private void initializeMainMenu(){

        ArrayList<String> headersTitle = new ArrayList<String>();
        ArrayList<Drawable> iconsChildren = new ArrayList<Drawable>();
        LinkedHashMap<String, ArrayList<String>> dataChildren = new LinkedHashMap<String, ArrayList<String>>();

        headersTitle.add(getString(R.string.main_activity_header_meals));
        headersTitle.add(getString(R.string.main_activity_header_recommendations));
        headersTitle.add(getString(R.string.main_activity_header_util));

        iconsChildren.add(getResources().getDrawable(R.drawable.ic_main_menu_meals_gray));
        ArrayList<String> meals = new ArrayList<>();
        meals.add(getString(R.string.main_activity_option_meal));

        iconsChildren.add(getResources().getDrawable(R.drawable.ic_main_menu_baseline_gray));
        iconsChildren.add(getResources().getDrawable(R.drawable.ic_main_menu_metabolic_rhythms_gray));
        ArrayList<String> recommendations = new ArrayList<String>();
        recommendations.add(getString(R.string.main_activity_option_baseline));
        recommendations.add(getString(R.string.main_activity_option_metabolic_rhythms));

        iconsChildren.add(getResources().getDrawable(R.drawable.ic_main_menu_charts_gray));
        iconsChildren.add(getResources().getDrawable(R.drawable.ic_main_menu_meals_gray));
        ArrayList<String> usefulTools = new ArrayList<String>();
        usefulTools.add(getString(R.string.main_activity_option_build_the_charts));
        usefulTools.add(getString(R.string.main_activity_option_complex_food));

        dataChildren.put(headersTitle.get(0), meals);
        dataChildren.put(headersTitle.get(1), recommendations);
        dataChildren.put(headersTitle.get(2), usefulTools);

        mAdapterMainMenu = new OptionsExpandableAdapter(this, headersTitle, iconsChildren, dataChildren);

        eListView = (ExpandableListView) findViewById(R.id.eListView);
        eListView.setAdapter(mAdapterMainMenu);

        // meal submenu expanded always
        eListView.expandGroup(0);
        eListView.expandGroup(1);
        eListView.expandGroup(2);

        final Handler h = new Handler();
        final Intent intent0 = new Intent(ActivityMain.this, ActivityMealsSnacks.class);
        final Intent intent3 = new Intent(ActivityMain.this, ActivityBaseline.class);
        final Intent intent4 = new Intent(ActivityMain.this, ActivityMetabolicRhythms.class);
        final Intent intent7 = new Intent(ActivityMain.this, ActivityBuildCharts.class);
        final Intent intent2 = new Intent(ActivityMain.this, ActivityAddComplexFood.class);

        // disable collapse of groups
        eListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });

        eListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                switch (groupPosition){
                    case 0:
                        switch (childPosition){
                            // take a meal
                            case 0:
                                mDrawerLayout.closeDrawers();
                                h.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(intent0);
                                        eListView.smoothScrollToPosition(0);
                                    }
                                }, 300);
                                return true;

                        }
                    case 1:
                        switch (childPosition){
                            case 0:
                                // baseline
                                mDrawerLayout.closeDrawers();
                                h.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(intent3);
                                        eListView.smoothScrollToPosition(0);
                                    }
                                }, 300);
                                return true;

                            case 1:
                                // metabolic rhythms new
                                mDrawerLayout.closeDrawers();
                                h.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(intent4);
                                        eListView.smoothScrollToPosition(0);
                                    }
                                }, 300);
                                return true;

                        }

                    case 2:
                        switch (childPosition){
                            case 0:
                                // build your charts
                                mDrawerLayout.closeDrawers();
                                h.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(intent7);
                                        eListView.smoothScrollToPosition(0);
                                    }
                                }, 300);
                                return true;

                            case 1:
                                // Add complex food
                                mDrawerLayout.closeDrawers();
                                h.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(intent2);
                                        eListView.smoothScrollToPosition(0);
                                    }
                                }, 300);
                                return true;


                        }

                }
                return false;
            }
        });




    }

    /*
     * TO MANAGE ACTIONBAR MENU
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        FragmentManager fm = getFragmentManager();

        switch(item.getItemId()){
            case R.id.glucose:
                DialogAddGlucose.newInstance().show(fm, "glucose_dialog_fragment");
                return true;

            case R.id.settings:
                Intent intent = new Intent(this, ActivitySettings.class);
                startActivity(intent);
                return true;

            case R.id.welcome:
                showWelcomeMessage();
                return true;

            case R.id.about:
                DialogMessage.newInstance(
                        getString(R.string.main_activity_about_title),
                        getString(R.string.main_activity_about_message)
                ).show(fm, "dialog_message");

                return true;

            case R.id.suggestion:
                Intent i = new Intent (Intent.ACTION_SEND);
                i.setType ("plain/text"); // "message/rfc822"
                i.putExtra (Intent.EXTRA_EMAIL, new String[] {"suggestions@cafydia.org"});
                i.putExtra (Intent.EXTRA_SUBJECT, getString(R.string.main_activity_option_suggestion_subject));
                startActivity(i);
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showWelcomeMessage(){
        FragmentManager fm = getFragmentManager();
        String t = "";
        switch (NearestTime.getNearestSnack()){
            case C.SNACK_AFTER_BREAKFAST:
                t = getString(R.string.main_activity_welcome_title_morning);
                break;
            case C.SNACK_AFTER_LUNCH:
                t = getString(R.string.main_activity_welcome_title_afternoon);
                break;
            case C.SNACK_BEFORE_BED:
                t = getString(R.string.main_activity_welcome_title_night);
                break;
        }
        DialogMessage.newInstance(
                t,
                getString(R.string.main_activity_welcome_message),
                "cafydia_welcome")
                .show(fm, "welcome_message");
    }


    /*
     * TO RECALCULATE HBA1C FROM DATABASE AND UPDATE THE FACE ICON
     */
    private class RecalculateHbA1c extends AsyncTask<Void, Integer, Boolean> {
        private HbA1cHelper h;
        protected Boolean doInBackground(Void... params){
            h = new HbA1cHelper(ActivityMain.this);
            h.recalculateHbA1c();
            return true;
        }
        protected void onPostExecute(Boolean result){
            if (result){
                updateFace(h);
                if(h.getHbA1cPercentage() == 0.0) {
                    // no data
                    new MyToast(ActivityMain.this, getString(R.string.main_activity_status_hba1c_no_data), Toast.LENGTH_LONG);
                } else {
                    if(h.getHbA1cPercentage() <= C.HBA1C_TOP_VERY_GOOD) {
                        new MyToast(ActivityMain.this, getString(R.string.main_activity_status_hba1c_very_good), Toast.LENGTH_LONG);
                    }
                    else if(h.getHbA1cPercentage() <= C.HBA1C_TOP_GOOD) {
                        new MyToast(ActivityMain.this, getString(R.string.main_activity_status_hba1c_good), Toast.LENGTH_LONG);
                    }
                    else if(h.getHbA1cPercentage() <= C.HBA1C_TOP_REGULAR) {
                        new MyToast(ActivityMain.this, getString(R.string.main_activity_status_hba1c_not_quite_well), Toast.LENGTH_LONG);
                    }
                    else if(h.getHbA1cPercentage() <= C.HBA1C_TOP_BAD) {
                        new MyToast(ActivityMain.this, getString(R.string.main_activity_status_hba1c_bad), Toast.LENGTH_LONG);
                    }
                    else {
                        new MyToast(ActivityMain.this, getString(R.string.main_activity_status_hba1c_very_bad), Toast.LENGTH_LONG);
                    }
                }
            }
        }
    }

    private class LoadGlucosesForRings extends AsyncTask<Void, Integer, Boolean> {
        private DataDatabase mDatabase;
        private Context mContext;

        private int breakfastLow;
        private int breakfastGood;
        private int breakfastLittleHigh;
        private int breakfastHigh;

        private int lunchLow;
        private int lunchGood;
        private int lunchLittleHigh;
        private int lunchHigh;

        private int dinnerLow;
        private int dinnerGood;
        private int dinnerLittleHigh;
        private int dinnerHigh;


        protected void onPreExecute(){
            mContext = ActivityMain.this;

            mBreakfastRing.reset();
            mLunchRing.reset();
            mDinnerRing.reset();
        }

        protected Boolean doInBackground(Void... params){
            mDatabase = new DataDatabase(ActivityMain.this);

            DataCollectionCriteriaInstant since = new DataCollectionCriteriaInstant(mContext, mDatabase, C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE, -30);
            DataCollectionCriteriaInstant until = new DataCollectionCriteriaInstant(mContext, mDatabase, C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE, 0);

            DataCollectionCriteria criteriaBreakfast = new DataCollectionCriteria(0,
                    since,
                    until,
                    C.DATA_COLLECTION_CRITERIA_NO_ACTIVATED,
                    0,
                    C.DATA_COLLECTION_CRITERIA_ACTIVATED,
                    0
            );

            criteriaBreakfast.setCollectOnGlucoseTime(0, true);
            criteriaBreakfast.setCollectOnGlucoseTime(1, true);


            DataCollectionCriteria criteriaLunch= new DataCollectionCriteria(0,
                    since,
                    until,
                    C.DATA_COLLECTION_CRITERIA_NO_ACTIVATED,
                    0,
                    C.DATA_COLLECTION_CRITERIA_ACTIVATED,
                    0
            );

            criteriaLunch.setCollectOnGlucoseTime(2, true);
            criteriaLunch.setCollectOnGlucoseTime(3, true);


            DataCollectionCriteria criteriaDinner = new DataCollectionCriteria(0,
                    since,
                    until,
                    C.DATA_COLLECTION_CRITERIA_NO_ACTIVATED,
                    0,
                    C.DATA_COLLECTION_CRITERIA_ACTIVATED,
                    0
            );

            criteriaDinner.setCollectOnGlucoseTime(4, true);
            criteriaDinner.setCollectOnGlucoseTime(5, true);

            ArrayList<GlucoseTest> gBreakfast = mDatabase.getGlucoseTestsByCriteria(criteriaBreakfast);
            ArrayList<GlucoseTest> gLunch = mDatabase.getGlucoseTestsByCriteria(criteriaLunch);
            ArrayList<GlucoseTest> gDinner = mDatabase.getGlucoseTestsByCriteria(criteriaDinner);

            for(GlucoseTest g : gBreakfast){
                if(g.getGlucoseLevel() < 60) {
                    breakfastLow++;
                }
                else if(g.getGlucoseLevel() < 150) {
                    breakfastGood++;
                }
                else if(g.getGlucoseLevel() < 180) {
                    breakfastLittleHigh++;
                }
                else {
                    breakfastHigh++;
                }
            }

            for(GlucoseTest g : gLunch){
                if(g.getGlucoseLevel() < 60) {
                    lunchLow++;
                }
                else if(g.getGlucoseLevel() < 150) {
                    lunchGood++;
                }
                else if(g.getGlucoseLevel() < 180) {
                    lunchLittleHigh++;
                }
                else {
                    lunchHigh++;
                }
            }

            for(GlucoseTest g : gDinner){
                if(g.getGlucoseLevel() < 60) {
                    dinnerLow++;
                }
                else if(g.getGlucoseLevel() < 150) {
                    dinnerGood++;
                }
                else if(g.getGlucoseLevel() < 180) {
                    dinnerLittleHigh++;
                }
                else {
                    dinnerHigh++;
                }
            }

            return true;
        }

        protected void onPostExecute(Boolean result){
            if (result && mContext != null){
                Resources r = getResources();
                mBreakfastRing.setRingTitle(getString(R.string.glucose_ring_breakfast));
                mBreakfastRing.addSection("low", r.getColor(R.color.low_inner), r.getColor(R.color.low_outer), breakfastLow);
                mBreakfastRing.addSection("good", r.getColor(R.color.good_inner), r.getColor(R.color.good_outer), breakfastGood);
                mBreakfastRing.addSection("regular", r.getColor(R.color.regular_inner), r.getColor(R.color.regular_outer), breakfastLittleHigh);
                mBreakfastRing.addSection("bad", r.getColor(R.color.bad_inner), r.getColor(R.color.bad_outer), breakfastHigh);

                mLunchRing.setRingTitle(getString(R.string.glucose_ring_lunch));
                mLunchRing.addSection("low", r.getColor(R.color.low_inner), r.getColor(R.color.low_outer), lunchLow);
                mLunchRing.addSection("good", r.getColor(R.color.good_inner), r.getColor(R.color.good_outer), lunchGood);
                mLunchRing.addSection("regular", r.getColor(R.color.regular_inner), r.getColor(R.color.regular_outer), lunchLittleHigh);
                mLunchRing.addSection("bad", r.getColor(R.color.bad_inner), r.getColor(R.color.bad_outer), lunchHigh);

                mDinnerRing.setRingTitle(getString(R.string.glucose_ring_dinner));
                mDinnerRing.addSection("low", r.getColor(R.color.low_inner), r.getColor(R.color.low_outer), dinnerLow);
                mDinnerRing.addSection("good", r.getColor(R.color.good_inner), r.getColor(R.color.good_outer), dinnerGood);
                mDinnerRing.addSection("regular", r.getColor(R.color.regular_inner), r.getColor(R.color.regular_outer), dinnerLittleHigh);
                mDinnerRing.addSection("bad", r.getColor(R.color.bad_inner), r.getColor(R.color.bad_outer), dinnerHigh);

                if(mAnimate){
                    mBreakfastRing.refreshAnimatedly();
                    mLunchRing.refreshAnimatedly();
                    mDinnerRing.refreshAnimatedly();

                    mAnimate = false;

                } else {
                    mBreakfastRing.refresh();
                    mLunchRing.refresh();
                    mDinnerRing.refresh();

                }

            }
        }
    }


    private void updateFace(HbA1cHelper h){
        mFace.setHbA1cPercentage(h.getHbA1cPercentage());

        if(mAnimate) {
            mFace.refreshAnimatedly();
        }

        if(h.getHbA1cPercentage() != 0.0) {
            mPercentage.setText(MyRound.round(h.getHbA1cPercentage()).toString());
            mMmolMol.setText(MyRound.round(h.getHbA1cMmolMol()).toString());
        }

    }

    private void checkIfItsNeededTheUpdate(){
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new LoadGlucosesForRings().execute();

                mH = new HbA1cHelper(ActivityMain.this);
                if (mH.isNeededToRecalculate()) {
                    new RecalculateHbA1c().execute();
                } else {
                    updateFace(mH);
                }

            }
        }, 200);
    }

    private class OptionsExpandableAdapter extends BaseExpandableListAdapter {
        private Context context;
        private ArrayList<String> headersText;
        private ArrayList<Drawable> iconsChildren;
        private LinkedHashMap<String, ArrayList<String>> dataChildren;

        public OptionsExpandableAdapter(
                Context c,
                ArrayList<String> headersText,
                ArrayList<Drawable> iconsChildren,
                LinkedHashMap<String, ArrayList<String>> dataChildren){

            context = c;
            this.headersText = headersText;
            this.iconsChildren = iconsChildren;
            this.dataChildren = dataChildren;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition){
            return this.dataChildren.get(this.headersText.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition){
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent){
            final String childText = (String) getChild(groupPosition, childPosition);
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.activity_main_expandable_child, null);
            }

            ImageView ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            TextView tvChildTitle = (TextView) convertView.findViewById(R.id.tvChildTitle);

            tvChildTitle.setText(childText);
            tvChildTitle.setTypeface(null, Typeface.BOLD);
            tvChildTitle.setTextColor(0xFF777777);


            // we love ninjas
            int index = 0;
            for(int a = 0; a < groupPosition ; a++) {
                index += getChildrenCount(a);
            }
            index += childPosition;
            ivIcon.setImageDrawable(iconsChildren.get(index));

            //DisplayMetrics metrics = getResources().getDisplayMetrics();
            //tvChildTitle.setTextSize(10 * metrics.density);

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition){
            return dataChildren.get(headersText.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition){
            return headersText.get(groupPosition);
        }

        @Override
        public int getGroupCount(){
            return headersText.size();
        }

        @Override
        public long getGroupId(int groupPosition){
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent){
            String headerTitleString = (String) getGroup(groupPosition);

            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.activity_main_expandable_header, null);
            }

            //ImageView headerIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            TextView headerTitle = (TextView) convertView.findViewById(R.id.tvTitle);

            //headerIcon.setImageDrawable(iconsChildren.get(groupPosition));
            headerTitle.setText(headerTitleString);

            headerTitle.setTypeface(null, Typeface.BOLD);
            headerTitle.setTextColor(0xFF444444);

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            //headerTitle.setTextSize(9 * metrics.density);

            convertView.setClickable(false);

            return convertView;
        }

        @Override
        public boolean hasStableIds(){
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mDrawerLayout.isDrawerOpen(mLeftDrawer)) {
                mDrawerLayout.closeDrawer(mLeftDrawer);
            } else {
                finish();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}