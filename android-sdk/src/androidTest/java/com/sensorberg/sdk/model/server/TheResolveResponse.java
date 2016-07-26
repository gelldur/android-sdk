package com.sensorberg.sdk.model.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.action.ActionFactory;
import com.sensorberg.sdk.di.TestComponent;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import javax.inject.Inject;

import util.TestConstants;
import util.Utils;

@RunWith(AndroidJUnit4.class)
public class TheResolveResponse {

    @Inject
    Gson gson;

    private static final JsonObject ANY_IN_APP_JSON = new JsonObject();

    static {
        try {
            ANY_IN_APP_JSON.addProperty("url", "sensorberg://");
        } catch (Exception e) {
            System.err.print("exception adding property to JsonObject = " + e.getMessage());
        }
    }

    private static final ResolveResponse PUBLISH_HISTORY_RESPONSE = new ResolveResponse.Builder()
            .withInstantActions(Arrays.asList(
                    new ResolveAction.Builder()
                            .withBeacons(Arrays.asList(TestConstants.ANY_BEACON_ID.getPid()))
                            .withType(ActionFactory.ServerType.IN_APP)
                            .withUuid(UUID.randomUUID().toString())
                            .withContent(ANY_IN_APP_JSON)
                            .build()
            ))
            .build();

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
    }

    @Test
    public void should_parse_response_from_the_resolver() throws IOException {
        ResolveResponse tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.response_layout_parse_error,
                InstrumentationRegistry.getContext()), ResolveResponse.class);
        Assertions.assertThat(tested).isNotNull();
    }

    @Test
    public void test_resolve_response_gson_serialization() throws Exception {
        String json = gson.toJson(PUBLISH_HISTORY_RESPONSE);
        ResolveResponse response = gson.fromJson(json, ResolveResponse.class);
        Assertions.assertThat(json).isNotEmpty();
        Assertions.assertThat(response).isNotNull().isEqualsToByComparingFields(PUBLISH_HISTORY_RESPONSE);
    }
}
