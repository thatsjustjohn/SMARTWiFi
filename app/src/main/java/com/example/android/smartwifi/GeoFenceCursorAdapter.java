package com.example.android.smartwifi;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.smartwifi.data.geofencedb.GeofenceContract;


/**
 * This GeoFenceCursorAdapter creates and binds ViewHolders, that hold the description and priority of a task,
 * to a RecyclerView to efficiently display data.
 */
public class GeoFenceCursorAdapter extends RecyclerView.Adapter<GeoFenceCursorAdapter.TaskViewHolder> {

    // Class variables for the Cursor that holds task data and the Context
    private Cursor mCursor;
    private Context mContext;


    /**
     * Constructor for the GeoFenceCursorAdapter that initializes the Context.
     *
     * @param mContext the current Context
     */
    public GeoFenceCursorAdapter(Context mContext) {
        this.mContext = mContext;
    }


    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new TaskViewHolder that holds the view for each task
     */
    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.geofence_item, parent, false);

        return new TaskViewHolder(view);
    }


    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {

        // Indices for the _id, description, and priority columns
        int idIndex = mCursor.getColumnIndex(GeofenceContract.GeofenceEntry._ID);
        int descriptionIndex = mCursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_DESCRIPTION);
        int latitidueIndex = mCursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_LATITUDE);
        int longitudeIndex = mCursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_LONGITUDE);
        int radiusIndex = mCursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_RADIUS);

        mCursor.moveToPosition(position); // get to the right location in the cursor

        // Determine the values of the wanted data
        final int id = mCursor.getInt(idIndex);
        String description = mCursor.getString(descriptionIndex);
        double latitude = mCursor.getDouble(latitidueIndex);
        double longitude = mCursor.getDouble(longitudeIndex);
        double radius = mCursor.getDouble(radiusIndex);

        //Set values
        holder.itemView.setTag(id);
        holder.geofenceDescriptionView.setText(description);
        holder.latitudeView.setText(String.valueOf(latitude));
        holder.longitudeView.setText(String.valueOf(longitude));
        holder.radiusView.setText(String.valueOf(radius));

        /*GradientDrawable priorityCircle = (GradientDrawable) holder.priorityView.getBackground();
        // Get the appropriate background color based on the priority
        int priorityColor = getPriorityColor(priority);
        priorityCircle.setColor(priorityColor);*/

    }


    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }


    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }


    // Inner class for creating ViewHolders
    class TaskViewHolder extends RecyclerView.ViewHolder {

        // Class variables for the task description and priority TextViews
        TextView geofenceDescriptionView;
        TextView latitudeView;
        TextView longitudeView;
        TextView radiusView;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public TaskViewHolder(View itemView) {
            super(itemView);

            geofenceDescriptionView = (TextView) itemView.findViewById(R.id.geofenceDescription);
            latitudeView = (TextView) itemView.findViewById(R.id.latitude);
            longitudeView = (TextView) itemView.findViewById(R.id.longitude);
            radiusView = (TextView) itemView.findViewById(R.id.radius);
        }
    }
}
