package com.example.android.smartwifi;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by jtwyp6 on 7/21/17.
 */

public class DetailAPActivity extends AppCompatActivity{
    private String mAP;
    private TextView mAPDisplay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_detail);

        mAPDisplay = (TextView) findViewById(R.id.tv_display_ap);

        Intent intentThatStartedThisActivity = getIntent();

        if(intentThatStartedThisActivity != null){
            if(intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)){
                mAP = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);
                mAPDisplay.setText(mAP);
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
