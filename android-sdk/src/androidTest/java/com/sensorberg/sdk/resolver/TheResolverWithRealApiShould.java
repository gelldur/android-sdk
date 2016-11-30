package com.sensorberg.sdk.resolver;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.test.BuildConfig;
import com.sensorberg.sdk.testUtils.TestHandlerManager;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

import static org.fest.assertions.api.Fail.fail;

@RunWith(AndroidJUnit4.class)
public class TheResolverWithRealApiShould {

    private Resolver tested;

    @Inject
    TestHandlerManager testHandlerManager;

    @Inject
    Clock clock;

    private RetrofitApiTransport transport;

    @Inject
    Gson gson;

    @Inject
    @Named("androidPlatformIdentifier")
    PlatformIdentifier platformIdentifier;

    @Inject
    Context context;

    @Before
    public void setUp() throws Exception {


        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        RetrofitApiTransport.RESOLVER_BASE_URL = "https://staging.sensorberg-cdn.io";
        ResolverConfiguration configuration = new ResolverConfiguration();
        configuration.setApiToken("50f912c903b59e10802537b1499172bbe6a38bab1c2a075639024f0975c3c35f");

        String baseUrl = BuildConfig.RESOLVER_URL != null ? BuildConfig.RESOLVER_URL : RetrofitApiTransport.RESOLVER_BASE_URL;
        RetrofitApiServiceImpl retrofitServiceWithOutCache = new RetrofitApiServiceImpl(null, gson, platformIdentifier, baseUrl);
        transport = new RetrofitApiTransport(retrofitServiceWithOutCache, clock);
        tested = new Resolver(configuration, testHandlerManager, transport, null);

    }


    /**
     * for BE integration check Ronaldo Pace user SDK_TEST app
     */
    @Test
    public void test_resolve_in_app_function() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        ResolverListener mockListener = new ResolverListener() {
            @Override
            public void onResolutionFailed(Throwable cause, ScanEvent scanEvent) {
                fail(cause.getMessage() + scanEvent.toString());
                latch.countDown();
            }

            @Override
            public void onResolutionsFinished(List<BeaconEvent> events) {
                Assertions.assertThat(events).hasSize(3);
                latch.countDown();
            }

        };
        tested.setListener(mockListener);

        tested.resolve(new ScanEvent.Builder()
                .withBeaconId(TestConstants.IN_APP_BEACON_ID)
                .withEntry(true).build()
        );
        Assertions.assertThat(latch.await(10, TimeUnit.SECONDS)).overridingErrorMessage("Request did not return within time").isEqualTo(true);

    }

    /**
     * for BE integration check Ronaldo Pace user SDK_TEST app
     */
    @Test
    public void test_beacon_with_delay() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        tested.setListener(new ResolverListener() {
            @Override
            public void onResolutionFailed(Throwable cause, ScanEvent scanEvent) {
                fail(cause.getMessage());
                latch.countDown();
            }

            @Override
            public void onResolutionsFinished(List<BeaconEvent> events) {
                Assertions.assertThat(events.get(0).getAction().getDelayTime()).isEqualTo(120000);
                latch.countDown();
            }
        });

        tested.resolve(new ScanEvent.Builder()
                .withBeaconId(TestConstants.DELAY_BEACON_ID)
                .withEntry(true)
                .build()
        );
        Assertions.assertThat(latch.await(10, TimeUnit.SECONDS)).overridingErrorMessage("Request did not return within time").isEqualTo(true);
    }
}
