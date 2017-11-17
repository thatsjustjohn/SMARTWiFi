package com.example.android.smartwifi;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.smartwifi.data.accesspointdb.AccesspointContract;


/**
 * This GeoFenceCursorAdapter creates and binds ViewHolders, that hold the description and priority of a task,
 * to a RecyclerView to efficiently display data.
 */
public class AccessPointCursorAdapter extends RecyclerView.Adapter<AccessPointCursorAdapter.TaskViewHolder> {

    // Class variables for the Cursor that holds task data and the Context
    private Cursor mCursor;
    private Context mContext;


    /**
     * Constructor for the GeoFenceCursorAdapter that initializes the Context.
     *
     * @param mContext the current Context
     */
    public AccessPointCursorAdapter(Context mContext) {
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
                .inflate(R.layout.accesspoint_item, parent, false);

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
        int idIndex = mCursor.getColumnIndex(AccesspointContract.AccesspointEntry._ID);
        int accesspointIndex = mCursor.getColumnIndex(AccesspointContract.AccesspointEntry.COLUMN_ACCESSPOINT);
        int macIndex = mCursor.getColumnIndex(AccesspointContract.AccesspointEntry.COLUMN_MACADDRESS);


        mCursor.moveToPosition(position); // get to the right location in the cursor

        // Determine the values of the wanted data
        final int id = mCursor.getInt(idIndex);
        String accesspoint = mCursor.getString(accesspointIndex);
        String macAddress = mCursor.getString(macIndex);


        //Set values
        holder.itemView.setTag(id);
        holder.accesspointView.setText(accesspoint);
        holder.macView.setText(String.valueOf(macAddress));

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
        TextView accesspointView;
        TextView macView;


        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public TaskViewHolder(View itemView) {
            super(itemView);

            accesspointView = (TextView) itemView.findViewById(R.id.accesspointDescription);
            macView = (TextView) itemView.findViewById(R.id.accesspointMac);

        }
    }
}
