package org.cafydia.android.recommendations;

import java.util.ArrayList;

/**
 * Created by user on 31/07/14.
 */
public class ModificationStart {
    private ArrayList<ModificationStartDot> mDots;
    private boolean mSorted = false;

    public ModificationStart(){
        mDots = new ArrayList<ModificationStartDot>();
    }

    public float getModification(float x) {
        // x: days from which ModificationByPeriod has become active. 0 is the first day.

        // first we sort the dots by the X axis from minor to max.
        if(!mSorted) {
            sortDots();
        }

        if(mDots.size() == 0 || x < mDots.get(0).getX()){
            return 0.0f;
        }
        else if(x > mDots.get(mDots.size() - 1).getX()) {
            return mDots.get(mDots.size() - 1).getY();
        }

        // Lagrange polynomial interpolation
        double numerator;
        double denominator;

        float modification = 0.0f;

        int c = mDots.size();

        for(int i = 0; i < c; i++) {
            numerator = 1.0;
            denominator = 1.0;

            for(int j = 0; j < c; j++){
                if(j == i) continue;
                numerator *= (x - mDots.get(j).getX());
                denominator *= (mDots.get(i).getX() - mDots.get(j).getX());
            }

            modification += mDots.get(i).getY() * (numerator / denominator);
        }

        return modification;
    }
    public ModificationStartDot getFirstDot(){
        if(!mSorted){
            sortDots();
        }

        return mDots.size() > 0 ? mDots.get(0) : null;
    }

    public void addDot(ModificationStartDot dot){
        mDots.add(dot);
        mSorted = false;
    }

    /*
     * Sort dots by X
     */
    private void sortDots(){
        int s = mDots.size();
    
        if(s > 0) {

            ArrayList<ModificationStartDot> auxDots = new ArrayList<ModificationStartDot>();
            for(ModificationStartDot dot : mDots){
                auxDots.add(dot);
            }

            int c;
            
            do {
                c = 0;
                for (int i = 0; i < s - 1; i++) {

                    if (auxDots.get(i).getX() > auxDots.get(i + 1).getX()) {

                        ModificationStartDot minDot = auxDots.get(i + 1);
                        ModificationStartDot maxDot = auxDots.get(i);

                        auxDots.set(i, minDot);
                        auxDots.set(i + 1, maxDot);

                    } else {
                        c++;
                    }
                }
            } while (c < s - 1);

            mDots = auxDots;
        }
    }

    public ModificationStartDot getMaxModificationDot(){
        ModificationStartDot resultDot = null;
        for (ModificationStartDot dot : mDots){
            if(resultDot == null || dot.getY() > resultDot.getY()){
                resultDot = dot;
            }
        }
        return resultDot;
    }

    public ArrayList<ModificationStartDot> getDots(){
        if(mDots == null) {
            mDots = new ArrayList<ModificationStartDot>();
        } else {
            sortDots();
        }
        return mDots;
    }
}
