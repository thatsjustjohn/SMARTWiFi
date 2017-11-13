package com.example.android.smartwifi.sync;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.util.Log;

import com.example.android.smartwifi.data.SMARTWifiPreferences;
import com.example.android.smartwifi.utilities.GeofenceUtils;
import com.example.android.smartwifi.utilities.NotificationUtils;
import com.example.android.smartwifi.utilities.WifiGeoUtils;

/**
 * Created by jtwyp6 on 10/21/17.
 */

public class SMARTWifiSyncTask{
    public static final String ACTION_WIFI_THRESHOLD = "wifi-threshold";
    public static final String ACTION_DISMISS_PRIORITY_WIFI_THRESHOLD = "priority-wifi-dismiss-threshold";
    public static final String ACTION_SWITCH_PRIORITY_WIFI_THRESHOLD = "priority-wifi-switch-threshold";
    public static final String ACTION_WIFI_ON = "wifi-on";
    public static WifiGeoUtils wifiGeoUtils;
    public static boolean pref_threshold;
    public static boolean pref_geolocation;
    public static boolean pref_datalogging;
    public static boolean pref_priority;


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

        //Monitoring

        //TEST DB ACCESS
        Log.d("DB ACCESS", "BALLS");
        GeofenceUtils.getInstance().loadGeoFencesFromDB(context);
        GeofenceUtils.getInstance().getGeofences();


        //SETUP PREFERENCES
        pref_threshold = SMARTWifiPreferences.isThresholdEnabled(context);
        pref_geolocation = SMARTWifiPreferences.isGeoFenceEnabled(context);
        pref_datalogging = SMARTWifiPreferences.isDataLoggingEnabled(context);
        pref_priority = SMARTWifiPreferences.isPriorityEnabled(context);

        if(pref_priority || pref_datalogging || pref_geolocation || pref_threshold) {

                //CREATE WIFI GEO UTILS
                 wifiGeoUtils = new WifiGeoUtils(context);

                //if wifi is on, start location tracking
                //if data logging is on

            //if (wifiGeoUtils.wifiManager.isWifiEnabled())
            //        wifiGeoUtils.startLocationTracking();

            // START GEO FENCING
            if (pref_geolocation) {
                    wifiGeoUtils.geoOnStart();
                    Log.d("GEO ENABLED", String.valueOf(pref_geolocation));
                    if(!wifiGeoUtils.registeredGeo) wifiGeoUtils.registerGeofences();
            }

            while (wifiGeoUtils.wifiManager.isWifiEnabled()) {
                //register/update geo fences if they aren't registered.
                if(!wifiGeoUtils.registeredGeo){
                    Log.d("MAIN TASK", "Trying to register second");
                    wifiGeoUtils.registerGeofences();
                }
                WifiInfo wifiInfo = wifiGeoUtils.getConnectionInfo();
                Log.d("MAIN TASK", wifiInfo.toString());

                if (pref_threshold) {
                    Log.d("THRESHOLD / PRIORITY", wifiInfo.toString());
                    wifiGeoUtils.thresholdMonitor();

                }
                if(pref_priority){
                    Log.d("PRIORITY", wifiInfo.toString());
                    wifiGeoUtils.searchNetworks();
                }
                Thread.sleep(1000);
            }

            //after wifi is disabled stop tracking
            //wifiGeoUtils.stopLocationTracking();
            wifiGeoUtils.geoOnStop();
            //while wifi is enable run code here.
        }
    }


    private static void handleWifiThreshold(Context context) {
        Log.d("LETS GET TO WORK DAWG", "we doing background shit meow");
        NotificationUtils.clearAllNotifications(context);
    }

}
