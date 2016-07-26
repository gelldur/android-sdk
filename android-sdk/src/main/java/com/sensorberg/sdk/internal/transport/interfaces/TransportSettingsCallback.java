package com.sensorberg.sdk.internal.transport.interfaces;

import com.sensorberg.sdk.internal.transport.model.SettingsResponse;

public interface TransportSettingsCallback {
    TransportSettingsCallback NONE = new TransportSettingsCallback() {
        @Override
        public void nothingChanged() {

        }

        @Override
        public void onFailure(Exception e) {

        }

        @Override
        public void onSettingsFound(SettingsResponse settings) {

        }
    };

    void nothingChanged();

    void onFailure(Exception e);

    void onSettingsFound(SettingsResponse settings);
}
