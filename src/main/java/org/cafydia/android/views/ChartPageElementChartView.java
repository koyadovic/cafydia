package org.cafydia.android.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.cafydia.android.R;
import org.cafydia.android.chartobjects.ChartPageElement;
import org.cafydia.android.chartobjects.DataCollectionCriteria;
import org.cafydia.android.chartobjects.DataCollectionLabelRule;
import org.cafydia.android.chartobjects.GlucoseTestsCrossedMeals;
import org.cafydia.android.chartobjects.Label;
import org.cafydia.android.chartobjects.StatisticalObject;
import org.cafydia.android.core.Annotation;
import org.cafydia.android.core.GlucoseTest;
import org.cafydia.android.core.Instant;
import org.cafydia.android.core.Meal;
import org.cafydia.android.datadatabase.DataDatabase;
import org.cafydia.android.util.C;
import org.cafydia.android.util.MyRound;
import org.cafydia.android.util.PolynomialFitter;
import org.cafydia.android.util.UnitChanger;
import org.cafydia.android.util.ViewUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by user on 8/12/14.
 */
public class ChartPageElementChartView extends View implements View.OnClickListener {
    private float DP_PADDING_TOP = 95;
    private float DP_PADDING_BOTTOM = 63;
    private float DP_PADDING_RIGHT = 10;
    private float DP_PADDING_LEFT = 10;

    private ArrayList<Annotation> mAnnotations = null;
    private ArrayList<Label> mLabels = null;

    private Path mPathChart, mPathTextAxis;
    private Paint mPaintChart, mPaintTextAxis;

    private Paint mPaintBlueZone, mPaintRedZone;

    private Paint mPaintTests;
    private Path mPathTests;

    private Paint mPaintSeparators;
    private Path mPathSeparators;

    private DataCollectionCriteria mGlobalCriteria;
    private DataCollectionCriteria mSpecificCriteria;
    private Paint mPaintTextCriteria;

    private float density = 1.0f;

    private float xLow = 0, xHigh = 0;
    private float yLow = 0, yHigh = 0;

    private boolean mLoading = true;

    private Instant i;
    private String[] dayWeeks;
    private String[] times;

    private ChartPageElement mChartPageElement;
    private ArrayList<GlucoseTest> glucoseTests;
    private GlucoseTestsCrossedMeals mTestsCrossedMeals;
    private Annotation annotationSinceGlobal, annotationUntilGlobal;
    private Annotation annotationSinceSpecific, annotationUntilSpecific;

    // for the statistical object
    private Object maximumValue = null, minimumValue = null;
    private Float median = 0f, mean = 0f;
    private Paint mPaintMaxAndMin;

    private Paint mPaintMeanMedianMode;
    private Path mPathMeanMedianMode;

    // linear regression
    private PolynomialFitter.Polynomial linearRegression;
    private Paint mPaintLinearRegression;
    private Path mPathLinearRegression;

    private Paint mPaintTextStatisticalObject;
    private Paint mPaintTextBackgroundStatisticalObject;
    private Path mPathTextBackgroundStatisticalObject;

    private Paint mPaintStatisticalLineColor;
    private Path mPathStatisticalLineColor;


    private Path mPathAnnotations;
    private Path mPathAnnotationsText;
    private Paint mPaintAnnotations;
    private Paint mPaintAnnotationsText;
    private ArrayList<Float> mAnnotationsXs = new ArrayList<>();

    private String maximumString = "";
    private String minimumString = "";
    private String meanString = "";
    private String medianString = "";
    private String linearRegressionString = "";

    // mPolynomialGrade2 regression
    private PolynomialFitter.Polynomial mPolynomialGrade2;
    private PolynomialFitter.Polynomial mPolynomialGrade3;
    private Paint mPaintPolynomial;
    private Path mPathPolynomial;

    private Paint mPaintLabels;

    private CornerPathEffect mCornerPathEffect = new CornerPathEffect(dp(4));

    private UnitChanger mChanger;

    private int mBreakfastInterval, mLunchInterval, mDinnerInterval;
    private int mBreakfastIntervalSituation, mLunchIntervalSituation, mDinnerIntervalSituation;

    private Paint mSummaryPaint;
    private Path mSummaryPath;
    private boolean mSummaryMode = false;

    private int mSummaryTotal = 0;
    private int mSummaryLow = 0;
    private int mSummaryGood = 0;
    private int mSummaryRegular = 0;
    private int mSummaryBad = 0;
    private RectF mRectF;

    public ChartPageElementChartView(Context c, AttributeSet attrs){
        super(c, attrs);
        setWillNotDraw(false);
        init(c);

        setClickable(true);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        mSummaryMode = !mSummaryMode;
        // todo aquí tocará hacer la animación
        invalidate();
    }

    private int getColor(int colorId){
        return getContext().getResources().getColor(colorId);
    }

    private void init(Context c){
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        i = new Instant();

        dayWeeks = c.getResources().getStringArray(R.array.day_weeks_short);
        times = c.getResources().getStringArray(R.array.meal_times_short);

        // get pixel density
        density = c.getResources().getDisplayMetrics().density;

        Resources r = c.getResources();

        // paint for the chart
        mPaintChart = new Paint();
        mPaintChart.setAntiAlias(true);
        mPaintChart.setColor(getColor(R.color.chart_view_chart)); // default alpha FF 0xFF000000
        mPaintChart.setStrokeWidth(dp(1));
        mPaintChart.setStyle(Paint.Style.STROKE);
        mPaintChart.setDither(true);
        mPathChart = new Path();

        mPaintMaxAndMin = new Paint();
        mPaintMaxAndMin.setAntiAlias(true);
        mPaintMaxAndMin.setStrokeWidth(dp(2));
        mPaintMaxAndMin.setStyle(Paint.Style.STROKE);
        mPaintMaxAndMin.setDither(true);

        mPaintMeanMedianMode = new Paint();
        mPaintMeanMedianMode.setAntiAlias(true);
        mPaintMeanMedianMode.setStrokeWidth(dp(2));
        mPaintMeanMedianMode.setStyle(Paint.Style.STROKE);
        mPaintMeanMedianMode.setDither(true);
        mPathMeanMedianMode = new Path();

        // for normal text
        mPaintTextAxis = new Paint();
        mPaintTextAxis.setAntiAlias(true);
        mPaintTextAxis.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextAxis.setColor(getColor(R.color.chart_view_text));
        mPaintTextAxis.setTextSize(dp(13));
        mPathTextAxis = new Path();

        mPaintTextStatisticalObject = new Paint();
        mPaintTextStatisticalObject.setAntiAlias(true);
        mPaintTextStatisticalObject.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextStatisticalObject.setColor(getColor(R.color.chart_view_text_statistical_object));
        mPaintTextStatisticalObject.setTextSize(dp(13));

        mPaintTextBackgroundStatisticalObject = new Paint();
        mPaintTextBackgroundStatisticalObject.setAntiAlias(true);
        mPaintTextBackgroundStatisticalObject.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextBackgroundStatisticalObject.setColor(getColor(R.color.chart_view_background_statistical_object));
        mPaintTextBackgroundStatisticalObject.setStrokeWidth(dp(6));
        mPaintTextBackgroundStatisticalObject.setPathEffect(new CornerPathEffect(dp(4)));
        mPathTextBackgroundStatisticalObject = new Path();


        mPaintStatisticalLineColor = new Paint();
        mPaintStatisticalLineColor.setAntiAlias(true);
        mPaintStatisticalLineColor.setStyle(Paint.Style.FILL);
        mPaintStatisticalLineColor.setStrokeWidth(dp(2));
        mPaintStatisticalLineColor.setPathEffect(new CornerPathEffect(dp(2)));
        mPathStatisticalLineColor = new Path();



        mPaintRedZone = new Paint();
        mPaintRedZone.setAntiAlias(true);
        mPaintRedZone.setColor(getColor(R.color.chart_view_red_zone));
        mPaintRedZone.setStyle(Paint.Style.FILL);
        mPaintRedZone.setDither(true);

        mPaintBlueZone = new Paint();
        mPaintBlueZone.setAntiAlias(true);
        mPaintBlueZone.setColor(getColor(R.color.chart_view_blue_zone));
        mPaintBlueZone.setStyle(Paint.Style.FILL);
        mPaintBlueZone.setDither(true);

        mPaintTests = new Paint();
        mPaintTests.setAntiAlias(true);
        mPaintTests.setColor(getColor(R.color.chart_view_glucose_tests));
        mPaintTests.setStrokeWidth(dp(1));
        mPaintTests.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTests.setDither(true);
        mPathTests = new Path();

        mPaintSeparators = new Paint();
        mPaintSeparators.setAntiAlias(true);
        mPaintSeparators.setColor(getColor(R.color.chart_view_separators));
        mPaintSeparators.setStrokeWidth(dp(1));
        mPaintSeparators.setStyle(Paint.Style.STROKE);
        mPaintSeparators.setDither(true);
        mPathSeparators = new Path();

        // for normal text
        mPaintTextCriteria = new Paint();
        mPaintTextCriteria.setAntiAlias(true);
        mPaintTextCriteria.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextCriteria.setTextSize(dp(11));

        // linear regression
        mPaintLinearRegression = new Paint();
        mPathLinearRegression = new Path();
        mPaintLinearRegression.setAntiAlias(true);
        mPaintLinearRegression.setColor(getColor(R.color.chart_view_linear_regression));
        mPaintLinearRegression.setStrokeWidth(dp(2));
        mPaintLinearRegression.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintLinearRegression.setDither(true);

        // polynomial regression
        mPathPolynomial = new Path();
        mPaintPolynomial = new Paint();
        mPaintPolynomial.setAntiAlias(true);
        mPaintPolynomial.setColor(getColor(R.color.chart_view_polynomial_regression_2));
        mPaintPolynomial.setStrokeWidth(dp(2));
        mPaintPolynomial.setStyle(Paint.Style.STROKE);
        mPaintPolynomial.setDither(true);
        mPaintPolynomial.setPathEffect(new CornerPathEffect(dp(4)));

        mPaintAnnotations = new Paint();
        mPaintAnnotations.setAntiAlias(true);
        mPaintAnnotations.setColor(getColor(R.color.colorAnnotations));
        mPaintAnnotations.setStrokeWidth(dp(1));
        mPaintAnnotations.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintAnnotations.setDither(true);
        mPathAnnotations = new Path();

        mPaintAnnotationsText = new Paint();
        mPaintAnnotationsText.setAntiAlias(true);
        mPaintAnnotationsText.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintAnnotationsText.setColor(getColor(R.color.colorAnnotationsText));
        mPaintAnnotationsText.setTextSize(dp(16));
        mPathAnnotationsText = new Path();

        mPaintLabels = new Paint();
        mPaintLabels.setAntiAlias(true);
        mPaintLabels.setStyle(Paint.Style.FILL);
        mPaintLabels.setDither(true);

        mSummaryPaint = new Paint();
        mSummaryPaint.setAntiAlias(true);
        mSummaryPaint.setColor(getColor(R.color.colorCafydiaDefault));
        mSummaryPaint.setStrokeWidth(dp(1));
        mSummaryPaint.setStyle(Paint.Style.STROKE);
        mSummaryPaint.setDither(true);
        mSummaryPath = new Path();

        mChanger = new UnitChanger(c);

        mRectF = new RectF();


    }

