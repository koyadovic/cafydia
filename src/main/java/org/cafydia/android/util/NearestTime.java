package org.cafydia.android.util;

import org.cafydia.android.core.Instant;

/**
 * Created by user on 1/07/14.
 *
 * returns 0 for breakfast, 1 for lunch, 2 for dinner
 */

public class NearestTime {
    private static float HOUR_BREAKFAST;
    private static final float HOUR_LUNCH = 14;
    private static final float HOUR_DINNER = 21;

    private static final int SELECT_BREAKFAST = 0;
    private static final int SELECT_LUNCH = 1;
    private static final int SELECT_DINNER = 2;

    private static float HOUR_SNACK_AFTER_BREAKFAST;
    private static final float HOUR_SNACK_AFTER_LUNCH = 17;
    private static final float HOUR_SNACK_BEFORE_BED = 23.5f;

    private static Integer selection;

    private static void refresh(){
        HOUR_BREAKFAST = 7;
        HOUR_SNACK_AFTER_BREAKFAST = 11.5f;
    }

    public static int getNearestMeal(){
        return getNearestMeal(new Instant());
    }

    public static int getNearestMeal(Instant instant){
        refresh();

        String[] parts = instant.getUserTimeString().split(":");
        float hour = Float.parseFloat(parts[0]);

        if(hour < HOUR_BREAKFAST) {
            hour += 24;
            HOUR_BREAKFAST += 24;
        }

        float nearBr = Math.abs(hour - HOUR_BREAKFAST);
        float nearLu = Math.abs(hour - HOUR_LUNCH);
        float nearDi = Math.abs(hour - HOUR_DINNER);

        if(nearBr < nearLu) {
            if(nearBr < nearDi) {
                selection = SELECT_BREAKFAST;
            } else {
                selection = SELECT_DINNER;
            }
        } else if (nearLu < nearDi) {
            selection = SELECT_LUNCH;
        } else {
            selection = SELECT_DINNER;
        }
        return selection;
    }

    public static int getNearestSnack(){
        refresh();

        Instant instant = new Instant();
        String[] parts = instant.getUserTimeString().split(":");
        int hour = Integer.parseInt(parts[0]);

        if(hour < HOUR_SNACK_AFTER_BREAKFAST) {
            hour += 24;
            HOUR_SNACK_AFTER_BREAKFAST += 24;
        }

        float nearBr = Math.abs(hour - HOUR_SNACK_AFTER_BREAKFAST);
        float nearLu = Math.abs(hour - HOUR_SNACK_AFTER_LUNCH);
        float nearDi = Math.abs(hour - HOUR_SNACK_BEFORE_BED);

        if(nearBr < nearLu) {
            if(nearBr < nearDi) {
                selection = SELECT_BREAKFAST;
            } else {
                selection = SELECT_DINNER;
            }
        } else if (nearLu < nearDi) {
            selection = SELECT_LUNCH;
        } else {
            selection = SELECT_DINNER;
        }
        return selection;

    }

    public static boolean isSomeMealNearestThanSomeSnack(){
        refresh();

        Instant instant = new Instant();
        String[] parts = instant.getUserTimeString().split(":");
        float hourMeal = Float.parseFloat(parts[0]);

        if(hourMeal < HOUR_BREAKFAST) {
            hourMeal += 24;
            HOUR_BREAKFAST += 24;
        }

        float hourSnack = Float.parseFloat(parts[0]);

        if(hourSnack < HOUR_SNACK_AFTER_BREAKFAST) {
            hourSnack += 24;
            HOUR_SNACK_AFTER_BREAKFAST += 24;
        }

        float nearBr = Math.abs(hourMeal - HOUR_BREAKFAST);
        float nearLu = Math.abs(hourMeal - HOUR_LUNCH);
        float nearDi = Math.abs(hourMeal - HOUR_DINNER);
        float nearABr = Math.abs(hourSnack - HOUR_SNACK_AFTER_BREAKFAST);
        float nearALu = Math.abs(hourSnack - HOUR_SNACK_AFTER_LUNCH);
        float nearBed = Math.abs(hourSnack - HOUR_SNACK_BEFORE_BED);

        float nearestMeal;
        float nearestSnack;

        if(nearBr < nearLu) {
            if(nearBr < nearDi) {
                nearestMeal = nearBr;
            } else {
                nearestMeal = nearDi;
            }
        } else if (nearLu < nearDi) {
            nearestMeal = nearLu;
        } else {
            nearestMeal = nearDi;
        }

        if(nearABr < nearALu) {
            if(nearABr < nearBed) {
                nearestSnack = nearABr;
            } else {
                nearestSnack = nearBed;
            }
        } else if (nearALu < nearBed) {
            nearestSnack = nearALu;
        } else {
            nearestSnack = nearBed;
        }

        return nearestMeal <= nearestSnack;
    }

}
