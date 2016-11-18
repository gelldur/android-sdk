package com.sensorberg.sdk.internal.transport.interfaces;

import com.sensorberg.sdk.model.persistence.ActionConversion;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.resolver.BeaconEvent;

import java.util.List;

public interface TransportHistoryCallback {
    void onFailure(Exception throwable);

    void onInstantActions(List<BeaconEvent> instantActions);

    void onSuccess(List<BeaconScan> scans, List<BeaconAction> actions, List<ActionConversion> conversions);
}
