package com.sensorberg.sdk.internal.http;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.http.helper.RawJSONMockResponse;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.testUtils.SuccessfulRetrofitApiService;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Call;
import retrofit2.Response;
import util.TestConstants;

@RunWith(AndroidJUnit4.class)
public class ApiServiceShould {

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

    private MockWebServer server;

    private String serverBaseUrl;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        server = new MockWebServer();
        server.start();
        serverBaseUrl = server.url("/").toString();
        realRetrofitApiService = new RetrofitApiServiceImpl(InstrumentationRegistry.getContext(), gson, realPlatformIdentifier, serverBaseUrl);
        realRetrofitApiService.setApiToken(TestConstants.API_TOKEN_DEFAULT);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
        realPlatformIdentifier.setAdvertisingIdentifier(null);
    }

    @Test
    public void apiservice_should_have_valid_useragent_in_header() throws Exception {
        MockResponse successfulCachedSettingsMockResponse = RawJSONMockResponse.fromRawResource(
                InstrumentationRegistry.getContext().getResources().openRawResource(com.sensorberg.sdk.test.R.raw.response_raw_layout_etag_001));
        server.enqueue(successfulCachedSettingsMockResponse);

        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout(serverBaseUrl);
        Response<BaseResolveResponse> response = call.execute();

        Assertions.assertThat(response.raw().request().headers()).isNotNull();
        Assertions.assertThat(response.raw().request().headers().get(Transport.HEADER_USER_AGENT))
                .isEqualTo(realPlatformIdentifier.getUserAgentString());
    }

    @Test
    public void apiservice_should_have_null_default_advertiserid_in_header() throws Exception {
        MockResponse successfulCachedSettingsMockResponse = RawJSONMockResponse.fromRawResource(
                InstrumentationRegistry.getContext().getResources().openRawResource(com.sensorberg.sdk.test.R.raw.response_raw_layout_etag_001));
        server.enqueue(successfulCachedSettingsMockResponse);

        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout(serverBaseUrl);

        Assertions.assertThat(realPlatformIdentifier.getAdvertiserIdentifier()).isNull();
        Response<BaseResolveResponse> responseWithAdvertiserId = call.clone().execute();

        Assertions.assertThat(responseWithAdvertiserId.raw().request().headers().get(Transport.HEADER_ADVERTISER_IDENTIFIER))
                .isEqualTo(realPlatformIdentifier.getAdvertiserIdentifier());
    }

    @Test
    public void apiservice_should_have_advertiserid_in_header() throws Exception {
        MockResponse successfulCachedSettingsMockResponse = RawJSONMockResponse.fromRawResource(
                InstrumentationRegistry.getContext().getResources().openRawResource(com.sensorberg.sdk.test.R.raw.response_raw_layout_etag_001));
        server.enqueue(successfulCachedSettingsMockResponse);

        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout(serverBaseUrl);
        realPlatformIdentifier.setAdvertisingIdentifier("TEST_ADID");

        Assertions.assertThat(realPlatformIdentifier.getAdvertiserIdentifier()).isNotNull();
        Assertions.assertThat(realPlatformIdentifier.getAdvertiserIdentifier()).isEqualToIgnoringCase("TEST_ADID");
        Response<BaseResolveResponse> responseWithAdvertiserId = call.clone().execute();

        Assertions.assertThat(responseWithAdvertiserId.raw().request().headers().get(Transport.HEADER_ADVERTISER_IDENTIFIER))
                .isEqualTo(realPlatformIdentifier.getAdvertiserIdentifier());
    }

    @Test
    public void apiservice_should_have_valid_installationid_in_header() throws Exception {
        MockResponse successfulCachedSettingsMockResponse = RawJSONMockResponse.fromRawResource(
                InstrumentationRegistry.getContext().getResources().openRawResource(com.sensorberg.sdk.test.R.raw.response_raw_layout_etag_001));
        server.enqueue(successfulCachedSettingsMockResponse);

        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout(serverBaseUrl);
        Response<BaseResolveResponse> response = call.execute();

        Assertions.assertThat(response.raw().request().headers()).isNotNull();
        Assertions.assertThat(response.raw().request().headers().get(Transport.HEADER_INSTALLATION_IDENTIFIER))
                .isEqualTo(realPlatformIdentifier.getDeviceInstallationIdentifier());
    }

    @Test
    public void apiservice_should_have_apitoken_header() throws Exception {
        MockResponse successfulCachedSettingsMockResponse = RawJSONMockResponse.fromRawResource(
                InstrumentationRegistry.getContext().getResources().openRawResource(com.sensorberg.sdk.test.R.raw.response_raw_layout_etag_001));
        server.enqueue(successfulCachedSettingsMockResponse);

        Call<BaseResolveResponse> call = realRetrofitApiService.updateBeaconLayout(serverBaseUrl);
        Response<BaseResolveResponse> response = call.execute();

        Assertions.assertThat(response.raw().request().headers()).isNotNull();
        Assertions.assertThat(response.raw().request().headers().get(Transport.HEADER_XAPIKEY))
                .isEqualTo(TestConstants.API_TOKEN_DEFAULT);
        Assertions.assertThat(response.raw().request().headers().get(Transport.HEADER_AUTHORIZATION))
                .isEqualTo(TestConstants.API_TOKEN_DEFAULT);
    }

    @Test
    public void apiservice_should_throw_an_unknown_host_exception() throws Exception {
        SuccessfulRetrofitApiService retrofitApiService = new SuccessfulRetrofitApiService(mContext, gson, realPlatformIdentifier,
                "http://localhost/");
        retrofitApiService.getOriginalOkHttpClient().cache().evictAll();

        exception.expect(UnknownHostException.class);

        OkHttpClient client = retrofitApiService.getOriginalOkHttpClient();
        okhttp3.Response okHttpResponse = client.newCall(new Request.Builder().url("https://test.comxxx").get().build())
                .execute();
        Assertions.assertThat(okHttpResponse).isNotNull();
    }

    @Test
    public void apiservice_should_cache_responses() throws Exception {
        MockResponse successfulCachedSettingsMockResponse = RawJSONMockResponse.fromRawResource(
                InstrumentationRegistry.getContext().getResources().openRawResource(com.sensorberg.sdk.test.R.raw.response_raw_layout_etag_001));
        server.enqueue(successfulCachedSettingsMockResponse);

        Call<BaseResolveResponse> call1 = realRetrofitApiService.updateBeaconLayout(serverBaseUrl);
        Response<BaseResolveResponse> response1 = call1.execute();
        Assertions.assertThat(response1.isSuccessful()).isTrue();

        Call<BaseResolveResponse> call2 = realRetrofitApiService.updateBeaconLayout(serverBaseUrl);
        Response<BaseResolveResponse> response2 = call2.execute();
        Assertions.assertThat(response2.isSuccessful()).isTrue();

        Assertions.assertThat(response2.raw().cacheResponse()).isNotNull();
        Assertions.assertThat(response2.raw().networkResponse()).isNull();
    }
}
