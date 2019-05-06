package org.cafydia.android.recommendations;

import org.cafydia.android.configdatabase.ConfigurationDatabase;
import org.cafydia.android.core.Meal;
import org.cafydia.android.util.C;

/**
 * Created by user on 1/08/14.
 */
public class CorrectiveComplex extends Corrective {
    private Float mModificationBr;
    private Float mModificationLu;
    private Float mModificationDi;

    /*
    Triggers are seven bits for the seven days when the type is complex and 21 bits for the three meals of seven days of the week.

    break   lunch   diner
    1111111 1111111 1111111

    from monday to sunday each one
     */
    private Integer mTriggers;


    public CorrectiveComplex(
            Integer id,
            String name,
            String description,
            Integer type,
            Integer metabolicRhythmId,
            Integer modificationType,
            Float modificationBr,
            Float modificationLu,
            Float modificationDi,
            Integer visible,
            Integer triggers){

        super(id, name, description, type, metabolicRhythmId, modificationType, visible);

        mModificationBr = modificationBr;
        mModificationLu = modificationLu;
        mModificationDi = modificationDi;

        mTriggers = triggers;
    }

    public CorrectiveComplex(String name, int metabolicRhythmId){
        super(name, C.CORRECTIVE_TYPE_COMPLEX, metabolicRhythmId);
        mModificationBr = 0f;
        mModificationLu = 0f;
        mModificationDi = 0f;
        mTriggers = 0;
    }


    public Float getModificationBr() {
        return mModificationBr;
    }

    public void setModificationBr(Float modificationBr) {
        this.mModificationBr = modificationBr;
    }

    public Float getModificationLu() {
        return mModificationLu;
    }

    public void setModificationLu(Float modificationLu) {
        this.mModificationLu = modificationLu;
    }

    public Float getModificationDi() {
        return mModificationDi;
    }

    public void setModificationDi(Float modificationDi) {
        this.mModificationDi = modificationDi;
    }


    public Integer getTriggers() {
        return mTriggers;
    }

    public void setTriggers(int triggers) {
        this.mTriggers = triggers;
    }

    public boolean applies(Meal m){
        return ((mTriggers >> (6 - m.getDayOfWeek())) & 1) == 1;
    }
    public boolean applies(int meal, int dayOfWeek){
        return ((mTriggers >> (6 - dayOfWeek)) & 1) == 1;
    }

    public float getModification(Meal m){
        switch (m.getMealTime()){
            case C.MEAL_BREAKFAST:
                return getModificationBr();
            case C.MEAL_LUNCH:
                return getModificationLu();
            case C.MEAL_DINNER:
                return getModificationDi();
        }
        return 0.0f;
    }

    public CorrectiveSimple toSimple(){
        return new CorrectiveSimple(0,getName(),getDescription(),C.CORRECTIVE_TYPE_SIMPLE,getMetabolicRhythmId(),getModificationType(), 0f, getVisible(), 0);
    }

    @Override
    public CorrectiveComplex cloneCorrective(){
        return new CorrectiveComplex(getId(),getName(),getDescription(),getType(),getMetabolicRhythmId(),getModificationType(),getModificationBr(),getModificationLu(),getModificationDi(),getVisible(),getTriggers());
    }

    @Override
    public boolean equals(Corrective c){
        return getId().equals(c.getId()) &&
                getName().equals(c.getName()) &&
                getDescription().equals(c.getDescription()) &&
                getType().equals(c.getType()) &&
                getMetabolicRhythmId().equals(c.getMetabolicRhythmId()) &&
                getModificationType().equals(c.getModificationType()) &&
                getModificationBr().equals(((CorrectiveComplex) c).getModificationBr()) &&
                getModificationLu().equals(((CorrectiveComplex) c).getModificationLu()) &&
                getModificationDi().equals(((CorrectiveComplex) c).getModificationDi()) &&
                getVisible().equals(c.getVisible()) &&
                getTriggers().equals(c.getTriggers());
    }

    @Override
    public void save(ConfigurationDatabase db){
        switch (getId()){
            case 0:
                db.insertCorrectiveComplex(this);
                break;
            default:
                db.updateCorrectiveComplex(this);

        }
    }

    @Override
    public void delete(ConfigurationDatabase db){
        switch (getId()){
            case 0:
                break;
            default:
                db.deleteCorrectiveComplex(this);

        }
    }

    @Override
    public void setTrigger(int meal, int dayOfWeek, boolean activated){
        int trigger = 1;
        trigger = trigger << (6 - dayOfWeek);

        if(activated) {
            mTriggers = mTriggers | trigger;
        } else {
            mTriggers = mTriggers ^ trigger;
        }
    }

    public String toString(Meal meal){
        if(meal == null) {
            String result = getName();
            if (getModificationType().equals(C.CORRECTIVE_MODIFICATION_TYPE_NUMBER)) {
                if (getModificationBr() >= 0) {
                    result += " (+" + getModificationBr().toString();
                } else {
                    result += " (" + getModificationBr().toString();
                }
                if (getModificationLu() >= 0) {
                    result += ", +" + getModificationLu().toString();
                } else {
                    result += ", " + getModificationLu().toString();
                }
                if (getModificationDi() >= 0) {
                    result += ", +" + getModificationDi().toString() + ")";
                } else {
                    result += ", " + getModificationDi().toString() + ")";
                }
            } else {
                if (getModificationBr() >= 0) {
                    result += " (+" + getModificationBr().toString() + "%";
                } else {
                    result += " (" + getModificationBr().toString() + "%";
                }
                if (getModificationLu() >= 0) {
                    result += ", +" + getModificationLu().toString() + "%";
                } else {
                    result += ", " + getModificationLu().toString() + "%";
                }
                if (getModificationDi() >= 0) {
                    result += ", +" + getModificationDi().toString() + "%)";
                } else {
                    result += ", " + getModificationDi().toString() + "%)";
                }
            }

            return result;
        } else {
            if (getModificationType().equals(C.CORRECTIVE_MODIFICATION_TYPE_NUMBER)) {
                switch (meal.getMealTime()) {
                    case C.MEAL_BREAKFAST:
                        if (getModificationBr() >= 0) {
                            return getName() + " (+" + getModificationBr().toString() + ")";
                        } else {
                            return getName() + " (" + getModificationBr().toString() + ")";
                        }
                    case C.MEAL_LUNCH:
                        if (getModificationLu() >= 0) {
                            return getName() + " (+" + getModificationLu().toString() + ")";
                        } else {
                            return getName() + " (" + getModificationLu().toString() + ")";
                        }
                    default:
                        if (getModificationDi() >= 0) {
                            return getName() + " (+" + getModificationDi().toString() + ")";
                        } else {
                            return getName() + " (" + getModificationDi().toString() + ")";
                        }

                }
            } else {
                switch (meal.getMealTime()) {
                    case C.MEAL_BREAKFAST:
                        if (getModificationBr() >= 0) {
                            return getName() + " (+" + getModificationBr().toString() + "%)";
                        } else {
                            return getName() + " (" + getModificationBr().toString() + "%)";
                        }
                    case C.MEAL_LUNCH:
                        if (getModificationLu() >= 0) {
                            return getName() + " (+" + getModificationLu().toString() + "%)";
                        } else {
                            return getName() + " (" + getModificationLu().toString() + "%)";
                        }
                    default:
                        if (getModificationDi() >= 0) {
                            return getName() + " (+" + getModificationDi().toString() + "%)";
                        } else {
                            return getName() + " (" + getModificationDi().toString() + "%)";
                        }

                }
            }
        }
    }
}
