package com.sensorberg.sdk.action;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import com.google.gson.Gson;
import com.sensorberg.sdk.internal.transport.HeadersJsonObjectRequest;
import com.sensorberg.sdk.model.server.ResolveAction;
import com.sensorberg.sdk.resolver.BeaconEvent;

import util.Utils;

import org.fest.assertions.api.Assertions;
import org.fest.assertions.data.Offset;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.sensorberg.sdk.test.R.raw.action_factory_payload_001_array;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_002_object;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_003_boolean;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_004_integer;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_005_string;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_006_double;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_008_integer_with_exponent;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_009_empty_string;

@RunWith(AndroidJUnit4.class)
public class TheActionShould extends AndroidTestCase {

    private Context context = InstrumentationRegistry.getContext();
    private Gson gson = HeadersJsonObjectRequest.gson;


    @Test(expected = JSONException.class)
    public void test_not_parse_an_array_as_an_object() throws IOException, JSONException {
        Action arrayPayloadAction = getAction(action_factory_payload_001_array);
        arrayPayloadAction.getPayloadJSONObject();
    }

    @Test(expected = JSONException.class)
    public void test_not_parse_an_object_as_an_array() throws IOException, JSONException {
        Action arrayPayloadAction = getAction(action_factory_payload_002_object);
        arrayPayloadAction.getPayloadJSONArray();
    }

    @Test
    public void test_allow_parsing_of_booleans() throws IOException, JSONException {
        Action arrayPayloadAction = getAction(action_factory_payload_003_boolean);

        Boolean output = Boolean.valueOf(arrayPayloadAction.getPayload());
        Assertions.assertThat(output).isEqualTo(true);
    }

    @Test
    public void test_allow_parsing_of_integer() throws IOException, JSONException {
        Action arrayPayloadAction = getAction(action_factory_payload_004_integer);

        Integer output = Integer.valueOf(arrayPayloadAction.getPayload());
        Assertions.assertThat(output).isEqualTo(1337);
    }

    @Test
    public void test_allow_parsing_of_strings() throws IOException, JSONException {
        Action arrayPayloadAction = getAction(action_factory_payload_005_string);


        String output = arrayPayloadAction.getPayload();
        Assertions.assertThat(output).isEqualTo("foo");
    }

    @Test
    public void test_allow_parsing_of_double_values() throws IOException, JSONException {
        Action arrayPayloadAction = getAction(action_factory_payload_006_double);

        Double output = Double.valueOf(arrayPayloadAction.getPayload());
        Assertions.assertThat(output).isEqualTo(1.2345, Offset.offset(0.00001));
    }

    @Test
    public void test_allow_parsing_of_integerWithExponentValue() throws IOException, JSONException {
        Action arrayPayloadAction = getAction(action_factory_payload_008_integer_with_exponent);

        Double output = Double.valueOf(arrayPayloadAction.getPayload());
        Assertions.assertThat(output).isEqualTo(1337, Offset.offset(0.1));
    }

    @Test
    public void test_allow_parsing_of_emptyString() throws IOException, JSONException {
        Action arrayPayloadAction = getAction(action_factory_payload_009_empty_string);

        Assertions.assertThat(arrayPayloadAction.getPayload()).isNotNull().hasSize(0);
    }

    private Action getAction(int resourceID) throws IOException {
        String string = Utils.getRawResourceAsString(resourceID, context);
        ResolveAction input = gson.fromJson(string, ResolveAction.class);
        BeaconEvent beaconEvent = ResolveAction.BEACON_EVENT_MAPPER.map(input);
        return beaconEvent != null ? beaconEvent.getAction() : null;
    }
}
