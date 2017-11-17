package com.example.android.smartwifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import java.nio.channels.ScatteringByteChannel;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jtwyp6 on 7/21/17.  ACCESS POINT ADAPTER
 */

public class APAdapter extends RecyclerView.Adapter<APAdapter.APAdapterViewHolder> {

    private List<ScanResult> mAPData;

    //private String[] mAPData;
    /**
    * An on-click handler that we've defined to make it easy for an Activity to interface with
    * our RecyclerView
    */
    private final APAdapterOnClickHandler mClickHandler;


    /**
     * The interface that receives onClick messages.
     */
    public interface APAdapterOnClickHandler{
        void onClick(ScanResult apItemInList);
    }

    /**
     * Creates a APAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public APAdapter(APAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a AP list item.
     */
    public class APAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        public final TextView mAPTextView;

        public APAdapterViewHolder(View view) {
            super(view);
            mAPTextView = (TextView) view.findViewById(R.id.tv_ap_data);
            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            //<-------This will change with the list information --------------->
            ScanResult apItemInList = mAPData.get(getAdapterPosition());
            mClickHandler.onClick(apItemInList);
        }
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new APAdapterViewHolder that holds the View for each list item
     */
    @Override
    public APAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.ap_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new APAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the Access Point
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param apAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(APAdapterViewHolder apAdapterViewHolder, int position) {
        String apForThisLocation = "SSID :: " + mAPData.get(position).SSID
                + "\nStrength :: " + mAPData.get(position).level
                + "\nBSSID :: " + mAPData.get(position).BSSID
                + "\nChannel :: "
                + convertFrequencyToChannel(mAPData.get(position).frequency)
                + "\nFrequency :: " + mAPData.get(position).frequency
                + "\nCapability :: " + mAPData.get(position).capabilities;

        apAdapterViewHolder.mAPTextView.setText(apForThisLocation);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our list
     */
    @Override
    public int getItemCount() {
        if (null == mAPData) return 0;
        return mAPData.size();
    }

    /**
     * This method is used to set the AP List on a APAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new APAdapter to display it.
     *
     * @param apData The new AP data to be displayed.
     */
    public void setAPData(List<ScanResult> apData) {
        mAPData = apData;
        notifyDataSetChanged();
    }

    public List<ScanResult> cleanAPData(List<ScanResult> apData){
        int count = 0;
        Iterator<ScanResult> scanIterator = apData.iterator();
        while(scanIterator.hasNext()){
            ScanResult scanResult = scanIterator.next();
            if(scanResult.SSID.equals("") || scanResult.SSID.equals("<unknown ssid>") || scanResult.SSID == null){
                scanIterator.remove();
                count++;
            }
        }
        Log.d("Data", "cleaned up " + String.valueOf(count) + " entries");
        return apData;
    }

    /**
     *  This method is used to convert Frequency to Channel
     * @param freq the frewquency to be convert to channel
     * @return the channel
     */

    public static int convertFrequencyToChannel(int freq) {
        if (freq>= 2412 &&freq<= 2484) { return (freq - 2412) / 5 + 1; } else if (freq>= 5170 &&freq<= 5825) {
            return (freq - 5170) / 5 + 34;
        } else {
            return -1;
        }
    }

}
