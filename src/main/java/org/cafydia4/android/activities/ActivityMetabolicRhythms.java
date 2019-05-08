package org.cafydia4.android.activities;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.cafydia4.android.R;
import org.cafydia4.android.configdatabase.ConfigurationDatabase;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.dialogfragments.CafydiaAlertDialog;
import org.cafydia4.android.dialogfragments.DialogDotEditor;
import org.cafydia4.android.dialogfragments.DialogDotSelector;
import org.cafydia4.android.fragments.MetabolicBeginningsDetailsBasal;
import org.cafydia4.android.fragments.MetabolicBeginningsDetailsPreprandial;
import org.cafydia4.android.fragments.MetabolicBeginningsList;
import org.cafydia4.android.fragments.MetabolicCorrectivesDetails;
import org.cafydia4.android.fragments.MetabolicCorrectivesList;
import org.cafydia4.android.fragments.MetabolicDetails;
import org.cafydia4.android.fragments.MetabolicList;
import org.cafydia4.android.recommendations.Corrective;
import org.cafydia4.android.recommendations.MetabolicFramework;
import org.cafydia4.android.recommendations.MetabolicRhythm;
import org.cafydia4.android.recommendations.MetabolicRhythmSlave;
import org.cafydia4.android.recommendations.ModificationStartDot;
import org.cafydia4.android.tutorial.HelpFragmentBundle;
import org.cafydia4.android.tutorial.Tutorial;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.MyToast;
import org.cafydia4.android.util.ViewUtil;

/**
 * Created by user on 15/09/14.
 */
