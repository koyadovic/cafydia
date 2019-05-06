package org.cafydia.android.dialogfragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.cafydia.android.R;
import org.cafydia.android.core.Annotation;
import org.cafydia.android.core.Instant;
import org.cafydia.android.datadatabase.DataDatabase;
import org.cafydia.android.genericdialogfragments.DialogDatePicker;
import org.cafydia.android.util.C;

/**
 * Created by user on 27/11/14.
 */
public class DialogAddAnnotation extends DialogFragment implements DialogDatePicker.OnDatePickedListener {
    private int mCurrentPagerPosition;
    private OnAnnotationAddedListener mCallBack;
    private boolean newAnnotation;

    private Instant mInstantSelected;
    private TextView mDateTextView;


    public static DialogAddAnnotation newInstance(int currentPagerPosition){
        DialogAddAnnotation dialog = new DialogAddAnnotation();
        Bundle args = new Bundle();

        args.putInt("current_pager_position", currentPagerPosition);
        args.putBoolean("new", true);
        dialog.setArguments(args);

        return dialog;
    }

    public static DialogAddAnnotation newInstance(int currentPagerPosition, Annotation a){
        DialogAddAnnotation dialog = new DialogAddAnnotation();
        Bundle args = new Bundle();

        args.putInt("current_pager_position", currentPagerPosition);
        args.putBoolean("new", false);
        args.putInt("id", a.getId());
        args.putString("date_string", a.getInternalDateString());
        args.putInt("created_by", a.getCreatedBy());
        args.putInt("scope", a.getOrderNumberScope());
        args.putString("text", a.getAnnotation());

        dialog.setArguments(args);

        return dialog;
    }

    /*
     * METHODS TO MANAGE LIFECYCLE
     */

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                R.string.dialog_add_annotation_title
        );

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_annotation, null);

        final RadioGroup rgScope = (RadioGroup) view.findViewById(R.id.rgScope);
        final EditText etAnnotation = (EditText) view.findViewById(R.id.etAnnotation);

        view.findViewById(R.id.ibModifyDate).setOnClickListener(buttonListener);
        mDateTextView = (TextView) view.findViewById(R.id.tvDate);

        Bundle args = getArguments();
        newAnnotation = args.getBoolean("new");

        if(!newAnnotation){
            mInstantSelected = new Instant(args.getString("date_string"));
            mDateTextView.setText(mInstantSelected.getUserDateString());


            if(args.getInt("scope") == C.ANNOTATION_SCOPE_GLOBAL){
                ((RadioButton)view.findViewById(R.id.rbGlobal)).setChecked(true);
            } else {
                ((RadioButton)view.findViewById(R.id.rbSpecific)).setChecked(true);
            }
            etAnnotation.setText(args.getString("text"));
        } else {
            // checks the first one. Annotation global.
            ((RadioButton)view.findViewById(R.id.rbGlobal)).setChecked(true);

            // set date to now
            mInstantSelected = new Instant();
            mDateTextView.setText(mInstantSelected.getUserDateString());
        }


        builder.setView(view);

        builder.setPositiveButton(R.string.dialog_add_annotation_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = etAnnotation.getText().toString();

                if(!text.equals("")){
                    if(newAnnotation) {
                        Annotation a;
                        switch (rgScope.getCheckedRadioButtonId()) {
                            case R.id.rbGlobal:
                                a = new Annotation(mInstantSelected.getInternalDateTimeString(), C.ANNOTATION_CREATED_BY_USER, C.ANNOTATION_SCOPE_GLOBAL, text);
                                break;
                            default:
                                a = new Annotation(mInstantSelected.getInternalDateTimeString(), C.ANNOTATION_CREATED_BY_USER, mCurrentPagerPosition, text);
                        }

                        DataDatabase db = new DataDatabase(getActivity());
                        a.save(db);

                        if(mCallBack != null) {
                            mCallBack.onAnnotationAddedOrEdited(a);
                        }

                    } else {
                        DataDatabase db = new DataDatabase(getActivity());
                        Bundle ar = getArguments();

                        int id = ar.getInt("id");
                        String dateString = mInstantSelected.getInternalDateString();
                        int scope = C.ANNOTATION_SCOPE_GLOBAL;

                        if(rgScope.getCheckedRadioButtonId() != R.id.rbGlobal) {
                            scope = mCurrentPagerPosition;
                        }

                        String txt = etAnnotation.getText().toString();

                        Annotation a = new Annotation(id, dateString, getArguments().containsKey("created_by") ? getArguments().getInt("created_by") : C.ANNOTATION_CREATED_BY_USER, scope, txt);
                        a.save(db);

                        if(mCallBack != null) {
                            mCallBack.onAnnotationAddedOrEdited(a);
                        }
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.dialog_add_annotation_button_cancel, null);

        return builder.create();
    }

        @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mCurrentPagerPosition = getArguments().getInt("current_pager_position");
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mCallBack = (OnAnnotationAddedListener) activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mCallBack = null;
    }

    public void onDatePicked(Instant instant, String tag){
        if(tag.equals("instant")){
            mInstantSelected = instant;
            mDateTextView.setText(mInstantSelected.getUserDateString());
        }
    }

    public void onDatePickerCanceled(String tag){

    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.ibModifyDate:
                    DialogDatePicker
                            .newInstance(mInstantSelected.toDate().getTime(), DialogAddAnnotation.this, "instant")
                            .show(getFragmentManager(), "dialog_date_picker");
                    break;

            }

        }
    };

    public interface OnAnnotationAddedListener {
        void onAnnotationAddedOrEdited(Annotation a);
    }
}
