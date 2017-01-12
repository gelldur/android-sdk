package com.sensorberg.sdk.location;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationResult;
import com.sensorberg.sdk.BuildConfig;
import com.sensorberg.sdk.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GeofenceReceiver extends BroadcastReceiver {

    public static final String ACTION_GEOFENCE = "com.sensorberg.sdk.receiver.GEOFENCE";
    public static final String ACTION_LOCATION = "com.sensorberg.sdk.receiver.LOCATION";

    private Context context;
    private GeofenceManager manager;

    private List<GeofenceListener> listeners = new ArrayList<>();

    public GeofenceReceiver(Context context, GeofenceManager manager) {
        this.context = context;
        this.manager = manager;
        registerReceiver();
    }

    public static PendingIntent getGeofencePendingIntent(Context context) {
        Intent intent = new Intent(GeofenceReceiver.ACTION_GEOFENCE);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getLocationPendingIntent(Context context) {
        Intent intent = new Intent(GeofenceReceiver.ACTION_LOCATION);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null || action.isEmpty()) {
            Logger.log.geofenceError("Received intent without action",null);
            return;
        }
        switch (action) {
            case ACTION_GEOFENCE:
                handleGeofence(intent);
                break;
            case ACTION_LOCATION:
                handleLocation(intent);
                break;
            default:
                Logger.log.geofenceError("Received intent with unknown action",null);
                break;
        }
    }

    private void handleLocation(Intent intent) {
        if (!LocationResult.hasResult(intent)) {
            if (LocationAvailability.hasLocationAvailability(intent)) {
                //Not needed yet.
                //LocationAvailability availability = LocationAvailability.extractLocationAvailability(intent);
                //Logger.log.geofence("Received LocationAvailability: " + availability.isLocationAvailable());
            }
            return;
        }
        LocationResult result = LocationResult.extractResult(intent);
        Location location = result.getLastLocation();
        for (GeofenceListener listener : listeners) {
            listener.onLocationChanged(location);
        }
    }

    private void handleGeofence(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event == null) {
            Logger.log.geofenceError("GeofencingEvent is null", null);
            return;
        }
        if (event.hasError() && event.getErrorCode() == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
            //This runs in a case of e.g. disabling location on the device.
            //If we've registered geofence before, Google Play Service lets us know about removal here.
            //(But we don't rely only on it, cause we're smart and listen to disabling location anyway)
            manager.setRegistered(false);
            return;
        }
        try {
            List<GeofenceData> geofenceDatas = GeofenceData.from(event);
            boolean entry = event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER;
            for (GeofenceData geofenceData : geofenceDatas) {
                Logger.log.geofence("Received "+ (entry ? "entry" : "exit") +
                        " event "+geofenceData.getGeohash() + ", radius "+geofenceData.getRadius());
                Logger.log.geofence("Debug build: "+BuildConfig.DEBUG+" Mock: "+geofenceData.isMock());
                if (!BuildConfig.DEBUG && geofenceData.isMock()) {
                    Logger.log.geofenceError("Geofence from mock location on non-debug build, ignoring", null);
                } else {
                    for (GeofenceListener listener : listeners) {
                        listener.onGeofenceEvent(geofenceData, entry);
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            Logger.log.geofenceError("GeofencingEvent is invalid", ex);
        }
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
        filter.addAction(ACTION_GEOFENCE);
        filter.addAction(ACTION_LOCATION);
        context.registerReceiver(this, filter);
    }
}
