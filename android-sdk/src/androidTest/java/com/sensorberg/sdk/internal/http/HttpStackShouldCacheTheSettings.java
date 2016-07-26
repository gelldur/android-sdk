package com.sensorberg.sdk.internal.http;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.http.helper.RawJSONMockResponse;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Call;
import retrofit2.Response;

@RunWith(AndroidJUnit4.class)
public class HttpStackShouldCacheTheSettings {

    @Inject
    Gson gson;

    @Inject
    @Named("androidPlatformIdentifier")
    PlatformIdentifier platformIdentifier;

    MockWebServer server;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        server = new MockWebServer();
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void test_should_only_call_the_network_once() throws Exception {
        String baseUrl = server.url("/").toString();
        MockResponse successfulCachedSettingsMockResponse = RawJSONMockResponse.fromRawResource(
                InstrumentationRegistry.getContext().getResources().openRawResource(com.sensorberg.sdk.test.R.raw.response_raw_settings_etag_001));

        server.enqueue(successfulCachedSettingsMockResponse);

        RetrofitApiServiceImpl realRetrofitApiService = new RetrofitApiServiceImpl(InstrumentationRegistry.getContext(), gson, platformIdentifier);
        Call<SettingsResponse> call = realRetrofitApiService.getSettings();

        Response response1 = call.execute();
        Response response2 = call.clone().execute();

        Assertions.assertThat(server.getRequestCount()).isEqualTo(1);
        Assertions.assertThat(response1).isNotNull();
        Assertions.assertThat(response1.isSuccessful()).isTrue();
        Assertions.assertThat(response2).isNotNull();
        Assertions.assertThat(response2.isSuccessful()).isTrue();
        Assertions.assertThat(response2.raw().cacheResponse()).isNotNull();
    }
}
