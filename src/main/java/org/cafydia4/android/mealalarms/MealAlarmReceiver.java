package org.cafydia4.android.mealalarms;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import org.cafydia4.android.R;
import org.cafydia4.android.activities.ActivityMealsSnacks;
import org.cafydia4.android.core.Meal;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.util.C;

/**
 * Created by user on 19/05/15.
 */
public class MealAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent i){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);

        if(sp.getBoolean("pref_meal_hours_notify_ten_minutes_before_limit", true)) {

            NotificationCompat.Builder builder;
            DataDatabase db = new DataDatabase(c);

            // notification
            int m = i.getIntExtra("meal", -1);
            Meal lastMeal;

            switch (m) {
                case C.MEAL_BREAKFAST:
                    lastMeal = db.getLastMealAdded(C.MEAL_BREAKFAST);

                    if(lastMeal != null && lastMeal.isToday())
                        return;

                    builder = new NotificationCompat.Builder(c)
                            .setSmallIcon(R.drawable.ic_main_menu_meals)
                            .setContentTitle(c.getString(R.string.meal_hour_limit_exceeded_title_br))
                            .setContentText(c.getString(R.string.meal_hour_limit_exceeded_message));
                    break;

                case C.MEAL_LUNCH:
                    lastMeal = db.getLastMealAdded(C.MEAL_LUNCH);

                    if(lastMeal != null && lastMeal.isToday())
                        return;

                    builder = new NotificationCompat.Builder(c)
                            .setSmallIcon(R.drawable.ic_main_menu_meals)
                            .setContentTitle(c.getString(R.string.meal_hour_limit_exceeded_title_lu))
                            .setContentText(c.getString(R.string.meal_hour_limit_exceeded_message));
                    break;

                case C.MEAL_DINNER:
                    lastMeal = db.getLastMealAdded(C.MEAL_DINNER);

                    if(lastMeal != null && lastMeal.isToday())
                        return;

                    builder = new NotificationCompat.Builder(c)
                            .setSmallIcon(R.drawable.ic_main_menu_meals)
                            .setContentTitle(c.getString(R.string.meal_hour_limit_exceeded_title_di))
                            .setContentText(c.getString(R.string.meal_hour_limit_exceeded_message));
                    break;

                default:
                    return;
            }

            // default sound
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(alarmSound);

            // vibrate
            Vibrator vibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);

            // set onClick Action
            Intent intent = new Intent(c, ActivityMealsSnacks.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(c, m, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            // on onClick remove itself from notification area
            builder.setAutoCancel(true);

            // shows the notification
            NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, builder.build());

            // vibrate
            vibrator.vibrate(2000);

        }

    }
}
