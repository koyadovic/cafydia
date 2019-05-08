package org.cafydia4.android.dialogfragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.cafydia4.android.R;
import org.cafydia4.android.chartobjects.StatisticalObject;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.views.ChartPageElementChartView;

/**
 * Created by user on 2/03/15.
 */
public class DialogStatisticalObject extends DialogFragment {
    private StatisticalObject mStatisticalObject;
    private static OnStatisticalObjectListener mCallback;
    private static DataDatabase mDb;
    private static ChartPageElementChartView mTargetChartView;

    public static DialogStatisticalObject newInstance(DataDatabase db, StatisticalObject o, ChartPageElementChartView target, OnStatisticalObjectListener callback) {
        DialogStatisticalObject dialog = new DialogStatisticalObject();
        Bundle args = new Bundle();

        args.putInt("id", o.getId());
        args.putInt("chartElementId", o.getChartPageElementId());
        args.putInt("configuration", o.getConfigurationInteger());

        dialog.setArguments(args);

        mCallback = callback;
        mDb = db;
        mTargetChartView = target;

        return dialog;
    }

    public interface OnStatisticalObjectListener {
        void onStatisticalObjectModified(ChartPageElementChartView target, StatisticalObject o);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        Bundle args = getArguments();
        mStatisticalObject = new StatisticalObject(args.getInt("id"), args.getInt("chartElementId"), args.getInt("configuration"));

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                R.string.dialog_statistical_object_title
        );

        boolean[] configuration = new boolean[] {
                mStatisticalObject.isGridActivated(),
                mStatisticalObject.isGlucoseTestsActivated(),
                mStatisticalObject.isMaximumActivated(),
                mStatisticalObject.isMinimumActivated(),
                mStatisticalObject.isMeanActivated(),
                mStatisticalObject.isMedianActivated(),
                mStatisticalObject.isLinearRegressionActivated(),
                mStatisticalObject.isPolynomialRegressionGrade2Activated(),
                mStatisticalObject.isPolynomialRegressionGrade3Activated()
        };

        builder.setMultiChoiceItems(R.array.statistical_object_elements, configuration, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                switch (which) {
                    case 0: // Grid
                        mStatisticalObject.setGridActivated(isChecked);
                        break;

                    case 1: // Glucose tests
                        mStatisticalObject.setGlucoseTestsActivated(isChecked);
                        break;

                    case 2: // Maximum
                        mStatisticalObject.setMaximumActivated(isChecked);
                        break;

                    case 3: // Minimum
                        mStatisticalObject.setMinimumActivated(isChecked);
                        break;

                    case 4: // Mean
                        mStatisticalObject.setMeanActivated(isChecked);
                        break;

                    case 5: // Median
                        mStatisticalObject.setMedianActivated(isChecked);
                        break;

                    case 6: // Linear regression
                        mStatisticalObject.setLinearRegressionActivated(isChecked);
                        break;

                    case 7: // polynomial regression
                        mStatisticalObject.setPolynomialRegressionGrade2Activated(isChecked);
                        break;

                    case 8: // Variance
                        mStatisticalObject.setPolynomialRegressionGrade3Activated(isChecked);
                        break;

                }

            }
        });


        builder.setPositiveButton(R.string.dialog_statistical_object_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mStatisticalObject.save(mDb);
                mCallback.onStatisticalObjectModified(mTargetChartView, mStatisticalObject);
            }
        });


        return builder.create();

    }
}
