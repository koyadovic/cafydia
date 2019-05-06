package org.cafydia.android.util;

/**
 * Created by user on 8/06/15.
 */


public class LinearFunction {
    private float m, b;

    public LinearFunction(float m, float b){
        this.m = m;
        this.b = b;
    }

    public float getY(float x){
        return (x * m) + b;
    }

    public float getX(float y){
        return (y - b) / m;
    }

    public static float getM(float minX, float maxX, float minY, float maxY){
        return (maxY - minY) / (maxX - minX);
    }

    public static float getB(float minX, float maxX, float minY, float maxY){
        return minY - (minX * getM(minX, maxX, minY, maxY));
    }
}
