package com.example.android.smartwifi.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Collections;
import java.util.List;

/**
 * Created by jtwyp6 on 10/21/17.
 */

public class WifiGeoUtils {
    //Managers
    private WifiManager wifiManager;
    private LocationManager locationManager;

    //Context
    private Context context;

    //Data
    private String wifis[];
    private WifiInfo wifiInfo;
    private List<ScanResult> wifiScanList;


    public WifiGeoUtils(@NonNull WifiManager wifiManager, @NonNull LocationManager locationManager, @NonNull Context context){
        this.wifiManager = wifiManager;
        this.locationManager = locationManager;
        this.context = context;
    }

    public void itializeWifi(){
        boolean isWiFiEnabled = false;
        try {
            isWiFiEnabled = wifiManager.isWifiEnabled();
            WifiInfo info = wifiManager.getConnectionInfo();
            if (!isWiFiEnabled) {
                Log.d("NOT ENABLED", String.valueOf(isWiFiEnabled));
            } else {
                Log.d("ENABLED", String.valueOf(isWiFiEnabled));
                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        int size;
                        wifiScanList = wifiManager.getScanResults();
                        size = wifiScanList.size();
                        wifis = new String[wifiScanList.size()];
                        Log.d("onRecieve WIFIGEO", String.valueOf(size) + " Access Points Register");
                    }
                }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiManager.startScan();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ERROR", String.valueOf(isWiFiEnabled));
        }

    }

    public List<ScanResult> getScanResults(){
        List<ScanResult> wifiScanList = Collections.emptyList();
        wifiScanList = wifiManager.getScanResults();
        return wifiScanList;
    }

    public WifiInfo getConnectionInfo(){
        wifiInfo = null;
        wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo;
    }
    public void startAllScan(){
        List<ScanResult> wifiScanList = Collections.emptyList();
        WifiInfo wifiInfo = null;
        if (wifiManager.startScan()) {
            wifiScanList = wifiManager.getScanResults();
        }
    }

}
