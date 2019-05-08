package org.cafydia4.android.chartobjects;

import org.cafydia4.android.core.GlucoseTest;
import org.cafydia4.android.core.Meal;

import java.util.ArrayList;

/**
 * Created by user on 28/02/15.
 */
public class GlucoseTestsCrossedMeals {

    private ArrayList<GlucoseTestsCrossedOneMeal> glucoseTestsCrossedMeals;

    public GlucoseTestsCrossedMeals () {
        glucoseTestsCrossedMeals = new ArrayList<>();
    }

    public int getCount(){
        return glucoseTestsCrossedMeals.size();
    }

    public GlucoseTestsCrossedOneMeal getItem(int index){
        return glucoseTestsCrossedMeals.get(index);
    }

    public void newElement(){
        glucoseTestsCrossedMeals.add(new GlucoseTestsCrossedOneMeal());
    }

    public GlucoseTestsCrossedOneMeal getCurrentElement(){
        if(getCount() > 0) {
            return glucoseTestsCrossedMeals.get(getCount() - 1);
        } else {
            return null;
        }
    }

    public void addNewElement(GlucoseTestsCrossedOneMeal one){
        addNewElement(one.getGlucoseTestBeforeMeal(), one.getGlucoseTestAfterMeal(), one.getMeal());
    }

    public void addNewElement(GlucoseTest before, GlucoseTest after, Meal m){
        GlucoseTestsCrossedOneMeal one = new GlucoseTestsCrossedOneMeal();
        one.setGlucoseTestAfterMeal(after);
        one.setGlucoseTestBeforeMeal(before);
        one.setMeal(m);
        glucoseTestsCrossedMeals.add(one);
    }

    public class GlucoseTestsCrossedOneMeal {
        private GlucoseTest mGlucoseTestBeforeMeal = null;
        private GlucoseTest mGlucoseTestAfterMeal = null;
        private Meal mMeal = null;

        public GlucoseTest getGlucoseTestBeforeMeal() {
            return mGlucoseTestBeforeMeal;
        }

        public void setGlucoseTestBeforeMeal(GlucoseTest glucoseTestBeforeMeal) {
            this.mGlucoseTestBeforeMeal = glucoseTestBeforeMeal;
        }

        public GlucoseTest getGlucoseTestAfterMeal() {
            return mGlucoseTestAfterMeal;
        }

        public void setGlucoseTestAfterMeal(GlucoseTest glucoseTestAfterMeal) {
            this.mGlucoseTestAfterMeal = glucoseTestAfterMeal;
        }

        public Meal getMeal() {
            return mMeal;
        }

        public void setMeal(Meal meal) {
            this.mMeal = meal;
        }
    }


}
