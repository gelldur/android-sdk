package com.sensorberg.sdk.location;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import ch.hsr.geohash.GeoHash;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GeoHashTest {
    @Test
    public void invalidGeohashDecodeTests() {
        try {
            //Can't have letter 'a'
            GeoHash.fromGeohashString("azs42");
            fail("Invalid geohash, exception should be thrown");
        } catch (NullPointerException ex) { }
        try {
            //Can't have letter 'i'
            GeoHash.fromGeohashString("ezs4i");
            fail("Invalid geohash, exception should be thrown");
        } catch (NullPointerException ex) { }
        try {
            //Can't have letter 'l'
            GeoHash.fromGeohashString("ezl42");
            fail("Invalid geohash, exception should be thrown");
        } catch (NullPointerException ex) { }
        try {
            //Can't have letter 'o'
            GeoHash.fromGeohashString("ezo42");
            fail("Invalid geohash, exception should be thrown");
        } catch (NullPointerException ex) { }
        try {
            //Can't have capital letters
            GeoHash.fromGeohashString("Ezs42");
            fail("Invalid geohash, exception should be thrown");
        } catch (NullPointerException ex) { }
    }

    @Test
    public void validGeohashDecodeTests() {
        try {
            //Point far north, expected value is from geohash.org
            assertGeohash(GeoHash.fromGeohashString("s252w7m54gv4"), 0.106279, 16.101962, 0.0000005);
            assertGeohash(GeoHash.fromGeohashString("s252w7m5"), 0.106, 16.102, 0.0005);
            assertGeohash(GeoHash.fromGeohashString("s252w7"), 0.11, 16.1, 0.005);
            //Point close to equator, expected value is from geohash.org
            assertGeohash(GeoHash.fromGeohashString("gnzgf2b96hv6"), 83.632290, -34.001906, 0.0000005);
            assertGeohash(GeoHash.fromGeohashString("gnzgf2b9"), 83.632, -34.002, 0.0005);
            assertGeohash(GeoHash.fromGeohashString("gnzgf2"), 83.63, -34.0, 0.005);
        } catch (NullPointerException ex) {
            fail("Valid geohashes, exception should NOT be thrown");
        }
    }

    @Test
    public void validGeohashEncodeTests() {
        try {
            //Point far north, expected value is from geohash.org
            assertGeohash(GeoHash.withCharacterPrecision(0.106279, 16.101962, 12), "s252w7m54gv4");
            assertGeohash(GeoHash.withCharacterPrecision(0.106, 16.102, 8), "s252w7m1");
            assertGeohash(GeoHash.withCharacterPrecision(0.11, 16.1, 6), "s252wk");
            //Point close to equator, expected value is from geohash.org
            assertGeohash(GeoHash.withCharacterPrecision(83.632290, -34.001906, 12), "gnzgf2b96hv6");
            assertGeohash(GeoHash.withCharacterPrecision(83.632, -34.002, 8), "gnzgf28r");
            assertGeohash(GeoHash.withCharacterPrecision(83.63, -34.0, 6), "gnzgf2");
        } catch (NullPointerException ex) {
            fail("Valid geohashes, exception should NOT be thrown");
        }
    }

    public void assertGeohash(GeoHash geoHash, double latitude, double longitude, double delta) {
        assertEquals(geoHash.getPoint().getLatitude(), latitude, delta);
        assertEquals(geoHash.getPoint().getLongitude(), longitude, delta);
    }

    public void assertGeohash(GeoHash geoHash, String hash) {
        assertEquals(geoHash.toBase32(), hash);
    }
}
