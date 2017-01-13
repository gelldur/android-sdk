package com.sensorberg.sdk.location;

import android.location.Location;

public interface GeofenceListener {
    void onGeofenceEvent(GeofenceData geofenceData, boolean entry);
    void onLocationChanged(Location location);
}
