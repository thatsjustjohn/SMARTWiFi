package com.example.android.smartwifi.utilities;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.HashMap;

import com.example.android.smartwifi.data.geofencedb.GeofenceContract;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;

/**
 * Created by jtwyp6 on 11/4/17.
 */

public class GeofenceUtils {
    protected GoogleApiClient mGoogleApiClient;
    protected HashMap<String, SGeofence> geofences = new HashMap<String, SGeofence>();

    private Cursor mCursor;
    private Context mContext;
    private static GeofenceUtils instance = new GeofenceUtils();

    public static GeofenceUtils getInstance() {
        return instance;
    }

    private static final int TASK_LOADER_ID = 0;


    public GeofenceUtils(){
    }

    public void loadGeoFencesFromDB(Context context){
        //set context maybe make function
        if(!geofences.isEmpty()){
            geofences.clear();
        }

        this.mContext = context;


        //get cursor via query
        mCursor = mContext.getContentResolver().query(GeofenceContract.GeofenceEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        //set indexs
        int idIndex = mCursor.getColumnIndex(GeofenceContract.GeofenceEntry._ID);
        int descriptionIndex = mCursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_DESCRIPTION);
        int latitidueIndex = mCursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_LATITUDE);
        int longitudeIndex = mCursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_LONGITUDE);
        int radiusIndex = mCursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_RADIUS);

        //move to first item in DB
        mCursor.moveToFirst();

        //loop through DB
        while (!mCursor.isAfterLast()) {

            Log.d("DB", mCursor.getString(descriptionIndex));

            //Get data
            String mDescription = mCursor.getString(descriptionIndex);
            double mLat = mCursor.getDouble(latitidueIndex);
            double mLong = mCursor.getDouble(longitudeIndex);
            float mRad = mCursor.getFloat(radiusIndex);

            //Add geofence to hashmap
            geofences.put(mDescription, new SGeofence(
                            mDescription,
                            mLat,
                            mLong,
                            mRad,
                            com.google.android.gms.location.Geofence.NEVER_EXPIRE,
                            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL |
                            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER |
                            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
                            )
                    );

            //move to next item in DB
            mCursor.moveToNext();
        }
        // make sure to close the cursor

        Log.d("DB", geofences.toString());


        mCursor.close();

        geofences.put("The Shire", new SGeofence("The Shire", 39.0015329, -77.0867936,
                50, 12* DateUtils.HOUR_IN_MILLIS,
                Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_DWELL
                        | Geofence.GEOFENCE_TRANSITION_EXIT));
    }


    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public HashMap<String, SGeofence> getGeofences() {
        Log.d("DB", "BITCHES" + geofences.toString());
        return this.geofences;
    }

    public boolean isEmpty(){
        if(geofences.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

}
