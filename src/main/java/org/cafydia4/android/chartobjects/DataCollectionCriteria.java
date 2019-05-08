package org.cafydia4.android.chartobjects;


import android.content.Context;

import org.cafydia4.android.core.Instant;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.util.C;

import java.util.ArrayList;

/**
 * Created by user on 28/01/15.
 */
public class DataCollectionCriteria {
    private int mId;

    private DataCollectionCriteriaInstant mSince, mUntil;

    // 1111111 all days, 1000000 monday, 0000001 sunday.
    private int mDayWeeksActivated = C.DATA_COLLECTION_CRITERIA_NO_ACTIVATED;
    private int mDayWeeks = 0;

    // 1111111 all meals before and after, 1000000 before breakfast, 0100000 after breakfast
    // 0000100 before dinner, 0000010 after dinner, 0000001 in the night.
    private int mMealTimesActivated = C.DATA_COLLECTION_CRITERIA_NO_ACTIVATED;
    private int mMealTimes = 0;

    private ArrayList<DataCollectionLabelRule> mDataCollectionLabelRules;

    public DataCollectionCriteria(Context context){
        DataDatabase db = new DataDatabase(context);
        mId = 0;

        mSince = new DataCollectionCriteriaInstant(context, db);
        mUntil = new DataCollectionCriteriaInstant(context, db);

        mDayWeeksActivated = C.DATA_COLLECTION_CRITERIA_NO_ACTIVATED;
        mDayWeeks = 0;
        mMealTimesActivated = C.DATA_COLLECTION_CRITERIA_NO_ACTIVATED;
        mMealTimes = 0;
    }

    public DataCollectionCriteria(int id, DataCollectionCriteriaInstant sin, DataCollectionCriteriaInstant unt, int dayWeeksActivated, int dayWeeks, int mealsActivated, int meals) {
        mId = id;
        mSince = sin;
        mUntil = unt;

        mDayWeeksActivated = dayWeeksActivated;
        mDayWeeks = dayWeeks;
        mMealTimesActivated = mealsActivated;
        mMealTimes = meals;

        mDataCollectionLabelRules = new ArrayList<>();
    }

    public Integer getId() {
        return mId;
    }



    /*
     *
     * Si el type es C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE son días pasados desde hoy
     * Si el type es C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION será el long de los milis pasados, osea > 0;
     */

    public void setSince(DataCollectionCriteriaInstant since) {
        this.mSince = since;
    }

    /**
     * @return Devuelve un Instant en caso de tipo relativo o absoluto, o un Annotation en caso de ser del tipo annotation.
     */
    public Instant getSinceInstant(){
        return mSince.getInstant();
    }

    public DataCollectionCriteriaInstant getSince() {
        return mSince;
    }

    public Instant getSinceInstantInTheMorning(){
        return getSinceInstant().setTimeToTheMorning();
    }


    /*
     *
     * Si el type es C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE son días pasados desde hoy
     * Si el type es C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_ANNOTATION es el ID de alguna anotación en la db
     */

    public void setUntil(DataCollectionCriteriaInstant until) {
        this.mUntil = until;
    }

    /**
     * @return Devuelve un Instant en caso de tipo relativo o absoluto, o un Annotation en caso de ser del tipo annotation.
     */
    public Instant getUntilInstant(){
        return mUntil.getInstant();
    }

    public DataCollectionCriteriaInstant getUntil() {
        return mUntil;
    }

    public Instant getUntilInstantIncreasedOneDayInTheMorning(){
        return getUntilInstant().increaseOneDay().setTimeToTheMorning();
    }

    public Integer getDayWeeksInteger() {
        return mDayWeeks;
    }

    public Integer getMealTimeInteger() {
        return mMealTimes;
    }


    // boolean methods to know if it's needed to collect data on certain day week or meal
    public boolean collectOnDayWeek(int dayWeek){
        return ((mDayWeeks >> (6 - dayWeek)) & 0b1) == 1;
    }

    public boolean collectOnMealTime(int meal){
        return ((mMealTimes >> (6 - meal)) & 0b1) == 1;
    }

    public void setCollectOnDayWeek(int dayWeek, boolean set){
        if(set){
            mDayWeeks = mDayWeeks | (0b1 << (6 - dayWeek));
        } else {
            mDayWeeks = mDayWeeks ^ (0b1 << (6 - dayWeek));
        }
    }

    public void setCollectOnGlucoseTime(int mealTime, boolean set){
        if(set){
            mMealTimes = mMealTimes | (0b1 << (6 - mealTime));
        } else {
            mMealTimes = mMealTimes ^ (0b1 << (6 - mealTime));
        }
    }

    public void setDayWeeksInteger(int i){
        mDayWeeks = i;
    }

    public void setMealTimesInteger(int i){
        mMealTimes = i;
    }

    public Integer getDayWeeksActivated() {
        return mDayWeeksActivated;
    }

    public void setDayWeeksActivated(int dayWeeksActivated) {
        this.mDayWeeksActivated = dayWeeksActivated;
    }

    public Integer getMealTimesActivated() {
        return mMealTimesActivated;
    }

    public void setMealTimesActivated(int mealTimesActivated) {
        this.mMealTimesActivated = mealTimesActivated;
    }

    // for label rules
    public ArrayList<DataCollectionLabelRule> getLabelRules(){
        return mDataCollectionLabelRules;
    }

    public void addLabelRule(DataCollectionLabelRule rule) {
        mDataCollectionLabelRules.add(rule);
    }
    public void resetLabelRules(){
        mDataCollectionLabelRules = new ArrayList<>();
    }

    public void setLabelRules(ArrayList<DataCollectionLabelRule> rules){
        mDataCollectionLabelRules = rules;
    }

    // to save and delete from database

    public void save(DataDatabase db){
        if(mDataCollectionLabelRules != null) {
            for(DataCollectionLabelRule rule : mDataCollectionLabelRules) {
                rule.save(db);
            }
        }

        if(getId() == 0){
            // nuevo
            db.insertDataCollectionCriteria(this);
        } else {
            // no nuevo
            db.updateDataCollectionCriteria(this);
        }

    }

    public void delete(DataDatabase db){
        if(mDataCollectionLabelRules != null) {
            for(DataCollectionLabelRule rule : mDataCollectionLabelRules) {
                rule.delete(db);
            }
        }

        db.deleteDataCollectionCriteria(this);
    }

    public boolean isLabelUsed(int labelId){
        for(DataCollectionLabelRule rule : mDataCollectionLabelRules){
            if(rule.getLabel().getId() == labelId)
                return true;
        }
        return false;
    }
}
