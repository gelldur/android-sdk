package com.sensorberg.sdk.resolver;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.testUtils.TestHandlerManager;

import org.fest.assertions.api.Assertions;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.List;

import javax.inject.Inject;

import retrofit2.mock.Calls;
import util.TestConstants;
import util.Utils;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class TheResolverShould {

    private static final ScanEvent SCANEVENT_1 = new ScanEvent.Builder()
            .withBeaconId(TestConstants.REGULAR_BEACON_ID)
            .build();

    private static final ScanEvent RESOLVABLE_ENTRY_EVENT_WITH_ID_3 = new ScanEvent.Builder()
            .withBeaconId(TestConstants.LEET_BEACON_ID_3)
            .withEventMask(ScanEventType.ENTRY.getMask())
            .build();

    private static final ScanEvent RESOLVABLE_ENTRY_EVENT_WITH_INAPP_ACTIONS = new ScanEvent.Builder()
            .withBeaconId(TestConstants.IN_APP_BEACON_ID)
            .withEventMask(ScanEventType.ENTRY.getMask())
            .build();

    @Inject
    Gson gson;

    private Resolver tested;

    RetrofitApiServiceImpl mockRetrofitApiService = mock(RetrofitApiServiceImpl.class);

    TestHandlerManager testHandlerManager = new TestHandlerManager();

    private ResolutionConfiguration resolutionConfiguration;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        ResolverConfiguration resolverConfiguration = new ResolverConfiguration();
        testHandlerManager.getCustomClock().setNowInMillis(new DateTime(2015, 7, 7, 1, 1, 1).getMillis());
        Transport testTransportWithMockService = new RetrofitApiTransport(mockRetrofitApiService, testHandlerManager.getCustomClock());

        tested = new Resolver(resolverConfiguration, testHandlerManager, testTransportWithMockService);

        resolutionConfiguration = new ResolutionConfiguration();
    }

    @Test
    public void test_should_try_to_resolve_a_beacon() throws Exception {
        resolutionConfiguration.setScanEvent(SCANEVENT_1);
        Resolution resolution = tested.createResolution(resolutionConfiguration);
        Resolution spyResolution = spy(resolution);
        Mockito.when(mockRetrofitApiService.getBeacon(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Calls.response(new ResolveResponse.Builder().build()));

        tested.startResolution(spyResolution);

        verify(spyResolution).queryServer();
    }

    @Test
    public void test_enter_exit_action() throws Exception {
        ResolveResponse resolveResponse = gson
                .fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_response_enter_exit_action, InstrumentationRegistry
                        .getContext()), ResolveResponse.class);
        Mockito.when(mockRetrofitApiService.getBeacon(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Calls.response(resolveResponse));

        ResolverListener testListener = new ResolverListener() {
            @Override
            public void onResolutionFailed(Resolution resolution, Throwable cause) {
                Assert.fail(cause.getMessage());
            }

            @Override
            public void onResolutionsFinished(List<BeaconEvent> events) {
                Assertions.assertThat(events).hasSize(1);
            }
        };

        tested.addResolverListener(testListener);
        resolutionConfiguration.setScanEvent(RESOLVABLE_ENTRY_EVENT_WITH_ID_3);
        Resolution resolution = tested.createResolution(resolutionConfiguration);

        resolution.start();
    }

    @Test
    public void test_resolve_in_app_function() throws Exception {
        ResolveResponse resolveResponse = gson
                .fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_response_inapp_action, InstrumentationRegistry
                        .getContext()), ResolveResponse.class);
        Mockito.when(mockRetrofitApiService.getBeacon(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Calls.response(resolveResponse));

        ResolverListener mockListener = new ResolverListener() {
            @Override
            public void onResolutionFailed(Resolution resolution, Throwable cause) {
                Assert.fail(cause.getMessage() + resolution.toString());
            }

            @Override
            public void onResolutionsFinished(List<BeaconEvent> events) {
                Assertions.assertThat(events).hasSize(3);
            }

        };

        tested.addResolverListener(mockListener);
        resolutionConfiguration.setScanEvent(RESOLVABLE_ENTRY_EVENT_WITH_INAPP_ACTIONS);
        Resolution resolution = tested.createResolution(resolutionConfiguration);
        resolution.start();
    }

    @Test
    public void test_beacon_with_delay() throws Exception {
        ResolveResponse resolveResponse = gson
                .fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_response_delayed_action, InstrumentationRegistry
                        .getContext()), ResolveResponse.class);
        Mockito.when(mockRetrofitApiService.getBeacon(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Calls.response(resolveResponse));

        ResolverListener mockListener = mock(ResolverListener.class);
        tested.addResolverListener(mockListener);
        resolutionConfiguration.setScanEvent(RESOLVABLE_ENTRY_EVENT_WITH_ID_3);
        Resolution resolution = tested.createResolution(resolutionConfiguration);
        resolution.start();

        verify(mockListener).onResolutionsFinished(argThat(new BaseMatcher<List<BeaconEvent>>() {
            public long delay;

            @Override
            public boolean matches(Object o) {
                List<BeaconEvent> list = (List<BeaconEvent>) o;
                delay = list.get(0).getAction().getDelayTime();
                return delay == 120000;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("Delaytime was %d and not %d as expected", delay, 120000));
            }
        }));
    }
}