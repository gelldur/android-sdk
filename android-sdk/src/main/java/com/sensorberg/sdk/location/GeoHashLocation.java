package com.sensorberg.sdk.location;

import android.location.Location;

import com.sensorberg.sdk.settings.TimeConstants;

public class GeoHashLocation extends Location {

    public static final float MIN_ACCURACY_RADIUS = 25.0F;
    public static final long MAX_LOCATION_AGE = TimeConstants.ONE_MINUTE;

    private String geohash;
    private boolean valid = false;

    public GeoHashLocation(Location location) {
        super(location);
        if (isAccurateAndFresh()) {
            geohash = GeoHash.encode(location.getLatitude(), location.getLongitude(), 9).toHashString();
            valid = true;
        }
    }

    public String getValidGeohash() {
        return isValid() ? geohash : null;
    }

    public boolean isValid() {
        if (!valid) {
            return false;
        }
        if (isAccurateAndFresh()) {
            valid = true;
        } else {
            valid = false;
        }
        return valid;
    }

    private boolean isAccurateAndFresh() {
        return getAccuracy() < MIN_ACCURACY_RADIUS && (System.currentTimeMillis() - getTime()) < MAX_LOCATION_AGE;
    }
}
