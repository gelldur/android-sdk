package com.sensorberg.sdk.internal.transport;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.BeaconHistoryUploadIntervalListener;
import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportHistoryCallback;
import com.sensorberg.sdk.internal.transport.interfaces.TransportSettingsCallback;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;
import com.sensorberg.sdk.model.persistence.ActionConversion;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.model.server.ResolveAction;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.receivers.NetworkInfoBroadcastReceiver;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.ScanEvent;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import lombok.Setter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sensorberg.sdk.settings.SharedPreferencesKeys.Network.KEY_RESOLVE_RESPONSE;
import static com.sensorberg.utils.ListUtils.map;

public class RetrofitApiTransport implements Transport {

    public static String RESOLVER_BASE_URL = "https://portal.sensorberg-cdn.com";

    public static int BACKEND_VERSION = 2;

    private final Clock mClock;

    private RetrofitApiServiceImpl apiService;

    private ResolveResponse lastSuccess;

    private SharedPreferences prefs;

    private Gson gson;

    @Setter
    private BeaconHistoryUploadIntervalListener beaconHistoryUploadIntervalListener = BeaconHistoryUploadIntervalListener.NONE;

    private ProximityUUIDUpdateHandler mProximityUUIDUpdateHandler = ProximityUUIDUpdateHandler.NONE;

    public RetrofitApiTransport(RetrofitApiServiceImpl retrofitApiService, Clock clk, SharedPreferences sharedPreferences, Gson gson) {
        apiService = retrofitApiService;
        mClock = clk;
        prefs = sharedPreferences;
        this.gson = gson;
        load();
    }

    private RetrofitApiServiceImpl getApiService() {
        return apiService;
    }

    private void load() {
        String json = prefs.getString(KEY_RESOLVE_RESPONSE, null);
        if (!TextUtils.isEmpty(json)) {
            lastSuccess = gson.fromJson(json, ResolveResponse.class);
        }
    }

    private void save(ResolveResponse body) {
        lastSuccess = body;
        prefs.edit().putString(KEY_RESOLVE_RESPONSE, gson.toJson(lastSuccess)).apply();
    }

    @Override
    public void setProximityUUIDUpdateHandler(ProximityUUIDUpdateHandler proximityUUIDUpdateHandler) {
        if (proximityUUIDUpdateHandler != null) {
            mProximityUUIDUpdateHandler = proximityUUIDUpdateHandler;
        } else {
            mProximityUUIDUpdateHandler = ProximityUUIDUpdateHandler.NONE;
        }
    }

    private boolean isModified(Response<ResolveResponse> response) {
        String header = response.headers().get(RetrofitApiServiceImpl.OKHTTP_HEADER);
        return !String.valueOf(HttpURLConnection.HTTP_NOT_MODIFIED).equals(header);
    }

