package org.cafydia.android.recommendations;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;

import org.cafydia.android.core.Instant;
import org.cafydia.android.util.C;

import java.util.Date;

/**
 * Created by user on 30/07/14.
 *
 */

public class MetabolicFrameworkState {
    // atributes
    private Context mContext;

    private int activatedMetabolicRhythmId;
    private Instant activatedMetabolicRhythmStartDate;

    /*
     * FOR MANDATORY USE INSULIN (Rapid, or mix-type insulin)
     */
    private float startingPreprandialModification;
    private float startingPreprandialModificationBreakfast;
    private float startingPreprandialModificationLunch;
    private float startingPreprandialModificationDinner;

    private int startingPreprandialType;

    /*
     * FOR OPTIONAL USE INSULIN (slow acting insulin)
     */
    private float startingBasalModificationBreakfast;
    private float startingBasalModificationLunch;
    private float startingBasalModificationDinner;

    private Instant lastCheck;

    private Instant lastPreprandialBaselineModification;
    private Instant lastBasalBaselineModification;

    public static final String SHARED_PREFERENCES_METABOLIC_FRAMEWORK_STATE = "metabolic_framework_state";

    /*
     * Constructor
     */
    public MetabolicFrameworkState(Context c) {
        mContext = c;
        load();
    }

    // Preprandial Global
    public ModificationStartDot getStartingPreprandialModificationDot() {
        return new ModificationStartDot(-1, startingPreprandialModification);
    }
    public void setStartingPreprandialModification(Float startingPreprandialModification) {
        this.startingPreprandialModification = startingPreprandialModification;
    }

    // Preprandial Breakfast
    public ModificationStartDot getStartingPreprandialModificationBreakfastDot() {
        return new ModificationStartDot(-1, startingPreprandialModificationBreakfast);
    }
    public void setStartingPreprandialModificationBreakfast(Float startingPreprandialModificationBreakfast) {
        this.startingPreprandialModificationBreakfast = startingPreprandialModificationBreakfast;
    }

    // Preprandial Lunch
    public ModificationStartDot getStartingPreprandialModificationLunchDot() {
        return new ModificationStartDot(-1, startingPreprandialModificationLunch);
    }
    public void setStartingPreprandialModificationLunch(Float startingPreprandialModificationLunch) {
        this.startingPreprandialModificationLunch = startingPreprandialModificationLunch;
    }

    // Preprandial Dinner
    public ModificationStartDot getStartingPreprandialModificationDinnerDot() {
        return new ModificationStartDot(-1, startingPreprandialModificationDinner);
    }
    public void setStartingPreprandialModificationDinner(Float startingPreprandialModificationDinner) {
        this.startingPreprandialModificationDinner = startingPreprandialModificationDinner;
    }

    // Preprandial Type
    public int getStartingPreprandialType() {
        return startingPreprandialType;
    }
    public void setStartingPreprandialType(int startingPreprandialType) {
        this.startingPreprandialType = startingPreprandialType;

        switch (startingPreprandialType){
            case C.STARTING_TYPE_GLOBAL:
                startingPreprandialModificationBreakfast = 0f;
                startingPreprandialModificationLunch = 0f;
                startingPreprandialModificationDinner = 0f;
                break;
            case C.STARTING_TYPE_SPECIFIC:
                startingPreprandialModification = 0f;
                break;
        }
    }

    // Basal Breakfast
    public ModificationStartDot getStartingBasalModificationBreakfastDot() {
        return new ModificationStartDot(-1, startingBasalModificationBreakfast);
    }
    public void setStartingBasalModificationBreakfast(Float startingBasalModificationBreakfast) {
        this.startingBasalModificationBreakfast = startingBasalModificationBreakfast;
    }

    // Basal Lunch
    public ModificationStartDot getStartingBasalModificationLunchDot() {
        return new ModificationStartDot(-1, startingBasalModificationLunch);
    }
    public void setStartingBasalModificationLunch(Float startingBasalModificationLunch) {
        this.startingBasalModificationLunch = startingBasalModificationLunch;
    }

    // Basal Dinner
    public ModificationStartDot getStartingBasalModificationDinnerDot() {
        return new ModificationStartDot(-1, startingBasalModificationDinner);
    }
    public void setStartingBasalModificationDinner(Float startingBasalModificationDinner) {
        this.startingBasalModificationDinner = startingBasalModificationDinner;
    }


    public Instant getLastCheckInstant() {
        return lastCheck;
    }

    public void setLastCheckInstant(Instant lastCheck) {
        this.lastCheck = lastCheck;
    }

    public int getActivatedMetabolicRhythmId() {
        return activatedMetabolicRhythmId;
    }

