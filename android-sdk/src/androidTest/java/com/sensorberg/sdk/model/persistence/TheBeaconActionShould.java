package com.sensorberg.sdk.model.persistence;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.testUtils.NoClock;

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
import java.util.UUID;

import javax.inject.Inject;

import util.TestConstants;

@RunWith(AndroidJUnit4.class)
public class TheBeaconActionShould {

    @Inject
    Gson gson;

    private BeaconAction tested;

    private UUID uuid = UUID.fromString("6133172D-935F-437F-B932-A901265C24B0");


    @Before
    public void setUp() throws Exception {
        BeaconEvent beaconEvent = new BeaconEvent.Builder()
                .withAction(new InAppAction(uuid, null, null, null, null, 0, UUID.randomUUID().toString()))
                .withPresentationTime(1337)
                .withTrigger(ScanEventType.ENTRY.getMask())
                .build();
        beaconEvent.setBeaconId(TestConstants.ANY_BEACON_ID);

        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        tested = BeaconAction.from(beaconEvent);
    }

    @Test
    public void test_tested_object_should_not_be_null() {
        Assertions.assertThat(tested).isNotNull();
    }

    @Test
    public void test_should_be_json_serializeable() throws Exception {
        String objectAsJSON = gson.toJson(tested);
        BeaconAction deserializedObject = gson.fromJson(objectAsJSON, BeaconAction.class);

        Assertions.assertThat(objectAsJSON).isNotEmpty();
        Assertions.assertThat(deserializedObject).isEqualTo(tested);
    }

    @Test
    public void test_should_serialize_a_list_of_objects() throws Exception {
        List<BeaconAction> objects = new ArrayList<>(Arrays.asList(tested));
        String objectsAsJson = gson.toJson(objects);

        Type listType = new TypeToken<Set<BeaconAction>>() {
        }.getType();
        Set<BeaconAction> beaconActions = gson.fromJson(objectsAsJson, listType);

        Assertions.assertThat(beaconActions)
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(beaconActions.toArray()[0]).isEqualTo(tested);
    }
}
