package org.cafydia4.android.initialconfiguration;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import org.cafydia4.android.R;

/**
 * Created by user on 2/05/15.
 */
public class Fragment3CorrectionFactor extends FragmentOneStep {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View layout = inflater.inflate(R.layout.initial_config_fragment_3_correction_factor, container, false);

        CheckBox cbAboveActivated = (CheckBox) layout.findViewById(R.id.cbAboveActivated);
        CheckBox cbBelowActivated = (CheckBox) layout.findViewById(R.id.cbBelowActivated);
        final Spinner spinnerAboveLevel = (Spinner) layout.findViewById(R.id.spinnerAboveLevel);
        final Spinner spinnerBelowLevel = (Spinner) layout.findViewById(R.id.spinnerBelowLevel);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean correctionFactorAboveActivated = sp.getBoolean("pref_key_correction_factor_above_activated", false);
        boolean correctionFactorBelowActivated = sp.getBoolean("pref_key_correction_factor_below_activated", false);

        float correctionFactorAboveValue = Float.parseFloat(sp.getString("pref_key_correction_factor_above", "100"));
        float correctionFactorBelowValue = Float.parseFloat(sp.getString("pref_key_correction_factor_below", "100"));

        cbAboveActivated.setChecked(correctionFactorAboveActivated);
        cbBelowActivated.setChecked(correctionFactorBelowActivated);

        spinnerAboveLevel.setEnabled(correctionFactorAboveActivated);
        spinnerBelowLevel.setEnabled(correctionFactorBelowActivated);

        ArrayAdapter<String> aboveSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.glucoses_above_correction_factor));
        ArrayAdapter<String> belowSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.glucoses_below_correction_factor));

        spinnerAboveLevel.setAdapter(aboveSpinnerAdapter);
        spinnerBelowLevel.setAdapter(belowSpinnerAdapter);

        spinnerAboveLevel.setSelection(6 - (int) ((correctionFactorAboveValue - 100) / 10));
        spinnerBelowLevel.setSelection(10 - (int) (correctionFactorBelowValue / 10));


        cbAboveActivated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sp.edit();

                editor.putBoolean("pref_key_correction_factor_above_activated", isChecked);
                editor.apply();

                spinnerAboveLevel.setEnabled(isChecked);
            }
        });

        cbBelowActivated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sp.edit();

                editor.putBoolean("pref_key_correction_factor_below_activated", isChecked);
                editor.apply();

                spinnerBelowLevel.setEnabled(isChecked);
            }
        });

        spinnerAboveLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sp.edit();

                editor.putString("pref_key_correction_factor_above", (int) (160 - (position * 10)) + "");
                editor.apply();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerBelowLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sp.edit();

                editor.putString("pref_key_correction_factor_below", (int) (100 - (position * 10)) + "");
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cbAboveActivated.setChecked(true);
        cbBelowActivated.setChecked(true);


        return layout;

    }


    public static Fragment3CorrectionFactor newInstance(){
        return new Fragment3CorrectionFactor();
    }

    public boolean canAdvance(){
        return true;
    }
}
