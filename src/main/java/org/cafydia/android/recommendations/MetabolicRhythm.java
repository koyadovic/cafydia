package org.cafydia.android.recommendations;

import org.cafydia.android.configdatabase.ConfigurationDatabase;
import org.cafydia.android.core.Instant;
import org.cafydia.android.core.Meal;
import org.cafydia.android.util.C;

/**
 * Created by user on 22/09/14.
 */
public abstract  class MetabolicRhythm {
    private Integer id;
    private String name;
    private String description;
    private Integer state;

    protected Instant startDate;

    protected int startingPreprandialType;

    // for rapid-acting or mix type insulin. You can use this (Default):
    private ModificationStart preprandialModificationStart = null;

    // or this three:
    private ModificationStart preprandialModificationStartBreakfast = null;
    private ModificationStart preprandialModificationStartLunch = null;
    private ModificationStart preprandialModificationStartDinner = null;

    // for slow-acting, intermediate-acting insulin
    private ModificationStart basalModificationStartBreakfast = null;
    private ModificationStart basalModificationStartLunch = null;
    private ModificationStart basalModificationStartDinner = null;

    public MetabolicRhythm(Integer id, String name, String description, int startingPreprandialType, int state, Instant startDate){
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPreprandialType = startingPreprandialType;
        this.state = state;
        if(startDate != null){
            startDate.setTimeToTheStartOfTheDay();
        }
        this.startDate = startDate;
    }

    public Float getPreprandialModification(Meal m){
        return getPreprandialModification(m, startingPreprandialType);
    }

    public Float getPreprandialModification(Meal m, int startingType){
        Float modification = 0.0f;
        float daysPassed = - startDate.getDaysPassedFromNow();
        switch (startingType){
            case C.STARTING_TYPE_GLOBAL:
                modification = preprandialModificationStart != null ? preprandialModificationStart.getModification(daysPassed) : 0f;
                break;
            case C.STARTING_TYPE_SPECIFIC:
                switch(m.getMealTime()){
                    case C.MEAL_BREAKFAST:
                        modification = preprandialModificationStartBreakfast != null ? preprandialModificationStartBreakfast.getModification(daysPassed) : 0f;
                        break;
                    case C.MEAL_LUNCH:
                        modification = preprandialModificationStartLunch != null ? preprandialModificationStartLunch.getModification(daysPassed) : 0f;
                        break;
                    case C.MEAL_DINNER:
                        modification = preprandialModificationStartDinner != null ? preprandialModificationStartDinner.getModification(daysPassed) : 0f;
                        break;
                }
                break;
        }

        return modification;
    }

    public Float getBasalModification(Meal m){
        Float modification = 0.0f;
        float daysPassed = - startDate.getDaysPassedFromNow();
        switch(m.getMealTime()){
            case C.MEAL_BREAKFAST:
                modification = basalModificationStartBreakfast != null ? basalModificationStartBreakfast.getModification(daysPassed) : 0f;
                break;
            case C.MEAL_LUNCH:
                modification = basalModificationStartLunch != null ? basalModificationStartLunch.getModification(daysPassed) : 0f;
                break;
            case C.MEAL_DINNER:
                modification = basalModificationStartDinner != null ? basalModificationStartDinner.getModification(daysPassed) : 0f;
                break;
        }

        return modification;
    }

    public void addDot(ModificationStartDot dot){
        switch(dot.getType()){

            case C.DOT_TYPE_PREPRANDIAL_INSULIN_GLOBAL:
                if(preprandialModificationStart == null) preprandialModificationStart = new ModificationStart();
                preprandialModificationStart.addDot(dot);
                //startingPreprandialType = C.STARTING_TYPE_GLOBAL;
                break;

            case C.DOT_TYPE_PREPRANDIAL_INSULIN_BREAKFAST:
                if(preprandialModificationStartBreakfast == null) preprandialModificationStartBreakfast = new ModificationStart();
                preprandialModificationStartBreakfast.addDot(dot);
                //startingPreprandialType = C.STARTING_TYPE_SPECIFIC;
                break;

            case C.DOT_TYPE_PREPRANDIAL_INSULIN_LUNCH:
                if(preprandialModificationStartLunch == null) preprandialModificationStartLunch = new ModificationStart();
                preprandialModificationStartLunch.addDot(dot);
                //startingPreprandialType = C.STARTING_TYPE_SPECIFIC;
                break;

            case C.DOT_TYPE_PREPRANDIAL_INSULIN_DINNER:
                if(preprandialModificationStartDinner == null) preprandialModificationStartDinner = new ModificationStart();
                preprandialModificationStartDinner.addDot(dot);
                //startingPreprandialType = C.STARTING_TYPE_SPECIFIC;
                break;


            case C.DOT_TYPE_BASAL_INSULIN_BREAKFAST:
                if(basalModificationStartBreakfast == null) basalModificationStartBreakfast = new ModificationStart();
                basalModificationStartBreakfast.addDot(dot);
                break;
            case C.DOT_TYPE_BASAL_INSULIN_LUNCH:
                if(basalModificationStartLunch == null) basalModificationStartLunch = new ModificationStart();
                basalModificationStartLunch.addDot(dot);
                break;
            case C.DOT_TYPE_BASAL_INSULIN_DINNER:
                if(basalModificationStartDinner == null) basalModificationStartDinner = new ModificationStart();
                basalModificationStartDinner.addDot(dot);
                break;
        }
    }

