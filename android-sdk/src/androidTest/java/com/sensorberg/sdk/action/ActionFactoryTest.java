package com.sensorberg.sdk.action;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;
import com.sensorberg.sdk.internal.transport.HeadersJsonObjectRequest;
import com.sensorberg.sdk.model.server.ResolveAction;
import com.sensorberg.sdk.resolver.BeaconEvent;

import util.Utils;

import org.fest.assertions.api.Assertions;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class ActionFactoryTest {

    private static final int[] payloadSamples = new int[]{
            com.sensorberg.sdk.test.R.raw.action_factory_payload_001_array,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_002_object,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_004_integer,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_003_boolean,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_005_string,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_006_double,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_008_integer_with_exponent,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_009_empty_string,
    };

    private static final int[] payloadSamplesFromFutureRelease = new int[]{
            com.sensorberg.sdk.test.R.raw.action_factory_payload_010_silent_campaign,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_010_silent_campaign_no_content_map,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_010_silent_campaign_null_map,
    };
    private Context context = InstrumentationRegistry.getContext();
    private Gson gson = HeadersJsonObjectRequest.gson;

    @Test
    public void should_parse_all_non_null_values() throws Exception {
        for (int i : payloadSamples) {
            Action action = getAction(i);
            Assertions.assertThat(action.getPayload()).isNotNull();
        }
    }

    @Test
    public void should_not_fail_on_future_releases() throws Exception {
        for (int i : payloadSamplesFromFutureRelease) {
            Action action = getAction(i);
            Assertions.assertThat(action).isNull();
        }
    }

    @Test
    public void should_parse_null_payloads() throws IOException, JSONException {
        Action action =getAction(com.sensorberg.sdk.test.R.raw.action_factory_payload_007_null);
        Assertions.assertThat(action.getPayload()).isNull();
    }

    private Action getAction(int resourceID) throws IOException {
        String string = Utils.getRawResourceAsString(resourceID, context);
        ResolveAction input = gson.fromJson(string, ResolveAction.class);
        BeaconEvent beaconEvent = ResolveAction.BEACON_EVENT_MAPPER.map(input);
        return beaconEvent != null ? beaconEvent.getAction() : null;
    }

}