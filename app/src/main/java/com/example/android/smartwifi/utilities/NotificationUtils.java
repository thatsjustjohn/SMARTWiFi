package com.example.android.smartwifi.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.android.smartwifi.MainActivity;
import com.example.android.smartwifi.R;
import com.example.android.smartwifi.sync.SMARTWifiIntentService;
import com.example.android.smartwifi.sync.SMARTWifiSyncTask;
import com.google.android.gms.location.Geofence;

/**
 * Created by jtwyp6 on 10/21/17.
 */

public class NotificationUtils {
    private static final int WIFI_THRESHOLD_NOTIFICATION_ID = 1138;
    public static final int GEO_FENCE_NOTIFICATION_ID = 20;

    /**
     * This pending intent id is used to uniquely reference the pending intent
     */
    private static final int WIFI_THRESHOLD_PENDING_INTENT_ID = 3417;

    private static final String WIFI_THRESHOLD_NOTIFICATION_CHANNEL_ID = "threshold_notification_channel";
    private static final int ACTION_DISMISS_PENDING_INTENT_ID = 14;
    private static final int ACTION_SWITCH_PENDING_INTENT_ID = 1;


    public static void clearAllNotifications(Context context){
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }


    public static void askUserToSwitchNonPriority(Context context){

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    WIFI_THRESHOLD_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context) //This should need a string, but whatever
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentTitle(context.getString(R.string.threshold_reminder_notification_title))
                        .setContentText(context.getString(R.string.threshold_reminder_notification_body))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(
                                context.getString(R.string.threshold_reminder_notification_body)))
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setContentIntent(contentIntent(context))
                        .addAction(switchPriorityThresholdAction(context))
                        .addAction(dismissPriorityWifiThresholdAction(context))
                        .setAutoCancel(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        // COMPLETED (11) Get a NotificationManager, using context.getSystemService(Context.NOTIFICATION_SERVICE);


        // COMPLETED (12) Trigger the notification by calling notify on the NotificationManager.
        // Pass in a unique ID of your choosing for the notification and notificationBuilder.build()
        notificationManager.notify(WIFI_THRESHOLD_NOTIFICATION_ID, notificationBuilder.build());
    }


    private static NotificationCompat.Action dismissPriorityWifiThresholdAction(Context context) {
        Intent dismissPriorityWifiThresholdIntent = new Intent(context, SMARTWifiIntentService.class);
        dismissPriorityWifiThresholdIntent.setAction(SMARTWifiSyncTask.ACTION_DISMISS_PRIORITY_WIFI_THRESHOLD);
        PendingIntent dismissPriorityWifiThresholdPendingIntent = PendingIntent.getService(
                context,
                ACTION_DISMISS_PENDING_INTENT_ID,
                dismissPriorityWifiThresholdIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action dismissPriorityThresholdAction = new NotificationCompat.Action(R.drawable.ic_info_black_24dp,
                "DISMISS",
               dismissPriorityWifiThresholdPendingIntent);

        return dismissPriorityThresholdAction;
    }

    private static NotificationCompat.Action switchPriorityThresholdAction(Context context) {

        Intent switchPriorityThresholdIntent = new Intent(context, SMARTWifiIntentService.class);

        switchPriorityThresholdIntent.setAction(SMARTWifiSyncTask.ACTION_SWITCH_PRIORITY_WIFI_THRESHOLD);

        PendingIntent switchPriorityThresholdPendingIntent = PendingIntent.getService(
                context,
                ACTION_SWITCH_PENDING_INTENT_ID,
                switchPriorityThresholdIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Action switchPriorityThresholdAction = new NotificationCompat.Action(R.drawable.ic_sync_black_24dp,
                "SWITCH",
                switchPriorityThresholdPendingIntent);

        return switchPriorityThresholdAction;
    }

    private static PendingIntent contentIntent(Context context){
        Intent startActivityIntent = new Intent(context, MainActivity.class);

        return PendingIntent.getActivity(
                context,
                WIFI_THRESHOLD_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }



}
