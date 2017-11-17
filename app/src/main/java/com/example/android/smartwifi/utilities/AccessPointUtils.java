package com.example.android.smartwifi.utilities;

import android.content.Context;
import android.database.Cursor;

import com.example.android.smartwifi.data.accesspointdb.AccesspointContract;
import com.example.android.smartwifi.data.prioritydb.PriorityContract;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by FailSafe on 11/13/2017.
 */

public class AccessPointUtils {
    protected HashMap<String, String> accesspointMap = new HashMap<String, String>();
    protected ArrayList<String> accesspointList = new ArrayList<String>();

    private Cursor mCursor;
    private Context mContext;

    private static AccessPointUtils instance = new AccessPointUtils();

    public static AccessPointUtils getInstance() {
        return instance;
    }

    private static final int AP_LOADER_ID = 2;


    public AccessPointUtils(){
    }

    public void loadaccesspointFromDB(Context context){
        //set context maybe make function
        if(!accesspointMap.isEmpty()){
           accesspointMap.clear();
          accesspointList.clear();
        }

        this.mContext = context;


        //get cursor via query
        mCursor = mContext.getContentResolver().query(AccesspointContract.AccesspointEntry.CONTENT_URI,
                null,
                null,
                null,
                AccesspointContract.AccesspointEntry.COLUMN_ACCESSPOINT);

        //set indexs
        int idIndex = mCursor.getColumnIndex(AccesspointContract.AccesspointEntry._ID);
        int accesspointIndex = mCursor.getColumnIndex(AccesspointContract.AccesspointEntry.COLUMN_ACCESSPOINT);
        int macIndex = mCursor.getColumnIndex(AccesspointContract.AccesspointEntry.COLUMN_MACADDRESS);

        //move to first item in DB
        mCursor.moveToFirst();

        //loop through DB
        while (!mCursor.isAfterLast()) {
            //Get data
            String accesspoint = mCursor.getString(accesspointIndex);
            String macAddress = mCursor.getString(macIndex);


            //Add geofence to hashmap
            accesspointMap.put(macAddress, accesspoint);

            //move to next item in DB
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

    public HashMap<String, String> getAccessPointMap() {
        return this.accesspointMap;
    }



    public boolean isEmpty(){
        if(accesspointMap.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

}
