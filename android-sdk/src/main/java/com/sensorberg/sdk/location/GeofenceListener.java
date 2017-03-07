package com.sensorberg.sdk.location;

import android.location.Location;

public interface GeofenceListener {
    void onGeofenceEvent(GeofenceData geofenceData, boolean entry, String pairingId);
    void onLocationChanged(Location location);
}