    @Override
    public void getBeacon(final ScanEvent scanEvent, SortedMap<String, String> attributes, final BeaconResponseHandler beaconResponseHandler) {
        String networkInfo = NetworkInfoBroadcastReceiver.latestNetworkInfo != null
                ? NetworkInfoBroadcastReceiver.getNetworkInfoString() : "";

        getApiService()
                .getBeacon(scanEvent.getBeaconId().getPid(), networkInfo, attributes)
                .enqueue(new Callback<ResolveResponse>() {
                    @Override
                    public void onResponse(Call<ResolveResponse> call, Response<ResolveResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            save(response.body());
                            onSuccess(response.body(), isModified(response));
                        } else {
                            onFail(new Throwable("No Content, Invalid Api Key"));
                        }
                    }

                    @Override
                    public void onFailure(Call<ResolveResponse> call, Throwable t) {
                        onFail(t);
                    }

                    void onSuccess(ResolveResponse resolveResponse, boolean changed) {
                        List<BeaconEvent> beaconEvents = checkSuccessfulBeaconResponse(scanEvent, resolveResponse);
                        beaconResponseHandler.onSuccess(beaconEvents);
                        checkShouldCallBeaconResponseHandlers(resolveResponse, changed);
                    }

                    void onFail(Throwable t) {
                        if (lastSuccess == null) {
                            beaconResponseHandler.onFailure(t);
                        } else {
                            Logger.log.logError("resolution failed, but we have a backup:" + scanEvent.getBeaconId().toTraditionalString(), t);
                            onSuccess(lastSuccess, false);
                        }
                    }

                });
    }

    private void checkShouldCallBeaconResponseHandlers(ResolveResponse successfulResponse, boolean changed) {
        mProximityUUIDUpdateHandler.proximityUUIDListUpdated(successfulResponse.getAccountProximityUUIDs(), changed);

        if (successfulResponse.reportTriggerSeconds != null) {
            beaconHistoryUploadIntervalListener
                    .historyUploadIntervalChanged(TimeUnit.SECONDS.toMillis(successfulResponse.reportTriggerSeconds));
        }
    }

    private List<BeaconEvent> checkSuccessfulBeaconResponse(ScanEvent scanEvent,
                                                            ResolveResponse successfulResponse) {


        List<ResolveAction> resolveActions = successfulResponse.resolve(scanEvent, mClock.now());

        List<BeaconEvent> beaconEvents = map(resolveActions, ResolveAction.BEACON_EVENT_MAPPER);
        for (BeaconEvent beaconEvent : beaconEvents) {
            beaconEvent.setBeaconId(scanEvent.getBeaconId());
            beaconEvent.setTrigger(scanEvent.getTrigger());
            beaconEvent.setResolvedTime(mClock.now());
            beaconEvent.setGeohash(scanEvent.getGeohash());
        }

        return beaconEvents;
    }

    @Override
    public boolean setApiToken(String apiToken) {
        return getApiService().setApiToken(apiToken);
    }

    @Override
    public void loadSettings(final TransportSettingsCallback transportSettingsCallback) {
        Call<SettingsResponse> call = getApiService().getSettings();

        enqueueWithRetry(call, new Callback<SettingsResponse>() {
            @Override
            public void onResponse(Call<SettingsResponse> call, Response<SettingsResponse> response) {
                if (response.isSuccessful()) {
                    if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                        transportSettingsCallback.onSettingsFound(null);
                    } else {
                        transportSettingsCallback.onSettingsFound(response.body());
                    }
                } else {
                    if (response.code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        transportSettingsCallback.nothingChanged();
                    } else {
                        transportSettingsCallback.onFailure(new Exception());
                    }
                }
            }

            @Override
            public void onFailure(Call<SettingsResponse> call, Throwable t) {
                transportSettingsCallback.onFailure(new Exception(t));
            }
        });
    }

    @Override
    public void publishHistory(final List<BeaconScan> scans, final List<BeaconAction> actions, final List<ActionConversion> conversions, final TransportHistoryCallback callback) {

        /**
         * Unfortunately from V1 to V2 our servers changed the response to `publishHistory`
         * V1 replied a ResolveResponse
         * V2 replies with 200 and an empty body (which is better)
         * This change breaks the strongly typed philosophy of Retrofit
         * and until we can abandon V0 and V1, below is the hacky code needed.
         */

        HistoryBody body = new HistoryBody(scans, actions, conversions, mClock);
        Call<ResponseBody> call = getApiService().publishHistory(body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(scans, actions, conversions);
                    if (response.body() != null) {
                        try {
                            ResolveResponse resolveResponse = apiService.mGson.fromJson(response.body().charStream(), ResolveResponse.class);
                            if (resolveResponse != null) {
                                callback.onInstantActions(resolveResponse.getInstantActionsAsBeaconEvent());
                            }
                        } catch (Exception e) {
                            // gson serialization exception
                            Logger.log.logError("Failed to de-serialize publishHistory response body", e);
                        }
                    }
                } else {
                    callback.onFailure(new Exception("No Content, Invalid Api Key"));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure(new Exception(t));
            }
        });
    }

    @Override
    public void updateBeaconLayout(SortedMap<String, String> attributes) {
        getApiService()
                .updateBeaconLayout(attributes)
                .enqueue(new Callback<ResolveResponse>() {
                    @Override
                    public void onResponse(Call<ResolveResponse> call, Response<ResolveResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            save(response.body());
                            onSuccess(response.body(), isModified(response));
                        } else {
                            onFail(new Exception("Failed to updateBeaconLayout. Response body is empty"));
                        }
                    }

                    @Override
                    public void onFailure(Call<ResolveResponse> call, Throwable t) {
                        onFail(t);
                    }

                    void onSuccess(ResolveResponse resolveResponse, boolean changed) {
                        mProximityUUIDUpdateHandler.proximityUUIDListUpdated(resolveResponse.getAccountProximityUUIDs(), changed);
                    }

                    void onFail(Throwable t) {
                        if (lastSuccess == null) {
                            Logger.log.logError("UpdateBeaconLayout failed", t);
                            mProximityUUIDUpdateHandler.proximityUUIDListUpdated(Collections.EMPTY_LIST, true);
                        } else {
                            Logger.log.logError("UpdateBeaconLayout failed, but we have a backup", t);
                            onSuccess(lastSuccess, false);
                        }
                    }

                });
    }

    @Override
    public void setLoggingEnabled(boolean enabled) {
        getApiService().setLoggingEnabled(enabled);
    }

    public <T> void enqueueWithRetry(Call<T> call, final Callback<T> callback) {
        call.enqueue(new CallbackWithRetry<>(callback));
    }
}
