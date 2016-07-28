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

import static com.sensorberg.sdk.internal.URLFactory.getSettingsURLString;

public class RetrofitApiServiceImpl implements PlatformIdentifier.DeviceInstallationIdentifierChangeListener,
        PlatformIdentifier.AdvertiserIdentifierChangeListener {

    private static final int CONNECTION_TIMEOUT = 30; //seconds

    private static final long HTTP_RESPONSE_DISK_CACHE_MAX_SIZE = 5 * 1024L * 1024L; //5MB

    protected final Context mContext;

    private final Gson mGson;

    private final PlatformIdentifier mPlatformIdentifier;

    private final String mBaseUrl;

    private String mApiToken;

    private HttpLoggingInterceptor.Level mApiServiceLogLevel = HttpLoggingInterceptor.Level.NONE;

    private RetrofitApiService mApiService;

    private OkHttpClient mClient;

    public RetrofitApiServiceImpl(Context ctx, Gson gson, PlatformIdentifier platformId, String baseUrl) {
        mContext = ctx;
        mGson = gson;
        mPlatformIdentifier = platformId;

        if (!baseUrl.endsWith("/")) {
            mBaseUrl = baseUrl + "/";
        } else {
            mBaseUrl = baseUrl;
        }

        platformId.addAdvertiserIdentifierChangeListener(this);
        platformId.addDeviceInstallationIdentifierChangeListener(this);
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

        mClient = okClientBuilder.build();
        return mClient;
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

    public Call<BaseResolveResponse> updateBeaconLayout(@Url String beaconLayoutUrl) {
        return getApiService().updateBeaconLayout(beaconLayoutUrl);
    }

    public Call<ResolveResponse> getBeacon(@Url String beaconURLString, @Header("X-pid") String beaconId, @Header("X-qos") String networkInfo) {
        return getApiService().getBeacon(beaconURLString, beaconId, networkInfo);
    }

    public Call<ResolveResponse> publishHistory(@Url String beaconLayoutUrl, @Body HistoryBody body) {
        return getApiService().publishHistory(beaconLayoutUrl, body);
    }

    public Call<SettingsResponse> getSettings() {
        return getSettings(getSettingsURLString(mApiToken));
    }

    public Call<SettingsResponse> getSettings(@Url String url) {
        return getApiService().getSettings(url);
    }

    public void setApiToken(String newToken) {
        if (!Objects.equals(newToken, mApiToken) && mClient != null){
            try {
                mClient.cache().evictAll();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.mApiToken = newToken;
    }


    @Override
    public void advertiserIdentifierChanged(String advertiserIdentifier) {
        //we don't care, it's always dynamic now
    }

    @Override
    public void deviceInstallationIdentifierChanged(String deviceInstallationIdentifier) {
        //we don't care, it's always dynamic now
    }
}
