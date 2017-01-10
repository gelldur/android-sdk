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
import com.sensorberg.sdk.settings.SettingsManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.content.SharedPreferences;
import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.mock.Calls;
import util.TestConstants;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class TheBeaconActionHistoryPublisherIntegrationShould {

    @Inject
    @Named("testHandlerWithCustomClock")
    HandlerManager testHandlerManager;

    @Inject
    Clock clock;

    @Inject
    @Named("dummyTransportSettingsManager")
    SettingsManager testSettingsManager;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    Gson gson;

    private RetrofitApiServiceImpl mockRetrofitApiService = mock(RetrofitApiServiceImpl.class);

    private Transport transport = mock(Transport.class);

    private BeaconActionHistoryPublisher tested;

    private RetrofitApiTransport testTransportWithMockService;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        testTransportWithMockService = new RetrofitApiTransport(mockRetrofitApiService, clock, sharedPreferences, gson);
        tested = new BeaconActionHistoryPublisher(testTransportWithMockService, clock,
                testHandlerManager, sharedPreferences, gson);
    }

    @Test
    public void test_should_send_history_to_the_server() throws Exception {
        Mockito.when(mockRetrofitApiService.publishHistory(Mockito.any(HistoryBody.class)))
                .thenReturn(Calls.response(PUBLISH_HISTORY_RESPONSE));

        tested.onScanEventDetected(TestConstants.BEACON_SCAN_ENTRY_EVENT(100));
        tested.publishHistory();

        verify(mockRetrofitApiService, times(1)).publishHistory(Mockito.any(HistoryBody.class));
    }

    @Test
    public void test_should_send_no_history_to_the_server_when_nothing_happend() throws Exception {
        Mockito.when(mockRetrofitApiService.publishHistory(Mockito.any(HistoryBody.class)))
                .thenReturn(Calls.response(PUBLISH_HISTORY_RESPONSE));

        tested.publishHistory();

        verify(mockRetrofitApiService, never()).publishHistory(Mockito.any(HistoryBody.class));
    }

    private static final ResponseBody PUBLISH_HISTORY_RESPONSE = ResponseBody.create(MediaType.parse("application/json"), "");

}
