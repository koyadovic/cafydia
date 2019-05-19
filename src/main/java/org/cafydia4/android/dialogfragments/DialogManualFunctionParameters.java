package org.cafydia4.android.dialogfragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import org.cafydia4.android.R;

import static org.cafydia4.android.recommendations.BaselinePreprandial.SHARED_PREFERENCES_FUNCTION_KEY;


public class DialogManualFunctionParameters extends DialogFragment {
    EditText brmEditText;
    EditText brbEditText;
    EditText lumEditText;
    EditText lubEditText;
    EditText dimEditText;
    EditText dibEditText;

    // to get new instances of the fragment
    public static DialogManualFunctionParameters newInstance(){
        DialogManualFunctionParameters dialog = new DialogManualFunctionParameters();
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.linear_parameters_manual, null);
        Context context = getContext();

        final SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_FUNCTION_KEY, Context.MODE_PRIVATE);

        this.findUIElements(view);
        this.populateUIElements(sp);

        builder.setView(view)
               .setPositiveButton(R.string.dialog_confirmation_button_ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                       System.out.println("lalala");
                       saveCurrentValues(sp);
                       dismiss();
                   }
               })

               .setNegativeButton(R.string.dialog_confirmation_button_cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                       dismiss();
                   }
               });

        return builder.create();
    }

    private void findUIElements(View view) {
        brmEditText = view.findViewById(R.id.brm);
        brbEditText = view.findViewById(R.id.brb);
        lumEditText = view.findViewById(R.id.lum);
        lubEditText = view.findViewById(R.id.lub);
        dimEditText = view.findViewById(R.id.dim);
        dibEditText = view.findViewById(R.id.dib);
    }

    private void populateUIElements(SharedPreferences sp) {
        fillEditTextValue(sp, "brm", brmEditText);
        fillEditTextValue(sp, "brb", brbEditText);
        fillEditTextValue(sp, "lum", lumEditText);
        fillEditTextValue(sp, "lub", lubEditText);
        fillEditTextValue(sp, "dim", dimEditText);
        fillEditTextValue(sp, "dib", dibEditText);
    }

    private void fillEditTextValue(SharedPreferences sp, String preferenceKey, EditText et) {
        Float v = sp.getFloat(preferenceKey, 0);
        et.setText(v.toString());
    }

    private void saveCurrentValues(SharedPreferences sp) {
        saveEditTextFloatValueToSharedPreference(sp, "brm", brmEditText);
        saveEditTextFloatValueToSharedPreference(sp, "brb", brbEditText);
        saveEditTextFloatValueToSharedPreference(sp, "lum", lumEditText);
        saveEditTextFloatValueToSharedPreference(sp, "lub", lubEditText);
        saveEditTextFloatValueToSharedPreference(sp, "dim", dimEditText);
        saveEditTextFloatValueToSharedPreference(sp, "dib", dibEditText);
    }

    private void saveEditTextFloatValueToSharedPreference(SharedPreferences sp, String preferenceKey, EditText et) {
        float v = getFloat(et.getText().toString());
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(preferenceKey, v);
        editor.apply();
    }

    private float getFloat(String str){
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
