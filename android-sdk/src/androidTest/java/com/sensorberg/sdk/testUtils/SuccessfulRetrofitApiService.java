package com.sensorberg.sdk.testUtils;

import com.google.gson.Gson;

import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.interfaces.RetrofitApiService;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.settings.Settings;

import android.content.Context;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Url;
import retrofit2.mock.BehaviorDelegate;
import retrofit2.mock.Calls;
import retrofit2.mock.MockRetrofit;
import retrofit2.mock.NetworkBehavior;

public class SuccessfulRetrofitApiService extends RetrofitApiServiceImpl {

    private final BehaviorDelegate<RetrofitApiService> delegate;

    public SuccessfulRetrofitApiService(Context ctx, Gson gson, PlatformIdentifier platformId, String baseUrl) {
        super(ctx, gson, platformId, baseUrl);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getOkHttpClient(ctx))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // Create a MockRetrofit object with a NetworkBehavior which manages the fake behavior of calls.
        NetworkBehavior behavior = NetworkBehavior.create();
        MockRetrofit mockRetrofit = new MockRetrofit.Builder(retrofit)
                .networkBehavior(behavior)
                .build();

        delegate = mockRetrofit.create(RetrofitApiService.class);
    }

    public OkHttpClient getOriginalOkHttpClient() {
        return getOkHttpClient(mContext);
    }

    @Override
    public boolean setApiToken(String newToken) {
        return super.setApiToken(newToken);
    }

    @Override
    public void setLoggingEnabled(boolean enabled) {
        super.setLoggingEnabled(enabled);
    }

    @Override
    public Call<BaseResolveResponse> updateBeaconLayout(@Url String beaconLayoutUrl) {
        //TODO
        ResolveResponse response = new ResolveResponse.Builder().build();
        return delegate.returningResponse(response).updateBeaconLayout(beaconLayoutUrl);
    }

    public Call<BaseResolveResponse> updateBeaconLayoutFail(@Url String beaconLayoutUrl) {
        Response<ResolveResponse> ret = Response.error(404, ResponseBody.create(MediaType.parse("plain/text"), ""));
        return delegate.returning(Calls.response(ret)).updateBeaconLayout(beaconLayoutUrl);
    }

    @Override
    public Call<ResolveResponse> getBeacon(@Url String beaconURLString, @Header("X-pid") String beaconId, @Header("X-qos") String networkInfo) {
        //TODO
        ResolveResponse response = new ResolveResponse.Builder().build();
        return delegate.returningResponse(response).getBeacon(beaconURLString, beaconId, networkInfo);
    }

    @Override
    public Call<ResolveResponse> publishHistory(@Url String beaconLayoutUrl, @Body HistoryBody body) {
        //TODO
        ResolveResponse response = new ResolveResponse.Builder().build();
        return delegate.returningResponse(response).publishHistory(beaconLayoutUrl, body);
    }

    @Override
    public Call<SettingsResponse> getSettings() {
        //TODO
        SettingsResponse response = new SettingsResponse(0, new Settings());
        return delegate.returningResponse(response).getSettings("");
    }

}
