package com.sensorberg.sdk.location;

import android.content.SharedPreferences;

import com.google.android.gms.location.Geofence;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lombok.Getter;

public class GeofenceStorage {

    private SharedPreferences preferences;
    private Gson gson;

    private HashMap<String, Geofence> storage = new HashMap<>();

    @Getter private boolean changed = true;

    public GeofenceStorage(SharedPreferences preferences, Gson gson) {
        this.preferences = preferences;
        this.gson = gson;
        restore();
    }

    /**
     * Updates stored geofences, only if something changed.
     * @param fences List of fence strings (8 char geohash plus 6 char radius).
     */
    public void updateFences(List<String> fences) {
        HashMap<String, Geofence> temp = new HashMap<>(fences.size());
        changed = false;
        for (String incoming : fences) {
            Geofence existing = storage.get(incoming);
            if (existing == null) {
                Geofence geofence = buildGeofence(incoming);
                if (geofence != null) {
                    temp.put(incoming, geofence);
                    changed = true;
                }
            } else {
                temp.put(incoming, existing);
            }
        }
        if (storage.size() != temp.size()) {
            changed = true;
        }
        if (changed) {
            storage = temp;
            store();
        }
    }

    public List<Geofence> getGeofences() {
        return new ArrayList<>(storage.values());
    }

    public List<String> getGeofencesKeys() { return new ArrayList<>(storage.keySet()); }

    private Geofence buildGeofence(String fence) {
        try {
            Fence temp = new Fence(fence);
            return new Geofence.Builder()
                    .setRequestId(temp.getId())
                    .setCircularRegion(
                            temp.getHash().getPoint().getLatitude(),
                            temp.getHash().getPoint().getLongitude(),
                            temp.getRadius())
                    .setExpirationDuration(Long.MAX_VALUE)
                    .setNotificationResponsiveness(5000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
        } catch (IllegalArgumentException ex) {
            Logger.log.logError("Invalid geofence: "+fence, ex);
            return null;
        }
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
                Geofence geofence = buildGeofence(fence);
                if (geofence != null) {
                    storage.put(fence, geofence);
                }
            }
        }
    }
}
