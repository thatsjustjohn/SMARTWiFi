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
        GeofenceUtils.getInstance().loadGeoFencesFromDB(context);
        GeofenceUtils.getInstance().getGeofences();


        //SETUP PREFERENCES
        boolean wasGeoLocation = pref_geolocation;

        //Delay for priority
        int priorityCount = 0;
        updatedSharedPreferences(context);

        //IF ANY OF THE OPTIONS ARE ACTIVE
        if(pref_priority || pref_datalogging || pref_geolocation || pref_threshold) {

            //CREATE WIFI GEO UTILS
            wifiGeoUtils = new WifiGeoUtils(context);


            //if GEOLOCATION IS ACTIVE...START (it doesn't require wifienabled)
            while (!wifiGeoUtils.wifiManager.isWifiEnabled()){
                if(pref_geolocation){
                    wifiGeoUtils.geoOnStart();
                    Log.d("GEO ENABLED", String.valueOf(pref_geolocation));
                    if(!wifiGeoUtils.registeredGeo) wifiGeoUtils.registerGeofences();
                }else{
                    Log.d("GEO DISABLED", String.valueOf(pref_geolocation));
                    wifiGeoUtils.geoOnStop();
                }
            }

            //IF WIFI IS ENABLED...
            while (wifiGeoUtils.wifiManager.isWifiEnabled()) {
                //register/update geo fences if they aren't registered.
                updatedSharedPreferences(context);
                wifiGeoUtils.initSLupdateSharedPreferences();

                //IF GEOLOCATION IS ON
                if(pref_geolocation){
                    wifiGeoUtils.geoOnStart();
                    Log.d("GEO ENABLED", String.valueOf(pref_geolocation));
                    if(!wifiGeoUtils.registeredGeo) wifiGeoUtils.registerGeofences();
                }else{
                    Log.d("GEO DISABLED", String.valueOf(pref_geolocation));
                    wifiGeoUtils.geoOnStop();
                }
                //IF THRESHOLD
                if (pref_threshold) {
                    Log.d("THRESHOLD", "Threshold");
                    wifiGeoUtils.thresholdMonitor();

                }
                //IF PRIORITY
                if(pref_priority && priorityCount == 10){
                    Log.d("PRIORITY", String.valueOf(priorityCount));
                    priorityCount = 0;
                    wifiGeoUtils.searchNetworks();
                }
                //IF DATALOGGING
                if(pref_datalogging){
                    Log.d("DATALOG", "ON");
                    wifiGeoUtils.startLocationTracking();
                    //Log data to file
                    //temp until on location change is verified
                    wifiGeoUtils.writeDataLogging();
                }else{
                    Log.d("DATALOG", "OFF");
                    wifiGeoUtils.stopLocationTracking();
                }

                //CHECK PRIORITY EVERY 10 seconds
                priorityCount += 1;
                Thread.sleep(1000);
            }
        }
        //Disable receiver
        wifiGeoUtils.geoOnStop();
    }

    private static void updatedSharedPreferences(Context context) {
        pref_threshold = SMARTWifiPreferences.isThresholdEnabled(context);
        pref_geolocation = SMARTWifiPreferences.isGeoFenceEnabled(context);
        pref_datalogging = SMARTWifiPreferences.isDataLoggingEnabled(context);
        pref_priority = SMARTWifiPreferences.isPriorityEnabled(context);
    }


    private static void handleWifiThreshold(Context context) {
        NotificationUtils.clearAllNotifications(context);
    }

}
