package com.example.android.smartwifi.sync;

import android.content.Context;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

/**
 * Created by FailSafe on 10/24/2017.
 */

public class SMARTWifiSyncUtils {
    private static final int SYNC_INTERVAL_MINUTES = 1; //we will be doing seconds in the future
    private static final int SYNC_INTERVAL_SECONDS = 5; //(int) TimeUnit.MINUTES.toSeconds(SYNC_INTERVAL_MINUTES);
    private static final int SYNC_FLEXTIME_SECONDS = 1;  //this is how much give we allow it to be off. so 1 second

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
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(constraintSMARTWifiJob);
        sInitialized = true;
    }
}
