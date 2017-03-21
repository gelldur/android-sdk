package com.sensorberg;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.action.UriMessageAction;
import com.sensorberg.sdk.action.VisitWebsiteAction;
import com.sensorberg.sdk.model.BeaconId;

import java.util.UUID;

/**
 * This is conversion-ready convenient drop-in class for receiving and processing SDK {@link Action}s
 * It receives and parses intents from Sensorberg SDK Service,
 * as well as from notifications it generated (if any).
 *
 * If {@link Action} generated by SDK is received it's calling (in order):
 * {@link #onAction(Action, BeaconId, Context) onAction}, then depending on Action type either
 * {@link #onVisitWebsiteAction(VisitWebsiteAction, BeaconId, Context) onVisitWebsiteAction},
 * {@link #onInAppAction(InAppAction, BeaconId, Context) onInAppAction},
 * {@link #onUriAction(UriMessageAction, BeaconId, Context) onUriAction}.
 * You may override those methods to process your action.
 *
 * If you wish to also send notification supply it in {@link #onGetNotification(Action, BeaconId, Uri, Context) onGetNotification},
 * using {@link #getNotificationContentPendingIntent(Action, BeaconId, Uri, Bundle, Context, int) getNotificationContentPendingIntent}
 * as your {@link Notification#contentIntent}. The SDK will be notified that you've attempted to show user
 * the action and will update conversion automatically.
 *
 * When user taps notification you will be notified in {@link #onNotificationSuccess(Action, BeaconId, Uri, Bundle, Context) onNotificationSuccess},
 * and the SDK will be notified the conversion was successful automatically.
 *
 * Remember to add your implementation of this ActionReceiver to Manifest file with following intent-filters:
 * * com.sensorberg.android.PRESENT_ACTION
 * * com.sensorberg.android.CONVERSION_SUCCESS
 * * com.sensorberg.android.CONVERSION_DELETE
 * And using android:process=".sensorberg" (Yes, it runs in separate process).
 *
 * For sample implementation please refer to this blog post: //TODO
 */
public abstract class ActionReceiver extends BroadcastReceiver {

    private static final String TAG = ActionReceiver.class.getName();

    private static final String ACTION_PRESENT = "com.sensorberg.android.PRESENT_ACTION";
    private static final String ACTION_CONVERSION_SUCCESS = "com.sensorberg.android.CONVERSION_SUCCESS";
    private static final String ACTION_CONVERSION_DELETE = "com.sensorberg.android.CONVERSION_DELETE";

