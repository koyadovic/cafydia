package org.cafydia4.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Thanks to: http://stackoverflow.com/a/7855852/726954
 */
public class VerticalTextView extends TextView {
    final boolean topDown;

    public VerticalTextView(Context context, AttributeSet attrs){
        super(context, attrs);
        final int gravity = getGravity();
        if(Gravity.isVertical(gravity) && (gravity&Gravity.VERTICAL_GRAVITY_MASK) == Gravity.BOTTOM) {
            setGravity((gravity&Gravity.HORIZONTAL_GRAVITY_MASK) | Gravity.TOP);
            topDown = false;
        }else
            topDown = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b){
        return super.setFrame(l, t, l+(b-t), t+(r-l));
    }

    @Override
    public void draw(Canvas canvas){
        int h = getHeight();
        int w = getWidth();
        if(topDown){
            canvas.translate(h, 0);
            canvas.rotate(90);
        }else {
            canvas.translate(0, w);
            canvas.rotate(-90);
        }
        canvas.clipRect(0, 0, w, h, Region.Op.INTERSECT);
        super.draw(canvas);
    }
}
