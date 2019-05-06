package org.cafydia.android.recommendations;

import org.cafydia.android.core.Instant;

/**
 * Created by user on 22/09/14.
 */
public class MetabolicRhythmMaster extends MetabolicRhythm {

    public MetabolicRhythmMaster(Integer id, String name, String description, int startingPreprandialType, int state, Instant startDate){
        super(id, name, description, startingPreprandialType, state, startDate);
    }


    public MetabolicRhythmMaster duplicate(){
        MetabolicRhythmMaster m = new MetabolicRhythmMaster(getId(), getName(), getDescription(), getStartingPreprandialType(), getState(), new Instant(getStartDate()));
        return m;
    }

    public boolean isEqual(MetabolicRhythm m){
        boolean result;
        result = getId().intValue() == m.getId().intValue();
        result = result && getName().equals(m.getName());
        result = result && getDescription().equals(m.getDescription());
        result = result && getStartingPreprandialType().intValue() == m.getStartingPreprandialType().intValue();
        result = result && getState().intValue() == m.getState().intValue();

        if(getStartDate() == null){
            if (m.getStartDate() != null) {
                return false;
            }
        } else {
            if(m.getStartDate() == null){
                return false;
            } else {
                result = result && getStartDate().toDate().getTime() == m.getStartDate().toDate().getTime();
            }
        }

        return result;
    }

}
