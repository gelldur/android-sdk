package com.sensorberg.sdk.internal.transport.model;

import com.google.gson.annotations.Expose;

import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;

import java.util.Date;
import java.util.List;

//serialized by gson
@SuppressWarnings({"unused", "WeakerAccess"})
public class HistoryBody {

    @Expose
    public final List<BeaconScan> events;

    @Expose
    public final List<BeaconAction> actions;

    @Expose
    public final Date deviceTimestamp;

    public HistoryBody(List<BeaconScan> scans, List<BeaconAction> actions, Clock clock) {
        this.events = scans;
        this.deviceTimestamp = new Date(clock.now());
        this.actions = actions;
    }
}
