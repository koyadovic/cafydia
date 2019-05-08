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
import android.widget.LinearLayout;

import org.cafydia4.android.R;
import org.cafydia4.android.configdatabase.ConfigurationDatabase;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.dialogfragments.DialogDotEditor;
import org.cafydia4.android.dialogfragments.DialogDotSelector;
import org.cafydia4.android.recommendations.BaselineBasal;
import org.cafydia4.android.recommendations.MetabolicFramework;
import org.cafydia4.android.recommendations.MetabolicRhythm;
import org.cafydia4.android.recommendations.ModificationStart;
import org.cafydia4.android.recommendations.ModificationStartDot;
import org.cafydia4.android.util.C;
import org.cafydia4.android.views.ModificationStartView;

/**
 * Created by user on 27/09/14.
 */
public class MetabolicBeginningsDetailsBasal extends Fragment {
    private BaselineBasal baselineBasal;
    private ModificationStart startBreakfast, startLunch, startDinner;
    private ModificationStartView chartBreakfast, chartLunch, chartDinner;

    private ModificationStartView targetChart = null;
    private ModificationStart targetStart = null;

    private static MetabolicFramework framework;
    private int metabolicRhythmId;
    private Integer targetType = null;

    private MetabolicRhythm metabolicRhythm;

    public static Fragment newInstance(Integer metabolicRhythmId, MetabolicFramework f){
        MetabolicBeginningsDetailsBasal fragment = new MetabolicBeginningsDetailsBasal();
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
        View layout = inflater.inflate(R.layout.fragment_metabolic_beginnings_details_basal, container, false);

        metabolicRhythmId = getArguments().getInt("metabolic_rhythm_id");

        LinearLayout lBreakfast = (LinearLayout) layout.findViewById(R.id.lBreakfast);
        LinearLayout lLunch = (LinearLayout) layout.findViewById(R.id.lLunch);
        LinearLayout lDinner = (LinearLayout) layout.findViewById(R.id.lDinner);

        baselineBasal = new BaselineBasal(getActivity());

        chartBreakfast = (ModificationStartView) layout.findViewById(R.id.chartBreakfast);
        chartLunch = (ModificationStartView) layout.findViewById(R.id.chartLunch);
        chartDinner = (ModificationStartView) layout.findViewById(R.id.chartDinner);

        chartBreakfast.setMetabolicStateStartType(C.STARTING_TYPE_SPECIFIC);
        chartBreakfast.setMetabolicRhythmStartType(C.STARTING_TYPE_SPECIFIC);
        chartLunch.setMetabolicStateStartType(C.STARTING_TYPE_SPECIFIC);
        chartLunch.setMetabolicRhythmStartType(C.STARTING_TYPE_SPECIFIC);
        chartDinner.setMetabolicStateStartType(C.STARTING_TYPE_SPECIFIC);
        chartDinner.setMetabolicRhythmStartType(C.STARTING_TYPE_SPECIFIC);

        chartBreakfast.white();
        chartLunch.white();
        chartDinner.white();

        registerForContextMenu(chartBreakfast);
        registerForContextMenu(chartLunch);
        registerForContextMenu(chartDinner);

        if(!baselineBasal.isBasalBreakfastActivated() || baselineBasal.getBasalDoseBreakfast() == 0f){
            lBreakfast.setVisibility(View.GONE);
        }

        if(!baselineBasal.isBasalLunchActivated() || baselineBasal.getBasalDoseLunch() == 0f){
            lLunch.setVisibility(View.GONE);
        }

        if(!baselineBasal.isBasalDinnerActivated() || baselineBasal.getBasalDoseDinner() == 0f){
            lDinner.setVisibility(View.GONE);
        }

        /*
         * First load the metabolic framework
         * and then, in postExecute, all the other objects
         * that depends on it.
         */
        new LoadBeginnings().execute();


        return layout;

    }

