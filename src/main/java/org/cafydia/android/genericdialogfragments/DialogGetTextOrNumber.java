package org.cafydia.android.genericdialogfragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import org.cafydia.android.R;
import org.cafydia.android.dialogfragments.CafydiaAlertDialog;
import org.cafydia.android.util.ViewUtil;

/**
 * Created by user on 17/02/15.
 */
public class DialogGetTextOrNumber extends DialogFragment {
    private static OnTextIntroducedListener mCallback;
    private EditText etText;
    private boolean mNumber;
    private static View target;

    public interface OnTextIntroducedListener {
        void onTextIntroduced(String tag, String text, View targetView);
    }


    public static DialogGetTextOrNumber newInstance(String tag, String title, View targetView, OnTextIntroducedListener callback) {
        return newInstance(tag, title, targetView, callback, false);
    }

    public static DialogGetTextOrNumber newInstance(String tag, String title, View targetView, OnTextIntroducedListener callback, boolean number) {
        DialogGetTextOrNumber dialog = new DialogGetTextOrNumber();

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("tag", tag);
        args.putBoolean("number", number);
        dialog.setArguments(args);

        mCallback = callback;
        target = targetView;

        return dialog;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        final Bundle a = getArguments();

        mNumber = a.getBoolean("number");

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                a.getString("title")
        );
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_get_text, null);

        etText = (EditText) view.findViewById(R.id.etText);

        if(mNumber) {
            etText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        } else {
            etText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        }

        ViewUtil.showKeyboard(getActivity(), etText);

        builder.setPositiveButton(R.string.dialog_get_text_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ViewUtil.hideKeyboard(getActivity(), etText);
                if(mCallback != null){
                    mCallback.onTextIntroduced(a.getString("tag"), etText.getText().toString(), target);
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_get_text_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ViewUtil.hideKeyboard(getActivity(), etText);
            }
        });

        builder.setView(view);


        return builder.create();

    }


}
