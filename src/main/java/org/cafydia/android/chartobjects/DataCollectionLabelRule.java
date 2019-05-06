package org.cafydia.android.chartobjects;

import org.cafydia.android.datadatabase.DataDatabase;

/**
 * Created by user on 29/01/15.
 */
public class DataCollectionLabelRule {
    private int mId;
    private int mCriteriaId;

    private int mAction;
    private Label mLabel;

    public DataCollectionLabelRule(int criteriaId, int action, Label label){
        this(0, criteriaId, action, label);
    }

    public DataCollectionLabelRule(int id, int criteriaId, int action, Label label){
        mId = id;
        mCriteriaId = criteriaId;
        mAction = action;
        mLabel = label;
    }

    public Integer getId() {
        return mId;
    }

    public Integer getCriteriaId() {
        return mCriteriaId;
    }

    public void setCriteriaId(int criteriaId) {
        this.mCriteriaId = criteriaId;
    }

    public Integer getAction() {
        return mAction;
    }

    public void setAction(int action) {
        this.mAction = action;
    }

    public Label getLabel() {
        return mLabel;
    }


    // to save and delete from database

    public void save(DataDatabase db){
        if(getId() == 0) {
            // es nuevo
            db.insertDataCollectionLabelRule(this);

        } else {
            //no lo es
            db.updateDataCollectionLabelRule(this);
        }
    }

    public void delete(DataDatabase db){
        db.deleteDataCollectionLabelRule(this);
    }

}
