package org.cafydia.android.dialogfragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.google.gson.Gson;

import org.cafydia.android.R;
import org.cafydia.android.adapters.CriteriaInstantAdapter;
import org.cafydia.android.adapters.LabelRuleAdapter;
import org.cafydia.android.chartobjects.DataCollectionCriteria;
import org.cafydia.android.chartobjects.DataCollectionCriteriaInstant;
import org.cafydia.android.chartobjects.DataCollectionLabelRule;
import org.cafydia.android.chartobjects.Label;
import org.cafydia.android.core.Annotation;
import org.cafydia.android.datadatabase.DataDatabase;
import org.cafydia.android.util.C;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by user on 10/02/15.
 */
public class DialogCriteriaEditor extends DialogFragment {

    private static View targetView;
    private DataCollectionCriteria mCriteria;
    private static OnCriteriaEditedListener mCallBack;
    private static ArrayList<Annotation> mAnnotations;
    private static DataCollectionCriteria mGlobalPageCriteria;

    private static DataDatabase db;

    private ToggleButton tbMonday, tbTuesday, tbWednesday, tbThursday, tbFriday, tbSaturday, tbSunday;
    private ToggleButton tbBeforeBreakfast, tbAfterBreakfast, tbBeforeLunch, tbAfterLunch;
    private ToggleButton tbBeforeDinner, tbAfterDinner, tbInTheNight;

    private ArrayList<ToggleButton> tbDayWeeks;
    private ArrayList<ToggleButton> tbTimes;

    private CheckBox cbSince, cbUntil, cbDayWeeks, cbTimes, cbLabels;
    private Spinner sSince, sUntil;

    private LinearLayout llDayWeeks, llTimes, llLabels;

    private ImageButton ibAddLabel;
    private RadioGroup rgLabels;
    private RadioButton rbExcludeLabels, rbIncludeLabels;

    private ListView lvLabelsSelected;
    private LabelRuleAdapter labelAdapter;

