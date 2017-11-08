package com.example.android.smartwifi.utilities;

import com.google.android.gms.location.Geofence;

/**
 * Created by jtwyp6 on 11/4/17.
 */

public class SGeofence {
    private final String id;
    private final double latitude;
    private final double longitude;
    private final float radius;
    private long expirationDuration;
    private int transitionType;
    private int loiteringDelay = 60000;

    public SGeofence(String geofenceId, double latitude, double longitude,
                     float radius, long expiration, int transition) {
        this.id = geofenceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.expirationDuration = expiration;
        this.transitionType = transition;
    }

    public String getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getRadius() {
        return radius;
    }

    public long getExpirationDuration() {
        return expirationDuration;
    }

    public int getTransitionType() {
        return transitionType;
    }

    public com.google.android.gms.location.Geofence buildGeofence() {
        com.google.android.gms.location.Geofence builtGeofence = new com.google.android.gms.location.Geofence.Builder().setRequestId(getId())
                .setTransitionTypes(transitionType)
                .setCircularRegion(getLatitude(), getLongitude(), getRadius())
                .setExpirationDuration(expirationDuration)
                .setLoiteringDelay(loiteringDelay).build();
        return builtGeofence;
    }


}
