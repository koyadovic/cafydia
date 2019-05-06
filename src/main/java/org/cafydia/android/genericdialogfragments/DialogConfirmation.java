package org.cafydia.android.genericdialogfragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.cafydia.android.R;
import org.cafydia.android.dialogfragments.CafydiaAlertDialog;

/**
 * Created by user on 18/02/15.
 */
public class DialogConfirmation extends DialogFragment {
    private static OnConfirmListener mCallback;
    private static Object mObject;

    public interface OnConfirmListener {
        void onConfirmPerformed(String tag, boolean confirmation, Object object);
    }

    public static DialogConfirmation newInstance(String tag, OnConfirmListener callback, int titleResourceId, int messageResourceId, Object object){
        DialogConfirmation dialog = new DialogConfirmation();
        Bundle args = new Bundle();

        args.putString("tag", tag);
        args.putInt("title_resource_id", titleResourceId);
        args.putInt("message_resource_id", messageResourceId);

        dialog.setArguments(args);

        mCallback = callback;
        mObject = object;

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // get arguments
        Bundle args = getArguments();
        int titleId = args.getInt("title_resource_id");
        int messageId = args.getInt("message_resource_id");
        final String tag = args.getString("tag");

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                R.drawable.ic_action_warning,
                titleId
            );

        // build the dialog
        builder.setMessage(messageId);

        builder.setPositiveButton(R.string.dialog_confirmation_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mCallback != null) {
                    mCallback.onConfirmPerformed(tag, true, mObject);
                }
            }
        });

        builder.setNegativeButton(R.string.dialog_confirmation_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mCallback != null) {
                    mCallback.onConfirmPerformed(tag, false, mObject);
                }
            }
        });

        return builder.create();
    }
}
