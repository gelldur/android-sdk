package com.sensorberg.sdk.settings;

public final class TimeConstants {

    public static final long ONE_SECOND = 1000;

    public static final long ONE_MINUTE = 60 * ONE_SECOND;

    public static final long ONE_HOUR = 60 * ONE_MINUTE;

    public static final long ONE_DAY = 24 * ONE_HOUR;

    private TimeConstants() {
        throw new IllegalAccessError("Utility class");
    }
}
