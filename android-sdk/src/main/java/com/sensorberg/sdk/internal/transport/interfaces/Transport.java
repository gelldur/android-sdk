package com.sensorberg.sdk.internal.transport.interfaces;

import com.sensorberg.sdk.internal.interfaces.BeaconHistoryUploadIntervalListener;
import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;

import java.util.List;

public interface Transport {

    String HEADER_INSTALLATION_IDENTIFIER = "X-iid";

    String HEADER_ADVERTISER_IDENTIFIER = "X-aid";

    String HEADER_USER_AGENT = "User-Agent";

    String HEADER_AUTHORIZATION = "Authorization";

    String HEADER_XAPIKEY = "X-Api-Key";

    interface ProximityUUIDUpdateHandler{
        ProximityUUIDUpdateHandler NONE = new ProximityUUIDUpdateHandler() {
            @Override
            public void proximityUUIDListUpdated(List<String> proximityUUIDs) {

            }
        };

        void proximityUUIDListUpdated(List<String> proximityUUIDs);
    }

    interface BeaconReportHandler {
        BeaconReportHandler NONE = new BeaconReportHandler() {
            @Override
            public void reportImmediately() {

            }
        };

        void reportImmediately();
    }
    void setBeaconReportHandler(BeaconReportHandler beaconReportHandler);

    void setProximityUUIDUpdateHandler(ProximityUUIDUpdateHandler proximityUUIDUpdateHandler);

    void getBeacon(ResolutionConfiguration resolutionConfiguration, BeaconResponseHandler beaconResponseHandler);

    void setApiToken(String apiToken);

    void loadSettings(TransportSettingsCallback transportSettingsCallback);

    void publishHistory(List<BeaconScan> scans, List<BeaconAction> actions, TransportHistoryCallback callback);

    void updateBeaconLayout();

    void setBeaconHistoryUploadIntervalListener(BeaconHistoryUploadIntervalListener listener);

    void setLoggingEnabled(boolean enabled);
}
