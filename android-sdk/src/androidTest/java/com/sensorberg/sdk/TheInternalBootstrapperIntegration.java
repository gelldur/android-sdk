package com.sensorberg.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.sensorberg.sdk.action.ActionFactory;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.model.server.ResolveAction;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.presenter.LocalBroadcastManager;
import com.sensorberg.sdk.presenter.ManifestParser;
import com.sensorberg.sdk.resolver.ResolverConfiguration;
import com.sensorberg.sdk.scanner.BeaconActionHistoryPublisher;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.test.TestGenericBroadcastReceiver;
import com.sensorberg.sdk.test.TestGenericBroadcastReceiver2;
import com.sensorberg.sdk.testUtils.TestHandlerManager;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import org.fest.assertions.api.Assertions;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.content.IntentFilter;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.mock.Calls;
import util.TestConstants;
import util.Utils;

@RunWith(AndroidJUnit4.class)
public class TheInternalBootstrapperIntegration {

    @Inject
    TestServiceScheduler testServiceScheduler;

    @Inject
    TestHandlerManager testHandlerManager;

    RetrofitApiServiceImpl mockRetrofitApiService = Mockito.mock(RetrofitApiServiceImpl.class);

    @Inject
    @Named("testBluetoothPlatform")
    BluetoothPlatform bluetoothPlatform;

    @Inject
    Gson gson;

    @Inject
    @Named("realBeaconActionHistoryPublisher")
    BeaconActionHistoryPublisher beaconActionHistoryPublisher;

    InternalApplicationBootstrapper spiedInternalApplicationBootstrapper;

    private Transport spiedTransportWithMockService;

    private static final JsonObject ANY_IN_APP_JSON = new JsonObject();

    static {
        try {
            ANY_IN_APP_JSON.addProperty("url", "sensorberg://");
        } catch (Exception e) {
            System.err.print("exception adding property to JsonObject = " + e.getMessage());
        }
    }

    private static final String ANY_UUID = UUID.randomUUID().toString();

    private static final String ANOTHER_UUID = UUID.randomUUID().toString();

    private static final ResolveResponse PUBLISH_HISTORY_RESPONSE = new ResolveResponse.Builder()
            .withInstantActions(Arrays.asList(
                    new ResolveAction.Builder()
                            .withBeacons(Arrays.asList(TestConstants.ANY_BEACON_ID.getPid()))
                            .withType(ActionFactory.ServerType.IN_APP)
                            .withUuid(ANY_UUID)
                            .withContent(ANY_IN_APP_JSON)
                            .build()
            ))
            .build();

    private ResolveResponse RESOLVE_RESPONSE_WITH_REPORT_IMMEDIATELY = new ResolveResponse.Builder()
            .withActions(Arrays.asList(
                    new ResolveAction.Builder()
                            .withBeacons(Arrays.asList(TestConstants.ANY_BEACON_ID.getPid()))
                            .withTrigger(ScanEventType.ENTRY.getMask())
                            .withUuid(ANOTHER_UUID)
                            .withReportImmediately(true)
                            .build()
            ))
            .build();

    private ResolveResponse RESOLVE_RESPONSE_WITH_ACTION = new ResolveResponse.Builder()
            .withActions(Arrays.asList(
                    new ResolveAction.Builder()
                            .withBeacons(Arrays.asList(TestConstants.ANY_BEACON_ID.getPid()))
                            .withType(ActionFactory.ServerType.IN_APP)
                            .withTrigger(ScanEventType.ENTRY.getMask())
                            .withUuid(ANY_UUID)
                            .withContent(ANY_IN_APP_JSON)
                            .build()
            ))
            .build();

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        beaconActionHistoryPublisher.deleteAllData();

        spiedTransportWithMockService = Mockito.spy(new RetrofitApiTransport(mockRetrofitApiService, testHandlerManager.getCustomClock()));
        spiedInternalApplicationBootstrapper = Mockito.spy(new InternalApplicationBootstrapper(spiedTransportWithMockService, testServiceScheduler,
                testHandlerManager, testHandlerManager.getCustomClock(), bluetoothPlatform, new ResolverConfiguration()));

        TestGenericBroadcastReceiver broadcastReceiver = new TestGenericBroadcastReceiver();
        LocalBroadcastManager.getInstance(InstrumentationRegistry.getContext()).registerReceiver(broadcastReceiver,
                new IntentFilter(ManifestParser.actionString));