    private void refreshUI(){
        boolean expanded;
        boolean enabled;

        ArrayList<DataCollectionCriteriaInstant> criteriaInstants = new ArrayList<>();

        // 7 días atrás
        criteriaInstants.add(
                new DataCollectionCriteriaInstant(
                        getActivity(),
                        db,
                        C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE,
                        -7)
        );
        // dos semanas atrás
        criteriaInstants.add(
                new DataCollectionCriteriaInstant(
                        getActivity(),
                        db,
                        C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE,
                        -14)
        );
        // un mes atrás
        criteriaInstants.add(
                new DataCollectionCriteriaInstant(
                        getActivity(),
                        db,
                        C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE,
                        -30)
        );
        // dos meses atrás
        criteriaInstants.add(
                new DataCollectionCriteriaInstant(
                        getActivity(),
                        db,
                        C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE,
                        -60)
        );
        // tres meses atrás
        criteriaInstants.add(
                new DataCollectionCriteriaInstant(
                        getActivity(),
                        db,
                        C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE,
                        -90)
        );

        for(Annotation a : mAnnotations){
            criteriaInstants.add(
                    new DataCollectionCriteriaInstant(
                            getActivity(),
                            db,
                            C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION,
                            a.getId()
                    )
            );
        }

        // los ordenamos cronológicamente
        Collections.sort(criteriaInstants, new CriteriaInstantComparator());

        final CriteriaInstantAdapter sinceAdapter = new CriteriaInstantAdapter(criteriaInstants, getActivity());
        final CriteriaInstantAdapter untilAdapter = new CriteriaInstantAdapter(criteriaInstants, getActivity());

        sSince.setAdapter(sinceAdapter);
        sUntil.setAdapter(untilAdapter);

        //
        // Since
        //
        enabled = ! (mGlobalPageCriteria != null && mGlobalPageCriteria.getSince().getData() != 0);


        cbSince.setEnabled(enabled);
        cbSince.setChecked(enabled);
        sSince.setEnabled(enabled);

        if(enabled) {
            expanded = mCriteria.getSince().getData() != 0;
            cbSince.setChecked(expanded);
            sSince.setVisibility(expanded ? View.VISIBLE : View.GONE);

            for(int a = 0; a < sSince.getAdapter().getCount(); a++){
                DataCollectionCriteriaInstant i = (DataCollectionCriteriaInstant) sSince.getAdapter().getItem(a);
                if(mCriteria.getSince().getType().equals(i.getType()) && mCriteria.getSince().getData().equals(i.getData())){
                    sSince.setSelection(a);
                }
            }
        } else {
            expanded = mGlobalPageCriteria.getSince().getData() != 0;
            cbSince.setChecked(expanded);
            sSince.setVisibility(expanded ? View.VISIBLE : View.GONE);

            for(int a = 0; a < sSince.getAdapter().getCount(); a++){
                DataCollectionCriteriaInstant i = (DataCollectionCriteriaInstant) sSince.getAdapter().getItem(a);
                if(mGlobalPageCriteria.getSince().getType().equals(i.getType()) && mGlobalPageCriteria.getSince().getData().equals(i.getData())){
                    sSince.setSelection(a);
                }
            }
        }

        //
        // Until
        //
        enabled = ! (mGlobalPageCriteria != null && mGlobalPageCriteria.getUntil().getData() != 0);

        cbUntil.setEnabled(enabled);
        cbUntil.setChecked(enabled);
        sUntil.setEnabled(enabled);

        if(enabled) {
            expanded = mCriteria.getUntil().getData() != 0;
            cbUntil.setChecked(expanded);
            sUntil.setVisibility(expanded ? View.VISIBLE : View.GONE);

            for(int a = 0; a < sUntil.getAdapter().getCount(); a++){
                DataCollectionCriteriaInstant i = (DataCollectionCriteriaInstant) sUntil.getAdapter().getItem(a);
                if(mCriteria.getUntil().getType().equals(i.getType()) && mCriteria.getUntil().getData().equals(i.getData())){
                    sUntil.setSelection(a);
                }
            }

        } else {
            expanded = mGlobalPageCriteria.getUntil().getData() != 0;
            cbUntil.setChecked(expanded);
            sUntil.setVisibility(expanded ? View.VISIBLE : View.GONE);

            for(int a = 0; a < sUntil.getAdapter().getCount(); a++){
                DataCollectionCriteriaInstant i = (DataCollectionCriteriaInstant) sUntil.getAdapter().getItem(a);
                if(mGlobalPageCriteria.getUntil().getType().equals(i.getType()) && mGlobalPageCriteria.getUntil().getData().equals(i.getData())){
                    sUntil.setSelection(a);
                }
            }
        }

        //
        // for day weeks
        //
        expanded = (mGlobalPageCriteria != null && mGlobalPageCriteria.getDayWeeksActivated().equals(C.DATA_COLLECTION_CRITERIA_ACTIVATED)) ||
                (mCriteria != null && mCriteria.getDayWeeksActivated().equals(C.DATA_COLLECTION_CRITERIA_ACTIVATED));

        enabled = ! (mGlobalPageCriteria != null && mGlobalPageCriteria.getDayWeeksActivated().equals(C.DATA_COLLECTION_CRITERIA_ACTIVATED));

        if(enabled) {
            for(int a = 0; a < 7; a++) {
                if(tbDayWeeks.get(a).isEnabled()) {
                    boolean b = mCriteria.collectOnDayWeek(a);
                    tbDayWeeks.get(a).setChecked(b);
                }
            }
        } else {
            for(int a = 0; a < 7; a++) {
                boolean c = mGlobalPageCriteria.collectOnDayWeek(a);
                tbDayWeeks.get(a).setChecked(c);
                tbDayWeeks.get(a).setEnabled(false);
            }
            cbDayWeeks.setEnabled(false);
        }

        cbDayWeeks.setChecked(expanded);
        llDayWeeks.setVisibility(expanded ? View.VISIBLE : View.GONE);

        //
        // for times
        //
        expanded = (mGlobalPageCriteria != null && mGlobalPageCriteria.getMealTimesActivated().equals(C.DATA_COLLECTION_CRITERIA_ACTIVATED)) ||
                (mCriteria != null && mCriteria.getMealTimesActivated().equals(C.DATA_COLLECTION_CRITERIA_ACTIVATED));

        enabled = ! (mGlobalPageCriteria != null && mGlobalPageCriteria.getMealTimesActivated().equals(C.DATA_COLLECTION_CRITERIA_ACTIVATED));

        if(enabled) {
            for(int a = 0; a < 7; a++) {
                if(tbTimes.get(a).isEnabled()) {
                    boolean b = mCriteria.collectOnMealTime(a);
                    tbTimes.get(a).setChecked(b);
                }
            }
        } else {
            for(int a = 0; a < 7; a++) {
                boolean c = mGlobalPageCriteria.collectOnMealTime(a);
                tbTimes.get(a).setChecked(c);
                tbTimes.get(a).setEnabled(false);
            }
            cbTimes.setEnabled(false);
        }

        cbTimes.setChecked(expanded);
        llTimes.setVisibility(expanded ? View.VISIBLE : View.GONE);

        //
        // For Labels
        //
        if(mGlobalPageCriteria != null && mGlobalPageCriteria.getLabelRules().size() > 0) {
            cbLabels.setChecked(true);
            llLabels.setVisibility(View.VISIBLE);
            if(mGlobalPageCriteria.getLabelRules().get(0).getAction() == C.LABEL_RULE_ACTION_EXCLUDE){
                rbExcludeLabels.setChecked(true);
            } else {
                rbIncludeLabels.setChecked(true);
            }
            cbLabels.setEnabled(false);
            rbExcludeLabels.setEnabled(false);
            rbIncludeLabels.setEnabled(false);
            ibAddLabel.setEnabled(false);

        } else {
            expanded = mCriteria.getLabelRules() != null && mCriteria.getLabelRules().size() > 0;
            cbLabels.setChecked(expanded);
            llLabels.setVisibility(expanded ? View.VISIBLE : View.GONE);
            if (expanded && mCriteria.getLabelRules().get(0).getAction() == C.LABEL_RULE_ACTION_EXCLUDE) {
                rbExcludeLabels.setChecked(true);
            } else {
                rbIncludeLabels.setChecked(true);
            }
        }

    }

