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
public class EditTextGlucose extends LinearLayout {
    private EditText mGlucoseLevel;
    private TextView mUnit;
    private Context mContext;
    private UnitChanger mUnitChanger;

    public EditTextGlucose(Context c){
        this(c, null);
    }

    public EditTextGlucose(Context c, AttributeSet attr){
        this(c, attr, 0);
    }

    public EditTextGlucose(Context c, AttributeSet attr, int style) {
        super(c, attr, style);

        mContext = c;

        setClickable(true);

        LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.edit_text_glucose, this);

        mGlucoseLevel = (EditText) findViewById(R.id.etGlucoseLevel);
        mUnit = (TextView) findViewById(R.id.tvUnit);

        mUnitChanger = new UnitChanger(mContext);
        mUnit.setText(mUnitChanger.getStringUnitForGlucose());

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mGlucoseLevel.getSelectionEnd() - mGlucoseLevel.getSelectionStart() == mGlucoseLevel.getText().toString().length()) {
                    cursorAtEnd();
                } else {
                    selectAll();
                }
                cursorAtEnd();
                selectAll();
                ViewUtil.showKeyboard(getContext(), mGlucoseLevel);
            }
        });

        mGlucoseLevel.addTextChangedListener(new TextWatcher() {
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

    public void setGlucoseLevel(float glucoseLevel) {
        mGlucoseLevel.setText(MyRound.round(mUnitChanger.toUIFromInternalGlucose(glucoseLevel), mUnitChanger.getDecimalsForGlucose()).toString());
    }

    public float getGlucoseLevelMgDl() {
        if(mGlucoseLevel.getText().toString().equals("")){
            return 0.0f;
        } else {
            Float level = Float.parseFloat(mGlucoseLevel.getText().toString());
            return mUnitChanger.toInternalGlucoseFromUI(level);
        }
    }

    public EditText getInternalEditTextReference(){
        return mGlucoseLevel;
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
        mGlucoseLevel.requestFocus();
        mGlucoseLevel.setCursorVisible(true);
        mGlucoseLevel.selectAll();

    }
    public void cursorAtEnd(){
        mGlucoseLevel.requestFocus();
        mGlucoseLevel.setCursorVisible(true);
        mGlucoseLevel.setSelection(mGlucoseLevel.getText().length());
    }

    public void white(){
        setBackgroundResource(R.drawable.bg_edit_text_white);
        getInternalEditTextReference().setTextColor(0xFFFFFFFF);
        mUnit.setTextColor(0xFFCCCCCC);
    }
}
