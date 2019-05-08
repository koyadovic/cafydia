package org.cafydia4.android.recommendations;

import org.cafydia4.android.configdatabase.ConfigurationDatabase;

/**
 * Created by user on 6/08/14.
 */
public class ModificationStartDot {
    // atributes
    Integer id;
    Integer type;
    Integer metabolicRhythmId;
    private float x;
    private float y;

    public ModificationStartDot(float x, float y){
        this.id = -1;
        this.metabolicRhythmId = -1;
        this.x = x;
        this.y = y;
    }

    public ModificationStartDot(int id, int type, int metabolicRhythmId, float x, float y){
        this.id = id;
        this.type = type;
        this.metabolicRhythmId = metabolicRhythmId;

        this.x = x;
        this.y = y;
    }

    // getters and setters
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Integer getMetabolicRhythmId() {
        return metabolicRhythmId;
    }

    public void setMetabolicRhythmId(int metabolicRhythmId) {
        this.metabolicRhythmId = metabolicRhythmId;
    }

    public void save(ConfigurationDatabase db){
        switch (id){
            case 0:
                db.insertDot(this);
                break;
            default:
                db.updateDot(this);
        }
    }
    public void delete(ConfigurationDatabase db){
        db.deleteDot(this);
    }
}
