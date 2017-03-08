package com.sensorberg.sdk.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
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
import com.sensorberg.sdk.settings.SettingsManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Setter;

public class GeofenceManager implements GeofenceListener {

    private Context context;
    private SettingsManager settings;
    private SharedPreferences prefs;
    private Gson gson;
    private GeofenceStorage storage;
    private PlayServiceManager play;

    private List<GeofenceListener> listeners = new ArrayList<>();

    private HashMap<String, String> entered;    //Fence, pairingID

    private Location previous;
    private Location current;
    private boolean updating = false;
    private boolean enabled = false;
    @Setter
    private boolean registered = false;

    private final boolean playServicesNotAvailable;

    public GeofenceManager(Context context, SettingsManager settings, SharedPreferences prefs, Gson gson, PlayServiceManager play) {
        this.context = context;
        this.prefs = prefs;
        this.gson = gson;
        this.play = play;
        this.settings = settings;

        playServicesNotAvailable = (play == null);

        if (playServicesNotAvailable) {
            return;
        }

        entered = loadEntered();
        storage = new GeofenceStorage(context, settings, prefs);
        play.addListener(connectionCallbacks);
        previous = restorePrevious();
        current = restoreLastKnown();
        if (storage.getCount() > 0) {
            Logger.log.geofence("Geofences restored from DB");
            enable();
        }
    }

    public void clear() {

        if (playServicesNotAvailable) {
            return;
        }

        previous = null;
        storePrevious(previous);
        entered = new HashMap<>();
        onFencesChanged(new ArrayList<String>());
    }

    private void enable() {

        if (playServicesNotAvailable) {
            return;
        }

        if (!enabled) {
            enabled = true;
            Logger.log.geofence("Enable GEOFENCING: Geofences appeared");
            play.connect();
            requestLocationUpdates();
        }
    }

    private void disable() {

        if (playServicesNotAvailable) {
            return;
        }

        if (enabled && storage.getCount() == 0) {
            enabled = false;
            Logger.log.geofence("Disable GEOFENCING: No geofences in layout");
            removeLocationUpdates();
            play.disconnect();
        }
    }

    /**
     * This should be called only if layout changed, to avoid unnecessary DB operations.
     *
     * @param fences List of fence strings (8 char geohash plus 6 char radius).
     */
    public void onFencesChanged(List<String> fences) {

        if (playServicesNotAvailable) {
            return;
        }

        prefs.edit().putLong(Constants.SharedPreferencesKeys.Location.LAST_DB_UPDATED,
                System.currentTimeMillis()).apply();

        Logger.log.geofence("Update: layout change");
        storage.updateFences(fences);
        if (fences.size() == 0 && storage.getCount() == 0) {
            disable();
        } else {
            enable();
        }
        registered = false;
        if (storage.getCount() <= GeofenceStorage.HIGH) {
            requestSingleUpdate();
        }
        if (trigger(current)) {
            removeGeofences(current);
        }
    }

    public boolean shouldForceUpdate() {
        long lastUpdate = prefs.getLong(Constants.SharedPreferencesKeys.Location.LAST_DB_UPDATED, 0);
        return System.currentTimeMillis() - lastUpdate > settings.getLayoutUpdateInterval();
    }

    public void ping() {

        if (playServicesNotAvailable) {
            return;
        }

        Logger.log.geofence("Update: ping at " + current);
        if (trigger(current)) {
            removeGeofences(current);
        }
    }

    @Override
    public void onLocationChanged(Location incoming) {
        locationListener.onLocationChanged(incoming);
    }

    public void onGeofenceNotAvailable() {

        if (playServicesNotAvailable) {
            return;
        }

        if (enabled) {
            Logger.log.geofence("Event: Location state changed");
            registered = false;
            requestSingleUpdate();
        }
    }

    private boolean trigger(Location location) {
        if (!enabled) {
            Logger.log.geofence("Deny: No geofences in layout");
            return false;
        }
        if (!play.isGeofencingAvailable()) {
            Logger.log.geofenceError("Deny: Service is not available", null);
            return false;
        }
        if (!play.isConnected()) {
            play.connect();
            Logger.log.geofenceError("Deny: Service is not connected, will retry", null);
            return false;
        }
        if (updating) {
            Logger.log.geofence("Deny: Already updating");
            return false;
        }
        if (storage.getCount() <= GeofenceStorage.HIGH) {
            //We don't have to consider location below 100 geofences
            if (registered) {
                Logger.log.geofence("Deny: Below " + GeofenceStorage.HIGH + " and all registered");
                return false;
            } else {
                Logger.log.geofence("Allow: Below " + GeofenceStorage.HIGH + " and not registered");
                return true;
            }
        } else {
            if (previous == null) {
                if (location == null) {
                    Logger.log.geofence("Deny: No location available");
                    return false;
                }
                Logger.log.geofence("Allow: New location at " + location);
                return true;
            }
            //From now on previous != null
            if (location != null) {
                float change = previous.distanceTo(location);
                if (change < storage.getRadius()) {
                    Logger.log.geofence("Deny: Location change too small, " +
                            "was: " + change + "m, needed: " + storage.getRadius() + "m");
                    return false;
                }
                Logger.log.geofence("Allow: Location change large enough, " +
                        "was: " + change + "m, needed: " + storage.getRadius() + "m");
                return true;
            }
            //Technically not possible here, but oh well...
            Logger.log.geofence("Allow: Just in case, at " + location);
            return true;
        }
    }

