package org.cafydia4.android.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.dialogfragments.CafydiaAlertDialog;
import org.cafydia4.android.util.MyRound;
import org.cafydia4.android.util.UnitChanger;

import java.util.ArrayList;

/**
 * Created by user on 22/04/15.
 */
public class GlucoseRingChart extends View implements View.OnClickListener {
    private Context mContext;
    private RectF mRectF;
    private Paint mPaint;
    private float mDensity;

    private ArrayList<String> mTitles;
    private ArrayList<Integer> mInnerColors;
    private ArrayList<Integer> mOuterColors;
    private ArrayList<Float> mValues;

    private String mRingTitle = "";

    private Float mTotal = 0f;
    private Float mInterpolator = 1f;

    private AnimationSet mStartAnimation;
    private ValueAnimator mSectionAnimator;

    public GlucoseRingChart(Context c, AttributeSet attrs){
        super(c, attrs);

        mContext = c;

        mDensity = c.getResources().getDisplayMetrics().density;

        mRectF = new RectF();


        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getColor(R.color.chart_view_text));


        mTitles = new ArrayList<>();
        mInnerColors = new ArrayList<>();
        mOuterColors = new ArrayList<>();
        mValues = new ArrayList<>();

        mStartAnimation = new AnimationSet(c, attrs);

        Animation scale = new ScaleAnimation(0f, 1f, 0f, 1f,Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        Animation alpha = new AlphaAnimation(0f, 1f);

        mSectionAnimator = ValueAnimator.ofFloat(0f, 1f);
        mSectionAnimator.setInterpolator(new OvershootInterpolator());
        mSectionAnimator.setDuration(1200);
        mSectionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mInterpolator = (Float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        mSectionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scale.setInterpolator(new OvershootInterpolator());
        alpha.setInterpolator(new OvershootInterpolator());


        mStartAnimation.setDuration(1200);

        mStartAnimation.addAnimation(scale);
        //mStartAnimation.addAnimation(alpha);

        setClickable(true);
        setOnClickListener(this);

    }

    public void setRingTitle(String ringTitle) {
        mRingTitle = ringTitle;
    }

    public void reset(){
        mTitles.clear();
        mInnerColors.clear();
        mOuterColors.clear();
        mValues.clear();
        mTotal = 0f;
    }

    @Override
    protected void onDraw(Canvas canvas){
        float r = getWidth() > getHeight() ? getHeight() / 2f : getWidth() / 2f;

        // the size of the mPaint stroke / 2
        r -= dp(10);

        float xCenter = getWidth() / 2f;
        float yCenter = getHeight() / 2f;

        mRectF.set(xCenter - r, yCenter - r, xCenter + r, yCenter + r);

        if(mTitles.size() > 0){
            float startAngle = 180f;

            for(int n=0; n<mTitles.size(); n++){

                float percentage, sweepAngle;

                if(mTotal.intValue() > 0) {
                    percentage = (mValues.get(n) * 100f) / mTotal;
                    sweepAngle = (percentage / 100f) * 360f;
                } else {
                    sweepAngle = 360f / mTitles.size();
                }

                // for the animator
                sweepAngle *= mInterpolator;

                if(mTotal.intValue() == 0 || mValues.get(n).intValue() > 0) {
                    mPaint.setStrokeWidth(dp(20));
                    mPaint.setColor(mOuterColors.get(n));
                    canvas.drawArc(mRectF, startAngle , sweepAngle + 3f, false, mPaint);

                    mPaint.setStrokeWidth(dp(10));
                    mPaint.setColor(mInnerColors.get(n));
                    canvas.drawArc(mRectF, startAngle , sweepAngle + 3f, false, mPaint);
                }

                startAngle += sweepAngle;
            }
        }

    }


    public void addSection(String title, int innerColor, int outerColor, float value){
        mTitles.add(title);
        mInnerColors.add(innerColor);
        mOuterColors.add(outerColor);
        mValues.add(value);

        mTotal += value;
    }

    public void refresh(){
        mInterpolator = 1f;
        invalidate();
    }

    public void refreshAnimatedly(){
        startAnimation(mStartAnimation);
        mSectionAnimator.start();
    }

    @Override
    public void onClick(View v){
        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                mContext,
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                getResources().getString(R.string.glucose_ring_chart_title) + (!mRingTitle.equals("") ? " " + mRingTitle : "")
        );
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.glucose_ring_details, null);

        TextView tvTotal = (TextView) view.findViewById(R.id.tvTotal);
        TextView tvLow = (TextView) view.findViewById(R.id.tvLow);
        TextView tvGood = (TextView) view.findViewById(R.id.tvGood);
        TextView tvRegular = (TextView) view.findViewById(R.id.tvRegular);
        TextView tvBad = (TextView) view.findViewById(R.id.tvBad);

        TextView tvLowTitle = (TextView) view.findViewById(R.id.tvLowTitle);
        TextView tvGoodTitle = (TextView) view.findViewById(R.id.tvGoodTitle);
        TextView tvRegularTitle = (TextView) view.findViewById(R.id.tvRegularTitle);
        TextView tvBadTitle = (TextView) view.findViewById(R.id.tvBadTitle);

        UnitChanger c = new UnitChanger(mContext);

        tvLowTitle.setText(getGlucoseLevelString(c, 0f) + " - " + getGlucoseLevelString(c, 60f));
        tvGoodTitle.setText(getGlucoseLevelString(c, 60f) + " - " + getGlucoseLevelString(c, 150f));
        tvRegularTitle.setText(getGlucoseLevelString(c, 150f) + " - " + getGlucoseLevelString(c, 180f));
        tvBadTitle.setText(getGlucoseLevelString(c, 180f) + " - " + getGlucoseLevelString(c, 600f));

        tvTotal.setText(mTotal.intValue() + "");

        tvLow.setText((int) ((mValues.get(0) * 100) / mTotal) + "%");
        tvLow.setText((int) (mTotal > 0 ? ((mValues.get(0) * 100) / mTotal) : 0f) + "%");
        tvLow.setTextColor(getDarkerColor(mInnerColors.get(0)));

        tvGood.setText((int) (mTotal > 0 ? ((mValues.get(1) * 100) / mTotal) : 0f) + "%");
        tvGood.setTextColor(getDarkerColor(mInnerColors.get(1)));

        tvRegular.setText((int) (mTotal > 0 ? ((mValues.get(2) * 100) / mTotal) : 0f) + "%");
        tvRegular.setTextColor(getDarkerColor(mInnerColors.get(2)));

        tvBad.setText((int) (mTotal > 0 ? ((mValues.get(3) * 100) / mTotal) : 0f) + "%");
        tvBad.setTextColor(getDarkerColor(mInnerColors.get(3)));

        builder.setView(view);

        builder.setPositiveButton(R.string.glucose_ring_chart_button_ok, null);

        builder.show();
    }

    private int getDarkerColor(int color){
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    private String getGlucoseLevelString(UnitChanger c, Float level){
        return MyRound.round(c.toUIFromInternalGlucose(level), c.getDecimalsForGlucose()).toString() + c.getStringUnitForGlucose();
    }

    /////////////////////////////////////////////////////////////////////
    private int getColor(int colorId){
        return getResources().getColor(colorId);
    }

    private float dp(float p){
        // dp to pixels
        return p * mDensity;
    }

}
