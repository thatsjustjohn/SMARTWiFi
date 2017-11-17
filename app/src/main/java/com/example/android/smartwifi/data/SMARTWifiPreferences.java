package com.example.android.smartwifi.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.example.android.smartwifi.R;
/**
 * Created by jtwyp6 on 10/28/17.
 * This Class is for the shared preferenes retrieval
 */

public final class SMARTWifiPreferences {

    public static boolean isThresholdEnabled(Context context) {

        String isThresholdEnabledKey = context.getString(R.string.pref_threshold_key);

        boolean isThresholdEnabledDefault = context
                .getResources()
                .getBoolean(R.bool.pref_threshold_default);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */
        boolean shouldThresholdEnabled = sp
                .getBoolean(isThresholdEnabledKey, isThresholdEnabledDefault);

        return shouldThresholdEnabled;
    }

    public static boolean isPriorityEnabled(Context context) {

        String isPriorityEnabledKey = context.getString(R.string.pref_priority_key);

        boolean isPriorityEnabledDefault = context
                .getResources()
                .getBoolean(R.bool.pref_priority_default);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */
        boolean shouldPriorityEnabled = sp
                .getBoolean(isPriorityEnabledKey, isPriorityEnabledDefault);

        return shouldPriorityEnabled;
    }

    public static boolean isGeoFenceEnabled(Context context) {

        String isGeoFenceEnabledKey = context.getString(R.string.pref_geo_fence_key);

        boolean isGeoFenceEnabledDefault = context
                .getResources()
                .getBoolean(R.bool.pref_geo_fence_default);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */
        boolean shouldGeoFenceEnabled = sp
                .getBoolean(isGeoFenceEnabledKey, isGeoFenceEnabledDefault);

        return shouldGeoFenceEnabled;
    }

    public static boolean isAccessPointEnabled(Context context) {

        String isAccessPointEnabledKey = context.getString(R.string.pref_access_point_key);

        boolean isAccessPointEnabledDefault = context
                .getResources()
                .getBoolean(R.bool.pref_access_point_default);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */
        boolean shouldAccessPointEnabled = sp
                .getBoolean(isAccessPointEnabledKey, isAccessPointEnabledDefault);

        return shouldAccessPointEnabled;
    }

    public static boolean isDataLoggingEnabled(Context context) {

        String isDataLogEnabledKey = context.getString(R.string.pref_data_log_key);

        boolean isDataLogEnabledDefault = context
                .getResources()
                .getBoolean(R.bool.pref_data_log_default);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */
        boolean shouldDataLoggingEnabled = sp
                .getBoolean(isDataLogEnabledKey, isDataLogEnabledDefault);

        return shouldDataLoggingEnabled;
    }


    public static int reconnectThreshold(Context context){
        String reconnectThresholdKey = context.getString(R.string.pref_threshold_connect_key);

        String reconnectThresholdDefault = context
                .getResources()
                .getString(R.string.pref_threshold_connect_default);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String reconnectThresholdValue = sp
                .getString(reconnectThresholdKey, reconnectThresholdDefault);

        return Integer.valueOf(reconnectThresholdValue);

    }

    public static int disconnectThreshold(Context context){
        String disconnectThresholdKey = context.getString(R.string.pref_threshold_disconnect_key);

        String disconnectThresholdDefault = context
                .getResources()
                .getString(R.string.pref_threshold_disconnect_default);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String disconnectThresholdValue = sp
                .getString(disconnectThresholdKey, disconnectThresholdDefault);

        return Integer.valueOf(disconnectThresholdValue);

    }
}
