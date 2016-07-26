package com.sensorberg.sdk.internal.interfaces;

import com.sensorberg.sdk.resolver.BeaconEvent;

import java.util.List;

public interface BeaconResponseHandler {

    BeaconResponseHandler NONE = new BeaconResponseHandler() {
        @Override
        public void onSuccess(List<BeaconEvent> beaconEvent) {

        }

        @Override
        public void onFailure(Throwable cause) {

        }
    };

    void onSuccess(List<BeaconEvent> beaconEvent);
    void onFailure(Throwable cause);
}
