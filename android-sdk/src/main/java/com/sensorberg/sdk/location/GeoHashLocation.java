package com.sensorberg.sdk.location;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import ch.hsr.geohash.GeoHash;
import lombok.Getter;

public class GeoHashLocation extends Location {

    /**
     * Geohash precision, array index is corresponding to character precision in reverse.
     */
    public static final float[] precision = {
            2.4F, 19, 76, 610, 2400, 20000, 78000, 630000, 2500000
    };

    /**
     * Note: This geohash has precision set according to equator-based location.
     * Depending on the latitude the Y-axis error might change.
     * Geohashes with precision based on equator-sized grid are more precise.
     * We'll send them to BE in that form because:
     * - we don't degrade accuracy even more by shortening the geohash
     * - frontend can visualize area with error at given latitude giving better final result
     */
    @Getter private String geohash;

    public GeoHashLocation(Location location) {
        super(location);
        for (int index = 0; index < precision.length; index++) {
            if (location.getAccuracy() <= precision[index]) {
                geohash = GeoHash.geoHashStringWithCharacterPrecision(
                        location.getLatitude(),
                        location.getLongitude(),
                        precision.length - index);
                return;
            }
        }
        geohash = GeoHash.geoHashStringWithCharacterPrecision(
                location.getLatitude(),
                location.getLongitude(),
                1);
    }

    // parcelable implementations
    public static final Parcelable.Creator<GeoHashLocation> CREATOR = new Parcelable.Creator<GeoHashLocation>() {
        @Override
        public GeoHashLocation createFromParcel(Parcel in) {
            Location location = Location.CREATOR.createFromParcel(in);
            return new GeoHashLocation(location);
        }

        @Override
        public GeoHashLocation[] newArray(int size) {
            return new GeoHashLocation[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
    }
}