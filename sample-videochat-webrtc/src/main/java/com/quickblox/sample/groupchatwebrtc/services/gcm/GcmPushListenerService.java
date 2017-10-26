package com.quickblox.sample.groupchatwebrtc.services.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.core.utils.constant.GcmConsts;
import com.quickblox.sample.groupchatwebrtc.services.CallService;
import com.quickblox.users.model.QBUser;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import com.quickblox.sample.groupchatwebrtc.activities.LoginActivity;

/**
 * Created by tereha on 13.05.16.
 */
public class GcmPushListenerService extends GcmListenerService {
    private static final String TAG = GcmPushListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(GcmConsts.EXTRA_GCM_MESSAGE);
        Log.v(TAG, "From: " + from);
        Log.v(TAG, "Message: " + message);

        this.showNotification(message);

        SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
        if (sharedPrefsHelper.hasQbUser()) {
            Log.d(TAG, "App has logged in user");
            QBUser qbUser = sharedPrefsHelper.getQbUser();
            startLoginService(qbUser);
        }
    }

    private void startLoginService(QBUser qbUser){
        CallService.start(this, qbUser);
    }

    public void showNotification(String from) {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, LoginActivity.class), 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(getApplicationContext().getApplicationInfo().icon)
                .setContentTitle("Call from:")
                .setContentText(from)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}