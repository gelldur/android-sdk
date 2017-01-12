package com.sensorberg.sdk.location;

import android.location.Location;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class GeofenceManagerTest implements GeofenceListener {

    @Inject protected GeofenceManager tested;

    @Before
    public void setUp() {
        tested.addListener(this);
    }

    @Test
    public void shoutThrowOnMoreThan100Fences() {
        List<String> fences = new ArrayList<>(100);
        for (int i = 0; i <= 100; i++) {
            fences.add("ccddeeff"+String.format("%06d", i));
        }
        try {
            tested.onFencesChanged(fences);
            fail("Should throw exception on more than 100 geofences");
        } catch (IllegalArgumentException ex) { }
    }

    @Test
    public void shouldReceiveGeofenceEventsAndNotifyListeners() {
        fail();
    }

    @Test
    public void shouldListenToSystemEvents() {
        fail();
    }

    public void shouldNotUpdateIfAlreadyUpdating () {
        fail();
    }

    @Override
    public void onGeofenceEvent(GeofenceData geofenceData, boolean entry) {
        fail();
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
