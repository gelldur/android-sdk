package com.sensorberg.sdk.model.sugar;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorbergorm.query.Select;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;

import util.TestConstants;

@RunWith(AndroidJUnit4.class)
public class TheSugarHistoryBodyShould {

    @Inject
    Gson gson;

    private HistoryBody tested;
    private BeaconScan scans;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        ScanEvent scanevent = new ScanEvent.Builder()
                .withEventMask(ScanEventType.ENTRY.getMask())
                .withBeaconId(TestConstants.ANY_BEACON_ID)
                .withEventTime(100)
                .build();
        Clock clock = new Clock() {
            @Override
            public long now() {
                return 1337;
            }

            @Override
            public long elapsedRealtime() {
                return 0;
            }
        };

        scans = BeaconScan.from(scanevent, clock.now());
        tested = new HistoryBody(Select.from(BeaconScan.class).list(), null, clock);
    }

    @Test
    public void test_should_be_serializeable() throws Exception {
        String asJSONString = gson.toJson(tested);

        Assertions.assertThat(asJSONString).isNotEmpty();
    }
}
