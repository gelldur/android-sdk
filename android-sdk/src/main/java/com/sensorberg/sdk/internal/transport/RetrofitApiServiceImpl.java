package com.sensorberg.sdk.internal.transport;

import com.google.gson.Gson;

import com.sensorberg.sdk.BuildConfig;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.interfaces.RetrofitApiService;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.model.server.ResolveResponse;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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

@Accessors(prefix = "m")
public class RetrofitApiServiceImpl {

    public static final String DEFAULT_BASE_URL = "https://resolver.sensorberg.com/";

    private static final int CONNECTION_TIMEOUT = 30; //seconds

    private static final long HTTP_RESPONSE_DISK_CACHE_MAX_SIZE = 5 * 1024L * 1024L; //5MB

    protected final Context mContext;

    private final Gson mGson;

    private final PlatformIdentifier mPlatformIdentifier;

    @Getter
    private String mBaseUrl;

    @Getter
    @Setter
    private String mApiToken;

    private HttpLoggingInterceptor.Level mApiServiceLogLevel = HttpLoggingInterceptor.Level.NONE;

    private RetrofitApiService mApiService;

    public RetrofitApiServiceImpl(Context ctx, Gson gson, PlatformIdentifier platformId) {
        mContext = ctx;
        mGson = gson;
        mPlatformIdentifier = platformId;

        if (BuildConfig.RESOLVER_URL != null && !BuildConfig.RESOLVER_URL.equalsIgnoreCase("null")) {
            mBaseUrl = BuildConfig.RESOLVER_URL;
        } else {
            mBaseUrl = DEFAULT_BASE_URL;
        }
    }

    private RetrofitApiService getApiService() {
        if (mApiService == null) {
            Retrofit restAdapter = new Retrofit.Builder()
                    .baseUrl(mBaseUrl)
                    .client(getOkHttpClient(mContext))
                    .addConverterFactory(GsonConverterFactory.create(mGson))
                    .build();

            mApiService = restAdapter.create(RetrofitApiService.class);
        }

        return mApiService;
    }

    private Interceptor headerAuthorizationInterceptor = new Interceptor() {
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
                        .add(Transport.HEADER_AUTHORIZATION, mApiToken)
                        .add(Transport.HEADER_XAPIKEY, mApiToken);
            }

            request = request.newBuilder().headers(headersBuilder.build()).build();
            return chain.proceed(request);
        }
    };

    protected OkHttpClient getOkHttpClient(Context context) {
        OkHttpClient.Builder okClientBuilder = new OkHttpClient.Builder();

        okClientBuilder.addInterceptor(headerAuthorizationInterceptor);

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(mApiServiceLogLevel);
        okClientBuilder.addInterceptor(httpLoggingInterceptor);

        okClientBuilder.retryOnConnectionFailure(true);

        final File baseDir = context.getCacheDir();
        if (baseDir != null) {
            final File cacheDir = new File(baseDir, "HttpResponseCache");
            okClientBuilder.cache(new Cache(cacheDir, HTTP_RESPONSE_DISK_CACHE_MAX_SIZE));
        }

        okClientBuilder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        okClientBuilder.readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        okClientBuilder.writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);

        return okClientBuilder.build();
    }

    public void setLoggingEnabled(boolean enabled) {
        synchronized (mGson) {
            if (enabled) {
                mApiServiceLogLevel = HttpLoggingInterceptor.Level.BODY;
            } else {
                mApiServiceLogLevel = HttpLoggingInterceptor.Level.NONE;
            }

            if (mApiService != null) {
                mApiService = null;
            }
        }
    }

    public void setBaseUrl(String baseUrl) {
        synchronized (mGson) {
            if (baseUrl == null) {
                mBaseUrl = DEFAULT_BASE_URL;
            } else if (!baseUrl.endsWith("/")) {
                mBaseUrl = baseUrl + "/";
            } else {
                mBaseUrl = baseUrl;
            }

            if (mApiService != null) {
                mApiService = null;
            }
        }
    }

    public Call<BaseResolveResponse> updateBeaconLayout() {
        return getApiService().updateBeaconLayout();
    }

    public Call<ResolveResponse> getBeacon(@Header("X-pid") String beaconId, @Header("X-qos") String networkInfo) {
        return getApiService().getBeacon(beaconId, networkInfo);
    }

    public Call<ResolveResponse> publishHistory(@Body HistoryBody body) {
        return getApiService().publishHistory(body);
    }

    public Call<SettingsResponse> getSettings() {
        return getApiService().getSettings(mApiToken);
    }

}
