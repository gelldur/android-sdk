package com.sensorberg.sdk.location;

import android.location.Location;
import android.location.LocationManager;

import com.sensorberg.sdk.internal.PermissionChecker;

import lombok.Getter;

public abstract class LocationHelper {

    private LocationManager manager;
    private PermissionChecker checker;

    private GeoHashLocation location;

    @Getter private String provider;

    public LocationHelper(LocationManager manager, PermissionChecker checker) {
        this.manager = manager;
        this.checker = checker;
        provider = determineProviderStartup(checker);
        //TODO listen for provider availability changes (optional)
        //TODO provider not existing case - list all providers, choose from them.
        //TODO cache provider/permissions for api >= 23 (?)
        //TODO tests for geocache
    }

    /**
     * Get most recent geohash fulfilling accuracy / age boundaries.
     * This methods works as passively as possible.
     * @return Geohash if location is within given accuracy and age, null if not.
     * Also null if the location is not available or permissions are missing.
     */
    public String getGeohash() {
        provider = determineProviderRuntime(checker);
        if (provider == null) {
            return null;
        }
        try {
            if (manager.isProviderEnabled(provider)) {
                Location fresh = manager.getLastKnownLocation(provider);
                location = wrap(fresh);
                if (location != null) {
                    return location.getValidGeohash();
                }
            }
        } catch (SecurityException | IllegalArgumentException ex) {
            //TODO better logging.
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Runs in constructor to determine provider at startup.
     * @param checker PermissionChecker used by abstract super class.
     * @return Provider string as per LocationManager constants or null if permissions missing.
     */
    protected abstract String determineProviderStartup(PermissionChecker checker);

    /**
     * Runs on every geohash request to determine provider.
     * @param checker PermissionChecker used by abstract super class.
     * @return Provider string as per LocationManager constants or null if permissions missing.
     */
    protected abstract String determineProviderRuntime(PermissionChecker checker);

    private GeoHashLocation wrap(Location fresh) {
        if (fresh != null && !fresh.equals(location)) {
            return new GeoHashLocation(fresh);
        } else {
            return location;
        }
    }
}
