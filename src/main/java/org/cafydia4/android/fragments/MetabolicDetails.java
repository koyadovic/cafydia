package org.cafydia4.android.fragments;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.genericdialogfragments.DialogDatePicker;
import org.cafydia4.android.recommendations.MetabolicFramework;
import org.cafydia4.android.recommendations.MetabolicRhythm;
import org.cafydia4.android.recommendations.MetabolicRhythmSlave;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.MyToast;

import java.util.ArrayList;

/**
 * Created by user on 21/09/14.
 */
public class MetabolicDetails extends Fragment implements DialogDatePicker.OnDatePickedListener {
    private Switch sState, sSchedule;
    private ScrollView rootScrollView;
    private Integer scrollPositionX = null, scrollPositionY = null;
    private EditText etName, etDescription;

    private Button bDescriptionDone;
    private TextView tvDescription;
    private ActionBar ab;

    private LinearLayout ibModifyStart, ibModifyEnd;
    private Instant iStart, iEnd;
    private TextView tvStart, tvEnd;

    private LinearLayout lBeginnings, lCorrectives;
    private LinearLayout lState, lActivateSchedule, lSchedule, lDescription, lRootSchedule;

    private MetabolicDetailsListener mCallBack;

    private static MetabolicFramework framework;

    private int metabolicId;
    private MetabolicRhythm mMetabolicRhythm;

