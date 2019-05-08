package org.cafydia4.android.recommendations;

import org.cafydia4.android.configdatabase.ConfigurationDatabase;
import org.cafydia4.android.core.Meal;
import org.cafydia4.android.util.C;

/**
 * Created by user on 1/08/14.
 */
public class CorrectiveSimple extends Corrective {
    private Float mModification;

    /*
    Triggers are seven bits for the seven days when the type is complex and 21 bits for the three meals of seven days of the week.

    break   lunch   diner
    1111111 1111111 1111111

    from monday to sunday each one
     */
    private Integer mTriggers;


    public CorrectiveSimple(Integer id, String name, String description, Integer type, Integer metabolicRhythmId, Integer modificationType, Float modification, Integer visible, Integer triggers){
        super(id, name, description, type, metabolicRhythmId, modificationType, visible);
        mModification = modification;
        mTriggers = triggers;
    }

    public CorrectiveSimple(String name, int metabolicRhythmId){
        super(name, C.CORRECTIVE_TYPE_SIMPLE, metabolicRhythmId);
        mModification = 0f;
        mTriggers = 0;
    }

    public Float getModification() {
        return mModification;
    }

    public float getModification(Meal m){
        return mModification;
    }

    public void setModification(Float modification) {
        this.mModification = modification;
    }

    public Integer getTriggers() {
        return mTriggers;
    }

    public void setTriggers(int triggers) {
        this.mTriggers = triggers;
    }

    public boolean applies(Meal m){
        int t = mTriggers >> ((2 - m.getMealTime()) * 7);
        return ((t >> (6 - m.getDayOfWeek())) & 1) == 1;
    }
    public boolean applies(int meal, int dayOfWeek){
        int t = mTriggers >> ((2 - meal) * 7);
        return ((t >> (6 - dayOfWeek)) & 1) == 1;
    }

    public CorrectiveComplex toComplex(){
        return new CorrectiveComplex(0, getName(), getDescription(), C.CORRECTIVE_TYPE_COMPLEX, getMetabolicRhythmId(), getModificationType(), 0f, 0f, 0f, getVisible(), 0);
    }


    @Override
    public CorrectiveSimple cloneCorrective(){
        return new CorrectiveSimple(getId(),getName(),getDescription(),getType(),getMetabolicRhythmId(),getModificationType(),getModification(),getVisible(),getTriggers());
    }

    @Override
    public boolean equals(Corrective c){
        return getId().equals(c.getId()) &&
                getName().equals(c.getName()) &&
                getDescription().equals(c.getDescription()) &&
                getType().equals(c.getType()) &&
                getMetabolicRhythmId().equals(c.getMetabolicRhythmId()) &&
                getModificationType().equals(c.getModificationType()) &&
                getModification().equals(((CorrectiveSimple) c).getModification()) &&
                getVisible().equals(c.getVisible()) &&
                getTriggers().equals(c.getTriggers());
    }

    @Override
    public void save(ConfigurationDatabase db){
        switch (getId()){
            case 0:
                db.insertCorrectiveSimple(this);
                break;
            default:
                db.updateCorrectiveSimple(this);

        }
    }

    @Override
    public void delete(ConfigurationDatabase db){
        switch (getId()){
            case 0:
                break;
            default:
                db.deleteCorrectiveSimple(this);

        }
    }

    @Override
    public void setTrigger(int meal, int dayOfWeek, boolean activated){
        int trigger = 1;
        trigger = trigger << ((2 - meal) * 7);
        trigger = trigger << (6 - dayOfWeek);

        if(activated) {
            mTriggers = mTriggers | trigger;
        } else {
            mTriggers = mTriggers ^ trigger;
        }
    }

    public String toString(Meal meal){
        if(getModificationType().equals(C.CORRECTIVE_MODIFICATION_TYPE_NUMBER)) {
            if (getModification() >= 0) {
                return getName() + " (+" + getModification().toString() + ")";
            } else {
                return getName() + " (" + getModification().toString() + ")";
            }
        } else {
            if (getModification() >= 0) {
                return getName() + " (+" + getModification().toString() + "%)";
            } else {
                return getName() + " (" + getModification().toString() + "%)";
            }
        }
    }
}
