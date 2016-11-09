package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.SensorbergServiceMessage;
import com.sensorberg.sdk.receivers.GenericBroadcastReceiver;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.MessageDelayWindowLengthListener;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.resolver.BeaconEvent;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.Setter;

public class AndroidServiceScheduler implements ServiceScheduler, MessageDelayWindowLengthListener {

    private final Context context;

    private final AlarmManager alarmManager;

    private final Clock clock;

    private final PersistentIntegerCounter postToServiceCounter;

    @Setter
    private long messageDelayWindowLength;

    private final Set<Integer> repeatingPendingIntents = new HashSet<>();

    private final PendingIntentStorage pendingIntentStorage;

    public AndroidServiceScheduler(Context ctx, AlarmManager am, Clock clk, PersistentIntegerCounter integerCounter, long defaultMessageDelayWindowLength) {
        context = ctx;
        alarmManager = am;
        clock = clk;
        postToServiceCounter = integerCounter;
        messageDelayWindowLength = defaultMessageDelayWindowLength;
        pendingIntentStorage = new PendingIntentStorage(this, clock);
    }

    @SuppressLint("NewApi")
    @Override
    public void scheduleIntent(long index, long delayInMillis, Bundle content) {
        PendingIntent pendingIntent = getPendingIntent(index, content);
        scheduleAlarm(delayInMillis, pendingIntent);
    }

    @Override
    public void unscheduleIntent(int index) {
        alarmManager.cancel(getPendingIntent(index, null));
    }

    private PendingIntent getPendingIntent(int MSG_type) {
        Intent intent = new Intent(context, GenericBroadcastReceiver.class);
        intent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, MSG_type);
        intent.setAction("broadcast_repeating:///message_" + MSG_type);

        return PendingIntent.getBroadcast(context,
                -1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPendingIntent(long index, Bundle extras) {
        return getPendingIntent(index, extras, "");
    }

    private PendingIntent getPendingIntent(long index, Bundle extras, String prefix) {
        Intent intent = new Intent(context, GenericBroadcastReceiver.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        intent.setData(Uri.parse("sensorberg" + prefix + ":" + index));

        return PendingIntent.getBroadcast(context,
                -1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressLint("NewApi")
    private void scheduleAlarm(long delayInMillis, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
/*            alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP, clock.elapsedRealtime() + delayInMillis, messageDelayWindowLength,
                    pendingIntent);*/
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, clock.elapsedRealtime() + delayInMillis,
                    pendingIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, clock.elapsedRealtime() + delayInMillis, pendingIntent);
        }
    }

    @Override
    public void cancelServiceMessage(int index) {
        PendingIntent pendingIntent = getPendingIntent(index, new Bundle());
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void cancelIntent(int message) {
        PendingIntent pendingIntent = getPendingIntent(message);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void cancelAllScheduledTimer() {
        for (Integer messageType : repeatingPendingIntents) {
            cancelIntent(messageType);
        }
        repeatingPendingIntents.clear();
    }

    @Override
    public void scheduleRepeating(int MSG_type, long value, TimeUnit timeUnit) {
        long millis = TimeUnit.MILLISECONDS.convert(value, timeUnit);
        PendingIntent pendingIntent = getPendingIntent(MSG_type);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, clock.elapsedRealtime() + millis, millis, pendingIntent);
        repeatingPendingIntents.add(MSG_type);
    }

    @Override
    public void postToServiceDelayed(long delay, int type, Parcelable what, boolean surviveReboot) {
        int index = postToServiceCounter.next();
        postToServiceDelayed(delay, type, what, surviveReboot, index);
    }

    @Override
    public void postToServiceDelayed(long delayMillis, int type, Parcelable what, boolean surviveReboot, int index) {
        Bundle bundle = getScheduleBundle(index, type, what);
        scheduleIntent(index, delayMillis, bundle);

        if (surviveReboot) {
            pendingIntentStorage.add(index, clock.now() + delayMillis, 0, bundle);
        }
    }

    private Bundle getScheduleBundle(int index, int type, Parcelable what) {
        Bundle bundle = new Bundle();
        bundle.putInt(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, type);
        bundle.putParcelable(SensorbergServiceMessage.EXTRA_GENERIC_WHAT, what);
        bundle.putInt(SensorbergServiceMessage.EXTRA_GENERIC_INDEX, index);
        return bundle;
    }

    @Override
    public void postDeliverAtOrUpdate(Date deliverAt, BeaconEvent beaconEvent) {
        long delayInMillis = deliverAt.getTime() - clock.now();
        if (delayInMillis < 0) {
            Logger.log.beaconResolveState(beaconEvent, "scheduled time is in the past, dropping event.");
            return;
        }
        int index = postToServiceCounter.next();
        int hashcode = beaconEvent.hashCode();

        Bundle bundle = getScheduleBundle(index, SensorbergServiceMessage.GENERIC_TYPE_BEACON_ACTION, beaconEvent);
        PendingIntent pendingIntent = getPendingIntent(hashcode, bundle, "DeliverAt");

        scheduleAlarm(delayInMillis, pendingIntent);
        pendingIntentStorage.add(index, deliverAt.getTime(), hashcode, bundle);
    }

    @Override
    public void clearAllPendingIntents() {
        pendingIntentStorage.clearAllPendingIntents();
    }

    @Override
    public void restorePendingIntents() {
        pendingIntentStorage.restorePendingIntents();
    }

    @Override
    public void removeStoredPendingIntent(int index) {
        pendingIntentStorage.removeStoredPendingIntent(index);
    }
}
