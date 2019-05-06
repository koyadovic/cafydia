package org.cafydia.android.application;

import android.app.Application;
import android.content.Intent;

/**
 * Created by user on 7/04/15.
 */
public class CafydiaDiabetes extends Application {
    public void onCreate () {
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException (Thread thread, Throwable e) {
                handleUncaughtException (thread, e);
            }
        });
    }

    public void handleUncaughtException (Thread thread, Throwable e) {
        e.printStackTrace();

        Intent intent = new Intent ();
        intent.setAction (".activities.SEND_CRASH");
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity (intent);

        System.exit(1);
    }
}
