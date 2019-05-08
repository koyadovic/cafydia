package org.cafydia4.android.dialogfragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.core.GlucoseTest;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.genericdialogfragments.DialogDatePicker;
import org.cafydia4.android.genericdialogfragments.DialogTimePicker;
import org.cafydia4.android.recommendations.MetabolicFramework;
import org.cafydia4.android.recommendations.MetabolicFrameworkState;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.HbA1cHelper;
import org.cafydia4.android.util.MyToast;
import org.cafydia4.android.util.NearestTime;
import org.cafydia4.android.util.UnitChanger;
import org.cafydia4.android.util.ViewUtil;
import org.cafydia4.android.views.EditTextGlucose;

/**
 * Created by miguel on 1/07/14.
 */
public class DialogAddGlucose extends DialogFragment
    implements DialogDatePicker.OnDatePickedListener,
        DialogTimePicker.OnTimePickedListener {

    // UI Elements
    private Spinner mSpinnerTimeMeal;
    private Spinner mSpinnerBeforeAfter;
    private EditText mEditTextLevel;
    private EditTextGlucose mEditTextGlucose;

    private TextView tvDate, tvTime;
    private Instant mDateAndTime;

    // To construct time column of database
    private int mSelectedMeal;
    private int mBeforeAfterTime;
    private int mResultTime;

    // the initial time spinner position
    private int mInitialSpinnerPosition;
    private static final String SPINNER_POSITION_TAG = "initial_spinner_position";

    private GlucoseTestAdded mGlucoseTestAddedCallback = null;
    private GlucoseTest mLastTest;

    private MetabolicFramework mFramework;

    UnitChanger mChange;

    // to get new instances of the fragment
    public static DialogAddGlucose newInstance(){
        DialogAddGlucose gdf = new DialogAddGlucose();

        Bundle args = new Bundle();
        args.putInt(SPINNER_POSITION_TAG, NearestTime.getNearestMeal()); // 0 breakfast, 1 lunch, 2 dinner
        gdf.setArguments(args);

        return gdf;
    }

    /*
     * METHODS TO MANAGE LIFECYCLE
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mInitialSpinnerPosition = getArguments().getInt(SPINNER_POSITION_TAG);

    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mGlucoseTestAddedCallback = (GlucoseTestAdded) activity;

        mChange = new UnitChanger(activity);
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mGlucoseTestAddedCallback = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        super.onCreateDialog(savedInstanceState);

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                R.drawable.ic_glucose_test_light,
                R.string.glucose_dialog_label
        );

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mFramework = new MetabolicFramework(getActivity());
            }
        });

        // cancel if exists, the glucose test reminder in the notification bar
        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_glucose, null);

        // find UI elements in the layout
        mSpinnerBeforeAfter = (Spinner) view.findViewById(R.id.spinnerBeforeAfter);
        mSpinnerTimeMeal = (Spinner) view.findViewById(R.id.spinnerMeal);
        mEditTextLevel = (EditText) view.findViewById(R.id.editTextLevel);
        mEditTextGlucose = (EditTextGlucose) view.findViewById(R.id.glucoseEditText);

        tvDate = (TextView) view.findViewById(R.id.tvDate);
        tvTime = (TextView) view.findViewById(R.id.tvTime);

        mDateAndTime = new Instant();
        tvDate.setText(mDateAndTime.getUserDateString());
        tvTime.setText(mDateAndTime.getUserTimeStringShort());

        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogDatePicker.newInstance(mDateAndTime.toDate().getTime(), DialogAddGlucose.this, "change_date").show(getActivity().getFragmentManager(), null);
            }
        });

        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogTimePicker.newInstance(DialogAddGlucose.this, mDateAndTime.getHour(), mDateAndTime.getMinute()).show(getActivity().getFragmentManager(), null);
            }
        });

        ((TextView) view.findViewById(R.id.tvGlycemiaUnit)).setText(mChange.getStringUnitForGlucose());


        // create the arrays of Strings
        ArrayAdapter adapterTimeBeforeAfter = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.time_before_after, android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter adapterTimeMeal = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.time_meal, android.R.layout.simple_spinner_dropdown_item);

        // set up adapters
        mSpinnerBeforeAfter.setAdapter(adapterTimeBeforeAfter);
        mSpinnerTimeMeal.setAdapter(adapterTimeMeal);

        //
        mLastTest = new DataDatabase(getActivity()).getLastGlucoseTestAdded();



        // set up the listeners
        mSpinnerBeforeAfter.setOnItemSelectedListener(listenerBeforeAfter);
        mSpinnerTimeMeal.setOnItemSelectedListener(listenerMeal);

        // 0 breakfast, 1 lunch, 2 dinner
        mSpinnerTimeMeal.setSelection(mInitialSpinnerPosition);


        builder.setView(view);

        builder.setPositiveButton(getString(R.string.glucose_positive_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(mEditTextGlucose.getGlucoseLevelMgDl() != 0.0f) {
                                    if(mEditTextGlucose.getGlucoseLevelMgDl() < 700) {
                                        DataDatabase db = new DataDatabase(getActivity());
                                        MetabolicFrameworkState state = new MetabolicFrameworkState(getActivity());

                                        final GlucoseTest g = new GlucoseTest(0, mDateAndTime.getInternalDateTimeString(), mResultTime, (int) mEditTextGlucose.getGlucoseLevelMgDl(), state.getActivatedMetabolicRhythmId());
                                        g.save(db);

                                        new HbA1cHelper(getActivity()).setNeedToRecalculate(true);

                                        // sólo si la glucosa es después de la comida se testea esto
                                        if(mBeforeAfterTime == C.GLUCOSE_TEST_AFTER) {

                                            mFramework.calculateLinearFunctionsFromDatabaseIfNeeded();
                                            //new BaselinePreprandial.Fitter(getActivity(), g).fit();
                                        }

                                        ViewUtil.hideKeyboard(getActivity(), mEditTextLevel);

                                        if (mGlucoseTestAddedCallback != null) {
                                            mGlucoseTestAddedCallback.onGlucoseTestAdded();
                                        }
                                    } else {
                                        new MyToast(getActivity(), getString(R.string.glucose_dialog_error_level_too_high));
                                    }
                                } else {
                                    new MyToast(getActivity(), getString(R.string.glucose_dialog_empty_level));
                                }
                            }
                        }
                )
                .setNegativeButton(getString(R.string.glucose_negative_button), null);

        return builder.create();
    }

    public void onDatePicked(Instant i, String tag){
        if(tag.equals("change_date")){
            if(i.getDaysPassedFromNow() <= 0) {
                mDateAndTime = i;
                tvDate.setText(mDateAndTime.getUserDateString());
            } else {
                new MyToast(getActivity(), getString(R.string.glucose_dialog_error_future_instant));
            }
        }
    }

    public void onDatePickerCanceled(String tag){

    }

    public void onTimePicked(int hour, int minute) {
        Instant i = new Instant(mDateAndTime.getDaysPassedFromNow());
        i.setHourAndMinute(hour, minute);

        if(i.getDaysPassedFromNow() <= 0) {
            mDateAndTime.setHourAndMinute(hour, minute);
            tvTime.setText(mDateAndTime.getUserTimeStringShort());
        } else {
            new MyToast(getActivity(), getString(R.string.glucose_dialog_error_future_instant));
        }

    }


    /*
     * LISTENERS
     */
    AdapterView.OnItemSelectedListener listenerBeforeAfter = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mBeforeAfterTime = position;
            switch(position){
                case 0:
                    mSpinnerTimeMeal.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    mSpinnerTimeMeal.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    mSpinnerTimeMeal.setVisibility(View.INVISIBLE);
                    break;
            }

            updateResultTime();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    AdapterView.OnItemSelectedListener listenerMeal = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSelectedMeal = position;

            boolean after = false;

            // todo hay que ver si esto soluciona el problema
            if(mLastTest != null && mLastTest.isToday()) {
                after = after || mLastTest.getGlucoseTime().equals(C.GLUCOSE_TEST_BEFORE_BREAKFAST) && mSelectedMeal == C.MEAL_BREAKFAST;
                after = after || mLastTest.getGlucoseTime().equals(C.GLUCOSE_TEST_BEFORE_LUNCH) && mSelectedMeal == C.MEAL_LUNCH;
                after = after || mLastTest.getGlucoseTime().equals(C.GLUCOSE_TEST_BEFORE_DINNER) && mSelectedMeal == C.MEAL_DINNER;
            }

            if(after)
                mSpinnerBeforeAfter.setSelection(1);
            else
                mSpinnerBeforeAfter.setSelection(0);

            updateResultTime();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void updateResultTime(){
        switch(mBeforeAfterTime){
            case C.GLUCOSE_TEST_BEFORE:
                switch (mSelectedMeal){
                    case C.MEAL_BREAKFAST:
                        mResultTime = C.GLUCOSE_TEST_BEFORE_BREAKFAST;
                        break;
                    case C.MEAL_LUNCH:
                        mResultTime = C.GLUCOSE_TEST_BEFORE_LUNCH;
                        break;
                    case C.MEAL_DINNER:
                        mResultTime = C.GLUCOSE_TEST_BEFORE_DINNER;
                        break;
                }
                break;
            case C.GLUCOSE_TEST_AFTER:
                switch (mSelectedMeal){
                    case C.MEAL_BREAKFAST:
                        mResultTime = C.GLUCOSE_TEST_AFTER_BREAKFAST;
                        break;
                    case C.MEAL_LUNCH:
                        mResultTime = C.GLUCOSE_TEST_AFTER_LUNCH;
                        break;
                    case C.MEAL_DINNER:
                        mResultTime = C.GLUCOSE_TEST_AFTER_DINNER;
                        break;
                }
                break;
            case C.GLUCOSE_TEST_NIGHT:
                mResultTime = C.GLUCOSE_TEST_IN_THE_NIGHT;
                break;
        }
    }

    /*
     * INTERFACE WITH METHOD CALLED WHEN GLUCOSE TEST IS ADDED TO DATABASE
     */
    public interface GlucoseTestAdded {
        void onGlucoseTestAdded();
    }
}
