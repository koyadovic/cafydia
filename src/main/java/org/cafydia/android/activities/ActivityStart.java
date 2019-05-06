package org.cafydia.android.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import org.cafydia.android.R;
import org.cafydia.android.initialconfiguration.InitialConfigurationActivity;
import org.cafydia.android.mealalarms.SetMealAlarmsService;

import java.util.Calendar;

/**
 * Created by user on 1/05/15.
 */
public class ActivityStart extends Activity {

    private boolean mInitialConfigurationDone;

    private void setOrientation(){
        if (getResources().getBoolean(R.bool.phone)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_start);

        Context c = getApplicationContext();
        Intent i = new Intent(c, SetMealAlarmsService.class);

        PendingIntent alarmIntent = PendingIntent
                .getService(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_NO_CREATE);

        boolean alarmUp = alarmIntent != null;

        AlarmManager alarmMgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        if (alarmUp) {
            Log.d("CafAlarm", "ActivityStart - onCreate() - Alarm is already active, cancelling");

        } else {
            alarmIntent = PendingIntent
                    .getService(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

            Log.d("CafAlarm", "ActivityStart - onCreate() - Alarm is not active, activating");

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 5);

            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);
        }


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mInitialConfigurationDone = sp.getBoolean(InitialConfigurationActivity.INITIAL_CONFIG_TAG, false);

        final ImageView logo = (ImageView) findViewById(R.id.logo);
        final ImageView logoBg = (ImageView) findViewById(R.id.logoBg);

        logo.setVisibility(View.INVISIBLE);
        logoBg.setVisibility(View.INVISIBLE);

        final Animation alpha = new AlphaAnimation(0f, 1f);
        final Animation alphaBg = new AlphaAnimation(0f, 1f);

        alpha.setInterpolator(new LinearInterpolator());
        alpha.setDuration(300);

        alphaBg.setInterpolator(new AccelerateInterpolator());
        alphaBg.setDuration(1500);

        alphaBg.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                logo.setVisibility(View.VISIBLE);
                logoBg.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent i;

                if (mInitialConfigurationDone) {
                    i = new Intent(ActivityStart.this, ActivityMain.class);
                } else {
                    i = new Intent(ActivityStart.this, InitialConfigurationActivity.class);
                }

                startActivity(i);
                finish();          }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                logo.startAnimation(alpha);
                logoBg.startAnimation(alphaBg);
            }
        }, 300);


    }
}
