package com.example.android.smartwifi;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by jtwyp6 on 7/21/17.
 */

public class DetailAPActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_detail);

        TextView mAPDisplay = (TextView) findViewById(R.id.tv_display_ap);
        TextView mAPBSSID = (TextView) findViewById(R.id.tv_display_bssid);
        TextView mAPRSSI = (TextView) findViewById(R.id.tv_display_rssi);
        TextView mAPFreq = (TextView) findViewById(R.id.tv_display_freq);
        TextView mAPChannel = (TextView) findViewById(R.id.tv_display_channel);
        Intent intentThatStartedThisActivity = getIntent();

        if(intentThatStartedThisActivity != null){
            Bundle extra = intentThatStartedThisActivity.getExtras();
            if(extra != null){
                String mAP = extra.getString("SSID");
                String mBSSID = extra.getString("BSSID");
                String mRSSI = extra.getString("RSSI");
                String mFreq = extra.getString("Freq");
                String mChannel = extra.getString("Channel");
                mAPDisplay.setText(mAP);
                mAPBSSID.setText(mBSSID);
                mAPRSSI.setText(mRSSI);
                mAPFreq.setText(mFreq);
                mAPChannel.setText(mChannel);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ap_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
