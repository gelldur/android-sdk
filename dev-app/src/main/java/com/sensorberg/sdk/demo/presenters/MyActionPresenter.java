package com.sensorberg.sdk.demo.presenters;

import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.action.UriMessageAction;
import com.sensorberg.sdk.action.VisitWebsiteAction;
import com.sensorberg.sdk.demo.DemoActivity;
import com.sensorberg.sdk.testApp.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class MyActionPresenter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Action action = intent.getExtras().getParcelable(Action.INTENT_KEY);
        String delayString = String.format("m delay : %d", action.getDelayTime());
        switch (action.getType()) {
            case MESSAGE_URI:
                UriMessageAction uriMessageAction = (UriMessageAction) action;
                showNotification(context, action.getUuid().hashCode(), uriMessageAction.getTitle(), uriMessageAction.getContent() + delayString,
                        Uri.parse(uriMessageAction.getUri()), action);
                break;
            case MESSAGE_WEBSITE:
                VisitWebsiteAction visitWebsiteAction = (VisitWebsiteAction) action;
                showNotification(context, action.getUuid().hashCode(), visitWebsiteAction.getSubject(), visitWebsiteAction.getBody() + delayString,
                        visitWebsiteAction.getUri(), action);
                break;
            case MESSAGE_IN_APP:
                InAppAction inAppAction = (InAppAction) action;
                showNotification(context, action.getUuid().hashCode(), inAppAction.getSubject(), inAppAction.getBody() + delayString,
                        inAppAction.getUri(), action);
                break;
        }
    }

    public static void showNotification(Context context, int id, String title, String content, Uri uri, Action action) {

        PendingIntent openApplicationWithAction = PendingIntent.getActivity(
                context,
                0,
                DemoActivity.getIntent(context, action),
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(context)
                .setContentIntent(openApplicationWithAction)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setShowWhen(true)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }
}