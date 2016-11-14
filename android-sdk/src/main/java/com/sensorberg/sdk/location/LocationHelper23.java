package com.sensorberg.sdk.location;

import android.Manifest;
import android.location.LocationManager;

import com.sensorberg.sdk.internal.PermissionChecker;

public class LocationHelper23 extends LocationHelper {

    public LocationHelper23(LocationManager locationManager, PermissionChecker permissionChecker) {
        super(locationManager, permissionChecker);
    }

    @Override
    protected String determineProviderRuntime(PermissionChecker checker) {
        return determineProviderStartup(checker);
    }

    @Override
    protected String determineProviderStartup(PermissionChecker checker) {
        if (checker.checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            return LocationManager.PASSIVE_PROVIDER;
        } else if (checker.checkForPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return LocationManager.NETWORK_PROVIDER;
        } else {
            return null;
        }
    }
}
