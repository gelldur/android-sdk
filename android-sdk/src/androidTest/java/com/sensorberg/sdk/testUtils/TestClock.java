package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.internal.interfaces.Clock;

public class TestClock implements Clock {

    private long nowInMillis = 0;

    @Override
    public long now() {
        return nowInMillis;
    }

    @Override
    public long elapsedRealtime() {
        return nowInMillis;
    }

    public void setNowInMillis(long nowInMillis) {
        this.nowInMillis = nowInMillis;
    }

    public void increaseTimeInMillis(long value) {
        setNowInMillis(nowInMillis + value);
    }
}