    /*
     * Contextual menu
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();

        inflater.inflate(R.menu.long_click_chart_menu, menu);

        switch(v.getId()){
            case R.id.chartBreakfast:
                targetChart = chartBreakfast;
                targetStart = startBreakfast;
                targetType = C.DOT_TYPE_BASAL_INSULIN_BREAKFAST;
                break;
            case R.id.chartLunch:
                targetChart = chartLunch;
                targetStart = startLunch;
                targetType = C.DOT_TYPE_BASAL_INSULIN_LUNCH;
                break;
            case R.id.chartDinner:
                targetChart = chartDinner;
                targetStart = startDinner;
                targetType = C.DOT_TYPE_BASAL_INSULIN_DINNER;
                break;

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        FragmentManager fm = getFragmentManager();

        if(targetChart != null && targetStart != null) {
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

    private class LoadBeginnings extends AsyncTask<Void, Integer, Boolean> {
        protected Boolean doInBackground(Void... params){
            if(getActivity() == null) {
                return false;
            }

            updateReferenceToMetabolicRhythm();
            framework.reassembleStarts();

            if(metabolicRhythm.getState().equals(C.METABOLIC_RHYTHM_STATE_ENABLED)){
                startBreakfast = framework.getEnabledBasalStartForBreakfast();
                startLunch = framework.getEnabledBasalStartForLunch();
                startDinner = framework.getEnabledBasalStartForDinner();
            } else {
                startBreakfast = framework.getSecondaryBasalStartForBreakfast();
                startLunch = framework.getSecondaryBasalStartForLunch();
                startDinner = framework.getSecondaryBasalStartForDinner();
            }
            return true;
        }

        protected void onPostExecute(Boolean result){
            if (result){
                // set action bar title and subtitle
                if(getActivity() != null) {
                    ActionBar ab = getActivity().getActionBar();
                    if (ab != null) {
                        ab.setSubtitle(metabolicRhythm.getName());
                    }

                    notifyStateChange();
                }
            }
        }
    }

    public void refreshDots(ModificationStartDot dot){
        ConfigurationDatabase db = framework.getConfigDatabase();
        ModificationStart start = db.getModificationStart(metabolicRhythm.getId(), dot.getType());

        switch(dot.getType()){
            case C.DOT_TYPE_BASAL_INSULIN_BREAKFAST:
                metabolicRhythm.setBasalModificationStartBreakfast(start);
                break;
            case C.DOT_TYPE_BASAL_INSULIN_LUNCH:
                metabolicRhythm.setBasalModificationStartLunch(start);
                break;
            case C.DOT_TYPE_BASAL_INSULIN_DINNER:
                metabolicRhythm.setBasalModificationStartDinner(start);
                break;
        }
        new LoadBeginnings().execute();
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

        if(metabolicRhythm.getState().equals(C.METABOLIC_RHYTHM_STATE_ENABLED)) {
            Instant startDate = new Instant(framework.getState().getActivatedMetabolicRhythmStartDate()).setTimeToTheStartOfTheDay();
            chartBreakfast.setStartDate(startDate);
            chartBreakfast.setDaysPassed(new Instant().setTimeToTheStartOfTheDay().getDaysPassedFromInstant(startDate));
            chartLunch.setStartDate(startDate);
            chartLunch.setDaysPassed(new Instant().setTimeToTheStartOfTheDay().getDaysPassedFromInstant(startDate));
            chartDinner.setStartDate(startDate);
            chartDinner.setDaysPassed(new Instant().setTimeToTheStartOfTheDay().getDaysPassedFromInstant(startDate));

            startBreakfast = framework.getEnabledBasalStartForBreakfast();
            startLunch = framework.getEnabledBasalStartForLunch();
            startDinner = framework.getEnabledBasalStartForDinner();
        } else {
            Instant startDate = new Instant().setTimeToTheStartOfTheDay();
            chartBreakfast.setStartDate(startDate);
            chartBreakfast.setDaysPassed(null);
            chartLunch.setStartDate(startDate);
            chartLunch.setDaysPassed(null);
            chartDinner.setStartDate(startDate);
            chartDinner.setDaysPassed(null);

            startBreakfast = framework.getSecondaryBasalStartForBreakfast();
            startLunch = framework.getSecondaryBasalStartForLunch();
            startDinner = framework.getSecondaryBasalStartForDinner();
        }

        chartBreakfast.setStart(startBreakfast);
        chartLunch.setStart(startLunch);
        chartDinner.setStart(startDinner);
    }

}
