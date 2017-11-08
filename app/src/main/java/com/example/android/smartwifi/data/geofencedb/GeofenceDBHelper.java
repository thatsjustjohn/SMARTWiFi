package com.example.android.smartwifi.data.geofencedb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jtwyp6 on 10/29/17.
 */

public class GeofenceDBHelper extends SQLiteOpenHelper {

    // The name of the database
    private static final String DATABASE_NAME = "tasksDb.db";

    // If you change the database schema, you must increment the database version
    private static final int VERSION = 1;


    // Constructor
    public GeofenceDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    /**
     * Called when the tasks database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create tasks table (careful to follow SQL formatting rules)
        final String CREATE_TABLE = "CREATE TABLE "  + GeofenceContract.TaskEntry.TABLE_NAME + " (" +
                GeofenceContract.TaskEntry._ID                + " INTEGER PRIMARY KEY, " +
                GeofenceContract.TaskEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                GeofenceContract.TaskEntry.COLUMN_LATITUDE   + " TEXT NOT NULL, " +
                GeofenceContract.TaskEntry.COLUMN_LONGITUDE   + " TEXT NOT NULL, " +
                GeofenceContract.TaskEntry.COLUMN_RADIUS   + " TEXT NOT NULL);";

        db.execSQL(CREATE_TABLE);
    }


    /**
     * This method discards the old table of data and calls onCreate to recreate a new one.
     * This only occurs when the version number for this database (DATABASE_VERSION) is incremented.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + GeofenceContract.TaskEntry.TABLE_NAME);
        onCreate(db);
    }
}
