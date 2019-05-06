package org.cafydia.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cafydia.android.R;
import org.cafydia.android.util.C;

/**
 * Created by user on 19/03/15.
 */
public class ModificationZoneView extends LinearLayout {
    private TextView left, all, right;
    private Context mContext;
    private OnModificationZoneChangeListener mCallback;
    private int mTypeOfModification;

    public ModificationZoneView(Context c){
        this(c, null);
    }

    public ModificationZoneView(Context c, AttributeSet attr){
        this(c, attr, 0);
    }

    public ModificationZoneView(Context c, AttributeSet attr, int style) {
        super(c, attr, style);

        mContext = c;

        LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.compound_modification_zone_view, this);

        left = (TextView) findViewById(R.id.left);
        all = (TextView) findViewById(R.id.all);
        right = (TextView) findViewById(R.id.right);


        left.setOnClickListener(onClickListener);
        all.setOnClickListener(onClickListener);
        right.setOnClickListener(onClickListener);

        mTypeOfModification = C.LINEAR_FUNCTIONS_MODIFICATION_BY_ITS_INDEX;

    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){

                case R.id.left:
                    switchOff(all);
                    switchOff(right);
                    switchOn(left);
                    redrawText();

                    mTypeOfModification = C.LINEAR_FUNCTIONS_MODIFICATION_BY_THE_LEFT;

                    if(mCallback != null) mCallback.onModificationChange(mTypeOfModification);

                    break;

                case R.id.all:
                    switchOff(left);
                    switchOff(right);
                    switchOn(all);
                    redrawText();

                    mTypeOfModification = C.LINEAR_FUNCTIONS_MODIFICATION_BY_ITS_INDEX;

                    if(mCallback != null) mCallback.onModificationChange(mTypeOfModification);

                    break;

                case R.id.right:
                    switchOff(left);
                    switchOff(all);
                    switchOn(right);
                    redrawText();

                    mTypeOfModification = C.LINEAR_FUNCTIONS_MODIFICATION_BY_THE_RIGHT;

                    if(mCallback != null) mCallback.onModificationChange(mTypeOfModification);

                    break;
            }
        }
    };

    public int getTypeOfModification(){
        return mTypeOfModification;
    }

    public void mealBelowAverage(){
        left.setVisibility(View.VISIBLE);
        all.setVisibility(View.VISIBLE);
        right.setVisibility(View.GONE);
    }
    public void mealAboveAverage(){
        left.setVisibility(View.GONE);
        all.setVisibility(View.VISIBLE);
        right.setVisibility(View.VISIBLE);
    }
    public void mealWithoutAverage(){
        left.setVisibility(View.GONE);
        all.setVisibility(View.VISIBLE);
        right.setVisibility(View.GONE);
    }


    private void switchOff(TextView v){
        v.setBackgroundResource(R.drawable.compound_modification_zone_not_selected_bg);
        v.setTextColor(0xFFAAAAAA);
    }
    private void switchOn(TextView v){
        v.setBackgroundResource(R.drawable.compound_modification_zone_selected_bg);
        v.setTextColor(0xFFFFFFFF);
    }
    private void redrawText(){
        left.setText(R.string.activity_baseline_modify_for_only_light_meals);
        all.setText(R.string.activity_baseline_modify_for_all_meals);
        right.setText(R.string.activity_baseline_modify_for_only_copious_meals);
    }

    //
    // Interface to communicate
    //

    public void setOnModificationZoneChangeListener(OnModificationZoneChangeListener callback) {
        mCallback = callback;
    }

    public interface OnModificationZoneChangeListener {
        void onModificationChange(int typeOfModification);
    }

}
