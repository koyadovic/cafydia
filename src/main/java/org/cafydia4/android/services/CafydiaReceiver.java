package org.cafydia4.android.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import org.cafydia4.android.R;
import org.cafydia4.android.activities.ActivityMain;

/**
 * Created by user on 19/11/14.
 */
public class CafydiaReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent i){
        // notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
                .setSmallIcon(R.drawable.glucose)
                .setContentTitle(c.getString(R.string.dialog_meal_finished_alert_title))
                .setContentText(c.getString(R.string.dialog_meal_finished_alert_message));



        // default sound
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        // vibrate
        Vibrator vibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);

        // set onClick Action
        Intent intent = new Intent(c, ActivityMain.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
