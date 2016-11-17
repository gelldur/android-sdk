package com.sensorberg.sdk.location;

import android.location.Location;
import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Created by ronaldo on 11/17/16.
 */
@RunWith(AndroidJUnit4.class)
public class GeoHashLocationTest {

    @Test
    public void parcelable_test() throws Exception {

        String provider = "some_provider";
        Location l = new Location(provider);
        l.setLatitude(1);
        l.setLongitude(2);
        l.setTime(System.currentTimeMillis());
        l.setAccuracy(4);

        GeoHashLocation tested = new GeoHashLocation(l);
        Parcel p = Parcel.obtain();
        tested.writeToParcel(p, 0);
        p.setDataPosition(0);
        GeoHashLocation generated = GeoHashLocation.CREATOR.createFromParcel(p);

        assertEquals(generated.getGeohash(), tested.getGeohash());
        assertEquals(generated.getLatitude(), tested.getLatitude(), 0.01);
        assertEquals(generated.getLongitude(), tested.getLongitude(), 0.01);
        assertEquals(generated.getTime(), tested.getTime());
        assertEquals(generated.getAccuracy(), tested.getAccuracy(), 0.01);

    }

}