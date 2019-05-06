package org.cafydia.android.fragments;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.Log;

import org.cafydia.android.R;
import org.cafydia.android.activities.ActivityMealsSnacks;
import org.cafydia.android.genericdialogfragments.DialogConfirmation;
import org.cafydia.android.mealalarms.SetMealAlarmsService;
import org.cafydia.android.tutorial.Tutorial;
import org.cafydia.android.util.C;
import org.cafydia.android.util.MyToast;
import org.cafydia.android.util.UnitChanger;

/**
 * Created by user on 13/09/14.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private int mWeightSelection;
    private BackupManager mBackupManager;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences,
                false);

        mBackupManager = new BackupManager(getActivity());

        initSummary(getPreferenceScreen());

        if(getActivity().getActionBar() != null)
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        Preference button = findPreference(getString(R.string.button_preference_reset_tutorial_mode_key));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogConfirmation.newInstance(
                        "reset_tutorial_mode",
                        confirmationListener,
                        R.string.generic_dialog_confirmation_tutorial_mode_title,
                        R.string.generic_dialog_confirmation_tutorial_mode_message,
                        null
                ).show(getActivity().getFragmentManager(), null);

                return true;
            }
        });

    }

    private DialogConfirmation.OnConfirmListener confirmationListener = new DialogConfirmation.OnConfirmListener() {
        @Override
        public void onConfirmPerformed(String tag, boolean confirmation, Object object) {
            if(tag.equals("reset_tutorial_mode") && confirmation) {
                Tutorial.tutorialOn(getActivity());
                new MyToast(getActivity(), R.string.tutorial_mode_activated_again);

                // restart the application
                Intent i = getActivity().getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    private void initSummary(Preference p) {

        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;

            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updateSummary(p);
        }
    }

    private void updateSummary(Preference p) {

        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
            if(p.getKey().equals("pref_key_units_weight")){
                mWeightSelection = Integer.parseInt(((ListPreference) p).getValue());
            }
        }
        else if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("pref_key_units_weight")){
            // we need to change values selected in the calculator activity
            SharedPreferences sp = getActivity().getSharedPreferences(ActivityMealsSnacks.SHARED_PREFERENCES_SNACK_VALUES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            Float br = sp.getFloat("snack_after_breakfast", 0.0f);
            Float lu = sp.getFloat("snack_after_lunch", 0.0f);
            Float di = sp.getFloat("snack_before_bed", 0.0f);

            int newWeightSelection = Integer.parseInt(((ListPreference) findPreference(key)).getValue());

            switch(mWeightSelection){
                case C.PREFERENCE_WEIGHT_GRAMS:
                    switch(newWeightSelection){
                        case C.PREFERENCE_WEIGHT_POUNDS:
                            br = UnitChanger.gramsToPounds(br);
                            lu = UnitChanger.gramsToPounds(lu);
                            di = UnitChanger.gramsToPounds(di);
                            break;
                        case C.PREFERENCE_WEIGHT_OUNCES:
                            br = UnitChanger.gramsToOunces(br);
                            lu = UnitChanger.gramsToOunces(lu);
                            Log.d("CambioUnidades", "Antes " + di.toString());
                            di = UnitChanger.gramsToOunces(di);
                            Log.d("CambioUnidades", "Despues " + di.toString());
                            break;
                    }
                    break;
                case C.PREFERENCE_WEIGHT_POUNDS:
                    switch(newWeightSelection){
                        case C.PREFERENCE_WEIGHT_GRAMS:
                            br = UnitChanger.poundsToGrams(br);
                            lu = UnitChanger.poundsToGrams(lu);
                            di = UnitChanger.poundsToGrams(di);
                            break;
                        case C.PREFERENCE_WEIGHT_OUNCES:
                            br = UnitChanger.poundsToOunces(br);
                            lu = UnitChanger.poundsToOunces(lu);
                            di = UnitChanger.poundsToOunces(di);
                            break;
                    }
                    break;
                case C.PREFERENCE_WEIGHT_OUNCES:
                    switch(newWeightSelection){
                        case C.PREFERENCE_WEIGHT_GRAMS:
                            br = UnitChanger.ouncesToGrams(br);
                            lu = UnitChanger.ouncesToGrams(lu);
                            Log.d("CambioUnidades", "Antes " + di.toString());
                            di = UnitChanger.ouncesToGrams(di);
                            Log.d("CambioUnidades", "Despues " + di.toString());
                            break;
                        case C.PREFERENCE_WEIGHT_POUNDS:
                            br = UnitChanger.ouncesToPounds(br);
                            lu = UnitChanger.ouncesToPounds(lu);
                            di = UnitChanger.ouncesToPounds(di);
                            break;
                    }
                    break;
            }

            editor.putFloat("snack_after_breakfast", br);
            editor.putFloat("snack_after_lunch", lu);
            editor.putFloat("snack_before_bed", di);

            editor.apply();

        }
        else if(key.equals("pref_meal_hours_notify_if_correct_meal_hour")){
            boolean activated = sharedPreferences.getBoolean("pref_meal_hours_notify_if_correct_meal_hour", true);

            if(!activated){
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("pref_meal_hours_notify_strict_mode", false);
                editor.apply();

                ((CheckBoxPreference) findPreference("pref_meal_hours_notify_strict_mode")).setChecked(false);
            }
        }

        else if(key.equals("pref_meal_hours_choose_minutes_of_range")){
            getActivity().startService(new Intent(getActivity(), SetMealAlarmsService.class));
        }

        updateSummary(findPreference(key));

        // to backup the change
        mBackupManager.dataChanged();
    }
}
