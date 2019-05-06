package org.cafydia.android.util;

import org.cafydia.android.core.Food;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by user on 24/08/14.
 */
public class MyFoodArrayList {
    private ArrayList<Food> foods;


    //
    // Constructors
    //
    public MyFoodArrayList(){
        foods = new ArrayList<>();
    }

    public MyFoodArrayList(ArrayList<Food> foods){
        this.foods = foods;
        sort();
    }

    //
    // To add, update or delete foods
    //
    public void addOrUpdateFood(Food food){
        if(food.getId() != 0) {
            for (Food f : foods) {
                if (f.getId().equals(food.getId())) {
                    foods.set(foods.indexOf(f), food);
                    sort();
                    return;
                }
            }
        }
        foods.add(food);
        sort();
    }
    public void addFood(Food food){
        foods.add(food);
        sort();
    }

    public void removeFood(Food food){
        foods.remove(food);
        sort();
    }

    //
    // To set and get the internal ArrayList with foods
    //
    public ArrayList<Food> getFoodArrayList() {
        return foods;
    }

    public void setFoods(ArrayList<Food> foods) {
        this.foods = foods;
        sort();
    }

    //
    // Private methods
    //
    public void sort(){
        Collections.sort(foods, new FoodNameComparator());
    }

    private class FoodNameComparator implements Comparator<Food> {
        public int compare(Food left, Food right) {
            return left.getName().compareTo(right.getName());
        }
    }

    /*
    public String toStringWeight(Context c){
        UnitChanger changer = new UnitChanger(c);

        String s = "";

        if(foods.size() == 0) {
            return s;
        }

        for(Food food : foods){
            if(s.equals("")){
                s = food.getName() + " (" + MyRound.roundToInteger(changer.toUIFromInternalWeight(food.getWeightInGrams())).toString() + changer.getStringUnitForWeightShort() + ")";
            } else {
                s = s + ", " + food.getName() + " (" + MyRound.roundToInteger(changer.toUIFromInternalWeight(food.getWeightInGrams())).toString() + changer.getStringUnitForWeightShort() + ")";
            }
        }
        return s + ".";
    }
    */
}
