package org.cafydia4.android.core;

import com.google.gson.Gson;

import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.util.C;

import java.util.ArrayList;

/**
 * Created by usuario on 2/03/14.
 */
public class Meal extends Instant {

    // attributes
    private int mId;
    private int mMeal;
    private Float mCarbohydrates;
    private Integer mMetabolicRhythmId;
    private String mMetabolicRhythmName;
    private Float mBaselinePreprandial = null;
    private Float mBaselineBasal = null;
    private Float mBeginningPreprandial = null;
    private Float mBeginningBasal = null;
    private Float mCorrectionFactorPreprandial = null;
    private Float mCorrectivesPreprandial = null;
    private String mCorrectivesName;
    private ArrayList<Food> mFoodSelected;

    private Float mTotalPreprandial;
    private Float mTotalBasal;

    private Float mFinalPreprandial = 0.0f;

    // constructors
    public Meal (int meal, int metabolicRhythmId, String metabolicRhythmName) {
        // superconstructor
        super();

        this.mMeal = meal;
        this.mTotalPreprandial = 0.0f;
        mFoodSelected = new ArrayList<>();

        mMetabolicRhythmId = metabolicRhythmId;
        mMetabolicRhythmName = metabolicRhythmName;
    }

    public Meal (int id, String dateString, int meal, Float carbohydrates, int metabolicRhythmId, String metabolicRhythmName,
                 Float baselinePreprandial, Float baselineBasal, Float metabolicPreprandial, Float metabolicBasal,
                 Float correctionFactorPreprandial, Float correctivesPreprandial, String correctivesName, String foodSelectedJson,
                 Float finalPreprandial) {
        // superconstructor

        super(dateString);

        this.mId = id;
        this.mMeal = meal;
        this.mCarbohydrates = carbohydrates;
        this.mMetabolicRhythmId = metabolicRhythmId;
        this.mMetabolicRhythmName = metabolicRhythmName;
        this.mBaselinePreprandial = baselinePreprandial;
        this.mBaselineBasal = baselineBasal;
        this.mBeginningPreprandial = metabolicPreprandial;
        this.mBeginningBasal = metabolicBasal;
        this.mCorrectionFactorPreprandial = correctionFactorPreprandial;
        this.mCorrectivesPreprandial = correctivesPreprandial;
        this.mCorrectivesName = correctivesName;
        this.mFoodSelected = getFoodSelectedFromJson(foodSelectedJson);

        this.mFinalPreprandial = finalPreprandial;

        calculateTotalDoses();
    }

    private void calculateTotalDoses(){
        if (mBaselinePreprandial != null) {
            mTotalPreprandial = mBaselinePreprandial;
            mTotalPreprandial += mBeginningPreprandial != null ? mBeginningPreprandial : 0.0f;
            mTotalPreprandial += mCorrectivesPreprandial != null ? mCorrectivesPreprandial : 0.0f;
            mTotalPreprandial += mCorrectionFactorPreprandial != null ? mCorrectionFactorPreprandial : 0.0f;
        } else {
            mTotalPreprandial = 0.0f;
        }

        if(mBaselineBasal != null){
            mTotalBasal = mBaselineBasal;
            mTotalBasal += mBeginningBasal != null ? mBeginningBasal : 0.0f;
        } else {
            mTotalBasal = 0.0f;
        }

    }


    // getters and setters
    public Integer getMealId() {
        return mId;
    }

    public Integer getMealTime() {
        return mMeal;
    }

    public Float getMealCarbohydrates() {
        return mCarbohydrates;
    }

    public void setMealCarbohydrates(float c){
        mCarbohydrates = c;
    }

    public Float getTotalPreprandialDose() {
        if(mCarbohydrates != null && mCarbohydrates > 0.0f) {
            return mTotalPreprandial;
        } else {
            return 0.0f;
        }
    }

    public Float getFinalPreprandialDose() {
        return mFinalPreprandial;
    }

    public void setFinalPreprandialDose(Float finalPreprandial) {
        this.mFinalPreprandial = finalPreprandial;
    }

    public Float getTotalBasalDose() {
        return mTotalBasal;
    }

