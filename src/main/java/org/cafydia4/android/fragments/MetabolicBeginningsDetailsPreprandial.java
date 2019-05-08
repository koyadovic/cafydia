package org.cafydia4.android.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.cafydia4.android.R;
import org.cafydia4.android.configdatabase.ConfigurationDatabase;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.dialogfragments.DialogDotEditor;
import org.cafydia4.android.dialogfragments.DialogDotSelector;
import org.cafydia4.android.recommendations.MetabolicFramework;
import org.cafydia4.android.recommendations.MetabolicRhythm;
import org.cafydia4.android.recommendations.ModificationStart;
import org.cafydia4.android.recommendations.ModificationStartDot;
import org.cafydia4.android.util.C;
import org.cafydia4.android.views.ModificationStartView;

/**
 * Created by user on 27/09/14.
 */
public class MetabolicBeginningsDetailsPreprandial extends Fragment {
    private ModificationStartView chartBreakfast, chartLunch, chartDinner;
    private ModificationStart startBreakfast, startLunch, startDinner;
    private RadioButton rbGlobal, rbSpecific;
    RadioGroup rgType;

    private int metabolicRhythmId;

    private static MetabolicFramework framework = null;

    private ModificationStart targetStart = null;
    private Integer targetType = null;

    private MetabolicRhythm metabolicRhythm;

    public static Fragment newInstance(Integer metabolicRhythmId, MetabolicFramework f){
        MetabolicBeginningsDetailsPreprandial fragment = new MetabolicBeginningsDetailsPreprandial();

        framework = f;

        if(metabolicRhythmId != null) {
            Bundle args = new Bundle();
            args.putInt("metabolic_rhythm_id", metabolicRhythmId);
            fragment.setArguments(args);
        }

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View layout = inflater.inflate(R.layout.fragment_metabolic_beginnings_details_preprandial, container, false);

        /*
         * Obtenemos el ritmo metabólico para tener la información más básica de él
         */
        this.metabolicRhythmId = getArguments().getInt("metabolic_rhythm_id");

        // los radio buttons, para activar el que el ritmo metabólico tenga activado.
        rbGlobal = (RadioButton) layout.findViewById(R.id.rbGlobal);
        rbSpecific = (RadioButton) layout.findViewById(R.id.rbSpecific);

        rgType = (RadioGroup) layout.findViewById(R.id.rgType);

        chartBreakfast = (ModificationStartView) layout.findViewById(R.id.chartBreakfast);
        chartLunch = (ModificationStartView) layout.findViewById(R.id.chartLunch);
        chartDinner = (ModificationStartView) layout.findViewById(R.id.chartDinner);

        chartBreakfast.white();
        chartLunch.white();
        chartDinner.white();

        new LoadDataNeeded().execute();

        registerForContextMenu(chartBreakfast);
        registerForContextMenu(chartLunch);
        registerForContextMenu(chartDinner);

        return layout;
    }

