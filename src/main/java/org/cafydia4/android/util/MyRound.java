package org.cafydia4.android.util;

import java.math.BigDecimal;

/**
 * Created by user on 30/06/14.
 */
public class MyRound {
    private static final int NUMBER_OF_DECIMALS = 1;


    public static Float round(Float f){
        return Float.parseFloat(new BigDecimal(f).setScale(NUMBER_OF_DECIMALS, BigDecimal.ROUND_HALF_UP).toString());
    }

    public static Integer roundToInteger(Float d){
        return Integer.valueOf(new BigDecimal(d).setScale(0, BigDecimal.ROUND_HALF_UP).toString());
    }

    public static Float round(Float f, int decimals){
        return Float.parseFloat(new BigDecimal(f).setScale(decimals, BigDecimal.ROUND_HALF_UP).toString());
    }

}
