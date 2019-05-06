package org.cafydia.android.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cafydia.android.R;
import org.cafydia.android.util.MyRound;
import org.cafydia.android.util.UnitChanger;
import org.cafydia.android.util.ViewUtil;

import java.util.ArrayList;

/**
 * Created by user on 17/03/15.
 */
public class EditTextWeight extends LinearLayout {
    private EditText mWeight;
    private TextView mUnit;
    private Context mContext;
    private UnitChanger mUnitChanger;

    public EditTextWeight(Context c){
        this(c, null);
    }

    public EditTextWeight(Context c, AttributeSet attr){
        this(c, attr, 0);
    }

    public EditTextWeight(Context c, AttributeSet attr, int style) {
        super(c, attr, style);

        if(!isInEditMode()) {

            mContext = c;

            setClickable(true);

            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.edit_text_weight, this);

            mWeight = (EditText) findViewById(R.id.etWeight);
            mWeight.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (mAdditionalTextWatchers != null) {
                        for (TextWatcher tw : mAdditionalTextWatchers) {
                            tw.beforeTextChanged(s, start, count, after);
                        }
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (mAdditionalTextWatchers != null) {
                        for (TextWatcher tw : mAdditionalTextWatchers) {
                            tw.onTextChanged(s, start, before, count);
                        }
                    }

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mAdditionalTextWatchers != null) {
                        for (TextWatcher tw : mAdditionalTextWatchers) {
                            tw.afterTextChanged(s);
                        }
                    }
                }
            });

            mUnit = (TextView) findViewById(R.id.tvUnit);
            mUnitChanger = new UnitChanger(mContext);
            mUnit.setText(mUnitChanger.getStringUnitForWeightInTheUI());

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mWeight.getSelectionEnd() - mWeight.getSelectionStart() == mWeight.getText().toString().length()) {
                        cursorAtEnd();
                    } else {
                        selectAll();
                    }
                    ViewUtil.showKeyboard(getContext(), mWeight);
                }
            });
        }

    }

    public void setWeightInGrams(float weight) {
        mWeight.setText(MyRound.round(mUnitChanger.toUIFromInternalWeight(weight), mUnitChanger.getDecimalsForWeight()).toString());
    }

    public float getWeightInGrams() {
        if(mWeight.getText().toString().equals("")){
            return 0.0f;
        } else {
            Float weight = Float.parseFloat(mWeight.getText().toString());
            return mUnitChanger.toInternalWeightFromUI(weight);
        }
    }

    public EditText getInternalEditTextReference(){
        return mWeight;
    }

    public void selectAll(){
        mWeight.requestFocus();
        mWeight.setCursorVisible(true);
        mWeight.selectAll();
    }
    public void cursorAtEnd(){
        mWeight.requestFocus();
        mWeight.setCursorVisible(true);
        mWeight.setSelection(mWeight.getText().length());
    }

    // additional text watchers
    private ArrayList<TextWatcher> mAdditionalTextWatchers = null;

    public void addTextChangedListener(TextWatcher additionalTextWatcher){
        if(mAdditionalTextWatchers == null) {
            mAdditionalTextWatchers = new ArrayList<>();
        }
        mAdditionalTextWatchers.add(additionalTextWatcher);
    }

    public void removeTextChangedListener(TextWatcher additionalTextWatcher){
        if(mAdditionalTextWatchers == null) {
            mAdditionalTextWatchers = new ArrayList<>();
        }
        if(mAdditionalTextWatchers.contains(additionalTextWatcher)) {
            mAdditionalTextWatchers.remove(additionalTextWatcher);
        }
    }

    public void white(){
        setBackgroundResource(R.drawable.bg_edit_text_white);
        getInternalEditTextReference().setTextColor(0xFFFFFFFF);
        mUnit.setTextColor(0xFFCCCCCC);
    }
}
