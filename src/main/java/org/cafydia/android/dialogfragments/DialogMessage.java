package org.cafydia.android.dialogfragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.cafydia.android.R;

/**

    Si se le pasa el shared preferences tag recuerda si fue marcado para no volver a ser mostrado

 */
public class DialogMessage extends DialogFragment {
    private static final String DIALOG_MESSAGE_TAG = "dialog_message";
    private String mSharedTagMessage;
    private String mTitle;
    private String mMessage;

    private TextView tvMessage;
    private CheckBox cbNotShowAgain;

    private Boolean mShowAgain = true;


    public static boolean shouldBeShown(Context c, String sharedPreferencesTagMessage){
        SharedPreferences sp = c.getSharedPreferences(DIALOG_MESSAGE_TAG, Context.MODE_PRIVATE);
        return sp.getBoolean(sharedPreferencesTagMessage, true);
    }

    public static DialogMessage newInstance(String title, String message){
        return newInstance(title, message, "");
    }
    public static DialogMessage newInstance(String title, String message, String sharedPreferencesTagMessage){
        DialogMessage dialog = new DialogMessage();
        Bundle args = new Bundle();

        args.putString("shared_preferences_tag_message", sharedPreferencesTagMessage);
        args.putString("title", title);
        args.putString("message", message);

        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        super.onCreateDialog(savedInstanceState);

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                R.drawable.ic_action_warning,
                mTitle
        );
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_message, null);

        searchViews(view);
        init();

        builder.setView(view);

        builder.setTitle(Html.fromHtml("<font color='" + getString(R.string.dialog_title_color) + "'>" + mTitle + "</font>"));
        tvMessage.setText(Html.fromHtml(mMessage));

        if(mSharedTagMessage.equals("")) {
           cbNotShowAgain.setVisibility(View.GONE);
        } else {
            cbNotShowAgain.setChecked(! mShowAgain);
        }

        builder.setPositiveButton(getString(R.string.dialog_message_ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(! mSharedTagMessage.equals("") && mShowAgain == cbNotShowAgain.isChecked()){
                    SharedPreferences sp = getActivity().getSharedPreferences(DIALOG_MESSAGE_TAG, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();

                    editor.putBoolean(mSharedTagMessage, ! cbNotShowAgain.isChecked());
                    editor.apply();
                }
            }
        });

        return builder.create();
    }

    private void searchViews(View view){
        tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        cbNotShowAgain = (CheckBox) view.findViewById(R.id.cbNotShowAgain);
    }

    private void init(){
        Bundle args = getArguments();

        mTitle = args.getString("title");
        mMessage = args.getString("message");
        mSharedTagMessage = args.getString("shared_preferences_tag_message");

        if(! mSharedTagMessage.equals("")) {
            SharedPreferences sp = getActivity().getSharedPreferences(DIALOG_MESSAGE_TAG, Context.MODE_PRIVATE);
            mShowAgain = sp.getBoolean(mSharedTagMessage, true);
        }


    }
}
