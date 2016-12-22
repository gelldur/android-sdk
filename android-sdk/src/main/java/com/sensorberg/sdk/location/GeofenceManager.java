package com.sensorberg.sdk.location;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.PermissionChecker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class for handling communication with Service.
 */
public class GeofenceManager extends BroadcastReceiver implements
        LocationHelper.LocationStateListener, GoogleApiClient.ConnectionCallbacks {

    public static final String ACTION = "com.sensorberg.sdk.receiver.GEOFENCE";

    private Context context;
    private PermissionChecker checker;
    private LocationHelper location;
    private GeofenceStorage storage;
    private PlayServiceManager play;

    private List<GeofenceListener> listeners = new ArrayList<>();

    private boolean updating = false;
    private boolean committed = false;

    public interface GeofenceListener {
        void onGeofenceEvent(GeofenceData geofenceData, boolean entry);
    }

    public GeofenceManager(Context context, SharedPreferences preferences, Gson gson,
                           PermissionChecker checker, LocationHelper location) {
        this.context = context;
        this.checker = checker;
        this.location = location;
        registerReceiver();
        storage = new GeofenceStorage(preferences, gson);
        //This will callback asynchronously here when service is connected.
        play = new PlayServiceManager(context, this);
        location.addListener(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event == null) {
            Logger.log.logError("GeofencingEvent is null");
            return;
        }
        if (event.hasError() && event.getErrorCode() == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
            //This runs in a case of e.g. disabling location on the device.
            //If we've registred geofence before, Google Play Service lets us know about removal here.
            //(But we don't rely only on it, cause we're smart and listen to disabling location anyway)
            committed = false;
            return;
        }
        try {
            List<GeofenceData> geofenceDatas = GeofenceData.from(event);
            boolean entry = event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER;
            for (GeofenceData geofenceData : geofenceDatas) {
                notifyListeners(geofenceData, entry);
            }
        } catch (IllegalArgumentException ex) {
            Logger.log.logError("Received invalid geofence event", ex);
        }
    }

    public void updateFences(List<String> fences) {
        if (fences.size() > 100) {
            throw new IllegalArgumentException("Can't have more than 100 geofences");
        }
        storage.updateFences(fences);
        updateRegistredGeofences();
    }

    private boolean checkConditions() {
        if (committed && !storage.isChanged()) {
            Logger.log.debug("Geofences update: No changes, aborting.");
            return false;
        }
        if (!play.isConnected()) {
            Logger.log.logError("Geofences update: Google API client is not connected.");
            return false;
        }
        if (updating) {
            Logger.log.debug("Geofences update: Already in progress.");
            return false;
        }
        if (!checker.checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Logger.log.debug("Geofences update: Missing ACCESS_FINE_LOCATION permission");
            return false;
        }
        if (!location.isLocationEnabled()) {
            Logger.log.debug("Geofences update: Location is not enabled");
            return false;
        }
        return true;
    }

    private void updateRegistredGeofences() {
        if (!checkConditions()) {
            return;
        }
        updating = true;
        try {
            LocationServices.GeofencingApi
                    .removeGeofences(play.getClient(), getPendingIntent())
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                registerAllGeofences();
                            } else {
                                Logger.log.logError("Failed to remove geofences, error code: "+status.getStatusCode());
                                updating = false;
                            }
                        }
                    });
        } catch (SecurityException ex) {
            Logger.log.logError("Can't remove geofences, missing ACCESS_FINE_LOCATION permission");
            updating = false;
        }
    }

    private void registerAllGeofences() {
        if (storage.getGeofences().isEmpty()) {
            updating = false;
            committed = true;
            Logger.log.debug("No geofences to add");
            return;
        }
        try {
            updating = true;
            LocationServices.GeofencingApi
                    .addGeofences(play.getClient(), getGeofencingRequest(), getPendingIntent())
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                committed = true;
                                Logger.log.debug("Geofences successfully added: "+storage.getGeofences());
                            } else {
                                Logger.log.logError("Failed to add geofences, error code: "+status.getStatusCode());
                            }
                            updating = false;
                        }
                    });
        } catch (SecurityException ex) {
            Logger.log.logError("Can't add geofences, missing ACCESS_FINE_LOCATION permission");
            updating = false;
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(storage.getGeofences());
        return builder.build();
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(ACTION);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void onLocationStateChanged(boolean enabled) {
        if (enabled) {
            //Setting as uncommitted - Google Play Services removes geofences when location is disabled.
            committed = false;
            updateRegistredGeofences();
        }
    }

    public void onLocationPermissionGranted () {
        //Right now there's no callback for that - on permission change app is killed, then restarted.
        //This is left in here in case the method for callback appears in Android.
        //committed = false;
        //updateRegistredGeofences();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        updateRegistredGeofences();
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
            registerReceiver();
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
                    context.unregisterReceiver(this);
                }
                return;
            }
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION);
        context.registerReceiver(this, filter);
    }

    private void notifyListeners(GeofenceData geofenceData, boolean entry) {
        for (GeofenceListener listener : listeners) {
            listener.onGeofenceEvent(geofenceData, entry);
        }
    }
}

