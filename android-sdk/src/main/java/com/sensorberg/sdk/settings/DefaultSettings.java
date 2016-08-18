package com.sensorberg.sdk.settings;

public class DefaultSettings {

    public static final boolean DEFAULT_SHOULD_RESTORE_BEACON_STATE = true;

    public static final long DEFAULT_LAYOUT_UPDATE_INTERVAL = TimeConstants.ONE_DAY;

    public static final long DEFAULT_HISTORY_UPLOAD_INTERVAL = 30 * TimeConstants.ONE_MINUTE;

    public static final long DEFAULT_SETTINGS_UPDATE_INTERVAL = TimeConstants.ONE_DAY;

    public static final long DEFAULT_EXIT_TIMEOUT_MILLIS = 9 * TimeConstants.ONE_SECOND;

    public static final long DEFAULT_FOREGROUND_SCAN_TIME = 10 * TimeConstants.ONE_SECOND;

    public static final long DEFAULT_FOREGROUND_WAIT_TIME = DEFAULT_FOREGROUND_SCAN_TIME;

    public static final long DEFAULT_BACKGROUND_WAIT_TIME = 2 * TimeConstants.ONE_MINUTE;

    public static final long DEFAULT_BACKGROUND_SCAN_TIME = 20 * TimeConstants.ONE_SECOND;

    public static final long DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT = TimeConstants.ONE_MINUTE;

    public static final long DEFAULT_MESSAGE_DELAY_WINDOW_LENGTH = TimeConstants.ONE_SECOND * 10;

    public static final long DEFAULT_MILLIS_BEETWEEN_RETRIES = 5 * TimeConstants.ONE_SECOND;

    public static final int DEFAULT_MAX_RETRIES = 3;
}
