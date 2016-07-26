package com.sensorberg.sdk.internal.http;

import com.sensorberg.sdk.internal.transport.CallbackWithRetry;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.support.test.runner.AndroidJUnit4;

import java.io.UnsupportedEncodingException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.mock.Calls;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(AndroidJUnit4.class)
public class CallbackWithRetryShould {

    RetrofitApiServiceImpl mockRetrofitApiService;

    @Before
    public void setUp() throws Exception {
        mockRetrofitApiService = mock(RetrofitApiServiceImpl.class);
    }

    @Test
    public void callback_with_retry_should_retry_3_times() throws Exception {
        Call<SettingsResponse> exceptionResponse = Calls.failure(new UnsupportedEncodingException());
        Mockito.when(mockRetrofitApiService.getSettings()).thenReturn(exceptionResponse);
        Call<SettingsResponse> call = mockRetrofitApiService.getSettings();
        Callback<SettingsResponse> mockedOriginalCallback = Mockito.spy(Callback.class);
        CallbackWithRetry<SettingsResponse> retryCallback = new CallbackWithRetry<>(mockedOriginalCallback);
        CallbackWithRetry<SettingsResponse> spiedRetryCallback = Mockito.spy(retryCallback);

        call.enqueue(spiedRetryCallback);

        Mockito.verify(mockedOriginalCallback, times(1)).onFailure(any(Call.class), any(Exception.class));
        Mockito.verify(mockedOriginalCallback, times(0)).onResponse(any(Call.class), any(Response.class));
        Mockito.verify(spiedRetryCallback, times(3)).onFailure(any(Call.class), any(Exception.class));
        Mockito.verify(spiedRetryCallback, times(0)).onResponse(any(Call.class), any(Response.class));
    }
}
