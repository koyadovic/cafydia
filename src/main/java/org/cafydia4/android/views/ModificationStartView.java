package org.cafydia4.android.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import org.cafydia4.android.R;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.recommendations.ModificationStart;
import org.cafydia4.android.recommendations.ModificationStartDot;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.MyRound;

/**
 * Created by user on 16/09/14.
 */
public class ModificationStartView extends View {
    private static final float MAX_MODIFICATION = 10.0f;
    private static final float MAX_DAYS_FORWARD = 10.0f;

    private static final float SP_PADDING_TOP = 20;
    private static final float SP_PADDING_BOTTOM = 20;
    private static final float SP_PADDING_RIGHT = 20;
    private static final float SP_PADDING_LEFT = 20;

    private int metabolicStateStartType = C.STARTING_TYPE_GLOBAL;
    private int metabolicRhythmStartType = C.STARTING_TYPE_GLOBAL;

    private Instant startDate = null;
    private Float daysPassed = null;

    private String mAbscissaTitle = "";
    private String mOrdinateTitle = "";

    private float density;

    private Paint mPaintToday;
    private Paint mPaintChart;
    private Paint mPaintLine;
    private Paint mPaintDotBorderSpecific;
    private Paint mPaintDotBorderGlobal;
    private Paint mPaintDotInner;
    private Paint mPaintTextDot;
    private Paint mPaintStartDate;

    private Paint mPaintTextAxis;
    private Paint mPaintTextNumberAxis;

    private Path mPathToday;
    private Path mPathForChart;
    private Path mPathForLine;
    private Path mPathText;
    private Path mPathStartDate;


    private ModificationStart mStart;
    private String[] daysStr = {
            "-1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", ""
    };
    private String[] modificationStr = {
            "-10", "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1",
            "0",
            "+1", "+2", "+3", "+4", "+5", "+6", "+7", "+8", "+9", "+10"
    };

    private int numberOfDots = 0;

    public ModificationStartView(Context c, AttributeSet a){
        super(c, a);
        init(c);
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(numberOfDots > 0) {
            drawChart(canvas);

            // the line
            ModificationStartDot sDot = mStart.getFirstDot();
            if(sDot != null) {
                if(mPathForLine.isEmpty()) {
                    if (sDot.getX() != -1.0) {
                        mPathForLine.moveTo(getTranslatedXPos(-1), getTranslatedYPos(0));
                    } else {
                        mPathForLine.moveTo(getTranslatedXPos(sDot.getX()), getTranslatedYPos(sDot.getY()));
                    }
                    for (float x = -1.0f; x <= MAX_DAYS_FORWARD; x = x + 0.2f) {
                        mPathForLine.lineTo(getTranslatedXPos(x), getTranslatedYPos(mStart.getModification(x)));
                    }
                    mPathForLine.lineTo(getWidth(), getTranslatedYPos(mStart.getModification(MAX_DAYS_FORWARD)));
                }
                mPaintLine.setStrokeWidth((4 * density));
                canvas.drawPath(mPathForLine, mPaintLine);

                for (ModificationStartDot dot : mStart.getDots()) {
                    if(dot.getX() == -1){
                        if(metabolicStateStartType == C.STARTING_TYPE_SPECIFIC) {
                            canvas.drawCircle(getTranslatedXPos(dot.getX()), getTranslatedYPos(dot.getY()), dp(15), mPaintDotBorderSpecific);
                        } else {
                            canvas.drawCircle(getTranslatedXPos(dot.getX()), getTranslatedYPos(dot.getY()), dp(15), mPaintDotBorderGlobal);
                        }
                    } else {
                        if(metabolicRhythmStartType == C.STARTING_TYPE_SPECIFIC){
                            canvas.drawCircle(getTranslatedXPos(dot.getX()), getTranslatedYPos(dot.getY()), dp(15), mPaintDotBorderSpecific);
                        } else {
                            canvas.drawCircle(getTranslatedXPos(dot.getX()), getTranslatedYPos(dot.getY()), dp(15), mPaintDotBorderGlobal);
                        }

                    }
                    canvas.drawCircle(getTranslatedXPos(dot.getX()), getTranslatedYPos(dot.getY()), dp(12), mPaintDotInner);

                    mPaintTextDot.setTextSize((11f * density));

                    float w = mPaintTextDot.measureText(modificationStr[MyRound.round(dot.getY(),0).intValue() + 10]);

                    canvas.drawText(modificationStr[MyRound.round(dot.getY(),0).intValue() + 10],
                            getTranslatedXPos(dot.getX()) - (w / 2),
                            getTranslatedYPos(dot.getY()) + (4f * density), mPaintTextDot);
                }
            }

        }
    }

