package com.example.android.smartwifi;

import android.Manifest;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;

import com.example.android.smartwifi.utilities.SGeofence;
import com.example.android.smartwifi.utilities.GeofenceUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.Map;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Location current = getLocation();
        // Add a marker in Sydney and move the camera
        if(current != null) {
            LatLng cur = new LatLng(current.getLatitude(), current.getLongitude());
            mMap.addMarker(new MarkerOptions().position(cur).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(cur));
        }
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        displayGeofences();

        /*Add a marker in Sydney and move the camera
        if(current != null) {
            LatLng cur = new LatLng(current.getLatitude(), current.getLongitude());
            mMap.addMarker(new MarkerOptions().position(cur).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(cur));
        }*/
    }

    private Location getLocation() {
        locationManager =(LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&ActivityCompat.checkSelfPermission(
                getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return null;
        }
        if(locationManager !=null)
        {
            Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            if (location != null) {
                return location;
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Error with GPS location is NULL", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
        return null;
    }

    protected void displayGeofences() {
        GeofenceUtils.getInstance().loadGeoFencesFromDB(this);
        HashMap<String, SGeofence> geofences = GeofenceUtils
                .getInstance().getGeofences();

        if(geofences.isEmpty()){
            Snackbar.make(findViewById(android.R.id.content), "No Geofences in DataBase", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        for (Map.Entry<String, SGeofence> item : geofences.entrySet()) {
            SGeofence gfence = item.getValue();

            CircleOptions circleOptions1 = new CircleOptions()
                    .center(new LatLng(gfence.getLatitude(), gfence.getLongitude()))
                    .radius(gfence.getRadius()).strokeColor(Color.BLACK)
                    .strokeWidth(2).fillColor(0x500000ff);
            mMap.addCircle(circleOptions1);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
