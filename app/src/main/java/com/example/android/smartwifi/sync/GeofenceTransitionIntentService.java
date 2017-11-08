package com.example.android.smartwifi.sync;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.smartwifi.MainActivity;
import com.example.android.smartwifi.R;
import com.example.android.smartwifi.utilities.GeofenceNotification;
import com.example.android.smartwifi.utilities.SGeofence;
import com.example.android.smartwifi.utilities.GeofenceUtils;
import com.example.android.smartwifi.utilities.GeofenceErrorMessagesUtils;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jtwyp6 on 11/4/17.
 */

public class GeofenceTransitionIntentService extends IntentService {
    protected static final String TAG = "geofence-transitions";
    public static final int NOTIFICATION_ID = 1;
    static public int accuracy;

    public GeofenceTransitionIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessagesUtils.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        } else if (accuracy < 50) {

            Log.d("GEO", "GeofenceReceiver : Transition -> "
                    + geofencingEvent.getGeofenceTransition());

            //get transition type
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
                    || geofenceTransition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL
                    || geofenceTransition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT) {

                // Get the geofences that were triggered. A single event can trigger multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                boolean changeWifi = false;
                for (Geofence geofence : triggeringGeofences) {

                    //GET THE SGEO FOR THE OFFENSE
                    SGeofence smartgeo = GeofenceUtils.getInstance().getGeofences().get(geofence.getRequestId());

                    String transitionName = "";
                    switch (geofenceTransition) {
                        case Geofence.GEOFENCE_TRANSITION_DWELL:
                            transitionName = "dwell";
                            break;

                        case Geofence.GEOFENCE_TRANSITION_ENTER:
                            transitionName = "enter";
                            break;

                        case Geofence.GEOFENCE_TRANSITION_EXIT:
                            transitionName = "exit";
                            break;
                    }
                    Log.d(TAG, smartgeo.getId() + ":" + transitionName);
                    GeofenceNotification geofenceNotification = new GeofenceNotification(
                            this);
                    geofenceNotification
                            .displayNotification(smartgeo, geofenceTransition);
                }
            } else {
                Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
            }
        }else{
            Log.e(TAG, "ACCURACY IS SHIT");

        }
    }

    private String getGeofenceTransitionDetails(Context context, int geofenceTransition, List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "Dwelling";
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_info_black_24dp)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_info_black_24dp))
                .setColor(Color.BLUE)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }
}
