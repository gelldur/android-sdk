package com.sensorberg.sdk.settings;

import static com.sensorberg.sdk.settings.Settings.BEACON_REPORT_LEVEL_ALL;

public class DefaultSettings {

    public static final boolean DEFAULT_SHOULD_RESTORE_BEACON_STATE = true;

    public static final long DEFAULT_LAYOUT_UPDATE_INTERVAL = TimeConstants.ONE_DAY;

    public static final long DEFAULT_HISTORY_UPLOAD_INTERVAL = 30 * TimeConstants.ONE_MINUTE;

    public static final long DEFAULT_SETTINGS_UPDATE_INTERVAL = TimeConstants.ONE_DAY;

    public static final long DEFAULT_EXIT_TIMEOUT_MILLIS = 9 * TimeConstants.ONE_SECOND;

    public static final long DEFAULT_FOREGROUND_SCAN_TIME = 10 * TimeConstants.ONE_SECOND;

    public static final long DEFAULT_FOREGROUND_WAIT_TIME = DEFAULT_FOREGROUND_SCAN_TIME;

    public static final long DEFAULT_BACKGROUND_WAIT_TIME = 2 * TimeConstants.ONE_MINUTE;

    public static final long DEFAULT_GEOHASH_MAX_AGE = TimeConstants.ONE_MINUTE;

    public static final int DEFAULT_GEOHASH_MIN_ACCURACY_RADIUS = 25;   //meters

    public static final long GEOFENCE_MIN_UPDATE_TIME = 5 * TimeConstants.ONE_MINUTE;

    public static final int GEOFENCE_MIN_UPDATE_DISTANCE = 500; //m

    public static final int GEOFENCE_MAX_DEVICE_SPEED = 50; //km/h

    public static final int GEOFENCE_NOTIFICATION_RESPONSIVENESS = (int) (5 * TimeConstants.ONE_SECOND);

    public static final long DEFAULT_BACKGROUND_SCAN_TIME = 20 * TimeConstants.ONE_SECOND;

    public static final long DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT = TimeConstants.ONE_MINUTE;

    public static final long DEFAULT_MESSAGE_DELAY_WINDOW_LENGTH = TimeConstants.ONE_SECOND * 10;

    public static final long DEFAULT_MILLIS_BEETWEEN_RETRIES = 5 * TimeConstants.ONE_SECOND;

    public static final int DEFAULT_MAX_RETRIES = 3;

    public static final int DEFAULT_BEACON_REPORT_LEVEL = BEACON_REPORT_LEVEL_ALL;

    public static final int DEFAULT_SCANNER_MIN_RSSI = Integer.MIN_VALUE;

    public static final int DEFAULT_SCANNER_MAX_DISTANCE = Integer.MAX_VALUE;

    public static final int DEFAULT_INITIAL_GEOFENCES_SEARCH_RADIUS = 100 * 1000; //meters, 100 km
}