    private void drawChart(Canvas canvas){
        // draw the chart
        if (mPathForChart.isEmpty()) {
            mPathForChart.moveTo(getTranslatedXPos(0), 0);
            mPathForChart.lineTo(getTranslatedXPos(0), getHeight());
            mPathForChart.moveTo(getTranslatedXPos(-1), getTranslatedYPos(0));
            mPathForChart.lineTo(getWidth(), getTranslatedYPos(0));
        }
        canvas.drawPath(mPathForChart, mPaintChart);

        // today date string
        if(mPathStartDate.isEmpty() && startDate != null){
            float measuredStartDate = mPaintStartDate.measureText(startDate.getUserDateString());
            mPathStartDate.moveTo(getTranslatedXPos(0f), measuredStartDate + dp(6));
            mPathStartDate.lineTo(getTranslatedXPos(0f), dp(4));
        }
        if(startDate != null) {
            canvas.drawTextOnPath(startDate.getUserDateString(), mPathStartDate, 0, dp(10), mPaintStartDate);
        }

        // today
        if(daysPassed != null && mPathToday.isEmpty()){
            if(daysPassed > 11){
                canvas.drawRect(getTranslatedXPos(10.5f) - dp(10), 0, getTranslatedXPos(10.5f) + dp(10), getHeight(), mPaintToday);
            } else {
                canvas.drawRect(getTranslatedXPos(daysPassed.intValue()) - dp(10), 0, getTranslatedXPos(daysPassed.intValue()) + dp(10), getHeight(), mPaintToday);
            }
        }


        // days numbers
        for (float x = -1; x <= MAX_DAYS_FORWARD; x++) {
            if(x == 0) continue;
            canvas.drawText(daysStr[(int)x + 1], getTranslatedXPos(x) - (mPaintTextNumberAxis.measureText(daysStr[(int) x + 1]) / 2.0f), getTranslatedYPos(0f) + (15f * density), mPaintTextNumberAxis);
        }

        // ordinate title
        float ordText = mPaintTextAxis.measureText(mOrdinateTitle);
        if(mPathText.isEmpty()) {
            mPathText.moveTo(getTranslatedXPos(-0f), getHeight());
            mPathText.lineTo(getTranslatedXPos(-0f), getHeight() - ordText);
        }
        canvas.drawTextOnPath(mOrdinateTitle, mPathText, 0, dp(-5), mPaintTextAxis);

        // abscissa title
        canvas.drawText(mAbscissaTitle, getWidth() - mPaintTextAxis.measureText(mAbscissaTitle), getTranslatedYPos(0) - dp(5), mPaintTextAxis);


    }

    public void setStart(ModificationStart start) {

        if(start == null){
            mStart = new ModificationStart();
        } else {
            mStart = start;
        }

        numberOfDots = mStart.getDots().size();

        mPathForLine.reset();
        mPaintChart.setAlpha(255);
        mPaintTextNumberAxis.setAlpha(255);
        mPaintTextAxis.setAlpha(255);
        mPaintToday.setAlpha(34);

        invalidate();

    }

    public void setAbscissaTitle(String abscissaTitle) {
        mAbscissaTitle = abscissaTitle;
        invalidate();
    }

    public void setOrdinateTitle(String ordinateTitle) {
        mOrdinateTitle = ordinateTitle;
        invalidate();
    }

    public void setDaysPassed(Float daysPassed) {
        this.daysPassed = daysPassed;
    }

    public void setStartDate(Instant i){
        startDate = i;
    }

    public ModificationStart getStart() {
        return mStart;
    }

    private void init(Context c) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        setOrdinateTitle(c.getString(R.string.metabolic_beginning_details_modification));
        setAbscissaTitle(c.getString(R.string.metabolic_beginning_details_days));

        Resources r = c.getResources();
        density = r.getDisplayMetrics().density;

        mPaintDotBorderSpecific = new Paint();
        mPaintDotBorderSpecific.setAntiAlias(true);
        mPaintDotBorderSpecific.setColor(r.getColor(R.color.modification_start_view_dot_border_specific));

        mPaintDotBorderSpecific.setStrokeWidth(dp(6));
        mPaintDotBorderSpecific.setStyle(Paint.Style.STROKE);

        mPaintDotBorderSpecific.setDither(true);

        mPaintDotBorderGlobal = new Paint();
        mPaintDotBorderGlobal.setAntiAlias(true);
        mPaintDotBorderGlobal.setColor(r.getColor(R.color.modification_start_view_dot_border_global));
        mPaintDotBorderGlobal.setStrokeWidth(dp(6));
        mPaintDotBorderGlobal.setStyle(Paint.Style.STROKE);

        mPaintDotBorderGlobal.setDither(true);

