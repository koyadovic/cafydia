package org.cafydia.android.dialogfragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.Gson;

import org.cafydia.android.R;
import org.cafydia.android.recommendations.ModificationStart;
import org.cafydia.android.recommendations.ModificationStartDot;
import org.cafydia.android.util.C;

/**
 * Created by user on 8/10/14.
 */
public class DialogDotSelector extends DialogFragment {

    private DotSelectedListener mDotSelectorCallBack;
    private int mAction;
    private ModificationStart mStart;
    private RadioGroup rgDots;

    public static DialogDotSelector newInstance(ModificationStart start, int action){
        DialogDotSelector selector = new DialogDotSelector();
        Bundle args = new Bundle();
        Gson gson = new Gson();
        args.putInt("action", action);
        args.putString("start", gson.toJson(start));
        selector.setArguments(args);

        return selector;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                R.string.dialog_dot_selector_title
        );

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialog = inflater.inflate(R.layout.dialog_dot_selector, null);

        mAction = getArguments().getInt("action");
        Gson gson = new Gson();
        mStart = gson.fromJson(getArguments().getString("start"), C.TYPE_TOKEN_TYPE_MODIFICATION_START);

        rgDots = (RadioGroup) dialog.findViewById(R.id.rgDots);

        for (ModificationStartDot dot : mStart.getDots()){
            if(dot.getX() == -1f) continue;

            RadioButton rButton = new RadioButton(getActivity());
            Integer day = (int) dot.getX();
            Integer modification = (int) dot.getY();
            rButton.setText(getString(R.string.dialog_dot_selector_day) + ": " +  day.toString() + ", " + getString(R.string.dialog_dot_selector_modification) + ": " + modification.toString());

            rgDots.addView(rButton);

        }

        // marcamos el primero de ellos.
        if(rgDots.getChildCount() > 0){
            ((RadioButton) rgDots.getChildAt(0)).setChecked(true);
        }

        builder.setView(dialog);

        builder.setPositiveButton(getString(R.string.dialog_dot_selector_positive_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int index = rgDots.indexOfChild(rgDots.findViewById(rgDots.getCheckedRadioButtonId()));
                if(index != -1) {
                    index++; // because dot in pos x = -1 are removed from radio group at start.
                    ModificationStartDot selectedDot = mStart.getDots().get(index);
                    mDotSelectorCallBack.onDotSelected(selectedDot, mAction);
                }
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_dot_selector_negative_button), null);

        return builder.create();

    }

    public interface DotSelectedListener {
        void onDotSelected(ModificationStartDot dot, int action);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(activity != null){
            mDotSelectorCallBack = (DotSelectedListener) activity;
        }
    }
}
