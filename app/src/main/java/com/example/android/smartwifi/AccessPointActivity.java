package com.example.android.smartwifi;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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

import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.android.smartwifi.data.accesspointdb.AccesspointContract;
import com.example.android.smartwifi.data.geofencedb.GeofenceContract;
import com.example.android.smartwifi.utilities.AccessPointUtils;

import java.util.HashMap;

public class AccessPointActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    // Constants for logging and referring to a unique loader
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int ACCESSPOINT_LOADER_ID = 0;

    // Member variables for the adapter and RecyclerView
    private AccessPointCursorAdapter mAdapter;
    RecyclerView mRecyclerView;

    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessage;
    private int mPosition = RecyclerView.NO_POSITION;
    private WifiManager wifiManager;
    private HashMap<String, String> mAccessPointMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_point);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get current map
        AccessPointUtils.getInstance().loadaccesspointFromDB(getApplicationContext());
        mAccessPointMap = AccessPointUtils.getInstance().getAccessPointMap();
        Log.d("APM", mAccessPointMap.toString());

        // Set the RecyclerView to its corresponding view
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewAccessPoints);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        mErrorMessage = (TextView) findViewById(R.id.tv_ap_error_display);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_ap_loading_indicator);

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new AccessPointCursorAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete

                // COMPLETED (1) Construct the URI for the item to delete
                //[Hint] Use getTag (from the adapter code) to get the id of the swiped item
                // Retrieve the id of the task to delete
                int id = (int) viewHolder.itemView.getTag();

                // Build appropriate uri with String row id appended
                String stringId = Integer.toString(id);
                Uri uri = AccesspointContract.AccesspointEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();

                // COMPLETED (2) Delete a single row of data using a ContentResolver
                getContentResolver().delete(uri, null, null);

                // COMPLETED (3) Restart the loader to re-query for all tasks after a deletion
                getSupportLoaderManager().restartLoader(ACCESSPOINT_LOADER_ID, null, AccessPointActivity.this);

            }
        }).attachToRecyclerView(mRecyclerView);

        /*
         Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddTaskActivity.
         */
        FloatingActionButton fabButton = (FloatingActionButton) findViewById(R.id.fabAPSav);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAPSav);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAddNetworkAP(view);
               /* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });


        //show loading
        showAccessPointLoading();


        /*
         Ensure a loader is initialized and active. If the loader doesn't already exist, one is
         created, otherwise the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(ACCESSPOINT_LOADER_ID, null, this);
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
        getSupportLoaderManager().restartLoader(ACCESSPOINT_LOADER_ID, null, this);
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
            Cursor mTaskData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mTaskData);
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
                    return getContentResolver().query(AccesspointContract.AccesspointEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            AccesspointContract.AccesspointEntry.COLUMN_ACCESSPOINT);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
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
        mAdapter.swapCursor(data);

        if (mPosition == RecyclerView.NO_POSITION){
            mPosition = 0;
        }

        mRecyclerView.smoothScrollToPosition(mPosition);

        if (data.getCount() != 0){
            showAccessPointDataView();
        }else{
            mErrorMessage.setText("Please click the add button\n To load in the current connected\n Access Point & Mac Address.");
            showAccessPointError();
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



    private void showAccessPointDataView(){
        mErrorMessage.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showAccessPointLoading(){
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showAccessPointError(){
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickAddNetworkAP(View view) {
        // Not yet implemented
        // Check if EditText is empty, if not retrieve input and store it in a ContentValues object
        // If the EditText input is empty -> don't create an entry
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String accesspoint = wifiManager.getConnectionInfo().getSSID().toString().replace("\"", "");
        String accesspointMacAdd = wifiManager.getConnectionInfo().getBSSID();
        if(accesspoint == null || accesspoint == "" || accesspointMacAdd == null || accesspointMacAdd == "")
        {
            notConnectedSnackBar(view);
            return;
        }
        if(mAccessPointMap.containsKey(accesspointMacAdd)){
            alreadyExistsSnackBar(view);
            return;
        }



        // Insert new task data via a ContentResolver
        // Create new empty ContentValues object
        ContentValues contentValues = new ContentValues();
        // Put the Geo Fence Data into the ContentValues
        contentValues.put(AccesspointContract.AccesspointEntry.COLUMN_ACCESSPOINT, accesspoint);
        contentValues.put(AccesspointContract.AccesspointEntry.COLUMN_MACADDRESS, accesspointMacAdd);


        // Insert the content values via a ContentResolver
        Uri uri = getContentResolver().insert(AccesspointContract.AccesspointEntry.CONTENT_URI, contentValues);

        // Display the URI that's returned with a Toast
        // [Hint] Don't forget to call finish() to return to MainActivity after this insert is complete
        if(uri != null) {
            Toast.makeText(getBaseContext(), "AP ADDED", Toast.LENGTH_LONG).show();
        }
        getSupportLoaderManager().restartLoader(ACCESSPOINT_LOADER_ID, null, this);

    }


    public void notConnectedSnackBar(View view){
        Snackbar.make(view, "You are not connected to a network", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }


    public void alreadyExistsSnackBar(View view){
        Snackbar.make(view, "This specific AP already exists in Database", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
