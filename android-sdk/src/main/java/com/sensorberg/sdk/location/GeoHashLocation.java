package com.sensorberg.sdk.location;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import lombok.Getter;

public class GeoHashLocation extends Location {

    @Getter private String geohash;

    public GeoHashLocation(Location location) {
        super(location);
        geohash = GeoHash.encode(location.getLatitude(), location.getLongitude(), 9).toHashString();
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