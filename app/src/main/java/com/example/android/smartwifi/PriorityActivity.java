package com.example.android.smartwifi;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.smartwifi.data.geofencedb.GeofenceContract;
import com.example.android.smartwifi.data.prioritydb.PriorityContract;
import com.example.android.smartwifi.utilities.PriorityUtils;
import com.example.android.smartwifi.utilities.SGeofence;
import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PriorityActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    // Constants for logging and referring to a unique loader
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PRIORITY_LOADER_ID = 1;

    private List<WifiConfiguration> configuredWifiList;
    private List<ScanResult> scanResultList;
    public WifiManager wifiManager;
   // protected HashMap<String, int> mPriorityMap = new HashMap<String, int>();
    protected ArrayList<String> mPriorityList;


    private ArrayList<String> matchingAP;
    private ArrayList<String> allAP;

    // Member variables for the adapter and RecyclerView
    private PriorityCursorAdapter mAdapter;
    RecyclerView mRecyclerView;
    private Switch mSwitchLocal;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessage;
    private int mPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_priority);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the RecyclerView to its corresponding view
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewPriority);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mErrorMessage = (TextView) findViewById(R.id.tv_priority_error_display);

        mSwitchLocal = (Switch) findViewById(R.id.sw_local_networks);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_priority_loading_indicator);

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new PriorityCursorAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN , 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPosition = viewHolder.getAdapterPosition();
                final int toPosition = target.getAdapterPosition();
                mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                getSupportLoaderManager().restartLoader(PRIORITY_LOADER_ID, null, PriorityActivity.this);
            }

        }).attachToRecyclerView(mRecyclerView);

        /*
         Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddTaskActivity.
         */

        FloatingActionButton fabDel = (FloatingActionButton) findViewById(R.id.fabDel);
        FloatingActionButton fabSav = (FloatingActionButton) findViewById(R.id.fabSav);

        mSwitchLocal.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                getSupportLoaderManager().restartLoader(PRIORITY_LOADER_ID, null, PriorityActivity.this);
            }
        });

        fabDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent addPriorityIntent = new Intent(PriorityActivity.this, AddPriorityActivity.class);
                //startActivity(addPriorityIntent);
                onBackPressed();
                Snackbar.make(view, "Changes Cancelled", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        fabSav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent addPriorityIntent = new Intent(PriorityActivity.this, AddPriorityActivity.class);
                //startActivity(addPriorityIntent);
                //bulk delete
                Uri uri = PriorityContract.PriorityEntry.CONTENT_URI;
                uri = uri.buildUpon().build();
                int deleted = getContentResolver().delete(uri, null, null);
                Log.d("DelB", String.valueOf(deleted));
                //bulk insert
                boolean success = priorityBulkInsert(uri);
                String isSaved;
                if(success){
                    isSaved = "Changes have been Saved";
                }else{
                    isSaved = "An error has occured";
                }
                //reload
                getSupportLoaderManager().restartLoader(PRIORITY_LOADER_ID, null, PriorityActivity.this);
                Snackbar.make(view, isSaved, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //show loading
        showPriorityLoading();


        /*
         Ensure a loader is initialized and active. If the loader doesn't already exist, one is
         created, otherwise the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(PRIORITY_LOADER_ID, null, this);
    }


    private boolean priorityBulkInsert(Uri uri){
        if(mPriorityList != null && mPriorityList.size() != 0) {
            ContentValues[] priorityContentValues = new ContentValues[mPriorityList.size()];
            for (int i = 0; i < mPriorityList.size(); i++) {
                ContentValues priorityValues = new ContentValues();
                priorityValues.put(PriorityContract.PriorityEntry.COLUMN_ACCESSPOINT, mPriorityList.get(i).toString());
                priorityValues.put(PriorityContract.PriorityEntry.COLUMN_PRIORITY, i);

                priorityContentValues[i] = priorityValues;
            }
            try {
                int rowsAdded = getContentResolver().bulkInsert(uri, priorityContentValues);
                Log.d("DB", String.valueOf(rowsAdded));
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to Save data.");
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * This method is called after this activity has been paused or restarted.
     * Often, this is after new data has been inserted through an AddTaskActivity,
     * so this restarts the loader to re-query the underlying data for any changes.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // re-queries for all tasks
        getSupportLoaderManager().restartLoader(PRIORITY_LOADER_ID, null, this);
    }


    /**
     * Instantiates and returns a new AsyncTaskLoader with the given ID.
     * This loader will return task data as a Cursor or null if an error occurs.
     *
     * Implements the required callbacks to take care of loading data at all stages of loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mNetworkData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mNetworkData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mNetworkData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                // Will implement to load data

                // Query and load all task data in the background; sort by priority
                // [Hint] use a try/catch block to catch any errors in loading data

                try {
                    return getContentResolver().query(PriorityContract.PriorityEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            PriorityContract.PriorityEntry.COLUMN_PRIORITY);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mNetworkData = data;
                super.deliverResult(data);
            }
        };

    }


    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the data that the adapter uses to create ViewHolders
        // THIS IS WHERE WE NEED TO GET THE CURRENT DATA, POPULATE IT, AND THEN ADD ON THE MISSING(NEW) NETWORKS TO THE LIST


        //mAdapter.swapCursor(data);
        ArrayList<String> mData = new ArrayList<String>();
        if(searchNetworks())
        {
        /*    mData = matchingAP;
            Log.d("STUFF", matchingAP.toString());

        }else{*/
            mData = mPriorityList;
            Log.d("STUFF", mPriorityList.toString());

        }
        //until algo fixed this defaults
        mData = mPriorityList;

        mAdapter.setNetworkData(mData);

        if (mPosition == RecyclerView.NO_POSITION){
            mPosition = 0;
        }

        mRecyclerView.smoothScrollToPosition(mPosition);

        if (mData.size() != 0){
            showPriorityDataView();
        }else{
            mErrorMessage.setText("No Configured Networks.");
            showPriorityError();
        }
    }


    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.
     * onLoaderReset removes any references this activity had to the loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }



    private void showPriorityDataView(){
        mErrorMessage.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showPriorityLoading(){
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showPriorityError(){
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }


    public boolean searchNetworks(){
        matchingAP = new ArrayList<String>();

        //GET ALL YOUR DATA

        PriorityUtils.getInstance().loadPriorityFromDB(this);
        mPriorityList = PriorityUtils.getInstance().getPriorityList();
        scanResultList = wifiManager.getScanResults();
        configuredWifiList = wifiManager.getConfiguredNetworks();

        //IF THERE IS NO PRIORITY LIST IN DB
        if(mPriorityList == null || mPriorityList.isEmpty()){
            Snackbar.make(findViewById(android.R.id.content), "Nothing in Database", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            //NO DATA SO GET CONFIGURED LIST AND MAKE THE RANDOM ORDER THE PRIORITY LIST
            configuredWifiList = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration knownAP : configuredWifiList) {
                mPriorityList.add(knownAP.SSID.replaceAll("^['\"]*", "").replaceAll("['\"]*$", ""));
            }
        }

        //IF LOCAL
        if(wifiManager.startScan()){
            //if we have data
            if(scanResultList != null && mPriorityList != null) {
                Log.d("TEST", scanResultList.toString());

                //Parse Scan list and clean up AP information
                Iterator<ScanResult> scanIterator = scanResultList.iterator();
                while (scanIterator.hasNext()) {
                    ScanResult scanResult = scanIterator.next();
                    if (scanResult.SSID.equals("") || scanResult.SSID.equals("<unknown ssid>") || scanResult.SSID == null) {
                        scanIterator.remove();
                    }
                }

                //Compare configured list to cleaned up wifiAPlist and
                if (configuredWifiList != null && scanResultList != null) {
                    for (ScanResult singleAP : scanResultList) {
                        for (String priorityAP : mPriorityList) {
                            if (singleAP.SSID.toString().equals(priorityAP)) {
                                matchingAP.add(singleAP.SSID.toString());
                            }
                        }
                    }
                }

                //
                if ((matchingAP != null && !matchingAP.isEmpty()) && mSwitchLocal.isChecked()) {
                    Log.d("Final AP", matchingAP.toString());
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }


    private void invalidateData() {mAdapter.setNetworkData(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.priority, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            //invalidateData();
            getSupportLoaderManager().restartLoader(PRIORITY_LOADER_ID, null, this);
            return true;
        }
        if(id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
