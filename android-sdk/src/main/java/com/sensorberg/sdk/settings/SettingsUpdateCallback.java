package com.sensorberg.sdk.settings;

public interface SettingsUpdateCallback {

    SettingsUpdateCallback NONE = new SettingsUpdateCallback() {
        @Override
        public void onSettingsUpdateIntervalChange(Long updateIntervalMillies) {

        }

        @Override
        public void onSettingsBeaconLayoutUpdateIntervalChange(long newLayoutUpdateInterval) {

        }

        @Override
        public void onHistoryUploadIntervalChange(long newHistoryUploadInterval) {

        }
    };

    void onSettingsUpdateIntervalChange(Long updateIntervalMillies);

    void onSettingsBeaconLayoutUpdateIntervalChange(long newLayoutUpdateInterval);

    void onHistoryUploadIntervalChange(long newHistoryUploadInterval);
}
