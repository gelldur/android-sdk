package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.internal.PersistentIntegerCounter;
import com.sensorberg.sdk.test.TestGenericBroadcastReceiver;
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
import java.util.concurrent.TimeUnit;

public class TestServiceScheduler implements ServiceScheduler, MessageDelayWindowLengthListener {

    public static final String TAG = "TestServiceScheduler";

    private final Context context;

    private final AlarmManager alarmManager;

    private final Clock clock;

    private long messageDelayWindowLength;

    public TestServiceScheduler(Context ctx, AlarmManager am, Clock clk, PersistentIntegerCounter integerCounter, long messageDelayWindowLength) {
        context = ctx;
        alarmManager = am;
        clock = clk;
        this.messageDelayWindowLength = messageDelayWindowLength;
    }

    @Override
    public void scheduleRepeating(int MSG_index, long value, TimeUnit timeUnit) {
        android.util.Log.e(TAG, "NOT IMPLEMENTED");
    }

    @Override
    public void postToServiceDelayed(long delay, int type, Parcelable what, boolean surviveReboot) {
        android.util.Log.e(TAG, "NOT IMPLEMENTED");
    }

    @Override
    public void postToServiceDelayed(long delay, int type, Parcelable what, boolean surviveReboot, int index) {
        android.util.Log.e(TAG, "NOT IMPLEMENTED");
    }

    @Override
    public void cancelIntent(int message) {
        android.util.Log.e(TAG, "NOT IMPLEMENTED");
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

    @Override
    public void cancelAllScheduledTimer() {
        android.util.Log.e(TAG, "NOT IMPLEMENTED");
    }

    @Override
    public void cancelServiceMessage(int index) {
        android.util.Log.e(TAG, "NOT IMPLEMENTED");
    }

    @Override
    public void postDeliverAtOrUpdate(Date deliverAt, BeaconEvent beaconEvent) {
        android.util.Log.e(TAG, "NOT IMPLEMENTED");
    }

    @Override
    public void clearAllPendingIntents() {
        android.util.Log.e(TAG, "NOT IMPLEMENTED");
    }

    @Override
    public void restorePendingIntents() {
        android.util.Log.e(TAG, "NOT IMPLEMENTED");
    }

    @Override
    public void removeStoredPendingIntent(int index) {
        android.util.Log.e(TAG, "NOT IMPLEMENTED");
    }

    private PendingIntent getPendingIntent(long index, Bundle extras) {
        return getPendingIntent(index, extras, "");
    }

    private PendingIntent getPendingIntent(long index, Bundle extras, String prefix) {
        Intent intent = new Intent(context, TestGenericBroadcastReceiver.class);
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
            alarmManager
                    .setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP, clock.elapsedRealtime() + delayInMillis, messageDelayWindowLength,
                            pendingIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, clock.elapsedRealtime() + delayInMillis, pendingIntent);
        }
    }

    @Override
    public void setMessageDelayWindowLength(long messageDelayWindowLength) {
        this.messageDelayWindowLength = messageDelayWindowLength;
    }
}
