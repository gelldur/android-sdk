package com.sensorberg.sdk.location;

import android.location.Location;
import android.location.LocationManager;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by ronaldo on 11/17/16.
 */
@RunWith(AndroidJUnit4.class)
public class LocationHelperTest {

    private LocationHelper tested;
    private LocationManager mockedManager;
    private List<String> PROVIDERS;
    private Location l0, l1;

    @Before
    public void setUp() throws Exception {

        PROVIDERS = Arrays.asList("provider.1", "provider.2");
        l0 = new Location(PROVIDERS.get(0));
        l1 = new Location(PROVIDERS.get(1));

        mockedManager = mock(LocationManager.class);
        when(mockedManager.getProviders(true)).thenReturn(PROVIDERS);
        when(mockedManager.getLastKnownLocation(PROVIDERS.get(0))).thenReturn(l0);
        when(mockedManager.getLastKnownLocation(PROVIDERS.get(1))).thenReturn(l1);
        tested = new LocationHelper(mockedManager);
    }

    @Test
    public void location_helper_return_freshest() throws Exception {

        long now = System.currentTimeMillis();

        l0.setAccuracy(LocationHelper.MIN_ACCURACY_RADIUS - 1);
        l0.setTime(now - 10);

        l1.setAccuracy(1);
        l1.setTime(now - 1000);

        String hash = tested.getGeohash();

        GeoHashLocation geoHashLocation = new GeoHashLocation(l0);
        assertEquals(hash, geoHashLocation.getGeohash());

    }

    @Test
    public void location_helper_return_null() throws Exception {

        l0.setAccuracy(1);
        l0.setTime(System.currentTimeMillis() - LocationHelper.MAX_LOCATION_AGE - 10);

        l1.setAccuracy(1);
        l1.setTime(System.currentTimeMillis() - LocationHelper.MAX_LOCATION_AGE - 10);

        String hash = tested.getGeohash();
        assertNull(hash);

    }

}