    private RadioGroup.OnCheckedChangeListener rgListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId){
                case R.id.rbGlobal:
                    metabolicRhythm.setStartingPreprandialType(C.STARTING_TYPE_GLOBAL);

                    break;
                case R.id.rbSpecific:
                    metabolicRhythm.setStartingPreprandialType(C.STARTING_TYPE_SPECIFIC);
                    break;
            }

            metabolicRhythm.save(framework.getConfigDatabase());

            if (metabolicRhythm.getState().equals(C.METABOLIC_RHYTHM_STATE_ENABLED)){
                Instant startDate = new Instant(framework.getState().getActivatedMetabolicRhythmStartDate()).setTimeToTheStartOfTheDay();
                chartBreakfast.setStartDate(startDate);
                chartBreakfast.setDaysPassed(new Instant().setTimeToTheStartOfTheDay().getDaysPassedFromInstant(startDate));
                chartLunch.setStartDate(startDate);
                chartLunch.setDaysPassed(new Instant().setTimeToTheStartOfTheDay().getDaysPassedFromInstant(startDate));
                chartDinner.setStartDate(startDate);
                chartDinner.setDaysPassed(new Instant().setTimeToTheStartOfTheDay().getDaysPassedFromInstant(startDate));

                startBreakfast = framework.getEnabledPreprandialStartForBreakfast();
                startLunch = framework.getEnabledPreprandialStartForLunch();
                startDinner = framework.getEnabledPreprandialStartForDinner();

                chartBreakfast.setMetabolicRhythmStartType(metabolicRhythm.getStartingPreprandialType());
                chartBreakfast.setMetabolicStateStartType(framework.getState().getStartingPreprandialType());

                chartLunch.setMetabolicRhythmStartType(metabolicRhythm.getStartingPreprandialType());
                chartLunch.setMetabolicStateStartType(framework.getState().getStartingPreprandialType());

                chartDinner.setMetabolicRhythmStartType(metabolicRhythm.getStartingPreprandialType());
                chartDinner.setMetabolicStateStartType(framework.getState().getStartingPreprandialType());

            } else {
                Instant startDate = new Instant().setTimeToTheStartOfTheDay();
                chartBreakfast.setStartDate(startDate);
                chartLunch.setStartDate(startDate);
                chartDinner.setStartDate(startDate);

                startBreakfast = framework.getSecondaryPreprandialStartForBreakfast();
                startLunch = framework.getSecondaryPreprandialStartForLunch();
                startDinner = framework.getSecondaryPreprandialStartForDinner();

                chartBreakfast.setMetabolicRhythmStartType(metabolicRhythm.getStartingPreprandialType());
                chartBreakfast.setMetabolicStateStartType(framework.getState().getStartingPreprandialType());

                chartLunch.setMetabolicRhythmStartType(metabolicRhythm.getStartingPreprandialType());
                chartLunch.setMetabolicStateStartType(framework.getState().getStartingPreprandialType());

                chartDinner.setMetabolicRhythmStartType(metabolicRhythm.getStartingPreprandialType());
                chartDinner.setMetabolicStateStartType(framework.getState().getStartingPreprandialType());
            }




            chartBreakfast.setStart(startBreakfast);
            chartLunch.setStart(startLunch);
            chartDinner.setStart(startDinner);

        }
    };

    @Override
    public void onSaveInstanceState(Bundle savedState){
        super.onSaveInstanceState(savedState);
    }

    /*
     * Contextual menu
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();

        inflater.inflate(R.menu.long_click_chart_menu, menu);

        if(metabolicRhythm.getStartingPreprandialType() == C.STARTING_TYPE_SPECIFIC) {
            switch (v.getId()) {
                case R.id.chartBreakfast:
                    targetStart = startBreakfast;
                    targetType = C.DOT_TYPE_PREPRANDIAL_INSULIN_BREAKFAST;
                    break;
                case R.id.chartLunch:
                    targetStart = startLunch;
                    targetType = C.DOT_TYPE_PREPRANDIAL_INSULIN_LUNCH;
                    break;
                case R.id.chartDinner:
                    targetStart = startDinner;
                    targetType = C.DOT_TYPE_PREPRANDIAL_INSULIN_DINNER;
                    break;

            }
        } else {
            targetStart = startBreakfast; // we catch one, because the others are equal.
            targetType = C.DOT_TYPE_PREPRANDIAL_INSULIN_GLOBAL;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        FragmentManager fm = getFragmentManager();

        if(targetStart != null) {
            switch (item.getItemId()){
                case R.id.addDot:
                    ModificationStartDot newDot = new ModificationStartDot(0, targetType, metabolicRhythmId, 0f, 0f);
                    DialogDotEditor.newInstance(newDot).show(fm, "new_dot_dialog");
                    return true;

                case R.id.editDot:
                    DialogDotSelector.newInstance(targetStart, C.DIALOG_DOT_SELECTOR_ACTION_EDIT).show(fm, "selector");
                    return true;

                case R.id.deleteDot:
                    DialogDotSelector.newInstance(targetStart, C.DIALOG_DOT_SELECTOR_ACTION_DELETE).show(fm, "selector");
                    return true;
            }
        }
        return super.onContextItemSelected(item);

    }

    private class LoadDataNeeded extends AsyncTask<Void, Integer, Boolean> {

        protected Boolean doInBackground(Void... params){
            if(getActivity() == null){
                return false;
            }

            updateReferenceToMetabolicRhythm();

            framework.reassembleStarts();

            return true;
        }
        protected void onPostExecute(Boolean result){
            if (result){
                /*
                 * Here load the other objects that depends
                 * on the metabolic framework
                 */

                if(getActivity() != null) {
                    ActionBar ab = getActivity().getActionBar();
                    // set action bar title and subtitle
                    if (ab != null) {
                        if (metabolicRhythm != null)
                            ab.setSubtitle(metabolicRhythm.getName());
                    }
                }

                notifyStateChange();


                rgType.setOnCheckedChangeListener(null);
                if(metabolicRhythm.getStartingPreprandialType().equals(C.STARTING_TYPE_SPECIFIC)){
                    rbSpecific.setChecked(true);

                } else {
                    rbGlobal.setChecked(true);
                }
                rgType.setOnCheckedChangeListener(rgListener);

            }
        }
    }

    public void refreshDots(ModificationStartDot dot){
        ConfigurationDatabase db = framework.getConfigDatabase();
        ModificationStart start = db.getModificationStart(metabolicRhythm.getId(), dot.getType());

        switch(dot.getType()){
            case C.DOT_TYPE_PREPRANDIAL_INSULIN_GLOBAL:
                metabolicRhythm.setPreprandialModificationStart(start);
                break;
            case C.DOT_TYPE_PREPRANDIAL_INSULIN_BREAKFAST:
                metabolicRhythm.setPreprandialModificationStartBreakfast(start);
                break;
            case C.DOT_TYPE_PREPRANDIAL_INSULIN_LUNCH:
                metabolicRhythm.setPreprandialModificationStartLunch(start);
                break;
            case C.DOT_TYPE_PREPRANDIAL_INSULIN_DINNER:
                metabolicRhythm.setPreprandialModificationStartDinner(start);
                break;
        }

        new LoadDataNeeded().execute();

    }

    private void updateReferenceToMetabolicRhythm(){
        // importantísimo
        if(framework.getState().getActivatedMetabolicRhythmId() == metabolicRhythmId){
            metabolicRhythm = framework.getEnabledMetabolicRhythm();
        } else {
            if(framework.getSecondaryMetabolicRhythm() == null) {
                metabolicRhythm = framework.getConfigDatabase().getMetabolicRhythmById(metabolicRhythmId);
                framework.connectSecondaryMetabolicRhythm(metabolicRhythm);
            } else {
                metabolicRhythm = framework.getSecondaryMetabolicRhythm();
            }
        }
    }

    public void notifyStateChange(){
        updateReferenceToMetabolicRhythm();

        if (metabolicRhythm != null && metabolicRhythm.getState().equals(C.METABOLIC_RHYTHM_STATE_ENABLED)){
            Instant startDate = new Instant(framework.getState().getActivatedMetabolicRhythmStartDate()).setTimeToTheStartOfTheDay();
            chartBreakfast.setStartDate(startDate);
            chartBreakfast.setDaysPassed(new Instant().setTimeToTheStartOfTheDay().getDaysPassedFromInstant(startDate));
            chartLunch.setStartDate(startDate);
            chartLunch.setDaysPassed(new Instant().setTimeToTheStartOfTheDay().getDaysPassedFromInstant(startDate));
            chartDinner.setStartDate(startDate);
            chartDinner.setDaysPassed(new Instant().setTimeToTheStartOfTheDay().getDaysPassedFromInstant(startDate));

            startBreakfast = framework.getEnabledPreprandialStartForBreakfast();
            startLunch = framework.getEnabledPreprandialStartForLunch();
            startDinner = framework.getEnabledPreprandialStartForDinner();

            chartBreakfast.setMetabolicRhythmStartType(metabolicRhythm.getStartingPreprandialType());
            chartBreakfast.setMetabolicStateStartType(framework.getState().getStartingPreprandialType());

            chartLunch.setMetabolicRhythmStartType(metabolicRhythm.getStartingPreprandialType());
            chartLunch.setMetabolicStateStartType(framework.getState().getStartingPreprandialType());

            chartDinner.setMetabolicRhythmStartType(metabolicRhythm.getStartingPreprandialType());
            chartDinner.setMetabolicStateStartType(framework.getState().getStartingPreprandialType());

        } else {
            Instant startDate = new Instant().setTimeToTheStartOfTheDay();
            chartBreakfast.setStartDate(startDate);
            chartBreakfast.setDaysPassed(null);
            chartLunch.setStartDate(startDate);
            chartLunch.setDaysPassed(null);
            chartDinner.setStartDate(startDate);
            chartDinner.setDaysPassed(null);

            startBreakfast = framework.getSecondaryPreprandialStartForBreakfast();
            startLunch = framework.getSecondaryPreprandialStartForLunch();
            startDinner = framework.getSecondaryPreprandialStartForDinner();

            chartBreakfast.setMetabolicRhythmStartType(metabolicRhythm.getStartingPreprandialType());
            chartBreakfast.setMetabolicStateStartType(framework.getState().getStartingPreprandialType());

            chartLunch.setMetabolicRhythmStartType(metabolicRhythm.getStartingPreprandialType());
            chartLunch.setMetabolicStateStartType(framework.getState().getStartingPreprandialType());

            chartDinner.setMetabolicRhythmStartType(metabolicRhythm.getStartingPreprandialType());
            chartDinner.setMetabolicStateStartType(framework.getState().getStartingPreprandialType());
        }

        chartBreakfast.setStart(startBreakfast);
        chartLunch.setStart(startLunch);
        chartDinner.setStart(startDinner);

    }
}
