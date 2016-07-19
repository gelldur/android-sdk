package com.sensorberg.sdk.model.sugar;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.testUtils.NoClock;
import com.sensorbergorm.SugarContext;
import com.sensorbergorm.SugarRecord;
import com.sensorbergorm.query.Select;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import util.TestConstants;
import util.TestDatabaseHelper;

@RunWith(AndroidJUnit4.class)
public class TheSugarActionObjectShould {

    @Inject
    Gson gson;

    private BeaconAction tested;
    private UUID uuid = UUID.fromString("6133172D-935F-437F-B932-A901265C24B0");
    private Clock clock;

    @Before
    public void setUp() throws Exception {
        TestDatabaseHelper testDatabaseHelper = new TestDatabaseHelper(InstrumentationRegistry.getTargetContext(), "sensorbergORM.db");
        testDatabaseHelper.getWritableDatabase();
        SugarContext.init(InstrumentationRegistry.getTargetContext());

        BeaconAction.deleteAll(BeaconAction.class);
        BeaconEvent beaconEvent = new BeaconEvent.Builder()
                .withAction(new InAppAction(uuid, null, null, null, null, 0))
                .withPresentationTime(1337)
                .withTrigger(ScanEventType.ENTRY.getMask())
                .build();
        beaconEvent.setBeaconId(TestConstants.ANY_BEACON_ID);

        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        clock = NoClock.CLOCK;
        tested = BeaconAction.from(beaconEvent, clock);
    }

    @Test
    public void test_tested_object_should_not_be_null() {
        Assertions.assertThat(tested).isNotNull();
    }

    @Test
    public void test_should_be_json_serializeable() throws Exception {
        String objectAsJSON = gson.toJson(tested);

        Assertions.assertThat(objectAsJSON)
                .isNotEmpty()
                .isEqualToIgnoringCase("{\"eid\":\"6133172d-935f-437f-b932-a901265c24b0\",\"trigger\":1,\"pid\":\"192e463c9b8e4590a23fd32007299ef50133701337\",\"dt\":\"1970-01-01T01:00:01.337+01:00\"}");
    }

    @Test
    public void test_should_serialize_a_list_of_objects() throws Exception {
        tested.save();

        List<BeaconAction> objects = SugarRecord.find(BeaconAction.class, "");
        Select.from(BeaconAction.class).list();

        String objectsAsJson = gson.toJson(objects);

        Assertions.assertThat(objectsAsJson)
                .isNotEmpty()
                .isEqualToIgnoringCase("[{\"eid\":\"6133172d-935f-437f-b932-a901265c24b0\",\"trigger\":1,\"pid\":\"192e463c9b8e4590a23fd32007299ef50133701337\",\"dt\":\"1970-01-01T01:00:01.337+01:00\"}]");
        BeaconAction.deleteAll(BeaconAction.class);
    }
}