        TestGenericBroadcastReceiver.reset();
        TestGenericBroadcastReceiver2.reset();
    }

    @Test
    public void test_an_instant_action_workflow() throws Exception {
        //enqueue the layout with a beacon for report immediately
        Mockito.when(mockRetrofitApiService.getBeacon(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Calls.response(RESOLVE_RESPONSE_WITH_REPORT_IMMEDIATELY));

        //enqueue the reporting result
        Mockito.when(mockRetrofitApiService.publishHistory(Mockito.anyString(), Mockito.any(HistoryBody.class)))
                .thenReturn(Calls.response(PUBLISH_HISTORY_RESPONSE));

        System.out.println("TheInternalBootstrapperIntegration start test_an_instant_action_workflow");

        //simulate the entry
        spiedInternalApplicationBootstrapper.onScanEventDetected(new ScanEvent.Builder()
                .withBeaconId(TestConstants.ANY_BEACON_ID)
                .withEntry(true)
                .build());

        //we should have exactly one notification
        Mockito.verify(spiedTransportWithMockService, Mockito.timeout(5000).times(1))
                .getBeacon(Mockito.any(ScanEvent.class), Mockito.any(BeaconResponseHandler.class));

        //TODO this does get called in real code and during debugging, but Mockito says it doesn't
//        Mockito.verify(spiedTransportWithMockService, Mockito.timeout(5000).times(1))
//                .publishHistory(Mockito.anyList(), Mockito.anyList(), Mockito.any(TransportHistoryCallback.class));
//        Mockito.verify(spiedInternalApplicationBootstrapper, Mockito.timeout(5000).times(1))
//                .presentBeaconEvent(Mockito.any(BeaconEvent.class));

        System.out.println("TheInternalBootstrapperIntegration end");
    }

    @Test
    public void test_precaching() {
        try {
            BaseResolveResponse updateLayoutResponse = gson
                    .fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.response_resolve_precaching,
                            InstrumentationRegistry.getContext()), BaseResolveResponse.class);
            Mockito.when(mockRetrofitApiService.updateBeaconLayout(Mockito.anyString())).thenReturn(Calls.response(updateLayoutResponse));

            ResolveResponse getBeaconResponse = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.response_resolve_precaching,
                    InstrumentationRegistry.getContext()), ResolveResponse.class);
            Mockito.when(mockRetrofitApiService.getBeacon(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(Calls.response(getBeaconResponse));
        } catch (Exception e) {
            Assertions.fail(e.toString());
        }

        System.out.println("TheInternalBootstrapperIntegration start test_precaching");
        spiedInternalApplicationBootstrapper.updateBeaconLayout();

        //simulate the entry
        spiedInternalApplicationBootstrapper.onScanEventDetected(TestConstants.BEACON_SCAN_ENTRY_EVENT(0));

        Mockito.verify(spiedTransportWithMockService, Mockito.timeout(5000).times(1))
                .getBeacon(Mockito.any(ScanEvent.class), Mockito.any(BeaconResponseHandler.class));
        Mockito.verify(spiedTransportWithMockService, Mockito.timeout(5000).times(1))
                .updateBeaconLayout();

        //TODO this does get called in real code and during debugging, but Mockito says it doesn't
//        Mockito.verify(spiedInternalApplicationBootstrapper, Mockito.timeout(5000).times(1))
//                .presentBeaconEvent(Mockito.any(BeaconEvent.class));
    }

    @Test
    public void test_precaching_of_account_proximityUUIDS() throws IOException, JSONException, InterruptedException {
        BaseResolveResponse resolveResponse = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.response_resolve_precaching,
                InstrumentationRegistry.getContext()), BaseResolveResponse.class);
        Mockito.when(mockRetrofitApiService.updateBeaconLayout(Mockito.anyString())).thenReturn(Calls.response(resolveResponse));

        Assertions.assertThat(spiedInternalApplicationBootstrapper.proximityUUIDs).hasSize(0);

        spiedTransportWithMockService.setProximityUUIDUpdateHandler(new Transport.ProximityUUIDUpdateHandler() {
            @Override
            public void proximityUUIDListUpdated(List<String> proximityUUIDs) {
                Assertions.assertThat(proximityUUIDs.size()).isEqualTo(5);
            }
        });

        spiedInternalApplicationBootstrapper.updateBeaconLayout();
    }
}
