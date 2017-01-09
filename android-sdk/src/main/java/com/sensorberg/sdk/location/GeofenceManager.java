package com.sensorberg.sdk.location;

import android.Manifest;
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
import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.PermissionChecker;
import com.sensorberg.sdk.settings.TimeConstants;

import java.util.List;

import lombok.Setter;

public class GeofenceManager implements LocationHelper.LocationStateListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final int FACTOR = 3;

    private Context context;
    private PermissionChecker checker;
    private SharedPreferences preferences;
    private Gson gson;
    private LocationHelper location;
    private GeofenceStorage storage;
    private GeofenceReceiver receiver;
    private PlayServiceManager play;

    private Location previous;
    private boolean updating = false;
    @Setter private boolean registred = true;

    public interface GeofenceListener {
        void onGeofenceEvent(GeofenceData geofenceData, boolean entry);
    }

    public GeofenceManager(Context context, SharedPreferences preferences, Gson gson,
                           PermissionChecker checker, LocationHelper location) {
        this.context = context;
        this.checker = checker;
        this.location = location;
        this.preferences = preferences;
        this.gson = gson;
        receiver = new GeofenceReceiver(context, this);
        storage = new GeofenceStorage(context, preferences);
        //This will callback asynchronously when service is connected.
        play = new PlayServiceManager(context, this);
        location.addListener(this);
    }

    /**
     * This should be called only if layout changed, to avoid unnecessary DB operations.
     * @param fences List of fence strings (8 char geohash plus 6 char radius).
     */
    public void updateFences(List<String> fences) {
        Logger.log.geofence("Update triggered by layout change");
        registred = false;
        int count = storage.getCount();
        storage.updateFences(fences);
        if (!play.isConnected() && count == 0 && !fences.isEmpty()) {
            //Enabling geofencing
            play.connect();
        }
        updateRegistredGeofences(getLocation());

    }

    public void onLocationStateChanged(boolean enabled) {
        registred = false;
        if (enabled) {
            Location location = getLocation();
            Logger.log.geofence("Update triggered by enabling location " + location);
            updateRegistredGeofences(location);
        }
    }

    @Override
    public void onLocationChanged(Location incoming) {
        if (incoming != null) {
            if (previous != null) {
                float change = previous.distanceTo(incoming);
                float needed = storage.getRadius() / FACTOR;
                if (change < needed) {
                    Logger.log.geofence("Aborting. Location change too small, " +
                            "was: " + change + "m, needed: " + needed + "m");
                    return;
                }
            }
            registred = false;
            Logger.log.geofence("Update triggered by location change " + incoming);
            updateRegistredGeofences(incoming);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Logger.log.geofence("Update triggered by Play services connection");
        updateRegistredGeofences(getLocation());
    }

    private void updateRegistredGeofences(Location location) {
        if (registred) {
            Logger.log.geofence("No changes, aborting");
            return;
        }
        if (updating) {
            Logger.log.geofence("Update already in progress");
            return;
        }
        if (!checker.checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Logger.log.geofence("Missing ACCESS_FINE_LOCATION permission");
            return;
        }
        if (!this.location.isLocationEnabled()) {
            Logger.log.geofence("Location is not enabled");
            return;
        }
        if (!play.isConnected()) {
            Logger.log.geofenceError("Google API client is not connected", null);
            play.connect(); //will update when connected.
            return;
        } else {

        }
        removeGeofences(location);
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
        final GeofencingRequest request = getGeofencingRequest(location);
        if (request == null) {
            onGeofencesRemoved(location);
            return;
        }
        try {
            LocationServices.GeofencingApi
                    .addGeofences(play.getClient(), request, getPendingIntent())
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                onGeofencesAdded(location, request.getGeofences());
                            } else {
                                onGeofencesFailed(null, status.getStatusCode());
                            }
                        }
                    });
        } catch (SecurityException ex) {
            onGeofencesFailed(ex, 0);
        }
    }

    private void onGeofencesAdded(Location location, List<Geofence> geofences) {
        registred = true;
        updating = false;
        previous = location;
        Logger.log.geofence("Successfully added " + geofences.size() + " geofences");
        requestLocationUpdates();
    }

    private void onGeofencesRemoved(Location location) {
        registred = true;
        updating = false;
        previous = location;
        Logger.log.geofence("No geofences to add");
        removeLocationUpdates();
        play.disconnect();
    }

    private void onGeofencesFailed(SecurityException ex, int status) {
        updating = false;
        if (ex != null) {
            Logger.log.geofenceError("Failed to add geofences, error code: "+status, ex);
        } else {
            Logger.log.geofenceError("Failed to add geofences, error code: "+status, ex);
        }
    }

    private GeofencingRequest getGeofencingRequest(Location location) {
        try {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            List<Geofence> geofences = storage.getGeofences(location);
            if (geofences.size() > 0) {
                builder.addGeofences(geofences);
            } else {
                return null;
            }
            return builder.build();
        } catch (SQLException ex) {
            Logger.log.geofenceError("Can't build geofencing reqest", ex);
            return null;
        }
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(GeofenceReceiver.ACTION);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void requestLocationUpdates() {
        LocationRequest request = LocationRequest.create();
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

    private Location getLocation() {
        if (play.isConnected()) {
            try {
                Location last = LocationServices.FusedLocationApi.getLastLocation(play.getClient());
                if (last != null) {
                    storeLastKnownNonNullLocation(last);
                    return last;
                }
            } catch (SecurityException ex) {
                Logger.log.geofenceError("Can't get location from PlayServices (using backup).", ex);
            }
        }
        Location last = location.getLastKnownLocation();
        if (last != null) {
            storeLastKnownNonNullLocation(last);
            return last;
        }
        return restoreLastKnownLocation();
    }

    private void storeLastKnownNonNullLocation(Location location) {
        LocationStorage.save(gson, preferences, Constants.SharedPreferencesKeys.LAST_KNOWN_LOCATION, location);
    }

    private Location restoreLastKnownLocation() {
        return LocationStorage.load(gson, preferences, Constants.SharedPreferencesKeys.LAST_KNOWN_LOCATION);
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

    public void addListener(GeofenceManager.GeofenceListener listener) {
        receiver.addListener(listener);
    }

    public void removeListener(GeofenceManager.GeofenceListener listener) {
        receiver.removeListener(listener);
    }
}

