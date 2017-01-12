package com.sensorberg.sdk.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.location.Geofence;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class GeofenceStorageTest {

    @Inject Context context;
    SharedPreferences preferences;

    private GeofenceStorage tested;

    @Before
    public void setUp() {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        preferences = InstrumentationRegistry.getContext().getSharedPreferences(
                Long.toString(System.currentTimeMillis()),
                Context.MODE_PRIVATE);
        tested = new GeofenceStorage(context, preferences);
    }

    /*@Test
    public void shouldWriteAndRestore() {
        List<String> fences = new ArrayList<>(2);
        fences.add("bbccddee001122");
        fences.add("ccddeeff002233");
        tested.onFencesChanged(fences);
        tested = null;

        tested = new GeofenceStorage(context, preferences);
        List<String> geofences = tested.getGeofencesKeys();
        assertEquals(fences.size(), geofences.size());
        assertTrue(geofences.contains(fences.get(0)));
        assertTrue(geofences.contains(fences.get(1)));

        List<Geofence> google = tested.getGeofences();
        assertEquals(fences.size(), google.size());
        assertTrue(fences.contains(google.get(0).getRequestId()));
        assertTrue(fences.contains(google.get(1).getRequestId()));
    }

    @Test
    public void shouldNotWriteBadGeofence() {
        List<String> fences = new ArrayList<>(2);
        fences.add("bbccddee001122");   //good
        fences.add("bbccddeef001122");
        fences.add("ccddeeff002233");   //good
        fences.add("ccddeeff0022333");
        fences.add(null);
        fences.add("");
        fences.add("aAsdve060341cadasdsd23091230");
        fences.add("ccddeeffd02233");

        tested.onFencesChanged(fences);
        tested = null;

        tested = new GeofenceStorage(context, preferences);
        List<String> geofences = tested.getGeofencesKeys();
        assertEquals(2, geofences.size());
        assertTrue(geofences.contains(fences.get(0)));
        assertTrue(geofences.contains(fences.get(2)));

        List<Geofence> google = tested.getGeofences();
        assertEquals(2, google.size());
        assertEquals(google.get(0).getRequestId(), "bbccddee001122");
        assertEquals(google.get(1).getRequestId(), "ccddeeff002233");
    }

    @Test
    public void shouldEmptyStorage() {
        //Write some fences
        List<String> fences = new ArrayList<>(2);
        fences.add("bbccddee001122");   //good
        fences.add("ccddeeff002233");   //good
        tested.onFencesChanged(fences);

        //Make sure they're written
        tested = null;
        tested = new GeofenceStorage(context, preferences);
        assertEquals(2, tested.getGeofences().size());
        assertEquals(2, tested.getGeofencesKeys().size());

        //Remove them
        fences.clear();
        tested.onFencesChanged(fences);

        //Make sure they're removed
        tested = null;
        tested = new GeofenceStorage(context, preferences);
        assertEquals(0, tested.getGeofences().size());
        assertEquals(0, tested.getGeofencesKeys().size());
    }

    @Test
    public void shouldNotOverwriteWithDiffrentRadius() {
        //Write some fences
        List<String> fences = new ArrayList<>(2);
        fences.add("bbccddee001122");
        fences.add("bbccddee001123");
        tested.onFencesChanged(fences);

        //Make sure they're written
        tested = null;
        tested = new GeofenceStorage(context, preferences);

        List<String> geofences = tested.getGeofencesKeys();
        assertEquals(2, geofences.size());
        assertTrue(geofences.contains(fences.get(0)));
        assertTrue(geofences.contains(fences.get(1)));

        List<Geofence> google = tested.getGeofences();
        assertEquals(2, google.size());
        assertEquals(google.get(0).getRequestId(), "bbccddee001123");
        assertEquals(google.get(1).getRequestId(), "bbccddee001122");
    }*/
}