    public void setActivatedMetabolicRhythmId(int activatedMetabolicRhythmId) {
        this.activatedMetabolicRhythmId = activatedMetabolicRhythmId;
    }

    public Instant getActivatedMetabolicRhythmStartDate() {
        return activatedMetabolicRhythmStartDate;
    }

    public void setActivatedMetabolicRhythmStartDate(Instant activatedMetabolicRhythmStartDate) {
        this.activatedMetabolicRhythmStartDate = activatedMetabolicRhythmStartDate;
    }

    /*
     * For the last modifications
     */

    public Instant getLastPreprandialBaselineModification() {
        return lastPreprandialBaselineModification;
    }

    public void notifyPreprandialBaselineModification(){
        this.lastPreprandialBaselineModification = new Instant();
    }

    public Instant getLastBasalBaselineModification() {
        return lastBasalBaselineModification;
    }

    public void notifyBasalBaselineModification(){
        this.lastBasalBaselineModification = new Instant();
    }

    /*
     * To load and save the state
     */
    private void load(){
        if(mContext != null) {
            SharedPreferences sp = mContext.getSharedPreferences(SHARED_PREFERENCES_METABOLIC_FRAMEWORK_STATE, Context.MODE_PRIVATE);

        /*
         * FOR MANDATORY USE INSULIN (Rapid, or mix-type insulin)
         */

            startingPreprandialModification = sp.getFloat("starting_mandatory_modification", 0.0f);
            startingPreprandialModificationBreakfast = sp.getFloat("starting_mandatory_modification_breakfast", 0.0f);
            startingPreprandialModificationLunch = sp.getFloat("starting_mandatory_modification_lunch", 0.0f);
            startingPreprandialModificationDinner = sp.getFloat("starting_mandatory_modification_dinner", 0.0f);

            startingPreprandialType = sp.getInt("starting_mandatory_type", C.STARTING_TYPE_GLOBAL);


        /*
         * FOR OPTIONAL USE INSULIN (slow acting insulin)
         */


            startingBasalModificationBreakfast = sp.getFloat("starting_optional_modification_breakfast", 0.0f);
            startingBasalModificationLunch = sp.getFloat("starting_optional_modification_lunch", 0.0f);
            startingBasalModificationDinner = sp.getFloat("starting_optional_modification_dinner", 0.0f);

            activatedMetabolicRhythmId = sp.getInt("activated_metabolic_rhythm_id", 1);
            activatedMetabolicRhythmStartDate = new Instant(sp.getLong("activated_metabolic_rhythm_start_date", new Instant().toDate().getTime()));

            lastCheck = new Instant(sp.getLong("last_check", new Date().getTime()));

            lastPreprandialBaselineModification = new Instant(sp.getLong("last_preprandial_baseline_modification", new Date().getTime()));
            lastBasalBaselineModification = new Instant(sp.getLong("last_basal_baseline_modification", new Date().getTime()));
        }
    }


    public void save(){
        if(mContext != null) {
            SharedPreferences sp = mContext.getSharedPreferences(SHARED_PREFERENCES_METABOLIC_FRAMEWORK_STATE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            /*
             * FOR MANDATORY USE INSULIN (Rapid, or mix-type insulin)
             */

            editor.putFloat("starting_mandatory_modification", startingPreprandialModification);
            editor.putFloat("starting_mandatory_modification_breakfast", startingPreprandialModificationBreakfast);
            editor.putFloat("starting_mandatory_modification_lunch", startingPreprandialModificationLunch);
            editor.putFloat("starting_mandatory_modification_dinner", startingPreprandialModificationDinner);

            editor.putInt("starting_mandatory_type", startingPreprandialType);


            /*
             * FOR OPTIONAL USE INSULIN (slow acting insulin)
             */

            editor.putFloat("starting_optional_modification_breakfast", startingBasalModificationBreakfast);
            editor.putFloat("starting_optional_modification_lunch", startingBasalModificationLunch);
            editor.putFloat("starting_optional_modification_dinner", startingBasalModificationDinner);

            editor.putInt("activated_metabolic_rhythm_id", activatedMetabolicRhythmId);
            editor.putLong("activated_metabolic_rhythm_start_date", activatedMetabolicRhythmStartDate.toDate().getTime());

            editor.putLong("last_check", lastCheck.toDate().getTime());
            editor.putLong("last_preprandial_baseline_modification", lastPreprandialBaselineModification.toDate().getTime());
            editor.putLong("last_basal_baseline_modification", lastBasalBaselineModification.toDate().getTime());

            editor.apply();

            new BackupManager(mContext).dataChanged();
        }
    }

}
