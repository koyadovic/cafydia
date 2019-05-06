package org.cafydia.android.recommendations;

import org.cafydia.android.core.Instant;

/**
 * Created by user on 30/07/14.
 */
public class MetabolicRhythmSlave extends MetabolicRhythm {


    protected Instant endDate;

    public MetabolicRhythmSlave(Integer id, String name, String description, Integer startPreprandialType, Integer state, Instant startDate, Instant endDate){
        super(id, name, description, startPreprandialType, state, startDate);
        if(endDate != null){
            endDate.setTimeToTheEndOfTheDay();
        }
        this.endDate = endDate;
    }

    public boolean isScheduled(){
        return endDate != null && endDate.toDate().getTime() > 100000000;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        if(endDate != null){
            endDate.setTimeToTheEndOfTheDay();
        }
        this.endDate = endDate;
    }

    public boolean applies(){
        Instant i = new Instant();
        if(startDate != null){
            startDate.setTimeToTheStartOfTheDay();
        }
        if(endDate != null){
            endDate.setTimeToTheEndOfTheDay();
        }

        return startDate != null &&
                endDate != null &&
                i.getDaysPassedFromInstant(startDate) >= 0 &&
                i.getDaysPassedFromInstant(endDate) <= 0;
    }
    public boolean appliesInInstant(Instant i){
        if(startDate != null){
            startDate.setTimeToTheStartOfTheDay();
        }
        if(endDate != null){
            endDate.setTimeToTheEndOfTheDay();
        }

        return startDate != null &&
                endDate != null &&
                i.getDaysPassedFromInstant(startDate) >= 0 &&
                i.getDaysPassedFromInstant(endDate) <= 0;
    }

    public boolean isExpirableAndIsExpired(){
        Instant i = new Instant();
        if(startDate != null){
            startDate.setTimeToTheStartOfTheDay();
        }
        if(endDate != null){
            endDate.setTimeToTheEndOfTheDay();
        }

        return endDate != null && i.getDaysPassedFromInstant(endDate) > 0;
    }

    public boolean isExpiredInInstant(Instant i){
        if(startDate != null){
            startDate.setTimeToTheStartOfTheDay();
        }
        if(endDate != null){
            endDate.setTimeToTheEndOfTheDay();
        }

        return endDate != null && i.getDaysPassedFromInstant(endDate) > 0;
    }

    public MetabolicRhythmSlave duplicate(){
        return new MetabolicRhythmSlave(getId(), getName(), getDescription(), getStartingPreprandialType(), getState(), new Instant(getStartDate()), new Instant(getEndDate()));
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
        if(getEndDate() == null){
            if (((MetabolicRhythmSlave)m).getEndDate() != null) {
                return false;
            }
        } else {
            if(((MetabolicRhythmSlave)m).getEndDate() == null){
                return false;
            } else {
                result = result && getEndDate().toDate().getTime() == ((MetabolicRhythmSlave)m).getEndDate().toDate().getTime();
            }
        }

        return result;

    }
}
