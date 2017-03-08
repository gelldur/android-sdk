package com.sensorberg.sdk.location;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.PermissionChecker;
import com.sensorberg.sdk.settings.TimeConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.Getter;

public class PlayServiceManager {

    private static final long SERVICE_RECONNECT_INTERVAL = 15 * TimeConstants.ONE_MINUTE;
    private boolean retry = false;

    private List<GoogleApiClient.ConnectionCallbacks> listeners = new ArrayList<>();

    private Context context;
    private PermissionChecker checker;
    private LocationHelper location;

    private GoogleApiClient client;
    private GoogleApiAvailability availability;
    private int logged = ConnectionResult.SUCCESS;

    private Handler handler;

    @Getter
    private int status;

    public PlayServiceManager(Context context, LocationHelper location, PermissionChecker checker) {
        this.context = context;
        this.location = location;
        this.checker = checker;
        availability = GoogleApiAvailability.getInstance();
        status = availability.isGooglePlayServicesAvailable(context);
        client = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .addApi(LocationServices.API)
                .build();
        if (status != ConnectionResult.SUCCESS) {
            Logger.log.geofenceError("Google Api Client status: " + status + " message: " + availability.getErrorString(status), null);
        }
        handler = new Handler(Looper.getMainLooper());
    }

    public GoogleApiClient getClient() {
        return client;
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public boolean isGeofencingAvailable() {
        status = availability.isGooglePlayServicesAvailable(context);
        return checker.checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && location.isLocationEnabled()
                && (status == ConnectionResult.SUCCESS || status == ConnectionResult.SERVICE_UPDATING);
    }

    public boolean connect() {
        status = availability.isGooglePlayServicesAvailable(context);
        switch (status) {
            case ConnectionResult.SUCCESS:
                if (!client.isConnected() && !client.isConnecting()) {
                    retry(0);
                }
                return true;
            case ConnectionResult.SERVICE_UPDATING:
                Logger.log.geofenceError("Google Api Client " + availability.getErrorString(status), null);
                retry(SERVICE_RECONNECT_INTERVAL);
                return true;
            default:
                if (logged != status) {
                    logged = status;
                    Logger.log.geofenceError("Google Api Client " + availability.getErrorString(status), null);
                }
                return false;
        }
    }

    public boolean disconnect() {
        handler.removeCallbacksAndMessages(null);
        retry = false;
        if (client.isConnected()) {
            client.disconnect();
            Logger.log.geofence("Google Api Client disconnected");
        }
        return true;
    }

    private void retry(long delay) {
        //Retry only if not already retrying
        if (!retry) {
            retry = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    client.connect();
                    Logger.log.geofence("Google Api Client connecting...");
                    retry = false;
                }
            }, delay);
        }
    }

    public void addListener(GoogleApiClient.ConnectionCallbacks listener) {
        for (GoogleApiClient.ConnectionCallbacks previous : listeners) {
            if (previous == listener) {
                return;
            }
        }
        listeners.add(listener);
    }

    public void removeListener(GoogleApiClient.ConnectionCallbacks listener) {
        Iterator<GoogleApiClient.ConnectionCallbacks> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            GoogleApiClient.ConnectionCallbacks existing = iterator.next();
            if (existing == listener) {
                iterator.remove();
                return;
            }
        }
    }

    private final GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            for (GoogleApiClient.ConnectionCallbacks listener : listeners) {
                listener.onConnected(bundle);
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            //Listener shouldn't do anything. Google Play Services should reconnect automatically.
        }
    };

    private final GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Logger.log.geofenceError("Could not connect to Google Services API: "
                    + connectionResult.getErrorMessage() + " code: " + connectionResult.getErrorCode(), null);
        }
    };

}
