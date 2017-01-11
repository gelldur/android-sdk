package com.sensorberg.sdk.location;

public interface GeofenceListener {
    void onGeofenceEvent(GeofenceData geofenceData, boolean entry);
}
