package com.sensorberg.sdk.scanner;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportHistoryCallback;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.sdk.testUtils.TestHandlerManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static util.Verfier.hasSize;

@RunWith(AndroidJUnit4.class)
public class TheBeaconActionHistoryPublisherShould {

    @Inject
    TestHandlerManager testHandlerManager;

    @Inject
    @Named("dummyTransportSettingsManager")
    SettingsManager testSettingsManager;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    Gson gson;

    private BeaconActionHistoryPublisher tested;

    private Transport transport = mock(Transport.class);

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        testHandlerManager.getCustomClock().setNowInMillis(System.currentTimeMillis());
        tested = new BeaconActionHistoryPublisher(InstrumentationRegistry.getContext(), transport, testSettingsManager,
                testHandlerManager.getCustomClock(), testHandlerManager, sharedPreferences, gson);
        tested.deleteAllData();
        tested = Mockito.spy(tested);

        tested.onScanEventDetected(TestConstants.REGULAR_BEACON_SCAN_EVENT(100));
        tested.onActionPresented(TestConstants.BEACON_EVENT_IN_FUTURE);
    }

    @Test
    public void test_should_persist_scans_that_need_queing() throws Exception {
        List<BeaconScan> notSentObjects = tested.notSentBeaconScans();
        assertThat(notSentObjects).hasSize(1);
    }

    @Test
    public void test_should_persist_actions_that_need_queing() throws Exception {
        List<BeaconAction> notSentObjects = tested.notSentBeaconActions();
        assertThat(notSentObjects).hasSize(1);
    }

    @Test
    public void test_should_schedule_the_sending_of_one_the_unsent_objects() throws Exception {
        tested.publishHistory();
        verify(transport).publishHistory(hasSize(1), hasSize(1), any(TransportHistoryCallback.class));
    }
}
