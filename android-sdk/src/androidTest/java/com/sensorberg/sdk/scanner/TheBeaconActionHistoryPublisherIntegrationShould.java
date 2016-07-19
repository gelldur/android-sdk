package com.sensorberg.sdk.scanner;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.settings.SettingsManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.mock.Calls;
import util.TestConstants;

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

    RetrofitApiServiceImpl mockRetrofitApiService = mock(RetrofitApiServiceImpl.class);

    private ScanEvent SCAN_EVENT = new ScanEvent.Builder()
            .withEventMask(ScanEventType.ENTRY.getMask())
            .withBeaconId(TestConstants.ANY_BEACON_ID)
            .withEventTime(100)
            .build();

    private BeaconActionHistoryPublisher tested;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        Transport testTransportWithMockService = new RetrofitApiTransport(mockRetrofitApiService, clock);
        tested = new BeaconActionHistoryPublisher(InstrumentationRegistry.getContext(), testTransportWithMockService, testSettingsManager, clock,
                testHandlerManager, sharedPreferences, gson);
    }

    @Test
    public void test_should_send_history_to_the_server() throws Exception {
        Mockito.when(mockRetrofitApiService.publishHistory(Mockito.anyString(), Mockito.any(HistoryBody.class)))
                .thenReturn(Calls.response(new ResolveResponse.Builder().build()));

        tested.onScanEventDetected(SCAN_EVENT);
        tested.publishHistory();

        verify(mockRetrofitApiService, times(1)).publishHistory(Mockito.anyString(), Mockito.any(HistoryBody.class));
    }
}
