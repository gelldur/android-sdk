package com.sensorberg.sdk.action;

import org.fest.assertions.api.Assertions;
import org.fest.assertions.data.Offset;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.io.IOException;

import util.Utils;

@RunWith(AndroidJUnit4.class)
public class TheActionShould {

    @Test
    public void test_not_parse_an_array_as_an_object() throws IOException, JSONException {
        Action arrayPayloadAction = ActionFactory.actionFromJSONObject(Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_payload_001_array, InstrumentationRegistry.getContext()));
        try{
            arrayPayloadAction.getPayloadJSONObject();
            Assertions.fail("there was no exception");
        } catch (JSONException e){
            //all is fine
        }
    }

    @Test
    public void test_not_parse_an_object_as_an_array() throws IOException, JSONException {
        Action arrayPayloadAction = ActionFactory.actionFromJSONObject(Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_payload_002_object, InstrumentationRegistry.getContext()));
        try{
            arrayPayloadAction.getPayloadJSONArray();
            Assertions.fail("there was no exception");
        } catch (JSONException e){
            //all is fine
        }
    }

    @Test
    public void test_allow_parsing_of_booleans() throws IOException, JSONException {
        Action arrayPayloadAction = ActionFactory.actionFromJSONObject(Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_payload_003_boolean, InstrumentationRegistry.getContext()));

        Boolean output = Boolean.valueOf(arrayPayloadAction.getPayload());
        Assertions.assertThat(output).isEqualTo(true);
    }

    @Test
    public void test_allow_parsing_of_integer() throws IOException, JSONException {
        Action arrayPayloadAction = ActionFactory.actionFromJSONObject(Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_payload_004_integer, InstrumentationRegistry.getContext()));

        Integer output = Integer.valueOf(arrayPayloadAction.getPayload());
        Assertions.assertThat(output).isEqualTo(1337);
    }

    @Test
    public void test_allow_parsing_of_strings() throws IOException, JSONException {
        Action arrayPayloadAction = ActionFactory.actionFromJSONObject(Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_payload_005_string, InstrumentationRegistry.getContext()));


        String output = arrayPayloadAction.getPayload();
        Assertions.assertThat(output).isEqualTo("foo");
    }

    @Test
    public void test_allow_parsing_of_double_values() throws IOException, JSONException {
        Action arrayPayloadAction = ActionFactory.actionFromJSONObject(Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_payload_006_double, InstrumentationRegistry.getContext()));

        Double output = Double.valueOf(arrayPayloadAction.getPayload());
        Assertions.assertThat(output).isEqualTo(1.2345, Offset.offset(0.00001));
    }

    @Test
    public void test_allow_parsing_of_integerWithExponentValue() throws IOException, JSONException {
        Action arrayPayloadAction = ActionFactory.actionFromJSONObject(Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_payload_008_integer_with_exponent, InstrumentationRegistry.getContext()));

        Double output = Double.valueOf(arrayPayloadAction.getPayload());
        Assertions.assertThat(output).isEqualTo(1337, Offset.offset(0.1));
    }

    @Test
    public void test_allow_parsing_of_emptyString() throws IOException, JSONException {
        Action arrayPayloadAction = ActionFactory.actionFromJSONObject(Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_payload_009_empty_string,
                InstrumentationRegistry.getContext()));

        Assertions.assertThat(arrayPayloadAction.getPayload()).isNotNull().hasSize(0);
    }
}
