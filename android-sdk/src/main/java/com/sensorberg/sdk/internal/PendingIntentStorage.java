package com.sensorberg.sdk.internal;

import com.sensorberg.SensorbergSdk;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;

import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;

import javax.inject.Inject;

public class PendingIntentStorage {

    private final ServiceScheduler serviceScheduler;

    private final SQLiteStore storage;

    @Inject
    protected Context context;

    private Clock clock;

    public PendingIntentStorage(ServiceScheduler serviceScheduler, Clock clk) {
        this.serviceScheduler = serviceScheduler;
        clock = clk;
        SensorbergSdk.getComponent().inject(this);
        storage = new SQLiteStore("pendingIntentStorage.sqlite", context);
    }

    public void add(int index, long timestamp, int identifier, Bundle bundle) {
        storage.deleteByIdentifier(identifier);
        storage.put(new SQLiteStore.Entry(index, timestamp, identifier, bundle));
    }

    public void restorePendingIntents() {
        storage.deleteOlderThan(clock.now());
        ArrayList<SQLiteStore.Entry> entries = storage.loadRegistry();
        for (SQLiteStore.Entry entry : entries) {
            long relativeFromNow = entry.timestamp - clock.now();
            serviceScheduler.scheduleIntent(entry.index, relativeFromNow, entry.bundle);
        }
    }

    public void clearAllPendingIntents() {
        ArrayList<SQLiteStore.Entry> entries = storage.loadRegistry();
        for (SQLiteStore.Entry entry : entries) {
            serviceScheduler.unscheduleIntent(entry.index);
        }
        storage.clear();
    }

    public void removeStoredPendingIntent(int index) {
        storage.delete(index);
    }
}
