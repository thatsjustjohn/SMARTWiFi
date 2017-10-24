package com.example.android.smartwifi.sync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

/**
 * Created by FailSafe on 10/24/2017.
 */

public class SMARTWifiSyncUtils {
    private static final int SYNC_INTERVAL_SECONDS = 0; //(int) TimeUnit.MINUTES.toSeconds(SYNC_INTERVAL_MINUTES);
    private static final int SYNC_FLEXTIME_SECONDS = 0;  //this is how much give we allow it to be off. so 1 second

    private static final String SMART_WIFI_TAG = "SMARTWIFI_job_tag";

    private static boolean sInitialized;

    synchronized  public static void scheduleSMARTWifi(@NonNull final Context context){
        if(sInitialized) return;
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job constraintSMARTWifiJob = dispatcher.newJobBuilder()
                .setService(SMARTWifiFirebasejobService.class)
                .setTag(SMART_WIFI_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        0,
                        0))
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(constraintSMARTWifiJob);
        Log.d("start", "START DAT FUCKING JOB");
        sInitialized = true;
    }
}
