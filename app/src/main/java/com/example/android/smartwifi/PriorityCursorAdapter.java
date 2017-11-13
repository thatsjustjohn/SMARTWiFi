package com.example.android.smartwifi;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.smartwifi.data.geofencedb.GeofenceContract;
import com.example.android.smartwifi.data.prioritydb.PriorityContract;

import java.util.ArrayList;


/**
 * This GeoFenceCursorAdapter creates and binds ViewHolders, that hold the description and priority of a task,
 * to a RecyclerView to efficiently display data.
 */
public class PriorityCursorAdapter extends RecyclerView.Adapter<PriorityCursorAdapter.PriorityViewHolder> {

    // Class variables for the Cursor that holds task data and the Context
    private Cursor mCursor;
    private Context mContext;
    private ArrayList<String> mNetworkList;


    /**
     * Constructor for the GeoFenceCursorAdapter that initializes the Context.
     *
     * @param mContext the current Context
     */
    public PriorityCursorAdapter(Context mContext) {
        this.mContext = mContext;
    }


    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new TaskViewHolder that holds the view for each task
     */
    @Override
    public PriorityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.priority_item, parent, false);

        return new PriorityViewHolder(view);
    }


    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(PriorityViewHolder holder, int position) {

        // Indices for the _id, description, and priority columns
        //int idIndex = mCursor.getColumnIndex(PriorityContract.PriorityEntry._ID);
        //int networkIndex = mCursor.getColumnIndex(PriorityContract.PriorityEntry.COLUMN_ACCESSPOINT);
        //int priorityIndex = mCursor.getColumnIndex(PriorityContract.PriorityEntry.COLUMN_PRIORITY);


        //mCursor.moveToPosition(position); // get to the right location in the cursor

        // Determine the values of the wanted data
        //final int id = mCursor.getInt(idIndex);
        //String network = mCursor.getString(networkIndex);
        //double priority = mCursor.getDouble(priorityIndex);

        String network = mNetworkList.get(position);
        //Set values
        holder.itemView.setTag(position);
        holder.networkDescriptionView.setText(network);
        holder.priorityView.setText(String.valueOf(position));

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
        if (mNetworkList == null) {
            return 0;
        }
        return mNetworkList.size();
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

    public void setNetworkData(ArrayList<String> networkList){
        mNetworkList = networkList;
        notifyDataSetChanged();
    }

    public void onItemMove(int fromPosition, int toPosition){
        String previous = mNetworkList.remove(fromPosition);
        mNetworkList.add(toPosition > fromPosition ? toPosition - 1 : toPosition, previous);
        notifyItemMoved(fromPosition, toPosition);
    }

    // Inner class for creating ViewHolders
    class PriorityViewHolder extends RecyclerView.ViewHolder {

        // Class variables for the task description and priority TextViews
        TextView networkDescriptionView;
        TextView priorityView;



        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public PriorityViewHolder(View itemView) {
            super(itemView);

            networkDescriptionView = (TextView) itemView.findViewById(R.id.networkDescription);
            priorityView = (TextView) itemView.findViewById(R.id.priority);
        }
    }
}
