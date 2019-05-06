package org.cafydia.android.dialogfragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;

import org.cafydia.android.R;
import org.cafydia.android.recommendations.ModificationStartDot;
import org.cafydia.android.util.C;

/**
 * Created by user on 21/09/14.
 */
public class DialogDotEditor extends DialogFragment {
    private ModificationStartDot dotToEdit;
    private SeekBar sbDay, sbModification;
    private TextView tvDay, tvModification;
    private Integer iDay, iModification;

    private DotEditedListener mDotEditedCallBack;

    private static final int MAX_DAY = 10;
    private static final int MAX_MODIFICATION = 10;
    private static final int MIN_MODIFICATION = -10;

    public static DialogDotEditor newInstance(ModificationStartDot dot){
        DialogDotEditor editor = new DialogDotEditor();
        Bundle args = new Bundle();
        Gson gson = new Gson();
        if(dot != null) {
            args.putString("dot_to_edit", gson.toJson(dot));
        } else {
            args.putString("dot_to_edit", "");
        }
        editor.setArguments(args);
        return editor;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                R.string.dialog_dot_editor_title
        );

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialog = inflater.inflate(R.layout.dialog_dot_editor, null);

        if(getArguments().getString("dot_to_edit").equals("")){
            dotToEdit = new ModificationStartDot(0, 0);
        } else {
            Gson gson = new Gson();
            dotToEdit = gson.fromJson(getArguments().getString("dot_to_edit"), C.TYPE_TOKEN_TYPE_MODIFICATION_START_DOT);
        }

        iDay = (int)dotToEdit.getX();
        iModification = (int) dotToEdit.getY();

        sbDay = (SeekBar) dialog.findViewById(R.id.sbDay);
        sbModification = (SeekBar) dialog.findViewById(R.id.sbModification);

        // disabled in the default metabolic rhythm
        sbModification.setEnabled(dotToEdit.getMetabolicRhythmId() != 1);

        tvDay = (TextView) dialog.findViewById(R.id.tvDay);
        tvModification = (TextView) dialog.findViewById(R.id.tvModification);

        tvDay.setText(Integer.toString((int)dotToEdit.getX()));
        tvModification.setText(Integer.toString((int) dotToEdit.getY()));

        sbDay.setMax(MAX_DAY);
        sbModification.setMax(MAX_MODIFICATION - MIN_MODIFICATION);

        sbDay.setOnSeekBarChangeListener(dayChangeListener);
        sbModification.setOnSeekBarChangeListener(modificationChangeListener);

        sbDay.setProgress((int)dotToEdit.getX());
        sbModification.setProgress((int)dotToEdit.getY() - MIN_MODIFICATION);

        builder.setView(dialog);
        builder.setPositiveButton(getString(R.string.dialog_dot_editor_positive_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mDotEditedCallBack != null) {
                    dotToEdit.setX(iDay);
                    dotToEdit.setY(iModification);
                    mDotEditedCallBack.onDotEdited(dotToEdit);
                }
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_dot_editor_negative_button), null);

        return builder.create();

    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(activity != null){
            mDotEditedCallBack = (DotEditedListener) activity;
        }
    }

    private SeekBar.OnSeekBarChangeListener dayChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            iDay = progress;
            tvDay.setText(iDay.toString());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private SeekBar.OnSeekBarChangeListener modificationChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            iModification = progress + MIN_MODIFICATION;
            tvModification.setText(iModification.toString());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };



    public interface DotEditedListener {
        void onDotEdited(ModificationStartDot dot);
    }
}
