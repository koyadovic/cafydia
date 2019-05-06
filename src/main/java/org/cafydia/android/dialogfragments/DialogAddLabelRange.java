package org.cafydia.android.dialogfragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.cafydia.android.R;
import org.cafydia.android.chartobjects.LabelRange;
import org.cafydia.android.core.Instant;
import org.cafydia.android.datadatabase.DataDatabase;
import org.cafydia.android.genericdialogfragments.DialogDatePicker;

/**
 * Created by user on 16/02/15.
 */
public class DialogAddLabelRange extends DialogFragment
    implements DialogDatePicker.OnDatePickedListener {

    private LabelRange mRange;

    private TextView tvStart, tvEnd;
    private ImageButton ibModifyStart, ibModifyEnd;

    private static DialogAddLabelRangeInterface mCallBack;

    public interface DialogAddLabelRangeInterface {
        void onLabelRangeAdded(LabelRange range);
    }

    public static DialogAddLabelRange newInstance(LabelRange range, DialogAddLabelRangeInterface callback) {
        Bundle args = new Bundle();
        args.putInt("id", range.getId());
        args.putInt("label_id", range.getLabelId());
        args.putLong("start", range.getStart().toDate().getTime());
        args.putLong("end", range.getEnd().toDate().getTime());

        DialogAddLabelRange dialog = new DialogAddLabelRange();
        dialog.setArguments(args);

        mCallBack = callback;

        return dialog;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                R.drawable.ic_action_labels,
                R.string.dialog_add_label_range_title
        );
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_label_date_range, null);

        //
        // search views in the layout
        //
        tvStart = (TextView) view.findViewById(R.id.tvStart);
        tvEnd = (TextView) view.findViewById(R.id.tvEnd);

        ibModifyStart = (ImageButton) view.findViewById(R.id.ibModifyStart);
        ibModifyEnd = (ImageButton) view.findViewById(R.id.ibModifyEnd);

        ibModifyStart.setOnClickListener(buttonListener);
        ibModifyEnd.setOnClickListener(buttonListener);

        Bundle a = getArguments();
        mRange = new LabelRange(a.getInt("id"), a.getInt("label_id"), a.getLong("start"), a.getLong("end"));

        tvStart.setText(mRange.getStart().getUserDateString());
        tvEnd.setText(mRange.getEnd().getUserDateString());

        builder.setView(view);

        builder.setPositiveButton(R.string.dialog_add_label_range_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mRange.getStart().getDaysPassedFromInstant(mRange.getEnd()) <= 0) {
                    mRange.save(new DataDatabase(getActivity()));

                    if(mCallBack != null) {
                        mCallBack.onLabelRangeAdded(mRange);
                    }
                } else {
                    // todo mensaje de que el final no puede ser anterior al inicio
                }
            }
        });

        builder.setNegativeButton(R.string.dialog_add_label_range_button_cancel, null);

        return builder.create();

    }

    public void onDatePicked(Instant instant, String tag){
        if(tag.equals("start")){
            mRange.setStart(instant);
            if(instant.getDaysPassedFromInstant(mRange.getEnd()) > 0) {
                mRange.setEnd(instant);
                tvEnd.setText(mRange.getEnd().getUserDateString());
            }
            tvStart.setText(mRange.getStart().getUserDateString());
        }
        else if(tag.equals("end")){
            mRange.setEnd(instant);
            if(instant.getDaysPassedFromInstant(mRange.getStart()) < 0) {
                mRange.setStart(instant);
                tvStart.setText(mRange.getStart().getUserDateString());
            }
            tvEnd.setText(mRange.getEnd().getUserDateString());
        }
    }

    public void onDatePickerCanceled(String tag){

    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.ibModifyStart:
                    DialogDatePicker
                            .newInstance(mRange.getStart().toDate().getTime(), DialogAddLabelRange.this, "start")
                            .show(getFragmentManager(), "dialog_date_picker");
                    break;

                case R.id.ibModifyEnd:
                    DialogDatePicker
                            .newInstance(mRange.getEnd().toDate().getTime(), DialogAddLabelRange.this, "end")
                            .show(getFragmentManager(), "dialog_date_picker");
                    break;
            }

        }
    };

}
