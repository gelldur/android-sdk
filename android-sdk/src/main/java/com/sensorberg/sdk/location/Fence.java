package com.sensorberg.sdk.location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.sensorberg.sdk.Logger;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.geohash.GeoHash;
import lombok.Getter;

public class Fence {

    @Getter private final int radius;
    @Getter private final GeoHash hash;
    @Getter private final String id;

    public Fence(String fence) {
        id = fence;
        String problem = check(fence);
        if (problem != null) {
            throw new IllegalArgumentException(problem);
        }
        radius = Integer.valueOf(fence.substring(8, 14));
        String geohash = fence.substring(0, 8);
        try {
            hash = GeoHash.fromGeohashString(geohash);
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException("Invalid geofence geohash: "+geohash);
        }
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

    public static List<Fence> from(GeofencingEvent event) {
        String problem = check(event);
        if (problem != null) {
            throw new IllegalArgumentException(problem);
        }
        ArrayList<Fence> result = new ArrayList<>();
        for (Geofence geofence : event.getTriggeringGeofences()) {
            try {
                result.add(new Fence(geofence.getRequestId()));
            } catch (IllegalArgumentException ex) {
                Logger.log.logError(ex.getMessage());
            }
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("GeofencingEvent has no triggering geofences");
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
