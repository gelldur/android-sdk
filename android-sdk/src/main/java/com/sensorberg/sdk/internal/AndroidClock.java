package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.internal.interfaces.Clock;

import android.os.SystemClock;

public class AndroidClock implements Clock {
    @Override
    public long now() {
        return System.currentTimeMillis();
    }

    @Override
    public long elapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }
}
