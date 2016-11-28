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

        ResolverConfiguration configuration = new ResolverConfiguration();
        configuration.setApiToken("f257de3b91d141aa93b6a9b39c97b83df257de3b91d141aa93b6a9b39c97b83d");

        String baseUrl = BuildConfig.RESOLVER_URL != null ? BuildConfig.RESOLVER_URL : RetrofitApiTransport.RESOLVER_BASE_URL;
        RetrofitApiServiceImpl retrofitServiceWithOutCache = new RetrofitApiServiceImpl(null, gson, platformIdentifier, baseUrl);
        transport = new RetrofitApiTransport(retrofitServiceWithOutCache, clock);
        tested = new Resolver(configuration, testHandlerManager, transport, null);

    }


    /**
     * https://manage.sensorberg.com/#/campaign/edit/ab68d4ee-8b2d-4f40-adc2-a7ebc9505e89
     * https://manage.sensorberg.com/#/campaign/edit/5dc7f22f-dbcf-4065-8b28-e81b0149fcc8
     * https://manage.sensorberg.com/#/campaign/edit/292ba508-226e-41c3-aac7-969fa712c435
     *
     * https://bm-frontend-staging.sensorberg.io/#/campaign/edit/ab68d4ee-8b2d-4f40-adc2-a7ebc9505e89
     * https://bm-frontend-staging.sensorberg.io/#/campaign/edit/5dc7f22f-dbcf-4065-8b28-e81b0149fcc8
     * https://bm-frontend-staging.sensorberg.io/#/campaign/edit/292ba508-226e-41c3-aac7-969fa712c435
     *
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
     *  https://manage.sensorberg.com/#/campaign/edit/6edd5ff0-d63a-4968-b7fa-b448d1c3a0e9
     *
     *  https://staging-manage.sensorberg.com/#/campaign/edit/be0c8822-937c-49ee-9890-13fb8ecbad05
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
