package com.sensorberg.sdk.internal.transport.interfaces;

import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.model.server.ResolveResponse;

import java.util.SortedMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface RetrofitApiServiceV1 {

    @GET("/api/v1/sdk/gateways/{apiKey}/interactions.json")
    @Headers("Cache-Control: max-age=0")
    Call<BaseResolveResponse> updateBeaconLayout(@Path("apiKey") String apiKey, @QueryMap SortedMap<String, String> attributes);

    @GET("/api/v1/sdk/gateways/{apiKey}/interactions.json")
    Call<ResolveResponse> getBeacon(@Header("X-pid") String beaconId, @Header("X-qos") String networkInfo, @Path("apiKey") String apiKey, @QueryMap SortedMap<String, String> attributes);

    @POST("/api/v1/sdk/events.json")
    Call<String> publishHistory(@Body HistoryBody body);

    @GET("/api/v1/sdk/gateways/{apiKey}/settings.json?platform=android")
    Call<SettingsResponse> getSettings(@Path("apiKey") String apiKey);
}