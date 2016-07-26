package com.sensorberg.sdk.internal.interfaces;

public interface BeaconHistoryUploadIntervalListener {

    BeaconHistoryUploadIntervalListener NONE = new BeaconHistoryUploadIntervalListener() {
        @Override
        public void historyUploadIntervalChanged(Long newHistoryUploadIntervalMillis) {
            //do nothing
        }
    };

    void historyUploadIntervalChanged(Long newHistoryUploadIntervalMillis);

}
