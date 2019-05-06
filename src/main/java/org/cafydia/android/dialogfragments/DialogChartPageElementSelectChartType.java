package org.cafydia.android.dialogfragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.cafydia.android.R;

/**
 * Created by user on 20/02/15.
 */
public class DialogChartPageElementSelectChartType extends DialogFragment {
    private int mPagerPosition;
    private static OnChartTypeSelectedListener mCallback;

    public interface OnChartTypeSelectedListener {
        void onChartTypeSelected(int type);
    }

    public static DialogChartPageElementSelectChartType newInstance(int pagerPosition, OnChartTypeSelectedListener callback){
        DialogChartPageElementSelectChartType dialog = new DialogChartPageElementSelectChartType();
        Bundle args = new Bundle();
        args.putInt("pager_position", pagerPosition);

        dialog.setArguments(args);

        mCallback = callback;

        return dialog;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        Bundle arguments = getArguments();
        mPagerPosition = arguments.getInt("pager_position");

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                R.string.dialog_select_chart_type_title
        );

        builder.setItems(R.array.chart_types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mCallback != null) {
                    mCallback.onChartTypeSelected(which + 1);
                }
            }
        });

        return builder.create();
    }
}
