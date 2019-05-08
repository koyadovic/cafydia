package org.cafydia4.android.mealalarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import org.cafydia4.android.core.Instant;
import org.cafydia4.android.util.Averages;
import org.cafydia4.android.util.C;

/**
 * Created by user on 19/05/15.
 *
 * será ejecutado nada más arrancar el teléfono
 * también tendría que ser ejecutado a una vez al día
 *
 * su cometido será el de configurar las alarmas que sonarán a 10 minutos de acabarse los rangos de comida
 *
 */
public class SetMealAlarmsService extends Service {
    private Handler mHandler = new Handler();
    private Averages mAverages;
    private Context mContext;

    @Override
    public IBinder onBind(Intent i){
        return null;
    }

    @Override
    public void onCreate(){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mContext = getApplicationContext();

                mAverages = new Averages(mContext, new Averages.OnAveragesCalculatedListener() {
                    @Override
                    public void onAveragesCalculated() {
                        mHandler.post(mServiceRunnable);
                    }
                });

            }
        });
    }

    private Runnable mServiceRunnable = new Runnable() {
        @Override
        public void run() {
            // tenemos que setear las alarmas para el día y parar el servicio
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            int minutesOfRange = Integer.parseInt(sp.getString("pref_meal_hours_choose_minutes_of_range", "60"));
            int halfOfRange = minutesOfRange / 2;


            Intent intentBr = new Intent(mContext, MealAlarmReceiver.class);
            Intent intentLu = new Intent(mContext, MealAlarmReceiver.class);
            Intent intentDi = new Intent(mContext, MealAlarmReceiver.class);

            intentBr.putExtra("meal", C.MEAL_BREAKFAST);
            intentLu.putExtra("meal", C.MEAL_LUNCH);
            intentDi.putExtra("meal", C.MEAL_DINNER);

            PendingIntent pIntentBr = PendingIntent.getBroadcast(mContext, 11, intentBr, 0);
            PendingIntent pIntentLu = PendingIntent.getBroadcast(mContext, 12, intentLu, 0);
            PendingIntent pIntentDi = PendingIntent.getBroadcast(mContext, 13, intentDi, 0);

            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

            Float avBr = mAverages.getAvMinutesPassedFromMidnightBreakfast();
            Float avLu = mAverages.getAvMinutesPassedFromMidnightLunch();
            Float avDi = mAverages.getAvMinutesPassedFromMidnightDinner();

            Instant i = new Instant();

            long n = (halfOfRange * 60 * 1000) - (10 * 60 * 1000); // 10 minutos antes de terminar

            long triggerBr, triggerLu, triggerDi;

            if(avBr != null) {
                triggerBr = i.setTimeToTheStartOfTheDay().increaseNMinutes(avBr.intValue()).toDate().getTime();
                triggerBr += Math.abs(n);

                if(new Instant(triggerBr).getDaysPassedFromNow() >= 0) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerBr, pIntentBr);
                }

            }

            if(avLu != null) {
                triggerLu = i.setTimeToTheStartOfTheDay().increaseNMinutes(avLu.intValue()).toDate().getTime();
                triggerLu += Math.abs(n);

                if(new Instant(triggerLu).getDaysPassedFromNow() >= 0) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerLu, pIntentLu);
                }

            }

            if(avDi != null) {
                triggerDi = i.setTimeToTheStartOfTheDay().increaseNMinutes(avDi.intValue()).toDate().getTime();
                triggerDi += Math.abs(n);

                if(new Instant(triggerDi).getDaysPassedFromNow() >= 0) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerDi, pIntentDi);
                }

            }

            SetMealAlarmsService.this.stopSelf();

        }
    };
}
