package com.sensorberg.sdk.internal;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.http.helper.RawJSONMockResponse;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.testUtils.TestWithMockServer;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Call;
import retrofit2.Response;
import util.TestConstants;


@RunWith(AndroidJUnit4.class)
public class RetrofitTransportWithCacheBusting extends TestWithMockServer {

    @Inject
    Gson gson;

    @Inject
    Context mContext;

    @Inject
    @Named("androidPlatformIdentifier")
    PlatformIdentifier realPlatformIdentifier;

    RetrofitApiServiceImpl realRetrofitApiService;

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        startWebserver();
        realRetrofitApiService = new RetrofitApiServiceImpl(mContext, gson, realPlatformIdentifier, getBaseUrl());
        realRetrofitApiService.setApiToken(TestConstants.API_TOKEN_DEFAULT);
    }


    @Override
    protected Context getContext() {
        return mContext;
    }

    @Test
    public void should_revalidate_a_cache_entry_with_a_304_response() throws Exception {
        enqueue(com.sensorberg.sdk.test.R.raw.response_raw_layout_cache_busting_001);
        enqueue(com.sensorberg.sdk.test.R.raw.response_raw_layout_cache_busting_002);


        //first request served from network with full body
        realRetrofitApiService.updateBeaconLayout(getUrl("layout")).execute();
        //this should still be served by the network
        realRetrofitApiService.updateBeaconLayout(getUrl("layout")).execute();

        //let´s expire the cache
        Thread.sleep(2100);

        //this response should be the body of 001 but still served from cache
        Response<ResolveResponse> response = realRetrofitApiService.getBeacon(getUrl("layout"), "irrelevant", "foo").execute();
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.body().getAccountProximityUUIDs()).hasSize(3);




    }

    @Test
    public void should_update_a_cache_entry_which_is_still_valid() throws Exception {
        enqueue(com.sensorberg.sdk.test.R.raw.response_raw_layout_cache_busting_003);
        enqueue(com.sensorberg.sdk.test.R.raw.response_raw_layout_cache_busting_004);


        //first request served from network with full body
        realRetrofitApiService.updateBeaconLayout(getUrl("layout")).execute();

        //serve cached response
        Response<ResolveResponse> responseFromInitialCacheValue = realRetrofitApiService.getBeacon(getUrl("layout"), "irrelevant", "foo").execute();
        Assertions.assertThat(responseFromInitialCacheValue).isNotNull();
        Assertions.assertThat(responseFromInitialCacheValue.body().getAccountProximityUUIDs()).hasSize(1);

        //the second request was cached, one real request should have been sent to the server
        Assertions.assertThat(server.getRequestCount()).isEqualTo(1);

        //this should still be served by the network, cache should be updated
        realRetrofitApiService.updateBeaconLayout(getUrl("layout")).execute();

        //we should have 2 requests to the server now
        Assertions.assertThat(server.getRequestCount()).isEqualTo(2);

        //new version, served by cache should contain a different number of proximity UUIDs
        Response<ResolveResponse> response = realRetrofitApiService.getBeacon(getUrl("layout"), "irrelevant", "foo").execute();
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.body().getAccountProximityUUIDs()).hasSize(3);






    }
}
