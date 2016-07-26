package com.sensorberg.sdk.model.persistence;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.scanner.ScanEvent;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import util.TestConstants;

@RunWith(AndroidJUnit4.class)
public class TheBeaconScanShould {

    @Inject
    Gson gson;

    private BeaconScan tested;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        ScanEvent scanevent = new ScanEvent.Builder()
                .withEntry(true)
                .withBeaconId(TestConstants.ANY_BEACON_ID)
                .withEventTime(100)
                .build();
        tested = BeaconScan.from(scanevent);
    }

    @Test
    public void test_should_generate_a_bid() throws Exception {
        Assertions.assertThat(tested.getPid()).isEqualToIgnoringCase("192E463C9B8E4590A23FD32007299EF50133701337");
    }

    @Test
    public void test_should_be_json_serializeable() throws Exception {
        String objectAsJSON = gson.toJson(tested);
        BeaconScan deserializedObject = gson.fromJson(objectAsJSON, BeaconScan.class);

        Assertions.assertThat(objectAsJSON).isNotEmpty();
        Assertions.assertThat(deserializedObject).isEqualTo(tested);
    }

    @Test
    public void test_should_serialize_a_list_of_objects() throws Exception {
        List<BeaconScan> objects = new ArrayList<>(Arrays.asList(tested));
        String objectsAsJson = gson.toJson(objects);

        Type listType = new TypeToken<Set<BeaconScan>>() {
        }.getType();
        Set<BeaconScan> beaconScans = gson.fromJson(objectsAsJson, listType);

        Assertions.assertThat(beaconScans)
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(beaconScans.toArray()[0]).isEqualTo(tested);
    }
}
