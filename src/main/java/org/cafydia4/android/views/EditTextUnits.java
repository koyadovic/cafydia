package org.cafydia4.android.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.util.ViewUtil;

import java.util.ArrayList;

/**
 * Created by user on 17/03/15.
 */
public class EditTextUnits extends LinearLayout {
    private EditText mUnits;
    private TextView mUnit;
    private Context mContext;

    public EditTextUnits(Context c){
        this(c, null);
    }

    public EditTextUnits(Context c, AttributeSet attr){
        this(c, attr, 0);
    }

    public EditTextUnits(Context c, AttributeSet attr, int style) {
        super(c, attr, style);

        mContext = c;

        setClickable(true);

        LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.edit_text_units, this);

        mUnits = (EditText) findViewById(R.id.etUnits);

        mUnit = (TextView) findViewById(R.id.tvUnit);

        mUnit.setText(getResources().getString(R.string.food_selected_unit_units));

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUnits.getSelectionEnd() - mUnits.getSelectionStart() == mUnits.getText().toString().length()) {
                    cursorAtEnd();
                } else {
                    selectAll();
                }
                ViewUtil.showKeyboard(getContext(), mUnits);
            }
        });

        mUnits.addTextChangedListener(new TextWatcher() {
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

    public void setUnits(int units) {
        mUnits.setText(Integer.toString(units));
    }

    public int getUnits() {
        if(mUnits.getText().toString().equals("")){
            return 0;
        } else {
            return Integer.parseInt(mUnits.getText().toString());
        }
    }

    public EditText getInternalEditTextReference(){
        return mUnits;
    }

    public void selectAll(){
        mUnits.selectAll();
        mUnits.requestFocus();
        mUnits.setCursorVisible(true);

    }
    public void cursorAtEnd(){
        mUnits.requestFocus();
        mUnits.setCursorVisible(true);
        mUnits.setSelection(mUnits.getText().length());
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
