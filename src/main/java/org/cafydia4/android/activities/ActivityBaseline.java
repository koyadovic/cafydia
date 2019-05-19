package org.cafydia4.android.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.dialogfragments.DialogManualFunctionParameters;
import org.cafydia4.android.dialogfragments.DialogMealFinished;
import org.cafydia4.android.fragments.BaselineFragment;
import org.cafydia4.android.genericdialogfragments.DialogConfirmation;
import org.cafydia4.android.recommendations.BaselineBasal;
import org.cafydia4.android.recommendations.BaselinePreprandial;
import org.cafydia4.android.recommendations.MetabolicFramework;
import org.cafydia4.android.tutorial.HelpFragmentBundle;
import org.cafydia4.android.tutorial.Tutorial;
import org.cafydia4.android.util.Averages;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.MyToast;
import org.cafydia4.android.util.ViewUtil;

/**
 * Created by user on 19/03/15.
 */
public class ActivityBaseline extends FragmentActivity {
    private FragmentTabHost mTabHost;
    private int mMealTime = C.MEAL_BREAKFAST;
    private String mMealFragmentTag = "br"; // the first tab selected

    private boolean mAllDataOk = false;

    private MetabolicFramework mFramework;

    private Averages mAverages;
    private DataDatabase mDataDatabase;
    private BaselinePreprandial mPreprandial;
    private BaselineBasal mBasal;

    private HelpFragmentBundle helpFragmentBundle;

    private FrameLayout flLoading;

    private void setOrientation(){
        if (getResources().getBoolean(R.bool.phone)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_baseline);

        helpFragmentBundle = new HelpFragmentBundle(ActivityBaseline.this, R.id.help);

        mTabHost = (FragmentTabHost) findViewById(R.id.tabHost);
        mTabHost.setup(ActivityBaseline.this, getSupportFragmentManager(), android.R.id.tabcontent);

        mFramework = new MetabolicFramework(ActivityBaseline.this, new MetabolicFramework.OnMetabolicFrameworkLoadedListener() {
            @Override
            public void onLoadFinished() {
                mDataDatabase = mFramework.getDataDatabase();
                mPreprandial = mFramework.getBaselinePreprandial();
                mBasal = mFramework.getBaselineBasal();
            }
        });

        mAverages = new Averages(ActivityBaseline.this, new Averages.OnAveragesCalculatedListener() {
            @Override
            public void onAveragesCalculated() {
                View v = findViewById(R.id.flLoading);
                ViewUtil.makeViewInvisibleAnimatedly(v);
            }
        });

        Bundle args1, args2, args3;

        args1 = new Bundle();
        args2 = new Bundle();
        args3 = new Bundle();

        args1.putInt("meal", C.MEAL_BREAKFAST);
        args2.putInt("meal", C.MEAL_LUNCH);
        args3.putInt("meal", C.MEAL_DINNER);

        mTabHost.addTab(mTabHost.newTabSpec("br").setIndicator(getResources().getStringArray(R.array.time_meal)[C.MEAL_BREAKFAST]),
                BaselineFragment.class,
                args1);
        mTabHost.addTab(mTabHost.newTabSpec("lu").setIndicator(getResources().getStringArray(R.array.time_meal)[C.MEAL_LUNCH]),
                BaselineFragment.class,
                args2);
        mTabHost.addTab(mTabHost.newTabSpec("di").setIndicator(getResources().getStringArray(R.array.time_meal)[C.MEAL_DINNER]),
                BaselineFragment.class,
                args3);

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                mMealTime = mTabHost.getCurrentTab();

                switch (mMealTime) {
                    case C.MEAL_BREAKFAST:
                        mMealFragmentTag = "br";
                        break;

                    case C.MEAL_LUNCH:
                        mMealFragmentTag = "lu";
                        break;

                    case C.MEAL_DINNER:
                        mMealFragmentTag = "di";
                        break;
                }

            }
        });

        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {

            final TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i)
                    .findViewById(android.R.id.title);

            if (tv != null)
                tv.setTextColor(0xFFFFFFFF);
        }

        if(getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);


        invalidateOptionsMenu();

        Tutorial.Baseline.aboutBaseline(helpFragmentBundle);




    }


    //
    // Options in the action bar
    //
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_baseline, menu);

        BaselineFragment fragment;

        boolean hasLastMeal = false;

        if(!mMealFragmentTag.equals("")) {
            fragment = (BaselineFragment) getSupportFragmentManager().findFragmentByTag(mMealFragmentTag);

            if(fragment != null) {
                mAllDataOk = fragment.isAllDataOk();
                hasLastMeal = fragment.hasLastMeal();
            } else {
                new MyToast(this, "Fragment es null");
            }
        }

        menu.findItem(R.id.done).setVisible(mAllDataOk);
        menu.findItem(R.id.done).setEnabled(mAllDataOk);

        menu.findItem(R.id.mealInfo).setVisible(hasLastMeal);
        menu.findItem(R.id.mealInfo).setEnabled(hasLastMeal);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BaselineFragment fragment;

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.done:
                if(!mMealFragmentTag.equals("")) {
                    fragment = (BaselineFragment) getSupportFragmentManager().findFragmentByTag(mMealFragmentTag);

                    if(fragment.isAllDataOk()) {

                        DialogConfirmation
                                .newInstance(
                                        "save_changes",
                                        confirmListener,
                                        R.string.activity_baseline_save_changes_title,
                                        R.string.activity_baseline_save_changes_message,
                                        fragment)
                                .show(getFragmentManager(), null);

                    }
                }
                return true;

            case R.id.mealInfo:
                fragment = (BaselineFragment) getSupportFragmentManager().findFragmentByTag(mMealFragmentTag);

                if(fragment.getLastMeal() != null) {
                    DialogMealFinished.newInstance(fragment.getLastMeal(), mFramework)
                            .show(getFragmentManager(), null);
                }

                return true;

            case R.id.manualConfig:
                FragmentManager fm = getSupportFragmentManager();
                DialogManualFunctionParameters.newInstance().show(fm, "manual_parameters_dialog_fragment");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    // for the confirmation before save changes
    private DialogConfirmation.OnConfirmListener confirmListener = new DialogConfirmation.OnConfirmListener() {
        @Override
        public void onConfirmPerformed(String tag, boolean confirmation, Object object) {
            if(tag.equals("save_changes")) {
                if(confirmation) {
                    BaselineFragment fragment = (BaselineFragment) object;
                    fragment.saveChanges();
                }
            }
        }
    };


    public Averages getAverages(){
        return mAverages;
    }

    public BaselineBasal getBasal() {
        return mBasal;
    }

    public BaselinePreprandial getPreprandial() {
        return mPreprandial;
    }

    public DataDatabase getDataDatabase() {
        return mDataDatabase;
    }


}