    public void resetDots(){
        preprandialModificationStart = null;
        preprandialModificationStartBreakfast = null;
        preprandialModificationStartLunch = null;
        preprandialModificationStartDinner = null;
        basalModificationStartBreakfast = null;
        basalModificationStartLunch = null;
        basalModificationStartDinner = null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        if(startDate != null){
            startDate.setTimeToTheStartOfTheDay();
        }
        this.startDate = startDate;
    }

    public Integer getStartingPreprandialType() {
        return startingPreprandialType;
    }

    public void setStartingPreprandialType(int startingPreprandialType) {
        this.startingPreprandialType = startingPreprandialType;
    }

    public void save(ConfigurationDatabase db){
        db.openWritable();
        switch(id){
            case 0:
                db.insertMetabolicRhythm(this);
                break;
            default:
                db.updateMetabolicRhythm(this);
        }
        db.close();
    }
    public void delete(ConfigurationDatabase db){
        db.openWritable();
        db.deleteMetabolicRhythm(this);
        db.close();
    }

    public ModificationStart getPreprandialModificationStart() {
        ModificationStart start = new ModificationStart();
        if(preprandialModificationStart != null) {
            for (ModificationStartDot dot : preprandialModificationStart.getDots()) {
                start.addDot(dot);
            }
        }
        return start;
    }

    public ModificationStart getPreprandialModificationStartBreakfast() {
        ModificationStart start = new ModificationStart();
        if(preprandialModificationStartBreakfast != null) {
            for (ModificationStartDot dot : preprandialModificationStartBreakfast.getDots()) {
                start.addDot(dot);
            }
        }
        return start;
    }

    public ModificationStart getPreprandialModificationStartLunch() {
        ModificationStart start = new ModificationStart();
        if(preprandialModificationStartLunch != null) {
            for (ModificationStartDot dot : preprandialModificationStartLunch.getDots()) {
                start.addDot(dot);
            }
        }
        return start;
    }

    public ModificationStart getPreprandialModificationStartDinner() {
        ModificationStart start = new ModificationStart();
        if(preprandialModificationStartDinner != null) {
            for (ModificationStartDot dot : preprandialModificationStartDinner.getDots()) {
                start.addDot(dot);
            }
        }
        return start;
    }

    public ModificationStart getBasalModificationStartBreakfast() {
        ModificationStart start = new ModificationStart();
        if(basalModificationStartBreakfast != null) {
            for (ModificationStartDot dot : basalModificationStartBreakfast.getDots()) {
                start.addDot(dot);
            }
        }
        return start;
    }

    public ModificationStart getBasalModificationStartLunch() {
        ModificationStart start = new ModificationStart();
        if(basalModificationStartLunch != null) {
            for (ModificationStartDot dot : basalModificationStartLunch.getDots()) {
                start.addDot(dot);
            }
        }
        return start;
    }

    public ModificationStart getBasalModificationStartDinner() {
        ModificationStart start = new ModificationStart();
        if(basalModificationStartDinner != null) {
            for (ModificationStartDot dot : basalModificationStartDinner.getDots()) {
                start.addDot(dot);
            }
        }
        return start;
    }

    public void setPreprandialModificationStart(ModificationStart start){
        preprandialModificationStart = start;
    }

    public void setPreprandialModificationStartBreakfast(ModificationStart preprandialModificationStartBreakfast) {
        this.preprandialModificationStartBreakfast = preprandialModificationStartBreakfast;
    }

    public void setPreprandialModificationStartLunch(ModificationStart preprandialModificationStartLunch) {
        this.preprandialModificationStartLunch = preprandialModificationStartLunch;
    }

    public void setPreprandialModificationStartDinner(ModificationStart preprandialModificationStartDinner) {
        this.preprandialModificationStartDinner = preprandialModificationStartDinner;
    }

    public void setBasalModificationStartBreakfast(ModificationStart basalModificationStartBreakfast) {
        this.basalModificationStartBreakfast = basalModificationStartBreakfast;
    }

    public void setBasalModificationStartLunch(ModificationStart basalModificationStartLunch) {
        this.basalModificationStartLunch = basalModificationStartLunch;
    }

    public void setBasalModificationStartDinner(ModificationStart basalModificationStartDinner) {
        this.basalModificationStartDinner = basalModificationStartDinner;
    }

    public abstract MetabolicRhythm duplicate();
    public abstract boolean isEqual(MetabolicRhythm m);
}
