package com.sensorberg.sdk.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.settings.TimeConstants;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import lombok.Setter;

public class GeofenceManager implements LocationSource.LocationStateListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener, GeofenceListener {

    private static final int FACTOR = 3;

    private Context context;
    private SharedPreferences preferences;
    private Gson gson;
    private LocationSource source;
    private GeofenceStorage storage;
    private GeofenceReceiver receiver;
    private PlayServiceManager play;

    private List<GeofenceListener> listeners = new ArrayList<>();

    private HashSet<String> entered;

    private Location previous;
    private Location current;
    private boolean updating = false;
    private boolean empty = true;
    @Setter private boolean registered = true;

    public GeofenceManager(Context context, SharedPreferences preferences, Gson gson,
                           LocationSource source, PlayServiceManager play) {
        this.context = context;
        this.source = source;
        this.preferences = preferences;
        this.gson = gson;
        this.play = play;
        entered = loadEntered();
        receiver = new GeofenceReceiver(context, this);
        storage = new GeofenceStorage(context, preferences);
        //This will callback asynchronously when service is connected.
        source.addListener(this);
        play.addListener(this);
        if (storage.getCount() > 0) {
            empty = false;
            Logger.log.geofence("Enable GEOFENCING: Geofences restored from DB");
            play.connect();
        }
    }

    /**
     * This should be called only if layout changed, to avoid unnecessary DB operations.
     * @param fences List of fence strings (8 char geohash plus 6 char radius).
     */
    public void updateFences(List<String> fences) {
        Logger.log.geofence("Update: layout change");
        if (!empty && storage.getCount() == 0 && fences.size() == 0) {
            empty = true;
            Logger.log.geofence("Disable GEOFENCING: No geofences in layout");
            play.disconnect();
            return;
        } else if (empty) {
            empty = false;
            Logger.log.geofence("Enable GEOFENCING: Geofences appeared in layout");
            play.connect();
        }
        storage.updateFences(fences);
        registered = false;
        if (trigger()) {
            removeGeofences(current);
        }
    }

    //TODO use only this callback for anything regarding location...
    @Override
    public void onLocationChanged(Location incoming) {
        if (incoming != null) {
            source.setLastKnownIfBetter(incoming, true);    //TODO here mock location leaks to location pool
        } else {
            Logger.log.geofence("Warning, Play Service returned null location, using backup");
            incoming = source.getLastKnownLocation();
        }
        Logger.log.geofence("Update: location change at " + incoming);
        if (trigger()) {
            removeGeofences(current);
        }
    }

