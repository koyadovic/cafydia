package org.cafydia.android.core;

import org.cafydia.android.util.MyFoodArrayList;

import java.util.ArrayList;

/**
 * Created by user on 25/08/14.
 */
public class FoodBundle {
    private MyFoodArrayList mFoods;


    public FoodBundle() {
        mFoods = new MyFoodArrayList();
    }

    public FoodBundle(MyFoodArrayList foodList){
        mFoods = foodList;
    }

    public MyFoodArrayList getFoods(){
        return mFoods;
    }
    public void setFoods(MyFoodArrayList f){
        mFoods = f;
    }

    public void setFoodsByArrayList(ArrayList<Food> al){
        if(mFoods == null) mFoods = new MyFoodArrayList();
        for (Food f : al){
            mFoods.addOrUpdateFood(f);
        }
    }

    public void addFood(Food food){
        mFoods.getFoodArrayList().add(food);
    }
    public void removeFood(Food food) {
        mFoods.getFoodArrayList().remove(food);
    }
    public void editFood(Food food){
        for(int a=0; a < mFoods.getFoodArrayList().size(); a++) {
            if(mFoods.getFoodArrayList().get(a).getId().equals(food.getId())) {
                mFoods.getFoodArrayList().set(a, food);
            }
        }
    }

    public Integer getFoodSize(){
        return mFoods.getFoodArrayList().size();
    }

    public Food getFoodElement(int position){ return mFoods.getFoodArrayList().get(position); }

    public Float getTotalCarbohydratesInGrams(){
        Float total = 0.0f;
        for(Food food : mFoods.getFoodArrayList()){
            total += food.getWeightInGrams() * (food.getCPercent() / 100.0f);
        }
        return total;
    }


}