    private void drawChart(Canvas canvas) {
        if(mPathChart.isEmpty()) {
            //mPathChart.moveTo(0 + dp(DP_PADDING_LEFT), 0 + dp(DP_PADDING_TOP));
            mPathChart.moveTo(0 + dp(DP_PADDING_LEFT), getHeight() - dp(DP_PADDING_BOTTOM));
            mPathChart.lineTo(getWidth() - dp(DP_PADDING_RIGHT), getHeight() - dp(DP_PADDING_BOTTOM));
        }
        canvas.drawPath(mPathChart, mPaintChart);

    }

    private void drawAnnotations(Canvas canvas){


        if(mAnnotations != null && mChartPageElement.getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES)){
            float measure;
            mAnnotationsXs.clear();

            mPathAnnotations.reset();

            for (Annotation a : mAnnotations){
                if(a.getDaysPassedFromNow() < xLow || a.getDaysPassedFromNow() > xHigh) continue;
                float x = getTranslatedXPos(a.getDaysPassedFromNow());

                measure = mPaintAnnotationsText.measureText(a.getNumber().toString());

                float y = getTranslatedYPos(yHigh) + dp(20);

                for(Float annotationX : mAnnotationsXs) {
                    if(x > annotationX) {
                        y += dp(24);
                    } else {
                        break;
                    }
                }

                if(y + dp(24) > getTranslatedYPos(yLow))
                    y = getTranslatedYPos(yHigh);

                mAnnotationsXs.add(x - measure - dp(11));

                mPathAnnotations.moveTo(x, getTranslatedYPos(yLow));
                mPathAnnotations.lineTo(x, y - dp(1));

                canvas.drawRect(x - measure - dp(9), y, x - dp(1), y + dp(20), mPaintAnnotations);

                if(a.getNumber() > 0)
                    canvas.drawText(a.getNumber().toString(), x - measure - dp(4), y + dp(16), mPaintAnnotationsText);
                else
                    canvas.drawText("?", x - measure - dp(4), y + dp(16), mPaintAnnotationsText);
            }

            canvas.drawPath(mPathAnnotations, mPaintAnnotations);
        }
    }

    private void drawCriteriaParameters(Canvas canvas){

        float y = dp(17);
        float x = dp(10);

        Resources r = getContext().getResources();


        String s;
        float measure;

        // SINCE
        s = r.getString(R.string.chart_view_criteria_since) + ": ";

        if(mGlobalCriteria != null && mGlobalCriteria.getSince().getData() != 0) {
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_global));

            if(mGlobalCriteria.getSince().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE)) {
                s += -mGlobalCriteria.getSince().getData() + " " + r.getString(R.string.data_collection_criteria_instant_days_ago);
            }
            else if (annotationSinceGlobal != null && mGlobalCriteria.getSince().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION)) {
                s += annotationSinceGlobal.getAnnotation().substring(0, annotationSinceGlobal.getAnnotation().length() > 12 ? 12 : annotationSinceGlobal.getAnnotation().length()) + (annotationSinceGlobal.getAnnotation().length() > 12 ? "..." : "");
            }
            else {
                s += mGlobalCriteria.getSinceInstant().getUserDateStringShort();
            }


        }
        else if(mSpecificCriteria != null && mSpecificCriteria.getSince().getData() != 0) {
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_specific));

            if(mSpecificCriteria.getSince().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE)) {
                s += -mSpecificCriteria.getSince().getData() + " " + r.getString(R.string.data_collection_criteria_instant_days_ago);
            }
            else if(annotationSinceSpecific != null && mSpecificCriteria.getSince().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION)) {
                s += annotationSinceSpecific.getAnnotation().substring(0, annotationSinceSpecific.getAnnotation().length() > 12 ? 12 : annotationSinceSpecific.getAnnotation().length()) + (annotationSinceSpecific.getAnnotation().length() > 12 ? "..." : "");
            }
            else {
                s += mSpecificCriteria.getSinceInstant().getUserDateStringShort();
            }

        } else {
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_global));
            s += r.getString(R.string.data_collection_criteria_instant_the_origin_the_ages);
        }

        canvas.drawText(s, x, y, mPaintTextCriteria);


        // UNTIL
        s = r.getString(R.string.chart_view_criteria_until) + ": ";
        //String s2 = "";
        if(mGlobalCriteria != null && mGlobalCriteria.getUntil().getData() != 0) {
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_global));

            if(mGlobalCriteria.getUntil().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE)) {
                s += -mGlobalCriteria.getUntil().getData() + " " + r.getString(R.string.data_collection_criteria_instant_days_ago);
            }
            else if (mGlobalCriteria.getUntil().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION)) {
                s += annotationUntilGlobal.getAnnotation().substring(0, annotationUntilGlobal.getAnnotation().length() > 12 ? 12 : annotationUntilGlobal.getAnnotation().length()) + (annotationUntilGlobal.getAnnotation().length() > 12 ? "..." : "");
            }
            else {
                s += mGlobalCriteria.getUntilInstant().getUserDateStringShort();
            }


        }
        else if(mSpecificCriteria != null && mSpecificCriteria.getUntil().getData() != 0) {
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_specific));

            if(mSpecificCriteria.getUntil().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE)) {
                s += -mSpecificCriteria.getUntil().getData() + " " + r.getString(R.string.data_collection_criteria_instant_days_ago);

            } else if(annotationUntilSpecific != null && mSpecificCriteria.getUntil().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION)) {
                s += annotationUntilSpecific.getAnnotation().substring(0, annotationUntilSpecific.getAnnotation().length() > 12 ? 12 : annotationUntilSpecific.getAnnotation().length()) + (annotationUntilSpecific.getAnnotation().length() > 12 ? "..." : "");
            }
            else {
                s += mSpecificCriteria.getUntilInstant().getUserDateStringShort();
            }

        } else {
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_global));
            s += r.getString(R.string.data_collection_criteria_instant_today);
            //s2 += r.getString(R.string.data_collection_criteria_instant_today);
        }

        measure = mPaintTextCriteria.measureText(s);

        canvas.drawText(s, getTranslatedXPos(xHigh) - measure, y, mPaintTextCriteria);

        y += dp(17);
        x = dp(10);

        //
        // DAY WEEKS
        //
        s = r.getString(R.string.data_collection_criteria_dayweeks) + ":";

        if(mGlobalCriteria != null && mGlobalCriteria.getDayWeeksActivated().equals(C.DATA_COLLECTION_CRITERIA_ACTIVATED)){

            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_global));
            canvas.drawText(s, x, y + dp(4), mPaintTextCriteria);

            x = getTranslatedXPos(xHigh) - dp(16);
            for(int a = 6; a > -1; a--) {
                s = dayWeeks[a];
                measure = mPaintTextCriteria.measureText(s);

                if(mGlobalCriteria.collectOnDayWeek(a)){

                    mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_global));
                    canvas.drawCircle(x + dp(8), y, dp(9), mPaintTextCriteria);

                    mPaintTextCriteria.setColor(0xFFFFFFFF);
                    canvas.drawText(s, x - (measure / 2f) + dp(8), y + dp(3), mPaintTextCriteria);

                } else {
                    mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_disabled));
                    canvas.drawCircle(x + dp(8), y, dp(9), mPaintTextCriteria);

                    mPaintTextCriteria.setColor(0xFFFFFFFF);
                    canvas.drawText(s, x - (measure / 2f) + dp(8), y + dp(3), mPaintTextCriteria);

                }
                x -= dp(21);
            }
        }
        else if(mSpecificCriteria != null && mSpecificCriteria.getDayWeeksActivated().equals(C.DATA_COLLECTION_CRITERIA_ACTIVATED)) {
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_specific));
            canvas.drawText(s, x, y + dp(4), mPaintTextCriteria);

            x = getTranslatedXPos(xHigh) - dp(16);
            for(int a = 6; a > -1; a--) {
                s = dayWeeks[a];
                measure = mPaintTextCriteria.measureText(s);

                if(mSpecificCriteria.collectOnDayWeek(a)){

                    mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_specific));
                    canvas.drawCircle(x + dp(8), y, dp(9), mPaintTextCriteria);

                    mPaintTextCriteria.setColor(0xFFFFFFFF);
                    canvas.drawText(s, x - (measure / 2f) + dp(8), y + dp(3), mPaintTextCriteria);

                } else {
                    mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_disabled));
                    canvas.drawCircle(x + dp(8), y, dp(9), mPaintTextCriteria);

                    mPaintTextCriteria.setColor(0xFFFFFFFF);
                    canvas.drawText(s, x - (measure / 2f) + dp(8), y + dp(3), mPaintTextCriteria);

                }
                x -= dp(21);
            }
        } else {
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_global));
            canvas.drawText(s, x, y + dp(4), mPaintTextCriteria);

            x = getTranslatedXPos(xHigh) - dp(16);
            for(int a = 6; a > -1; a--) {
                s = dayWeeks[a];
                measure = mPaintTextCriteria.measureText(s);

                mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_disabled));
                canvas.drawCircle(x + dp(8), y, dp(9), mPaintTextCriteria);

                mPaintTextCriteria.setColor(0xFFFFFFFF);
                canvas.drawText(s, x - (measure / 2f) + dp(8), y + dp(3), mPaintTextCriteria);

                x -= dp(21);
            }

        }
        y += dp(15);
        x = dp(10);


        //
        // TIMES
        //
        mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_global));
        s = r.getString(R.string.data_collection_criteria_times) + ":";

        if(mGlobalCriteria != null && mGlobalCriteria.getMealTimesActivated().equals(C.DATA_COLLECTION_CRITERIA_ACTIVATED)){
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_global));
            canvas.drawText(s, x, y + dp(10), mPaintTextCriteria);
            x = getTranslatedXPos(xHigh) + dp(3);
            y -= dp(1);

            for(int a = 6; a > -1; a--) {
                s = times[a];
                measure = mPaintTextCriteria.measureText(s);

                x -= measure + dp(8);

                if(mGlobalCriteria.collectOnMealTime(a)){
                    mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_global));


                    mPaintTextCriteria.setPathEffect(mCornerPathEffect);
                    canvas.drawRect(x, y, x + measure + dp(6), y + dp(14), mPaintTextCriteria);
                    mPaintTextCriteria.setPathEffect(null);

                    mPaintTextCriteria.setColor(0xFFFFFFFF);
                    canvas.drawText(s, x + dp(3), y + dp(10), mPaintTextCriteria);



                } else {
                    mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_disabled));
                    mPaintTextCriteria.setPathEffect(mCornerPathEffect);
                    canvas.drawRect(x, y, x + measure + dp(6), y + dp(14), mPaintTextCriteria);
                    mPaintTextCriteria.setPathEffect(null);

                    mPaintTextCriteria.setColor(0xFFFFFFFF);
                    canvas.drawText(s, x + dp(3), y + dp(10), mPaintTextCriteria);


                }
            }
        }
        else if(mSpecificCriteria != null && mSpecificCriteria.getMealTimesActivated().equals(C.DATA_COLLECTION_CRITERIA_ACTIVATED)) {
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_specific));
            canvas.drawText(s, x, y + dp(10), mPaintTextCriteria);
            x = getTranslatedXPos(xHigh) + dp(3);
            y -= dp(1);

            for(int a = 6; a > -1; a--) {
                s = times[a];
                measure = mPaintTextCriteria.measureText(s);


                x -= measure + dp(8);

                if(mSpecificCriteria.collectOnMealTime(a)){
                    mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_specific));

                    mPaintTextCriteria.setPathEffect(mCornerPathEffect);
                    canvas.drawRect(x, y, x + measure + dp(6), y + dp(14), mPaintTextCriteria);
                    mPaintTextCriteria.setPathEffect(null);

                    mPaintTextCriteria.setColor(0xFFFFFFFF);
                    canvas.drawText(s, x + dp(3), y + dp(10), mPaintTextCriteria);




                } else {
                    mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_disabled));

                    mPaintTextCriteria.setPathEffect(mCornerPathEffect);
                    canvas.drawRect(x, y, x + measure + dp(6), y + dp(14), mPaintTextCriteria);
                    mPaintTextCriteria.setPathEffect(null);

                    mPaintTextCriteria.setColor(0xFFFFFFFF);
                    canvas.drawText(s, x + dp(3), y + dp(10), mPaintTextCriteria);


                }
            }
        } else {
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_global));
            canvas.drawText(s, x, y + dp(10), mPaintTextCriteria);
            x = getTranslatedXPos(xHigh) + dp(3);
            y -= dp(1);

            for(int a = 6; a > -1; a--) {
                s = times[a];
                measure = mPaintTextCriteria.measureText(s);

                x -= measure + dp(8);

                mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_disabled));
                mPaintTextCriteria.setPathEffect(mCornerPathEffect);
                canvas.drawRect(x, y, x + measure + dp(6), y + dp(14), mPaintTextCriteria);
                mPaintTextCriteria.setPathEffect(null);

                mPaintTextCriteria.setColor(0xFFFFFFFF);
                canvas.drawText(s, x + dp(3), y + dp(10), mPaintTextCriteria);


            }

        }

        //
        // Labels
        //

        y += dp(28);
        x = dp(10);

        if(mGlobalCriteria != null && mGlobalCriteria.getLabelRules().size() > 0) {
            if(mGlobalCriteria.getLabelRules().get(0).getAction().equals(C.LABEL_RULE_ACTION_INCLUDE)) {
                s = getResources().getString(R.string.data_collection_criteria_include_labels);
            } else {
                s = getResources().getString(R.string.data_collection_criteria_exclude_labels);
            }
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_global));

            canvas.drawText(s, x, y + dp(4), mPaintTextCriteria);
            x = getTranslatedXPos(xHigh) - dp(7);

            for(DataCollectionLabelRule rule : mGlobalCriteria.getLabelRules()) {
                mPaintLabels.setColor(rule.getLabel().getColor());
                canvas.drawCircle(x, y, dp(8), mPaintLabels);
                x -= dp(19);

                if(x - dp(6) < getTranslatedXPos(xLow))
                    break;
            }
        }
        else if(mSpecificCriteria != null && mSpecificCriteria.getLabelRules().size() > 0) {
            if(mSpecificCriteria.getLabelRules().get(0).getAction().equals(C.LABEL_RULE_ACTION_INCLUDE)) {
                s = getResources().getString(R.string.data_collection_criteria_include_labels);
            } else {
                s = getResources().getString(R.string.data_collection_criteria_exclude_labels);
            }
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_specific));
            measure = mPaintTextCriteria.measureText(s);

            canvas.drawText(s, x, y + dp(4), mPaintTextCriteria);
            x = getTranslatedXPos(xHigh) - dp(7);

            for(DataCollectionLabelRule rule : mSpecificCriteria.getLabelRules()) {
                mPaintLabels.setColor(rule.getLabel().getColor());
                canvas.drawCircle(x, y, dp(8), mPaintLabels);

                x -= dp(19);

                if(x - dp(6) < getTranslatedXPos(xLow))
                    break;
            }
        } else {
            s = getResources().getString(R.string.data_collection_criteria_no_labels_specified);
            mPaintTextCriteria.setColor(getColor(R.color.chart_view_criteria_global));
            canvas.drawText(s, x, y + dp(3), mPaintTextCriteria);
        }


    }

    private void drawTextAxis(Canvas canvas) {
        mPathSeparators.reset();
        mPathTextAxis.reset();

        if(mChartPageElement != null) {

            mPathSeparators.reset();
            float measure;
            switch (mChartPageElement.getType()) {
                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS:
                    if(glucoseTests != null) {
                        i.setTimeToTheMorning();
                        int minutes = i.getMinutesPassedSince5AM();
                        while(minutes <= xHigh) {
                            if(i.getMinutesPassedSince5AM() <= xLow) {
                                minutes += 120;
                                i.increaseNMinutes(120);
                            }
                            else {

                                // text
                                measure = mPaintTextAxis.measureText(i.getUserTimeStringShort());

                                mPathTextAxis.moveTo(getTranslatedXPos(i.getMinutesPassedSince5AM()) - dp(5), getTranslatedYPos(yLow) + dp(10));
                                mPathTextAxis.lineTo(getTranslatedXPos(i.getMinutesPassedSince5AM()) + measure - dp(5), getTranslatedYPos(yLow) + dp(34));

                                canvas.drawTextOnPath(i.getUserTimeStringShort(), mPathTextAxis, 0, 0, mPaintTextAxis);

                                mPathTextAxis.reset();

                                // separators
                                //if(mChartPageElement.getStatisticalObject() != null && mChartPageElement.getStatisticalObject().isGridActivated()) {
                                //    mPathSeparators.moveTo(getTranslatedXPos(i.getMinutesPassedSince5AM()), getTranslatedYPos(yLow));
                                //    mPathSeparators.lineTo(getTranslatedXPos(i.getMinutesPassedSince5AM()), getTranslatedYPos(yHigh));
                                //}

                                minutes += 120;
                                i.increaseNMinutes(120);
                            }

                        }

                    }
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS:
                    i.setTimeToTheMorningOnMonday();

                    for(int a = 0; a < 7; a++){
                        if(i.getMinutesPassedSince5AMMonday() <= xLow - 200) {
                            i.increaseOneDay();

                        } else if(i.getMinutesPassedSince5AMMonday() >= xHigh + 200) {
                            break;

                        } else {
                            // test
                            measure = mPaintTextAxis.measureText(dayWeeks[a]);
                            canvas.drawText(dayWeeks[a], getTranslatedXPos(i.getMinutesPassedSince5AMMonday()) - (measure / 2f), getTranslatedYPos(yLow) + dp(16), mPaintTextAxis);
                            i.increaseOneDay();

                            // separators
                            //if(mChartPageElement.getStatisticalObject() != null && mChartPageElement.getStatisticalObject().isGridActivated()) {
                            //    mPathSeparators.moveTo(getTranslatedXPos(i.getMinutesPassedSince5AMMonday()), getTranslatedYPos(yLow));
                            //    mPathSeparators.lineTo(getTranslatedXPos(i.getMinutesPassedSince5AMMonday()), getTranslatedYPos(yHigh));
                            //}

                        }
                    }
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES:
                    if(glucoseTests != null && glucoseTests.size() > 1) {
                        float unit = (xHigh - xLow) / 5.0f;

                        float d;

                        long nowMillis = System.currentTimeMillis();

                        for (float a = xLow + 1; a < xHigh; a += unit) {
                            mPathTextAxis.reset();
                            i.setTimeInMilis(nowMillis + (long) (a * 24 * 60 * 60 * 1000));

                            measure = mPaintTextAxis.measureText(i.getUserDateString());

                            if (getTranslatedXPos(a) + (measure - dp(30)) > getTranslatedXPos(xHigh))
                                continue;

                            d = i.getDaysPassedFromNow();

                            mPathTextAxis.moveTo(getTranslatedXPos(d) - dp(4), getTranslatedYPos(yLow) + dp(10));
                            mPathTextAxis.lineTo(getTranslatedXPos(d) - dp(4) + measure, getTranslatedYPos(yLow) + dp(60));

                            // separators
                            //if(mChartPageElement.getStatisticalObject() != null && mChartPageElement.getStatisticalObject().isGridActivated()) {
                            //    mPathSeparators.moveTo(getTranslatedXPos(d), getTranslatedYPos(yLow) - dp(1));
                            //    mPathSeparators.lineTo(getTranslatedXPos(d), getTranslatedYPos(yHigh));
                            //}

                            canvas.drawTextOnPath(i.getUserDateStringShort(), mPathTextAxis, 0, 0, mPaintTextAxis);

                        }
                    }
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE:
                    float unit = (xHigh - xLow) / 5.0f;
                    float displacement = (xHigh - xLow) / 10.0f;

                    for(float a = xLow + displacement; a < xHigh; a+= unit){
                        String s = MyRound.round(a, 0).intValue() + "gr";
                        measure = mPaintTextAxis.measureText(s);

                        canvas.drawText(s, getTranslatedXPos(a) - (measure / 2.0f), getTranslatedYPos(yLow) + dp(16), mPaintTextAxis);

                        //if(mChartPageElement.getStatisticalObject() != null && mChartPageElement.getStatisticalObject().isGridActivated()) {
                        //    mPathSeparators.moveTo(getTranslatedXPos(a), getTranslatedYPos(yLow));
                        //    mPathSeparators.lineTo(getTranslatedXPos(a), getTranslatedYPos(yHigh));
                        //}
                    }
                    break;
            }

            // y axis
            int level = -20;
            int increment = (((int) ((yHigh - yLow) / 5)) / 10) * 10;

            //measure = 0f;
            while (increment != 0 && level < yHigh - (increment / 5f)) {
                if(level <= yLow + 5) {
                    level += increment;
                } else {

                    // separators
                    if(mChartPageElement.getStatisticalObject() != null && mChartPageElement.getStatisticalObject().isGridActivated()) {
                        String s = mChanger.toUIFromInternalGlucoseString((float) level) + mChanger.getStringUnitForGlucose();
                        canvas.drawText(s, getTranslatedXPos(xLow) + dp(2), getTranslatedYPos(level) - dp(4), mPaintTextAxis);

                        mPathSeparators.moveTo(getTranslatedXPos(xLow), getTranslatedYPos(level));
                        mPathSeparators.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(level));
                    }

                    level += increment;

                }
            }

            // separators
            if(mChartPageElement.getStatisticalObject() != null && mChartPageElement.getStatisticalObject().isGridActivated()) {
                canvas.drawPath(mPathSeparators, mPaintSeparators);
            }
        }

    }

    private void drawGlucoseTest(Canvas canvas, float x, float y){
        canvas.drawCircle(x, y, dp(1), mPaintTests);
        mPathTests.moveTo(x - dp(1), y);
        mPathTests.lineTo(x, y - dp(2));
        mPathTests.lineTo(x + dp(1), y);
        mPathTests.lineTo(x - dp(1), y);
    }

    private void drawGlucoseTests(Canvas canvas) {
        mPathTests.reset();

        switch (mChartPageElement.getType()) {
            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS:
            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS:
            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES:
                if(glucoseTests != null) {

                    for(GlucoseTest test : glucoseTests) {
                        float x = 0;
                        switch (mChartPageElement.getType()){
                            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS:
                                x = test.getMinutesPassedSince5AM();
                                break;

                            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS:
                                x = test.getMinutesPassedSince5AMMonday();
                                break;

                            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES:
                                x = test.getDaysPassedFromNow();
                                break;
                        }

                        drawGlucoseTest(canvas, getTranslatedXPos(x), getTranslatedYPos(test.getGlucoseLevel().floatValue()));
                    }
                    canvas.drawPath(mPathTests, mPaintTests);
                }
                break;

            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE:
                if(mTestsCrossedMeals != null) {
                    for(int a = 0; a < mTestsCrossedMeals.getCount(); a++) {
                        float x = mTestsCrossedMeals.getItem(a).getMeal().getMealCarbohydrates();
                        if(mTestsCrossedMeals.getItem(a).getGlucoseTestAfterMeal() != null){
                            GlucoseTest test = mTestsCrossedMeals.getItem(a).getGlucoseTestAfterMeal();
                            drawGlucoseTest(canvas, getTranslatedXPos(x), getTranslatedYPos(test.getGlucoseLevel().floatValue()));
                        }
                        if(mTestsCrossedMeals.getItem(a).getGlucoseTestBeforeMeal() != null){
                            GlucoseTest test = mTestsCrossedMeals.getItem(a).getGlucoseTestBeforeMeal();
                            drawGlucoseTest(canvas, getTranslatedXPos(x), getTranslatedYPos(test.getGlucoseLevel().floatValue()));
                        }
                    }
                    canvas.drawPath(mPathTests, mPaintTests);

                }
                break;

            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_MEAL_HOUR:
                if(mTestsCrossedMeals != null) {
                    for (int a = 0; a < mTestsCrossedMeals.getCount(); a++) {
                        GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal oneMeal = mTestsCrossedMeals.getItem(a);

                        int minutesPassed = 0;
                        Meal meal = oneMeal.getMeal();

                        switch (meal.getMealTime()){
                            case C.MEAL_BREAKFAST:
                                minutesPassed += mBreakfastIntervalSituation;
                                break;

                            case C.MEAL_LUNCH:
                                minutesPassed += mBreakfastInterval;
                                minutesPassed += mLunchIntervalSituation;
                                break;

                            case C.MEAL_DINNER:
                                minutesPassed += mBreakfastInterval + mLunchInterval;
                                minutesPassed += mDinnerIntervalSituation;
                                break;
                        }

                        GlucoseTest be = oneMeal.getGlucoseTestBeforeMeal();
                        GlucoseTest af = oneMeal.getGlucoseTestAfterMeal();

                        if(be != null) {
                            int gSituation = be.getMinutesPassedSinceHourAndMinuteInstant(meal);
                            drawGlucoseTest(canvas, getTranslatedXPos(minutesPassed + gSituation), getTranslatedYPos(be.getGlucoseLevel().floatValue()));

                        }
                        if(af != null) {
                            int gSituation = af.getMinutesPassedSinceHourAndMinuteInstant(meal);
                            drawGlucoseTest(canvas, getTranslatedXPos(minutesPassed + gSituation), getTranslatedYPos(af.getGlucoseLevel().floatValue()));
                        }
                    }
                    canvas.drawPath(mPathTests, mPaintTests);
                }
                break;
        }
    }

    private void drawZones(Canvas canvas) {
        if(yLow < 60) {
            canvas.drawRect(getTranslatedXPos(xLow), getTranslatedYPos(60), getTranslatedXPos(xHigh), getTranslatedYPos(yLow), mPaintRedZone);
        }
        if(yHigh > 150) {
            canvas.drawRect(getTranslatedXPos(xLow), getTranslatedYPos(yHigh), getTranslatedXPos(xHigh), getTranslatedYPos(150), mPaintRedZone);
        }
        canvas.drawRect(
                getTranslatedXPos(xLow),
                getTranslatedYPos(yHigh <= 150 ? yHigh  : 150) - dp(1),
                getTranslatedXPos(xHigh),
                getTranslatedYPos(yLow >= 60 ? yLow : 60) + dp(1),
                mPaintBlueZone
        );
    }


    private void drawRectanglesAndText(Canvas canvas) {
        StatisticalObject o = mChartPageElement.getStatisticalObject();

        if(o != null) {
            if (o.isMaximumActivated()) {
                drawText(canvas, maximumString, getTranslatedXPos(xHigh), getTranslatedYPos(yHigh), getColor(R.color.chart_view_max));
            }

            if (o.isMinimumActivated()) {
                drawText(canvas, minimumString, getTranslatedXPos(xHigh), getTranslatedYPos(yHigh), getColor(R.color.chart_view_min));
            }

            if(o.isMeanActivated()) {
                drawText(canvas, meanString, getTranslatedXPos(xHigh), getTranslatedYPos(yHigh), getColor(R.color.chart_view_mean));
            }

            if(o.isMedianActivated()) {
                drawText(canvas, medianString, getTranslatedXPos(xHigh), getTranslatedYPos(yHigh), getColor(R.color.chart_view_median));
            }

            if(linearRegression != null && o.isLinearRegressionActivated()) {
                drawText(canvas, linearRegressionString, getTranslatedXPos(xHigh), getTranslatedYPos(yHigh), getColor(R.color.chart_view_linear_regression));
            }

            if(mPolynomialGrade2 != null && o.isPolynomialRegressionGrade2Activated()) {
                String s = getResources().getString(R.string.activity_charts_polynomial_regression_grade_2);
                drawText(canvas, s, getTranslatedXPos(xHigh), getTranslatedYPos(yHigh), getColor(R.color.chart_view_polynomial_regression_2));
            }

            if(mPolynomialGrade3 != null && o.isPolynomialRegressionGrade3Activated()) {
                String s = getResources().getString(R.string.activity_charts_polynomial_regression_grade_3);
                drawText(canvas, s, getTranslatedXPos(xHigh), getTranslatedYPos(yHigh), getColor(R.color.chart_view_polynomial_regression_3));
            }
        }
    }

    private void drawMinimum(Canvas canvas) {
        if(minimumValue != null) {
            float x = 0;
            float y = 0;
            boolean draw = false;
            String s = getResources().getString(R.string.activity_charts_glucose_min);
            switch (mChartPageElement.getType()) {
                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS:
                    x = ((GlucoseTest)minimumValue).getMinutesPassedSince5AM();
                    y = ((GlucoseTest)minimumValue).getGlucoseLevel();
                    s += ": " + mChanger.toUIFromInternalGlucoseString((float) ((GlucoseTest) minimumValue).getGlucoseLevel()) + mChanger.getStringUnitForGlucose() + " / ";
                    s += ((GlucoseTest)minimumValue).getUserDateStringShort() + " " + ((GlucoseTest)minimumValue).getUserTimeStringShort();
                    draw = true;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS:
                    x = ((GlucoseTest)minimumValue).getMinutesPassedSince5AMMonday();
                    y = ((GlucoseTest)minimumValue).getGlucoseLevel();
                    s += ": " + mChanger.toUIFromInternalGlucose((float) ((GlucoseTest) minimumValue).getGlucoseLevel()) + mChanger.getStringUnitForGlucose() + " / ";
                    s += ((GlucoseTest)minimumValue).getUserDateStringShort() + " " + ((GlucoseTest)minimumValue).getUserTimeStringShort();
                    draw = true;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES:
                    x = ((GlucoseTest)minimumValue).getDaysPassedFromNow();
                    y = ((GlucoseTest)minimumValue).getGlucoseLevel();
                    s += ": " + mChanger.toUIFromInternalGlucoseString((float) ((GlucoseTest) minimumValue).getGlucoseLevel()) + mChanger.getStringUnitForGlucose() + " / ";
                    s += ((GlucoseTest)minimumValue).getUserDateStringShort() + " " + ((GlucoseTest)minimumValue).getUserTimeStringShort();

                    draw = true;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE:
                    x = ((GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal) minimumValue).getMeal().getMealCarbohydrates();
                    GlucoseTest g = getMinimumGlucoseTest(
                            ((GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal) minimumValue).getGlucoseTestAfterMeal(),
                            ((GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal) minimumValue).getGlucoseTestBeforeMeal()
                    );
                    y = g.getGlucoseLevel();
                    s += ": " + mChanger.toUIFromInternalGlucoseString((float) (g.getGlucoseLevel())) + mChanger.getStringUnitForGlucose() + " / ";
                    s += g.getUserDateStringShort() + " " + g.getUserTimeStringShort();

                    draw = true;
                    break;
            }

            if(draw) {
                drawGlucoseTest(canvas, getTranslatedXPos(x), getTranslatedYPos(y));
                canvas.drawPath(mPathTests, mPaintTests);

                mPaintMaxAndMin.setColor(getColor(R.color.chart_view_min));
                canvas.drawCircle(getTranslatedXPos(x), getTranslatedYPos(y), dp(8), mPaintMaxAndMin);

                // fpr minimum rectangle
                minimumString = s;
            }
        }

    }

    private void drawMaximum(Canvas canvas) {
        if(maximumValue != null) {
            float x = 0;
            float y = 0;
            boolean draw = false;
            String s = getResources().getString(R.string.activity_charts_glucose_max);
            switch (mChartPageElement.getType()) {
                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS:
                    x = ((GlucoseTest)maximumValue).getMinutesPassedSince5AM();
                    y = ((GlucoseTest)maximumValue).getGlucoseLevel();
                    s += ": " + mChanger.toUIFromInternalGlucoseString((float) ((GlucoseTest) maximumValue).getGlucoseLevel()) + mChanger.getStringUnitForGlucose() + " / ";
                    s += ((GlucoseTest)maximumValue).getUserDateStringShort() + " " + ((GlucoseTest)maximumValue).getUserTimeStringShort();
                    draw = true;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS:
                    x = ((GlucoseTest)maximumValue).getMinutesPassedSince5AMMonday();
                    y = ((GlucoseTest)maximumValue).getGlucoseLevel();
                    s += ": " + mChanger.toUIFromInternalGlucoseString((float) ((GlucoseTest) maximumValue).getGlucoseLevel()) + mChanger.getStringUnitForGlucose() + " / ";
                    s += ((GlucoseTest)maximumValue).getUserDateStringShort() + " " + ((GlucoseTest)maximumValue).getUserTimeStringShort();
                    draw = true;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES:
                    x = ((GlucoseTest)maximumValue).getDaysPassedFromNow();
                    y = ((GlucoseTest)maximumValue).getGlucoseLevel();
                    s += ": " + mChanger.toUIFromInternalGlucoseString((float) ((GlucoseTest) maximumValue).getGlucoseLevel()) + mChanger.getStringUnitForGlucose() + " / ";
                    s += ((GlucoseTest)maximumValue).getUserDateStringShort() + " " + ((GlucoseTest)maximumValue).getUserTimeStringShort();
                    draw = true;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE:
                    x = ((GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal) maximumValue).getMeal().getMealCarbohydrates();
                    GlucoseTest g = getMaximumGlucoseTest(
                            ((GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal) maximumValue).getGlucoseTestAfterMeal(),
                            ((GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal) maximumValue).getGlucoseTestBeforeMeal()
                    );
                    y = g.getGlucoseLevel();
                    s += ": " + mChanger.toUIFromInternalGlucoseString((float) g.getGlucoseLevel()) + mChanger.getStringUnitForGlucose() + " / ";
                    s += g.getUserDateStringShort() + " " + g.getUserTimeStringShort();
                    draw = true;
                    break;
            }

            if(draw) {
                drawGlucoseTest(canvas, getTranslatedXPos(x), getTranslatedYPos(y));
                canvas.drawPath(mPathTests, mPaintTests);

                mPaintMaxAndMin.setColor(getColor(R.color.chart_view_max));
                canvas.drawCircle(getTranslatedXPos(x), getTranslatedYPos(y), dp(8), mPaintMaxAndMin);

                // for the maximum rectangle
                maximumString = s;

            }
        }
    }

    private void drawMean(Canvas canvas) {
        if(mean != 0){
            mPathMeanMedianMode.reset();

            mPaintMeanMedianMode.setColor(getColor(R.color.chart_view_mean));
            mPathMeanMedianMode.moveTo(getTranslatedXPos(xLow) + dp(1), getTranslatedYPos(mean));
            mPathMeanMedianMode.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(mean));
            canvas.drawPath(mPathMeanMedianMode, mPaintMeanMedianMode);

            // mean rectangle
            meanString = getResources().getStringArray(R.array.statistical_object_elements)[4] + ": " + mChanger.toUIFromInternalGlucoseString(mean) + mChanger.getStringUnitForGlucose();
        }

    }

    private void drawMedian(Canvas canvas) {
        if(median != 0){
            mPathMeanMedianMode.reset();

            mPaintMeanMedianMode.setColor(getColor(R.color.chart_view_median));
            mPathMeanMedianMode.moveTo(getTranslatedXPos(xLow) + dp(1), getTranslatedYPos(median));
            mPathMeanMedianMode.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(median));
            canvas.drawPath(mPathMeanMedianMode, mPaintMeanMedianMode);

            // median rectangle
            medianString = getResources().getStringArray(R.array.statistical_object_elements)[5] + ": " + mChanger.toUIFromInternalGlucoseString(median) + mChanger.getStringUnitForGlucose();
        }
    }

    private void drawLinearRegression(Canvas canvas) {
        if(linearRegression != null) {
            mPathLinearRegression.reset();

            float xl, xh;
            xl = xLow;
            xh = xHigh;
            float inc = (xHigh - xLow) / 100.0f;

            while((float) linearRegression.getY(xl) > yHigh || (float) linearRegression.getY(xl) < yLow){
                xl += inc;
            }
            while((float) linearRegression.getY(xh) > yHigh || (float) linearRegression.getY(xh) < yLow){
                xh -= inc;
            }


            mPathLinearRegression.moveTo(getTranslatedXPos(xl), getTranslatedYPos((float) linearRegression.getY(xl)));
            mPathLinearRegression.lineTo(getTranslatedXPos(xh), getTranslatedYPos((float) linearRegression.getY(xh)));
            canvas.drawPath(mPathLinearRegression, mPaintLinearRegression);

            // linear regression rectangle
            linearRegressionString = getResources().getString(R.string.activity_charts_linear_regression);

        }
    }

    private void drawPolynomialRegressionGrade2(Canvas canvas) {
        if(mPolynomialGrade2 != null) {
            mPathPolynomial.reset();
            mPaintPolynomial.setColor(getColor(R.color.chart_view_polynomial_regression_2));

            float y;
            boolean out = false;

            mPathPolynomial.moveTo(getTranslatedXPos(xLow) + dp(1), getTranslatedYPos((float) mPolynomialGrade2.getY(xLow)));

            for (double a = xLow; a < xHigh; a += (xHigh - xLow) / 500.0) {
                y = (float) mPolynomialGrade2.getY(a);

                if(y < yLow || y > yHigh) {
                    out = true;
                    continue;
                }

                else if(out && (y > yLow || y < yHigh)) {
                    out = false;
                    mPathPolynomial.moveTo(getTranslatedXPos((float) a), getTranslatedYPos(y));
                }

                mPathPolynomial.lineTo(getTranslatedXPos((float) a), getTranslatedYPos(y));
            }



            canvas.drawPath(mPathPolynomial, mPaintPolynomial);

        }
    }

    private void drawPolynomialRegressionGrade3(Canvas canvas) {


        if(mPolynomialGrade3 != null) {
            mPathPolynomial.reset();
            mPaintPolynomial.setColor(getColor(R.color.chart_view_polynomial_regression_3));

            float y;
            boolean out = false;

            mPathPolynomial.moveTo(getTranslatedXPos(xLow) + dp(1), getTranslatedYPos((float) mPolynomialGrade3.getY(xLow)));

            float it;

            if(mPolynomialGrade3.size() <= 4) {
                it = 5000;
            } else {
                it = 500;
            }

            for (double a = xLow; a < xHigh; a += (xHigh - xLow) / it) {
                y = (float) mPolynomialGrade3.getY(a);

                if(y < yLow || y > yHigh) {
                    out = true;
                    continue;
                }

                else if(out && (y > yLow || y < yHigh)) {
                    out = false;
                    mPathPolynomial.moveTo(getTranslatedXPos((float) a), getTranslatedYPos(y));
                }

                mPathPolynomial.lineTo(getTranslatedXPos((float) a), getTranslatedYPos(y));
            }


            canvas.drawPath(mPathPolynomial, mPaintPolynomial);
        }
    }

    private void drawSummaryMode(Canvas canvas){

        mSummaryPaint.setStyle(Paint.Style.STROKE);

        float r = (getWidth() - dp(DP_PADDING_LEFT) - dp(DP_PADDING_RIGHT) > getHeight() - dp(DP_PADDING_TOP) - dp(DP_PADDING_BOTTOM)) ? (getHeight() - dp(DP_PADDING_TOP) - dp(DP_PADDING_BOTTOM)) / 2f : (getWidth() - dp(DP_PADDING_LEFT) - dp(DP_PADDING_RIGHT)) / 2f;

        float xCenter = ((getWidth() - dp(DP_PADDING_LEFT) - dp(DP_PADDING_RIGHT)) / 2f) + dp(DP_PADDING_LEFT);
        //float yCenter = ((getHeight() - dp(DP_PADDING_TOP) - dp(DP_PADDING_BOTTOM)) / 2f) + dp(DP_PADDING_TOP);
        float yCenter = getHeight() - dp(60);



        float startAngle = 180f;


        float percentage, sweepAngle;

        for(float f = 0f; f < 80; f += 20){
            mRectF.set(xCenter - r + dp(f), yCenter - r + dp(f), xCenter + r - dp(f), yCenter + r - dp(f));
            mSummaryPaint.setStrokeWidth(dp(20));
            mSummaryPaint.setColor(getColor(R.color.all_outer));
            canvas.drawArc(mRectF, startAngle, 180f, false, mSummaryPaint);
            mSummaryPaint.setStrokeWidth(dp(15));
            mSummaryPaint.setColor(getColor(R.color.all_inner));
            canvas.drawArc(mRectF, startAngle, 180f, false, mSummaryPaint);

        }

        if(mSummaryTotal > 0) {
            // low
            mRectF.set(xCenter - r, yCenter - r, xCenter + r, yCenter + r);
            percentage = (mSummaryLow * 100f) / (float) mSummaryTotal;
            sweepAngle = (percentage / 100f) * 180f;
            mSummaryPaint.setStrokeWidth(dp(20));
            mSummaryPaint.setColor(getColor(R.color.low_outer));
            canvas.drawArc(mRectF, startAngle, sweepAngle, false, mSummaryPaint);
            mSummaryPaint.setStrokeWidth(dp(15));
            mSummaryPaint.setColor(getColor(R.color.low_inner));
            canvas.drawArc(mRectF, startAngle, sweepAngle, false, mSummaryPaint);
            drawText(canvas, getResources().getString(R.string.glucose_low) + ": " + MyRound.round(percentage).toString() + "%", xCenter - r, dp(120), getColor(R.color.low_inner));
            startAngle += sweepAngle;

            // good
            mRectF.set(xCenter - r + dp(20), yCenter - r + dp(20), xCenter + r - dp(20), yCenter + r - dp(20));
            percentage = (mSummaryGood * 100f) / (float) mSummaryTotal;
            sweepAngle = (percentage / 100f) * 180f;
            mSummaryPaint.setStrokeWidth(dp(20));
            mSummaryPaint.setColor(getColor(R.color.good_outer));
            canvas.drawArc(mRectF, startAngle, sweepAngle, false, mSummaryPaint);
            mSummaryPaint.setStrokeWidth(dp(15));
            mSummaryPaint.setColor(getColor(R.color.good_inner));
            canvas.drawArc(mRectF, startAngle, sweepAngle, false, mSummaryPaint);
            drawText(canvas, getResources().getString(R.string.glucose_good) + ": " + MyRound.round(percentage).toString() + "%", xCenter - r, dp(140), getColor(R.color.good_inner));
            startAngle += sweepAngle;

            // regular
            mRectF.set(xCenter - r + dp(40), yCenter - r + dp(40), xCenter + r - dp(40), yCenter + r - dp(40));
            percentage = (mSummaryRegular * 100f) / (float) mSummaryTotal;
            sweepAngle = (percentage / 100f) * 180f;
            mSummaryPaint.setStrokeWidth(dp(20));
            mSummaryPaint.setColor(getColor(R.color.regular_outer));
            canvas.drawArc(mRectF, startAngle, sweepAngle, false, mSummaryPaint);
            mSummaryPaint.setStrokeWidth(dp(15));
            mSummaryPaint.setColor(getColor(R.color.regular_inner));
            canvas.drawArc(mRectF, startAngle, sweepAngle, false, mSummaryPaint);
            drawText(canvas, getResources().getString(R.string.glucose_regular) + ": " + MyRound.round(percentage).toString() + "%", xCenter + r, dp(120), getColor(R.color.regular_inner));
            startAngle += sweepAngle;

            // bad
            mRectF.set(xCenter - r + dp(60), yCenter - r + dp(60), xCenter + r - dp(60), yCenter + r - dp(60));
            percentage = (mSummaryBad * 100f) / (float) mSummaryTotal;
            sweepAngle = (percentage / 100f) * 180f;
            mSummaryPaint.setStrokeWidth(dp(20));
            mSummaryPaint.setColor(getColor(R.color.bad_outer));
            canvas.drawArc(mRectF, startAngle, sweepAngle, false, mSummaryPaint);
            mSummaryPaint.setStrokeWidth(dp(15));
            mSummaryPaint.setColor(getColor(R.color.bad_inner));
            canvas.drawArc(mRectF, startAngle, sweepAngle, false, mSummaryPaint);
            drawText(canvas, getResources().getString(R.string.glucose_bad) + ": " + MyRound.round(percentage).toString() + "%", xCenter + r, dp(140), getColor(R.color.bad_inner));


        } else {
            drawText(canvas, getResources().getString(R.string.glucose_low) + ": 0.0%", xCenter - r, dp(120), getColor(R.color.low_inner));
            drawText(canvas, getResources().getString(R.string.glucose_good) + ": 0.0%", xCenter - r, dp(140), getColor(R.color.good_inner));
            drawText(canvas, getResources().getString(R.string.glucose_regular) + ": 0.0%", xCenter + r, dp(120), getColor(R.color.regular_inner));
            drawText(canvas, getResources().getString(R.string.glucose_bad) + ": 0.0%", xCenter + r, dp(140), getColor(R.color.bad_inner));
        }

        mRectF.set(xCenter - r - dp(10), yCenter - (r * 2), xCenter + r + dp(10), yCenter + r + dp(10));
        mSummaryPaint.setStrokeWidth(dp(1));
        mSummaryPaint.setColor(Color.GRAY);
        //canvas.drawArc(mRectF, startAngle, 180f, false, mSummaryPaint);
        canvas.drawLine(xCenter - r - dp(10), yCenter, xCenter + r + dp(10), yCenter, mSummaryPaint);

        mSummaryPaint.setColor(Color.GRAY);
        mSummaryPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSummaryPaint.setTextSize(dp(30));
        float m = mSummaryPaint.measureText(mSummaryTotal + "");
        canvas.drawText(mSummaryTotal + "", xCenter - (m / 2f), yCenter - dp(10), mSummaryPaint);

    }




    //////////////////////////////////////////////////////////////////////////////
    /////////////////////////// DRAW TEXT RECTANGLES /////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    private ArrayList<Float> lefts = new ArrayList<>();
    private ArrayList<Float> ups = new ArrayList<>();
    private ArrayList<Float> rights = new ArrayList<>();
    private ArrayList<Float> downs = new ArrayList<>();


    private boolean isIn(float x, float y, float l, float u, float r, float d) {
        return x >= l - dp(10) && x <= r + dp(10) && y >= u - dp(10) && y <= d + dp(10);
    }

    private boolean checkCoordinates(float left, float up, float right, float down) {
        for(int a = 0; a < lefts.size(); a++) {
            float l, u, r, d;
            l = lefts.get(a);
            u = ups.get(a);
            r = rights.get(a);
            d = downs.get(a);

            if(isIn(left, up, l, u, r, d) || isIn(right, up, l, u, r, d) ||
                    isIn(right, down, l, u, r, d) || isIn(left, down, l, u, r, d)) {
                return false;
            }
        }

        lefts.add(left);
        ups.add(up);
        rights.add(right);
        downs.add(down);

        return true;
    }

    private void drawText(Canvas canvas, String s, float x, float y, int color) {
        if(s != null && !s.equals("")){

            float measure = mPaintTextStatisticalObject.measureText(s);
            float leftX = x - (measure / 2.0f) - dp(2);
            float upY = y - dp(7);
            float rightX = x + (measure / 2.0f) + dp(2);
            float downY = y + dp(7);

            if(leftX < getTranslatedXPos(xLow)) {
                float inc = getTranslatedXPos(xLow) - leftX;
                leftX += inc + dp(4);
                rightX += inc + dp(4);
            }

            if(rightX > getTranslatedXPos(xHigh)) {
                float dec = rightX - getTranslatedXPos(xHigh);
                leftX -= (dec + dp(4));
                rightX -= (dec + dp(4));
            }

            if(upY < getTranslatedYPos(yHigh)) {
                float inc = getTranslatedYPos(yHigh) - upY;
                upY += inc + dp(4);
                downY += inc + dp(4);
            }

            if(downY > getTranslatedYPos(yLow)) {
                float dec = downY - getTranslatedYPos(yLow);
                upY -= (dec + dp(4));
                downY -= (dec + dp(4));
            }

            while(!checkCoordinates(leftX, upY, rightX, downY)) {
                upY += 5;
                downY += 5;
            }

            canvas.drawRect(leftX, upY, rightX, downY, mPaintTextBackgroundStatisticalObject);

            mPathStatisticalLineColor.reset();

            mPaintStatisticalLineColor.setColor(color);
            mPaintStatisticalLineColor.setAlpha(255);


            canvas.drawRect(leftX - dp(1), upY, leftX + dp(4), downY, mPaintStatisticalLineColor);

            canvas.drawText(s, leftX + dp(5), downY - dp(3), mPaintTextStatisticalObject);
        }
    }

    private class GlucoseTestsComparator implements Comparator<GlucoseTest> {
        public int compare(GlucoseTest left, GlucoseTest right) {
            return left.getGlucoseLevel().compareTo(right.getGlucoseLevel());
        }
    }


    //////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////// ON DRAW METHOD ////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onDraw(Canvas canvas){
        lefts.clear();
        ups.clear();
        rights.clear();
        downs.clear();

        if(mGlobalCriteria == null && mSpecificCriteria == null) {
            drawNoCriteria(canvas);
        }
        else if(mLoading) {
            drawLoading(canvas);

        } else {
            if(mSummaryMode){
                drawCriteriaParameters(canvas);
                drawSummaryMode(canvas);
            } else {
                drawZones(canvas);
                drawChart(canvas);
                drawTextAxis(canvas);
                drawCriteriaParameters(canvas);
                drawAnnotations(canvas);

                StatisticalObject o = mChartPageElement.getStatisticalObject();

                if (o != null) {
                    if (o.isGlucoseTestsActivated()) {
                        drawGlucoseTests(canvas);
                    }

                    if (o.isMinimumActivated()) {
                        drawMinimum(canvas);
                    }

                    if (o.isMaximumActivated()) {
                        drawMaximum(canvas);
                    }

                    if (o.isMeanActivated()) {
                        drawMean(canvas);
                    }

                    if (o.isMedianActivated()) {
                        drawMedian(canvas);
                    }

                    if (o.isLinearRegressionActivated()) {
                        drawLinearRegression(canvas);
                    }

                    if (o.isPolynomialRegressionGrade2Activated()) {
                        drawPolynomialRegressionGrade2(canvas);
                    }

                    if (o.isPolynomialRegressionGrade3Activated()) {
                        drawPolynomialRegressionGrade3(canvas);
                    }

                    drawRectanglesAndText(canvas);
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////

    public void redraw(){
        mPathTextAxis.reset();
        mPathTests.reset();
        mPathChart.reset();
        mPathSeparators.reset();
        mPathMeanMedianMode.reset();
        mPathLinearRegression.reset();
        mPathTextBackgroundStatisticalObject.reset();
        mPathPolynomial.reset();
        invalidate();
    }


    public ChartPageElement getChartPageElement() {
        return mChartPageElement;
    }

    public void setChartPageElement(ChartPageElement chartPageElement) {
        this.mChartPageElement = chartPageElement;

        if(mChartPageElement != null) {
            switch (mChartPageElement.getType()) {
                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS:
                    DP_PADDING_BOTTOM = 50;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS:
                    DP_PADDING_BOTTOM = 40;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES:
                    DP_PADDING_BOTTOM = 63;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE:
                    DP_PADDING_BOTTOM = 63;
                    break;

            }
        }

    }

    public void setGlucoseTests(ArrayList<GlucoseTest> glucoseTests) {
        resetPaths();

        mPolynomialGrade2 = null;
        mPolynomialGrade3 = null;
        linearRegression = null;

        this.glucoseTests = glucoseTests;

        mSummaryTotal = 0;
        mSummaryBad = 0;
        mSummaryGood = 0;
        mSummaryRegular = 0;
        mSummaryLow = 0;

        // for the polynomial regression
        PolynomialFitter fitterLinear = new PolynomialFitter(1);

        PolynomialFitter fitterGrade2 = null;
        if(glucoseTests.size() > 1) {
            fitterGrade2 = new PolynomialFitter(2);
        }

        PolynomialFitter fitterGrade3 = null;
        if(glucoseTests.size() > 2) {
            fitterGrade3 = new PolynomialFitter(3);
        }

        // Aquí hay que ajustar el máximo y mínimo tanto de X como de Y
        // en función del tipo de gráfica que es y los valores que ofrecen
        // las glucemias pasadas.



        maximumValue = null;
        minimumValue = null;

        xLow = 0;
        xHigh = 0;
        yLow = 0;
        yHigh = 0;
        if(glucoseTests.size() == 0) {
            yHigh =  160;
            yLow = 40;
            switch (mChartPageElement.getType()) {
                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS:
                    xLow = 120;
                    xHigh = 22 * 60;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS:
                    xLow = 0;
                    xHigh = 24 * 60 * 7;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES:
                    xLow = -14;
                    xHigh = 0;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE:
                    break;
            }
        } else if(glucoseTests.size() == 1) {

            // for summary
            mSummaryTotal ++;
            if(glucoseTests.get(0).getGlucoseLevel() < 60){
                mSummaryLow ++;
            }
            else if(glucoseTests.get(0).getGlucoseLevel() < 150){
                mSummaryGood ++;
            }
            else if(glucoseTests.get(0).getGlucoseLevel() < 180){
                mSummaryRegular ++;

            } else {
                mSummaryBad ++;
            }

            Integer l = glucoseTests.get(0).getGlucoseLevel();

            // median and mean
            mean = (float) glucoseTests.get(0).getGlucoseLevel();
            median = (float) glucoseTests.get(0).getGlucoseLevel();

            if(l > 120) {
                yHigh =  l + 20;
                yLow = 40;
            } else {
                yHigh =  160;
                yLow = l - 20;
            }
            switch (mChartPageElement.getType()) {
                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS:
                    xLow = glucoseTests.get(0).getMinutesPassedSince5AM() - 60;
                    xHigh = glucoseTests.get(0).getMinutesPassedSince5AM() + 60;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS:
                    xLow = 0;
                    xHigh = 24 * 60 * 7;
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES:
                    float d = glucoseTests.get(0).getDaysPassedFromNow();

                    if(d > -14){
                        xLow = -14;
                        xHigh = 0;
                    } else {
                        xLow = new Instant(d - 7).setTimeToTheStartOfTheDay().getDaysPassedFromNow();
                        xHigh = new Instant(d + 7).setTimeToTheEndOfTheDay().getDaysPassedFromNow();
                    }
                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE:
                    break;
            }

        } else {

            // for mean
            float elements = 0;
            float total = 0;


            for (GlucoseTest test : glucoseTests) {
                // for summary
                mSummaryTotal ++;
                if(test.getGlucoseLevel() < 60){
                    mSummaryLow ++;
                }
                else if(test.getGlucoseLevel() < 150){
                    mSummaryGood ++;
                }
                else if(test.getGlucoseLevel() < 180){
                    mSummaryRegular ++;

                } else {
                    mSummaryBad ++;
                }

                // for mean
                elements += 1.0f;
                total += test.getGlucoseLevel();

                //
                // For the statistics Maximum and Minimum
                //
                if(maximumValue == null || ((GlucoseTest)maximumValue).getGlucoseLevel() < test.getGlucoseLevel()){
                    maximumValue = test;
                }

                if(minimumValue == null || ((GlucoseTest)minimumValue).getGlucoseLevel() > test.getGlucoseLevel()) {
                    minimumValue = test;
                }
                //
                //
                //


                if (yLow > test.getGlucoseLevel() || yLow == 0) {
                    yLow = test.getGlucoseLevel();
                }
                if (yHigh < test.getGlucoseLevel() || yHigh == 0) {
                    yHigh = test.getGlucoseLevel();
                }

                switch (mChartPageElement.getType()) {
                    case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS:
                        if (xLow > test.getMinutesPassedSince5AM() || xLow == 0) {
                            xLow = test.getMinutesPassedSince5AM();
                        }
                        if (xHigh < test.getMinutesPassedSince5AM() || xHigh == 0) {
                            xHigh = test.getMinutesPassedSince5AM();
                        }

                        //
                        // Linear regression
                        //
                        fitterLinear.addPoint(test.getMinutesPassedSince5AM(), test.getGlucoseLevel());

                        //
                        // polynomial regression
                        //
                        if(fitterGrade2 != null) {
                            fitterGrade2.addPoint(test.getMinutesPassedSince5AM(), test.getGlucoseLevel());
                        }

                        if(fitterGrade3 != null) {
                            fitterGrade3.addPoint(test.getMinutesPassedSince5AM(), test.getGlucoseLevel());
                        }
                        break;

                    case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS:
                        if (xLow > test.getMinutesPassedSince5AMMonday() || xLow == 0) {
                            xLow = test.getMinutesPassedSince5AMMonday();
                        }
                        if (xHigh < test.getMinutesPassedSince5AMMonday() || xHigh == 0) {
                            xHigh = test.getMinutesPassedSince5AMMonday();
                        }

                        //
                        // Linear regression
                        //
                        fitterLinear.addPoint(test.getMinutesPassedSince5AMMonday(), test.getGlucoseLevel());

                        //
                        // polynomial regression
                        //
                        if(fitterGrade2 != null) {
                            fitterGrade2.addPoint(test.getMinutesPassedSince5AMMonday(), test.getGlucoseLevel());
                        }

                        if(fitterGrade3 != null) {
                            fitterGrade3.addPoint(test.getMinutesPassedSince5AMMonday(), test.getGlucoseLevel());
                        }
                        break;

                    case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES:
                        float d = test.getDaysPassedFromNow();

                        if (xLow > d || xLow == 0) {
                            xLow = d;
                        }
                        if (xHigh < d || xHigh == 0) {
                            xHigh = d;
                        }

                        //
                        // Linear regression
                        //
                        fitterLinear.addPoint(d, test.getGlucoseLevel());

                        //
                        // polynomial regression
                        //
                        if(fitterGrade2 != null) {
                            fitterGrade2.addPoint(d, test.getGlucoseLevel());
                        }
                        if(fitterGrade3 != null) {
                            fitterGrade3.addPoint(d, test.getGlucoseLevel());
                        }
                        break;

                    case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE:
                        break;
                }
            }

            // for mean
            if(elements > 0) {
                mean = total / elements;
            }

            // for median
            if(glucoseTests.size() > 0) {
                Collections.sort(glucoseTests, new GlucoseTestsComparator());
                if (glucoseTests.size() % 2 == 0) {
                    // si es par
                    median = ((float) glucoseTests.get(glucoseTests.size() / 2).getGlucoseLevel() + (float) glucoseTests.get((glucoseTests.size() / 2) - 1).getGlucoseLevel()) / 2.0f;
                } else {
                    // si es impar
                    median = (float) glucoseTests.get(glucoseTests.size() / 2).getGlucoseLevel();
                }
            }


            linearRegression = fitterLinear.getBestFit();

            //
            // for polynomial regression
            //
            if(fitterGrade2 != null) {
                mPolynomialGrade2 = fitterGrade2.getBestFit();
            }
            if(fitterGrade3 != null) {
                mPolynomialGrade3 = fitterGrade3.getBestFit();
            }
        }



        float diffY = yHigh - yLow;
        float marginY = diffY / 15;

        if(yHigh != yLow) {

            yLow -= marginY;
            yHigh += marginY;

            while(yLow > 80) {
                yLow -= marginY;
            }
            while(yHigh < 130) {
                yHigh += marginY;
            }
        } else {
            if(yLow > 60) {
                yLow = 60;
            }
            if(yHigh < 150) {
                yHigh = 160;
            }
        }

        switch (mChartPageElement.getType()) {
            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS:
            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS:
            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES:
                float diffX = xHigh - xLow;
                float marginX = diffX / 30;

                xHigh += marginX;
                xLow -= marginX;

                break;
        }

        mLoading = false;
        ViewUtil.makeViewVisibleAnimatedly(ChartPageElementChartView.this);

        invalidate();
    }

    //
    //
    // Private methods to get maximum or minimum about two glucose tests passed
    //
    //
    private GlucoseTest getMaximumGlucoseTest(GlucoseTest g1, GlucoseTest g2) {
        if(g1 == null) {
            return g2;
        }
        else if(g2 == null) {
            return g1;
        }
        else {
            return g1.getGlucoseLevel() >= g2.getGlucoseLevel() ? g1 : g2;
        }
    }
    private GlucoseTest getMinimumGlucoseTest(GlucoseTest g1, GlucoseTest g2) {
        if(g1 == null) {
            return g2;
        }
        else if(g2 == null) {
            return g1;
        }
        else {
            return g1.getGlucoseLevel() <= g2.getGlucoseLevel() ? g1 : g2;
        }
    }

    public void setGlucoseTestsCrossedMeals(GlucoseTestsCrossedMeals testsCrossedMeals) {
        resetPaths();

        mPolynomialGrade2 = null;
        mPolynomialGrade3 = null;
        linearRegression = null;

        mTestsCrossedMeals = testsCrossedMeals;

        mSummaryTotal = 0;
        mSummaryBad = 0;
        mSummaryGood = 0;
        mSummaryRegular = 0;
        mSummaryLow = 0;

        xHigh = 0;
        xLow = 0;

        ChartPageElement element = getChartPageElement();
        switch (element.getType()) {
            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE:
                xHigh = 30;
                xLow = 50;
                yHigh =  160;
                yLow = 40;
                break;

            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_MEAL_HOUR:
                xHigh = 0;
                xLow = 0;
                break;
        }


        // for mean
        float elements = 0;
        float total = 0;

        // for median
        ArrayList<GlucoseTest> tests = new ArrayList<>();

        GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal minBreakfast = null, maxBreakfast = null, minLunch = null, maxLunch = null, minDinner = null, maxDinner = null;
        int minBreakfastMinutes = 0, maxBreakfastMinutes = 0, minLunchMinutes = 0, maxLunchMinutes = 0, minDinnerMinutes = 0, maxDinnerMinutes = 0;

        for(int a = 0; a < mTestsCrossedMeals.getCount(); a++) {

            GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal tcm = mTestsCrossedMeals.getItem(a);

            // for median
            if(tcm.getGlucoseTestBeforeMeal() != null) {
                tests.add(tcm.getGlucoseTestBeforeMeal());
            }
            if(tcm.getGlucoseTestAfterMeal() != null) {
                tests.add(tcm.getGlucoseTestAfterMeal());
            }


            //
            // For the statistics Maximum and Minimum
            //
            if(maximumValue == null){
                maximumValue = tcm;

            } else {
                GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal max = (GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal) maximumValue;

                GlucoseTest g = getMaximumGlucoseTest(tcm.getGlucoseTestAfterMeal(), tcm.getGlucoseTestBeforeMeal());
                GlucoseTest maxG = getMaximumGlucoseTest(max.getGlucoseTestAfterMeal(), max.getGlucoseTestBeforeMeal());

                GlucoseTest mg = getMaximumGlucoseTest(g, maxG);
                if(mg != null && g != null && mg.getGlucoseLevel().equals(g.getGlucoseLevel())){
                    maximumValue = tcm;
                }
            }

            if(minimumValue == null) {
                minimumValue = tcm;
            } else {
                GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal min = (GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal) minimumValue;

                GlucoseTest g = getMaximumGlucoseTest(tcm.getGlucoseTestAfterMeal(), tcm.getGlucoseTestBeforeMeal());
                GlucoseTest minG = getMaximumGlucoseTest(min.getGlucoseTestAfterMeal(), min.getGlucoseTestBeforeMeal());

                GlucoseTest mg = getMinimumGlucoseTest(g, minG);

                if(mg != null && g != null && mg.getGlucoseLevel().equals(g.getGlucoseLevel())){
                    minimumValue = tcm;
                }
            }
            //
            //
            //

            // for the summary
            if(tcm.getGlucoseTestAfterMeal() != null) {
                mSummaryTotal ++;
                if(tcm.getGlucoseTestAfterMeal().getGlucoseLevel() < 60){
                    mSummaryLow ++;
                }
                else if(tcm.getGlucoseTestAfterMeal().getGlucoseLevel() < 150){
                    mSummaryGood ++;
                }
                else if(tcm.getGlucoseTestAfterMeal().getGlucoseLevel() < 180){
                    mSummaryRegular ++;

                } else {
                    mSummaryBad ++;
                }

            }
            if(tcm.getGlucoseTestBeforeMeal() != null) {
                mSummaryTotal ++;
                if(tcm.getGlucoseTestBeforeMeal().getGlucoseLevel() < 60){
                    mSummaryLow ++;
                }
                else if(tcm.getGlucoseTestBeforeMeal().getGlucoseLevel() < 150){
                    mSummaryGood ++;
                } else if(tcm.getGlucoseTestBeforeMeal().getGlucoseLevel() < 180){
                    mSummaryRegular ++;

                } else {
                    mSummaryBad ++;
                }
            }
            //

            // for mean
            if(tcm.getGlucoseTestAfterMeal() != null) {
                elements += 1.0f;
                total += tcm.getGlucoseTestAfterMeal().getGlucoseLevel();


            }
            if(tcm.getGlucoseTestBeforeMeal() != null) {
                elements += 1.0f;
                total += tcm.getGlucoseTestBeforeMeal().getGlucoseLevel();
            }
            //


            GlucoseTestsCrossedMeals.GlucoseTestsCrossedOneMeal oneMeal = mTestsCrossedMeals.getItem(a);

            Meal m = oneMeal.getMeal();
            GlucoseTest before = oneMeal.getGlucoseTestBeforeMeal();
            GlucoseTest after = oneMeal.getGlucoseTestAfterMeal();

            if(element.getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE)) {

                if(xLow > m.getMealCarbohydrates()){
                    xLow = m.getMealCarbohydrates();
                }

                if(xHigh < m.getMealCarbohydrates()) {
                    xHigh = m.getMealCarbohydrates();
                }

            }
            else if(mChartPageElement.getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_MEAL_HOUR)){

                if(before != null) {

                    int minutes = before.getMinutesPassedSinceHourAndMinuteInstant(m);

                    switch (before.getGlucoseTime()) {
                        case C.GLUCOSE_TEST_BEFORE_BREAKFAST:
                            if(minBreakfastMinutes == 0 || minutes < minBreakfastMinutes) {
                                minBreakfastMinutes = minutes;
                            }
                            break;

                        case C.GLUCOSE_TEST_BEFORE_LUNCH:
                            if(minLunchMinutes == 0 || minutes < minLunchMinutes) {
                                minLunchMinutes = minutes;
                            }
                            break;

                        case C.GLUCOSE_TEST_BEFORE_DINNER:
                            if(minDinnerMinutes == 0 || minutes < minDinnerMinutes) {
                                minDinnerMinutes = minutes;
                            }
                            break;
                    }
                }
                if(after != null) {
                    int minutes = after.getMinutesPassedSinceHourAndMinuteInstant(m);

                    switch (after.getGlucoseTime()) {
                        case C.GLUCOSE_TEST_AFTER_BREAKFAST:
                            if(maxBreakfastMinutes == 0 || maxBreakfastMinutes < minutes) {
                                maxBreakfastMinutes = minutes;
                            }
                            break;

                        case C.GLUCOSE_TEST_AFTER_LUNCH:
                            if(maxLunchMinutes == 0 || maxLunchMinutes < minutes) {
                                maxLunchMinutes = minutes;
                            }
                            break;

                        case C.GLUCOSE_TEST_AFTER_DINNER:
                            if(maxDinnerMinutes == 0 || maxDinnerMinutes < minutes) {
                                maxDinnerMinutes = minutes;
                            }
                            break;
                    }
                }
            }


            // el ajuste del eje Y lo comparten los dos tipos de gráfica
            if(before != null && before.getGlucoseLevel() > yHigh) {
                yHigh = before.getGlucoseLevel();
            }
            if(before != null && before.getGlucoseLevel() < yLow) {
                yLow = before.getGlucoseLevel();
            }

            if(after != null && after.getGlucoseLevel() > yHigh) {
                yHigh = after.getGlucoseLevel();
            }
            if(after != null && after.getGlucoseLevel() < yLow) {
                yLow = after.getGlucoseLevel();
            }



        }

        if(element.getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE)) {
            float diffY = yHigh - yLow;
            float marginY = diffY / 15;

            if(yHigh != yLow) {

                yLow -= marginY;
                yHigh += marginY;

                while(yLow > 80) {
                    yLow -= marginY;
                }
                while(yHigh < 130) {
                    yHigh += marginY;
                }
            } else {
                if(yLow > 60) {
                    yLow = 60;
                }
                if(yHigh < 150) {
                    yHigh = 160;
                }
            }

            // si esto es true, es porque los resultados encontrados en la db es 0 o 1.
            if(xHigh <= xLow) {
                xHigh = 41;
                xLow = 39;
            }

            float diffX = xHigh - xLow;
            float marginX = diffX / 10;

            xHigh += marginX;
            xLow -= marginX;
        }

        else if(mChartPageElement.getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_MEAL_HOUR)){

            mBreakfastInterval = maxBreakfastMinutes - minBreakfastMinutes;
            mLunchInterval = maxLunchMinutes - minLunchMinutes;
            mDinnerInterval = maxDinnerMinutes - minDinnerMinutes;

            int nb = mBreakfastInterval / 5;
            int nl = mLunchInterval / 5;
            int nd = mDinnerInterval / 5;

            mBreakfastInterval += nb;
            mLunchInterval += nl;
            mDinnerInterval += nd;

            mBreakfastIntervalSituation = -minBreakfastMinutes + (nb / 2);
            mLunchIntervalSituation = -minLunchMinutes + (nl / 2);
            mDinnerIntervalSituation = -minDinnerMinutes + (nd / 2);

            xLow = 0;
            xHigh = mBreakfastInterval + mLunchInterval + mDinnerInterval;

        }



        // for mean
        if(elements > 0) {
            mean = total / elements;
        }

        // for median
        if(tests.size() > 0) {
            Collections.sort(tests, new GlucoseTestsComparator());
            if (tests.size() % 2 == 0) {
                // si es par
                median = ((float) tests.get(tests.size() / 2).getGlucoseLevel() + (float) tests.get((tests.size() / 2) - 1).getGlucoseLevel()) / 2.0f;
            } else {
                // si es impar
                median = (float) tests.get(tests.size() / 2).getGlucoseLevel();
            }

        }

        if(mTestsCrossedMeals.getCount() > 1) {
            // for regressions
            PolynomialFitter fitterLinear = new PolynomialFitter(1);
            PolynomialFitter fitterGrade2 = new PolynomialFitter(2);
            PolynomialFitter fitterGrade3 = new PolynomialFitter(3);

            ArrayList<Integer> glucoseLevelsBreakfast = new ArrayList<>();
            ArrayList<Integer> minutesSinceMealBreakfast = new ArrayList<>();
            ArrayList<Integer> glucoseLevelsLunch = new ArrayList<>();
            ArrayList<Integer> minutesSinceMealLunch = new ArrayList<>();
            ArrayList<Integer> glucoseLevelsDinner = new ArrayList<>();
            ArrayList<Integer> minutesSinceMealDinner = new ArrayList<>();

            for (int a = 0; a < mTestsCrossedMeals.getCount(); a++) {
                if (mTestsCrossedMeals.getItem(a).getGlucoseTestAfterMeal() != null) {
                    GlucoseTest g = mTestsCrossedMeals.getItem(a).getGlucoseTestAfterMeal();
                    Meal m = mTestsCrossedMeals.getItem(a).getMeal();

                    if(mChartPageElement.getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_MEAL_HOUR)) {
                        fitterLinear.addPoint(
                                g.getMinutesPassedSinceHourAndMinuteInstant(m),
                                g.getGlucoseLevel()
                        );
                        fitterGrade2.addPoint(
                                g.getMinutesPassedSinceHourAndMinuteInstant(m),
                                g.getGlucoseLevel()
                        );
                        fitterGrade3.addPoint(
                                g.getMinutesPassedSinceHourAndMinuteInstant(m),
                                g.getGlucoseLevel()
                        );

                    } else {
                        fitterLinear.addPoint(
                                m.getMealCarbohydrates(),
                                g.getGlucoseLevel()
                        );
                        fitterGrade2.addPoint(
                                m.getMealCarbohydrates(),
                                g.getGlucoseLevel()
                        );
                        fitterGrade3.addPoint(
                                m.getMealCarbohydrates(),
                                g.getGlucoseLevel()
                        );
                    }

                }
                if (mTestsCrossedMeals.getItem(a).getGlucoseTestBeforeMeal() != null) {
                    GlucoseTest g = mTestsCrossedMeals.getItem(a).getGlucoseTestBeforeMeal();
                    Meal m = mTestsCrossedMeals.getItem(a).getMeal();

                    if(mChartPageElement.getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_MEAL_HOUR)) {
                        switch (g.getGlucoseTime()){
                            case C.GLUCOSE_TEST_BEFORE_BREAKFAST:
                                glucoseLevelsBreakfast.add(g.getGlucoseLevel());
                                minutesSinceMealBreakfast.add(g.getMinutesPassedSinceHourAndMinuteInstant(m));
                                break;
                            case C.GLUCOSE_TEST_BEFORE_LUNCH:
                                glucoseLevelsLunch.add(g.getGlucoseLevel());
                                minutesSinceMealLunch.add(g.getMinutesPassedSinceHourAndMinuteInstant(m));
                                break;
                            case C.GLUCOSE_TEST_BEFORE_DINNER:
                                glucoseLevelsDinner.add(g.getGlucoseLevel());
                                minutesSinceMealDinner.add(g.getMinutesPassedSinceHourAndMinuteInstant(m));
                                break;
                        }
                    } else {
                        fitterLinear.addPoint(
                                m.getMealCarbohydrates(),
                                g.getGlucoseLevel()
                        );
                        fitterGrade2.addPoint(
                                m.getMealCarbohydrates(),
                                g.getGlucoseLevel()
                        );
                        fitterGrade3.addPoint(
                                m.getMealCarbohydrates(),
                                g.getGlucoseLevel()
                        );
                    }
                }

            }

            // si el tipo de gráfica es el de glucosa cruzada con la hora de la comida relativa
            // añadimos sólo un punto de antes de la comida en cuestión, la media
            if(mChartPageElement.getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_MEAL_HOUR)) {
                int tl;
                int tm;

                if(glucoseLevelsBreakfast.size() > 0) {
                    tl = 0;
                    tm = 0;
                    for(int a = 0; a < glucoseLevelsBreakfast.size(); a++) {
                        tl += glucoseLevelsBreakfast.get(a);
                        tm += minutesSinceMealBreakfast.get(a);
                    }

                    fitterLinear.addPoint((double) tm / (double)glucoseLevelsBreakfast.size(), (double)tl / (double)glucoseLevelsBreakfast.size());
                    fitterGrade2.addPoint((double) tm / (double)glucoseLevelsBreakfast.size(), (double)tl / (double)glucoseLevelsBreakfast.size());
                    fitterGrade3.addPoint((double) tm / (double)glucoseLevelsBreakfast.size(), (double)tl / (double)glucoseLevelsBreakfast.size());

                }

                if(glucoseLevelsLunch.size() > 0) {
                    tl = 0;
                    tm = 0;
                    for (int a = 0; a < glucoseLevelsLunch.size(); a++) {
                        tl += glucoseLevelsLunch.get(a);
                        tm += minutesSinceMealLunch.get(a);
                    }

                    fitterLinear.addPoint((double) tm / (double)glucoseLevelsLunch.size(), (double)tl / (double)glucoseLevelsLunch.size());
                    fitterGrade2.addPoint((double) tm / (double)glucoseLevelsLunch.size(), (double)tl / (double)glucoseLevelsLunch.size());
                    fitterGrade3.addPoint((double) tm / (double)glucoseLevelsLunch.size(), (double)tl / (double)glucoseLevelsLunch.size());

                }

                if(glucoseLevelsDinner.size() > 0) {
                    tl = 0;
                    tm = 0;
                    for (int a = 0; a < glucoseLevelsDinner.size(); a++) {
                        tl += glucoseLevelsDinner.get(a);
                        tm += minutesSinceMealDinner.get(a);
                    }

                    fitterLinear.addPoint((double) tm / (double)glucoseLevelsDinner.size(), (double)tl / (double)glucoseLevelsDinner.size());
                    fitterGrade2.addPoint((double) tm / (double)glucoseLevelsDinner.size(), (double)tl / (double)glucoseLevelsDinner.size());
                    fitterGrade3.addPoint((double) tm / (double)glucoseLevelsDinner.size(), (double)tl / (double)glucoseLevelsDinner.size());
                }


            }

            linearRegression = fitterLinear.getBestFit();
            mPolynomialGrade2 = fitterGrade2.getBestFit();
            mPolynomialGrade3 = fitterGrade3.getBestFit();
        }

        mLoading = false;
        ViewUtil.makeViewVisibleAnimatedly(ChartPageElementChartView.this);

        invalidate();

    }


    public void setGlobalCriteria(DataCollectionCriteria globalCriteria, DataDatabase db) {
        this.mGlobalCriteria = globalCriteria;
        if(mGlobalCriteria != null) {
            if(mGlobalCriteria.getSince().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION)) {
                annotationSinceGlobal = db.getAnnotationById(mGlobalCriteria.getSince().getData().intValue());
            }

            if(mGlobalCriteria.getUntil().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION)) {
                annotationUntilGlobal = db.getAnnotationById(mGlobalCriteria.getUntil().getData().intValue());
            }
        }
    }

    public void setSpecificCriteria(DataCollectionCriteria specificCriteria, DataDatabase db) {
        this.mSpecificCriteria = specificCriteria;
        if(mSpecificCriteria != null) {


            if(mSpecificCriteria.getSince().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION)) {
                annotationSinceSpecific = db.getAnnotationById(mSpecificCriteria.getSince().getData().intValue());
            }

            if(mSpecificCriteria.getUntil().getType().equals(C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION)) {
                annotationUntilSpecific = db.getAnnotationById(mSpecificCriteria.getUntil().getData().intValue());
            }
        }
    }

    /**
     *
     * @param annotations Anotaciones que serán mostradas en el caso de que se trate de ChartView de fechas
     */
    public void setAnnotations(ArrayList<Annotation> annotations){
        mAnnotations = annotations;
    }

    public void setLabels(ArrayList<Label> labels) {
        mLabels = labels;
    }

    public float getXHigh() {
        return xHigh;
    }

    public float getXLow() {
        return xLow;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int desiredWidth = (int)(getWidth() - dp(DP_PADDING_LEFT) - dp(DP_PADDING_RIGHT));
        int desiredHeight = (int) dp(390);

        switch (mChartPageElement.getType()) {
            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS:
                desiredHeight = (int) dp(400);
                break;

            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS:
                desiredHeight = (int) dp(390);
                break;

            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES:
                desiredHeight = (int) dp(413);
                break;

            case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE:
                desiredHeight = (int) dp(413);
                break;

        }


        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    private float getTranslatedXPos(float x){
        float translatedX;

        float width = getWidth() - (dp(DP_PADDING_LEFT) + dp(DP_PADDING_RIGHT));
        float unit = width / (xHigh - xLow);
        translatedX = ((x - xLow) * unit) + dp(DP_PADDING_LEFT);

        return translatedX;
    }

    private float getTranslatedYPos(float y){
        float height = getHeight() - (dp(DP_PADDING_TOP) + dp(DP_PADDING_BOTTOM));
        float unit = height / (yHigh - yLow);
        return (height + dp(DP_PADDING_TOP)) - (unit * (y - yLow));
    }


    private float dp(float p){
        // dp to pixels
        return p * density;
    }

    public void startLoading(){
        mLoading = true;
        mAnnotations = null;
        invalidate();
    }

    private void drawNoCriteria(Canvas canvas){
        String s = getContext().getResources().getString(R.string.activity_charts_no_criteria);
        float measure = mPaintTextAxis.measureText(s);
        canvas.drawText(s, (getWidth() / 2.0f) - (measure / 2.0f), getHeight() / 2.0f, mPaintTextAxis);
    }

    private void drawLoading(Canvas canvas) {
        String s = getContext().getResources().getString(R.string.activity_charts_loading_message);
        float measure = mPaintTextAxis.measureText(s);
        canvas.drawText(s, (getWidth() / 2.0f) - (measure / 2.0f), getHeight() / 2.0f, mPaintTextAxis);
    }


    private void resetPaths(){
        mPathTests.reset();
        mPathSeparators.reset();
        mPathMeanMedianMode.reset();
        mPathLinearRegression.reset();
        mPathTextBackgroundStatisticalObject.reset();
        mPathStatisticalLineColor.reset();
        mPathAnnotations.reset();
        mPathAnnotationsText.reset();
        mPathPolynomial.reset();
        mSummaryPath.reset();
    }

}
