
package com.quickblox.instadate.groupchatwebrtc.utils;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.instadate.groupchatwebrtc.R;

import java.util.ArrayList;

/**
 * Created by tereha on 13.05.16.
 */
public class PushNotificationSender {

    public static void sendPushMessage(ArrayList<Integer> recipients, String message) {
        // Send Push: create QuickBlox Push Notification Event
        QBEvent qbEvent = new QBEvent();
        qbEvent.setNotificationType(QBNotificationType.PUSH);
        qbEvent.setEnvironment(QBEnvironment.DEVELOPMENT);
        // Generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)
        qbEvent.setMessage(message);

        StringifyArrayList<Integer> userIds = new StringifyArrayList<>(recipients);
        qbEvent.setUserIds(userIds);

        QBPushNotifications.createEvent(qbEvent).performAsync(null);
    }
}
