package org.cafydia.android.recommendations;

import org.cafydia.android.configdatabase.ConfigurationDatabase;
import org.cafydia.android.core.Meal;
import org.cafydia.android.util.C;

/**
 * Created by user on 16/10/14.
 */
public abstract class Corrective {
    private Integer mId;
    private String mName;
    private String mDescription;
    private Integer mType;
    private Integer mMetabolicRhythmId;
    private Integer mModificationType;
    private Integer mVisible;
    private Boolean temporalState = null;

    public Corrective(){
        this("", C.CORRECTIVE_TYPE_SIMPLE, 1);
    }
    public Corrective(String name, int type, int metabolicRhythmId){
        this(0, name, "", type, metabolicRhythmId, C.CORRECTIVE_MODIFICATION_TYPE_NUMBER, C.CORRECTIVE_VISIBLE_YES);
    }

    public Corrective(int id, String name, String description, int type, int metabolicRhythmId, int modificationType, int visible){
        mId = id;
        mName = name;
        mDescription = description;
        mType = type;
        mMetabolicRhythmId = metabolicRhythmId;
        mModificationType = modificationType;
        mVisible = visible;
    }



    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Integer getType() {
        return mType;
    }

    public void setType(Integer type) {
        this.mType = type;
    }

    public Integer getMetabolicRhythmId() {
        return mMetabolicRhythmId;
    }

    public void setMetabolicRhythmId(Integer metabolicRhythmId) {
        this.mMetabolicRhythmId = metabolicRhythmId;
    }

    public Integer getModificationType() {
        return mModificationType;
    }

    public void setModificationType(Integer modificationType) {
        mModificationType = modificationType;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public Integer getVisible() {
        return mVisible;
    }

    public void setVisible(Integer visible) {
        this.mVisible = visible;
    }

    public Boolean getTemporalState() {
        return temporalState;
    }

    public void setTemporalState(Boolean temporalState) {
        this.temporalState = temporalState;
    }

    public abstract Corrective cloneCorrective();
    public abstract boolean equals(Corrective c);
    public abstract boolean applies(Meal meal);
    public abstract boolean applies(int meal, int dayOfWeek);
    public abstract float getModification(Meal meal);
    public abstract Integer getTriggers();
    public abstract void save(ConfigurationDatabase db);
    public abstract void delete(ConfigurationDatabase db);
    public abstract void setTrigger(int meal, int dayOfWeek, boolean activated);
    public abstract void setTriggers(int triggers);
    public abstract String toString(Meal meal);
}
