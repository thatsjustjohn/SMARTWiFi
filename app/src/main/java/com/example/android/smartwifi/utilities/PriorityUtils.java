package com.example.android.smartwifi.utilities;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;

import com.example.android.smartwifi.data.geofencedb.GeofenceContract;
import com.example.android.smartwifi.data.prioritydb.PriorityContract;
import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by FailSafe on 11/13/2017.
 */

public class PriorityUtils {
    protected HashMap<String, Integer> priorityMap = new HashMap<String, Integer>();
    protected ArrayList<String> priorityList = new ArrayList<String>();

    private Cursor mCursor;
    private Context mContext;

    private static PriorityUtils instance = new PriorityUtils();

    public static PriorityUtils getInstance() {
        return instance;
    }

    private static final int PRIORITY_LOADER_ID = 1;


    public PriorityUtils(){
    }

    public void loadPriorityFromDB(Context context){
        //set context maybe make function
        if(!priorityMap.isEmpty()){
           priorityMap.clear();
           priorityList.clear();
        }

        this.mContext = context;


        //get cursor via query
        mCursor = mContext.getContentResolver().query(PriorityContract.PriorityEntry.CONTENT_URI,
                null,
                null,
                null,
                PriorityContract.PriorityEntry.COLUMN_PRIORITY);

        //set indexs
        int idIndex = mCursor.getColumnIndex(PriorityContract.PriorityEntry._ID);
        int networkIndex = mCursor.getColumnIndex(PriorityContract.PriorityEntry.COLUMN_ACCESSPOINT);
        int priorityIndex = mCursor.getColumnIndex(PriorityContract.PriorityEntry.COLUMN_PRIORITY);

        //move to first item in DB
        mCursor.moveToFirst();

        //loop through DB
        while (!mCursor.isAfterLast()) {

            Log.d("DB", mCursor.getString(networkIndex));

            //Get data
            String mNetwork = mCursor.getString(networkIndex);
            int mPriority = mCursor.getInt(priorityIndex);


            //Add geofence to hashmap
            priorityMap.put(mNetwork,mPriority);
            priorityList.add(mPriority, mNetwork);

            //move to next item in DB
            mCursor.moveToNext();
        }
        // make sure to close the cursor

        Log.d("DB", priorityMap.toString());


        mCursor.close();
    }


    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public HashMap<String, Integer> getPriorityMap() {
        return this.priorityMap;
    }

    public ArrayList<String> getPriorityList() {
        return this.priorityList;
    }

    public boolean isEmpty(){
        if(priorityMap.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

}
