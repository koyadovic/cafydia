package org.cafydia4.android.core;

import org.cafydia4.android.datadatabase.DataDatabase;

/**
 * Created by usuario on 2/03/14.
 */
public class GlucoseTest extends Instant {
    // attributes
    private Integer mLevel;  // glucose level
    private Integer mId; // database's ID of the object
    private Integer mTime; // before dinner, after breakfast, and so on
    private Integer mMetabolicRhythmId;

    // constructors

    // for now
    public GlucoseTest(Integer _id, Integer time, Integer level, int metabolicRhythmId) {
        super();
        this.mId = _id;
        this.mTime = time;
        this.mLevel = level;
        this.mMetabolicRhythmId = metabolicRhythmId;
    }

    // for past glucose tests
    public GlucoseTest(int _id, String dateString, int time, int level, int metabolicRhythmId) {
        super(dateString);
        this.mId = _id;
        this.mLevel = level;
        this.mTime = time;
        this.mMetabolicRhythmId = metabolicRhythmId;
    }

    // getters and setters
    public Integer getGlucoseLevel() {
        return mLevel;
    }

    public void setGlucoseLevel(Integer level) {
        this.mLevel = level;
    }

    public Integer getGlucoseId() {
        return mId;
    }
    public void setGlucoseId(int _id) {
        this.mId = _id;
    }

    public Integer getGlucoseTime() {
        return mTime;
    }

    public void setGlucoseTime(Integer time) {
        this.mTime = time;
    }

    public Integer getMetabolicRhythmId() {
        return mMetabolicRhythmId;
    }

    public void setMetabolicRhythmId(Integer mMetabolicRhythmId) {
        this.mMetabolicRhythmId = mMetabolicRhythmId;
    }

    // to save and delete glucose tests onto database
    public void save(DataDatabase db){
        switch (mId){
            case 0:
                db.insertGlucoseTest(this);
                break;
            default:
                db.updateGlucoseText(this);
        }

    }
    public void delete(DataDatabase db){
        db.deleteGlucoseTest(this);
    }

}
