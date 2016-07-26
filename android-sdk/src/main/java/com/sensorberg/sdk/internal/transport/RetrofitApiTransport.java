package com.sensorberg.sdk.internal.transport;

import com.sensorberg.sdk.internal.interfaces.BeaconHistoryUploadIntervalListener;
import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportHistoryCallback;
import com.sensorberg.sdk.internal.transport.interfaces.TransportSettingsCallback;
import com.sensorberg.sdk.internal.transport.model.HistoryBody;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;
import com.sensorberg.sdk.model.server.BaseResolveResponse;
import com.sensorberg.sdk.model.server.ResolveAction;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.receivers.NetworkInfoBroadcastReceiver;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;

import android.util.Pair;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sensorberg.sdk.internal.URLFactory.getResolveURLString;
import static com.sensorberg.utils.ListUtils.map;

public class RetrofitApiTransport implements Transport {

    private final Clock mClock;

    private RetrofitApiServiceImpl apiService;

    @Setter
    private BeaconHistoryUploadIntervalListener beaconHistoryUploadIntervalListener = BeaconHistoryUploadIntervalListener.NONE;

    private ProximityUUIDUpdateHandler mProximityUUIDUpdateHandler = ProximityUUIDUpdateHandler.NONE;

    private BeaconReportHandler mBeaconReportHandler;

    public RetrofitApiTransport(RetrofitApiServiceImpl retrofitApiService, Clock clk) {
        apiService = retrofitApiService;
        mClock = clk;
    }

    private RetrofitApiServiceImpl getApiService() {
        return apiService;
    }

    @Override
    public void setBeaconReportHandler(BeaconReportHandler beaconReportHandler) {
        mBeaconReportHandler = beaconReportHandler;
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
    public void getBeacon(final ResolutionConfiguration resolutionConfiguration, final BeaconResponseHandler beaconResponseHandler) {
        String networkInfo = NetworkInfoBroadcastReceiver.latestNetworkInfo != null
                ? NetworkInfoBroadcastReceiver.getNetworkInfoString() : "";

        Call<ResolveResponse> call = getApiService()
                .getBeacon(resolutionConfiguration.getScanEvent().getBeaconId().getBid(), networkInfo);

        call.enqueue(new Callback<ResolveResponse>() {
            @Override
            public void onResponse(Call<ResolveResponse> call, Response<ResolveResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pair<Boolean, List<BeaconEvent>> beaconEventPair = checkSuccessfulBeaconResponse(resolutionConfiguration, response.body());
                    beaconResponseHandler.onSuccess(beaconEventPair.second);
                    checkShouldCallBeaconResponseHandlers(beaconEventPair.first, response.body());
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

    private void checkShouldCallBeaconResponseHandlers(boolean shouldReportImmediately, ResolveResponse successfulResponse) {
        if (shouldReportImmediately && mBeaconReportHandler != null) {
            mBeaconReportHandler.reportImmediately();
        }

        mProximityUUIDUpdateHandler.proximityUUIDListUpdated(successfulResponse.getAccountProximityUUIDs());

        if (successfulResponse.reportTriggerSeconds != null) {
            beaconHistoryUploadIntervalListener
                    .historyUploadIntervalChanged(TimeUnit.SECONDS.toMillis(successfulResponse.reportTriggerSeconds));
        }
    }

    private Pair<Boolean, List<BeaconEvent>> checkSuccessfulBeaconResponse(ResolutionConfiguration resolutionConfiguration,
            ResolveResponse successfulResponse) {

        boolean reportImmediately = false;

        List<ResolveAction> resolveActions = successfulResponse.resolve(resolutionConfiguration.getScanEvent(), mClock.now());
        for (ResolveAction resolveAction : resolveActions) {
            reportImmediately |= resolveAction.isReportImmediately();
        }

        List<BeaconEvent> beaconEvents = map(resolveActions, ResolveAction.BEACON_EVENT_MAPPER);
        for (BeaconEvent beaconEvent : beaconEvents) {
            beaconEvent.setBeaconId(resolutionConfiguration.getScanEvent().getBeaconId());
        }

        return new Pair<Boolean, List<BeaconEvent>>(reportImmediately, beaconEvents);
    }

    @Override
    public void setApiToken(String apiToken) {
        getApiService().setApiToken(apiToken);
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
    public void publishHistory(final List<BeaconScan> scans, final List<BeaconAction> actions, final TransportHistoryCallback callback) {

        HistoryBody body = new HistoryBody(scans, actions, mClock);
        Call<ResolveResponse> call = getApiService().publishHistory(body);

        call.enqueue(new Callback<ResolveResponse>() {
            @Override
            public void onResponse(Call<ResolveResponse> call, Response<ResolveResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(scans, actions);
                    callback.onInstantActions(response.body().getInstantActionsAsBeaconEvent());
                } else {
                    callback.onFailure(new Exception("No Content, Invalid Api Key"));
                }
            }

            @Override
            public void onFailure(Call<ResolveResponse> call, Throwable t) {
                callback.onFailure(new Exception(t));
            }
        });
    }

    @Override
    public void updateBeaconLayout() {

        Call<BaseResolveResponse> call = getApiService().updateBeaconLayout();

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
