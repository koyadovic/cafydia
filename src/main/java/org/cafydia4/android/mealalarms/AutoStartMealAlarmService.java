package org.cafydia4.android.mealalarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by user on 19/05/15.
 */
public class AutoStartMealAlarmService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent startServiceIntent = new Intent(context, SetMealAlarmsService.class);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set the alarm to start at approximately 5:00 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 5);

        PendingIntent alarmIntent = PendingIntent
                .getService(context, 0, startServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

    }
}
