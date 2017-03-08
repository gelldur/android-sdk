package com.sensorberg.utils;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class PlayServicesUtils {

    private static Boolean playServicesResult = null;

    public static boolean isGooglePlayServicesAvailable(Context context) {
        if (playServicesResult != null) {
            return playServicesResult;
        }

        boolean result;

        try {
            Class.forName("com.google.android.gms.common.GoogleApiAvailability");
            result = checkPlayServices(context);

            if (result) {
                Class.forName("com.google.android.gms.location.LocationServices");
            }

        } catch (Exception e) {
            result = false;
        }
        playServicesResult = result;
        return result;
    }

    private static boolean checkPlayServices(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }
}
