package org.cafydia4.android.bedcasearch;

/**
 * Created by user on 11/05/15.
 */
public class BedcaFood {
    private Integer mBedcaId;
    private String mNameEn;
    private String mNameEs;
    private Float mCarbohydrates;
    private boolean mFromLocalDatabase;

    public BedcaFood(int bedcaId, String nameEn, String nameEs, float carbohydrates){
        mBedcaId = bedcaId;
        mNameEn = nameEn;
        mNameEs = nameEs;
        mCarbohydrates = carbohydrates;
    }

    public Float getCarbohydrates() {
        return mCarbohydrates;
    }

    public void setCarbohydrates(Float carbohydrates) {
        this.mCarbohydrates = carbohydrates;
    }

    public String getNameEs() {
        return mNameEs;
    }

    public void setNameEs(String nameEs) {
        this.mNameEs = nameEs;
    }

    public String getNameEn() {
        return mNameEn;
    }

    public void setNameEn(String nameEn) {
        this.mNameEn = nameEn;
    }

    public Integer getBedcaId() {
        return mBedcaId;
    }

    public void setBedcaId(Integer bedcaId) {
        this.mBedcaId = bedcaId;
    }

    public boolean isFromLocalDatabase() {
        return mFromLocalDatabase;
    }

    public void setFromLocalDatabase(boolean fromLocalDatabase) {
        this.mFromLocalDatabase = fromLocalDatabase;
    }

    //
    // To save and update the food in database
    //
    public void save(BedcaLocalDatabase db){
        db.updateOrInsertBedcaFood(this);
    }

    public void delete(BedcaLocalDatabase db){
        db.deleteBedcaFood(this);
    }
}
