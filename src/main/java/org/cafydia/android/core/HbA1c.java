package org.cafydia.android.core;

import org.cafydia.android.datadatabase.DataDatabase;

/**
 * Created by user on 12/10/14.
 */
public class HbA1c extends Instant {
    private Integer id;
    private Integer metabolicRhythmId;
    private String metabolicRhythmName;
    private Float percentage;
    private Float mmolMol;

    public HbA1c(Integer id, String dateString, Integer metabolicRhythmId, String metabolicRhythmName, Float percentage, Float mmolMol) {
        super(dateString);
        this.id = id;
        this.metabolicRhythmId = metabolicRhythmId;
        this.metabolicRhythmName = metabolicRhythmName;
        this.percentage = percentage;
        this.mmolMol = mmolMol;
    }
    public HbA1c(Integer metabolicRhythmId, String metabolicRhythmName) {
        super();
        this.id = 0;
        this.metabolicRhythmId = metabolicRhythmId;
        this.metabolicRhythmName = metabolicRhythmName;
        this.percentage = 0f;
        this.mmolMol = 0f;
    }

    // getters
    public Integer getId() {
        return id;
    }

    public Integer getMetabolicRhythmId() {
        return metabolicRhythmId;
    }

    public String getMetabolicRhythmName() {
        return metabolicRhythmName;
    }

    public Float getPercentage() {
        return percentage;
    }

    public Float getMmolMol() {
        return percentage != 0 ? mmolMol : 0f;
    }

    // setters
    public void setMetabolicRhythmId(Integer metabolicRhythmId) {
        this.metabolicRhythmId = metabolicRhythmId;
    }

    public void setMetabolicRhythmName(String metabolicRhythmName) {
        this.metabolicRhythmName = metabolicRhythmName;
    }

    public void setPercentage(Float percentage) {
        this.percentage = percentage;
        this.mmolMol = percentToMol(percentage);
    }

    public void setMmolMol(Float mmolMol) {
        this.mmolMol = mmolMol;
        this.percentage = molToPercent(mmolMol);
    }

    public static float percentToMol(float percent){
        return (percent - 2.15f) * 10.929f;
    }

    public static float molToPercent(float mol){
        return (mol / 10.929f) + 2.15f;
    }

    public void save(DataDatabase db){
        switch (id){
            case 0:
                db.insertHbA1c(this);
                break;
            default:
                db.updateHbA1c(this);

        }
    }
    public void delete(DataDatabase db){
        db.deleteHbA1c(this);
    }
}
