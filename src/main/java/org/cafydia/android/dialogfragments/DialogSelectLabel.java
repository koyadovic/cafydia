package org.cafydia.android.dialogfragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.cafydia.android.R;
import org.cafydia.android.adapters.LabelAdapter;
import org.cafydia.android.chartobjects.Label;
import org.cafydia.android.datadatabase.DataDatabase;
import org.cafydia.android.util.C;

/**
 * Created by user on 21/02/15.
 */
public class DialogSelectLabel extends DialogFragment {

    private ListView lvLabels;
    private LabelAdapter mLabelAdapter;
    private static OnLabelSelectedListener mCallback;

    private static DataDatabase db;

    public static DialogSelectLabel newInstance(DataDatabase d, OnLabelSelectedListener callback) {
        DialogSelectLabel dialog = new DialogSelectLabel();
        db = d;
        mCallback = callback;
        return dialog;
    }

    public interface OnLabelSelectedListener {
        void onLabelSelected(Label l);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                R.string.dialog_select_label_title
        );
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_label, null);

        lvLabels = (ListView) view.findViewById(R.id.lvLabels);

        mLabelAdapter = new LabelAdapter(getActivity(), db.getLabels(), C.LABEL_TEXT_COLOR_BLACK);

        lvLabels.setAdapter(mLabelAdapter);

        builder.setView(view);

        lvLabels.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mCallback != null) {
                    mCallback.onLabelSelected(mLabelAdapter.getItem(position));
                }
                dismiss();
            }
        });

        return builder.create();
    }
}