    private static final String EXTRA_ACTION = "action";
    private static final String EXTRA_BEACON = "beacon";
    private static final String EXTRA_URI = "uri";
    private static final String EXTRA_BUNDLE = "bundle";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Basic validation
        if (intent == null) {
            Logger.log.debug("Received null intent");
            return;
        }
        String intentAction = intent.getAction();
        if (intentAction == null) {
            Logger.log.debug("Received intent without intent action string");
            return;
        }
        //Recognize kind of intent and act accordingly
        if (intentAction.equals(ACTION_PRESENT)) {
            Action sdkAction = intent.getExtras().getParcelable(Action.INTENT_KEY);
            if (sdkAction == null) {
                Logger.log.debug("Received intent without SDK Action");
                return;
            }
            BeaconId beaconId = intent.getExtras().getParcelable(BeaconId.INTENT_KEY);
            Uri uri = processAction(sdkAction, beaconId, context);
            Notification notification = onGetNotification(sdkAction, beaconId, uri, context);
            if (notification != null) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(sdkAction.getInstanceUuid().hashCode(), notification);
                SensorbergSdk.notifyActionShowAttempt(sdkAction.getInstanceUuid(), context);
            }
        } else if (intentAction.equals(ACTION_CONVERSION_SUCCESS) || intentAction.equals(ACTION_CONVERSION_DELETE)) {
            Action action = intent.getParcelableExtra(EXTRA_ACTION);
            if (action == null) {
                Logger.log.debug("Received "+ACTION_CONVERSION_SUCCESS+" intent is missing SDK Action");
            }
            BeaconId beaconId = intent.getParcelableExtra(EXTRA_BEACON);
            if (beaconId == null) {
                Logger.log.debug("Received "+ACTION_CONVERSION_SUCCESS+" intent is missing BeaconId");
            }
            Uri uri = intent.getParcelableExtra(EXTRA_URI);
            Bundle bundle = intent.getBundleExtra(EXTRA_BUNDLE);
            if (intentAction.equals(ACTION_CONVERSION_SUCCESS)) {
                onNotificationSuccess(action, beaconId, uri, bundle, context);
            } else {
                onNotificationDeleted(action, beaconId, uri, bundle, context);
            }
        } else {
            Logger.log.debug("Received intent with unknown intent action string: "+intentAction);
        }
    }

    private Uri processAction(Action action, BeaconId beaconId, Context context) {
        onAction(action, beaconId, context);
        Uri uri = null;
        switch (action.getType()) {
            case MESSAGE_URI:
                UriMessageAction uriMessageAction = (UriMessageAction) action;
                uri = Uri.parse(uriMessageAction.getUri());
                onUriAction(uriMessageAction, beaconId, context);
                break;
            case MESSAGE_WEBSITE:
                VisitWebsiteAction visitWebsiteAction = (VisitWebsiteAction) action;
                uri = visitWebsiteAction.getUri();
                onVisitWebsiteAction(visitWebsiteAction, beaconId, context);
                break;
            case MESSAGE_IN_APP:
                InAppAction inAppAction = (InAppAction) action;
                uri = inAppAction.getUri();
                onInAppAction(inAppAction, beaconId, context);
                break;
        }
        return uri;
    }

    /**
     * Process every {@link Action} received from SDK here,
     * before notification is shown (if any)
     * @param action {@link Action} as received from SDK.
     * @param beaconId {@link BeaconId} as received from SDK.
     * @param context This ActionReceiver's context.
     */
    public abstract void onAction(Action action, BeaconId beaconId, Context context);

    /**
     * Process {@link UriMessageAction} received from SDK here,
     * before notification is shown (if any)
     * @param action {@link UriMessageAction} as received from SDK.
     * @param beaconId {@link BeaconId} as received from SDK.
     * @param context This ActionReceiver's context.
     */
    public abstract void onUriAction(UriMessageAction action, BeaconId beaconId, Context context);

    /**
     * Process {@link VisitWebsiteAction} received from SDK here,
     * before notification is shown (if any)
     * @param action {@link VisitWebsiteAction} as received from SDK.
     * @param beaconId {@link BeaconId} as received from SDK.
     * @param context This ActionReceiver's context.
     */
    public abstract void onVisitWebsiteAction(VisitWebsiteAction action, BeaconId beaconId, Context context);

    /**
     * Process {@link InAppAction} received from SDK here,
     * before notification is shown (if any)
     * @param action {@link InAppAction} as received from SDK.
     * @param beaconId {@link BeaconId} as received from SDK.
     * @param context This ActionReceiver's context.
     */
    public abstract void onInAppAction(InAppAction action, BeaconId beaconId, Context context);

    /**
     * Build your notification here if you wish to use conversion-ready implementation.
     * The conversion-ready implementation will let SDK know that user has tapped your notification.
     * You will get result of tapping in {@link #onNotificationSuccess(Action, BeaconId, Uri, Bundle, Context) onNotificationSuccess}.
     * Use result of {@link #getNotificationContentPendingIntent(Action, BeaconId, Uri, Bundle, Context, int) getNotificationContentPendingIntent} as {@link Notification#contentIntent}.
     * Without above the SDK won't get to know about successfull conversion, unless you'll let it know
     * by {@link SensorbergSdk#notifyActionSuccess(String, Context) SensorbergSdk.notifyActionSuccess}.
     * @param action {@link Action} as received from SDK.
     * @param beaconId {@link BeaconId} as received from SDK.
     * @param context This ActionReceiver's context.
     * @return Notification to show, or null if no notification should be shown.
     */
    public abstract Notification onGetNotification(Action action, BeaconId beaconId, Uri uri, Context context);

    /**
     * This callback will let you know when user tapped on your notification supplied in {@link #onGetNotification(Action, BeaconId, Uri, Context) onGetNotification}
     * You may proceed with e.g. opening web browser to show webpage or redirect user to your app's Activity.
     * Remember to always call super to let SDK know about conversion!
     * @param action {@link Action as received from SDK}
     * @param beaconId {@link BeaconId as received from SDK}
     * @param uri {@link Uri as received from SDK}
     * @param bundle Bundle you've supplied to {@link #getNotificationContentPendingIntent(Action, BeaconId, Uri, Bundle, Context, int) getNotificationContentPendingIntent}
     *               in {@link #onGetNotification(Action, BeaconId, Uri, Context) onGetNotification}
     * @param context This ActionReceiver's context.
     */
    public void onNotificationSuccess(Action action, BeaconId beaconId, Uri uri, Bundle bundle, Context context) {
        SensorbergSdk.notifyActionSuccess(action.getInstanceUuid(), context);
    }

    /**
     * This callback will let you know when user swiped out your notification (or cleared all notifications)
     * which you supplied in {@link #onGetNotification(Action, BeaconId, Uri, Context) onGetNotification}
     * Remember to always call super to let SDK know about conversion!
     * @param action {@link Action as received from SDK}
     * @param beaconId {@link BeaconId as received from SDK}
     * @param uri {@link Uri as received from SDK}
     * @param bundle Bundle you've supplied to {@link #getNotificationDeletedPendingIntent(Action, BeaconId, Uri, Bundle, Context, int) getNotificationDeletedPendingIntent}
     *               in {@link #onGetNotification(Action, BeaconId, Uri, Context) onGetNotification}
     * @param context This ActionReceiver's context.
     */
    private void onNotificationDeleted(Action action, BeaconId beaconId, Uri uri, Bundle bundle, Context context) {
        //TODO This is just a stub in case we want to change conversion type based on user dismissing the notification in the future.
        SensorbergSdk.notifyActionRejected(action.getInstanceUuid(), context);
    }

    /**
     * Get {@link PendingIntent} that works with this {@link ActionReceiver} conversion-ready implementation.
     * Use it as {@link Notification#contentIntent}
     * @param action Pass here SDK Action as received in {@link #onGetNotification(Action, BeaconId, Uri, Context)} onGetNotification}.
     * @param beaconId Pass here BeaconId as received in {@link #onGetNotification(Action, BeaconId, Uri, Context)} onGetNotification}.
     * @param uri Pass here Uri as received in {@link #onGetNotification(Action, BeaconId, Uri, Context)} onGetNotification}.
     * @param bundle Bundle you'll receive in {@link #onNotificationSuccess(Action, BeaconId, Uri, Bundle, Context) onNotificationSuccess}.
     * @param context This ActionReceiver's context.
     * @param flags Flags for returned PendingIntent e.g. {@link PendingIntent#FLAG_UPDATE_CURRENT}
     * @return {@link PendingIntent} that will be received in this {@link ActionReceiver} with {@link Bundle} you've provided.
     */
    public PendingIntent getNotificationContentPendingIntent(Action action, BeaconId beaconId, Uri uri, Bundle bundle, Context context, int flags) {
        return getPendingIntent(ACTION_CONVERSION_SUCCESS, action, beaconId, uri, bundle, context, flags);
    }

    /**
     * Get {@link PendingIntent} that works with this {@link ActionReceiver} conversion-ready implementation.
     * Use it as {@link Notification#deleteIntent}
     * @param action Pass here SDK Action as received in {@link #onGetNotification(Action, BeaconId, Uri, Context)} onGetNotification}.
     * @param beaconId Pass here BeaconId as received in {@link #onGetNotification(Action, BeaconId, Uri, Context)} onGetNotification}.
     * @param uri Pass here Uri as received in {@link #onGetNotification(Action, BeaconId, Uri, Context)} onGetNotification}.
     * @param bundle Bundle you'll receive in {@link #onNotificationDeleted(Action, BeaconId, Uri, Bundle, Context) onNotificationDeleted}.
     * @param context This ActionReceiver's context.
     * @param flags Flags for returned PendingIntent e.g. {@link PendingIntent#FLAG_UPDATE_CURRENT}
     * @return {@link PendingIntent} that will be received in this {@link ActionReceiver} with {@link Bundle} you've provided.
     */
    private PendingIntent getNotificationDeletedPendingIntent(Action action, BeaconId beaconId, Uri uri, Bundle bundle, Context context, int flags) {
        return getPendingIntent(ACTION_CONVERSION_DELETE, action, beaconId, uri, bundle, context, flags);
    }

    private PendingIntent getPendingIntent(String actionString, Action action, BeaconId beaconId, Uri uri, Bundle bundle, Context context, int flags) {
        Intent intent = new Intent(actionString);
        intent.setClass(context, this.getClass());
        if (action == null) {
            Logger.log.debug("Missing SDK Action! Action will be null in onNotificationSuccess");
        }
        if (beaconId == null) {
            Logger.log.debug("Missing BeaconId! BeaconId will be null in onNotificationSuccess");
        }
        intent.putExtra(EXTRA_ACTION, action);
        intent.putExtra(EXTRA_BEACON, (Parcelable) beaconId);
        intent.putExtra(EXTRA_URI, uri);
        intent.putExtra(EXTRA_BUNDLE, bundle);
        return PendingIntent.getBroadcast(context, action.getInstanceUuid().hashCode(), intent, flags);
    }
}
