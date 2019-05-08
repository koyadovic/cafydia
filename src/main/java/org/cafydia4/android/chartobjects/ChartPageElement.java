package org.cafydia4.android.chartobjects;

import org.cafydia4.android.datadatabase.DataDatabase;

/**
 * Created by user on 3/12/14.
 */
public class ChartPageElement {
    // Member vars
    private int mId;
    private int mType;
    private int mChartPageId;
    private String mTextHeader = "";

    private DataCollectionCriteria mCriteria = null;
    private StatisticalObject mStatisticalObject = null;

    // Constructor
    public ChartPageElement(int id, int type, int mChartPageId, String textHeader){
        this.mId = id;
        this.mType = type;
        this.mChartPageId = mChartPageId;
        this.mTextHeader = textHeader;
    }

    /////////////////////////////

    // Getters and setters

    /////////////////////////////

    public Integer getId() {
        return mId;
    }

    public Integer getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public Integer getChartPageId() {
        return mChartPageId;
    }

    public void setChartPageId(int chartPageId) {
        this.mChartPageId = chartPageId;
    }

    public String getTextHeader() {
        return mTextHeader;
    }

    public void setTextHeader(String textHeader) {
        this.mTextHeader = textHeader;
    }

    public DataCollectionCriteria getCriteria() {
        return mCriteria;
    }

    public void setCriteria(DataCollectionCriteria criteria) {
        this.mCriteria = criteria;
    }

    public StatisticalObject getStatisticalObject() {
        return mStatisticalObject;
    }

    public void setStatisticalObject(StatisticalObject statisticalObject) {
        this.mStatisticalObject = statisticalObject;
    }

    ////////////////////////////////////

    // To save and delete in database

    ////////////////////////////////////

    public void save(DataDatabase db){
        if(getId() == 0) {
            db.insertChartPageElement(this);
        } else {
            db.updateChartPageElement(this);
        }
    }
    public void delete(DataDatabase db){
        db.deleteChartPageElement(this);
    }
    //////////////////////////////////////
    //////////////////////////////////////
    //////////////////////////////////////
}