        mPaintDotInner = new Paint();
        mPaintDotInner.setAntiAlias(true);
        mPaintDotInner.setColor(r.getColor(R.color.modification_start_view_dot_inner));
        mPaintDotInner.setStyle(Paint.Style.FILL);
        mPaintDotInner.setDither(true);

        mPaintTextDot = new Paint();
        mPaintTextDot.setAntiAlias(true);
        mPaintTextDot.setColor(r.getColor(R.color.modification_start_view_text_dot));
        mPaintTextDot.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextDot.setDither(true);
        mPaintTextDot.setTextSize(dp(16));

        // for the line that will be draw the curve modification
        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true);
        mPaintLine.setStrokeWidth(dp(4));
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setDither(true);
        mPaintLine.setPathEffect(new CornerPathEffect(dp(4)));
        mPathForLine = new Path();

        mPaintChart = new Paint();
        mPaintChart.setAntiAlias(true);
        mPaintChart.setStrokeWidth(dp(1));
        mPaintChart.setStyle(Paint.Style.STROKE);
        mPaintChart.setDither(true);
        mPathForChart = new Path();

        mPaintStartDate = new Paint();
        mPaintStartDate.setAntiAlias(true);
        mPaintStartDate.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintStartDate.setTextSize(dp(10));
        mPathStartDate = new Path();

        mPaintToday = new Paint();
        mPaintToday.setAntiAlias(true);
        mPaintToday.setStyle(Paint.Style.FILL);
        mPaintToday.setStrokeWidth(dp(1));
        mPaintToday.setDither(true);
        mPaintToday.setPathEffect(new CornerPathEffect(dp(10)));
        mPathToday = new Path();

        // for normal text
        mPaintTextAxis = new Paint();
        mPaintTextAxis.setAntiAlias(true);
        mPaintTextAxis.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextAxis.setTextSize(dp(13));
        mPathText = new Path();

        mPaintTextNumberAxis = new Paint();
        mPaintTextNumberAxis.setAntiAlias(true);
        mPaintTextNumberAxis.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextNumberAxis.setColor(r.getColor(R.color.modification_start_view_text_number_axis));
        mPaintTextNumberAxis.setTextSize(dp(14));

        final View v = this;

        black();

    }


    private float getTranslatedYPos(float yValue){
        // sacamos la altura usable
        float height = getHeight() - (dp(SP_PADDING_TOP) + dp(SP_PADDING_BOTTOM));

        // sacamos qué es la mitad del gráfico. Esto será el 0 para las y's que pasemos.
        float middle = height / (float) 2.0;

        // sacamos qué se correspondería con una unidad en el gráfico
        float unit = middle / MAX_MODIFICATION;

        // devolvemos la mitad menos, la unidad por yValue y le sumamos el padding de arriba.
        return (middle - (unit * yValue)) + dp(SP_PADDING_TOP);


    }
    private float getTranslatedXPos(float xValue){
        // sacamos el ancho
        float width = getWidth() - (dp(SP_PADDING_LEFT) + dp(SP_PADDING_RIGHT));

        // sacamos la unidad
        float unit = width / (MAX_DAYS_FORWARD + 1); // hay que contar con el día -1

        // retornamos el xValue pasado, por la unidad, más el padding izquierdo.
        return ((xValue * unit) + (dp(SP_PADDING_LEFT))) + unit;

    }

    private float dp(float p){
        // dp to pixels
        return p * density;
    }

    public void setMetabolicStateStartType(int metabolicStateStartType) {
        this.metabolicStateStartType = metabolicStateStartType;
    }

    public void setMetabolicRhythmStartType(int metabolicRhythmStartType) {
        this.metabolicRhythmStartType = metabolicRhythmStartType;
    }

    public void black(){
        mPaintChart.setColor(getContext().getResources().getColor(R.color.modification_start_view_chart));
        mPaintTextAxis.setColor(getContext().getResources().getColor(R.color.modification_start_view_text));
        mPaintToday.setColor(getContext().getResources().getColor(R.color.modification_start_view_today));
        mPaintStartDate.setColor(getContext().getResources().getColor(R.color.modification_start_view_start_day));
        mPaintLine.setColor(getContext().getResources().getColor(R.color.modification_start_view_line));
        invalidate();
    }

    public void white(){
        mPaintChart.setColor(getContext().getResources().getColor(R.color.modification_start_view_chart_white));
        mPaintTextAxis.setColor(getContext().getResources().getColor(R.color.modification_start_view_text_white));
        mPaintToday.setColor(getContext().getResources().getColor(R.color.modification_start_view_today_white));
        mPaintStartDate.setColor(getContext().getResources().getColor(R.color.modification_start_view_day_white));
        mPaintLine.setColor(getContext().getResources().getColor(R.color.modification_start_view_line_white));
        invalidate();
    }
}
