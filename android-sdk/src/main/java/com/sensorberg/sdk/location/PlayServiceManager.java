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
    private Runnable connect;

    @Getter private int status;

    private Context context;
    private GoogleApiClient client;
    private GoogleApiClient.ConnectionCallbacks listener;
    private GoogleApiAvailability availability;

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
        if (status == ConnectionResult.SUCCESS) {
            client.connect();
        } else {
            Logger.log.logError("Google Api Client status: "+status+" message: "+availability.getErrorString(status));
        }
        handler = new Handler(Looper.getMainLooper());
    }

    public GoogleApiClient getClient() {
        return client;
    }

    public boolean isConnected() {
        status = availability.isGooglePlayServicesAvailable(context);
        switch (status) {
            case ConnectionResult.SUCCESS:
                boolean connected = client.isConnected();
                if (!connected && !client.isConnecting()) {
                    retry(0);
                }
                return connected;
            case ConnectionResult.SERVICE_UPDATING:
                Logger.log.logError("Google Api Client "+availability.getErrorString(status));
                retry(SERVICE_RECONNECT_INTERVAL);
                return false;
            default:
                Logger.log.logError("Google Api Client "+availability.getErrorString(status));
                return false;
        }
    }

    private void retry(long delay) {
        //Retry only if not already retrying
        if (connect == null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    client.connect();
                    connect = null;
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
        Logger.log.logError("Could not connect to Google Services API: "
                +connectionResult.getErrorMessage()+" code: "+connectionResult.getErrorCode());
    }
}
