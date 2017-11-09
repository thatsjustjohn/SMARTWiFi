package com.example.android.smartwifi;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.smartwifi.data.geofencedb.GeofenceContract;

public class AddGeoFenceActivity extends AppCompatActivity {

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_geo_fence);
    }

    /**
     * onClickAddTask is called when the "ADD" button is clicked.
     * It retrieves user input and inserts that new task data into the underlying database.
     */
    public void onClickAddGeoFence(View view) {
        // Not yet implemented
        // Check if EditText is empty, if not retrieve input and store it in a ContentValues object
        // If the EditText input is empty -> don't create an entry
        String geoFenceDescription = ((EditText) findViewById(R.id.editTextGeoFenceDescription)).getText().toString();
        if (geoFenceDescription.length() == 0) {
            missingInformationSnackBar(view);
            return;
        }
        String geoFenceLatitude = ((EditText) findViewById(R.id.editTextGeoFenceLatitude)).getText().toString();
        if (geoFenceLatitude.length() == 0) {
            missingInformationSnackBar(view);
            return;
        }
        String geoFenceLongitude = ((EditText) findViewById(R.id.editTextGeoFenceLongitude)).getText().toString();
        if (geoFenceLongitude.length() == 0) {
            missingInformationSnackBar(view);
            return;
        }
        String geoFenceRadius = ((EditText) findViewById(R.id.editTextGeoFenceRadius)).getText().toString();
        if (geoFenceRadius.length() == 0) {
            missingInformationSnackBar(view);
            return;
        }


        // Insert new task data via a ContentResolver
        // Create new empty ContentValues object
        ContentValues contentValues = new ContentValues();
        // Put the Geo Fence Data into the ContentValues
        contentValues.put(GeofenceContract.GeofenceEntry.COLUMN_DESCRIPTION, geoFenceDescription);
        contentValues.put(GeofenceContract.GeofenceEntry.COLUMN_LATITUDE, geoFenceLatitude);
        contentValues.put(GeofenceContract.GeofenceEntry.COLUMN_LONGITUDE, geoFenceLongitude);
        contentValues.put(GeofenceContract.GeofenceEntry.COLUMN_RADIUS, geoFenceRadius);

        // Insert the content values via a ContentResolver
        Uri uri = getContentResolver().insert(GeofenceContract.GeofenceEntry.CONTENT_URI, contentValues);

        // Display the URI that's returned with a Toast
        // [Hint] Don't forget to call finish() to return to MainActivity after this insert is complete
        if(uri != null) {
            Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
        }

        // Finish activity (this returns back to MainActivity)
        finish();

    }

    public void onClickGetLocation(View view){
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(locationManager != null) {
            Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            if(location != null)
            {
                ((EditText) findViewById(R.id.editTextGeoFenceLatitude)).setText(String.valueOf(location.getLatitude()));
                ((EditText) findViewById(R.id.editTextGeoFenceLongitude)).setText(String.valueOf(location.getLongitude()));

            }else
            {
                Snackbar.make(view, "Error with GPS location is NULL", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();            }
        }


    }

    public void missingInformationSnackBar(View view){
        Snackbar.make(view, "Every field Must be filed", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
