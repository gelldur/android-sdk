package com.sensorberg.sdk.location;

import android.content.SharedPreferences;
import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

/**
 * This class is workaround for problem in native Android code with restoring Location objects:
 * http://stackoverflow.com/a/40379377
 * http://stackoverflow.com/a/37279407
 */
public class LocationStorage {

    @Expose public final String provider;
    @Expose public final double latitude;
    @Expose public final double longitude;

    public LocationStorage(String provider, double latitude, double longitude) {
        this.provider = provider;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static void save(Gson gson, SharedPreferences preferences, String key, Location location) {
        if (location != null) {
            LocationStorage storage = new LocationStorage(
                    location.getProvider(), location.getLatitude(), location.getLongitude());
            preferences.edit().putString(key, gson.toJson(storage)).apply();
        }
    }

    public static Location load(Gson gson, SharedPreferences preferences, String key) {
        String json = preferences.getString(key, null);
        if (json == null || json.isEmpty()) {
            return null;
        }
        LocationStorage storage = gson.fromJson(json, LocationStorage.class);
        if (storage == null) {
            return null;
        } else {
            Location result = new Location(storage.provider);
            result.setLatitude(storage.latitude);
            result.setLongitude(storage.longitude);
            return result;
        }
    }
}
