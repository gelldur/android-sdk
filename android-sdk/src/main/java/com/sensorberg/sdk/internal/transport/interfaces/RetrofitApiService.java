package com.sensorberg.sdk.internal.transport.interfaces;

import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.model.server.ResolveResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitApiService {

    @GET("layout/")
    Call<BaseResolveResponse> updateBeaconLayout();

    @GET("layout/")
    Call<ResolveResponse> getBeacon(@Header("X-pid") String beaconId, @Header("X-qos") String networkInfo);

    @POST("layout/")
    Call<ResolveResponse> publishHistory(@Body HistoryBody body);

    @GET("applications/{apiKey}/settings/android")
    Call<SettingsResponse> getSettings(@Path("apiKey") String apiKey);
}
