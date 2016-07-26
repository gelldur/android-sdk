package com.sensorberg.sdk.internal.transport;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallbackWithRetry<T> implements Callback<T> {

    private static final int TOTAL_RETRIES = 2;

    private final Callback<T> callback;

    private int retryCount = 0;

    public CallbackWithRetry(Callback<T> cbk) {
        callback = cbk;
    }

    private void retry(Call<T> call) {
        retryCount++;
        call.clone().enqueue(this);
    }

    private boolean willRetry() {
        return retryCount < TOTAL_RETRIES;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        callback.onResponse(call, response);
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if (willRetry()) {
            retry(call);
        } else {
            callback.onFailure(call, t);
        }
    }
}
