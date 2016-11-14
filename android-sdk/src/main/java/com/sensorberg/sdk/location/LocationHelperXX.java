package com.sensorberg.sdk.location;

import android.Manifest;
import android.location.LocationManager;

import com.sensorberg.sdk.internal.PermissionChecker;

public class LocationHelperXX extends LocationHelper {

    public LocationHelperXX(LocationManager locationManager, PermissionChecker permissionChecker) {
        super(locationManager, permissionChecker);
    }

    @Override
    protected String determineProviderRuntime(PermissionChecker checker) {
        return getProvider();
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
