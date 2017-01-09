package com.sensorberg.sdk.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.settings.SettingsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class LocationHelper extends BroadcastReceiver {

    private Context context;
    private LocationManager manager;
    private SettingsManager settings;
    private boolean enabled = false;

    private GeoHashLocation location;

    private List<LocationStateListener> listeners = new ArrayList<>();

    /**
     * Interface for listening to location state changes.
     * It corresponds to status bar location setting being on/off.
     */
    public interface LocationStateListener {
        void onLocationStateChanged(boolean enabled);
    }

    public LocationHelper(Context context, LocationManager manager, SettingsManager settings) {
        this.context = context;
        this.manager = manager;
        this.settings = settings;
        enabled = isLocationEnabled();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
            if (isLocationEnabled() != enabled) {
                enabled = !enabled;
                notifyListeners(enabled);
            }
        }
    }

    public void addListener(LocationStateListener listener) {
        for (LocationStateListener previous : listeners) {
            if(previous == listener) {
                return;
            }
        }
        if (listeners.size() == 0) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
            context.registerReceiver(this, filter);
            enabled = isLocationEnabled();
        }
        listeners.add(listener);
    }

    public void removeListener(LocationStateListener listener) {
        Iterator<LocationStateListener> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            LocationStateListener existing = iterator.next();
            if(existing == listener) {
                iterator.remove();
                if (listeners.size() == 0) {
                    context.unregisterReceiver(this);
                }
                return;
            }
        }
    }

    private void notifyListeners(boolean enabled) {
        for (LocationStateListener listener : listeners) {
            listener.onLocationStateChanged(enabled);
        }
    }

    /**
     * Get most recent geohash fulfilling accuracy / age boundaries.
     * This methods works as passively as possible.
     *
     * @return Geohash if location is within given accuracy and age, null if not.
     * Also null if the location is not available or permissions are missing.
     */
    public String getGeohash() {
        location = acquireGeohash(true);
        if (location != null) {
            return location.getGeohash();
        }
        return null;
    }

    public GeoHashLocation getLastKnownLocation() {
        return acquireGeohash(false);
    }

    /**
     * Is location enabled, as per status bar indicator being on/off
     * @return location enabled/disabled
     */
    public boolean isLocationEnabled() {
        for (String provider : manager.getProviders(true)) {
            if (LocationManager.GPS_PROVIDER.equals(provider)
                    || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Try to get current location as passively as possible.
     * (by using getLastKnownLocation only)
     *
     * @return Most recent known location.
     */
    private GeoHashLocation acquireGeohash(boolean filter) {
        List<Location> locations = new ArrayList<>(3);

        // re-evaluated current location
        // it might be still the best, or might be too old
        if (filter) {
            if (isAccurateAndFreshAndNotNull(location)) {
                locations.add(location);
            }
        } else if (location != null) {
            locations.add(location);
        }

        // loop through available providers and grab getLastKnownLocation from them
        List<String> providers = manager.getProviders(true);
        for (int i = 0, size = providers.size(); i < size; i++) {
            String provider = providers.get(i);
            try {
                Location location = manager.getLastKnownLocation(provider);
                // filter out bad locations
                if (filter) {
                    if (isAccurateAndFreshAndNotNull(location)) {
                        locations.add(location);
                    }
                } else if (location != null) {
                    locations.add(location);
                }
            } catch (SecurityException ex) {
                // this exception should not happen because `getProviders(true);` shouldn't allow
                Logger.log.logError("Missing permission for " + provider + " provider", ex);
            }
        }

        // sort by time and grab the freshest location
        Location freshest = null;
        if (locations.size() > 1) {
            Collections.sort(locations, timeComparator);
        }
        if (locations.size() > 0) {
            freshest = locations.get(0);
        }

        if (freshest instanceof GeoHashLocation) {
            // if the previous location still the best, return it without creating a new object.
            return (GeoHashLocation) freshest;
        } else {
            return freshest == null ? null : new GeoHashLocation(freshest);
        }
    }

    public long getMaxLocationAge() {
        return settings.getGeohashMaxAge();
    }

    public int getGeohashMinAccuracyRadius() {
        return settings.getGeohashMinAccuracyRadius();
    }

    private boolean isAccurateAndFreshAndNotNull(Location location) {
        return location != null &&
                location.getAccuracy() < getGeohashMinAccuracyRadius() &&
                (System.currentTimeMillis() - location.getTime()) < getMaxLocationAge();
    }

    private final Comparator<Location> timeComparator = new Comparator<Location>() {
        @Override
        public int compare(Location t1, Location t2) {
            if (t1.getTime() > t2.getTime()) {
                return 1;
            }
            if (t2.getTime() > t1.getTime()) {
                return -1;
            }
            return 0;
        }
    };
}
