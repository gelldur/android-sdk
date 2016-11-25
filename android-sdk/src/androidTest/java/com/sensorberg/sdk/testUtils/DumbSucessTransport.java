package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.internal.interfaces.BeaconHistoryUploadIntervalListener;
import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportHistoryCallback;
import com.sensorberg.sdk.internal.transport.interfaces.TransportSettingsCallback;
import com.sensorberg.sdk.model.persistence.ActionConversion;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.scanner.ScanEvent;

import java.util.List;
import java.util.SortedMap;

public class DumbSucessTransport implements Transport {

    @Override
    public void updateBeaconLayout(SortedMap<String, String> attributes) {

    }

    @Override
    public void setBeaconHistoryUploadIntervalListener(BeaconHistoryUploadIntervalListener listener) {

    }

    @Override
    public void setLoggingEnabled(boolean enabled) {

    }

    @Override
    public void setProximityUUIDUpdateHandler(ProximityUUIDUpdateHandler proximityUUIDUpdateHandler) {

    }

    @Override
    public void getBeacon(ScanEvent scanEvent, SortedMap<String, String> attributes, BeaconResponseHandler beaconResponseHandler) {
        beaconResponseHandler.onFailure(new IllegalArgumentException("this transport is dumb"));
    }

    @Override
    public boolean setApiToken(String apiToken) {
        return false;
    }

    @Override
    public void loadSettings(TransportSettingsCallback transportSettingsCallback) {

    }

    @Override
    public void publishHistory(List<BeaconScan> scans, List<BeaconAction> actions, List<ActionConversion> conversions, TransportHistoryCallback callback) {
        callback.onSuccess(scans, actions, conversions);
    }
}