    private CompoundButton.OnCheckedChangeListener listenerToggleButton = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()){
                case R.id.tbMonday:
                case R.id.tbTuesday:
                case R.id.tbWednesday:
                case R.id.tbThursday:
                case R.id.tbFriday:
                case R.id.tbSaturday:
                case R.id.tbSunday:
                    if(cbDayWeeks.isEnabled()) {
                        mCriteria.setCollectOnDayWeek(tbDayWeeks.indexOf(buttonView), isChecked);
                    }
                    break;

                case R.id.tbBeforeBreakfast:
                case R.id.tbAfterBreakfast:
                case R.id.tbBeforeLunch:
                case R.id.tbAfterLunch:
                case R.id.tbBeforeDinner:
                case R.id.tbAfterDinner:
                case R.id.tbInTheNight:
                    if(cbTimes.isEnabled()) {
                        mCriteria.setCollectOnGlucoseTime(tbTimes.indexOf(buttonView), isChecked);
                    }
                    break;

            }
        }
    };

    private CompoundButton.OnCheckedChangeListener listenerCheckBox = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()){
                case R.id.cbSince:
                    sSince.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    break;

                case R.id.cbUntil:
                    sUntil.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    break;

                case R.id.cbDayWeeks:
                    mCriteria.setDayWeeksActivated(isChecked ? C.DATA_COLLECTION_CRITERIA_ACTIVATED : C.DATA_COLLECTION_CRITERIA_NO_ACTIVATED);
                    llDayWeeks.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    break;

                case R.id.cbTimes:
                    mCriteria.setMealTimesActivated(isChecked ? C.DATA_COLLECTION_CRITERIA_ACTIVATED : C.DATA_COLLECTION_CRITERIA_NO_ACTIVATED);
                    llTimes.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    break;

                case R.id.cbLabels:
                    llLabels.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    break;
            }
        }
    };

    private class CriteriaInstantComparator implements Comparator<DataCollectionCriteriaInstant> {
        public int compare(DataCollectionCriteriaInstant left, DataCollectionCriteriaInstant right) {
            return left.getInstant().getDaysPassedFromInstant(right.getInstant()) > 0 ? -1 : 1;
        }
    }

    private void setListeners(){


        sSince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(cbSince.isEnabled()) {
                    DataCollectionCriteriaInstant i = (DataCollectionCriteriaInstant) sSince.getAdapter().getItem(position);
                    mCriteria.setSince(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sUntil.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(cbUntil.isEnabled()) {
                    DataCollectionCriteriaInstant i = (DataCollectionCriteriaInstant) sUntil.getAdapter().getItem(position);
                    mCriteria.setUntil(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //
        // the toggle buttons
        //
        for(ToggleButton button : tbDayWeeks){
            button.setOnCheckedChangeListener(listenerToggleButton);
        }
        tbAfterBreakfast.setOnCheckedChangeListener(listenerToggleButton);
        tbBeforeBreakfast.setOnCheckedChangeListener(listenerToggleButton);

        for(ToggleButton button : tbTimes){
            button.setOnCheckedChangeListener(listenerToggleButton);
        }

        //
        // checkboxes
        //
        cbSince.setOnCheckedChangeListener(listenerCheckBox);
        cbUntil.setOnCheckedChangeListener(listenerCheckBox);
        cbDayWeeks.setOnCheckedChangeListener(listenerCheckBox);
        cbTimes.setOnCheckedChangeListener(listenerCheckBox);
        cbLabels.setOnCheckedChangeListener(listenerCheckBox);

        //
        // Spinners
        //

        //
        // RadioGroup y RadioButtons
        //
        rgLabels.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(mCriteria.getLabelRules() != null && mCriteria.getLabelRules().size() > 0) {
                    switch (checkedId) {
                        case R.id.rbIncludeLabels:
                            for(DataCollectionLabelRule rule : mCriteria.getLabelRules()){
                                rule.setAction(C.LABEL_RULE_ACTION_INCLUDE);
                                rule.save(db);
                            }
                            break;
                        case R.id.rbExcludeLabels:
                            for(DataCollectionLabelRule rule : mCriteria.getLabelRules()){
                                rule.setAction(C.LABEL_RULE_ACTION_EXCLUDE);
                                rule.save(db);
                            }
                            break;
                    }
                }
            }
        });

        //
        // ListView
        //
        if(mGlobalPageCriteria != null) {
            if(mGlobalPageCriteria.getLabelRules() != null && mGlobalPageCriteria.getLabelRules().size() > 0) {
                labelAdapter = new LabelRuleAdapter(getActivity(), mGlobalPageCriteria.getLabelRules());
                lvLabelsSelected.setEnabled(false);
            } else {
                if(mCriteria.getLabelRules() != null && mCriteria.getLabelRules().size() > 0) {
                    labelAdapter = new LabelRuleAdapter(getActivity(), mCriteria.getLabelRules());
                } else {
                    labelAdapter = new LabelRuleAdapter(getActivity(), new ArrayList<DataCollectionLabelRule>());
                }
            }
        } else {
            if(mCriteria.getLabelRules() != null && mCriteria.getLabelRules().size() > 0) {
                labelAdapter = new LabelRuleAdapter(getActivity(), mCriteria.getLabelRules());
            } else {
                labelAdapter = new LabelRuleAdapter(getActivity(), new ArrayList<DataCollectionLabelRule>());
            }
        }


        // thanks to Moisés Olmedo and bwest for http://stackoverflow.com/a/14577399
        lvLabelsSelected.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        lvLabelsSelected.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ya pensaremos qué cojones hacemos con esta mierda
                Label l = labelAdapter.getItem(position).getLabel();
                DialogAddLabel.newInstance(l.getId(), l.getTitle(), l.getColor()).show(getFragmentManager(), "dialog_edit_label");
            }
        });

        lvLabelsSelected.setAdapter(labelAdapter);

        registerForContextMenu(lvLabelsSelected);


        // salta el dialogo para seleccionar un label entre todos los que existen en la db
        ibAddLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogSelectLabel.newInstance(db, new DialogSelectLabel.OnLabelSelectedListener() {
                    @Override
                    public void onLabelSelected(Label l) {
                        if(mCriteria.getId().equals(0)) {
                            mCriteria.save(db);
                            mCriteria = db.getLastDataCollectionCriteriaAdded();
                        }

                        int action = rbExcludeLabels.isChecked() ? C.LABEL_RULE_ACTION_EXCLUDE : C.LABEL_RULE_ACTION_INCLUDE;
                        DataCollectionLabelRule rule = new DataCollectionLabelRule(
                                0,
                                mCriteria.getId(),
                                action,
                                l
                        );
                        rule.save(db);

                        if(rule.getId().equals(0)) {
                            rule = db.getLastDataCollectionLabelRuleAdded();
                        }

                        labelAdapter.addRule(rule);
                    }
                }).show(getActivity().getFragmentManager(), null);
            }
        });

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// Contextual menu /////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if(v.getId() == R.id.lvLabelsSelected) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.dialog_criteria_editor_listview_contextual_menu, menu);

            // ñapa
            MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    onContextItemSelected(item);
                    return true;
                }
            };

            for (int i = 0, n = menu.size(); i < n; i++)
                menu.getItem(i).setOnMenuItemClickListener(listener);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        // label clickada
        DataCollectionLabelRule r = labelAdapter.getItem(info.position);

        switch (item.getItemId()){
            case R.id.edit_label_from_list:
                Label l = r.getLabel();
                DialogAddLabel.newInstance(l.getId(), l.getTitle(), l.getColor()).show(getActivity().getFragmentManager(), "dialog_edit_label");
                return true;

            case R.id.delete_label_from_list:
                r.delete(db);
                labelAdapter.removeRule(r);
                return true;
        }

        return super.onContextItemSelected(item);
    }

    private void searchViewsInTheUi(View view){
        tbMonday = (ToggleButton) view.findViewById(R.id.tbMonday);
        tbTuesday = (ToggleButton) view.findViewById(R.id.tbTuesday);
        tbWednesday = (ToggleButton) view.findViewById(R.id.tbWednesday);
        tbThursday = (ToggleButton) view.findViewById(R.id.tbThursday);
        tbFriday = (ToggleButton) view.findViewById(R.id.tbFriday);
        tbSaturday = (ToggleButton) view.findViewById(R.id.tbSaturday);
        tbSunday = (ToggleButton) view.findViewById(R.id.tbSunday);

        tbDayWeeks = new ArrayList<>();
        tbDayWeeks.add(tbMonday);
        tbDayWeeks.add(tbTuesday);
        tbDayWeeks.add(tbWednesday);
        tbDayWeeks.add(tbThursday);
        tbDayWeeks.add(tbFriday);
        tbDayWeeks.add(tbSaturday);
        tbDayWeeks.add(tbSunday);

        tbBeforeBreakfast = (ToggleButton) view.findViewById(R.id.tbBeforeBreakfast);
        tbAfterBreakfast = (ToggleButton) view.findViewById(R.id.tbAfterBreakfast);
        tbBeforeLunch = (ToggleButton) view.findViewById(R.id.tbBeforeLunch);
        tbAfterLunch = (ToggleButton) view.findViewById(R.id.tbAfterLunch);
        tbBeforeDinner = (ToggleButton) view.findViewById(R.id.tbBeforeDinner);
        tbAfterDinner = (ToggleButton) view.findViewById(R.id.tbAfterDinner);
        tbInTheNight = (ToggleButton) view.findViewById(R.id.tbInTheNight);

        tbTimes = new ArrayList<>();
        tbTimes.add(tbBeforeBreakfast);
        tbTimes.add(tbAfterBreakfast);
        tbTimes.add(tbBeforeLunch);
        tbTimes.add(tbAfterLunch);
        tbTimes.add(tbBeforeDinner);
        tbTimes.add(tbAfterDinner);
        tbTimes.add(tbInTheNight);

        cbSince = (CheckBox) view.findViewById(R.id.cbSince);
        cbUntil = (CheckBox) view.findViewById(R.id.cbUntil);
        cbDayWeeks = (CheckBox) view.findViewById(R.id.cbDayWeeks);
        cbTimes = (CheckBox) view.findViewById(R.id.cbTimes);
        cbLabels = (CheckBox) view.findViewById(R.id.cbLabels);

        sSince = (Spinner) view.findViewById(R.id.sSince);
        sUntil = (Spinner) view.findViewById(R.id.sUntil);

        llDayWeeks = (LinearLayout) view.findViewById(R.id.llDayWeeks);
        llTimes = (LinearLayout) view.findViewById(R.id.llTimes);
        llLabels = (LinearLayout) view.findViewById(R.id.llLabels);

        ibAddLabel = (ImageButton) view.findViewById(R.id.ibAddLabel);
        rgLabels = (RadioGroup) view.findViewById(R.id.rgLabels);

        rbExcludeLabels = (RadioButton) view.findViewById(R.id.rbExcludeLabels);
        rbIncludeLabels = (RadioButton) view.findViewById(R.id.rbIncludeLabels);

        lvLabelsSelected = (ListView) view.findViewById(R.id.lvLabelsSelected);

    }

    private void processArguments(Bundle args) {
        if(!args.isEmpty()){
            Gson gson = new Gson();
            ArrayList<DataCollectionLabelRule> labelRules = gson.fromJson(args.getString("label_rules"), C.TYPE_TOKEN_TYPE_ARRAY_LIST_DATA_COLLECTION_LABEL_RULE);

            DataCollectionCriteriaInstant sin = new DataCollectionCriteriaInstant(getActivity(), db, args.getInt("since_type"), args.getLong("since"));
            DataCollectionCriteriaInstant unt = new DataCollectionCriteriaInstant(getActivity(), db, args.getInt("until_type"), args.getLong("until"));


            mCriteria = new DataCollectionCriteria (
                    args.getInt("id"),
                    sin,
                    unt,
                    args.getInt("day_weeks_activated"),
                    args.getInt("day_weeks"),
                    args.getInt("times_activated"),
                    args.getInt("times")
            );
            mCriteria.setLabelRules(labelRules);


        } else {
            mCriteria = new DataCollectionCriteria (getActivity());
        }
    }


    public static DialogCriteriaEditor newInstance(ArrayList<Annotation> annotations, DataCollectionCriteria globalPageCriteria, DataCollectionCriteria criteria, View target, DataDatabase d, OnCriteriaEditedListener callback){
        DialogCriteriaEditor dialog = new DialogCriteriaEditor();
        Bundle args = new Bundle();

        if(criteria != null) {
            args.putInt("id", criteria.getId());

            args.putInt("since_type", criteria.getSince().getType());
            args.putLong("since", criteria.getSince().getData());

            args.putInt("until_type", criteria.getUntil().getType());
            args.putLong("until", criteria.getUntil().getData());

            args.putInt("day_weeks_activated", criteria.getDayWeeksActivated());
            args.putInt("day_weeks", criteria.getDayWeeksInteger());

            args.putInt("times_activated", criteria.getMealTimesActivated());
            args.putInt("times", criteria.getMealTimeInteger());

            Gson gson = new Gson();
            args.putString("label_rules", gson.toJson(criteria.getLabelRules()));

        }

        mGlobalPageCriteria = globalPageCriteria;


        mAnnotations = annotations;

        dialog.setArguments(args);

        db = d;
        mCallBack = callback;
        targetView = target;

        return dialog;

    }

    public interface OnCriteriaEditedListener {
        void onCriteriaEdited(DataCollectionCriteria c, View targetView);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                R.string.dialog_data_collection_criteria_editor_title
        );
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_data_collection_criteria_editor, null);

        // private methods to manage dialog behavior
        processArguments(getArguments());
        searchViewsInTheUi(view);
        refreshUI();
        setListeners();


        builder.setView(view);

        builder.setPositiveButton(R.string.dialog_data_collection_criteria_editor_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!cbSince.isChecked()){
                    mCriteria.setSince(new DataCollectionCriteriaInstant(getActivity(), db));
                }

                if(!cbUntil.isChecked()){
                    mCriteria.setUntil(new DataCollectionCriteriaInstant(getActivity(), db));

                }

                if(!cbDayWeeks.isChecked()) {
                    mCriteria.setDayWeeksInteger(0);
                }

                if(!cbTimes.isChecked()) {
                    mCriteria.setMealTimesInteger(0);
                }

                if(mCriteria.getId().equals(0)) {
                    mCriteria.save(db);
                    mCriteria = db.getLastDataCollectionCriteriaAdded();
                }

                if(!cbLabels.isChecked()) {
                    db.deleteDataCollectionLabelRuleByCriteriaId(mCriteria.getId());
                    mCriteria.setLabelRules(null);
                }

                mCriteria.save(db);

                if(mCallBack != null) {
                    mCallBack.onCriteriaEdited(mCriteria, targetView);
                }

                targetView = null;
            }
        });

        builder.setNegativeButton(R.string.dialog_data_collection_criteria_editor_button_cancel, null);

        return builder.create();
    }
}
