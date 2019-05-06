package org.cafydia.android.dialogfragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.cafydia.android.R;

/**
 * Created by user on 21/03/15.
 */
public class CafydiaExceptionDialog extends DialogFragment {

    public static void show(Activity c, String text){
        String stackTrace = "";

        for(StackTraceElement element : Thread.currentThread().getStackTrace()) {
            stackTrace += element.toString() + "\n";
        }

        newInstance(text, stackTrace).show(c.getFragmentManager(), null);
    }

    private static CafydiaExceptionDialog newInstance(String text, String exception) {
        CafydiaExceptionDialog dialog = new CafydiaExceptionDialog();
        Bundle args = new Bundle();

        args.putString("text", text);
        args.putString("exception", exception);

        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                "Exception"
        );

        View view = getActivity().getLayoutInflater().inflate(R.layout.cafydia_exception_dialog, null);

        Bundle args = getArguments();
        String text = args.getString("text");
        String exceptionText = args.getString("exception");

        ((EditText) view.findViewById(R.id.editTextText)).setText(text);
        ((EditText) view.findViewById(R.id.editTextException)).setText(exceptionText);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        builder.setView(view);

        return builder.create();
    }
}
