package com.sensorberg;

import com.sensorberg.sdk.resolver.BeaconEvent;

/**
 * {@code SensorbergSdkEventListener} enables an Activity to receive Sensorberg SDK events that could be presented with a UI.
 *
 * @since 2.0
 */
public interface SensorbergSdkEventListener {

    /**
     * Called when the SDK encounters a beacon with a BeaconEvent that should be presented to the user via UI.
     *
     * @param beaconEvent {@code BeaconEvent} BeaconEvent that can be presented to the user via UI.
     */
    void presentBeaconEvent(BeaconEvent beaconEvent);

}
