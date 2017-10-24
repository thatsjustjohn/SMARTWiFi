package com.example.android.smartwifi.utilities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.sip.SipAudioCall;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.android.smartwifi.MainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jtwyp6 on 10/21/17.
 */

public class WifiGeoUtils{
    //Managers
    public WifiManager wifiManager;
    public LocationManager locationManager;
    public LocationListener listener;

    public boolean isGPSEnabled = false;
    public boolean isNetworkEnabled = false;
    public boolean canGetLocation = false;
    public Location location;
    public double latitidue;
    public double longitude;

    //parameters for location
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 500;
    private static final long MIN_TIME_BETWEEN_UPDATES = 0;


    //Context
    private Context context;

    //Data
    private String wifis[];
    private WifiInfo wifiInfo;
    private List<ScanResult> wifiScanList;
    private List<WifiConfiguration> configuredWifiList;

    //variables
    public boolean isWiFiEnabled  = false;
    public boolean isWiFiConnected = false;

    //shared preferences
    public int threshold_disconnect = -40;
    public int threshold_connect = -70;


    public WifiGeoUtils(@NonNull Context context) {
        //services
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.context = context;
        //initializeWifi manager and reciever
        initializeWifi();
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("GPS", "on location change event");
                Log.d("On Changed", String.valueOf(location));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
    }

    public void initializeWifi() {
        try {
            isWiFiEnabled = wifiManager.isWifiEnabled();
            isWiFiConnected = isWifiConnected();
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

    public List<ScanResult> getScanResults() {
        List<ScanResult> wifiScanList = Collections.emptyList();
        wifiScanList = wifiManager.getScanResults();
        return wifiScanList;
    }

    public WifiInfo getConnectionInfo() {
        wifiInfo = null;
        wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo;
    }

    public void startLocationTracking() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener, Looper.getMainLooper());
    }

    public void stopLocationTracking(){
        if(locationManager != null){
            locationManager.removeUpdates(listener);
        }
    }
    public Location getLocation(){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        if(locationManager != null) {
            Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            if(location != null)
            {
                Log.e("Location", String.valueOf(location));
                return location;
            }else
            {
                Log.e("Location", "Location is NULL");
            }
        }
        return null;
    }

    public void startAllScan(){
        List<ScanResult> wifiScanList = Collections.emptyList();
        WifiInfo wifiInfo = null;
        if (wifiManager.startScan()) {
            wifiScanList = wifiManager.getScanResults();
        }
    }

    public void thresholdMonitor(){
        wifiInfo = wifiManager.getConnectionInfo();
        if(wifiInfo.getRssi() < threshold_disconnect && wifiInfo.getRssi() != -127){ //-127 is nothing / broken
            //maybe reassociate networks here before disconnecting
            wifiManager.disconnect();
            isWiFiConnected = false;
            Log.e("Threshold", "Disconnecting");
        }
        //If you are not connect, search
        if(!isWiFiConnected){
            Log.e("Threshold", "Searching to Reconnect");
            searchNetworks();
        }

    }

    public void searchNetworks() {
        wifiManager.startScan();
        wifiScanList = getScanResults();
        List<ScanResult> matchingAP = new ArrayList<ScanResult>();
        configuredWifiList = wifiManager.getConfiguredNetworks();
        int count = 0;
        Iterator<ScanResult> scanIterator = wifiScanList.iterator();
        while (scanIterator.hasNext()) {
            ScanResult scanResult = scanIterator.next();
            if (scanResult.SSID.equals("") || scanResult.SSID.equals("<unknown ssid>") || scanResult.SSID == null || scanResult.level < threshold_connect) {
                scanIterator.remove();
                count++;
            }/*else{
                Log.d("AP",scanResult.toString());
            }*/
        }
        Log.d("WifiScanReconnect", "cleaned up " + String.valueOf(count) + " entries");
        if (configuredWifiList != null && wifiScanList != null) {
            //Log.d("CF", configuredWifiList.toString());
           // Log.d("SF", wifiScanList.toString());
            for (ScanResult singleAP : wifiScanList) {
                for (WifiConfiguration knownAP : configuredWifiList) {
                    Log.d("Matchy", singleAP.SSID.toString() + " " + knownAP.SSID.toString().replaceAll("^['\"]*", "").replaceAll("['\"]*$", ""));
                    if (singleAP.SSID.toString().equals(knownAP.SSID.toString().replaceAll("^['\"]*", "").replaceAll("['\"]*$", ""))) {
                        matchingAP.add(singleAP);
                        Log.d("AP2", singleAP.toString());
                    }
                }
            }
        }
        if (matchingAP != null) {
            Log.d("Final AP", matchingAP.toString());
        }
    }

    public boolean isWifiConnected(){
        if(wifiManager.isWifiEnabled()){
            if(wifiManager.getConnectionInfo().getNetworkId() == -1){
                return false;
            }
            return true;
        }
        isWiFiEnabled = false;
        return false;
    }

}
