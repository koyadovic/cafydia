package org.cafydia4.android.dialogfragments;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.cafydia4.android.R;
import org.cafydia4.android.adapters.FoodSelectedAdapter;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.core.Meal;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.recommendations.BaselinePreprandial;
import org.cafydia4.android.recommendations.MetabolicFramework;
import org.cafydia4.android.services.CafydiaReceiver;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.MyFoodArrayList;
import org.cafydia4.android.util.MyRound;
import org.cafydia4.android.util.MyToast;
import org.cafydia4.android.util.ViewUtil;

/**
 * Created by user on 11/11/14.
 */
public class DialogMealFinished extends DialogFragment {
    private static final String LAST_STATE_GLUCOSE_SHARED_PREFERENCES = "memorized_last_button_state";
    private static final String LAST_STATE_BUTTON = "button_state";
    private TextView tvPreprandialDose, tvBasalDose;
    private EditText etGetPreprandialDose;
    private LinearLayout lGetPreprandialDose, lPreprandialDose, lBasalDose;
    private ListView lvFoodSelected;
    private CheckBox cbAlert;

    private static Meal meal;
    private static MetabolicFramework framework;

    private int mMinutesAfterMeal;

    private boolean insufficientData = false;

    public static DialogMealFinished newInstance(Meal m, MetabolicFramework f){
        meal = m;
        framework = f;
        return new DialogMealFinished();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        super.onCreateDialog(savedInstanceState);

        CafydiaAlertDialog.Builder builder;
        if(meal.getMealId().equals(0)) {
            builder = new CafydiaAlertDialog.Builder(
                    getActivity(),
                    getResources().getColor(R.color.colorCafydiaDefault),
                    R.drawable.ic_action_warning,
                    null
            );
        } else {
            builder = new CafydiaAlertDialog.Builder(
                    getActivity(),
                    getResources().getColor(R.color.colorCafydiaDefault),
                    R.drawable.ic_action_about,
                    null
            );
        }

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_meal_finished, null);

        searchUIElements(view);

        builder.setView(view);

        /*
         * Here we can start to work with the information passed or restored
         */


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mMinutesAfterMeal = Integer.parseInt(sp.getString("pref_key_glucose_after_meal", "120"));

