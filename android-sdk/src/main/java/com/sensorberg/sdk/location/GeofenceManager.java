package com.sensorberg.sdk.location;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.PermissionChecker;
import com.sensorberg.sdk.settings.TimeConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class for handling communication with Service.
 */
public class GeofenceManager extends BroadcastReceiver implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = GeofenceManager.class.getName();

    public static final String ACTION = "com.sensorberg.sdk.receiver.GEOFENCE";

    private int retries;
    public static final int SERVICE_RECONNECT_LIMIT = 3;
    public static final long SERVICE_RECONNECT_INTERVAL = TimeConstants.ONE_MINUTE;

    public static final long UPDATE_IN_PROGRESS_RETRY_DELAY = TimeConstants.ONE_MINUTE;

    private Context context;

    private PermissionChecker checker;

    private LocationHelper location;

    protected GoogleApiClient googleApiClient;

    private GeofenceStorage storage;

    private Handler handler;

    private boolean updateInProgress;

    public interface GeofenceListener {
        void onGeofenceEvent(String geofence, boolean entry);
    }

    public GeofenceManager(Context context, SharedPreferences preferences, Gson gson,
                           PermissionChecker checker, LocationHelper location) {
        this.context = context;
        this.checker = checker;
        this.location = location;
        storage = new GeofenceStorage(preferences, gson);
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        handler = new Handler();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Logger.log.logError("Received geofence with error code: "+geofencingEvent.getErrorCode());
            return;
        } else {
            String geofenceId = geofencingEvent.getTriggeringGeofences().get(0).getRequestId();
            boolean entry = geofencingEvent.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER;
            notifyListeners(geofenceId, entry);
        }
    }

    public void updateGeofences(List<String> geofences) {
        if (geofences.size() > 100) {
            throw new IllegalArgumentException("Can't have more than 100 geofences");
        }
        storage.updateGeofences(geofences);
        updateGeofences();
    }

    private boolean checkConditions() {
        if (!storage.hasPending()) {
            //We've got same set of geofences.
            return false;
        }
        if (updateInProgress) {
            //Update already in progress, retry in some time just to be sure.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateGeofences();
                }
            }, UPDATE_IN_PROGRESS_RETRY_DELAY);
            return false;
        }
        if (!googleApiClient.isConnected()) {
            //Client not connected
            if (googleApiClient.isConnecting()) {
                //Already trying to connect
                return false;
            } else {
                Logger.log.logError("Can't update geofences, Google API client is not connected.");
                return false;
            }
        }
        if (!checker.checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Logger.log.logError("Can't update geofences, missing ACCESS_FINE_LOCATION permission");
            return false;
        }
        if (!location.isLocationEnabled()) {
            Logger.log.logError("Can't update geofences, location is not enabled");
            return false;
        }
        return true;
    }

    private void updateGeofences() {
        if (!checkConditions()) {
            return;
        }
        updateInProgress = true;
        try {
            LocationServices.GeofencingApi
                    .removeGeofences(googleApiClient, getPendingIntent())
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                addAllGeofences();
                            } else {
                                Logger.log.logError("Failed to remove geofences, error code: "+status.getStatusCode());
                                updateInProgress = false;
                            }
                        }
                    });
        } catch (SecurityException ex) {
            Logger.log.logError("Can't remove geofences, missing ACCESS_FINE_LOCATION permission");
            updateInProgress = false;
        }
    }

    private void addAllGeofences() {
        try {
            updateInProgress = true;
            LocationServices.GeofencingApi
                    .addGeofences(googleApiClient, getGeofencingRequest(), getPendingIntent())
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                storage.setPending(false);
                            } else {
                                Logger.log.logError("Failed to add geofences, error code: "+status.getStatusCode());
                            }
                            updateInProgress = false;
                        }
                    });
        } catch (SecurityException ex) {
            Logger.log.logError("Can't add geofences, missing ACCESS_FINE_LOCATION permission");
            updateInProgress = false;
        }
    }

    public void onLocationAvailable() {
        //Not used yet, TODO
        //updateGeofences();
    }

    public void onLocationPermissionGranted () {
        //Right now there's no callback for that - on permission change app is killed, then restarted.
        //This is left in here in case the method for callback appears in Android.
        //updateGeofences();
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        updateGeofences();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Nothing. Google Play Services should reconnect automatically.
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Logger.log.logError("Could not connect to Google Services API: "
                +connectionResult.getErrorMessage()+" code: "+connectionResult.getErrorCode());
        if (retries < SERVICE_RECONNECT_LIMIT) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    retries++;
                    googleApiClient.connect();
                }
            }, SERVICE_RECONNECT_INTERVAL);
        }
    }

    private List<GeofenceListener> listeners = new ArrayList<>();

    public void addListener(GeofenceListener listener) {
        for (GeofenceListener previous : listeners) {
            if(previous == listener) {
                //TODO log if already added
                return;
            }
        }
        if (listeners.size() == 0) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION);
            context.registerReceiver(this, filter);
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
        //TODO log if not found
    }

    private void notifyListeners(String geofenceId, boolean entry) {
        for (GeofenceListener listener : listeners) {
            listener.onGeofenceEvent(geofenceId, entry);
        }
    }
}

