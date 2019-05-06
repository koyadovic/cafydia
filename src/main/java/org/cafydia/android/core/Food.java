package org.cafydia.android.core;

import android.os.Parcel;
import android.os.Parcelable;

import org.cafydia.android.configdatabase.ConfigurationDatabase;
import org.cafydia.android.util.C;

/**
 * Created by usuario on 5/03/14.
 */
public class Food implements Parcelable {
    // atributes
    private Integer mId;
    private String mName;
    private Integer mType;
    private Integer mFavorite;
    private Float mCarbohydratePercent;
    private Float mWeight;

    private Float mUnitWeight;

    private Integer mBedcaId = 0;

    // constructor
    /**
     *
     * @param _id If the Food is a new Food added, id must be set to zero (0)
     * @param name name of food
     * @param type type of food
     * @param favorite true or false.
     * @param cPercent carbohydrate percentage.
     * @param unitWeight Weight for one unit of current food. (One cookie, etc)
     *
     */
    public Food(int _id, String name, int type, int favorite, Float cPercent, Float unitWeight){
        this.mId = _id;
        this.mName = name;
        this.mType = type;
        this.mFavorite = favorite;
        this.mCarbohydratePercent = cPercent;
        this.mWeight = 0.0f;
        this.mUnitWeight = unitWeight;
    }
    public Food(int _id, String name, int type, int favorite, Float cPercent){
        this.mId = _id;
        this.mName = name;
        this.mType = type;
        this.mFavorite = favorite;
        this.mCarbohydratePercent = cPercent;
        this.mWeight = 0.0f;
        this.mUnitWeight = 0.0f;
    }
    public Food(){
        this.mId = 0;
        this.mName = "";
        this.mType = C.FOOD_TYPE_SIMPLE;
        this.mFavorite = C.FOOD_FAVORITE_NO;
        this.mCarbohydratePercent = 0.0f;
        this.mWeight = 0.0f;
        this.mUnitWeight = 0.0f;
    }

    // geters & setters
    public Integer getId() {
        return mId;
    }
    public void setId(int _id) {
        this.mId = _id;
    }

    public String getName() {
        return mName;
    }
    public void setName(String mName) {
        this.mName = mName;
    }

    public Integer getType() {
        return mType;
    }

    public void setType(Integer mType) {
        this.mType = mType;
    }

    public Float getCPercent() {
        return mCarbohydratePercent;
    }
    public void setCPercent(Float cPercent) {
        this.mCarbohydratePercent = cPercent;
    }

    public Integer getFavorite() {
        return mFavorite;
    }

    public void setFavorite(int mFavorite) {
        this.mFavorite = mFavorite;
    }

    public Float getWeightInGrams() {
        return mWeight;
    }

    public void setWeight(Float mWeight) {
        this.mWeight = mWeight;
    }


    public Integer getBedcaId() {
        return mBedcaId;
    }

    public void setBedcaId(Integer bedcaId) {
        this.mBedcaId = bedcaId;
    }

    public Food (Parcel in) {
        String[] data = new String[6];
        in.readStringArray(data);
        this.mId = Integer.parseInt(data[0]);
        this.mName = data[1];
        this.mType = Integer.parseInt(data[2]);
        this.mFavorite = Integer.parseInt(data[3]);
        this.mCarbohydratePercent = Float.parseFloat(data[4]);
        this.mWeight = Float.parseFloat(data[5]);

    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeStringArray(new String[] {
                mId.toString(),
                mName,
                mType.toString(),
                mFavorite.toString(),
                mCarbohydratePercent.toString(),
                mWeight.toString()
        });
    }

    public static final Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel parcel) {
            return new Food(parcel);
        }

        @Override
        public Food[] newArray(int i) {
            return new Food[i];
        }
    };

    public String toString(){
        if(mWeight > 0){
            return mName + " (" + mWeight.toString() + "gr)";
        } else {
            return mName + " (" + mCarbohydratePercent.toString() + "%)";
        }
    }

    // to save and delete food onto database
    public void save(ConfigurationDatabase db){
        switch (getId()){
            case 0:
                mId = db.insertFood(this);
                break;
            default:
                db.updateFood(this);
        }
    }
    public void delete(ConfigurationDatabase db){
        db.deleteFood(this);
    }

    public Float getWeightPerUnitInGrams() {
        return mUnitWeight;
    }

    public void setUnitWeight(Float mUnitWeight) {
        this.mUnitWeight = mUnitWeight;
    }

}
