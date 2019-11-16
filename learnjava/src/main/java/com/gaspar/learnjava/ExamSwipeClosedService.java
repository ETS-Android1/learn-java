package com.gaspar.learnjava;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * A helper service that detects if the activity was swiped from the recent list.
 */
public class ExamSwipeClosedService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        //if activity is destroyed always cancel notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(notificationManager != null) notificationManager.cancel(ExamActivity.NOTIFICATION_REQUEST_CODE);
        stopSelf();
    }

}
