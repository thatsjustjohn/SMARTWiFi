package com.example.android.smartwifi.utilities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;


import com.example.android.smartwifi.MainActivity;
import com.example.android.smartwifi.sync.GeofenceTransitionIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import com.example.android.smartwifi.data.SMARTWifiPreferences;
import com.example.android.smartwifi.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jtwyp6 on 10/21/17.
 */

public class WifiGeoUtils implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        ResultCallback<Status> {
    //file stuff
    private String fileName = "AnalysisData.csv";
    File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), fileName);

    //Managers
    public WifiManager wifiManager;
    public LocationManager locationManager;
    public LocationListener listener;

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    public boolean isGPSEnabled = false;
    public boolean isNetworkEnabled = false;
    public boolean canGetLocation = false;
    private boolean reconnect =  false;

    //MAIN FUNCTIONS SHARED PREFERENCES
    public boolean isThresholdEnabled = true;
    public boolean isPriorityEnabled = true;
    public boolean isGeoFenceEnabled = false;
    public boolean isDataLoggingEnabled = false;
    public boolean isAccessPointEnabled = false;

    public Location mlocation;
    public double latitidue;
    public double longitude;

    //parameters for location
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 20;
    private static final long MIN_TIME_BETWEEN_UPDATES = 0;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 5;

    private PendingIntent mPendingIntent;

    //Context
    private Context context;

    //Data
    private String wifis[];
    private WifiInfo wifiInfo;
    private List<ScanResult> wifiScanList;
    private List<WifiConfiguration> configuredWifiList;
    private BroadcastReceiver wifiReceiver;

    protected HashMap<String, SGeofence> mGeofenceMap = new HashMap<String, SGeofence>();
    protected HashMap<String, String> mAccessPointMap = new HashMap<String, String>();
    protected HashMap<String, Integer> mPriorityMap = new HashMap<>();

    //variables
    public boolean isWiFiEnabled = false;
    public boolean isWiFiConnected = false;
    public boolean registeredGeo = false;
    public boolean getGeoUpdates = false;


    //shared preferences
    public int threshold_disconnect = -75;
    public int threshold_connect = -70;

    public WifiGeoUtils(@NonNull Context context) {
        //services
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.context = context;


        //Geolocation setup?
        //GET Geo Fences
        GeofenceUtils.getInstance().loadGeoFencesFromDB(context);
        mGeofenceMap = GeofenceUtils.getInstance().getGeofences();

        //Priority Setup
        PriorityUtils.getInstance().loadPriorityFromDB(context);
        mPriorityMap = PriorityUtils.getInstance().getPriorityMap();
        //Log.d("MAP", mPriorityMap.toString());

        //Access Point Setup
        AccessPointUtils.getInstance().loadaccesspointFromDB(context);
        mAccessPointMap = AccessPointUtils.getInstance().getAccessPointMap();

        //BUILD API
        buildGoogleApiClient();

        //SharedPreferences
        initSLupdateSharedPreferences();

        //initializeWifi manager and receiver
        initializeWifi();

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("GPS On Changed", String.valueOf(location));
                mlocation = location;
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

    //GEO LOCATION UTIL SETUP
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest
                .setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //
    public void geoOnStart() {
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    public void geoOnStop() {
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            registeredGeo = false;
        }
    }

    public void registerGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            return;
        }

        GeofenceUtils.getInstance().loadGeoFencesFromDB(context);
        mGeofenceMap = GeofenceUtils.getInstance().getGeofences();

        //IF NO GEO LOCATIONS
        if (mGeofenceMap.isEmpty()) return;


        //BUILD AND ADD MAPS TO GEOFENCING REQUEST
        GeofencingRequest.Builder geofencingRequestBuilder = new GeofencingRequest.Builder();
        for (Map.Entry<String, SGeofence> item : mGeofenceMap.entrySet()) {

            SGeofence smartgeo = item.getValue();

            geofencingRequestBuilder.addGeofence(smartgeo.buildGeofence());
            //BUILT ONE GEOLOCATION
            Log.d("REGISTERGEO", "BUILT A GEO LOCATION");
        }

        GeofencingRequest geofencingRequest = geofencingRequestBuilder.build();

        mPendingIntent = requestPendingIntent();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient,
                    geofencingRequest,
                    mPendingIntent).setResultCallback(this);
        }


        registeredGeo = true;
    }

    private PendingIntent requestPendingIntent() {

        if (null != mPendingIntent) {

            return mPendingIntent;
        } else {

            Intent intent = new Intent(context, GeofenceTransitionIntentService.class);
            return PendingIntent.getService(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("GEO ONCONNECTED", "Connected to GoogleApiClient");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("GEO ONFAILED",
                "Connection failed: ConnectionResult.getErrorCode() = "
                        + connectionResult.getErrorCode());
    }

    @Override
    public void onResult(@NonNull Status status) {

        if (status.isSuccess()) {
            String addedAnything = "Geofences Added";
            Toast.makeText(context,
                    addedAnything, Toast.LENGTH_SHORT)
                    .show();

        } else {
            registeredGeo = false;
            String errorMessage = GeofenceErrorMessagesUtils.getErrorString(context, status.getStatusCode());
            Toast.makeText(context, errorMessage,
                    Toast.LENGTH_LONG).show();
        }
    }


     /* Location stuff(fused location for getting updates for API) */

    public void startLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    public void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("GPS-OLC",
                "new location : " + location.getLatitude() + ", "
                        + location.getLongitude() + ". "
                        + location.getAccuracy());
        GeofenceTransitionIntentService.accuracy = (int)location.getAccuracy();
        mlocation = location;
    }


    public void initSLupdateSharedPreferences() {
        isThresholdEnabled = SMARTWifiPreferences.isThresholdEnabled(context);
        isPriorityEnabled = SMARTWifiPreferences.isPriorityEnabled(context);
        isDataLoggingEnabled = SMARTWifiPreferences.isDataLoggingEnabled(context);
        isGeoFenceEnabled = SMARTWifiPreferences.isGeoFenceEnabled(context);
        isAccessPointEnabled = SMARTWifiPreferences.isAccessPointEnabled(context);
        threshold_connect = SMARTWifiPreferences.reconnectThreshold(context);
        threshold_disconnect = SMARTWifiPreferences.disconnectThreshold(context);
        /*Log.d("SP", String.valueOf(isThresholdEnabled) + ":"
                + String.valueOf(isPriorityEnabled) + ":"
                + String.valueOf(isGeoFenceEnabled) + ":"
                + String.valueOf(isDataLoggingEnabled) + ":"
                + String.valueOf(threshold_connect) + ":"
                + String.valueOf(threshold_disconnect));
        */
    }

    public void initializeWifi() {
        try {
            isWiFiEnabled = wifiManager.isWifiEnabled();
            isWiFiConnected = isWifiConnected();
            WifiInfo info = wifiManager.getConnectionInfo();
            if (!isWiFiEnabled) {
                return;
            } else {
                context.registerReceiver(wifiReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        int size;
                        wifiScanList = wifiManager.getScanResults();
                        size = wifiScanList.size();
                        wifis = new String[wifiScanList.size()];
                    }
                }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiManager.startScan();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void unregisterReciever(){
        if(wifiReceiver != null)
        {
            context.unregisterReceiver(wifiReceiver);
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
        if(getGeoUpdates == false) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener, Looper.getMainLooper());
            getGeoUpdates = true;
        }
    }

    public void stopLocationTracking(){
        if(locationManager != null) {
            if (getGeoUpdates == true) {
                locationManager.removeUpdates(listener);
                getGeoUpdates = false;
            }
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
                Log.e("GPS Location", String.valueOf(location));
                mlocation = location;
                //UNTIL CHANGE ON LOCATION IS VERIFIED AS WORKING
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
        isWiFiConnected = isWifiConnected();
        //IF WIFI IS CONNECTED 1. CHECK THRESHOLD 2. REASSOCIATE 3. SEARCH FOR NEW DIFFERENT NETWORK 4. DISCONNECT
        if(isWiFiConnected && wifiInfo != null) {
            if (wifiInfo.getRssi() < threshold_disconnect && wifiInfo.getRssi() != -127) { //-127 is nothing / broken
                //TRY TO SEARCH FOR A BETTER NETWORK (SAME)
                if (!wifiManager.reassociate()) {
                    //TRY TO SEARCH FOR A BETTER NETWORK SAME OR DIFFERENT
                    reconnect = true;
                    if (!searchNetworks()) {
                        //IF FAIL DISCONNECT
                        wifiManager.disconnect();
                        isWiFiConnected = false;
                        Log.e("Threshold", "Disconnecting / No Available Networks");
                    }
                }
            }
        }else //IF WIFI IS NOT CONNECTED (SEARCH)
        {
            Log.e("Threshold", "Searching to Reconnect");
            reconnect = true;
            searchNetworks();
        }

    }

    public boolean searchNetworks() {
        initSLupdateSharedPreferences();
        ScanResult apToConnectTo = null;
        wifiManager.startScan();
        wifiScanList = getScanResults();
        //Holds matching AP AND WIFI CONFIG
        List<ScanResult> matchingAP = new ArrayList<ScanResult>();
        List<WifiConfiguration> matchingConfig = new ArrayList<WifiConfiguration>();
        configuredWifiList = wifiManager.getConfiguredNetworks();
        int count = 0;

        //Parse Scan list and clean up AP information
        Iterator<ScanResult> scanIterator = wifiScanList.iterator();
        while (scanIterator.hasNext()) {
            ScanResult scanResult = scanIterator.next();
            if (scanResult.SSID.equals("") || scanResult.SSID.equals("<unknown ssid>") || scanResult.SSID == null || scanResult.level < threshold_connect) {
                scanIterator.remove();
                count++;
            }else {
                if (isThresholdEnabled) {
                    if (scanResult.level < threshold_connect){
                        scanIterator.remove();
                        count++;
                    }
                }
            }
                /*else{
                Log.d("AP",scanResult.toString());
            }*/
        }

        //Compare configured list to cleaned up wifiAPlist and
        Log.d("WifiScanReconnect", "cleaned up " + String.valueOf(count) + " entries");
        if (configuredWifiList != null && wifiScanList != null) {
            //Log.d("CF", configuredWifiList.toString());
           // Log.d("SF", wifiScanList.toString());
            for (ScanResult singleAP : wifiScanList) {
                for (WifiConfiguration knownAP : configuredWifiList) {
                    //Log.d("Matchy", singleAP.SSID.toString() + " " + knownAP.SSID.toString().replaceAll("^['\"]*", "").replaceAll("['\"]*$", ""));
                    if (singleAP.SSID.toString().equals(knownAP.SSID.toString().replaceAll("^['\"]*", "").replaceAll("['\"]*$", ""))) {
                        matchingAP.add(singleAP);
                        matchingConfig.add(knownAP);
                        Log.d("AP2", singleAP.toString());
                    }
                }
            }
        }

        //IF NETWORKS MATCHES ARE FOUND
        if (matchingAP != null && !matchingAP.isEmpty()) {

            AccessPointUtils.getInstance().loadaccesspointFromDB(context);
            mAccessPointMap = AccessPointUtils.getInstance().getAccessPointMap();

            if(isAccessPointEnabled && !mAccessPointMap.isEmpty() && !(mAccessPointMap == null) ) {
                reconnect = true;
                scanIterator = matchingAP.iterator();
                while (scanIterator.hasNext()) {
                    ScanResult scanResult = scanIterator.next();
                    if(!mAccessPointMap.containsKey(scanResult.BSSID)){
                        scanIterator.remove();
                    }
                }
                Log.d("Final AP List", matchingAP.toString());
            }
            //Log.d("Final AP", matchingAP.toString());
            //Check For Priority if no priority take the highest signal
            PriorityUtils.getInstance().loadPriorityFromDB(context);
            mPriorityMap = PriorityUtils.getInstance().priorityMap;

            if(isPriorityEnabled && !mPriorityMap.isEmpty() && !(mPriorityMap == null) ){
                reconnect = true;
                boolean firstPass = true;
                for(ScanResult singleAP : matchingAP){
                    if (firstPass) {
                        firstPass = false;
                        apToConnectTo = singleAP;
                    }
                    if (mPriorityMap.get(singleAP.SSID.toString()) < mPriorityMap.get(apToConnectTo.SSID.toString())){
                        apToConnectTo = singleAP;
                    }
                }
                Log.d("Priority SAYS", apToConnectTo.toString());

            }else{
                boolean firstPass = true;
                for (ScanResult singleAP : matchingAP){
                    if(firstPass){
                        firstPass = false;
                        apToConnectTo = singleAP;
                    }
                    if(singleAP.level > apToConnectTo.level){
                        apToConnectTo = singleAP;
                    }

                }
            }

            //remove quotes from current network
            String curWifi = wifiInfo.getSSID().toString().replace("\"", "");

            //compare to see if they are the same if they are not connect
            if(!curWifi.equals(apToConnectTo.SSID.toString()) && reconnect) {
                Log.d("ATTEMPT Connect", apToConnectTo.toString());
                for (WifiConfiguration knownConfig : matchingConfig) {
                    if (apToConnectTo.SSID.toString().equals(knownConfig.SSID.toString().replaceAll("^['\"]*", "").replaceAll("['\"]*$", ""))) {
                        Log.d("CONNECTING TO", apToConnectTo.toString());

                        //if access point is enabled
                        if(isAccessPointEnabled)knownConfig.BSSID = apToConnectTo.BSSID;

                        wifiManager.enableNetwork(knownConfig.networkId, true);
                        isWiFiConnected = isWifiConnected();
                        reconnect = false;
                        return true;
                    }
                }
            }
            //else already connected to a network
        }else{
            Log.d("Final AP", "NO NETWORKS FOUND");
            return false;
        }
        return false;
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

    //WRiting stuff
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void writeDataLogging() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("NODATA", "PERMISSION NOT ENABLED");
            return;
        }

        getLocation();
        wifiInfo = wifiManager.getConnectionInfo();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat tf = new SimpleDateFormat( "HH:mm:ss");
        SimpleDateFormat dayf = new SimpleDateFormat("EEEE");
        SimpleDateFormat hourf = new SimpleDateFormat("kk");
        //SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(mlocation.getTime());
        String formattedTime = tf.format(mlocation.getTime());
        String formattedDay = dayf.format(mlocation.getTime());
        String formattedHour = hourf.format(mlocation.getTime());
        String data = "";
        FileOutputStream outputstream;
        try {
            File file1 = new File(Environment.getExternalStorageDirectory(), "TEST.csv");
            outputstream = new FileOutputStream(file1, true);
            OutputStreamWriter oswriter = new OutputStreamWriter(outputstream);
            BufferedWriter bwriter = new BufferedWriter(oswriter);
            data = String.valueOf(mlocation.getLatitude()) + ", " +
                    String.valueOf(mlocation.getLongitude()) + ", " +
                    String.valueOf(mlocation.getAccuracy()) + ", " +
                    String.valueOf(mlocation.getSpeed()) + ", " +
                    formattedDate + ", " +
                    formattedTime + ", " +
                    formattedDay + ", " +
                    formattedHour + ", " +
                    wifiInfo.getSSID().replace("\"", "") + ", " +
                    String.valueOf(wifiInfo.getRssi()) + ", " +
                    wifiInfo.getBSSID() + ", " +
                    wifiInfo.getMacAddress() + ", " +
                    String.valueOf(wifiInfo.getFrequency()) + ", " +
                    String.valueOf(wifiInfo.getIpAddress()) + ", " +
                    String.valueOf(wifiInfo.getLinkSpeed()) + ", " +
                    String.valueOf(wifiInfo.getNetworkId());
            bwriter.append(data);
            bwriter.newLine();
            bwriter.close();
            outputstream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("DATA", data);
    }
}
