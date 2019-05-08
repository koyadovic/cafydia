package org.cafydia4.android.genericdialogfragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import org.cafydia4.android.R;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.dialogfragments.CafydiaAlertDialog;
import org.cafydia4.android.views.CustomDatePicker;

import java.util.Calendar;

/**
 * Created by user on 16/02/15.
 */
public class DialogDatePicker extends DialogFragment {

    private static OnDatePickedListener mCallBack;

    private CustomDatePicker datePicker;
    private ImageButton ibDatePrevious, ibDateNext;
    private String mTag;

    public interface OnDatePickedListener {
        void onDatePicked(Instant date, String tag);
        void onDatePickerCanceled(String tag);
    }

    public static DialogDatePicker newInstance(Long milis, OnDatePickedListener callback, String tag) {
        mCallBack = callback;

        Bundle args = new Bundle();

        if(milis != null){
            args.putLong("milis", milis);
        } else {
            args.putLong("milis", new Instant().toDate().getTime());
        }

        args.putString("tag", tag);

        DialogDatePicker dialog = new DialogDatePicker();
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                R.drawable.ic_action_go_to_today,
                R.string.dialog_date_picker_title
        );
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_date_picker, null);

        datePicker = (CustomDatePicker) view.findViewById(R.id.dpDate);
        datePicker.getCalendarView().setFirstDayOfWeek(Calendar.getInstance().getFirstDayOfWeek());

        ibDateNext = (ImageButton) view.findViewById(R.id.ibDateNext);
        ibDatePrevious = (ImageButton) view.findViewById(R.id.ibDatePrevious);

        ibDateNext.setOnClickListener(buttonClickListener);
        ibDatePrevious.setOnClickListener(buttonClickListener);

        Bundle args = getArguments();
        datePicker.setInstant(new Instant(args.getLong("milis")));
        mTag = args.getString("tag");

        builder.setView(view);

        builder.setPositiveButton(R.string.dialog_date_picker_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mCallBack != null){
                    mCallBack.onDatePicked(datePicker.getInstant(), mTag);
                }
            }
        });

        builder.setNegativeButton(R.string.dialog_date_picker_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mCallBack != null){
                    mCallBack.onDatePickerCanceled(mTag);
                }
            }
        });

        return builder.create();

    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.ibDateNext:
                    datePicker.increaseOneMonth();
                    break;
                case R.id.ibDatePrevious:
                    datePicker.decreaseOneMonth();
                    break;
            }
        }
    };
}
