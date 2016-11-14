package com.sensorberg.sdk.location;

import android.location.Location;

public class GeoHashLocation extends Location {

    private GeoHash geohash;

    public GeoHashLocation(String provider) {
        super(provider);
    }

    public GeoHashLocation(Location location) {
        super(location);
    }
}
