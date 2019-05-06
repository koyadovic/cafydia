package org.cafydia.android.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

import org.cafydia.android.R;
import org.cafydia.android.core.Meal;
import org.cafydia.android.util.MyRound;
import org.cafydia.android.util.UnitChanger;

import java.util.ArrayList;

/**
 * Created by user on 22/09/14.
 */
public class BaselineChartView extends View {
    private static final String KEY_PREFERENCES = "pref_key_advanced_function_parameters";
    //private static final float DP_PADDING = 20;
    private float DP_PADDING_TOP = 2;
    private float DP_PADDING_BOTTOM = 20;
    private float DP_PADDING_RIGHT = 2;
    private float DP_PADDING_LEFT = 2;

    private UnitChanger changer;

    private float xLow = 20, xHigh = 120;
    private float yLow = 0, yHigh = 20;

    private float density = 1.0f;
    //private float padding;

    private Float mainLineM = 0f, mainLineB = 0f;
    private Float secondaryLineM = 0f, secondaryLineB = 0f;

    // for the draw
    private Paint mPaintChart;

    private Paint mPaintTextAxis;
    private Paint mPaintTextNumberAxis;

    private Paint mPaintMainLine;
    private Paint mPaintSecondaryLine;
    private Paint mPaintAverage;
    private Paint mPaintAverageText;
    private Paint mPaintMealToDraw;
    private Paint mPaintMealToDrawText;
    private Paint mPaintChartBackground;
    private Paint mPaintTextLegend;
    private Paint mPaintTextBackgroundLegend;
    private Paint mPaintLegendLineColor;
    private Paint mPaintSeparators;

    private Path mPathChart;
    private Path mPathMainLine;
    private Path mPathSecondaryLine;
    private Path mPathText;
    private Path mPathAverage;
    private Path mPathMealToDraw;
    private Path mPathTextBackgroundLegend;
    private Path mPathLegendLineColor;
    private Path mPathSeparators;

    private String labelX, labelY;

    private float averageCarbohydrates = 0.0f;
    private String averageString;
    private Meal mealToDraw;

    private boolean showFunctionParameters;

    public BaselineChartView(Context c, AttributeSet a){
        super(c, a);
        init(c);
    }

    @Override
    protected void onDraw(Canvas canvas){
        lefts.clear();
        ups.clear();
        rights.clear();
        downs.clear();

        float measured;

        // background
        canvas.drawRect(
                getTranslatedXPos(xLow),
                getTranslatedYPos(yHigh),
                getTranslatedXPos(xHigh),
                getTranslatedYPos(yLow) + dp(1),
                mPaintChartBackground
        );


        // chart
        if(mPathChart.isEmpty()) {
            mPathChart.moveTo(getTranslatedXPos(xLow), getTranslatedYPos(yLow));
            mPathChart.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(yLow));
        }
        canvas.drawPath(mPathChart, mPaintChart);
        //canvas.drawLine(getTranslatedXPos(xLow) + dp(2), getTranslatedYPos(yHigh), getTranslatedXPos(xHigh), getTranslatedYPos(yHigh), mPaintSeparators);
        //canvas.drawLine(getTranslatedXPos(xHigh), getTranslatedYPos(yHigh), getTranslatedXPos(xHigh), getTranslatedYPos(yLow) - dp(2), mPaintSeparators);

        // chart labels
        measured = mPaintTextAxis.measureText(labelX);
        canvas.drawText(labelX, getTranslatedXPos(xHigh) - measured - dp(2), getTranslatedYPos(yLow) - dp(4), mPaintTextAxis);