        if(meal.getMealId().equals(0)) {
            cbAlert.setButtonDrawable(R.drawable.ic_action_add_alarm);
            cbAlert.setAlpha(0.2f);
            cbAlert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    cbAlert.setAlpha(isChecked ? 1 : 0.2f);

                    // we save the last state of the button
                    SharedPreferences s = getActivity().getSharedPreferences(LAST_STATE_GLUCOSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor e = s.edit();
                    e.putBoolean(LAST_STATE_BUTTON, isChecked);
                    e.apply();
                }
            });

            switch (mMinutesAfterMeal) {
                case 90:
                    cbAlert.setText(getString(R.string.dialog_meal_finished_alert_glucose_after_meal_90));
                    break;
                case 105:
                    cbAlert.setText(getString(R.string.dialog_meal_finished_alert_glucose_after_meal_105));
                    break;
                case 120:
                    cbAlert.setText(getString(R.string.dialog_meal_finished_alert_glucose_after_meal_120));
                    break;
            }
            sp = getActivity().getSharedPreferences(LAST_STATE_GLUCOSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            cbAlert.setChecked(sp.getBoolean(LAST_STATE_BUTTON, false));

            BaselinePreprandial functions = new BaselinePreprandial(getActivity());

            if(functions.isFunctionGenerated(meal.getMealTime())){
                lPreprandialDose.setVisibility(View.VISIBLE);
                lGetPreprandialDose.setVisibility(View.GONE);
                builder.setTitle(getString(R.string.dialog_meal_finished_recommendation_generated));

                // preprandial
                tvPreprandialDose.setText(Integer.toString(MyRound.round(meal.getFinalPreprandialDose(), 0).intValue()));

            } else {
                lGetPreprandialDose.setVisibility(View.VISIBLE);
                lPreprandialDose.setVisibility(View.GONE);
                builder.setTitle(getString(R.string.dialog_meal_finished_insufficient_data));

                insufficientData = true;

            }

        } else {
            cbAlert.setVisibility(View.GONE);

            lPreprandialDose.setVisibility(View.VISIBLE);
            builder.setTitle(getString(R.string.dialog_meal_finished_last_recommendation));

            // preprandial
            tvPreprandialDose.setText(Integer.toString(MyRound.round(meal.getFinalPreprandialDose(), 0).intValue()));

        }




        // basal
        int bDose = MyRound.round(meal.getTotalBasalDose(), 0).intValue();
        if(bDose > 0) {
            tvBasalDose.setText(Integer.toString(bDose));
        } else {
            lBasalDose.setVisibility(View.GONE);
        }


        // food selected in list view
        FoodSelectedAdapter adapter = new FoodSelectedAdapter((FragmentActivity) getActivity());
        MyFoodArrayList foods = new MyFoodArrayList(meal.getFoodSelected());
        adapter.setFoodSelected(foods);
        lvFoodSelected.setAdapter(adapter);


        builder.setPositiveButton(getString(R.string.dialog_meal_finished_ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(meal.getMealId().equals(0)) {
                    if (lGetPreprandialDose.getVisibility() == View.VISIBLE) {

                        // cannot generate recommendations yet
                        if (!etGetPreprandialDose.getText().toString().equals("")) {
                            meal.setBaselinePreprandial(Float.parseFloat(etGetPreprandialDose.getText().toString()));
                            meal.setFinalPreprandialDose(meal.getTotalPreprandialDose());
                        } else {
                            meal = null;
                        }
                    }

                    if (meal != null) {
                        if (cbAlert.isChecked()) {
                            Intent intent = new Intent(getActivity(), CafydiaReceiver.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 1366613666, intent, 0);
                            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

                            Instant now = new Instant();
                            long time = now.toDate().getTime();

                            time += mMinutesAfterMeal * 60 * 1000;
                            //time += 10 * 1000;

                            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                        }

                        // we add the meal to database
                        DataDatabase db = new DataDatabase(getActivity());
                        meal.save(db);




                        // we show a toast
                        new MyToast(getActivity(), getString(R.string.dialog_meal_finished_new_meal_added));

                    } else {
                        // we show a toast of error
                    }

                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
            }
        });

        return builder.create();

    }

    @Override
    public void onResume(){
        super.onResume();

        if(insufficientData) {
            ViewUtil.showKeyboard(getActivity(), etGetPreprandialDose);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Gson gson = new Gson();

        outState.putString("meal", gson.toJson(meal));

    }

    private void processSavedState(Bundle savedState){
        Gson gson = new Gson();
        meal = gson.fromJson(savedState.getString("meal"), C.TYPE_TOKEN_TYPE_MEAL);

    }

    private void processArguments(Bundle arguments){
        if(arguments != null){
            Gson gson = new Gson();
            meal = gson.fromJson(arguments.getString("meal"), C.TYPE_TOKEN_TYPE_MEAL);
        }
    }

    private void searchUIElements(View view){
        tvPreprandialDose = (TextView) view.findViewById(R.id.tvPreprandialDose);
        tvBasalDose = (TextView) view.findViewById(R.id.tvBasalDose);
        etGetPreprandialDose = (EditText) view.findViewById(R.id.etGetPreprandialDose);
        lGetPreprandialDose = (LinearLayout) view.findViewById(R.id.lGetPreprandialDose);
        lPreprandialDose = (LinearLayout) view.findViewById(R.id.lPreprandialDose);
        lBasalDose = (LinearLayout) view.findViewById(R.id.lBasalDose);
        lvFoodSelected = (ListView) view.findViewById(R.id.lvFoodSelected);
        cbAlert = (CheckBox) view.findViewById(R.id.cbAlert);
    }
}
