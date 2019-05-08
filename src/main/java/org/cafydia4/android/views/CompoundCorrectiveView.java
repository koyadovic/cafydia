package org.cafydia4.android.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.core.Meal;
import org.cafydia4.android.recommendations.Corrective;
import org.cafydia4.android.recommendations.CorrectiveComplex;
import org.cafydia4.android.recommendations.CorrectiveSimple;
import org.cafydia4.android.util.C;

/**
 * Created by user on 27/10/14.
 */
public class CompoundCorrectiveView extends LinearLayout {
    private Corrective mCorrective;
    private Meal mMeal;
    private Boolean mActivated = false;

    private TextView tvName;
    private TextView tvModification;
    private OnSwitchChangeListener mCallBack;

    public CompoundCorrectiveView(Context c){
        this(c, null);
    }

    public CompoundCorrectiveView(Context c, AttributeSet attr){
        this(c, attr, 0);
    }

    public CompoundCorrectiveView(Context c, AttributeSet attr, int style){
        super(c, attr, style);

        LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.compound_corrective_view, this);

        tvName = (TextView) findViewById(R.id.tvName);
        tvModification = (TextView) findViewById(R.id.tvModification);

        setClickable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchActivation();
            }
        });
    }

    // this methods are a must
    public void setCorrective(Corrective c){
        mCorrective = c;
        checkIfRefresh();
    }

    public void setMeal(Meal m){
        mMeal = m;
        checkIfRefresh();
    }

    // to switch status
    public void switchActivation(){
        mActivated = ! mActivated;

        mCorrective.setTemporalState(mActivated);
        refreshView();
        if(mCallBack != null){
            mCallBack.onSwitchChange(mCorrective);
        }
    }

    private void checkIfRefresh(){
        if(mCorrective != null && mMeal != null){

            if(mCorrective.getTemporalState() != null){
                mActivated = mCorrective.getTemporalState();
            } else {
                mActivated = mCorrective.applies(mMeal);
                mCorrective.setTemporalState(mActivated);
            }

            refreshText();
            refreshView();
        }
    }

    // to query status and get Corrective
    public Corrective getCorrective(){
        return mCorrective;
    }

    public boolean isActivated(){
        return mActivated;
    }


    /*
     * To refresh the view and the TextViews information
     */
    private void refreshText(){
        if(mCorrective.getType().equals(C.CORRECTIVE_TYPE_SIMPLE)){
            CorrectiveSimple c = (CorrectiveSimple) mCorrective;
            tvName.setText(c.getName());
            if(c.getModificationType().equals(C.CORRECTIVE_MODIFICATION_TYPE_NUMBER)){
                if(c.getModification() > 0){
                    tvModification.setText("+" + c.getModification().toString());
                } else {
                    tvModification.setText(c.getModification().toString());
                }
            } else {
                if(c.getModification() > 0){
                    tvModification.setText("+" + c.getModification().toString() + "%");
                } else {
                    tvModification.setText(c.getModification().toString() + "%");
                }
            }
        } else {
            CorrectiveComplex c = (CorrectiveComplex) mCorrective;
            tvName.setText(c.getName());

            Float mod = 0f;
            switch(mMeal.getMealTime()){
                case C.MEAL_BREAKFAST:
                    mod = c.getModificationBr();
                    break;
                case C.MEAL_LUNCH:
                    mod = c.getModificationLu();
                    break;
                case C.MEAL_DINNER:
                    mod = c.getModificationDi();
                    break;

            }

            if(c.getModificationType().equals(C.CORRECTIVE_MODIFICATION_TYPE_NUMBER)){
                if(mod >= 0){
                    tvModification.setText("+" + mod.toString());
                } else {
                    tvModification.setText(mod.toString());
                }
            } else {
                if(mod >= 0){
                    tvModification.setText("+" + mod.toString() + "%");
                } else {
                    tvModification.setText(mod.toString() + "%");
                }
            }

        }
    }

    private void refreshView(){
        if(mActivated){
            setBackgroundResource(R.drawable.compound_corrective_view_selected_bg);
        } else {
            setBackgroundResource(R.drawable.compound_corrective_view_not_selected_bg);
        }
    }

    public void registerActivityForCallBackMethod(Activity activity){
        mCallBack = (OnSwitchChangeListener) activity;
    }

    public interface OnSwitchChangeListener {
        void onSwitchChange(Corrective c);
    }
}
