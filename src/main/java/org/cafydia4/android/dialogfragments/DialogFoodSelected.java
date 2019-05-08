package org.cafydia4.android.dialogfragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import org.cafydia4.android.R;
import org.cafydia4.android.core.Food;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.MyRound;
import org.cafydia4.android.util.UnitChanger;
import org.cafydia4.android.util.ViewUtil;
import org.cafydia4.android.views.EditTextUnits;
import org.cafydia4.android.views.EditTextWeight;

/**
 * Created by user on 25/08/14.
 */
public class DialogFoodSelected extends DialogFragment {

    private OnFoodFinallySelectedListener mCallBack;
    private Float mTopWeightValue;
    private Integer mTopUnitValue;

    //private EditText mEtWeight;
    //private EditText mEtUnits;

    private EditTextWeight mEditTextWeight;
    private EditTextUnits mEditTextUnits;

    private boolean mIsOnlyUnits;

    private Food mSelectedFood;
    private UnitChanger mChange;

    public static DialogFoodSelected newInstance(String foodJSon){
        DialogFoodSelected d = new DialogFoodSelected();
        Bundle args = new Bundle();
        args.putString("food_selected", foodJSon);
        d.setArguments(args);

        return d;
    }
    public static DialogFoodSelected newInstance(String foodJSon, Float topWeightValue){
        DialogFoodSelected d = new DialogFoodSelected();
        Bundle args = new Bundle();
        args.putString("food_selected", foodJSon);
        args.putFloat("top_weight_value", topWeightValue);
        d.setArguments(args);

        return d;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        mChange = new UnitChanger(getActivity());

        // inflate the root layout
        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                R.string.dialog_selecting_food_title
        );
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialog = inflater.inflate(R.layout.dialog_selecting_food, null);

        // workAndGetResults everything in the layout
        TextView name = (TextView) dialog.findViewById(R.id.textViewName);
        TextView weightTitle = (TextView) dialog.findViewById(R.id.textViewWeightTitle);
        TextView unitsTitle = (TextView) dialog.findViewById(R.id.textViewUnits);
        TextView tvMaxSelectable = (TextView) dialog.findViewById(R.id.tvMaxSelectable);

        mEditTextWeight = (EditTextWeight) dialog.findViewById(R.id.editTextWeight);
        mEditTextUnits = (EditTextUnits) dialog.findViewById(R.id.editTextUnits);

        Gson gson = new Gson();
        mSelectedFood = gson.fromJson(getArguments().getString("food_selected"), C.TYPE_TOKEN_TYPE_FOOD);

        // asign top values for weight and units
        mTopWeightValue = getArguments().getFloat("top_weight_value");
        mTopUnitValue = (int) (mTopWeightValue / mSelectedFood.getWeightPerUnitInGrams());

        name.setText(mSelectedFood.getName());

        builder.setView(dialog);

        mIsOnlyUnits = !(mSelectedFood.getWeightPerUnitInGrams() == 0.0);

        if(!mIsOnlyUnits){
            weightTitle.setText(getResources().getString(R.string.dialog_selecting_food_fragment_weight_title));

            if(mTopWeightValue != null && mTopWeightValue > 0.0) {

                mEditTextWeight.setWeightInGrams(mTopWeightValue);

                // significa que hay un maximo ya especificado

                tvMaxSelectable.setText(
                        getResources().getString(R.string.dialog_selecting_food_maximum_selectable) +
                                " " +
                                MyRound.round(
                                        mChange.toUIFromInternalWeight(mTopWeightValue),
                                        mChange.getDecimalsForWeight()
                                ).toString() + " " +
                                mChange.getStringUnitForWeightInTheUI()
                );

            } else {
                // si no hay tope, ocultamos el footer y mostramos el teclado
                tvMaxSelectable.setVisibility(View.GONE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ViewUtil.showKeyboard(getActivity(), mEditTextWeight.getInternalEditTextReference());
                    }
                }, 200);

            }

            unitsTitle.setVisibility(View.GONE);
            mEditTextUnits.setVisibility(View.GONE);


        } else {
            unitsTitle.setText(getResources().getString(R.string.dialog_selecting_food_units_title));

            if(mTopUnitValue > 0) {
                Integer u = (int)(mSelectedFood.getWeightInGrams() / mSelectedFood.getWeightPerUnitInGrams());
                mEditTextUnits.setUnits(u);

                tvMaxSelectable.setText(getResources().getString(R.string.dialog_selecting_food_maximum_selectable) +
                        " " + u.toString() + " " + getResources().getString(R.string.dialog_selecting_food_units_unit));
            } else {
                // si no hay tope, ocultamos el footer y mostramos el teclado
                tvMaxSelectable.setVisibility(View.GONE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ViewUtil.showKeyboard(getActivity(), mEditTextUnits.getInternalEditTextReference());
                    }
                }, 200);
            }

            weightTitle.setVisibility(View.GONE);
            mEditTextWeight.setVisibility(View.GONE);

        }


        builder.setPositiveButton(R.string.dialog_selecting_food_select, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ViewUtil.hideKeyboard(getActivity(), mEditTextUnits.getInternalEditTextReference());

                if(mIsOnlyUnits && mEditTextUnits.getUnits() != 0){
                    mSelectedFood.setWeight(mEditTextUnits.getUnits() * mSelectedFood.getWeightPerUnitInGrams());
                    mCallBack.onFoodFinallySelected(mSelectedFood);

                } else if (!mIsOnlyUnits && mEditTextWeight.getWeightInGrams() != 0.0f){
                    mSelectedFood.setWeight(mEditTextWeight.getWeightInGrams());
                    mCallBack.onFoodFinallySelected(mSelectedFood);
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_selecting_food_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ViewUtil.hideKeyboard(getActivity(), mEditTextUnits.getInternalEditTextReference());
            }
        });

        if(getParentFragment() != null) {
            mCallBack = (OnFoodFinallySelectedListener) getParentFragment();
        } else {
            mCallBack = (OnFoodFinallySelectedListener) getActivity();
        }

        return builder.create();
    }

    public interface OnFoodFinallySelectedListener {
        void onFoodFinallySelected(Food food);
    }

}
