package com.sensorberg.sdk.settings;


import android.content.SharedPreferences;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Settings {

    @Getter
    @Expose
    @SerializedName("network.beaconLayoutUpdateInterval")
    private long layoutUpdateInterval = DefaultSettings.DEFAULT_LAYOUT_UPDATE_INTERVAL;

    @Getter
    @Expose
    @SerializedName("presenter.messageDelayWindowLength")
    private long messageDelayWindowLength = DefaultSettings.DEFAULT_MESSAGE_DELAY_WINDOW_LENGTH;

    @Getter
    @Expose
    @SerializedName("scanner.exitTimeoutMillis")
    private long exitTimeoutMillis = DefaultSettings.DEFAULT_EXIT_TIMEOUT_MILLIS;

    @Getter
    @Expose
    @SerializedName("scanner.exitForegroundGraceMillis")
    private long exitForegroundGraceMillis = DefaultSettings.DEFAULT_EXIT_FOREGROUND_GRACE_MILLIS;

    @Getter
    @Expose
    @SerializedName("scanner.exitBackgroundGraceMillis")
    private long exitBackgroundGraceMillis = DefaultSettings.DEFAULT_EXIT_BACKGROUND_GRACE_MILLIS;

    @Getter
    @Expose
    @SerializedName("scanner.foreGroundScanTime")
    private long foreGroundScanTime = DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME;

    @Getter
    @Expose
    @SerializedName("scanner.foreGroundWaitTime")
    private long foreGroundWaitTime = DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME;

    @Getter
    @Expose
    @SerializedName("scanner.backgroundScanTime")
    private long backgroundScanTime = DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME;

    @Getter
    @Expose
    @SerializedName("scanner.backgroundWaitTime")
    private long backgroundWaitTime = DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME;

    @Getter
    @Expose
    @SerializedName("location.geohashMaxAge")
    private long geohashMaxAge = DefaultSettings.DEFAULT_GEOHASH_MAX_AGE;

    @Getter
    @Expose
    @SerializedName("location.geohashMinAccuracyRadius")
    private int geohashMinAccuracyRadius = DefaultSettings.DEFAULT_GEOHASH_MIN_ACCURACY_RADIUS;

    @Getter
    @Expose
    @SerializedName("geofence.minUpdateTime")
    private long geofenceMinUpdateTime = DefaultSettings.GEOFENCE_MIN_UPDATE_TIME;

    @Getter
    @Expose
    @SerializedName("geofence.minUpdateDistance")
    private int geofenceMinUpdateDistance = DefaultSettings.GEOFENCE_MIN_UPDATE_DISTANCE;

    @Getter
    @Expose
    @SerializedName("geofence.maxDeviceSpeed")
    private int geofenceMaxDeviceSpeed = DefaultSettings.GEOFENCE_MAX_DEVICE_SPEED;

    @Getter
    @Expose
    @SerializedName("geofence.notificationResponsiveness")
    private int geofenceNotificationResponsiveness = DefaultSettings.GEOFENCE_NOTIFICATION_RESPONSIVENESS;

    @Getter
    @Expose
    @SerializedName("network.millisBetweenRetries")
    private long millisBetweenRetries = DefaultSettings.DEFAULT_MILLIS_BEETWEEN_RETRIES;

    @Getter
    @Expose
    @SerializedName("network.maximumResolveRetries")
    private int maxRetries = DefaultSettings.DEFAULT_MAX_RETRIES; //TODO is this used anywhere?

    @Getter
    @Expose
    @SerializedName("network.historyUploadInterval")
    @Setter
    private long historyUploadInterval = DefaultSettings.DEFAULT_HISTORY_UPLOAD_INTERVAL;

    @Getter
    @Expose
    @SerializedName("scanner.cleanBeaconMapRestartTimeout")
    private long cleanBeaconMapRestartTimeout = DefaultSettings.DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT;

    @Getter
    @Expose
    @SerializedName("settings.updateTime")
    private long settingsUpdateInterval = DefaultSettings.DEFAULT_SETTINGS_UPDATE_INTERVAL;

    @Getter
    @Expose
    @SerializedName("scanner.restoreBeaconStates")
    private boolean shouldRestoreBeaconStates = DefaultSettings.DEFAULT_SHOULD_RESTORE_BEACON_STATE;

    @Getter
    @Expose
    @SerializedName("scanner.minimumAcceptableRssi")
    private int scannerMinRssi = DefaultSettings.DEFAULT_SCANNER_MIN_RSSI;

    @Getter
    @Expose
    @SerializedName("scanner.maximumAcceptableDistanceMeters")
    private int scannerMaxDistance = DefaultSettings.DEFAULT_SCANNER_MAX_DISTANCE;

    /**
     * Beacon report level.
     * REPORT_ALL = 0;
     * REPORT_ONLY_CONTAINED = 1;
     * REPORT_NONE = 2;
     */
    @Getter
    @Expose
    @SerializedName("network.beaconReportLevel")
    private int beaconReportLevel = DefaultSettings.DEFAULT_BEACON_REPORT_LEVEL;

    public static final int BEACON_REPORT_LEVEL_ALL = 0;
    public static final int BEACON_REPORT_LEVEL_ONLY_CONTAINED = 1;
    public static final int BEACON_REPORT_LEVEL_NONE = 2;

    @Getter
    private Long revision = null;

    public Settings() {
    }

    public Settings(SharedPreferences preferences) {
        if (preferences != null) {
            exitTimeoutMillis = preferences
                    .getLong(SharedPreferencesKeys.Scanner.TIMEOUT_MILLIES, DefaultSettings.DEFAULT_EXIT_TIMEOUT_MILLIS);
            exitForegroundGraceMillis = preferences
                    .getLong(SharedPreferencesKeys.Scanner.TIMEOUT_GRACE_FOREGROUND_MILLIES, DefaultSettings.DEFAULT_EXIT_FOREGROUND_GRACE_MILLIS);
            exitBackgroundGraceMillis = preferences
                    .getLong(SharedPreferencesKeys.Scanner.TIMEOUT_GRACE_BACKGROUND_MILLIES, DefaultSettings.DEFAULT_EXIT_BACKGROUND_GRACE_MILLIS);
            foreGroundScanTime = preferences
                    .getLong(SharedPreferencesKeys.Scanner.FORE_GROUND_SCAN_TIME, DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME);
            foreGroundWaitTime = preferences
                    .getLong(SharedPreferencesKeys.Scanner.FORE_GROUND_WAIT_TIME, DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME);
            backgroundScanTime = preferences
                    .getLong(SharedPreferencesKeys.Scanner.BACKGROUND_SCAN_TIME, DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME);
            backgroundWaitTime = preferences
                    .getLong(SharedPreferencesKeys.Scanner.BACKGROUND_WAIT_TIME, DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);
            geohashMaxAge = preferences
                    .getLong(SharedPreferencesKeys.Location.GEOHASH_MAX_AGE, DefaultSettings.DEFAULT_GEOHASH_MAX_AGE);
            geohashMinAccuracyRadius = preferences
                    .getInt(SharedPreferencesKeys.Location.GEOHASH_MIN_ACCURACY_RADIUS, DefaultSettings.DEFAULT_GEOHASH_MIN_ACCURACY_RADIUS);
            geofenceMinUpdateTime = preferences
                    .getLong(SharedPreferencesKeys.Location.GEOFENCE_MIN_UPDATE_TIME, DefaultSettings.GEOFENCE_MIN_UPDATE_TIME);
            geofenceMinUpdateDistance = preferences
                    .getInt(SharedPreferencesKeys.Location.GEOFENCE_MIN_UPDATE_DISTANCE, DefaultSettings.GEOFENCE_MIN_UPDATE_DISTANCE);
            geofenceMaxDeviceSpeed = preferences
                    .getInt(SharedPreferencesKeys.Location.GEOFENCE_MAX_DEVICE_SPEED, DefaultSettings.GEOFENCE_MAX_DEVICE_SPEED);
            geofenceNotificationResponsiveness = preferences
                    .getInt(SharedPreferencesKeys.Location.GEOFENCE_NOTIFICATION_RESPONSIVENESS, DefaultSettings.GEOFENCE_NOTIFICATION_RESPONSIVENESS);
            cleanBeaconMapRestartTimeout = preferences.getLong(SharedPreferencesKeys.Scanner.CLEAN_BEACON_MAP_RESTART_TIMEOUT,
                    DefaultSettings.DEFAULT_CLEAN_BEACONMAP_ON_RESTART_TIMEOUT);
            revision = preferences.getLong(SharedPreferencesKeys.Settings.REVISION, Long.MIN_VALUE);

            settingsUpdateInterval = preferences
                    .getLong(SharedPreferencesKeys.Settings.UPDATE_INTERVAL, DefaultSettings.DEFAULT_SETTINGS_UPDATE_INTERVAL);

            maxRetries = preferences.getInt(SharedPreferencesKeys.Network.MAX_RESOLVE_RETRIES, DefaultSettings.DEFAULT_MAX_RETRIES);
            millisBetweenRetries = preferences
                    .getLong(SharedPreferencesKeys.Network.TIME_BETWEEN_RESOLVE_RETRIES, DefaultSettings.DEFAULT_MILLIS_BEETWEEN_RETRIES);

            historyUploadInterval = preferences
                    .getLong(SharedPreferencesKeys.Network.HISTORY_UPLOAD_INTERVAL, DefaultSettings.DEFAULT_HISTORY_UPLOAD_INTERVAL);
            layoutUpdateInterval = preferences
                    .getLong(SharedPreferencesKeys.Network.BEACON_LAYOUT_UPDATE_INTERVAL, DefaultSettings.DEFAULT_HISTORY_UPLOAD_INTERVAL);
            shouldRestoreBeaconStates = preferences.getBoolean(SharedPreferencesKeys.Scanner.SHOULD_RESTORE_BEACON_STATES,
                    DefaultSettings.DEFAULT_SHOULD_RESTORE_BEACON_STATE);
            beaconReportLevel = preferences.getInt(SharedPreferencesKeys.Network.BEACON_REPORT_LEVEL,
                    DefaultSettings.DEFAULT_BEACON_REPORT_LEVEL);
            scannerMinRssi = preferences.getInt(SharedPreferencesKeys.Scanner.MIN_RSSI,
                    DefaultSettings.DEFAULT_SCANNER_MIN_RSSI);
            scannerMaxDistance = preferences.getInt(SharedPreferencesKeys.Scanner.MAX_DISTANCE,
                    DefaultSettings.DEFAULT_SCANNER_MAX_DISTANCE);
        }
    }

    public Settings(long rev, Settings newSettings, SettingsUpdateCallback settingsUpdateCallback) {
        exitTimeoutMillis = newSettings.getExitTimeoutMillis();
        exitForegroundGraceMillis = newSettings.getExitForegroundGraceMillis();
        exitBackgroundGraceMillis = newSettings.getExitBackgroundGraceMillis();
        foreGroundScanTime = newSettings.getForeGroundScanTime();
        foreGroundWaitTime = newSettings.getForeGroundWaitTime();
        backgroundScanTime = newSettings.getBackgroundScanTime();
        backgroundWaitTime = newSettings.getBackgroundWaitTime();
        geohashMaxAge = newSettings.getGeohashMaxAge();
        geohashMinAccuracyRadius = newSettings.getGeohashMinAccuracyRadius();
        geofenceMinUpdateTime = newSettings.getGeofenceMinUpdateTime();
        geofenceMinUpdateDistance = newSettings.getGeofenceMinUpdateDistance();
        geofenceMaxDeviceSpeed = newSettings.getGeofenceMaxDeviceSpeed();
        geofenceNotificationResponsiveness = newSettings.getGeofenceNotificationResponsiveness();

        cleanBeaconMapRestartTimeout = newSettings.getCleanBeaconMapRestartTimeout();

        messageDelayWindowLength = newSettings.getMessageDelayWindowLength();
        maxRetries = newSettings.getMaxRetries();
        millisBetweenRetries = newSettings.getMillisBetweenRetries();
        shouldRestoreBeaconStates = newSettings.isShouldRestoreBeaconStates();
        beaconReportLevel = newSettings.getBeaconReportLevel();
        scannerMinRssi = newSettings.getScannerMinRssi();
        scannerMaxDistance = newSettings.getScannerMaxDistance();

        if (rev >= 0) {
            revision = rev;
        } else {
            revision = null;
        }

        if (newSettings.getHistoryUploadInterval() != historyUploadInterval) {
            historyUploadInterval = newSettings.getHistoryUploadInterval();
            settingsUpdateCallback.onHistoryUploadIntervalChange(historyUploadInterval);
        }

        if (newSettings.getLayoutUpdateInterval() != layoutUpdateInterval) {
            layoutUpdateInterval = newSettings.getLayoutUpdateInterval();
            settingsUpdateCallback.onSettingsBeaconLayoutUpdateIntervalChange(layoutUpdateInterval);
        }

        if (newSettings.getSettingsUpdateInterval() != settingsUpdateInterval) {
            settingsUpdateInterval = newSettings.getSettingsUpdateInterval();
            settingsUpdateCallback.onSettingsUpdateIntervalChange(settingsUpdateInterval);
        }
    }

    public void persistToPreferences(SharedPreferences preferences) {
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();

            if (revision != null) {
                editor.putLong(SharedPreferencesKeys.Settings.REVISION, revision);
            } else {
                editor.remove(SharedPreferencesKeys.Settings.REVISION);
            }

            editor.putLong(SharedPreferencesKeys.Scanner.TIMEOUT_MILLIES, exitTimeoutMillis);
            editor.putLong(SharedPreferencesKeys.Scanner.TIMEOUT_GRACE_FOREGROUND_MILLIES, exitForegroundGraceMillis);
            editor.putLong(SharedPreferencesKeys.Scanner.TIMEOUT_GRACE_BACKGROUND_MILLIES, exitBackgroundGraceMillis);
            editor.putLong(SharedPreferencesKeys.Scanner.FORE_GROUND_SCAN_TIME, foreGroundScanTime);
            editor.putLong(SharedPreferencesKeys.Scanner.FORE_GROUND_WAIT_TIME, foreGroundWaitTime);
            editor.putLong(SharedPreferencesKeys.Scanner.BACKGROUND_SCAN_TIME, backgroundScanTime);
            editor.putLong(SharedPreferencesKeys.Scanner.BACKGROUND_WAIT_TIME, backgroundWaitTime);
            editor.putLong(SharedPreferencesKeys.Location.GEOHASH_MAX_AGE, geohashMaxAge);
            editor.putInt(SharedPreferencesKeys.Location.GEOHASH_MIN_ACCURACY_RADIUS, geohashMinAccuracyRadius);
            editor.putLong(SharedPreferencesKeys.Location.GEOFENCE_MIN_UPDATE_TIME, geofenceMinUpdateTime);
            editor.putInt(SharedPreferencesKeys.Location.GEOFENCE_MIN_UPDATE_DISTANCE, geofenceMinUpdateDistance);
            editor.putInt(SharedPreferencesKeys.Location.GEOFENCE_MAX_DEVICE_SPEED, geofenceMaxDeviceSpeed);
            editor.putInt(SharedPreferencesKeys.Location.GEOFENCE_NOTIFICATION_RESPONSIVENESS, geofenceNotificationResponsiveness);
            editor.putBoolean(SharedPreferencesKeys.Scanner.SHOULD_RESTORE_BEACON_STATES, shouldRestoreBeaconStates);

            editor.putLong(SharedPreferencesKeys.Settings.MESSAGE_DELAY_WINDOW_LENGTH, messageDelayWindowLength);
            editor.putLong(SharedPreferencesKeys.Settings.UPDATE_INTERVAL, settingsUpdateInterval);

            editor.putInt(SharedPreferencesKeys.Network.MAX_RESOLVE_RETRIES, maxRetries);
            editor.putLong(SharedPreferencesKeys.Network.TIME_BETWEEN_RESOLVE_RETRIES, millisBetweenRetries);
            editor.putLong(SharedPreferencesKeys.Network.HISTORY_UPLOAD_INTERVAL, historyUploadInterval);
            editor.putLong(SharedPreferencesKeys.Network.BEACON_LAYOUT_UPDATE_INTERVAL, layoutUpdateInterval);
            editor.putInt(SharedPreferencesKeys.Network.BEACON_REPORT_LEVEL, beaconReportLevel);
            editor.putInt(SharedPreferencesKeys.Scanner.MIN_RSSI, scannerMinRssi);
            editor.putInt(SharedPreferencesKeys.Scanner.MAX_DISTANCE, scannerMaxDistance);

            editor.apply();
        }
    }

    public static class Builder {

        private Settings settings;

        public Builder(Settings inputSettings) {
            settings = inputSettings;
        }

        public Settings build() {
            return settings;
        }

        public Builder withExitTimeoutMillis(long timeoutMillis) {
            settings.exitTimeoutMillis = timeoutMillis;
            return this;
        }

        public Builder withForegroundScanTime(long scanTime) {
            settings.foreGroundScanTime = scanTime;
            return this;
        }

        public Builder withForegroundWaitTime(long scanTime) {
            settings.foreGroundWaitTime = scanTime;
            return this;
        }

        public Builder withBackgroundScanTime(long scanTime) {
            settings.backgroundScanTime = scanTime;
            return this;
        }

        public Builder withBackgroundWaitTime(long scanTime) {
            settings.backgroundWaitTime = scanTime;
            return this;
        }

    }
}
