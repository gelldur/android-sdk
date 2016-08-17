package com.sensorberg.sdk.internal.http;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.URLFactory;
import com.sensorberg.sdk.internal.interfaces.BeaconHistoryUploadIntervalListener;
import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportHistoryCallback;
import com.sensorberg.sdk.internal.transport.interfaces.TransportSettingsCallback;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.settings.TimeConstants;
import com.sensorberg.sdk.testUtils.TestClock;

import junit.framework.Assert;

import org.fest.assertions.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.mock.Calls;
import util.TestConstants;
import util.Utils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(AndroidJUnit4.class)
public class TransportShould {

    private static final UUID BEACON_ID = UUID.fromString("192E463C-9B8E-4590-A23F-D32007299EF5");

    private static final int MAJOR = 1337;

    private static final int MINOR = 1337;

    @Inject
    @Named("testClock")
    TestClock clock;

    @Inject
    Gson gson;

    private Transport tested;

    private ScanEvent scanEvent;

    RetrofitApiServiceImpl mockRetrofitApiService;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        clock.setNowInMillis(new DateTime(2015, 7, 10, 1, 1, 1).getMillis());

        scanEvent = new ScanEvent.Builder()
                .withBeaconId(new BeaconId(BEACON_ID, MAJOR, MINOR))
                .withEntry(true)
                .withEventTime(clock.now())
                .build();

        mockRetrofitApiService = mock(RetrofitApiServiceImpl.class);
        tested = new RetrofitApiTransport(mockRetrofitApiService, clock);
        tested.setApiToken(TestConstants.API_TOKEN);
    }

    @Test
    public void test_should_forward_the_layout_upload_interval_to_the_settings() throws Exception {
        BeaconHistoryUploadIntervalListener mockListener = mock(BeaconHistoryUploadIntervalListener.class);
        tested.setBeaconHistoryUploadIntervalListener(mockListener);

        ResolveResponse resolveResponse = new ResolveResponse.Builder().withReportTrigger(1337).build();
        Mockito.when(mockRetrofitApiService.getBeacon(anyString(), anyString(), anyString()))
                .thenReturn(Calls.response(resolveResponse));

        tested.getBeacon(new ResolutionConfiguration(scanEvent), BeaconResponseHandler.NONE);
        Mockito.verify(mockListener).historyUploadIntervalChanged(1337L * 1000);
    }

    @Test
    public void test_failures() throws Exception {
        Call<SettingsResponse> exceptionResponse = Calls.failure(new UnsupportedEncodingException());
        Mockito.when(mockRetrofitApiService.getSettings()).thenReturn(exceptionResponse);

        tested.loadSettings(new TransportSettingsCallback() {
            @Override
            public void nothingChanged() {
                Assert.fail();
            }

            @Override
            public void onFailure(Exception e) {
                Assert.assertNotNull(e);
            }

            @Override
            public void onSettingsFound(SettingsResponse settings) {
                Assert.fail();
            }
        });
    }

    @Test
    public void test_a_beacon_request() throws Exception {
        ResolveResponse response = gson.fromJson(
                Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_response_005, InstrumentationRegistry.getContext()),
                ResolveResponse.class);
        Mockito.when(mockRetrofitApiService.getBeacon(anyString(), anyString(), anyString())).thenReturn(Calls.response(response));

        Assertions.assertThat(response).isNotNull();
        tested.getBeacon(new ResolutionConfiguration(scanEvent), new BeaconResponseHandler() {
            @Override
            public void onSuccess(List<BeaconEvent> foundBeaconEvents) {
                Assertions
                        .assertThat(foundBeaconEvents)
                        .overridingErrorMessage("There should be 1 action to the Beacon %s at %s there were %d",
                                scanEvent.getBeaconId().toTraditionalString(), URLFactory.getResolveURLString(), foundBeaconEvents.size())
                        .isNotNull()
                        .hasSize(1);
            }

            @Override
            public void onFailure(Throwable cause) {
                Assert.fail("there was a failure with this request");
            }
        });
    }

    @Test
    public void test_a_settings_request() {
        Mockito.when(mockRetrofitApiService.getSettings()).thenReturn(Calls.response(new SettingsResponse(0, new Settings())));

        tested.loadSettings(new TransportSettingsCallback() {
            @Override
            public void nothingChanged() {
                Assert.fail("there should be changes to no settings");
            }

            @Override
            public void onFailure(Exception e) {
                Assert.fail();
            }

            @Override
            public void onSettingsFound(SettingsResponse settings) {
                Assertions.assertThat(settings).isNotNull();
            }
        });
    }

    @Test
    public void test_publish_data_to_the_server() throws Exception {
        List<BeaconScan> scans = new ArrayList<>();
        List<BeaconAction> actions = new ArrayList<>();

        BeaconScan scan1 = new BeaconScan();
        scan1.setCreatedAt(System.currentTimeMillis() - TimeConstants.ONE_HOUR);
        scan1.setPid("test34563Pid");
        scan1.setCreatedAt(scan1.getCreatedAt());

        scans.add(scan1);

        Mockito.when(mockRetrofitApiService.publishHistory(anyString(), any(HistoryBody.class)))
                .thenReturn(Calls.response(new ResolveResponse.Builder().build()));

        tested.publishHistory(scans, actions, new TransportHistoryCallback() {
            @Override
            public void onFailure(Exception volleyError) {
                Assert.fail();
            }

            @Override
            public void onInstantActions(List<BeaconEvent> instantActions) {
                Assertions.assertThat(instantActions.size()).isEqualTo(0);
            }

            @Override
            public void onSuccess(List<BeaconScan> scans, List<BeaconAction> actions) {
                Assertions.assertThat(scans).isNotNull();
                Assertions.assertThat(scans.size()).isEqualTo(1);
            }
        });
    }

    @Test
    public void transport_settings_call_should_call_enqueue_with_retry() throws Exception {
        Mockito.when(mockRetrofitApiService.getSettings()).thenReturn(Calls.response(new SettingsResponse(0, new Settings())));
        TransportSettingsCallback transportSettingsCallback = mock(TransportSettingsCallback.class);
        RetrofitApiTransport spiedTransport = (RetrofitApiTransport) Mockito.spy(tested);

        spiedTransport.loadSettings(transportSettingsCallback);
        Mockito.verify(spiedTransport, times(1)).enqueueWithRetry(any(Call.class), any(retrofit2.Callback.class));
    }
}