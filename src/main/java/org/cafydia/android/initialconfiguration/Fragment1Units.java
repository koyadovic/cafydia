package org.cafydia.android.initialconfiguration;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.cafydia.android.R;

/**
 * Created by user on 2/05/15.
 */
public class Fragment1Units extends FragmentOneStep {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.initial_config_fragment_1_units, container, false);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        int valueWeight = Integer.parseInt(sp.getString("pref_key_units_weight", "0"));
        int valueGlucose = Integer.parseInt(sp.getString("pref_key_units_glucose", "0"));

        if(valueGlucose == 0) {
            ((RadioButton) layout.findViewById(R.id.rbMgDl)).setChecked(true);
        }
        else if(valueGlucose == 1){
            ((RadioButton) layout.findViewById(R.id.rbMmolL)).setChecked(true);
        }


        if(valueWeight == 0) {
            ((RadioButton) layout.findViewById(R.id.rbGrams)).setChecked(true);
        }
        else if(valueWeight == 1) {
            ((RadioButton) layout.findViewById(R.id.rbPounds)).setChecked(true);
        }
        else if(valueWeight == 2){
            ((RadioButton) layout.findViewById(R.id.rbOunces)).setChecked(true);
        }

        ((RadioGroup) layout.findViewById(R.id.rgGlucose)).setOnCheckedChangeListener(mRadioGroupListener);
        ((RadioGroup) layout.findViewById(R.id.rgWeight)).setOnCheckedChangeListener(mRadioGroupListener);

        return layout;
    }

    private RadioGroup.OnCheckedChangeListener mRadioGroupListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = sp.edit();

            switch (group.getId()){
                case R.id.rgGlucose:
                    switch (checkedId){
                        case R.id.rbMgDl:
                            editor.putString("pref_key_units_glucose", 0 + "");
                            editor.apply();
                            break;

                        case R.id.rbMmolL:
                            editor.putString("pref_key_units_glucose", 1 + "");
                            editor.apply();
                            break;
                    }
                    break;

                case R.id.rgWeight:
                    switch (checkedId) {
                        case R.id.rbGrams:
                            editor.putString("pref_key_units_weight", 0 + "");
                            editor.apply();
                            break;

                        case R.id.rbPounds:
                            editor.putString("pref_key_units_weight", 1 + "");
                            editor.apply();
                            break;

                        case R.id.rbOunces:
                            editor.putString("pref_key_units_weight", 2 + "");
                            editor.apply();
                            break;


                    }
                    break;
            }
        }
    };

    public static Fragment1Units newInstance(){
        return new Fragment1Units();
    }

    public boolean canAdvance(){
        return true;
    }
}
