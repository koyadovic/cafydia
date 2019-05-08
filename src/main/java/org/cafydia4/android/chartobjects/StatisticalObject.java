package org.cafydia4.android.chartobjects;

import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.util.C;

/**
 * Created by user on 4/02/15.
 */
public class StatisticalObject {
    private Integer mId;
    private Integer mChartPageElementId;

    private boolean mMinimum;
    private boolean mMaximum;
    private boolean mMean;
    private boolean mMedian;
    private boolean mLinearRegression;
    private boolean mPolynomialRegressionGrade2;
    private boolean mPolynomialRegressionGrade3;
    private boolean mGlucoseTests;
    private boolean mGrid;

    // ten bits
    // grid, glucose tests, minimum, maximum, mean, median, linear regression, polynomial regression, variance
    // 1      1             1        1        1     1           1                1                      1

    // 0b1100000000 es glucosas y el grid, lo que se muestra por defecto
    public StatisticalObject(ChartPageElement e){
        this(0, e.getId(), C.STATISTICAL_GRID | C.STATISTICAL_GLUCOSE_TESTS);
    }

    public StatisticalObject(int id, int chartPageElementId, int configuration){
        mId = id;
        mChartPageElementId = chartPageElementId;

        mPolynomialRegressionGrade3 = (configuration & 0b1) == 1;
        configuration >>= 1;

        mPolynomialRegressionGrade2 = (configuration & 0b1) == 1;
        configuration >>= 1;

        mLinearRegression = (configuration & 0b1) == 1;
        configuration >>= 1;

        mMedian = (configuration & 0b1) == 1;
        configuration >>= 1;

        mMean = (configuration & 0b1) == 1;
        configuration >>= 1;

        mMaximum = (configuration & 0b1) == 1;
        configuration >>= 1;

        mMinimum = (configuration & 0b1) == 1;
        configuration >>= 1;

        mGlucoseTests = (configuration & 0b1) == 1;
        configuration >>= 1;

        mGrid = (configuration & 0b1) == 1;
    }

    public Integer getConfigurationInteger(){
        int con = 0;

        if(isGridActivated()){
            con |= 0b1;
        }
        con <<= 1;

        if(isGlucoseTestsActivated()){
            con |= 0b1;
        }
        con <<= 1;

        if(isMinimumActivated()) {
            con |= 0b1;
        }
        con <<= 1;

        if(isMaximumActivated()) {
            con |= 0b1;
        }
        con <<= 1;

        if(isMeanActivated()) {
            con |= 0b1;
        }
        con <<= 1;

        if(isMedianActivated()) {
            con |= 0b1;
        }
        con <<= 1;

        if(isLinearRegressionActivated()) {
            con |= 0b1;
        }
        con <<= 1;

        if(isPolynomialRegressionGrade2Activated()) {
            con |= 0b1;
        }
        con <<= 1;

        if(isPolynomialRegressionGrade3Activated()) {
            con |= 0b1;
        }


        return con;
    }


    public Integer getId() {
        return mId;
    }

    public void setId(Integer mId) {
        this.mId = mId;
    }

    public Integer getChartPageElementId() {
        return mChartPageElementId;
    }

    public void setChartPageElementId(Integer mChartPageElementId) {
        this.mChartPageElementId = mChartPageElementId;
    }

    public Boolean isMinimumActivated() {
        return mMinimum;
    }

    public void setMinimumActivated(Boolean mMinimum) {
        this.mMinimum = mMinimum;
    }

    public Boolean isMaximumActivated() {
        return mMaximum;
    }

    public void setMaximumActivated(Boolean mMaximum) {
        this.mMaximum = mMaximum;
    }

    public Boolean isMeanActivated() {
        return mMean;
    }

    public void setMeanActivated(Boolean mMean) {
        this.mMean = mMean;
    }

    public Boolean isMedianActivated() {
        return mMedian;
    }

    public void setMedianActivated(Boolean mMedian) {
        this.mMedian = mMedian;
    }

    public Boolean isLinearRegressionActivated() {
        return mLinearRegression;
    }

    public void setLinearRegressionActivated(Boolean mLinearRegression) {
        this.mLinearRegression = mLinearRegression;
    }

    public Boolean isPolynomialRegressionGrade2Activated() {
        return mPolynomialRegressionGrade2;
    }

    public void setPolynomialRegressionGrade2Activated(Boolean mPolynomialRegression) {
        this.mPolynomialRegressionGrade2 = mPolynomialRegression;
    }

    public Boolean isPolynomialRegressionGrade3Activated() {
        return mPolynomialRegressionGrade3;
    }

    public void setPolynomialRegressionGrade3Activated(Boolean mVariance) {
        this.mPolynomialRegressionGrade3 = mVariance;
    }

    public Boolean isGlucoseTestsActivated() {
        return mGlucoseTests;
    }

    public void setGlucoseTestsActivated(boolean glucoseTests) {
        this.mGlucoseTests = glucoseTests;
    }

    public boolean isGridActivated() {
        return mGrid;
    }

    public void setGridActivated(boolean grid) {
        this.mGrid = grid;
    }

/*
     To save and delete from database
     */

    public void save(DataDatabase db) {
        switch (getId()){
            case 0:
                db.insertStatisticalObject(this);
                break;
            default:
                db.updateStatisticalObject(this);
        }
    }

    public void delete(DataDatabase db){
        db.deleteStatisticalObject(this);
    }


}
