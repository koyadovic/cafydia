package org.cafydia4.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import org.cafydia4.android.R;
import org.cafydia4.android.chartobjects.Label;
import org.cafydia4.android.core.Annotation;
import org.cafydia4.android.core.HbA1c;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.util.MyRound;
import org.cafydia4.android.util.PolynomialFitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by user on 26/11/14.
 */
public class ChartHbA1c extends View {
    private static final float SP_PADDING_TOP = 0;
    private static final float SP_PADDING_BOTTOM = 44;
    private static final float SP_PADDING_RIGHT = 0;
    private static final float SP_PADDING_LEFT = 0;

    private ArrayList<HbA1c> mLevels;
    private ArrayList<Annotation> mAnnotations = null;
    private ArrayList<Label> mLabels = null;

    private Path mPathChart;
    private Path mPathLine;
    private Path mPathRegressionLine;
    private Path mPathFill;
    private Path mPathTextAxis;
    private Path mPathSeparators;
    private Path mPathAnnotations;
    private Path mPathAnnotationsText;
    private Path mPathTextBackgroundStatisticalObject;
    private Path mPathStatisticalLineColor;
    private Path mPathCurrentHbA1c;
    private Path mPathFutureLine;

    private ArrayList<Float> mAnnotationsXs = new ArrayList<>();

    private Paint mPaintChart;
    private Paint mPaintLine;
    private Paint mPaintRegressionLine;
    private Paint mPaintFill;
    private Paint mPaintTextAxis;
    private Paint mPaintSeparators;
    private Paint mPaintAnnotations;
    private Paint mPaintAnnotationsText;
    private Paint mPaintRedZone;

    private Paint mPaintTextStatisticalObject;
    private Paint mPaintStatisticalLineColor;

    private Paint mPaintTextBackgroundStatisticalObject;
    private Paint mPaintCurrentHbA1c;
    private Paint mPaintFutureLine;


    private PolynomialFitter.Polynomial mPolynomial;

    private float xLow = 0, xHigh = 0;
    private float yLow = 6, yHigh = 6;

    private float density = 1.0f;

    private Instant i;
    private Instant today;

    public ChartHbA1c (Context c, AttributeSet a){
        super(c, a);
        init(c);
    }

    private void drawZones(Canvas canvas) {
        canvas.drawRect(
                getTranslatedXPos(xLow),
                getTranslatedYPos(yHigh),
                getTranslatedXPos(xHigh),
                getTranslatedYPos(yLow),
                mPaintRedZone
        );
    }

    @Override
    public void onDraw(Canvas canvas){
        lefts.clear();
        ups.clear();
        rights.clear();
        downs.clear();

        drawZones(canvas);

        // chart
        if(mPathChart.isEmpty()) {
            //mPathChart.moveTo(getTranslatedXPos(xLow), getTranslatedYPos(yHigh));
            mPathChart.moveTo(getTranslatedXPos(xLow), getTranslatedYPos(yLow));
            mPathChart.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(yLow));
        }
        canvas.drawPath(mPathChart, mPaintChart);

        // text axis
        float unit = (yHigh - yLow) / 4.0f;