    public void onLocationStateChanged(boolean enabled) {
        if (enabled) {
            Logger.log.geofence("Update: enabling location");
            registered = false;
            if (trigger()) {
                removeGeofences(current);
            } else if (current == null && storage.getCount() > storage.HIGH) {
                requestLocationUpdates(1);  //TODO well, crap. It removes previous request.
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Logger.log.geofence("Update: Play services connection");
        if (trigger()) {
            removeGeofences(current);
        }
    }

    private boolean trigger() {
        //TODO what if updating?
        if (empty) {
            Logger.log.geofence("Deny: No geofences in layout");
            return false;
        }
        if (!play.isGeofencingAvailable()) {
            Logger.log.geofenceError("Deny: Service is not available", null);
            return false;
        }
        if (!play.isConnected()) {
            play.connect();
            Logger.log.geofenceError("Deny: Service is not connected, will retry when connects", null);
            return false;
        }
        if (storage.getCount() < GeofenceStorage.HIGH) {    //TODO keep getCount in memory
            //We don't have to consider location below 100 geofences
            if (registered) {
                Logger.log.geofence("Deny: Below " + GeofenceStorage.HIGH + " and all are registered");
                return false;
            } else {
                Logger.log.geofence("Allow: Below " + GeofenceStorage.HIGH + " and not registered");
                current = source.getLastKnownLocation();
                return true;
            }
        } else {
            current = source.getLastKnownLocation();
            if (previous == null) {
                if (current == null) {
                    Logger.log.geofence("Deny: No location available");
                    return false;
                }
                Logger.log.geofence("Allow: New location at " + current);
                return true;
            }
            //From now on previous != null
            if (current != null) {
                float change = previous.distanceTo(current);
                float needed = storage.getRadius() / FACTOR;
                if (change < needed) {
                    Logger.log.geofence("Deny: Location change too small, " + "was: " + change + "m, needed: " + needed + "m");
                    return false;
                }
                Logger.log.geofence("Allow: Location change large enough, " + "was: " + change + "m, needed: " + needed + "m");
                return true;
            }
            //Technically not possible at this moment, but oh well...
            Logger.log.geofence("Allow: Just in case, at " + current);
            return true;
        }
    }

    private void removeGeofences(final Location location) {
        updating = true;
        try {
            LocationServices.GeofencingApi
                    .removeGeofences(play.getClient(), getPendingIntent())
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                registerGeofences(location);
                            } else {
                                onGeofencesFailed(null, status.getStatusCode());
                            }
                        }
                    });
        } catch (SecurityException ex) {
            onGeofencesFailed(ex, 0);
        }
    }

    private void registerGeofences(final Location location) {
        final List<GeofencingRequest> requests = getGeofencingRequests(location);
        if (requests.isEmpty()) {
            onGeofencesRemoved(location);
            return;
        }
        try {
            for (final GeofencingRequest request : requests) {
                LocationServices.GeofencingApi
                        .addGeofences(play.getClient(), request, getPendingIntent())
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess()) {
                                    onGeofencesAdded(location, request.getGeofences(), request.getInitialTrigger());
                                } else {
                                    onGeofencesFailed(null, status.getStatusCode());
                                }
                            }
                        });
            }
        } catch (SecurityException ex) {
            onGeofencesFailed(ex, 0);
        }
    }

    private void onGeofencesAdded(Location location, List<Geofence> geofences, int initialTrigger) {
        registered = true;
        updating = false;
        previous = location;
        Logger.log.geofence("Successfully added " + geofences.size() + " geofences, initial trigger: "+initialTrigger);
        requestLocationUpdates(0);
    }

    private void onGeofencesRemoved(Location location) {
        registered = true;
        updating = false;
        previous = location;
        Logger.log.geofence("No geofences around, disconnecting from Play Service, at: "+location);
        removeLocationUpdates();
        play.disconnect();
        empty = true;
    }

    private void onGeofencesFailed(SecurityException ex, int status) {
        updating = false;
        if (ex != null) {
            Logger.log.geofenceError("Failed to add geofences, error code: "+status, ex);
        } else {
            Logger.log.geofenceError("Failed to add geofences, error code: "+status, ex);
        }
    }


    @Override
    public void onGeofenceEvent(GeofenceData geofenceData, boolean entry) {
        if (entry) {
            if (entered.contains(geofenceData.getFence())) {
                Logger.log.geofence("Duplicate entry, skipping geofence: "+geofenceData.getFence());
                return;
            } else {
                entered.add(geofenceData.getFence());
                save(entered);
            }
        } else {
            if (!entered.contains(geofenceData.getFence())) {
                Logger.log.geofence("Duplicate exit, skipping geofence: "+geofenceData.getFence());
                return;
            } else {
                entered.remove(geofenceData.getFence());
                save(entered);
            }
        }
        notifyListeners(geofenceData, entry);
    }

    private List<GeofencingRequest> getGeofencingRequests(Location location) {
        List<GeofencingRequest> result = new ArrayList<>(2);
        try {
            HashMap<String, Geofence> triggerEnter = storage.getGeofences(location);
            HashMap<String, Geofence> triggerExit = new HashMap<>();
            //Move geofences we're inside to saved set of geofences which will be triggered when registered outside of geofence
            Iterator<String> iterator = entered.iterator();
            while (iterator.hasNext()) {
                String inside = iterator.next();
                Geofence temp = triggerEnter.get(inside);
                if (temp != null) {
                    triggerEnter.remove(inside);
                    triggerExit.put(inside, temp);
                } else {
                    //Cleanup entered geofence if it's not anymore in range
                    iterator.remove();
                    //It's because of either:
                    // - Device moving far enough from this geofence with location disabled, so it's not listed in 100 nearest
                    // - Removing this geofence from layout
                    Logger.log.geofenceError("Exit event for "+inside+" not triggered, probably not relevant anymore", null);
                }
            }
            save(entered);
            if (triggerEnter.size() > 0) {
                GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
                builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
                builder.addGeofences(new ArrayList<>(triggerEnter.values()));
                result.add(builder.build());
            }
            if (triggerExit.size() > 0) {
                GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
                builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
                builder.addGeofences(new ArrayList<>(triggerExit.values()));
                result.add(builder.build());
            }
        } catch (SQLException ex) {
            Logger.log.geofenceError("Can't build geofencing reqest", ex);
        }
        return result;
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(GeofenceReceiver.ACTION);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void requestLocationUpdates(int count) {
        LocationRequest request = LocationRequest.create();
        if (count > 0) {
            request.setNumUpdates(count);
        }
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        request.setFastestInterval(TimeConstants.ONE_HOUR);
        request.setInterval(2 * TimeConstants.ONE_HOUR);
        request.setSmallestDisplacement(storage.getRadius() / FACTOR);
        request.setMaxWaitTime(TimeConstants.ONE_HOUR);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(play.getClient(), request, this, Looper.myLooper());
        } catch (SecurityException ex) {
            Logger.log.geofenceError("Insufficient permission for location updates", ex);
        }
    }

    private void removeLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(play.getClient(), this);
    }

    public void onLocationPermissionGranted () {
        //Right now there's no callback for that - on permission change app is killed, then restarted.
        //This is left in here in case the method for callback appears in Android.
        //updateRegistredGeofences();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Nothing. Google Play Services should reconnect automatically.
    }

    public void addListener(GeofenceListener listener) {
        for (GeofenceListener previous : listeners) {
            if(previous == listener) {
                return;
            }
        }
        if (listeners.size() == 0) {
            receiver.addListener(this);
        }
        listeners.add(listener);
    }

    public void removeListener(GeofenceListener listener) {
        Iterator<GeofenceListener> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            GeofenceListener existing = iterator.next();
            if(existing == listener) {
                iterator.remove();
                if (listeners.size() == 0) {
                    receiver.removeListener(this);
                }
                return;
            }
        }
    }

    private void notifyListeners(GeofenceData geofenceData, boolean entry) {
        for (GeofenceListener listener : listeners) {
            listener.onGeofenceEvent(geofenceData, entry);
        }
    }

    public void save(HashSet<String> entered) {
        String key = Constants.SharedPreferencesKeys.Location.ENTERED_GEOFENCES_SET;
        preferences.edit().putString(key, gson.toJson(entered)).apply();
    }

    public HashSet<String> loadEntered() {
        String key = Constants.SharedPreferencesKeys.Location.ENTERED_GEOFENCES_SET;
        String json = preferences.getString(key, null);
        if (json == null || json.isEmpty()) {
            return new HashSet<>();
        }
        Type type = new TypeToken<HashSet<String>>() {}.getType();
        return gson.fromJson(json, type);
    }
}