        measured = mPaintTextAxis.measureText(labelY);
        if(mPathText.isEmpty()){
            mPathText.moveTo(getTranslatedXPos(xLow), (getTranslatedYPos(yLow) / 2.0f) + (measured / 2.0f) + dp(10));
            mPathText.lineTo(getTranslatedXPos(xLow), (getTranslatedYPos(yLow) / 2.0f) - (measured / 2.0f) + dp(10));
        }
        //canvas.drawTextOnPath(labelY, mPathText, 0, -dp(4), mPaintTextAxis);
        canvas.drawText(labelY, getTranslatedXPos(xLow), getTranslatedYPos(yHigh) + dp(10), mPaintTextAxis);

        if(! isInEditMode()) {
            // numbers
            // X's
            String s;
            float increment = (xHigh - xLow) / 5f;
            for(float x = xLow + (increment / 2f); x < xHigh; x += increment){
                s = MyRound.roundToInteger(changer.toUIFromInternalWeight(x)).toString() + changer.getStringUnitForWeightShort();
                measured = mPaintTextNumberAxis.measureText(s);
                canvas.drawText(s, getTranslatedXPos(x) - (measured / 2f), getTranslatedYPos(yLow) + dp(14), mPaintTextNumberAxis);
            }

            mPathSeparators.reset();
            // Y's
            increment = (yHigh - yLow) / 5f;
            for(float y = yLow + increment; y < yHigh - (increment / 2f); y += increment){
                canvas.drawText(MyRound.round(y, 1).toString(), getTranslatedXPos(xLow), getTranslatedYPos(y) - dp(4), mPaintTextNumberAxis);
                mPathSeparators.moveTo(getTranslatedXPos(xLow), getTranslatedYPos(y));
                mPathSeparators.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(y));
            }
            canvas.drawPath(mPathSeparators, mPaintSeparators);

            // main line
            if (mainLineM != null && mainLineM > 0) {
                if (mPathMainLine.isEmpty()) {
                    float y1 = (xLow * mainLineM) + mainLineB;
                    float y2 = (xHigh  * mainLineM) + mainLineB;

                    mPathMainLine.moveTo(getTranslatedXPos(xLow), getTranslatedYPos(y1));
                    mPathMainLine.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(y2));
                }
                canvas.drawPath(mPathMainLine, mPaintMainLine);

