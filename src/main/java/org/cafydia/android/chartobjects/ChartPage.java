package org.cafydia.android.chartobjects;

import org.cafydia.android.datadatabase.DataDatabase;

/**
 * Created by user on 3/12/14.
 */
public class ChartPage {
    // member vars
    private Integer mId;
    private Integer mScopeOrderNumber;
    private String mTitle;

    private DataCollectionCriteria mCriteria;

    // constructor
    public ChartPage(int mId, int mOrderNumber, String mTitle, DataCollectionCriteria criteria){
        this.mId = mId;
        this.mScopeOrderNumber = mOrderNumber;
        this.mTitle = mTitle;
        mCriteria = criteria;
    }

    public ChartPage(int mOrderNumber, String mTitle, DataCollectionCriteria criteria){
        this(0, mOrderNumber, mTitle, criteria);
    }

    ///////////////////////

    // Getters and setters

    ///////////////////////

    public Integer getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public Integer getPagerScopePosition() {
        return mScopeOrderNumber;
    }

    public void setScopeOrderNumber(int scopeOrderNumber) {
        this.mScopeOrderNumber = scopeOrderNumber;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public DataCollectionCriteria getCriteria() {
        return mCriteria;
    }

    public void setCriteria(DataCollectionCriteria mCriteria) {
        this.mCriteria = mCriteria;
    }

    ////////////////////////////////////
    ////////////////////////////////////
    ////////////////////////////////////



    ////////////////////////////////////

    // To save and delete in database

    ////////////////////////////////////

    public void save(DataDatabase db){
        if(getId() == 0){
            db.insertChartPage(this);
        } else {
            db.updateChartPage(this);
        }

    }
    public void delete(DataDatabase db){
        db.deleteChartPage(this);
    }
    ////////////////////////////////////
    ////////////////////////////////////
    ////////////////////////////////////
}
