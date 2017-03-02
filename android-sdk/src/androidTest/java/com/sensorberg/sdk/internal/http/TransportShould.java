package com.sensorberg.sdk.internal.http;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.BeaconHistoryUploadIntervalListener;
import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportHistoryCallback;
import com.sensorberg.sdk.internal.transport.interfaces.TransportSettingsCallback;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;
import com.sensorberg.sdk.model.persistence.ActionConversion;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.settings.TimeConstants;
import com.sensorberg.sdk.testUtils.TestClock;

import junit.framework.Assert;

import org.fest.assertions.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;

import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
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

    @Inject
    Gson gson;

    @Inject
    SharedPreferences prefs;

    @Inject
    TestClock clock;

    private Transport tested;

    RetrofitApiServiceImpl mockRetrofitApiService;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        clock.setNowInMillis(new DateTime(2015, 7, 10, 1, 1, 1).getMillis());

        mockRetrofitApiService = mock(RetrofitApiServiceImpl.class);
        tested = new RetrofitApiTransport(mockRetrofitApiService, clock, prefs, gson);
        tested.setApiToken(TestConstants.API_TOKEN);
    }

    @Test
    public void test_should_forward_the_layout_upload_interval_to_the_settings() throws Exception {
        BeaconHistoryUploadIntervalListener mockListener = mock(BeaconHistoryUploadIntervalListener.class);
        tested.setBeaconHistoryUploadIntervalListener(mockListener);

        ResolveResponse resolveResponse = new ResolveResponse.Builder().withReportTrigger(1337).build();
        Mockito.when(mockRetrofitApiService.getBeacon(anyString(), anyString(), Matchers.<TreeMap<String, String>>any()))
                .thenReturn(Calls.response(resolveResponse));

        tested.getBeacon(TestConstants.BEACON_SCAN_ENTRY_EVENT(clock.now()), null, BeaconResponseHandler.NONE);
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
        Mockito.when(mockRetrofitApiService.getBeacon(anyString(), anyString(), Matchers.<TreeMap<String, String>>any())).thenReturn(Calls.response(response));

        Assertions.assertThat(response).isNotNull();
        tested.getBeacon(TestConstants.BEACON_SCAN_ENTRY_EVENT(clock.now()), null, new BeaconResponseHandler() {
            @Override
            public void onSuccess(List<BeaconEvent> foundBeaconEvents) {
                Assertions
                        .assertThat(foundBeaconEvents)
//                        .overridingErrorMessage("There should be 1 action to the Beacon %s at %s there were %d",
//                                TestConstants.BEACON_SCAN_ENTRY_EVENT(clock.now()).getBeaconId().toTraditionalString(),
//                                foundBeaconEvents.size())
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
        List<ActionConversion> conversions = new ArrayList<>();

        BeaconScan scan1 = BeaconScan.from(TestConstants.BEACON_SCAN_ENTRY_EVENT(System.currentTimeMillis() - TimeConstants.ONE_HOUR));
        scan1.setCreatedAt(System.currentTimeMillis() - TimeConstants.ONE_HOUR);
        scans.add(scan1);

        Mockito.when(mockRetrofitApiService.publishHistory(any(HistoryBody.class)))
                .thenReturn(Calls.response(PUBLISH_HISTORY_RESPONSE));

        tested.publishHistory(scans, actions, conversions, new TransportHistoryCallback() {
            @Override
            public void onFailure(Exception volleyError) {
                Assert.fail();
            }

            @Override
            public void onInstantActions(List<BeaconEvent> instantActions) {
                Assertions.assertThat(instantActions.size()).isEqualTo(0);
            }

            @Override
            public void onSuccess(List<BeaconScan> scans, List<BeaconAction> actions, List<ActionConversion> conversions) {
                Assertions.assertThat(scans).isNotNull();
                Assertions.assertThat(scans.size()).isEqualTo(1);
                Assertions.assertThat(conversions).isNotNull();
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

    private static final ResponseBody PUBLISH_HISTORY_RESPONSE = ResponseBody.create(MediaType.parse("application/json"), "");
}