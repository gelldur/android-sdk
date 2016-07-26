package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.internal.interfaces.BeaconHistoryUploadIntervalListener;
import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportHistoryCallback;
import com.sensorberg.sdk.internal.transport.interfaces.TransportSettingsCallback;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;

import java.util.List;

public class DumbSucessTransport implements Transport {

    @Override
    public void updateBeaconLayout() {

    }

    @Override
    public void setBeaconHistoryUploadIntervalListener(BeaconHistoryUploadIntervalListener listener) {

    }

    @Override
    public void setLoggingEnabled(boolean enabled) {

    }

    @Override
    public void setBaseUrl(String baseUrl) {

    }

    @Override
    public String getBaseUrl() {
        return null;
    }

    @Override
    public void setBeaconReportHandler(BeaconReportHandler beaconReportHandler) {

    }

    @Override
    public void setProximityUUIDUpdateHandler(ProximityUUIDUpdateHandler proximityUUIDUpdateHandler) {

    }

    @Override
    public void getBeacon(ResolutionConfiguration resolutionConfiguration, BeaconResponseHandler beaconResponseHandler) {
        beaconResponseHandler.onFailure(new IllegalArgumentException("this transport is dumb"));
    }

    @Override
    public void setApiToken(String apiToken) {

    }

    @Override
    public void loadSettings(TransportSettingsCallback transportSettingsCallback) {

    }

    @Override
    public void publishHistory(List<BeaconScan> scans, List<BeaconAction> actions, TransportHistoryCallback callback) {
        callback.onSuccess(scans,actions);
    }
}
