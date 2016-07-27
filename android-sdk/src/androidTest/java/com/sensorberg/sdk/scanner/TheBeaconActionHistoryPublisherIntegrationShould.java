package com.sensorberg.sdk.scanner;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.settings.SettingsManager;

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

import retrofit2.mock.Calls;
import util.TestConstants;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class TheBeaconActionHistoryPublisherIntegrationShould {

    @Inject
    @Named("testHandlerWithCustomClock")
    HandlerManager testHandlerManager;

    @Inject
    @Named("realClock")
    Clock clock;

    @Inject
    @Named("dummyTransportSettingsManager")
    SettingsManager testSettingsManager;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    Gson gson;

    private RetrofitApiServiceImpl mockRetrofitApiService = mock(RetrofitApiServiceImpl.class);

    private RetrofitApiTransport testTransportWithMockService;

    private BeaconActionHistoryPublisher tested;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        testTransportWithMockService = new RetrofitApiTransport(mockRetrofitApiService, clock);
        tested = new BeaconActionHistoryPublisher(InstrumentationRegistry.getContext(), testTransportWithMockService, testSettingsManager, clock,
                testHandlerManager, sharedPreferences, gson);
    }

    @Test
    public void test_should_send_history_to_the_server() throws Exception {
        Mockito.when(mockRetrofitApiService.publishHistory(Mockito.anyString(), Mockito.any(HistoryBody.class)))
                .thenReturn(Calls.response(new ResolveResponse.Builder().build()));

        tested.onScanEventDetected(TestConstants.BEACON_SCAN_ENTRY_EVENT(100));
        tested.publishHistory();

        verify(mockRetrofitApiService, times(1)).publishHistory(Mockito.anyString(), Mockito.any(HistoryBody.class));
    }

    @Test
    public void should_have_persisted_beacon_actions() throws Exception {
        //first we delete all previous data
        tested.deleteAllData();

        //add one action in the future
        tested.onActionPresented(TestConstants.BEACON_EVENT_IN_FUTURE);
        List<BeaconAction> notSentObjects = tested.notSentBeaconActions();
        assertThat(notSentObjects).hasSize(1);

        //persist it, nullify and make new instance
        tested.saveAllData();
        tested = null;
        tested = new BeaconActionHistoryPublisher(InstrumentationRegistry.getContext(), testTransportWithMockService, testSettingsManager, clock,
                testHandlerManager, sharedPreferences, gson);

        //check that this instance read from local persistence layer
        List<BeaconAction> notSentObjectsFromPersistence = tested.notSentBeaconActions();
        assertThat(notSentObjectsFromPersistence).hasSize(1);
    }

    @Test
    public void should_have_persisted_beacon_scans() throws Exception {
        //first we delete all previous data
        tested.deleteAllData();

        //add one scan
        tested.onScanEventDetected(TestConstants.BEACON_SCAN_ENTRY_EVENT(100));
        List<BeaconScan> notSentObjects = tested.notSentBeaconScans();
        assertThat(notSentObjects).hasSize(1);

        //persist it, nullify and make new instance
        tested.saveAllData();
        tested = null;
        tested = new BeaconActionHistoryPublisher(InstrumentationRegistry.getContext(), testTransportWithMockService, testSettingsManager, clock,
                testHandlerManager, sharedPreferences, gson);

        //check that this instance read from local persistence layer
        List<BeaconScan> notSentObjectsFromPersistence = tested.notSentBeaconScans();
        assertThat(notSentObjectsFromPersistence).hasSize(1);
    }
}
