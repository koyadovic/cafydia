package org.cafydia4.android.dialogfragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.cafydia4.android.R;
import org.cafydia4.android.util.C;

/**
 * Created by user on 10/12/14.
 */
public class DialogNewChartActivityElement extends DialogFragment {
    private OnNewChartElementListener mCallBack;

    public static DialogNewChartActivityElement newInstance(int pagerPosition){
        DialogNewChartActivityElement dialog = new DialogNewChartActivityElement();
        Bundle args = new Bundle();
        args.putInt("pager_position", pagerPosition);

        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        mCallBack = (OnNewChartElementListener) getActivity();

        final int pagerPosition = getArguments().getInt("pager_position");

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                R.string.dialog_new_chart_element_title
        );

        builder.setItems(pagerPosition > 0 ? R.array.chart_page_elements : R.array.chart_page_elements_hba1c_position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mCallBack != null) {

                    if (pagerPosition > 0) {
                        mCallBack.onNewChartActivityElementSelected(which);
                    } else {
                        switch (which) {
                            case 0:
                                mCallBack.onNewChartActivityElementSelected(C.CHART_ACTIVITY_ELEMENT_PAGE);
                                break;
                            case 1:
                                mCallBack.onNewChartActivityElementSelected(C.CHART_ACTIVITY_ELEMENT_ANNOTATION);
                                break;
                            case 2:
                                mCallBack.onNewChartActivityElementSelected(C.CHART_ACTIVITY_ELEMENT_LABEL);
                                break;
                        }
                    }
                }
            }
        });

        return builder.create();
    }

    public interface OnNewChartElementListener {
        void onNewChartActivityElementSelected(int selection);
    }
}
