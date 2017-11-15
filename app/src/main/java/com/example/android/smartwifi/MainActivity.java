package com.example.android.smartwifi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.smartwifi.APAdapter.APAdapterOnClickHandler;
import com.example.android.smartwifi.sync.SMARTWifiIntentService;
import com.example.android.smartwifi.sync.SMARTWifiSyncTask;
import com.example.android.smartwifi.sync.SMARTWifiSyncUtils;
import com.example.android.smartwifi.utilities.GeofenceUtils;
import com.example.android.smartwifi.utilities.WifiGeoUtils;


import java.util.List;


public class MainActivity extends AppCompatActivity implements
        APAdapterOnClickHandler,
        LoaderCallbacks<List<ScanResult>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    //XML Objects
    private RecyclerView mRecyclerView;
    private APAdapter mAPAdapter;

    private TextView mConnectionInfoTextView;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessageDisplay;

    private RadioButton mThresholdRadioButton;
    private RadioButton mPriorityRadioButton;
    private RadioButton mGeoFencingRadioButton;
    private RadioButton mDataLoggingRadioButton;



    private static final int AP_LOADER_ID = 0;

    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;

    static public boolean geofencesAlreadyRegistered = false;
    static public boolean prriorityUpdated = false;
    public static String TAG2 = "DEBUG_GEO";


    //Data
    private String wifis[];
    private WifiInfo wifiInfo;
    private List<ScanResult> wifiScanList;

    //Utils
    private WifiGeoUtils wifiGeoUtils;

    //Managers
    protected WifiManager wifiManager;
    protected LocationManager locationManager;

    //Toast
    private Toast mToast;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //WifiData may become new class
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //WIFIGEO
        wifiGeoUtils = new WifiGeoUtils(this);

        //Link objects

          /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_scan_aps);

        /* This TextView is used to display the current connected WiFi / GPS information and will be hidden if there errors */
        mConnectionInfoTextView = (TextView) findViewById(R.id.tv_current_wifi);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);


        /* These are the RAdio buttons used to display the activate functions of the program */
        mThresholdRadioButton = (RadioButton) findViewById(R.id.rb_threshold);
        mPriorityRadioButton = (RadioButton) findViewById(R.id.rb_priority);
        mDataLoggingRadioButton = (RadioButton) findViewById(R.id.rb_data_log);
        mGeoFencingRadioButton = (RadioButton) findViewById(R.id.rb_geo_fence);

        /*
         * LinearLayoutManager can support HORIZONTAL or VERTICAL orientations. The reverse layout
         * parameter is useful mostly for HORIZONTAL layouts that should reverse for right to left
         * languages.
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);

        mRecyclerView.setLayoutManager(layoutManager);

         /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

         /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         */
        mAPAdapter = new APAdapter(this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mAPAdapter);

         /*
         * This ID will uniquely identify the Loader. We can use it, for example, to get a handle
         * on our Loader at a later point in time through the support LoaderManager.
         */
        int loaderId = AP_LOADER_ID;

         /*
         * From MainActivity, we have implemented the LoaderCallbacks interface with the type of
         * String array. (implements LoaderCallbacks<String[]>) The variable callback is passed
         * to the call to initLoader below. This means that whenever the loaderManager has
         * something to notify us of, it will do so through this callback.
         */
        LoaderCallbacks<List<ScanResult>> callback = MainActivity.this;

        /*
         * The second parameter of the initLoader method below is a Bundle. Optionally, you can
         * pass a Bundle to initLoader that you can then access from within the onCreateLoader
         * callback. In our case, we don't actually use the Bundle, but it's here in case we wanted
         * to.
         */
        Bundle bundleForLoader = null;

        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callback);

        Log.d(TAG, "onCreate: registering preference changed listener");

        // COMPLETED (6) Register MainActivity as a OnSharedPreferenceChangedListener in onCreate
        /*
         * Register MainActivity as an OnPreferenceChangedListener to receive a callback when a
         * SharedPreference has changed. Please note that we must unregister MainActivity as an
         * OnSharedPreferenceChanged listener in onDestroy to avoid any memory leaks.
         */

        setupSharedPreferences();

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);


        ///This Code doesn't really work yet...moving to utils

        //CHECK PERMISSIONS WIFI AND LAT LONG
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.INTERNET,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                10);
            }
            return;
        }

        //schedule background jobs
        SMARTWifiSyncUtils.scheduleSMARTWifi(this);


        //service
       // startService(new Intent(this, SMARTWifiIntentService.class));


    }



    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //setup radio buttons for the moment.... will be stored in wifi class.
        mPriorityRadioButton.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_priority_key),getResources().getBoolean(R.bool.pref_priority_default)));
        mThresholdRadioButton.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_threshold_key),getResources().getBoolean(R.bool.pref_threshold_default)));
        mGeoFencingRadioButton.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_geo_fence_key),getResources().getBoolean(R.bool.pref_geo_fence_default)));
        mDataLoggingRadioButton.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_data_log_key),getResources().getBoolean(R.bool.pref_data_log_default)));


        /*setS(sharedPreferences.getBoolean(getString(R.string.pref_show_bass_key),
                getResources().getBoolean(R.bool.pref_show_bass_default)));
        mVisualizerView.setShowMid(sharedPreferences.getBoolean(getString(R.string.pref_show_mid_range_key),
                getResources().getBoolean(R.bool.pref_show_mid_range_default)));
        mVisualizerView.setShowTreble(sharedPreferences.getBoolean(getString(R.string.pref_show_treble_key),
                getResources().getBoolean(R.bool.pref_show_treble_default)));
        loadColorFromPreferences(sharedPreferences);
        loadSizeFromSharedPreferences(sharedPreferences);*/

        // Register the listener
    }
