package org.cafydia4.android.chartobjects;

import org.cafydia4.android.core.Instant;
import org.cafydia4.android.datadatabase.DataDatabase;

/**
 * Created by user on 3/12/14.
 */
/////////////////////////////////////////////////////
// Class to specify a single date range object
/////////////////////////////////////////////////////
public class LabelRange {
    private int id;
    private int labelId;
    private Instant start;
    private Instant end;

    public LabelRange(int id, int labelId, Instant start, Instant end){
        this.id = id;
        this.labelId = labelId;
        this.start = start;
        this.end = end;
    }

    public LabelRange(int id, int labelId, long start, long end){
        this(id, labelId, new Instant(start), new Instant(end));
    }
    public LabelRange(int labelId, long start, long end){
        this(0, labelId, new Instant(start), new Instant(end));
    }
    public LabelRange(int labelId, Instant start, Instant end){
        this(0, labelId, start, end);
    }

    public Instant getStart() {
        return start;
    }
    public Instant getStartInTheMorning(){
        // 5:00 A.M.
        return new Instant(start).setTimeToTheMorning();
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Instant getEnd() {
        return end;
    }
    public Instant getEndIncreasedOneDayInTheMorning(){
        // 5:00 A.M. of the next day
        return new Instant(end).increaseOneDay().setTimeToTheMorning();
    }

    public void setEnd(Instant end) {
        this.end = end;
    }

    public Integer getId() {
        return id;
    }

    public Integer getLabelId() {
        return labelId;
    }

    public boolean applies(Instant i){
        return i != null &&
                i.getDaysPassedFromInstant(start) >= 0 &&
                i.getDaysPassedFromInstant(end) <= 0;
    }

    public Float getDaysPassedBetweenStartAndEnd(){
        return getStartInTheMorning().getDaysPassedFromInstant(new Instant(getEnd()).setTimeToTheMorning());
    }

    public void save(DataDatabase db){
        if(id == 0) {
            db.insertLabelRange(this);
        } else {
            db.updateLabelRange(this);
        }
    }

    public void delete(DataDatabase db){
        db.deleteLabelRange(this);
    }

}