    public static Fragment newInstance(Integer metabolicRhythmId, MetabolicFramework f){
        MetabolicDetails fragment = new MetabolicDetails();
        framework = f;


        if(metabolicRhythmId != null) {
            Bundle args = new Bundle();
            args.putInt("metabolic_rhythm_id", metabolicRhythmId);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallBack = (MetabolicDetailsListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        metabolicId = getArguments().getInt("metabolic_rhythm_id");


        View layout = inflater.inflate(R.layout.fragment_metabolic_details, container, false);

        searchViews(layout);
        // restore the point of the scroll
        if(scrollPositionX != null && scrollPositionY != null){
            rootScrollView.post(new Runnable() {
                @Override
                public void run() {
                    rootScrollView.scrollTo(scrollPositionX, scrollPositionY);
                }
            });

        }
        updateReferenceToMetabolicRhythm();

        prepareLayoutAndSetListeners(metabolicId);
        updateUIWithMetabolicRhythmParameters();

        // set action bar title and subtitle
        ab = getActivity().getActionBar();
        if(ab != null){
            ab.setTitle(getString(R.string.metabolic_rhythms_activity_label));
            if(mMetabolicRhythm != null)
                ab.setSubtitle(mMetabolicRhythm.getName());
        }

        return layout;
    }

    @Override
    public void onStop(){
        super.onStop();
        scrollPositionX = rootScrollView.getScrollX();
        scrollPositionY = rootScrollView.getScrollY();
    }

    public void setScrollPositionX(Integer scrollPositionX) {
        this.scrollPositionX = scrollPositionX;
    }

    public void setScrollPositionY(Integer scrollPositionY) {
        this.scrollPositionY = scrollPositionY;
    }

    public void notifyStateChange(){
        updateReferenceToMetabolicRhythm();

        sState.setOnCheckedChangeListener(null);
        sState.setChecked(mMetabolicRhythm.getState().equals(C.METABOLIC_RHYTHM_STATE_ENABLED));
        sState.setOnCheckedChangeListener(sStateListener);
    }

    public void saveMetabolicRhythm(){
        /*
         * Catch the dates from the start and end schedule
         */
        if(mMetabolicRhythm.getId() != 1) {

            if(sSchedule.isChecked()) {
                iStart.setTimeToTheStartOfTheDay();
                iEnd.setTimeToTheEndOfTheDay();

                if(iStart.getDaysPassedFromInstant(iEnd) > 0){
                    mMetabolicRhythm.setStartDate(null);
                    ((MetabolicRhythmSlave) mMetabolicRhythm).setEndDate(null);
                } else {
                    mMetabolicRhythm.setStartDate(iStart);
                    ((MetabolicRhythmSlave) mMetabolicRhythm).setEndDate(iEnd);

                    ArrayList<MetabolicRhythmSlave> metabolicRhythms = framework.getPlannedSortedMetabolicRhythms();

                    for (MetabolicRhythmSlave m : metabolicRhythms) {
                        if(m.getId().equals(mMetabolicRhythm.getId())) continue;

                        if (m.appliesInInstant(iStart) ||
                                m.appliesInInstant(iEnd) ||
                                ((MetabolicRhythmSlave) mMetabolicRhythm).appliesInInstant(m.getStartDate()) ||
                                ((MetabolicRhythmSlave) mMetabolicRhythm).appliesInInstant(m.getEndDate())) {

                            mMetabolicRhythm.setStartDate(null);
                            ((MetabolicRhythmSlave) mMetabolicRhythm).setEndDate(null);
                            break;
                        }
                    }
                }
            } else {
                mMetabolicRhythm.setStartDate(null);
                ((MetabolicRhythmSlave) mMetabolicRhythm).setEndDate(null);
            }

            mMetabolicRhythm.save(framework.getConfigDatabase());

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    framework.reloadPlannedSortedMetabolicRhythms();
                }
            }, 500);

        } else {
            mMetabolicRhythm.save(framework.getConfigDatabase());
        }

    }



    private void searchViews(View layout){
        // search the views
        etName = (EditText) layout.findViewById(R.id.etName);
        etDescription = (EditText) layout.findViewById(R.id.etDescription);

        lState = (LinearLayout) layout.findViewById(R.id.lState);
        lActivateSchedule = (LinearLayout) layout.findViewById(R.id.lActivateSchedule);
        lRootSchedule = (LinearLayout) layout.findViewById(R.id.lRootSchedule);

        lDescription = (LinearLayout) layout.findViewById(R.id.lDescription);
        bDescriptionDone = (Button) layout.findViewById(R.id.bDescriptionDone);
        tvDescription = (TextView) layout.findViewById(R.id.tvDescription);

        rootScrollView = (ScrollView) layout.findViewById(R.id.rootScrollView);

        sState = (Switch) layout.findViewById(R.id.sState);
        sSchedule = (Switch) layout.findViewById(R.id.sSchedule);

        ibModifyStart = (LinearLayout) layout.findViewById(R.id.ibModifyStart);
        ibModifyEnd = (LinearLayout) layout.findViewById(R.id.ibModifyEnd);
        tvStart = (TextView) layout.findViewById(R.id.tvStart);
        tvEnd = (TextView) layout.findViewById(R.id.tvEnd);

        lSchedule = (LinearLayout) layout.findViewById(R.id.lSchedule);
        lBeginnings = (LinearLayout) layout.findViewById(R.id.lBeginnings);
        lCorrectives = (LinearLayout) layout.findViewById(R.id.lCorrectives);
    }

    public void onDatePickerCanceled(String tag){
    }

    public void onDatePicked(Instant instant, String tag){
        if(tag.equals("start")){
            iStart = instant;
            if(instant.getDaysPassedFromInstant(iEnd) > 0) {
                tvEnd.setText(iEnd.getUserDateString());
            }
            tvStart.setText(iStart.getUserDateString());

            checkScheduleDateRange(instant);
        }
        else if(tag.equals("end")){
            iEnd = instant;
            if(instant.getDaysPassedFromInstant(iStart) < 0) {
                tvStart.setText(iStart.getUserDateString());
            }
            tvEnd.setText(iEnd.getUserDateString());

            checkScheduleDateRange(instant);
        }
    }

    private void checkScheduleDateRange(Instant i){
        iStart.setTimeToTheStartOfTheDay();
        iEnd.setTimeToTheEndOfTheDay();

        if(iStart.getDaysPassedFromInstant(iEnd) > 0){
            // el final no puede ser anterior al comienzo
            iEnd.setInstant(new Instant(iStart));
            tvEnd.setText(iEnd.getUserDateString());
        } else {
            mMetabolicRhythm.setStartDate(iStart);
            ((MetabolicRhythmSlave) mMetabolicRhythm).setEndDate(iEnd);

            ArrayList<MetabolicRhythmSlave> metabolicRhythms = framework.getPlannedSortedMetabolicRhythms();

            for (MetabolicRhythmSlave m : metabolicRhythms) {
                if(m.getId().equals(mMetabolicRhythm.getId())) continue;

                if (m.appliesInInstant(i) ||
                        ((MetabolicRhythmSlave) mMetabolicRhythm).appliesInInstant(m.getStartDate()) ||
                        ((MetabolicRhythmSlave) mMetabolicRhythm).appliesInInstant(m.getEndDate())) {

                    mMetabolicRhythm.setStartDate(null);
                    ((MetabolicRhythmSlave) mMetabolicRhythm).setEndDate(null);

                    // no se puede usar la fecha por solaparse con la del ritmo metabólico tal.
                    new MyToast(getActivity(), getString(R.string.metabolic_details_date_in_use) + " " + m.getName());
                }
            }
        }

    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.ibModifyStart:
                    DialogDatePicker
                            .newInstance(iStart.toDate().getTime(), MetabolicDetails.this, "start")
                            .show(getFragmentManager(), "dialog_date_picker");
                    break;

                case R.id.ibModifyEnd:
                    DialogDatePicker
                            .newInstance(iEnd.toDate().getTime(), MetabolicDetails.this, "end")
                            .show(getFragmentManager(), "dialog_date_picker");
                    break;
            }

        }
    };


    private CompoundButton.OnCheckedChangeListener sStateListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            sState.setEnabled(false);
            boolean result = mCallBack.onRhythmActivateChangeInDetails(mMetabolicRhythm.getId(), isChecked);
            if(!result){
                sState.setOnCheckedChangeListener(null);
                sState.setChecked(! isChecked);
                sState.setOnCheckedChangeListener(this);

            } else {
                updateReferenceToMetabolicRhythm();
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sState.setEnabled(true);
                }
            },1000);
        }
    };

    private void prepareLayoutAndSetListeners(final int id){
        /*
         * NAME AND DESCRIPTION
         */

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mMetabolicRhythm.setName(s.toString());
                if (ab != null) {
                    ab.setSubtitle(s.toString());
                    mCallBack.onRhythmNameChanged(mMetabolicRhythm);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mMetabolicRhythm.setDescription(s.toString());
                tvDescription.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        switch(id){
            case 1:
                lState.setVisibility(View.GONE);
                lRootSchedule.setVisibility(View.GONE);

                break;
            default:
                /*
                 * FOR ACTIVATE OR DEACTIVATE THE METABOLIC RHYTHM
                 */
                sState.setOnCheckedChangeListener(sStateListener);

                //
                // añadido ahora
                iStart = new Instant(mMetabolicRhythm.getStartDate());
                iEnd = new Instant(((MetabolicRhythmSlave) mMetabolicRhythm).getEndDate());

        }

        ibModifyStart.setOnClickListener(buttonListener);
        ibModifyEnd.setOnClickListener(buttonListener);

        /*
         * To the description logic
         */
        lDescription.setVisibility(View.GONE);
        tvDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDescription.setVisibility(View.GONE);
                lDescription.setVisibility(View.VISIBLE);
            }
        });
        bDescriptionDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDescription.setVisibility(View.VISIBLE);
                lDescription.setVisibility(View.GONE);
            }
        });

        lBeginnings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallBack.onRequestedChangeActivityState(C.METABOLIC_ACTIVITY_STATE_BEGINNINGS_LIST, id);
            }
        });

        lCorrectives.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallBack.onRequestedChangeActivityState(C.METABOLIC_ACTIVITY_STATE_CORRECTIVES_LIST, id);
            }
        });

    }

    private void updateUIWithMetabolicRhythmParameters(){
        etName.setText(mMetabolicRhythm.getName());
        etDescription.setText(mMetabolicRhythm.getDescription());

        if(mMetabolicRhythm.getId() != 1){
            sState.setOnCheckedChangeListener(null);
            sState.setChecked(mMetabolicRhythm.getState() == C.METABOLIC_RHYTHM_STATE_ENABLED);
            sState.setOnCheckedChangeListener(sStateListener);

            Instant end = ((MetabolicRhythmSlave)mMetabolicRhythm).getEndDate();
            if(end != null && end.toDate().getTime() != 0) {
                sSchedule.setChecked(true);

                iStart.setInstant(mMetabolicRhythm.getStartDate());
                iEnd.setInstant(((MetabolicRhythmSlave) mMetabolicRhythm).getEndDate());

            } else {
                sSchedule.setChecked(false);

                lSchedule.setVisibility(View.GONE);

                iStart = new Instant();
                iEnd = new Instant();

            }

            tvStart.setText(iStart.getUserDateString());
            tvEnd.setText(iEnd.getUserDateString());

            /*
             * IF ACTIVATE THE SCHEDULE, SCROLL DOWN 500 PIXELS TO INDICATE THAT IS MORE LAYOUT DOWNWARD
             */
            sSchedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        lSchedule.setVisibility(View.VISIBLE);

                        iStart.setTimeToTheStartOfTheDay();
                        iEnd.setTimeToTheEndOfTheDay();
                        tvStart.setText(iStart.getUserDateString());
                        tvEnd.setText(iEnd.getUserDateString());

                        mMetabolicRhythm.setStartDate(iStart);
                        ((MetabolicRhythmSlave) mMetabolicRhythm).setEndDate(iEnd);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                rootScrollView.smoothScrollBy(0, 500);
                            }
                        }, 100);

                        saveMetabolicRhythm();
                    } else {
                        lSchedule.setVisibility(View.GONE);

                        mMetabolicRhythm.setStartDate(null);
                        ((MetabolicRhythmSlave) mMetabolicRhythm).setEndDate(null);

                        saveMetabolicRhythm();

                    }
                }
            });
        }
    }

    public ScrollView getRootScrollView() {
        return rootScrollView;
    }

    public void setScrollPositionXY(int scrollPositionX, int scrollPositionY) {
        this.scrollPositionX = scrollPositionX;
        this.scrollPositionY = scrollPositionY;
    }

    public interface MetabolicDetailsListener {
        void onRequestedChangeActivityState(int action, int metabolicId);
        boolean onRhythmActivateChangeInDetails(int id, boolean isChecked);
        void onRhythmNameChanged(MetabolicRhythm m);
    }

    private void updateReferenceToMetabolicRhythm(){
        framework.refresh();

        // importantísimo
        if(framework.getState().getActivatedMetabolicRhythmId() == metabolicId){
            mMetabolicRhythm = framework.getEnabledMetabolicRhythm();
        } else {
            if(framework.getSecondaryMetabolicRhythm() == null) {
                mMetabolicRhythm = framework.getConfigDatabase().getMetabolicRhythmById(metabolicId);
                framework.connectSecondaryMetabolicRhythm(mMetabolicRhythm);
            } else {
                mMetabolicRhythm = framework.getSecondaryMetabolicRhythm();
            }
        }
    }

}