    public void addFood(Food food){
        mFoodSelected.add(food);
        mCarbohydrates += (food.getCPercent() * food.getWeightInGrams()) / 100.0f;
    }

    public void deleteFood(Food food){
        mFoodSelected.remove(food);
        mCarbohydrates -= (food.getCPercent() * food.getWeightInGrams()) / 100.0f;
    }

    public void setFoodSelected(ArrayList<Food> foods){
        mFoodSelected = new ArrayList<Food>();
        mCarbohydrates = 0.0f;

        for (Food f : foods){
            mFoodSelected.add(f);
            mCarbohydrates += (f.getCPercent() * f.getWeightInGrams()) / 100.0f;
        }
    }

    public Food getFood(int index){
        return mFoodSelected.get(index);
    }

    public int getNumberOfFoodSelected(){
        return mFoodSelected.size();
    }

    public ArrayList<Food> getFoodSelected(){
        return mFoodSelected;
    }

    public String getMetabolicRhythmName() {
        return mMetabolicRhythmName;
    }

    public String getCorrectivesAppliedName() {
        return mCorrectivesName;
    }

    public void setCorrectivesAppliedName(String correctivesAppliedName) {
        this.mCorrectivesName = correctivesAppliedName;
    }

    public String getMealString(){
        return C.MEAL_STRING[mMeal];
    }

    public Integer getMetabolicRhythmId() {
        return mMetabolicRhythmId;
    }


    /*
     * Getter and setters for all the recommendation parameters
     */
    public Float getBaselinePreprandial() {
        return mBaselinePreprandial;
    }

    public void setBaselinePreprandial(Float mBaselinePreprandial) {
        this.mBaselinePreprandial = mBaselinePreprandial;
        calculateTotalDoses();
    }

    public Float getBaselineBasal() {
        return mBaselineBasal;
    }

    public void setBaselineBasal(Float mBaselineBasal) {
        this.mBaselineBasal = mBaselineBasal;
        calculateTotalDoses();
    }

    public Float getBeginningPreprandial() {
        return mBeginningPreprandial;
    }

    public void setBeginningPreprandial(Float mBeginningPreprandial) {
        this.mBeginningPreprandial = mBeginningPreprandial;
        calculateTotalDoses();
    }

    public Float getBeginningBasal() {
        return mBeginningBasal;
    }

    public void setBeginningBasal(Float mBeginningBasal) {
        this.mBeginningBasal = mBeginningBasal;
        calculateTotalDoses();
    }

    public Float getCorrectionFactorPreprandial() {
        return mCorrectionFactorPreprandial;
    }

    public void setCorrectionFactorPreprandial(Float mCorrectionFactorPreprandial) {
        this.mCorrectionFactorPreprandial = mCorrectionFactorPreprandial;
        calculateTotalDoses();
    }

    public Float getCorrectivesPreprandial() {
        return mCorrectivesPreprandial;
    }

    public void setCorrectivesPreprandial(Float mCorrectivesPreprandial) {
        this.mCorrectivesPreprandial = mCorrectivesPreprandial;
        calculateTotalDoses();
    }

    // to convert list of food selected to Json and from it
    public String getFoodSelectedToJson(){
        Gson gson = new Gson();
        return gson.toJson(mFoodSelected);
    }

    public ArrayList<Food> getFoodSelectedFromJson(String json){
        return new Gson().fromJson(json, C.TYPE_TOKEN_TYPE_ARRAY_LIST_FOOD);
    }

    // to save changes to database
    public void save(DataDatabase db){
        switch (this.getMealId()){
            case 0: // es nuevo
                db.insertMeal(this);
                break;
            default: // no lo es
                db.updateMeal(this);
        }
    }

    public void delete(DataDatabase db){
        db.deleteMeal(this);
    }

    @Override
    public String toString(){
        String s = getMealString() + ": ";

        s += getUserDateStringShort() + " " + getUserTimeStringShort() + " ";
        s += "Final preprandial: " + (getFinalPreprandialDose() != 0.0 ? getFinalPreprandialDose().toString() : getTotalPreprandialDose().toString()) + " ";

        if(getTotalBasalDose() != 0.0f) {
            s += "Final basal: " + getTotalBasalDose().toString();
        }
        return s;
    }

}
