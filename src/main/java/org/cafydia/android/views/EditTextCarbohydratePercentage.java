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
import org.cafydia.android.util.ViewUtil;

import java.util.ArrayList;

/**
 * Created by user on 17/03/15.
 */
public class EditTextCarbohydratePercentage extends LinearLayout {
    private EditText mCarbohydratePercentage;
    private TextView mUnit;
    private Context mContext;

    public EditTextCarbohydratePercentage(Context c){
        this(c, null);
    }

    public EditTextCarbohydratePercentage(Context c, AttributeSet attr){
        this(c, attr, 0);
    }

    public EditTextCarbohydratePercentage(Context c, AttributeSet attr, int style) {
        super(c, attr, style);

        mContext = c;

        setClickable(true);

        LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.edit_text_carbohydrate_percentage, this);

        mCarbohydratePercentage = (EditText) findViewById(R.id.etCarbohydratePercentage);

        mUnit = (TextView) findViewById(R.id.tvUnit);
        mUnit.setText("%");

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCarbohydratePercentage.getSelectionEnd() - mCarbohydratePercentage.getSelectionStart() == mCarbohydratePercentage.getText().toString().length()) {
                    cursorAtEnd();
                } else {
                    selectAll();
                }
                ViewUtil.showKeyboard(getContext(), mCarbohydratePercentage);
            }
        });



        mCarbohydratePercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(mAdditionalTextWatchers != null) {
                    for (TextWatcher tw : mAdditionalTextWatchers) {
                        tw.beforeTextChanged(s, start, count, after);
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("")) {
                    Float p = Float.parseFloat(s.toString());
                    if(p > 100f) {
                        mCarbohydratePercentage.removeTextChangedListener(this);
                        mCarbohydratePercentage.setText("100");
                        cursorAtEnd();
                        mCarbohydratePercentage.addTextChangedListener(this);
                    }
                }

                if(mAdditionalTextWatchers != null) {
                    for (TextWatcher tw : mAdditionalTextWatchers) {
                        tw.onTextChanged(s, start, before, count);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mAdditionalTextWatchers != null) {
                    for (TextWatcher tw : mAdditionalTextWatchers) {
                        tw.afterTextChanged(s);
                    }
                }
            }
        });
    }

    public void setCarbohydratePercentage(float percentage) {
        if(percentage <= 100.0f) {
            mCarbohydratePercentage.setText(Float.toString(percentage));
            mCarbohydratePercentage.setSelection(mCarbohydratePercentage.getText().length());
        }
    }

    public float getCarbohydratePercentage() {
        if(mCarbohydratePercentage.getText().toString().equals("")){
            return 0.0f;
        } else {
            return Float.parseFloat(mCarbohydratePercentage.getText().toString());
        }
    }

    public EditText getInternalEditTextReference(){
        return mCarbohydratePercentage;
    }

    // additional text watcher
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

    public void selectAll(){
        mCarbohydratePercentage.requestFocus();
        mCarbohydratePercentage.setCursorVisible(true);
        mCarbohydratePercentage.selectAll();

    }
    public void cursorAtEnd(){
        mCarbohydratePercentage.requestFocus();
        mCarbohydratePercentage.setCursorVisible(true);
        mCarbohydratePercentage.setSelection(mCarbohydratePercentage.getText().length());
    }

    public void white(){
        setBackgroundResource(R.drawable.bg_edit_text_white);
        getInternalEditTextReference().setTextColor(0xFFFFFFFF);
        mUnit.setTextColor(0xFFCCCCCC);
    }
}
