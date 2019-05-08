package org.cafydia4.android.chartobjects;

import android.content.Context;

import org.cafydia4.android.R;
import org.cafydia4.android.core.Annotation;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.util.C;

/**
 * Created by user on 21/02/15.
 */
public class DataCollectionCriteriaInstant {
    private int mType;
    private long mData;

    private Context mContext;
    private Instant mInstant;

    private static DataDatabase db;

    public DataCollectionCriteriaInstant(Context c, DataDatabase d){
        this(c, d, C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE, 0l);
    }

    public DataCollectionCriteriaInstant(Context context, DataDatabase d, int type, long data){
        mContext = context;

        mType = type;
        mData = data;

        db = d;
    }

    /**
     * @return Devuelve un Instant en caso de tipo relativo o absoluto, o un Annotation en caso de ser del tipo annotation.
     */
    public Instant getInstant(){
        switch (mType) {
            case C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE:
                if(mInstant == null) {
                    mInstant = new Instant((int) mData);
                }
                break;
            case C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ABSOLUTE:
                if(mInstant == null) {
                    mInstant = new Instant(mData);
                }
                break;
            case C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION:
                if(mInstant == null) {
                    mInstant = db.getAnnotationById((int) mData);
                }
                break;

        }
        return mInstant;
    }

    public Integer getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public Long getData() {
        return mData;
    }

    public void setData(long data) {
        this.mData = data;
    }

    @Override
    public String toString() {
        String ret = "";

        switch (mType) {
            case C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE:
                ret = Long.toString(mData < 0 ? -mData : mData) + " " + mContext.getString(R.string.data_collection_criteria_instant_days_ago);
                break;
            case C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ABSOLUTE:
                ret = getInstant().getUserDateString();
                break;

            case C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION:
                Annotation a = (Annotation) getInstant();
                ret = a.getUserDateStringShort() + " " + a.getAnnotation();
                break;


        }

        return ret;
    }

}
