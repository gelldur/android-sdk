package com.sensorberg.sdk.settings;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import android.content.SharedPreferences;

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
    @SerializedName("scanner.foreGroundScanTime")
    private long foreGroundScanTime = DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME;

    @Getter
    @Expose
    @SerializedName("scanner.foreGroundWaitTime")
    private long foreGroundWaitTime = DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME;

    @Getter
    @Expose
    @SerializedName("scanner.foreGroundScanSplit")
    private int foreGroundScanSplit = DefaultSettings.DEFAULT_FOREGROUND_SCAN_SPLIT;

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
    @SerializedName("scanner.backgroundScanSplit")
    private int backgroundScanSplit = DefaultSettings.DEFAULT_BACKGROUND_SCAN_SPLIT;

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
    private Long revision = null;

    public Settings() {
    }

    public Settings(SharedPreferences preferences) {
        if (preferences != null) {
            exitTimeoutMillis = preferences
                    .getLong(SharedPreferencesKeys.Scanner.TIMEOUT_MILLIES, DefaultSettings.DEFAULT_EXIT_TIMEOUT_MILLIS);
            foreGroundScanTime = preferences
                    .getLong(SharedPreferencesKeys.Scanner.FORE_GROUND_SCAN_TIME, DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME);
            foreGroundWaitTime = preferences
                    .getLong(SharedPreferencesKeys.Scanner.FORE_GROUND_WAIT_TIME, DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME);
            foreGroundScanSplit = preferences
                    .getInt(SharedPreferencesKeys.Scanner.FORE_GROUND_SCAN_SPLIT, DefaultSettings.DEFAULT_FOREGROUND_SCAN_SPLIT);
            backgroundScanTime = preferences
                    .getLong(SharedPreferencesKeys.Scanner.BACKGROUND_SCAN_TIME, DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME);
            backgroundWaitTime = preferences
                    .getLong(SharedPreferencesKeys.Scanner.BACKGROUND_WAIT_TIME, DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);
            backgroundScanSplit = preferences
                    .getInt(SharedPreferencesKeys.Scanner.BACKGROUND_SCAN_SPLIT, DefaultSettings.DEFAULT_BACKGROUND_SCAN_SPLIT);
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
        }
    }

    public Settings(long rev, Settings newSettings, SettingsUpdateCallback settingsUpdateCallback) {
        exitTimeoutMillis = newSettings.getExitTimeoutMillis();
        foreGroundScanTime = newSettings.getForeGroundScanTime();
        foreGroundWaitTime = newSettings.getForeGroundWaitTime();
        foreGroundScanSplit = newSettings.getForeGroundScanSplit();
        backgroundScanTime = newSettings.getBackgroundScanTime();
        backgroundWaitTime = newSettings.getBackgroundWaitTime();
        backgroundScanSplit = newSettings.getBackgroundScanSplit();

        cleanBeaconMapRestartTimeout = newSettings.getCleanBeaconMapRestartTimeout();

        messageDelayWindowLength = newSettings.getMessageDelayWindowLength();
        maxRetries = newSettings.getMaxRetries();
        millisBetweenRetries = newSettings.getMillisBetweenRetries();
        shouldRestoreBeaconStates = newSettings.isShouldRestoreBeaconStates();

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
            editor.putLong(SharedPreferencesKeys.Scanner.FORE_GROUND_SCAN_TIME, foreGroundScanTime);
            editor.putLong(SharedPreferencesKeys.Scanner.FORE_GROUND_WAIT_TIME, foreGroundWaitTime);
            editor.putInt(SharedPreferencesKeys.Scanner.FORE_GROUND_SCAN_SPLIT, foreGroundScanSplit);
            editor.putLong(SharedPreferencesKeys.Scanner.BACKGROUND_SCAN_TIME, backgroundScanTime);
            editor.putLong(SharedPreferencesKeys.Scanner.BACKGROUND_WAIT_TIME, backgroundWaitTime);
            editor.putInt(SharedPreferencesKeys.Scanner.BACKGROUND_SCAN_SPLIT, backgroundScanSplit);
            editor.putBoolean(SharedPreferencesKeys.Scanner.SHOULD_RESTORE_BEACON_STATES, shouldRestoreBeaconStates);

            editor.putLong(SharedPreferencesKeys.Settings.MESSAGE_DELAY_WINDOW_LENGTH, messageDelayWindowLength);
            editor.putLong(SharedPreferencesKeys.Settings.UPDATE_INTERVAL, settingsUpdateInterval);

            editor.putInt(SharedPreferencesKeys.Network.MAX_RESOLVE_RETRIES, maxRetries);
            editor.putLong(SharedPreferencesKeys.Network.TIME_BETWEEN_RESOLVE_RETRIES, millisBetweenRetries);
            editor.putLong(SharedPreferencesKeys.Network.HISTORY_UPLOAD_INTERVAL, historyUploadInterval);
            editor.putLong(SharedPreferencesKeys.Network.BEACON_LAYOUT_UPDATE_INTERVAL, layoutUpdateInterval);

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

        public Builder withForegroundScanSplit(int scanSplit) {
            settings.foreGroundScanSplit = scanSplit;
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

        public Builder withBackgroundScanSplit(int scanSplit) {
            settings.backgroundScanSplit = scanSplit;
            return this;
        }

    }
}
