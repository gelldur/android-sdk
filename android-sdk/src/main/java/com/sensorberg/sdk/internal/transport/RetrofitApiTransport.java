package com.sensorberg.sdk.internal.transport;

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
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.model.server.ResolveAction;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.receivers.NetworkInfoBroadcastReceiver;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.ScanEvent;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sensorberg.utils.ListUtils.map;

public class RetrofitApiTransport implements Transport {

    public static String RESOLVER_BASE_URL = "https://demo.sensorberg-cdn.com";

    public static int BACKEND_VERSION = 2;

    private final Clock mClock;

    private RetrofitApiServiceImpl apiService;

    @Setter
    private BeaconHistoryUploadIntervalListener beaconHistoryUploadIntervalListener = BeaconHistoryUploadIntervalListener.NONE;

    private ProximityUUIDUpdateHandler mProximityUUIDUpdateHandler = ProximityUUIDUpdateHandler.NONE;

    public RetrofitApiTransport(RetrofitApiServiceImpl retrofitApiService, Clock clk) {
        apiService = retrofitApiService;
        mClock = clk;
    }

    private RetrofitApiServiceImpl getApiService() {
        return apiService;
    }

    @Override
    public void setProximityUUIDUpdateHandler(ProximityUUIDUpdateHandler proximityUUIDUpdateHandler) {
        if (proximityUUIDUpdateHandler != null) {
            mProximityUUIDUpdateHandler = proximityUUIDUpdateHandler;
        } else {
            mProximityUUIDUpdateHandler = ProximityUUIDUpdateHandler.NONE;
        }
    }

    @Override
    public void getBeacon(final ScanEvent scanEvent, SortedMap<String, String> attributes, final BeaconResponseHandler beaconResponseHandler) {
        String networkInfo = NetworkInfoBroadcastReceiver.latestNetworkInfo != null
                ? NetworkInfoBroadcastReceiver.getNetworkInfoString() : "";

        Call<ResolveResponse> call = getApiService().getBeacon(scanEvent.getBeaconId().getPid(), networkInfo, attributes);

        call.enqueue(new Callback<ResolveResponse>() {
            @Override
            public void onResponse(Call<ResolveResponse> call, Response<ResolveResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BeaconEvent> beaconEvents = checkSuccessfulBeaconResponse(scanEvent, response.body());
                    beaconResponseHandler.onSuccess(beaconEvents);
                    checkShouldCallBeaconResponseHandlers(response.body());
                } else {
                    beaconResponseHandler.onFailure(new Throwable("No Content, Invalid Api Key"));
                }
            }

            @Override
            public void onFailure(Call<ResolveResponse> call, Throwable t) {
                beaconResponseHandler.onFailure(t);
            }
        });
    }

    private void checkShouldCallBeaconResponseHandlers(ResolveResponse successfulResponse) {
        mProximityUUIDUpdateHandler.proximityUUIDListUpdated(successfulResponse.getAccountProximityUUIDs());

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

        HistoryBody body = new HistoryBody(scans, actions, conversions, mClock);
        Call<String> call = getApiService().publishHistory(body);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(scans, actions, conversions);
                    if (response.body() != null) {
                        try {
                            ResolveResponse resolveResponse = apiService.mGson.fromJson(response.body(), ResolveResponse.class);
                            callback.onInstantActions(resolveResponse.getInstantActionsAsBeaconEvent());
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
            public void onFailure(Call<String> call, Throwable t) {
                callback.onFailure(new Exception(t));
            }
        });
    }

    @Override
    public void updateBeaconLayout(SortedMap<String, String> attributes) {

        Call<BaseResolveResponse> call = getApiService().updateBeaconLayout(attributes);

        call.enqueue(new Callback<BaseResolveResponse>() {
            @Override
            public void onResponse(Call<BaseResolveResponse> call, Response<BaseResolveResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mProximityUUIDUpdateHandler.proximityUUIDListUpdated(response.body().getAccountProximityUUIDs());
                } else {
                    mProximityUUIDUpdateHandler.proximityUUIDListUpdated(Collections.EMPTY_LIST);
                }
            }

            @Override
            public void onFailure(Call<BaseResolveResponse> call, Throwable t) {
                mProximityUUIDUpdateHandler.proximityUUIDListUpdated(Collections.EMPTY_LIST);
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
