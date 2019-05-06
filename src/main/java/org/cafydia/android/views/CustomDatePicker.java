package org.cafydia.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.DatePicker;

import org.cafydia.android.core.Instant;

/**
 * Created by user on 2/10/14.
 */
public class CustomDatePicker extends DatePicker {

    private Instant instant;
    private OnDatePickedListener mCallback;

    public CustomDatePicker(Context context, AttributeSet attrs){
        super(context, attrs);
        instant = new Instant();
    }

    public void increaseOneMonth(){
        instant.increaseOneMonth();
        updateUI();
    }
    public void decreaseOneMonth(){
        instant.decreaseOneMonth();
        updateUI();
    }

    public Instant getInstant(){
        return instant;
    }

    public void setInstant(Instant i){
        instant = i != null ? i : new Instant();
        init(instant.getYear(), instant.getMonth(), instant.getDay(), new OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                instant.setYearMonthDay(year, monthOfYear, dayOfMonth);
                if(mCallback != null) {
                    mCallback.onDatePicked(CustomDatePicker.this, instant);
                }
            }
        });
        updateUI();
    }
    public void setInstantToNow(){
        setInstant(new Instant());
    }

    private void updateUI(){
        updateDate(instant.getYear(), instant.getMonth(), instant.getDay());
    }

    public interface OnDatePickedListener {
        void onDatePicked(CustomDatePicker datePicker, Instant i);
    }

    public void setOnDatePickedListener(OnDatePickedListener listener){
        mCallback = listener;
    }


}
