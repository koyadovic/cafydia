package org.cafydia.android.core;

import com.google.gson.Gson;

import org.cafydia.android.datadatabase.DataDatabase;
import org.cafydia.android.util.C;

/**
 * Created by user on 12/10/14.
 */

public class Snack extends Instant {
    private Integer id;
    private Integer snack;
    private Integer metabolicRhythmId;
    private String metabolicRhythmName;
    private FoodBundle foodSelected;

    public Snack(Integer id, String dateString, Integer snack, Integer metabolicRhythmId, String metabolicRhythmName, String foodSelectedJson) {
        super(dateString);
        this.id = id;
        this.snack = snack;
        this.metabolicRhythmId = metabolicRhythmId;
        this.metabolicRhythmName = metabolicRhythmName;

        Gson gson = new Gson();
        foodSelected = gson.fromJson(foodSelectedJson, C.TYPE_TOKEN_TYPE_FOOD_BUNDLE);
    }
    public Snack(Integer snack, Integer metabolicRhythmId, String metabolicRhythmName) {
        super();
        this.id = 0;
        this.snack = snack;
        this.metabolicRhythmId = metabolicRhythmId;
        this.metabolicRhythmName = metabolicRhythmName;
        foodSelected = new FoodBundle();
    }

    // getters
    public Integer getId() {
        return id;
    }

    public Integer getSnack() {
        return snack;
    }

    public Integer getMetabolicRhythmId() {
        return metabolicRhythmId;
    }

    public String getMetabolicRhythmName() {
        return metabolicRhythmName;
    }

    public FoodBundle getFoodSelected() {
        return foodSelected;
    }

    // setters
    public void setSnack(Integer snack) {
        this.snack = snack;
    }

    public void setMetabolicRhythmId(Integer metabolicRhythmId) {
        this.metabolicRhythmId = metabolicRhythmId;
    }

    public void setMetabolicRhythmName(String metabolicRhythmName) {
        this.metabolicRhythmName = metabolicRhythmName;
    }

    public void setFoodSelected(FoodBundle foodSelected) {
        this.foodSelected = foodSelected;
    }

    /*
     * To save, update and delete from database
     */

    public void save(DataDatabase db){
        switch (id){
            case 0:
                db.insertSnack(this);
                break;
            default:
                db.updateSnack(this);
        }
    }
    public void delete(DataDatabase db){
        db.deleteSnack(this);
    }
}