/*
    private void loadColorFromPreferences(SharedPreferences sharedPreferences) {
        mVisualizerView.setColor(sharedPreferences.getString(getString(R.string.pref_color_key),
                getString(R.string.pref_color_red_value)));
    }

    private void loadSizeFromSharedPreferences(SharedPreferences sharedPreferences) {
        float minSize = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_size_key),
                getString(R.string.pref_size_default)));
        mVisualizerView.setMinSizeScale(minSize);
    }
*/
    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id The ID whose loader is to be created.
     * @param loaderArgs Any arguments supplied by the caller.
     *
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<List<ScanResult>> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<List<ScanResult>>(this) {

            /* This String array will hold and help cache our weather data */
            List<ScanResult> mAPData = null;

            /**
             * Subclasses of AsyncTaskLoader must implement this to take care of loading their data.
             */
            @Override
            protected void onStartLoading() {
                if (mAPData != null) {
                    deliverResult(mAPData);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            /**
             * This is the method of the AsyncTaskLoader that will load and parse the JSON data
             * from OpenWifiMap in the background.
             *
             * @return  Wifi data from OpenWifiMap as an array of Strings.
             *         null if an error occurs
             */
            @Override
            public List<ScanResult> loadInBackground() {
                try {
                    List<ScanResult> apData = wifiGeoUtils.getScanResults();
                    wifiInfo = wifiGeoUtils.getConnectionInfo();
                    return apData;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            /**
             * Sends the result of the load to the registered listener.
             *
             * @param data The result of the load
             */
            public void deliverResult(List<ScanResult> data) {
                mAPData = data;
                super.deliverResult(data);
            }
        };
    }


    /**
     * This method will make the View for the AP data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showScanResultsDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }
    /**
     * This method will make the error message visible and hide the AP SCAN
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showScanErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<List<ScanResult>> loader, List<ScanResult> data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        //Connection info debug
        if(null == wifiInfo || wifiInfo.getSSID().equals("<unknown ssid>") || wifiInfo.getSSID().equals("")){
            Log.d("LD", "connect Info is bad");
            mConnectionInfoTextView.setText("Not Connected");

        }else{
            formatConnectionInfoText(wifiInfo);
        }
        wifiScanList = wifiGeoUtils.getScanResults();
        int size = wifiScanList.size();
        Log.d("onRecieve", String.valueOf(size) + " Access Points on Scan");
        data = mAPAdapter.cleanAPData(data);
        mAPAdapter.setAPData(data);
        if (null == data) {
            showScanErrorMessage();
        } else {
            showScanResultsDataView();
        }
    }

    public void formatConnectionInfoText(WifiInfo wifiInfo){
        mConnectionInfoTextView.setText("SSID :: " + wifiInfo.getSSID()
        + "\nStrength :: " + wifiInfo.getRssi()
        + "\nLink Speed :: " + wifiInfo.getLinkSpeed());
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<List<ScanResult>> loader) {
        /*
         * We aren't using this method in our example application, but we are required to Override
         * it to implement the LoaderCallbacks<String> interface
         */
    }

    /**
     * This method is used when we are resetting data, so that at one point in time during a
     * refresh of our data, you can see that there is no data showing.
     */
    private void invalidateData() {mAPAdapter.setAPData(null);
    }

    /**
     * This method is for responding to clicks from our list.
     *
     * @param apItemInList String describing weather details for a particular day
     */
    @Override
    public void onClick(String apItemInList) {
        Context context = this;
        Class destinationClass = DetailAPActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, apItemInList);

        startActivity(intentToStartDetailActivity);
    }

    /**
     * OnStart is called when the Activity is coming into view. This happens when the Activity is
     * first created, but also happens when the Activity is returned to from another Activity. We
     * are going to use the fact that onStart is called when the user returns to this Activity to
     * check if the location setting or the preferred units setting has changed. If it has changed,
     * we are going to perform a new query.
     */
    @Override
    protected void onStart() {
        super.onStart();

        /*
         * If the preferences for location or units have changed since the user was last in
         * MainActivity, perform another query and set the flag to false.
         *
         * This isn't the ideal solution because there really isn't a need to perform another
         * GET request just to change the units, but this is the simplest solution that gets the
         * job done for now. Later in this course, we are going to show you more elegant ways to
         * handle converting the units from celsius to fahrenheit and back without hitting the
         * network again by keeping a copy of the data in a manageable format.
         */
        if (PREFERENCES_HAVE_BEEN_UPDATED) {
            Log.d(TAG, "onStart: preferences were updated");
            getSupportLoaderManager().restartLoader(AP_LOADER_ID, null, this);
            PREFERENCES_HAVE_BEEN_UPDATED = false;
        }
    }

    public void smartWifiTask(){
        if(mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, "Test", Toast.LENGTH_SHORT);
        mToast.show();

        Intent smartWifiIntent = new Intent(this, SMARTWifiIntentService.class);
        smartWifiIntent.setAction(SMARTWifiSyncTask.ACTION_WIFI_THRESHOLD);
        startService(smartWifiIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiGeoUtils.unregisterReciever();
        /* Unregister MainActivity as an OnPreferenceChangedListener to avoid any memory leaks. */
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            wifiGeoUtils.startAllScan();
            invalidateData();

            ////// DEBUGGING ///
            // BACKGROUND TASK //
            //smartWifiTask();
            GeofenceUtils.getInstance().loadGeoFencesFromDB(this);
            ///GeofenceUtils geoFenceUtils = new GeofenceUtils(this);
            //geoFenceUtils.loadGeoFencesFromDB();
            //NOTIFICATION //
            //NotificationUtils.askUserToSwitchNonPriority(this);
            getSupportLoaderManager().restartLoader(AP_LOADER_ID, null, this);
            return true;
        }

        if(id == R.id.action_settings){
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void restartLoaderTest(){
        getSupportLoaderManager().restartLoader(AP_LOADER_ID, null, this);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        /* ADD REST OF UPDATES HERE AND LINK TO WIFI CLASS */
        if(key.equals(getString(R.string.pref_priority_key))){
            mPriorityRadioButton.setChecked(sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_priority_default)));
        }else if (key.equals(getString(R.string.pref_threshold_key))){
            mThresholdRadioButton.setChecked(sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_threshold_default)));
        }else if (key.equals(getString(R.string.pref_data_log_key))){
            mDataLoggingRadioButton.setChecked(sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_data_log_default)));
        }else if (key.equals(getString(R.string.pref_geo_fence_key))){
            mGeoFencingRadioButton.setChecked(sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_geo_fence_default)));
        }
        /*
         * Set this flag to true so that when control returns to MainActivity, it can refresh the
         * data.
         *
         * This isn't the ideal solution because there really isn't a need to perform another
         * GET request just to change the units, but this is the simplest solution that gets the
         * job done for now. Later in this course, we are going to show you more elegant ways to
         * handle converting the units from celsius to fahrenheit and back without hitting the
         * network again by keeping a copy of the data in a manageable format.
         */
        PREFERENCES_HAVE_BEEN_UPDATED = true;
    }
}
