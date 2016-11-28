package com.sensorberg.sdk.internal.transport.interfaces;

import com.sensorberg.sdk.internal.interfaces.BeaconHistoryUploadIntervalListener;
import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.model.persistence.ActionConversion;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.scanner.ScanEvent;

import java.util.List;
import java.util.SortedMap;

public interface Transport {

    String HEADER_INSTALLATION_IDENTIFIER = "X-iid";

    String HEADER_ADVERTISER_IDENTIFIER = "X-aid";

    String HEADER_USER_AGENT = "User-Agent";

    String HEADER_XAPIKEY = "X-Api-Key";

    interface ProximityUUIDUpdateHandler{
        ProximityUUIDUpdateHandler NONE = new ProximityUUIDUpdateHandler() {
            @Override
            public void proximityUUIDListUpdated(List<String> proximityUUIDs) {

            }
        };

        void proximityUUIDListUpdated(List<String> proximityUUIDs);
    }

    void setProximityUUIDUpdateHandler(ProximityUUIDUpdateHandler proximityUUIDUpdateHandler);

    void getBeacon(ScanEvent scanEvent, SortedMap<String, String> attributes, BeaconResponseHandler beaconResponseHandler);

    boolean setApiToken(String apiToken);

    void loadSettings(TransportSettingsCallback transportSettingsCallback);

    void publishHistory(List<BeaconScan> scans, List<BeaconAction> actions, List<ActionConversion> conversions, TransportHistoryCallback callback);

    void updateBeaconLayout(SortedMap<String, String> attributes);

    void setBeaconHistoryUploadIntervalListener(BeaconHistoryUploadIntervalListener listener);

    void setLoggingEnabled(boolean enabled);
}