public class ActivityMetabolicRhythms extends FragmentActivity implements
        MetabolicList.MetabolicListListener,
        MetabolicDetails.MetabolicDetailsListener,
        MetabolicBeginningsList.BeginningsListListener,
        MetabolicCorrectivesList.CorrectivesListListener,
        DialogDotEditor.DotEditedListener,
        DialogDotSelector.DotSelectedListener {


    private Integer mCurrentRhythmIdSelected;
    private int mCurrentState;
    private Corrective mCurrentCorrectiveSelected;

    private Fragment mainFragment, lateralFragment, lateralFragment2;

    private MetabolicFramework mFramework;

    private boolean isTablet;

    private boolean forward = true;

    private HelpFragmentBundle mHelpFragmentBundle;

    private void setOrientation(){
        if (getResources().getBoolean(R.bool.phone)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isTablet = false;
        } else {
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
        setContentView(R.layout.activity_metabolic_rhythms);

        mHelpFragmentBundle = new HelpFragmentBundle(this, R.id.help);

        mFramework = new MetabolicFramework(this);

        if(savedInstanceState != null) {
            mCurrentRhythmIdSelected = savedInstanceState.getInt("current_rhythm_selected");
            mCurrentState = savedInstanceState.getInt("current_position");
        } else {
            mCurrentRhythmIdSelected = 0;
            mCurrentState = C.METABOLIC_ACTIVITY_STATE_LIST;
        }

        if(getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);

        updateFragments();


    }

    @Override
    public void onSaveInstanceState(Bundle savedState){
        super.onSaveInstanceState(savedState);

        savedState.putInt("current_rhythm_selected", mCurrentRhythmIdSelected);
        savedState.putInt("current_position", mCurrentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        if(mCurrentState == C.METABOLIC_ACTIVITY_STATE_LIST ||
                (mCurrentState == C.METABOLIC_ACTIVITY_STATE_DETAILS && findViewById(R.id.lateralContainer) != null)) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.activity_metabolic_menu, menu);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                up();
                return true;

            case R.id.action_new_metabolic_rhythm:
                // nuevo ritmo metabólico
                CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                        this,
                        getResources().getColor(R.color.colorCafydiaDefault),
                        null,
                        R.string.metabolic_list_dialog_new_title
                );

                LayoutInflater inflater = this.getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_add_metabolic_rhythm, null);
                final EditText etName = (EditText) view.findViewById(R.id.etName);

                ViewUtil.showKeyboard(this, etName);

                builder.setView(view);
                builder.setPositiveButton(getResources().getString(R.string.metabolic_list_dialog_new_ok_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ViewUtil.hideKeyboard(ActivityMetabolicRhythms.this, etName);
                                if (!etName.getText().toString().equals("")) {
                                    MetabolicRhythmSlave m;
                                    m = new MetabolicRhythmSlave(
                                            0, // id
                                            etName.getText().toString(), // name
                                            "",  // description
                                            C.STARTING_TYPE_GLOBAL, // starting type
                                            C.METABOLIC_RHYTHM_STATE_DISABLED, // no active
                                            new Instant(""), // without startdate
                                            new Instant("")); // without enddate

                                    ConfigurationDatabase db = new ConfigurationDatabase(ActivityMetabolicRhythms.this);
                                    m.save(db);

                                    if (lateralFragment instanceof MetabolicList) {
                                        ((MetabolicList) lateralFragment).notifyStateChange();
                                    }

                                    if (mainFragment instanceof MetabolicList) {
                                        ((MetabolicList) mainFragment).notifyStateChange();
                                    }
                                }
                            }
                        });
                builder.setNegativeButton(getResources().getString(R.string.metabolic_list_dialog_new_cancel_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ViewUtil.hideKeyboard(ActivityMetabolicRhythms.this, etName);

                            }
                        });

                builder.show();

                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            up();
            forward = false;
            return true;

        }

        return super.onKeyDown(keyCode, event);
    }

    private void up(){
        if(mCurrentRhythmIdSelected == 0 && mCurrentState == C.METABOLIC_ACTIVITY_STATE_LIST){
            finish();
        } else {
            boolean changes = false;

            switch (mCurrentState){
                case C.METABOLIC_ACTIVITY_STATE_LIST:
                    finish();
                    break;

                case C.METABOLIC_ACTIVITY_STATE_DETAILS:
                    mCurrentRhythmIdSelected = 0;

                    if(getActionBar() != null) {
                        getActionBar().setSubtitle(null);
                    }

                    // first we save changes in Metabolic Fragment Details
                    ((MetabolicDetails) mainFragment).saveMetabolicRhythm();
                    mFramework.refresh();

                    // And later, reload changes onto Metabolic Fragment List
                    if(lateralFragment instanceof MetabolicList){
                        ((MetabolicList) lateralFragment).notifyStateChange();
                    }
                    mCurrentState = C.METABOLIC_ACTIVITY_STATE_LIST;

                    changes = true;
                    break;
                case C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_LIST:
                case C.METABOLIC_ACTIVITY_STATE_CORRECTIVES_LIST:
                    mCurrentState = C.METABOLIC_ACTIVITY_STATE_DETAILS;
                    changes = true;
                    break;

                case C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_DETAILS_PREPRANDIAL:
                    mCurrentState = C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_LIST;
                    changes = true;
                    break;

                case C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_DETAILS_BASAL:
                    mCurrentState = C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_LIST;
                    changes = true;
                    break;

                case C.METABOLIC_ACTIVITY_STATE_CORRECTIVES_DETAILS:
                    mCurrentState = C.METABOLIC_ACTIVITY_STATE_CORRECTIVES_LIST;

                    if(mainFragment != null && mainFragment instanceof MetabolicCorrectivesDetails) {
                        ((MetabolicCorrectivesDetails) mainFragment).saveCorrectiveToDatabase();
                    }
                    changes = true;
                    break;


            }

            if(changes)
                updateFragments();
        }
    }

    private void updateActionBar(){
        ActionBar a = getActionBar();

        if(a != null) {
            switch (mCurrentState) {
                case C.METABOLIC_ACTIVITY_STATE_LIST:
                case C.METABOLIC_ACTIVITY_STATE_DETAILS:
                    a.setTitle(R.string.metabolic_rhythms_activity_label);
                    break;

                case C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_LIST:
                case C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_DETAILS_PREPRANDIAL:
                case C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_DETAILS_BASAL:
                    a.setTitle(R.string.metabolic_beginning_details_beginnings_actionbar);
                    break;

                case C.METABOLIC_ACTIVITY_STATE_CORRECTIVES_LIST:
                case C.METABOLIC_ACTIVITY_STATE_CORRECTIVES_DETAILS:
                    a.setTitle(R.string.metabolic_correctives_details_label);
                    break;
            }
        }
    }

    //
    ///
    //// When changes happen to current state, current metabolic id or current corrective selected
    //// call this method to update the fragments in the screen
    private void updateFragments(){
        invalidateOptionsMenu();

        updateActionBar();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction tr =  fm.beginTransaction();

        boolean isLand = findViewById(R.id.lateralContainer) != null;

        if(isLand) {
            if(lateralFragment == null) {
                lateralFragment = MetabolicList.newInstance(mFramework);
            }

            tr.replace(R.id.lateralContainer, lateralFragment);

            switch (mCurrentState) {
                case C.METABOLIC_ACTIVITY_STATE_LIST:


                    // hide main fragment
                    if(mainFragment != null && mainFragment.isAdded())
                        tr.remove(mainFragment);

                    // hide secondary fragment
                    if(lateralFragment2 != null && lateralFragment2.isAdded())
                        tr.remove(lateralFragment2);

                    gone(R.id.lateralContainer2);
                    break;

                case C.METABOLIC_ACTIVITY_STATE_DETAILS:


                    // hide main fragment
                    mainFragment = MetabolicDetails.newInstance(mCurrentRhythmIdSelected, mFramework);
                    tr.replace(R.id.mainContainer, mainFragment);

                    // hide secondary fragment
                    if(lateralFragment2 != null && lateralFragment2.isAdded())
                        tr.remove(lateralFragment2);

                    gone(R.id.lateralContainer2);
                    break;

                case C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_LIST:
                    // hide main fragment
                    if(mainFragment != null && mainFragment.isAdded())
                        tr.remove(mainFragment);

                    // show in the secondary fragment the beginnings list
                    lateralFragment2 = MetabolicBeginningsList.newInstance(mCurrentRhythmIdSelected);
                    tr.replace(R.id.lateralContainer2, lateralFragment2);

                    visible(R.id.lateralContainer2);
                    break;

                case C.METABOLIC_ACTIVITY_STATE_CORRECTIVES_LIST:
                    // hide main fragment
                    if(mainFragment != null && mainFragment.isAdded())
                        tr.remove(mainFragment);

                    // show in the secondary fragment the correctives list
                    lateralFragment2 = MetabolicCorrectivesList.newInstance(mCurrentRhythmIdSelected);
                    tr.replace(R.id.lateralContainer2, lateralFragment2);

                    visible(R.id.lateralContainer2);
                    break;

                case C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_DETAILS_PREPRANDIAL:
                    mainFragment = MetabolicBeginningsDetailsPreprandial.newInstance(mCurrentRhythmIdSelected, mFramework);
                    tr.replace(R.id.mainContainer, mainFragment);
                    // show in the secondary fragment the beginnings list
                    lateralFragment2 = MetabolicBeginningsList.newInstance(mCurrentRhythmIdSelected);
                    tr.replace(R.id.lateralContainer2, lateralFragment2);

                    visible(R.id.lateralContainer2);
                    break;

                case C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_DETAILS_BASAL:
                    mainFragment = MetabolicBeginningsDetailsBasal.newInstance(mCurrentRhythmIdSelected, mFramework);
                    tr.replace(R.id.mainContainer, mainFragment);
                    // show in the secondary fragment the beginnings list
                    lateralFragment2 = MetabolicBeginningsList.newInstance(mCurrentRhythmIdSelected);
                    tr.replace(R.id.lateralContainer2, lateralFragment2);

                    visible(R.id.lateralContainer2);
                    break;

                case C.METABOLIC_ACTIVITY_STATE_CORRECTIVES_DETAILS:
                    mainFragment = MetabolicCorrectivesDetails.newInstance(mCurrentCorrectiveSelected);
                    tr.replace(R.id.mainContainer, mainFragment);

                    // show in the secondary fragment the correctives list
                    lateralFragment2 = MetabolicCorrectivesList.newInstance(mCurrentRhythmIdSelected);
                    tr.replace(R.id.lateralContainer2, lateralFragment2);

                    visible(R.id.lateralContainer2);
                    break;
            }

        } else {
            tr.setCustomAnimations(R.animator.enter, R.animator.exit);

            switch (mCurrentState) {
                case C.METABOLIC_ACTIVITY_STATE_LIST:
                    Tutorial.MetabolicRhythms.aboutMetabolicRhythmsList(mHelpFragmentBundle);

                    mainFragment = MetabolicList.newInstance(mFramework);
                    tr.replace(R.id.mainContainer, mainFragment);

                    break;

                case C.METABOLIC_ACTIVITY_STATE_DETAILS:
                    Tutorial.MetabolicRhythms.aboutMetabolicRhythmsDetails(mHelpFragmentBundle);

                    mainFragment = MetabolicDetails.newInstance(mCurrentRhythmIdSelected, mFramework);
                    tr.replace(R.id.mainContainer, mainFragment);
                    break;

                case C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_LIST:
                    Tutorial.Beginnings.aboutBeginningsList(mHelpFragmentBundle);

                    mainFragment = MetabolicBeginningsList.newInstance(mCurrentRhythmIdSelected);
                    tr.replace(R.id.mainContainer, mainFragment);
                    break;

                case C.METABOLIC_ACTIVITY_STATE_CORRECTIVES_LIST:
                    Tutorial.Correctives.aboutCorrectivesList(mHelpFragmentBundle);

                    mainFragment = MetabolicCorrectivesList.newInstance(mCurrentRhythmIdSelected);
                    tr.replace(R.id.mainContainer, mainFragment);
                    break;

                case C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_DETAILS_PREPRANDIAL:
                    Tutorial.Beginnings.aboutBeginningsDetails(mHelpFragmentBundle);

                    mainFragment = MetabolicBeginningsDetailsPreprandial.newInstance(mCurrentRhythmIdSelected, mFramework);
                    tr.replace(R.id.mainContainer, mainFragment);
                    break;

                case C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_DETAILS_BASAL:
                    Tutorial.Beginnings.aboutBeginningsDetails(mHelpFragmentBundle);

                    mainFragment = MetabolicBeginningsDetailsBasal.newInstance(mCurrentRhythmIdSelected, mFramework);
                    tr.replace(R.id.mainContainer, mainFragment);
                    break;

                case C.METABOLIC_ACTIVITY_STATE_CORRECTIVES_DETAILS:
                    Tutorial.Correctives.aboutCorrectivesDetails(mHelpFragmentBundle);
                    mainFragment = MetabolicCorrectivesDetails.newInstance(mCurrentCorrectiveSelected);
                    tr.replace(R.id.mainContainer, mainFragment);
                    break;
            }
        }


        if(!tr.isEmpty()) {
            tr.commit();
        }

    }

    private void gone(int viewId){
        View v = findViewById(viewId);
        if(v != null && v.getVisibility() != View.GONE){
            v.setVisibility(View.GONE);
        }
    }

    private void visible(int viewId){
        View v = findViewById(viewId);
        if(v != null && v.getVisibility() != View.VISIBLE){
            v.setVisibility(View.VISIBLE);
        }
    }

    //
    // Implementation of MetabolicList Interface
    //
    @Override
    public void onRhythmSelected(int id) {
        // we save the changes in main fragment
        if(mainFragment != null && mainFragment instanceof MetabolicDetails){
            ((MetabolicDetails) mainFragment).saveMetabolicRhythm();

            // we refresh the metabolic framework state
            mFramework.refresh();

            // and later, load it onto Metabolic Fragment List
            if(lateralFragment instanceof MetabolicList){
                ((MetabolicList) lateralFragment).notifyStateChange();
            }
        }

        mCurrentRhythmIdSelected = id;
        MetabolicRhythm m = mFramework.getConfigDatabase().getMetabolicRhythmById(id);

        if(m.getState().equals(C.METABOLIC_RHYTHM_STATE_DISABLED)){
            mFramework.connectSecondaryMetabolicRhythm(m);
        } else {
            mFramework.connectSecondaryMetabolicRhythm(null);
        }

        mCurrentState = C.METABOLIC_ACTIVITY_STATE_DETAILS;

        updateFragments();
    }

    @Override
    public void onRhythmDeleted(int id) {

        if(lateralFragment instanceof MetabolicList) {
            ((MetabolicList) lateralFragment).notifyStateChange();
        }

        if(mCurrentRhythmIdSelected.equals(id)) {
            mCurrentState = C.METABOLIC_ACTIVITY_STATE_LIST;
            mCurrentRhythmIdSelected = 0;
        }

        updateFragments();

    }

    @Override
    public boolean onRhythmActivateChangeInList(int id, boolean checked) {
        if(checked) {
            int result = mFramework.requestToConnectAndActivateMetabolicRhythmById(id);
            switch (result) {
                case C.METABOLIC_FRAMEWORK_ERROR_METABOLIC_RHYTHM_PLANED:
                    new MyToast(ActivityMetabolicRhythms.this, getString(R.string.metabolic_details_planned_cannot_disable));
                    return false;

                case C.METABOLIC_FRAMEWORK_ERROR_ALL_OK:

                    if(mainFragment instanceof MetabolicDetails){
                        ((MetabolicDetails) mainFragment).notifyStateChange();
                    }
                    else if(mainFragment instanceof MetabolicBeginningsDetailsPreprandial){
                        ((MetabolicBeginningsDetailsPreprandial) mainFragment).notifyStateChange();
                    }
                    else if(mainFragment instanceof MetabolicBeginningsDetailsBasal){
                        ((MetabolicBeginningsDetailsBasal) mainFragment).notifyStateChange();
                    }
                    else if(mainFragment instanceof MetabolicList){
                        ((MetabolicList) mainFragment).notifyStateChange();
                    }

                    if(lateralFragment instanceof MetabolicList) {
                        ((MetabolicList) lateralFragment).notifyStateChange();
                    }
                    break;
            }
        } else {
            int result = mFramework.requestToDisconnectMetabolicRhythmById(id);
            switch (result) {
                case C.METABOLIC_FRAMEWORK_ERROR_METABOLIC_RHYTHM_PLANED:
                    new MyToast(ActivityMetabolicRhythms.this, getString(R.string.metabolic_details_planned_cannot_disable));
                    return false;

                case C.METABOLIC_FRAMEWORK_ERROR_ALL_OK:
                    if(mainFragment instanceof MetabolicDetails){
                        ((MetabolicDetails) mainFragment).notifyStateChange();
                    }
                    else if(mainFragment instanceof MetabolicBeginningsDetailsPreprandial){
                        ((MetabolicBeginningsDetailsPreprandial) mainFragment).notifyStateChange();
                    }
                    else if(mainFragment instanceof MetabolicBeginningsDetailsBasal){
                        ((MetabolicBeginningsDetailsBasal) mainFragment).notifyStateChange();
                    }
                    else if(mainFragment instanceof MetabolicList){
                        ((MetabolicList) mainFragment).notifyStateChange();
                    }

                    if(lateralFragment instanceof MetabolicList){
                        ((MetabolicList) lateralFragment).notifyStateChange();
                    }
                    break;

            }

        }
        return true;
    }

    //
    // Implementation of MetabolicDetails Interface
    //
    @Override
    public void onRequestedChangeActivityState(int state, int metabolicId) {

        if(mainFragment instanceof MetabolicDetails){
            ((MetabolicDetails) mainFragment).saveMetabolicRhythm();
        }

        mCurrentState = state;
        mCurrentRhythmIdSelected = metabolicId;

        forward = true;

        updateFragments();
    }

    @Override
    public boolean onRhythmActivateChangeInDetails(int id, boolean isChecked) {
        if(isChecked) {
            int result = mFramework.requestToConnectAndActivateMetabolicRhythmById(id);
            switch (result) {
                case C.METABOLIC_FRAMEWORK_ERROR_METABOLIC_RHYTHM_PLANED:
                    new MyToast(ActivityMetabolicRhythms.this, getString(R.string.metabolic_details_planned_cannot_disable));
                    return false;

                case C.METABOLIC_FRAMEWORK_ERROR_ALL_OK:

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(lateralFragment instanceof MetabolicList){
                                ((MetabolicList) lateralFragment).notifyStateChange();
                            }
                            if(mainFragment instanceof MetabolicList){
                                ((MetabolicList) mainFragment).notifyStateChange();
                            }
                        }
                    }, 300);
                    break;
            }
        } else {
            int result = mFramework.requestToDisconnectMetabolicRhythmById(id);
            switch (result) {
                case C.METABOLIC_FRAMEWORK_ERROR_METABOLIC_RHYTHM_PLANED:
                    new MyToast(ActivityMetabolicRhythms.this, getString(R.string.metabolic_details_planned_cannot_disable));
                    return false;

                case C.METABOLIC_FRAMEWORK_ERROR_ALL_OK:

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(lateralFragment instanceof MetabolicList){
                                ((MetabolicList) lateralFragment).notifyStateChange();
                            }
                            if(mainFragment instanceof MetabolicList){
                                ((MetabolicList) mainFragment).notifyStateChange();
                            }
                        }
                    }, 300);
                    break;

            }

        }
        return true;
    }

    @Override
    public void onRhythmNameChanged(MetabolicRhythm m) {
        if(lateralFragment instanceof MetabolicList){
            ((MetabolicList) lateralFragment).onMetabolicRhythmNameTextChanged(m);
        }
        if(mainFragment instanceof MetabolicList){
            ((MetabolicList) mainFragment).onMetabolicRhythmNameTextChanged(m);
        }

    }

    //
    // Implementation of MetabolicBeginningList Interface
    //
    @Override
    public void onBeginningListElementClicked(int elementClicked){
        switch (elementClicked) {

            case C.METABOLIC_BEGINNINGS_LIST_ELEMENT_CLICKED_PREPRANDIAL:
                mCurrentState = C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_DETAILS_PREPRANDIAL;
                forward = true;
                updateFragments();
                break;

            case C.METABOLIC_BEGINNINGS_LIST_ELEMENT_CLICKED_BASAL:
                mCurrentState = C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_DETAILS_BASAL;
                forward = true;
                updateFragments();
                break;
        }
    }


    //
    // Implementation of MetabolicCorrectivesList Interface
    //
    @Override
    public void onCorrectiveSelected(Corrective c){
        mCurrentCorrectiveSelected = c;
        mCurrentState = C.METABOLIC_ACTIVITY_STATE_CORRECTIVES_DETAILS;
        updateFragments();

    }

    @Override
    public void onCorrectiveDeleted(Corrective c){
        if(mCurrentCorrectiveSelected != null && mCurrentCorrectiveSelected.getId().equals(c.getId())){
            mCurrentCorrectiveSelected = null;
        }
        updateFragments();
    }


    //
    // DotEditedListener implementation
    //
    @Override
    public void onDotEdited(final ModificationStartDot dot){

        ConfigurationDatabase db = new ConfigurationDatabase(this);
        dot.save(db);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mainFragment instanceof MetabolicBeginningsDetailsPreprandial) {
                    ((MetabolicBeginningsDetailsPreprandial) mainFragment).refreshDots(dot);
                }
                else if(mainFragment instanceof MetabolicBeginningsDetailsBasal){
                    ((MetabolicBeginningsDetailsBasal) mainFragment).refreshDots(dot);
                }
            }
        }, 300);

    }

    //
    // DotSelectedListener implementation
    //
    @Override
    public void onDotSelected(final ModificationStartDot dot, int action){
        switch (action){
            case C.DIALOG_DOT_SELECTOR_ACTION_EDIT:
                FragmentManager fm = getFragmentManager();
                DialogDotEditor.newInstance(dot).show(fm, "new_dot_dialog");
                break;
            case C.DIALOG_DOT_SELECTOR_ACTION_DELETE:
                ConfigurationDatabase db = new ConfigurationDatabase(this);

                dot.delete(db);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mainFragment instanceof MetabolicBeginningsDetailsPreprandial) {
                            ((MetabolicBeginningsDetailsPreprandial) mainFragment).refreshDots(dot);
                        }
                        else if(mainFragment instanceof MetabolicBeginningsDetailsBasal){
                            ((MetabolicBeginningsDetailsBasal) mainFragment).refreshDots(dot);
                        }
                    }
                }, 300);

                break;
        }
    }

}