    private void removeGeofences(final Location location) {
        updating = true;
        try {
            LocationServices.GeofencingApi
                    .removeGeofences(
                            play.getClient(),
                            GeofenceReceiver.getGeofencePendingIntent(context))
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
        } catch (SecurityException | IllegalStateException ex) {
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
                        .addGeofences(
                                play.getClient(),
                                request,
                                GeofenceReceiver.getGeofencePendingIntent(context))
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess()) {
                                    onGeofencesAdded(location, request.getGeofences(),
                                            request.getInitialTrigger());
                                } else {
                                    onGeofencesFailed(null, status.getStatusCode());
                                }
                            }
                        });
            }
        } catch (SecurityException | IllegalStateException ex) {
            onGeofencesFailed(ex, 0);
        }
    }

    private void onGeofencesAdded(Location location, List<Geofence> geofences, int initialTrigger) {
        registered = true;
        updating = false;
        previous = location;
        storePrevious(previous);
        Logger.log.geofence("Successfully added " + geofences.size() +
                " geofences, initial trigger: " + initialTrigger);
        requestLocationUpdates();
    }

    private void onGeofencesRemoved(Location location) {
        registered = true;
        updating = false;
        previous = location;
        storePrevious(previous);
        Logger.log.geofence("No geofences around, nothing tracked at " + location);
        disable();
    }

    private void onGeofencesFailed(Exception ex, int status) {
        updating = false;
        if (ex != null) {
            Logger.log.geofenceError("Failed to add geofences, error code: " + status, ex);
        } else {
            Logger.log.geofenceError("Failed to add geofences, error code: " + status, ex);
        }
    }

    @Override
    public void onGeofenceEvent(GeofenceData geofenceData, boolean entry, String pairingIdNotUsedHere) {

        if (playServicesNotAvailable) {
            return;
        }

        String pairingId = entered.get(geofenceData.getFence());
        if (entry) {
            if (pairingId != null) {
                Logger.log.geofence(
                        "Duplicate entry, skipping geofence: " + geofenceData.getFence());
                return;
            } else {
                pairingId = UUID.randomUUID().toString();
                entered.put(geofenceData.getFence(), pairingId);
                saveEntered(entered);
            }
        } else {
            if (pairingId == null) {
                Logger.log.geofence(
                        "Duplicate exit, skipping geofence: " + geofenceData.getFence());
                return;
            } else {
                entered.remove(geofenceData.getFence());
                saveEntered(entered);
            }
        }
        //TODO in V3 - If it's at the edge of registered radius then re-register.
        notifyListeners(geofenceData, entry, pairingId);
    }

    private List<GeofencingRequest> getGeofencingRequests(Location location) {
        List<GeofencingRequest> result = new ArrayList<>(2);
        try {
            HashMap<String, Geofence> triggerEnter = storage.getGeofences(location);
            HashMap<String, Geofence> triggerExit = new HashMap<>();
            //Move geofences we're inside to saved set of geofences,
            //which will be triggered when registered outside of geofence
            Iterator<Map.Entry<String, String>> iterator = entered.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> inside = iterator.next();
                Geofence temp = triggerEnter.get(inside.getKey());
                if (temp != null) {
                    triggerEnter.remove(inside.getKey());
                    triggerExit.put(inside.getKey(), temp);
                } else {
                    //Cleanup entered geofence if it's not anymore in range
                    iterator.remove();
                    //It's because of either:
                    // - Device moving far enough from this geofence with location disabled,
                    // so it's not listed in 100 nearest
                    // - Removing this geofence from layout
                    Logger.log.geofenceError("Exit event for " + inside.getKey() +
                            " not triggered, probably not relevant anymore", null);
                }
            }
            saveEntered(entered);
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

    private void requestSingleUpdate() {
        LocationRequest request = LocationRequest.create();
        request.setNumUpdates(1);
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        try {
            PendingResult<Status> result;
            result = LocationServices.FusedLocationApi.requestLocationUpdates(
                    play.getClient(), request, locationListener, Looper.myLooper());
            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (!status.isSuccess()) {
                        Logger.log.geofenceError("Requesting single location update failed " + status.getStatusCode(), null);
                    }
                }
            });
        } catch (SecurityException ex) {
            Logger.log.geofence("Insufficient permission for location updates");
        } catch (IllegalStateException ex) {
            Logger.log.geofence("Play service client is not connected");
        }
    }

    private void requestLocationUpdates() {
        if (storage.getCount() <= GeofenceStorage.HIGH) {
            return;
        }

        //Calculate displacement and update time
        final float displacement = Math.max(
                storage.getRadius(),
                settings.getGeofenceMinUpdateDistance());
        final long time = Math.max(
                ((long) displacement / settings.getGeofenceMaxDeviceSpeed()) * 1000,
                settings.getGeofenceMinUpdateTime());
        LocationRequest request = LocationRequest.create();
        //BALANCED priority gives "block" level accuracy - about 100m, which suits our case
        //Next priority LOW_POWER is "city" level accuracy - about 10km, which might be not enough
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //Set normal, min, max update timing
        request.setInterval(time);
        request.setFastestInterval(time / 2);
        request.setMaxWaitTime(2 * time);
        //Smallest displacement overrides all three timing settings above
        request.setSmallestDisplacement(displacement);

        try {
            PendingResult<Status> result;
            result = LocationServices.FusedLocationApi.requestLocationUpdates(
                    play.getClient(),
                    request,
                    GeofenceReceiver.getLocationPendingIntent(context));
            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (!status.isSuccess()) {
                        Logger.log.geofenceError("Requesting location updates failed " +
                                status.getStatusCode(), null);
                    } else {
                        Logger.log.geofence("Registered location updates with time: " + time + " displacement: " + displacement);
                    }
                }
            });
        } catch (SecurityException ex) {
            Logger.log.geofence("Insufficient permission for location updates");
        } catch (IllegalStateException ex) {
            Logger.log.geofence("Play service client is not connected");
        }
    }

    private void removeLocationUpdates() {
        try {
            PendingResult<Status> result;
            result = LocationServices.FusedLocationApi.removeLocationUpdates(
                    play.getClient(),
                    GeofenceReceiver.getLocationPendingIntent(context));
            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (!status.isSuccess()) {
                        Logger.log.geofenceError("Removing location updates failed " +
                                status.getStatusCode(), null);
                    }
                }
            });
        } catch (SecurityException ex) {
            Logger.log.geofence("Insufficient permission for location updates");
        } catch (IllegalStateException ex) {
            Logger.log.geofence("Play service client is not connected");
        }
    }

    public void onLocationPermissionGranted() {

        //if (playServicesNotAvailable) {
        //    return;
        //}

        //Right now there's no callback for that - on permission change app is killed and restarted.
        //This is left in here in case the method for callback appears in Android.
        //updateRegistredGeofences();
    }

    public void addListener(GeofenceListener listener) {

        if (playServicesNotAvailable) {
            return;
        }

        for (GeofenceListener previous : listeners) {
            if (previous == listener) {
                return;
            }
        }
        listeners.add(listener);
    }

    public void removeListener(GeofenceListener listener) {

        if (playServicesNotAvailable) {
            return;
        }

        Iterator<GeofenceListener> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            GeofenceListener existing = iterator.next();
            if (existing == listener) {
                iterator.remove();
                return;
            }
        }
    }

    private void notifyListeners(GeofenceData geofenceData, boolean entry, String pairingId) {
        for (GeofenceListener listener : listeners) {
            listener.onGeofenceEvent(geofenceData, entry, pairingId);
        }
    }

    private void saveEntered(HashMap<String, String> entered) {
        String key = Constants.SharedPreferencesKeys.Location.ENTERED_GEOFENCES_SET;
        prefs.edit().putString(key, gson.toJson(entered)).apply();
    }

    private HashMap<String, String> loadEntered() {
        String key = Constants.SharedPreferencesKeys.Location.ENTERED_GEOFENCES_SET;
        String json = prefs.getString(key, null);
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    private void storeLastKnown(Location location) {
        LocationStorage.save(gson, prefs,
                Constants.SharedPreferencesKeys.Location.LAST_KNOWN_LOCATION, location);
    }

    private Location restoreLastKnown() {
        return LocationStorage.load(gson, prefs,
                Constants.SharedPreferencesKeys.Location.LAST_KNOWN_LOCATION);
    }

    private void storePrevious(Location location) {
        LocationStorage.save(gson, prefs,
                Constants.SharedPreferencesKeys.Location.PREVIOUS_LOCATION, location);
    }

    private Location restorePrevious() {
        return LocationStorage.load(gson, prefs,
                Constants.SharedPreferencesKeys.Location.PREVIOUS_LOCATION);
    }

    private final GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            if (playServicesNotAvailable) {
                return;
            }

            if (enabled) {
                Logger.log.geofence("Event: Play services connection");
                requestSingleUpdate();
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            //Nothing. Google Play Services should reconnect automatically.
        }
    };

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location incoming) {

            if (playServicesNotAvailable) {
                return;
            }

            if (incoming != null) {
                current = incoming;
                storeLastKnown(current);
                Logger.log.geofence("Update: location change at " + incoming);
                if (trigger(incoming)) {
                    removeGeofences(incoming);
                }
            }
        }
    };
}

