package org.cafydia4.android.initialconfiguration;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import org.cafydia4.android.R;
import org.cafydia4.android.recommendations.BaselineBasal;

/**
 * Created by user on 2/05/15.
 */
public class Fragment2BasalDoses extends FragmentOneStep {

    private BaselineBasal mBasalDoses;

    private EditText etBreakfastDose, etLunchDose, etDinnerDose;

    private CheckBox mConfirm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.initial_config_fragment_2_basal_doses, container, false);

        mBasalDoses = new BaselineBasal(getActivity());

        etBreakfastDose = (EditText) layout.findViewById(R.id.etBreakfastDose);
        etLunchDose = (EditText) layout.findViewById(R.id.etLunchDose);
        etDinnerDose = (EditText) layout.findViewById(R.id.etDinnerDose);
        mConfirm = (CheckBox) layout.findViewById(R.id.checkBox);

        if(mBasalDoses.getBasalDoseBreakfast() > 0f) {
            etBreakfastDose.setText(mBasalDoses.getBasalDoseBreakfast().toString());
        }

        if(mBasalDoses.getBasalDoseLunch() > 0f) {
            etLunchDose.setText(mBasalDoses.getBasalDoseLunch().toString());
        }

        if(mBasalDoses.getBasalDoseDinner() > 0f) {
            etDinnerDose.setText(mBasalDoses.getBasalDoseDinner().toString());
        }

        etBreakfastDose.addTextChangedListener(breakfastWatcher);
        etLunchDose.addTextChangedListener(lunchWatcher);
        etDinnerDose.addTextChangedListener(dinnerWatcher);

        mConfirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getParentActivity().updateButtons();
            }
        });


        return layout;
    }

    private TextWatcher breakfastWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = s.toString();
            if(!text.equals("")){
                mBasalDoses.setBasalBreakfastActivated(true);
                mBasalDoses.setBasalDoseBreakfast(Float.parseFloat(text));

            } else {
                mBasalDoses.setBasalBreakfastActivated(false);
                mBasalDoses.setBasalDoseBreakfast(0f);
            }
            mBasalDoses.saveOnly();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher lunchWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = s.toString();
            if(!text.equals("")){
                mBasalDoses.setBasalLunchActivated(true);
                mBasalDoses.setBasalDoseLunch(Float.parseFloat(text));

            } else {
                mBasalDoses.setBasalLunchActivated(false);
                mBasalDoses.setBasalDoseLunch(0f);
            }
            mBasalDoses.saveOnly();

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher dinnerWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = s.toString();
            if(!text.equals("")){
                mBasalDoses.setBasalDinnerActivated(true);
                mBasalDoses.setBasalDoseDinner(Float.parseFloat(text));

            } else {
                mBasalDoses.setBasalDinnerActivated(false);
                mBasalDoses.setBasalDoseDinner(0f);
            }
            mBasalDoses.saveOnly();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    public static Fragment2BasalDoses newInstance(){
        return new Fragment2BasalDoses();
    }

    public boolean canAdvance(){
        return mConfirm != null && mConfirm.isChecked();
    }
}
