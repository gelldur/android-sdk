package com.sensorberg.sdk.scanner;

import android.content.SharedPreferences;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportHistoryCallback;
import com.sensorberg.sdk.model.persistence.ActionConversion;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.sdk.testUtils.TestHandlerManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

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
        tested = new BeaconActionHistoryPublisher(transport, testHandlerManager.getCustomClock(), testHandlerManager, sharedPreferences, gson);
        tested.deleteAllData();
        tested = Mockito.spy(tested);
    }


    @Test
    public void test_should_schedule_the_sending_of_one_the_unsent_objects() throws Exception {
        tested.onScanEventDetected(TestConstants.BEACON_SCAN_ENTRY_EVENT(100));
        tested.onActionPresented(TestConstants.BEACON_EVENT_IN_FUTURE);
        tested.onConversionUpdate(TestConstants.ACTION_CONVERSION(ActionConversion.TYPE_SUCCESS));
        tested.publishHistory();
        verify(transport).publishHistory(hasSize(1), hasSize(1), hasSize(1), any(TransportHistoryCallback.class));
    }

    @Test
    public void should_have_persisted_beacon_actions() throws Exception {
        //add one action in the future
        tested.onActionPresented(TestConstants.BEACON_EVENT_IN_FUTURE);

        tested.publishHistory();
        verify(transport).publishHistory(hasSize(0), hasSize(1), hasSize(0), any(TransportHistoryCallback.class));
        Mockito.reset(transport);

        // nullify and make new instance

        tested = null;
        tested = new BeaconActionHistoryPublisher(transport, testHandlerManager.getCustomClock(), testHandlerManager, sharedPreferences, gson);

        //Make sure the object returned is not null.
        assertThat(tested);

        //check that this instance read from local persistence layer
        tested.publishHistory();
        verify(transport).publishHistory(hasSize(0), hasSize(1), hasSize(0), any(TransportHistoryCallback.class));
    }

    @Test
    public void should_have_persisted_beacon_scans() throws Exception {
        //add one action in the future
        tested.onScanEventDetected(TestConstants.BEACON_SCAN_ENTRY_EVENT(100));

        tested.publishHistory();
        verify(transport).publishHistory(hasSize(1), hasSize(0), hasSize(0), any(TransportHistoryCallback.class));
        Mockito.reset(transport);

        // nullify and make new instance

        tested = null;
        tested = new BeaconActionHistoryPublisher(transport, testHandlerManager.getCustomClock(), testHandlerManager, sharedPreferences, gson);

        //Make sure the object returned is not null.
        assertThat(tested);

        //check that this instance read from local persistence layer
        tested.publishHistory();
        verify(transport).publishHistory(hasSize(1), hasSize(0), hasSize(0), any(TransportHistoryCallback.class));
    }

    @Test
    public void should_have_persisted_action_conversion() throws Exception {
        //add one action in the future
        tested.onConversionUpdate(TestConstants.ACTION_CONVERSION(ActionConversion.TYPE_SUCCESS));

        tested.publishHistory();
        verify(transport).publishHistory(hasSize(0), hasSize(0), hasSize(1), any(TransportHistoryCallback.class));
        Mockito.reset(transport);

        // nullify and make new instance

        tested = null;
        tested = new BeaconActionHistoryPublisher(transport, testHandlerManager.getCustomClock(), testHandlerManager, sharedPreferences, gson);

        //Make sure the object returned is not null.
        assertThat(tested);

        //check that this instance read from local persistence layer
        tested.publishHistory();
        verify(transport).publishHistory(hasSize(0), hasSize(0), hasSize(1), any(TransportHistoryCallback.class));
    }
}