                if(showFunctionParameters) {
                    String t = "f(x) = " + MyRound.round(mainLineM, 4).toString() + "x" + (mainLineB < 0 ? " - " : " + ") + MyRound.round(mainLineB < 0 ? -mainLineB : mainLineB, 4).toString();
                    drawText(canvas, t, getTranslatedXPos(xHigh), getTranslatedYPos(yHigh), mPaintMainLine.getColor());
                }
            }

            // secondary line
            if (secondaryLineM != null && secondaryLineM > 0) {
                if (mPathSecondaryLine.isEmpty()) {

                    // todo habrá que ver si no la cagamos aquí
                    float y1 = (xLow * secondaryLineM) + secondaryLineB;
                    float y2 = (xHigh  * secondaryLineM) + secondaryLineB;

                    mPathSecondaryLine.moveTo(getTranslatedXPos(xLow),  getTranslatedYPos(y1));
                    mPathSecondaryLine.lineTo(getTranslatedXPos(xHigh), getTranslatedYPos(y2));
                }
                canvas.drawPath(mPathSecondaryLine, mPaintSecondaryLine);

                if(showFunctionParameters) {
                    String t = "f(x) = " + MyRound.round(secondaryLineM, 4).toString() + "x" + (secondaryLineB < 0 ? " - " : " + ") + MyRound.round(secondaryLineB < 0 ? -secondaryLineB : secondaryLineB, 4).toString();
                    drawText(canvas, t, getTranslatedXPos(xHigh), getTranslatedYPos(yHigh), mPaintSecondaryLine.getColor());
                }
            }


            // average
            if (averageCarbohydrates != 0.0f) {
                if (mPathAverage.isEmpty()) {
                    mPathAverage.moveTo(getTranslatedXPos(averageCarbohydrates), getTranslatedYPos(yHigh));
                    mPathAverage.lineTo(getTranslatedXPos(averageCarbohydrates), getTranslatedYPos(yLow));
                }
                canvas.drawPath(mPathAverage, mPaintAverage);

                s = averageString + ": " + MyRound.round(changer.toUIFromInternalWeight(averageCarbohydrates)).toString() + changer.getStringUnitForWeightShort();
                drawText(canvas, s, getTranslatedXPos(xHigh), getTranslatedYPos(yHigh), mPaintAverage.getColor());

            }


            // meal to draw
            if (mealToDraw != null) {
                if (mealToDraw.getBaselinePreprandial() != null && mealToDraw.getBaselinePreprandial() > 0.0f) {
                    canvas.drawCircle(getTranslatedXPos(mealToDraw.getMealCarbohydrates()), getTranslatedYPos(mealToDraw.getBaselinePreprandial()), dp(2), mPaintMealToDraw);
                }

                Integer car = MyRound.roundToInteger(changer.toUIFromInternalWeight(mealToDraw.getMealCarbohydrates()));
                s = "Carb: " + car.toString() + changer.getStringUnitForWeightShort();
                measured = mPaintMealToDrawText.measureText(s);

                canvas.drawRect(getTranslatedXPos(xHigh) - measured - dp(14), getTranslatedYPos(yLow) - dp(50), getTranslatedXPos(xHigh) - dp(10), getTranslatedYPos(yLow) - dp(20), mPaintMealToDraw);

                canvas.drawText(s, getTranslatedXPos(xHigh) - measured - dp(12), getTranslatedYPos(yLow) - dp(37), mPaintMealToDrawText);

                s = "Pre: " + MyRound.round(mealToDraw.getBaselinePreprandial()).toString();
                canvas.drawText(s, getTranslatedXPos(xHigh) - measured - dp(12), getTranslatedYPos(yLow) - dp(24), mPaintMealToDrawText);

                if (mealToDraw.getBaselinePreprandial() != null && mealToDraw.getBaselinePreprandial() > 0.0f) {
                    if (mealToDraw.getMealCarbohydrates() > 0.0f) {
                        if (getTranslatedXPos(mealToDraw.getMealCarbohydrates()) >= getTranslatedXPos(xHigh) - measured - dp(40)) {
                            mPathMealToDraw.moveTo(getTranslatedXPos(mealToDraw.getMealCarbohydrates()), getTranslatedYPos(mealToDraw.getBaselinePreprandial()));
                            mPathMealToDraw.lineTo(getTranslatedXPos(xHigh) - (measured / 2.0f) - dp(15), getTranslatedYPos(yLow) - dp(54));
                            mPathMealToDraw.lineTo(getTranslatedXPos(xHigh) - (measured / 2.0f) - dp(5), getTranslatedYPos(yLow) - dp(54));
                        } else {
                            mPathMealToDraw.moveTo(getTranslatedXPos(mealToDraw.getMealCarbohydrates()), getTranslatedYPos(mealToDraw.getBaselinePreprandial()));
                            mPathMealToDraw.lineTo(getTranslatedXPos(xHigh) - measured - dp(18), getTranslatedYPos(yLow) - dp(45));
                            mPathMealToDraw.lineTo(getTranslatedXPos(xHigh) - measured - dp(18), getTranslatedYPos(yLow) - dp(35));
                        }
                    }
                    canvas.drawPath(mPathMealToDraw, mPaintMealToDraw);
                }
            }

        }
    }

    private int getColor(int colorId){
        return getContext().getResources().getColor(colorId);
    }

    private void init(Context c){
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // get pixel density
        density = c.getResources().getDisplayMetrics().density;

        // set default padding
        //padding = dp(DP_PADDING);


        // paint for the chart
        mPaintChart = new Paint();
        mPaintChart.setAntiAlias(true);
        mPaintChart.setColor(getColor(R.color.baseline_chart_chart)); // default alpha FF 0xFF000000
        mPaintChart.setStrokeWidth(dp(1));
        mPaintChart.setStyle(Paint.Style.STROKE);
        mPaintChart.setDither(true);
        mPathChart = new Path();

        mPaintChartBackground = new Paint();
        mPaintChartBackground.setAntiAlias(true);
        mPaintChartBackground.setColor(getColor(R.color.baseline_chart_background));
        mPaintChartBackground.setStyle(Paint.Style.FILL);
        mPaintChartBackground.setDither(true);

        mPaintSeparators = new Paint();
        mPaintSeparators.setAntiAlias(true);
        mPaintSeparators.setColor(getColor(R.color.baseline_chart_separators));
        mPaintSeparators.setStyle(Paint.Style.STROKE);
        mPaintSeparators.setDither(true);
        mPathSeparators = new Path();


        // paint for the main line
        mPaintMainLine = new Paint();
        mPaintMainLine.setAntiAlias(true);
        mPaintMainLine.setColor(getColor(R.color.baseline_chart_main_line)); // default alpha AA 0xAA007799
        mPaintMainLine.setStrokeWidth(dp(4));
        mPaintMainLine.setStyle(Paint.Style.STROKE);
        mPaintMainLine.setDither(true);
        //mPaintMainLine.setShadowLayer(dp(4), dp(3), dp(3), 0x99000000);

        mPathMainLine = new Path();


        // paint for the secondary line
        mPaintSecondaryLine = new Paint();
        mPaintSecondaryLine.setAntiAlias(true);
        mPaintSecondaryLine.setColor(getColor(R.color.baseline_chart_secondary_line)); // default alpha AA 0x77777777
        mPaintSecondaryLine.setStrokeWidth(dp(4));
        mPaintSecondaryLine.setStyle(Paint.Style.STROKE);
        mPaintSecondaryLine.setDither(true);
        mPaintSecondaryLine.setPathEffect(new DashPathEffect(new float[] {dp(10), dp(3)}, 0));
        mPathSecondaryLine = new Path();

        mPaintAverage = new Paint();
        mPaintAverage.setAntiAlias(true);
        mPaintAverage.setColor(getColor(R.color.baseline_chart_average)); // 0xFFBBBBBB
        mPaintAverage.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintAverage.setStrokeWidth(dp(20));
        mPaintAverage.setDither(true);
        mPathAverage = new Path();

        mPaintAverageText = new Paint();
        mPaintAverageText.setAntiAlias(true);
        mPaintAverageText.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintAverageText.setColor(getColor(R.color.baseline_chart_average_text));
        mPaintAverageText.setTextSize(dp(13));

        // for normal text
        mPaintTextAxis = new Paint();
        mPaintTextAxis.setAntiAlias(true);
        mPaintTextAxis.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextAxis.setColor(getColor(R.color.baseline_chart_text_axis));
        mPaintTextAxis.setTextSize(dp(13));
        mPathText = new Path();

        mPaintTextNumberAxis = new Paint();
        mPaintTextNumberAxis.setAntiAlias(true);
        mPaintTextNumberAxis.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextNumberAxis.setColor(getColor(R.color.baseline_chart_text_number_axis));
        mPaintTextNumberAxis.setTextSize(dp(13));


        mPaintMealToDraw = new Paint();
        mPaintMealToDraw.setAntiAlias(true);
        mPaintMealToDraw.setColor(getColor(R.color.baseline_chart_meal_to_draw)); // 0x99003344
        mPaintMealToDraw.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintMealToDraw.setShadowLayer(dp(2), dp(3), dp(3), 0x30000000);
        mPaintMealToDraw.setDither(true);
        mPaintMealToDraw.setStrokeWidth(dp(2));
        mPaintMealToDraw.setPathEffect(new CornerPathEffect(dp(4)));
        mPathMealToDraw = new Path();



        mPaintMealToDrawText = new Paint();
        mPaintMealToDrawText.setAntiAlias(true);
        mPaintMealToDrawText.setColor(getColor(R.color.baseline_chart_meal_to_draw_text)); // 0xFFFFFFFF
        mPaintMealToDrawText.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintMealToDrawText.setDither(true);
        mPaintMealToDrawText.setTextSize(dp(13));



        mPaintTextLegend = new Paint();
        mPaintTextLegend.setAntiAlias(true);
        mPaintTextLegend.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextLegend.setColor(getColor(R.color.baseline_chart_text_legend));
        mPaintTextLegend.setTextSize(dp(13));

        mPaintTextBackgroundLegend = new Paint();
        mPaintTextBackgroundLegend.setAntiAlias(true);
        mPaintTextBackgroundLegend.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextBackgroundLegend.setColor(getColor(R.color.baseline_chart_text_background_legend));
        mPaintTextBackgroundLegend.setStrokeWidth(dp(6));
        mPaintTextBackgroundLegend.setPathEffect(new CornerPathEffect(dp(4)));
        mPathTextBackgroundLegend = new Path();

        mPaintLegendLineColor = new Paint();
        mPaintLegendLineColor.setAntiAlias(true);
        mPaintLegendLineColor.setStyle(Paint.Style.FILL);
        mPaintLegendLineColor.setStrokeWidth(dp(2));
        mPaintLegendLineColor.setPathEffect(new CornerPathEffect(dp(2)));
        mPathLegendLineColor = new Path();


        labelX = getContext().getString(R.string.linear_function_view_x_label);
        labelY = getContext().getString(R.string.linear_function_view_y_label);
        averageString = getContext().getString(R.string.linear_function_view_average);

        if(! isInEditMode()) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            showFunctionParameters = sp.getBoolean(KEY_PREFERENCES, false);
            changer = new UnitChanger(getContext());
        } else {
            showFunctionParameters = false;
        }
    }

    public void setMainLineParameters(Float m, Float b){
        mainLineM = m;
        mainLineB = b;

    }
    public void setSecondaryLineParameters(Float m, Float b){
        secondaryLineM = m;
        secondaryLineB = b;

    }

    public void setAverageCarbohydrates(Float averageCarbohydrates){
        if(averageCarbohydrates == null) {
            this.averageCarbohydrates = 0.0f;
        } else {
            this.averageCarbohydrates = averageCarbohydrates;
        }
    }

    public void setMealToDraw(Meal mealToDraw) {
        this.mealToDraw = mealToDraw;
    }

    private void readjustChartSize(){

        // x adjust
        if(averageCarbohydrates != 0.0f) {
            if (mealToDraw != null && (averageCarbohydrates * 2) - 20 < mealToDraw.getMealCarbohydrates() + 20) {
                xHigh = mealToDraw.getMealCarbohydrates() + 20;
            } else {
                xHigh = (averageCarbohydrates * 2) - 20;
            }
        } else if(mealToDraw != null && mealToDraw.getMealCarbohydrates() > xHigh - 20){
            xHigh = mealToDraw.getMealCarbohydrates() + 20;
        }

        if(mealToDraw != null){
            if (mealToDraw.getMealCarbohydrates() > 0.0f && mealToDraw.getMealCarbohydrates() <= xLow){
                xLow = 0;
            }
            else if(mealToDraw.getMealCarbohydrates() > 20.0f){
                xLow = 20;
            }
        }
        if(xHigh - xLow < 20) {
            if(xLow > 10) {
                xLow -= 10;
                xHigh += 10;
            } else {
                xHigh += 20;
            }
        }

        // y adjust
        if(mainLineM != null && mainLineB != null && mainLineM > 0.0f) {
            // readjust the yHigh
            float y = (xHigh * mainLineM) + mainLineB;

            if(y > yHigh){
                yHigh = y;

            } else {
                if(secondaryLineM == null || secondaryLineB == null){
                    yHigh = y;

                } else {
                    float y2 = (xHigh * secondaryLineM) + secondaryLineB;
                    yHigh = y > y2 ? y : y2;
                }
            }
        } else {
            if(mealToDraw != null && mealToDraw.getBaselinePreprandial() != null && mealToDraw.getBaselinePreprandial() > 20) {
                yHigh = mealToDraw.getBaselinePreprandial();
            } else {
                yHigh = 20;
            }
        }


        mPathChart.reset();
        mPathSecondaryLine.reset();
        mPathMainLine.reset();

        mPathAverage.reset();
        mPathMainLine.reset();
        mPathSecondaryLine.reset();
        mPathMealToDraw.reset();
    }

    //////////////////////////////////////////////////////////////////////////////
    /////////////////////////// DRAW TEXT RECTANGLES /////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    private ArrayList<Float> lefts = new ArrayList<>();
    private ArrayList<Float> ups = new ArrayList<>();
    private ArrayList<Float> rights = new ArrayList<>();
    private ArrayList<Float> downs = new ArrayList<>();


    private boolean isIn(float x, float y, float l, float u, float r, float d) {
        return x >= l - dp(10) && x <= r + dp(10) && y >= u - dp(7) && y <= d + dp(7);
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

            float measure = mPaintTextLegend.measureText(s);
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

            canvas.drawRect(leftX, upY, rightX, downY, mPaintTextBackgroundLegend);

            mPathTextBackgroundLegend.reset();
            mPathLegendLineColor.reset();

            mPaintLegendLineColor.setColor(color);
            mPaintLegendLineColor.setAlpha(255);

            canvas.drawRect(leftX - dp(1), upY, leftX + dp(4), downY, mPaintLegendLineColor);

            canvas.drawText(s, leftX + dp(5), downY - dp(3), mPaintTextLegend);
        }
    }


    public void refresh(){
        readjustChartSize();
        invalidate();
    }

    private float getTranslatedXPos(float x){
        float width = getWidth() - (dp(DP_PADDING_LEFT) + dp(DP_PADDING_RIGHT));
        float unit = width / (xHigh - xLow);
        return ((x - xLow) * unit) + (dp(DP_PADDING_LEFT));
    }

    private float getTranslatedYPos(float y){
        float height = getHeight() - (dp(DP_PADDING_BOTTOM) + dp(DP_PADDING_TOP));
        float unit = height / (yHigh - yLow);
        return (height + dp(DP_PADDING_TOP)) - (unit * (y - yLow));
    }

    private float dp(float p){
        // dp to pixels
        return (p * density) + 0.5f;
    }

    public void black(){
        mPaintChart.setColor(getColor(R.color.baseline_chart_chart));
        mPaintTextAxis.setColor(getColor(R.color.baseline_chart_text_axis));
        mPaintMainLine.setColor(getColor(R.color.baseline_chart_main_line));
        mPaintSecondaryLine.setColor(getColor(R.color.baseline_chart_secondary_line));
        mPaintMealToDraw.setColor(getColor(R.color.baseline_chart_meal_to_draw));
        mPaintTextNumberAxis.setColor(getColor(R.color.baseline_chart_text_number_axis));
        mPaintSeparators.setColor(getColor(R.color.baseline_chart_separators));
        invalidate();
    }

    public void white(){
        mPaintChart.setColor(getContext().getResources().getColor(R.color.baseline_chart_chart_white));
        mPaintTextAxis.setColor(getContext().getResources().getColor(R.color.baseline_chart_text_axis_white));
        mPaintMainLine.setColor(getColor(R.color.baseline_chart_main_line_white));
        mPaintSecondaryLine.setColor(getColor(R.color.baseline_chart_secondary_line_white));
        mPaintMealToDraw.setColor(getColor(R.color.baseline_chart_meal_to_draw_white));
        mPaintTextNumberAxis.setColor(getColor(R.color.baseline_chart_text_number_axis_white));
        mPaintSeparators.setColor(getColor(R.color.baseline_chart_separators_white));
        invalidate();
    }
}
