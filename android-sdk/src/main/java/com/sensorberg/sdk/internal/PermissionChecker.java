package com.sensorberg.sdk.internal;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

public class PermissionChecker {

    private final Context context;

    public PermissionChecker(Context context) {
        this.context = context;
    }

    public boolean hasVibratePermission() {
        return checkForPermission(Manifest.permission.VIBRATE);
    }

    public boolean hasReadSyncSettingsPermissions() {
        return checkForPermission(Manifest.permission.READ_SYNC_SETTINGS);
    }

    public boolean hasLocationPermission() {
        return checkForPermission(Manifest.permission.ACCESS_COARSE_LOCATION) || checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean checkForPermission(String permissionIdentifier){
        return context.checkCallingOrSelfPermission(permissionIdentifier)== PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Returns true if either both the device and sdk are 6 plus have location permission set or if the SDK
     * or device is < android 6/23.
     *
     * @return returnValue - true|false
     */
    public boolean hasScanPermissionCheckAndroid6() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasLocationPermission()) {
            return true;
        }
        return false;
    }
}
