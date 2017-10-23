package com.example.android.smartwifi.sync;

import android.content.Context;
import android.util.Log;

/**
 * Created by jtwyp6 on 10/21/17.
 */

public class SMARTWifiSyncTask {
    public static final String ACTION_WIFI_THRESHOLD = "wifi-threshold";

    public static void executeTask(Context context, String action) {
        if (ACTION_WIFI_THRESHOLD.equals(action)) {
            handleWifiThreshold(context);
        }
    }
    private static void handleWifiThreshold(Context context) {
        //PreferenceUtilities.incrementWaterCount(context);
        Log.d("LETS GET TO WORK DAWG", "we doing background shit meow");

    }
}
