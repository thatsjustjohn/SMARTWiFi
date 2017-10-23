package com.example.android.smartwifi.sync;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by jtwyp6 on 10/21/17.
 */

public class SMARTWifiIntentService extends IntentService {
    public SMARTWifiIntentService() {super("SMARTWifiIntentService");}

    @Override
    protected void onHandleIntent(Intent intent) {
//      COMPLETED (12) Get the action from the Intent that started this Service
        String action = intent.getAction();

//      COMPLETED (13) Call ReminderTasks.executeTask and pass in the action to be performed
        SMARTWifiSyncTask.executeTask(this, action);
    }
}