        if(unit > 0) {

            canvas.drawText(MyRound.round(yLow + (unit * 0.2f), 2).toString() + "%", getTranslatedXPos(xLow) + dp(2), getTranslatedYPos(yLow + (unit * 0.2f)) - dp(16), mPaintTextAxis);
            canvas.drawText(MyRound.round(yLow + (unit * 1.2f), 2).toString() + "%", getTranslatedXPos(xLow) + dp(2), getTranslatedYPos(yLow + (unit * 1.2f)) - dp(16), mPaintTextAxis);
            canvas.drawText(MyRound.round(yLow + (unit * 2.2f), 2).toString() + "%", getTranslatedXPos(xLow) + dp(2), getTranslatedYPos(yLow + (unit * 2.2f)) - dp(16), mPaintTextAxis);
            canvas.drawText(MyRound.round(yLow + (unit * 3.2f), 2).toString() + "%", getTranslatedXPos(xLow) + dp(2), getTranslatedYPos(yLow + (unit * 3.2f)) - dp(16), mPaintTextAxis);

            canvas.drawText(MyRound.round(HbA1c.percentToMol(yLow + (unit * 0.2f)), 1).toString() + "mmol/mol", getTranslatedXPos(xLow) + dp(2), getTranslatedYPos(yLow + (unit * 0.2f)) - dp(4), mPaintTextAxis);
            canvas.drawText(MyRound.round(HbA1c.percentToMol(yLow + (unit * 1.2f)), 1).toString() + "mmol/mol", getTranslatedXPos(xLow) + dp(2), getTranslatedYPos(yLow + (unit * 1.2f)) - dp(4), mPaintTextAxis);
            canvas.drawText(MyRound.round(HbA1c.percentToMol(yLow + (unit * 2.2f)), 1).toString() + "mmol/mol", getTranslatedXPos(xLow) + dp(2), getTranslatedYPos(yLow + (unit * 2.2f)) - dp(4), mPaintTextAxis);
            canvas.drawText(MyRound.round(HbA1c.percentToMol(yLow + (unit * 3.2f)), 1).toString() + "mmol/mol", getTranslatedXPos(xLow) + dp(2), getTranslatedYPos(yLow + (unit * 3.2f)) - dp(4), mPaintTextAxis);

        } else {
            canvas.drawText(MyRound.round(yLow, 2).toString() + "%", getTranslatedXPos(xLow) + dp(2), getTranslatedYPos(yLow) - dp(16), mPaintTextAxis);
            canvas.drawText(MyRound.round(HbA1c.percentToMol(yLow), 1).toString() + "mmol/mol", getTranslatedXPos(xLow) + dp(2), getTranslatedYPos(yLow) - dp(4), mPaintTextAxis);
        }

