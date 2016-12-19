package com.sensorberg.sdk.location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class GeofenceId {

    @Getter private final String hash;
    @Getter private final int radius;
    @Getter private final String geofenceId;

    public GeofenceId(String geofenceId) {
        String problem = check(geofenceId);
        if (problem != null) {
            throw new IllegalArgumentException(problem);
        }
        hash = geofenceId.substring(0, 8);
        radius = Integer.valueOf(geofenceId.substring(8, 14));
        this.geofenceId = geofenceId;
    }

    public static boolean isValid(String geofenceId) {
        return check(geofenceId) == null;
    }

    private static String check(String geofenceId) {
        if (geofenceId == null) {
            return "Geofence string can't be null";
        }
        if (geofenceId.length() != 14) {
            return "Geofence string has to be exactly 14 chars";
        }
        if (!geofenceId.substring(8, 14).matches("^[0-9]{6}$")) {
            return "Geofence last 6 chars have to be digits";
        }
        return null;
    }

    public static List<GeofenceId> from(GeofencingEvent event) {
        String problem = check(event);
        if (problem != null) {
            throw new IllegalArgumentException(problem);
        }
        ArrayList<GeofenceId> result = new ArrayList<>();
        for (Geofence geofence : event.getTriggeringGeofences()) {
            result.add(new GeofenceId(geofence.getRequestId()));
        }
        return result;
    }

    private static String check(GeofencingEvent event) {
        if (event == null) {
            return "GeofencingEvent is null";
        }
        if (event.hasError()) {
            return "GeofencingEvent has error: "+event.getErrorCode();
        }
        if (event.getTriggeringGeofences() == null) {
            return "GeofencingEvent has no triggering geofences";
        }
        if (event.getTriggeringGeofences().isEmpty()) {
            return "GeofencingEvent has empty list of triggering geofences";
        }
        int transition = event.getGeofenceTransition();
        //Only GEOFENCE_TRANSITION_ENTER and GEOFENCE_TRANSITION_EXIT are supported for now.
        if (transition != Geofence.GEOFENCE_TRANSITION_ENTER && transition != Geofence.GEOFENCE_TRANSITION_EXIT) {
            return "GeofencingEvent has invalid transition: "+transition;
        }
        return null;
    }
}
