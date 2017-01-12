package com.sensorberg.sdk.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.sensorberg.sdk.BuildConfig;
import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.settings.SettingsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class LocationSource extends BroadcastReceiver {

    private Context context;
    private LocationManager manager;
    private SettingsManager settings;
    private SharedPreferences prefs;
    private Gson gson;
    private PlayServiceManager play;
    private boolean enabled = false;

    private GeoHashLocation filteredLocation;
    private Location nonFilteredLocation;

    private List<LocationStateListener> stateListeners = new ArrayList<>();

    /**
     * Interface for listening to location state changes.
     * It corresponds to status bar location setting being on/off.
     */
    public interface LocationStateListener {
        void onLocationStateChanged(boolean enabled);
    }

    public LocationSource(Context context, LocationManager manager, SettingsManager settings,
                          Gson gson, SharedPreferences prefs, PlayServiceManager play) {
        this.context = context;
        this.manager = manager;
        this.settings = settings;
        this.gson = gson;
        this.prefs = prefs;
        this.play = play;
        enabled = isLocationEnabled(manager);
        nonFilteredLocation = restoreLastKnown();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
            if (isLocationEnabled(manager) != enabled) {
                enabled = !enabled;
                notifyStateListeners(enabled);
            }
        }
    }

    public void addListener(LocationStateListener listener) {
        for (LocationStateListener previous : stateListeners) {
            if(previous == listener) {
                return;
            }
        }
        if (stateListeners.size() == 0) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
            context.registerReceiver(this, filter);
            enabled = isLocationEnabled(manager);
        }
        stateListeners.add(listener);
    }

    public void removeListener(LocationStateListener listener) {
        Iterator<LocationStateListener> iterator = stateListeners.iterator();
        while (iterator.hasNext()) {
            LocationStateListener existing = iterator.next();
            if(existing == listener) {
                iterator.remove();
                if (stateListeners.size() == 0) {
                    context.unregisterReceiver(this);
                }
                return;
            }
        }
    }

    private void notifyStateListeners(boolean enabled) {
        for (LocationStateListener listener : stateListeners) {
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
        filteredLocation = chooseFreshest(acquire(true));
        if (filteredLocation != null) {
            return filteredLocation.getGeohash();
        }
        return null;
    }

    public void setLastKnownIfBetter(Location location, boolean backup) {
        if (nonFilteredLocation == null) {
            nonFilteredLocation = location;
            if (backup) {
                storeLastKnown(nonFilteredLocation);
            }
        }
    }

    public Location getLastKnownLocation() {
        //Priority of using fused location
        List<Location> locations = acquire(false);
        for (Location location : locations) {
            if ("fused".equals(location.getProvider())) {
                nonFilteredLocation = location;
                storeLastKnown(nonFilteredLocation);
                return nonFilteredLocation;
            }
        }
        nonFilteredLocation = chooseFreshest(locations);
        storeLastKnown(nonFilteredLocation);
        return nonFilteredLocation;
    }

    /**
     * Is location enabled, as per status bar indicator being on/off
     * @return location enabled/disabled
     */
    public static boolean isLocationEnabled(LocationManager manager) {
        for (String provider : manager.getProviders(true)) {
            if (LocationManager.GPS_PROVIDER.equals(provider)
                    || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                return true;
            }
        }
        return false;
    }

    private GeoHashLocation chooseFreshest(List<Location> locations) {
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

    /**
     * Try to get current location as passively as possible.
     * (by using getLastKnownLocation only)
     *
     * @return Most recent known location.
     */
    private List<Location> acquire(boolean filter) {
        List<Location> locations = new ArrayList<>(3);

        // re-evaluated current location
        // it might be still the best, or might be too old
        insertIfOk(filter ? filteredLocation : nonFilteredLocation, locations, filter);

        // loop through available providers and grab getLastKnownLocation from them
        List<String> providers = manager.getProviders(true);
        for (int i = 0, size = providers.size(); i < size; i++) {
            String provider = providers.get(i);
            try {
                Location location = manager.getLastKnownLocation(provider);
                // filter out bad locations
                insertIfOk(location, locations, filter);
            } catch (SecurityException ex) {
                // this exception should not happen because `getProviders(true);` shouldn't allow
                Logger.log.logError("Missing permission for " + provider + " provider", ex);
            }
        }

        // use also play services if available
        try {
            Location fused = LocationServices.FusedLocationApi.getLastLocation(play.getClient());
            insertIfOk(fused, locations, filter);
        } catch (SecurityException ex) {
            // this exception should not happen because `getProviders(true);` shouldn't allow
            Logger.log.logError("Missing permission for fused provider", ex);
        }

        return locations;
    }

    private void insertIfOk(Location location, List<Location> list, boolean filter) {
        if (location != null) {
            if (filter) {
                if (isAccurateFreshReal(location)) {
                    list.add(location);
                }
            } else {
                list.add(location);
            }
        }
    }

    public long getMaxLocationAge() {
        return settings.getGeohashMaxAge();
    }

    public int getGeohashMinAccuracyRadius() {
        return settings.getGeohashMinAccuracyRadius();
    }

    private boolean isAccurateFreshReal(Location location) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && !BuildConfig.DEBUG) {
            return !location.isFromMockProvider() &&
                    location.getAccuracy() < getGeohashMinAccuracyRadius() &&
                    (System.currentTimeMillis() - location.getTime()) < getMaxLocationAge();
        } else {
            return location.getAccuracy() < getGeohashMinAccuracyRadius() &&
                    (System.currentTimeMillis() - location.getTime()) < getMaxLocationAge();
        }
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

    private void storeLastKnown(Location location) {
        LocationStorage.save(gson, prefs, Constants.SharedPreferencesKeys.Location.LAST_KNOWN_LOCATION, location);
    }

    private Location restoreLastKnown() {
        return LocationStorage.load(gson, prefs, Constants.SharedPreferencesKeys.Location.LAST_KNOWN_LOCATION);
    }
}
