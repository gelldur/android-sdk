package com.sensorberg.sdk.internal.interfaces;

import com.sensorberg.sdk.resolver.BeaconEvent;

import android.os.Bundle;
import android.os.Parcelable;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public interface ServiceScheduler {

    void scheduleIntent(long key, long delayInMillis, Bundle content);

    void unscheduleIntent(int index);

    void cancelServiceMessage(int index);

    void cancelIntent(int message);

    void cancelAllScheduledTimer();

    void scheduleRepeating(int MSG_index, long value, TimeUnit timeUnit);

    void postToServiceDelayed(long delay, int type, Parcelable what, boolean surviveReboot);

    void postToServiceDelayed(long delay, int type, Parcelable what, boolean surviveReboot, int index);

    void postDeliverAtOrUpdate(Date deliverAt, BeaconEvent beaconEvent);

    void clearAllPendingIntents();

    void restorePendingIntents();

    void removeStoredPendingIntent(int index);
}
