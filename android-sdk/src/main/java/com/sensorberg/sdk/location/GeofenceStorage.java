package com.sensorberg.sdk.location;

import android.content.SharedPreferences;

import com.google.android.gms.location.Geofence;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sensorberg.sdk.Constants;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GeofenceStorage {

    private HashMap<String, Geofence> storage = new HashMap<>();

    private SharedPreferences preferences;

    private Gson gson;

    private boolean pending = false;

    public GeofenceStorage(SharedPreferences preferences, Gson gson) {
        this.preferences = preferences;
        this.gson = gson;
        restore();
    }

    public void updateGeofences(List<String> geofences) {
        HashMap<String, Geofence> newGeofences = new HashMap<>(geofences.size());
        if (storage.size() != geofences.size()) {
            pending = true;
        }
        for (String fence : geofences) {
            Geofence existing = storage.get(fence);
            if (existing == null) {
                newGeofences.put(fence, buildGeofence(fence));
                pending = true;
            } else {
                newGeofences.put(fence, existing);
            }
        }
        storage = newGeofences;
        store();
    }

    private void store() {
        String map = gson.toJson(storage.keySet());
        preferences.edit().putString(Constants.SharedPreferencesKeys.Data.GEOFENCES, map).apply();
        preferences.edit().putBoolean(Constants.SharedPreferencesKeys.Data.GEOFENCES_PENDING, pending).apply();
    }

    private void restore() {
        pending = preferences.getBoolean(Constants.SharedPreferencesKeys.Data.GEOFENCES_PENDING, false);
        String mapJson = preferences.getString(Constants.SharedPreferencesKeys.Data.GEOFENCES, "");
        HashSet<String> restored;
        if (!mapJson.isEmpty()) {
            Type mapType = new TypeToken<HashSet<String>>() {}.getType();
            restored = gson.fromJson(mapJson, mapType);
            for (String fence : restored) {
                storage.put(fence, buildGeofence(fence));
            }
        }
    }

    private Geofence buildGeofence(String geofenceId) {
        GeoHash geoHash = GeoHash.decode(geofenceId.substring(0, 8));
        int radius = Integer.valueOf(geofenceId.substring(8, 14));
        return new Geofence.Builder()
                .setRequestId(geofenceId)
                .setCircularRegion(geoHash.lat, geoHash.lon, radius)
                .setExpirationDuration(Long.MAX_VALUE)
                .setNotificationResponsiveness(5000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    public List<Geofence> getGeofences() {
        return new ArrayList<>(storage.values());
    }

    public boolean hasPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
        preferences.edit().putBoolean(Constants.SharedPreferencesKeys.Data.GEOFENCES_PENDING, pending).apply();
    }
}
