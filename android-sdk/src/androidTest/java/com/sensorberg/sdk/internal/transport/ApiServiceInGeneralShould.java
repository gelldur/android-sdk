package com.sensorberg.sdk.internal.transport;

import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Named;

import util.TestConstants;

@RunWith(AndroidJUnit4.class)
public class ApiServiceInGeneralShould {

    @Inject
    Gson gson;

    @Inject
    @Named("androidPlatformIdentifier")
    PlatformIdentifier realPlatformIdentifier;

    RetrofitApiServiceImpl realRetrofitApiService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        realRetrofitApiService = new RetrofitApiServiceImpl(null, gson, realPlatformIdentifier, "https://test.comxxx");
        realRetrofitApiService.setApiToken(TestConstants.API_TOKEN_DEFAULT);
    }

    @Test
    public void apiservice_should_throw_an_unknown_host_exception() throws Exception {
        exception.expect(UnknownHostException.class);
        realRetrofitApiService.getSettings().execute();
    }

}
