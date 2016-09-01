package com.sensorberg.sdk.internal.transport;

import com.google.gson.Gson;

import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.interfaces.RetrofitApiService;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.utils.Objects;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Url;

public class RetrofitApiServiceImpl {

    private static final int CONNECTION_TIMEOUT = 30; //seconds

    private static final long HTTP_RESPONSE_DISK_CACHE_MAX_SIZE = 5 * 1024L * 1024L; //5MB

    private final Gson mGson;

    private final PlatformIdentifier mPlatformIdentifier;

    private String mApiToken;

    private final RetrofitApiService mApiService;

    private OkHttpClient mClient;
    private HttpLoggingInterceptor httpLoggingInterceptor;

    public RetrofitApiServiceImpl(File cacheFolder, Gson gson, PlatformIdentifier platformId, String baseUrl) {
        mGson = gson;
        mPlatformIdentifier = platformId;

        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getOkHttpClient(cacheFolder))
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .build();

        mApiService = restAdapter.create(RetrofitApiService.class);
    }

    private final Interceptor headerAuthorizationInterceptor = new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            Headers.Builder headersBuilder = request.headers()
                    .newBuilder()
                    .add(Transport.HEADER_USER_AGENT, mPlatformIdentifier.getUserAgentString())
                    .add(Transport.HEADER_INSTALLATION_IDENTIFIER, mPlatformIdentifier.getDeviceInstallationIdentifier());

            if (mPlatformIdentifier.getAdvertiserIdentifier() != null) {
                headersBuilder.add(Transport.HEADER_ADVERTISER_IDENTIFIER, mPlatformIdentifier.getAdvertiserIdentifier());
            }

            if (mApiToken != null) {
                headersBuilder
                        .add(Transport.HEADER_XAPIKEY, mApiToken);
            }

            request = request.newBuilder().headers(headersBuilder.build()).build();
            return chain.proceed(request);
        }
    };

    private OkHttpClient getOkHttpClient(File baseDir) {
        OkHttpClient.Builder okClientBuilder = new OkHttpClient.Builder();

        okClientBuilder.addInterceptor(headerAuthorizationInterceptor);

        httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        okClientBuilder.addInterceptor(httpLoggingInterceptor);

        okClientBuilder.retryOnConnectionFailure(true);

        if (baseDir != null) {
            final File cacheDir = new File(baseDir, "HttpResponseCache");
            okClientBuilder.cache(new Cache(cacheDir, HTTP_RESPONSE_DISK_CACHE_MAX_SIZE));
        }

        okClientBuilder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        okClientBuilder.readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        okClientBuilder.writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);

        mClient = okClientBuilder.build();
        return mClient;
    }

    public void setLoggingEnabled(boolean enabled) {
        synchronized (mGson) {
            if (enabled) {
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            } else {
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
            }
        }
    }

    public Call<BaseResolveResponse> updateBeaconLayout() {
        return mApiService.updateBeaconLayout();
    }

    public Call<ResolveResponse> getBeacon(String beaconId,String networkInfo) {
        return mApiService.getBeacon(beaconId, networkInfo);
    }

    public Call<ResolveResponse> publishHistory(@Body HistoryBody body) {
        return mApiService.publishHistory(body);
    }

    public Call<SettingsResponse> getSettings() {
        return getSettings(mApiToken);
    }

    public Call<SettingsResponse> getSettings(@Url String apiKey) {
        return mApiService.getSettings(apiKey);
    }

    public boolean setApiToken(String newToken) {
        boolean tokensDiffer = mApiToken != null && !Objects.equals(newToken, mApiToken);
        if (tokensDiffer && mClient != null){
            try {
                mClient.cache().evictAll();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.mApiToken = newToken;
        return tokensDiffer;
    }
}
