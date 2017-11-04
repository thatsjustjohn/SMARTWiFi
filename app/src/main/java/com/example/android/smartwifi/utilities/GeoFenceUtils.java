package com.example.android.smartwifi.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.android.smartwifi.data.GeoFenceContract;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;

/**
 * Created by jtwyp6 on 11/4/17.
 */

public class GeoFenceUtils {
    protected GoogleApiClient mGoogleApiClient;
    protected HashMap<String, SMARTWifiGeoFence> geofences = new HashMap<String, SMARTWifiGeoFence>();

    private Cursor mCursor;
    private Context mContext;
    private static GeoFenceUtils instance = new GeoFenceUtils();

    public static GeoFenceUtils getInstance() {
        return instance;
    }

    private static final int TASK_LOADER_ID = 0;


    public GeoFenceUtils(){
        geofences.put("The Shire", new SMARTWifiGeoFence("The Shire", 39.0015329, -77.0867936,
                50, Geofence.NEVER_EXPIRE,
                Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_DWELL
                        | Geofence.GEOFENCE_TRANSITION_EXIT));
    }

    public void loadGeoFencesFromDB(Context context){
        //set context maybe make function
        this.mContext = context;


        //get cursor via query
        mCursor = mContext.getContentResolver().query(GeoFenceContract.TaskEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        //set indexs
        int idIndex = mCursor.getColumnIndex(GeoFenceContract.TaskEntry._ID);
        int descriptionIndex = mCursor.getColumnIndex(GeoFenceContract.TaskEntry.COLUMN_DESCRIPTION);
        int latitidueIndex = mCursor.getColumnIndex(GeoFenceContract.TaskEntry.COLUMN_LATITUDE);
        int longitudeIndex = mCursor.getColumnIndex(GeoFenceContract.TaskEntry.COLUMN_LONGITUDE);
        int radiusIndex = mCursor.getColumnIndex(GeoFenceContract.TaskEntry.COLUMN_RADIUS);

        //move to first
        mCursor.moveToFirst();

        //loop through DB
        while (!mCursor.isAfterLast()) {

            Log.d("DB", mCursor.getString(descriptionIndex));
            //String description
            //geofences.put(mCursor.getString)
            mCursor.moveToNext();
        }
        // make sure to close the cursor
        mCursor.close();
    }


    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

}
