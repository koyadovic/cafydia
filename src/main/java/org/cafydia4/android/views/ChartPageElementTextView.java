package org.cafydia4.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import org.cafydia4.android.chartobjects.ChartPageElement;

/**
 * Created by user on 17/02/15.
 */
public class ChartPageElementTextView extends TextView {

    private ChartPageElement mChartPageElement;

    public ChartPageElementTextView(Context c){
        this(c, (AttributeSet) null);
    }

    public ChartPageElementTextView(Context c, ChartPageElement element){
        this(c, (AttributeSet) null);
        setText(element.getTextHeader());
        mChartPageElement = element;
    }

    public ChartPageElementTextView(Context c, AttributeSet attrs){
        super(c, attrs);
    }

    public ChartPageElement getChartPageElement() {
        return mChartPageElement;
    }

    public void setChartPageElement(ChartPageElement element) {
        setText(element.getTextHeader());
        this.mChartPageElement = element;
    }
}
