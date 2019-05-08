package org.cafydia4.android.initialconfiguration;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;

import org.cafydia4.android.R;
import org.cafydia4.android.activities.ActivityMain;

import java.util.ArrayList;

/**
 * Created by user on 2/05/15.
 */
public class InitialConfigurationActivity extends FragmentActivity {
    public static final String INITIAL_CONFIG_TAG = "initial_configuration";
    private int mPosition;
    private Button bPrev, bNext;

    private ArrayList<FragmentOneStep> mSteps;

    private void setOrientation(){
        if (getResources().getBoolean(R.bool.phone)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_initial_configuration);

        bPrev = (Button) findViewById(R.id.bPrev);
        bNext = (Button) findViewById(R.id.bNext);

        bPrev.setOnClickListener(bListener);
        bNext.setOnClickListener(bListener);

        mSteps = new ArrayList<>();
        mSteps.add(Fragment0Eula.newInstance());
        mSteps.add(Fragment1Units.newInstance());
        mSteps.add(Fragment2BasalDoses.newInstance());
        mSteps.add(Fragment3CorrectionFactor.newInstance());
        mSteps.add(Fragment4TutorialMode.newInstance());
        mSteps.add(FragmentFinish.newInstance());

        mPosition = 0;
        updateCurrentPositionFragment();
    }

    public void requestToAdvance(){
        if(mPosition == mSteps.size() - 1){
            // final
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(INITIAL_CONFIG_TAG, true);
            editor.apply();

            Intent i = new Intent(InitialConfigurationActivity.this, ActivityMain.class);
            startActivity(i);

            finish();

        } else {

            if (mSteps.get(mPosition).canAdvance()) {
                mPosition++;
                updateCurrentPositionFragment();
            }
        }
    }

    public void requestToGoBack(){
        if(mPosition > 0){
            mPosition --;
            updateCurrentPositionFragment();
        } else {

            finish();
        }
    }

    private void updateCurrentPositionFragment(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, mSteps.get(mPosition), "current_step");
        transaction.commit();

        updateButtons();

    }

    public void updateButtons(){
        if(mPosition == 0) {
            bPrev.setText(getString(R.string.initial_configuration_button_quit));
        } else {
            bPrev.setText(getString(R.string.initial_configuration_button_previous));
        }

        if(mPosition == mSteps.size() - 1){
            bNext.setText(getString(R.string.initial_configuration_button_finish));
        } else {
            bNext.setText(getString(R.string.initial_configuration_button_next));
        }

        if(mSteps.get(mPosition).canAdvance()){
            bNext.setEnabled(true);
        } else {
            bNext.setEnabled(false);
        }

    }

    private View.OnClickListener bListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.bPrev:
                    requestToGoBack();
                    break;

                case R.id.bNext:
                    requestToAdvance();
                    break;
            }
        }
    };
}
