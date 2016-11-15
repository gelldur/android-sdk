package com.sensorberg.sdk.location;

import android.location.Location;
import android.location.LocationManager;

import com.sensorberg.sdk.Logger;
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
    }

    /**
     * Get most recent geohash fulfilling accuracy / age boundaries.
     * This methods works as passively as possible.
     * @return Geohash if location is within given accuracy and age, null if not.
     * Also null if the location is not available or permissions are missing.
     */
    public String getGeohash() {
        location = acquireGeohash();
        if(location != null) {
            return location.getValidGeohash();
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

    /**
     * Try to get current location as passively as possible.
     * (by using getLastKnownLocation only)
     * @return Most recent known location.
     */
    private GeoHashLocation acquireGeohash() {
        provider = determineProviderRuntime(checker);
        if (provider == null) {
            return location;
        }
        try {
            if (!manager.isProviderEnabled(provider)) {
                return location;
            }
            Location fresh = manager.getLastKnownLocation(provider);
            if (fresh == null) {
                return location;
            }
            if (location == null || location.isDifferent(fresh)) {
                return new GeoHashLocation(fresh);
            }
        } catch (SecurityException ex) {
            //This should not happen since we're always checking for permission.
            Logger.log.logError("Missing permission for "+provider+" provider", ex);
        } catch (IllegalArgumentException ex) {
            //This should not happen since there are probably no devices without network or passive provider.
            Logger.log.logError("Provider "+provider+" is missing on this device", ex);
        }
        return location;
    }
}
