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

    private SharedPreferences preferences;
    private Gson gson;

    private HashMap<String, Geofence> storage = new HashMap<>();

    private boolean hasNew = true;
    private boolean committed = false;

    public GeofenceStorage(SharedPreferences preferences, Gson gson) {
        this.preferences = preferences;
        this.gson = gson;
        restore();
    }

    public void updateGeofences(List<String> geofences) {
        HashMap<String, Geofence> temp = new HashMap<>(geofences.size());
        hasNew = false;
        if (storage.size() != geofences.size()) {
            setHasNew();
        }
        for (String incoming : geofences) {
            Geofence existing = storage.get(incoming);
            if (existing == null) {
                temp.put(incoming, buildGeofence(incoming));
                setHasNew();
            } else {
                temp.put(incoming, existing);
            }
        }
        if (hasNew) {
            storage = temp;
            store();
        }
    }

    public List<Geofence> getGeofences() {
        return new ArrayList<>(storage.values());
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    public boolean hasUncommitted() {
        return !committed || hasNew;
    }

    private void setHasNew() {
        hasNew = true;
        committed = false;
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

    private void store() {
        String map = gson.toJson(storage.keySet());
        preferences.edit().putString(Constants.SharedPreferencesKeys.Data.GEOFENCES, map).apply();
    }

    private void restore() {
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
}