        mPathSeparators.reset();
        mPathSeparators.moveTo(getTranslatedXPos(xLow), getTranslatedYPos(yLow + (unit * 0.2f)));
        mPathSeparators.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(yLow + (unit * 0.2f)));

        mPathSeparators.moveTo(getTranslatedXPos(xLow), getTranslatedYPos(yLow + (unit * 1.2f)));
        mPathSeparators.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(yLow + (unit * 1.2f)));

        mPathSeparators.moveTo(getTranslatedXPos(xLow), getTranslatedYPos(yLow + (unit * 2.2f)));
        mPathSeparators.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(yLow + (unit * 2.2f)));

        mPathSeparators.moveTo(getTranslatedXPos(xLow), getTranslatedYPos(yLow + (unit * 3.2f)));
        mPathSeparators.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(yLow + (unit * 3.2f)));

        // the line and its fill
        if(mLevels != null) {
            unit = (xHigh - xLow) / 5.0f;
            float d;
            String lastDateString = "";

            for (float a = xLow + (unit / 2f); a < xHigh ; a += unit) {
                mPathTextAxis.reset();
                i.setTimeInMilis(today.toDate().getTime() + (long) (a * 24 * 60 * 60 * 1000));

                float measure = mPaintTextAxis.measureText(i.getUserDateString());

                if (getTranslatedXPos(a) + (measure - dp(30)) > getTranslatedXPos(xHigh))
                    continue;

                d = i.getDaysPassedFromInstant(today);

                mPathTextAxis.moveTo(getTranslatedXPos(d) - dp(2), getTranslatedYPos(yLow) + dp(10));
                mPathTextAxis.lineTo(getTranslatedXPos(d) - dp(2) + measure, getTranslatedYPos(yLow) + dp(60));

                if (!lastDateString.equals(i.getUserDateStringShort())) {
                    canvas.drawTextOnPath(i.getUserDateStringShort(), mPathTextAxis, 0, 0, mPaintTextAxis);
                    lastDateString = i.getUserDateStringShort();
                }


            }

            if (xLow < -14f && mLevels.size() >= 10) {
                float i;
                float start = today.getDaysPassedFromNow();

                int points = 800;
                float increment = (start - mLevels.get(mLevels.size() - 1).getDaysPassedFromInstant(today)) / points;

                mPathFill.moveTo(getTranslatedXPos(xHigh), getTranslatedYPos((float) mPolynomial.getY(xHigh)));
                mPathFutureLine.moveTo(getTranslatedXPos(xHigh), getTranslatedYPos((float) mPolynomial.getY(xHigh)));

                for(i = xHigh; i >= start; i -= increment){
                    float y = (float) mPolynomial.getY(i);
                    if(y > yHigh || y < yLow) {
                        mPathFill.moveTo(getTranslatedXPos(i), getTranslatedYPos(y));
                        mPathFutureLine.moveTo(getTranslatedXPos(i), getTranslatedYPos(y));
                        continue;
                    }

                    mPathFill.lineTo(getTranslatedXPos(i), getTranslatedYPos(y));
                    mPathFutureLine.lineTo(getTranslatedXPos(i), getTranslatedYPos(y));
                }
                canvas.drawPath(mPathFutureLine, mPaintFutureLine);

                mPathRegressionLine.moveTo(getTranslatedXPos(start), getTranslatedYPos((float) mPolynomial.getY(start)));

                for (i = start; i >= mLevels.get(mLevels.size() - 1).getDaysPassedFromInstant(today); i -= increment) {

                    float y = (float) mPolynomial.getY(i);
                    if(y > yHigh || y < yLow) {
                        mPathFill.moveTo(getTranslatedXPos(i), getTranslatedYPos(y));
                        mPathRegressionLine.moveTo(getTranslatedXPos(i), getTranslatedYPos(y));
                        continue;
                    }

                    mPathFill.lineTo(getTranslatedXPos(i), getTranslatedYPos(y));
                    mPathRegressionLine.lineTo(getTranslatedXPos(i), getTranslatedYPos(y));


                }

                mPathFill.lineTo(getTranslatedXPos(xLow), getTranslatedYPos((float) mPolynomial.getY(xLow)));
                mPathRegressionLine.lineTo(getTranslatedXPos(xLow), getTranslatedYPos((float) mPolynomial.getY(xLow)));

                mPathFill.lineTo(getTranslatedXPos(xLow), getTranslatedYPos(yLow));
                mPathFill.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(yLow));

                float y = (float) mPolynomial.getY(xHigh);

                if(y > yHigh) {
                    mPathFill.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(yHigh));
                } else if(y > yLow){
                    mPathFill.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos((float) mPolynomial.getY(xHigh)));
                }

                canvas.drawPath(mPathFill, mPaintFill);
                canvas.drawPath(mPathRegressionLine, mPaintRegressionLine);

                HbA1c h = mLevels.get(0);
                drawCurrentHbA1c(canvas, h);

            } else {

                if(mLevels.size() > 0){
                    HbA1c h = mLevels.get(0);
                    drawCurrentHbA1c(canvas, h);

                }
            }
        }

        canvas.drawPath(mPathSeparators, mPaintSeparators);

        float measure;
        mAnnotationsXs.clear();

        if(mAnnotations != null){
            for (Annotation a : mAnnotations){

                if(a.getDaysPassedFromInstant(today) < xLow || a.getDaysPassedFromInstant(today) > xHigh) continue;

                mPathAnnotations.reset();

                float x = getTranslatedXPos(a.getDaysPassedFromInstant(today));


                measure = mPaintAnnotationsText.measureText(a.getNumber().toString());

                float y = getTranslatedYPos(yHigh) + dp(30);

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

                canvas.drawPath(mPathAnnotations, mPaintAnnotations);
                canvas.drawRect(x - measure - dp(9), y, x - dp(1), y + dp(20), mPaintAnnotations);

                if(a.getNumber() > 0)
                    canvas.drawText(a.getNumber().toString(), x - measure - dp(4), y + dp(16), mPaintAnnotationsText);

            }
        }

    }

    private void drawCurrentHbA1c(Canvas canvas, HbA1c current){
        float x = getTranslatedXPos(current.getDaysPassedFromInstant(today));
        float y = getTranslatedYPos(current.getPercentage());

        mPaintCurrentHbA1c.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(x, y, dp(4), mPaintCurrentHbA1c); // O

        String s = "";
        s += getResources().getString(R.string.activity_charts_current_hba1c) + ": ";
        s += MyRound.round(current.getPercentage()) + "% - " + MyRound.round(current.getMmolMol()) + "mmol/mol";
        float m = mPaintTextStatisticalObject.measureText(s);

        mPaintCurrentHbA1c.setStyle(Paint.Style.STROKE);
        drawText(canvas, s, getTranslatedXPos(xHigh), getTranslatedYPos(yHigh), mPaintCurrentHbA1c.getColor());
    }

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
                upY -= 5;
                downY -= 5;
            }

            canvas.drawRect(leftX, upY, rightX, downY, mPaintTextBackgroundStatisticalObject);

            mPathStatisticalLineColor.reset();

            mPaintStatisticalLineColor.setColor(color);
            mPaintStatisticalLineColor.setAlpha(255);


            canvas.drawRect(leftX - dp(1), upY, leftX + dp(4), downY, mPaintStatisticalLineColor);

            canvas.drawText(s, leftX + dp(5), downY - dp(3), mPaintTextStatisticalObject);
        }
    }

    private int getColor(int colorId){
        return getContext().getResources().getColor(colorId);
    }

    private void init(Context c){
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        i = new Instant();
        today = new Instant().setTimeToTheEndOfTheDay();

        // get pixel density
        density = c.getResources().getDisplayMetrics().density;

        // paint for the chart
        mPaintChart = new Paint();
        mPaintChart.setAntiAlias(true);
        mPaintChart.setColor(getColor(R.color.chart_hba1c_chart)); // default alpha FF 0xFF000000
        mPaintChart.setStrokeWidth(dp(1));
        mPaintChart.setStyle(Paint.Style.STROKE);
        mPaintChart.setDither(true);
        mPathChart = new Path();


        // paint for the main line
        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true);
        mPaintLine.setColor(getColor(R.color.chart_hba1c_line)); // default alpha AA 0xAA007799
        mPaintLine.setStrokeWidth(dp(4));
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setDither(true);
        mPaintLine.setPathEffect(new CornerPathEffect(dp(2)));
        //mPaintLine.setShadowLayer(dp(3), dp(5), dp(5), 0x55000000);
        mPathLine = new Path();

        // paint for the regression line
        mPaintRegressionLine = new Paint();
        mPaintRegressionLine.setAntiAlias(true);
        mPaintRegressionLine.setColor(getColor(R.color.chart_hba1c_regression_line)); // default alpha AA 0xAA007799
        mPaintRegressionLine.setStrokeWidth(dp(2));
        mPaintRegressionLine.setStyle(Paint.Style.STROKE);
        mPaintRegressionLine.setDither(true);
        mPaintRegressionLine.setPathEffect(new CornerPathEffect(dp(4)));
        //mPaintLine.setShadowLayer(dp(3), dp(5), dp(5), 0x55000000);
        mPathRegressionLine = new Path();


        mPaintFill = new Paint();
        mPaintFill.setAntiAlias(true);
        mPaintFill.setColor(getColor(R.color.chart_hba1c_fill)); // default alpha AA 0xAA007799
        mPaintFill.setStrokeWidth(dp(1));
        mPaintFill.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintFill.setDither(true);
        //mPaintFill.setPathEffect(new CornerPathEffect(dp(10)));
        mPathFill = new Path();

        // for normal text
        mPaintTextAxis = new Paint();
        mPaintTextAxis.setAntiAlias(true);
        mPaintTextAxis.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextAxis.setColor(getColor(R.color.chart_hba1c_text_axis));
        mPaintTextAxis.setTextSize(dp(13));
        mPathTextAxis = new Path();

        mPaintSeparators = new Paint();
        mPaintSeparators.setAntiAlias(true);
        mPaintSeparators.setColor(getColor(R.color.chart_hba1c_separators));
        mPaintSeparators.setStyle(Paint.Style.STROKE);
        mPaintSeparators.setStrokeWidth(dp(1));
        mPaintSeparators.setDither(true);
        mPathSeparators = new Path();

        mPaintAnnotations = new Paint();
        mPaintAnnotations.setAntiAlias(true);
        mPaintAnnotations.setColor(getResources().getColor(R.color.colorAnnotations));
        mPaintAnnotations.setStrokeWidth(dp(1));
        mPaintAnnotations.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintAnnotations.setDither(true);
        mPathAnnotations = new Path();

        mPaintAnnotationsText = new Paint();
        mPaintAnnotationsText.setAntiAlias(true);
        mPaintAnnotationsText.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintAnnotationsText.setColor(getResources().getColor(R.color.colorAnnotationsText));
        mPaintAnnotationsText.setTextSize(dp(16));
        mPathAnnotationsText = new Path();

        mPaintRedZone = new Paint();
        mPaintRedZone.setAntiAlias(true);
        mPaintRedZone.setColor(getColor(R.color.chart_hba1c_red_zone));
        mPaintRedZone.setStyle(Paint.Style.FILL);
        mPaintRedZone.setDither(true);

        mPaintTextStatisticalObject = new Paint();
        mPaintTextStatisticalObject.setAntiAlias(true);
        mPaintTextStatisticalObject.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextStatisticalObject.setColor(getColor(R.color.chart_hba1c_text_statistical_object));
        mPaintTextStatisticalObject.setTextSize(dp(13));

        mPaintTextBackgroundStatisticalObject = new Paint();
        mPaintTextBackgroundStatisticalObject.setAntiAlias(true);
        mPaintTextBackgroundStatisticalObject.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextBackgroundStatisticalObject.setColor(getColor(R.color.chart_hba1c_background_statistical_object));
        mPaintTextBackgroundStatisticalObject.setStrokeWidth(dp(6));
        mPaintTextBackgroundStatisticalObject.setPathEffect(new CornerPathEffect(dp(4)));
        mPathTextBackgroundStatisticalObject = new Path();

        mPaintStatisticalLineColor = new Paint();
        mPaintStatisticalLineColor.setAntiAlias(true);
        mPaintStatisticalLineColor.setStyle(Paint.Style.FILL);
        mPaintStatisticalLineColor.setStrokeWidth(dp(2));
        mPaintStatisticalLineColor.setPathEffect(new CornerPathEffect(dp(2)));
        mPathStatisticalLineColor = new Path();


        mPaintFutureLine = new Paint();
        mPaintFutureLine.setAntiAlias(true);
        mPaintFutureLine.setColor(getColor(R.color.chart_hba1c_future_line)); // default alpha AA 0xAA007799
        mPaintFutureLine.setStrokeWidth(dp(2));
        mPaintFutureLine.setStyle(Paint.Style.STROKE);
        mPaintFutureLine.setDither(true);
        mPaintFutureLine.setPathEffect(new CornerPathEffect(dp(4)));
        mPaintFutureLine.setPathEffect(new DashPathEffect(new float[] {dp(10), dp(3)}, 0));
        mPathFutureLine = new Path();

        mPaintCurrentHbA1c = new Paint();
        mPaintCurrentHbA1c.setAntiAlias(true);
        mPaintCurrentHbA1c.setColor(getColor(R.color.chart_hba1c_current_hba1c)); // default alpha AA 0xAA007799
        mPaintCurrentHbA1c.setStrokeWidth(dp(4));
        mPaintCurrentHbA1c.setStyle(Paint.Style.STROKE);
        mPaintCurrentHbA1c.setDither(true);
        mPathCurrentHbA1c = new Path();

    }

    public void setAnnotations(ArrayList<Annotation> annotations){
        this.mAnnotations = annotations;
    }

    //
    // Ordena cronológicamente desde el más nuevo, index 0 al más antiguo, index n.
    //
    private class InstantDaysPassedFromNowComparator implements Comparator<Instant> {
        public int compare(Instant left, Instant right) {
            return right.getDaysPassedFromInstant(today) < left.getDaysPassedFromInstant(today) ? -1 : 1;
        }
    }

    public void setHbA1cs(ArrayList<HbA1c> hbA1cs){
        mLevels = hbA1cs;


        yHigh = 0f;
        yLow = 10f;
        xHigh = 14f;
        xLow = -0.2f;

        PolynomialFitter fitter;

        // si no hay datos suficientes se marca esto:
        if(mLevels.size() != 0 && mLevels.get(mLevels.size() - 1).getDaysPassedFromInstant(today) < -30) {
            fitter = new PolynomialFitter(3);
        } else {
            fitter = new PolynomialFitter(2);
        }


        // hay que ordenarlas primero, porque a veces aparecen desordenadas de la db
        Collections.sort(mLevels, new InstantDaysPassedFromNowComparator());

        // aquí hay que ajustar el xhigh, xlow, yhigh y ylow

        for(HbA1c h : mLevels){
            float daysPassedFromNow = h.getDaysPassedFromInstant(today);
            float level = h.getPercentage();
            fitter.addPoint(daysPassedFromNow, level);

            // X's
            if(daysPassedFromNow < xLow){
                xLow = new Instant(daysPassedFromNow).setTimeToTheStartOfTheDay().getDaysPassedFromInstant(today);
            }
            else if(daysPassedFromNow > xHigh){
                xHigh = new Instant(daysPassedFromNow).setTimeToTheEndOfTheDay().getDaysPassedFromInstant(today);
            }
        }

        mPolynomial = fitter.getBestFit();

        // si no hay datos suficientes se marca esto:
        if(xLow > -7f || mLevels.size() < 10) {

            xLow = -14f;
            xHigh = 7f;

            if(mLevels.size() > 0) {
                // cogemos el último valor más reciente
                HbA1c h = mLevels.get(0);
                yLow = h.getPercentage() - 0.3f;
                yHigh = h.getPercentage() + 0.3f;
            } else {
                yLow = 5.5f;
                yHigh = 7f;
            }

        } else {
            for(float d=xLow; d<=1; d+= ((1 - xLow) / 100) ){
                // Y's
                if(mPolynomial.getY(d) > yHigh){
                    yHigh = (float) mPolynomial.getY(d);
                }
                if(mPolynomial.getY(d) < yLow){
                    yLow = (float) mPolynomial.getY(d);
                }

            }

            float diff = yHigh - yLow;

            if(diff / 5f < 0.5)
                diff = 0.5f * 5f;

            yHigh += (diff / 5f);
            yLow -= (diff / 5f);
        }

    }

    public void setLabels(ArrayList<Label> labels) {
        mLabels = labels;
    }

    public void refresh(){
        mPathChart.reset();
        mPathLine.reset();
        mPathFill.reset();
        invalidate();
    }

    private float getTranslatedXPos(float x){
        float width = getWidth() - dp(SP_PADDING_LEFT) - dp(SP_PADDING_RIGHT);
        float unit = width / (xHigh - xLow);
        return ((x - xLow) * unit) + dp(SP_PADDING_LEFT);
    }

    private float getTranslatedYPos(float y){
        float height = getHeight() - (dp(SP_PADDING_TOP) + dp(SP_PADDING_BOTTOM));
        float unit = height / (yHigh - yLow);
        return (height + dp(SP_PADDING_TOP)) - (unit * (y - yLow));
    }

    private float dp(float p){

        // dp to pixels
        return p * density;
    }
}
