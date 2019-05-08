package org.cafydia4.android.genericdialogfragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import org.cafydia4.android.R;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.dialogfragments.CafydiaAlertDialog;

/**
 * Created by user on 19/02/15.
 */
public class DialogTimePicker extends DialogFragment {

    private TimePicker tpTime;
    private static OnTimePickedListener mCallback;

    public interface OnTimePickedListener {
        void onTimePicked(int hour, int minute);
    }


    public static DialogTimePicker newInstance(OnTimePickedListener callback, Integer hour, Integer minute){
        DialogTimePicker dialog = new DialogTimePicker();
        Bundle args = new Bundle();

        if(hour == null || minute == null) {
            Instant i = new Instant();
            args.putInt("hour", i.getHour());
            args.putInt("minute", i.getMinute());
        } else {
            args.putInt("hour", hour);
            args.putInt("minute", minute);
        }

        dialog.setArguments(args);

        mCallback = callback;

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        Bundle args = getArguments();

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                R.drawable.ic_action_alarms,
                R.string.dialog_time_picker_title
        );
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_time_picker, null);

        tpTime = (TimePicker) view.findViewById(R.id.tpTime);
        tpTime.setIs24HourView(true);
        tpTime.setCurrentHour(args.getInt("hour"));
        tpTime.setCurrentMinute(args.getInt("minute"));

        builder.setView(view);

        builder.setPositiveButton(R.string.dialog_time_picker_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mCallback != null) {
                    mCallback.onTimePicked(tpTime.getCurrentHour(), tpTime.getCurrentMinute());
                }
            }
        });

        builder.setNegativeButton(R.string.dialog_time_picker_button_cancel, null);

        return builder.create();

    }
}
