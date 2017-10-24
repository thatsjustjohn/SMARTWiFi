package com.example.android.smartwifi.sync;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.example.android.smartwifi.utilities.NotificationUtils;
import com.example.android.smartwifi.utilities.WifiGeoUtils;

/**
 * Created by jtwyp6 on 10/21/17.
 */

public class SMARTWifiSyncTask {
    public static final String ACTION_WIFI_THRESHOLD = "wifi-threshold";
    public static final String ACTION_DISMISS_PRIORITY_WIFI_THRESHOLD = "priority-wifi-dismiss-threshold";
    public static final String ACTION_SWITCH_PRIORITY_WIFI_THRESHOLD = "priority-wifi-switch-threshold";
    public static final String ACTION_WIFI_ON = "wifi-on";
    public static WifiGeoUtils wifiGeoUtils;

    public static void executeTask(Context context, String action) {
        if (ACTION_SWITCH_PRIORITY_WIFI_THRESHOLD.equals(action)) {
            handleWifiThreshold(context);
        }else if (ACTION_DISMISS_PRIORITY_WIFI_THRESHOLD.equals(action)){
            NotificationUtils.clearAllNotifications(context);
        }else if (ACTION_WIFI_ON.equals(action)){
            try {
                    startMonitoringBecauseWifiOn(context);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void startMonitoringBecauseWifiOn(Context context) throws InterruptedException {
        wifiGeoUtils = new WifiGeoUtils(context);
        while(wifiGeoUtils.wifiManager.isWifiEnabled()){
            Log.d("MAIN TASK", "While Enabled");
            Thread.sleep(1000);
        }
        //while wifi is enable run code here.
    }


    private static void handleWifiThreshold(Context context) {
        //PreferenceUtilities.incrementWaterCount(context);
        Log.d("LETS GET TO WORK DAWG", "we doing background shit meow");
        NotificationUtils.clearAllNotifications(context);
    }
}
