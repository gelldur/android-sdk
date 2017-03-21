package com.sensorberg.sdk.scanner;

import java.io.Serializable;

import lombok.Getter;

public class EventEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    private final long lastBeaconTime;

    @Getter
    private final int eventMask;

    @Getter
    private final String pairingId;

    EventEntry(EventEntry other) {
        this.lastBeaconTime = other.lastBeaconTime;
        this.eventMask = other.eventMask;
        this.pairingId = other.pairingId;
        //we do not copy the restoredTimestamp since it is irrelevant...
    }

    EventEntry(long lastBeaconTime, int eventMask, String pairingId) {
        this.lastBeaconTime = lastBeaconTime;
        this.eventMask = eventMask;
        this.pairingId = pairingId;
        //we do not copy the restoredTimestamp since it is irrelevant...
    }
}
