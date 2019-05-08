package org.cafydia4.android.dialogfragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;

import org.cafydia4.android.R;
import org.cafydia4.android.configdatabase.ConfigurationDatabase;
import org.cafydia4.android.core.Food;
import org.cafydia4.android.interfaces.OnFoodEdited;
import org.cafydia4.android.interfaces.OnFoodModifiedInterface;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.MyRound;
import org.cafydia4.android.util.UnitChanger;
import org.cafydia4.android.util.ViewUtil;
import org.cafydia4.android.views.EditTextCarbohydratePercentage;
import org.cafydia4.android.views.EditTextWeight;

/**
 * Created by user on 23/08/14.
 */
@SuppressWarnings("NullableProblems")
public class DialogFoodEditor extends DialogFragment implements OnFoodModifiedInterface {

    private static final String FOOD_TO_EDIT = "food_to_edit";
    private static final String CALLER_POSITION = "caller_position";
    private static final String NEW_FOOD = "new_food";

    private int mCallerPosition;
    private OnFoodEdited mCallBack;

    private UnitChanger mChange;

    // to get new instances of the fragment
    public static DialogFoodEditor newInstance(String foodJSon, int callerPosition){
        DialogFoodEditor editor = new DialogFoodEditor();

        Bundle args = new Bundle();
        if(foodJSon == null) {
            args.putString(FOOD_TO_EDIT, NEW_FOOD);
        }else {
            args.putString(FOOD_TO_EDIT, foodJSon);
        }
        args.putInt(CALLER_POSITION, callerPosition);
        editor.setArguments(args);

        return editor;
    }

    public void onAttachFragment(Fragment fragment){
        if(fragment != null) {
            mCallBack = (OnFoodEdited) fragment;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                R.string.dialog_food_editor_title
        );

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialog = inflater.inflate(R.layout.dialog_editing_food, null);

        final EditText etName = (EditText) dialog.findViewById(R.id.editTextName);
        //final EditText etCarb = (EditText) dialog.findViewById(R.id.editTextCarb);
        //final EditText etWeightUnit = (EditText) dialog.findViewById(R.id.editTextWeightForUnit);

        final EditTextCarbohydratePercentage carbohydratePercentage = (EditTextCarbohydratePercentage) dialog.findViewById(R.id.carbohydratePercentage);
        final EditTextWeight weightPerUnit = (EditTextWeight) dialog.findViewById(R.id.weightPerUnit);

        //TextView tvWeightUnits = (TextView) dialog.findViewById(R.id.tvWeightUnits);

        Gson gson = new Gson();
        String foodJSon = getArguments().getString(FOOD_TO_EDIT);

        Food food;

        if(foodJSon == null || foodJSon.equals(NEW_FOOD)){
            food = new Food();
        } else {
            food = gson.fromJson(getArguments().getString(FOOD_TO_EDIT), C.TYPE_TOKEN_TYPE_FOOD);
            etName.setText(food.getName());
            carbohydratePercentage.setCarbohydratePercentage(MyRound.round(food.getCPercent()));
            if(food.getWeightPerUnitInGrams() != 0.0) {
                weightPerUnit.setWeightInGrams(food.getWeightPerUnitInGrams());
            }
        }

        mCallerPosition = getArguments().getInt(CALLER_POSITION);

        final Food finalFood = food;

        builder.setView(dialog);

        builder.setPositiveButton(getResources().getString(R.string.dialog_food_editor_button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = etName.getText().toString();
                float carb = carbohydratePercentage.getCarbohydratePercentage();
                float weightPerUnitF = weightPerUnit.getWeightInGrams();

                finalFood.setName(name);
                finalFood.setCPercent(carb);
                finalFood.setUnitWeight(weightPerUnitF);

                // save changes to database
                ConfigurationDatabase db = new ConfigurationDatabase(getActivity());
                finalFood.save(db);

                // hide the keyboard
                if(carbohydratePercentage.getInternalEditTextReference().hasFocus()) {
                    ViewUtil.hideKeyboard(getActivity(), carbohydratePercentage.getInternalEditTextReference());
                }
                else if (etName.hasFocus()){
                    ViewUtil.hideKeyboard(getActivity(), etName);
                }
                else {
                    ViewUtil.hideKeyboard(getActivity(), weightPerUnit.getInternalEditTextReference());
                }

                mCallBack.onFoodEdited(mCallerPosition, finalFood);

            }
        });
        builder.setNegativeButton(getResources().getString(R.string.dialog_food_editor_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // hide the keyboard
                // hide the keyboard
                if(carbohydratePercentage.getInternalEditTextReference().hasFocus()) {
                    ViewUtil.hideKeyboard(getActivity(), carbohydratePercentage.getInternalEditTextReference());
                }
                else if (etName.hasFocus()){
                    ViewUtil.hideKeyboard(getActivity(), etName);
                }
                else {
                    ViewUtil.hideKeyboard(getActivity(), weightPerUnit.getInternalEditTextReference());
                }
            }
        });

        onAttachFragment(getParentFragment());

        return builder.create();
    }

    public void onFoodModified(int action, int positionInTheViewPager, Food food) {

    }
}
