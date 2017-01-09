package com.sensorberg.sdk.location;

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
import com.sensorberg.sdk.settings.TimeConstants;

import lombok.Getter;

public class PlayServiceManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final long SERVICE_RECONNECT_INTERVAL = 15 * TimeConstants.ONE_MINUTE;
    private boolean retry = false;

    @Getter private int status;

    private Context context;
    private GoogleApiClient client;
    private GoogleApiClient.ConnectionCallbacks listener;
    private GoogleApiAvailability availability;
    private int logged = ConnectionResult.SUCCESS;

    private Handler handler;

    public PlayServiceManager(Context context, GoogleApiClient.ConnectionCallbacks listener) {
        this.context = context;
        this.listener = listener;
        availability = GoogleApiAvailability.getInstance();
        status = availability.isGooglePlayServicesAvailable(context);
        client = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
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

    public boolean connect() {
        status = availability.isGooglePlayServicesAvailable(context);
        switch (status) {
            case ConnectionResult.SUCCESS:
                if (!client.isConnected() && !client.isConnecting()) {
                    retry(0);
                }
                return true;
            case ConnectionResult.SERVICE_UPDATING:
                Logger.log.geofenceError("Google Api Client "+availability.getErrorString(status), null);
                retry(SERVICE_RECONNECT_INTERVAL);
                return true;
            default:
                if (logged != status) {
                    logged = status;
                    Logger.log.geofenceError("Google Api Client "+availability.getErrorString(status), null);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        listener.onConnected(bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Listener shouldn't do anything. Google Play Services should reconnect automatically.
        listener.onConnectionSuspended(i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Logger.log.geofenceError("Could not connect to Google Services API: "
                +connectionResult.getErrorMessage()+" code: "+connectionResult.getErrorCode(), null);
    }
}
