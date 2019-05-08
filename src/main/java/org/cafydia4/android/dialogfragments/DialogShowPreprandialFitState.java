package org.cafydia4.android.dialogfragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.recommendations.BaselinePreprandial;
import org.cafydia4.android.util.C;
import org.cafydia4.android.views.BaselineChartView;

/**
 * Created by user on 6/06/15.
 */
public class DialogShowPreprandialFitState extends DialogFragment {
    private Float oldM = null;
    private Float oldB = null;

    private Float newM = null;
    private Float newB = null;

    private int mMealTime;

    private BaselinePreprandial mPreprandial;

    public static DialogShowPreprandialFitState newInstance(int meal){
        return newInstance(meal, null, null, null, null);
    }

    public static DialogShowPreprandialFitState newInstance(int meal, Float oldM, Float oldB, Float newM, Float newB){
        DialogShowPreprandialFitState dialog = new DialogShowPreprandialFitState();
        Bundle args = new Bundle();

        args.putInt("meal", meal);

        if(oldM != null)
            args.putFloat("oldm", oldM);

        if(oldB != null)
            args.putFloat("oldb", oldB);

        if(newM != null)
            args.putFloat("newm", newM);

        if(newB != null)
            args.putFloat("newb", newB);

        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        Bundle args = getArguments();

        mMealTime = args.getInt("meal");

        if(args.containsKey("oldm"))
            oldM = args.getFloat("oldm");

        if(args.containsKey("oldb"))
            oldB = args.getFloat("oldb");

        if(args.containsKey("newm"))
            newM = args.getFloat("newm");

        if(args.containsKey("newb"))
            newB = args.getFloat("newb");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mPreprandial = new BaselinePreprandial(getActivity());

        int mealTime = getArguments().getInt("meal");

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_show_preprandial_fit_state, null);

        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        ImageView ivGenerated = (ImageView) view.findViewById(R.id.ivGenerated);
        ImageView ivValidated = (ImageView) view.findViewById(R.id.ivValidated);
        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);

        if(mPreprandial.isFunctionGenerated(mMealTime)){
            ivGenerated.setImageResource(R.drawable.ic_validator_done);

            if(BaselinePreprandial.Fitter.isSuccess(getActivity(), mMealTime)){
                ivValidated.setImageResource(R.drawable.ic_validator_done);
            } else {
                ivValidated.setImageResource(R.drawable.ic_validator_updating);
            }

        } else {
            ivGenerated.setImageResource(R.drawable.ic_validator_updating);

            ivValidated.setImageResource(R.drawable.ic_validator_not_yet);
        }

        String message;
        message = BaselinePreprandial.Fitter.getMode(getActivity(), mMealTime) + " - ";
        message += BaselinePreprandial.Fitter.getSuccessMessage(getActivity(), mMealTime);
        tvMessage.setText(message);


        BaselineChartView baselineChartView = (BaselineChartView) view.findViewById(R.id.baselineChartView);

        if(newM == null && newB == null && oldM == null && newB == null){
            baselineChartView.setVisibility(View.GONE);
        } else {
            if (newM != null && newB != null)
                baselineChartView.setSecondaryLineParameters(newM, newB);

            if (oldM != null && oldB != null)
                baselineChartView.setMainLineParameters(oldM, oldB);

            baselineChartView.refresh();
        }

        switch (mealTime) {
            case C.MEAL_BREAKFAST:
                tvTitle.setText(getString(R.string.dialog_generate_validate_preprandial_title_breakfast));
                break;

            case C.MEAL_LUNCH:
                tvTitle.setText(getString(R.string.dialog_generate_validate_preprandial_title_lunch));
                break;

            case C.MEAL_DINNER:
                tvTitle.setText(getString(R.string.dialog_generate_validate_preprandial_title_dinner));
                break;

        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPreprandial.setMByMeal(newM, mMealTime);
                mPreprandial.setBByMeal(newB, mMealTime);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // foo
            }
        });

        builder.setView(view);
        return builder.create();
    }
}
