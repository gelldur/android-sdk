package com.sensorberg.sdk.model.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.testUtils.TestClock;

import org.fest.assertions.api.Assertions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;
import javax.inject.Named;

import util.Utils;

@RunWith(AndroidJUnit4.class)
public class ResolveActionTest {

    @Inject
    Gson gson;

    @Inject
    @Named("testClock")
    TestClock testClock;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
    }

    @Test
    public void test_should_be_parseable() throws Exception {
        ResolveAction tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_action_001, InstrumentationRegistry
                .getContext()), ResolveAction.class);

        Assertions.assertThat(tested.getContent().get("url")).isNotNull();
        Assertions.assertThat(tested.getContent().get("payload")).isNotNull();
        JsonObject payload = tested.getContent().get("payload").getAsJsonObject();

        Assertions.assertThat(payload.get("string").getAsString()).isEqualTo("string");
        Assertions.assertThat(payload.get("integer").getAsInt()).isEqualTo(123456);
        Assertions.assertThat(payload.get("double").getAsDouble()).isEqualTo(1.2345);
        Assertions.assertThat(payload.get("long").getAsLong()).isEqualTo(9223372036854775806L);
        Assertions.assertThat(payload.get("longWithE").getAsLong()).isEqualTo(9223372036000000000L);
        Assertions.assertThat(payload.get("doubleWithE").getAsDouble()).isEqualTo(0.00014);

        Assertions.assertThat(payload.get("true").getAsBoolean()).isTrue();
        Assertions.assertThat(payload.get("false").getAsBoolean()).isFalse();

        Assertions.assertThat(payload.get("null").isJsonNull());
        Assertions.assertThat(payload.get("object").getAsJsonObject().get("foo").getAsString()).isEqualTo("bar");
        Assertions.assertThat(payload.get("array").getAsJsonArray().size()).isEqualTo(5);
    }

    @Test
    public void test_should_be_parcelable_as_a_list() throws Exception {
        ResolveAction[] tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_action_002,
                InstrumentationRegistry.getContext()), ResolveAction[].class);

        Assertions.assertThat(tested).hasSize(2);

        Assertions.assertThat(tested[0].getContent().get("url")).isNotNull();
        Assertions.assertThat(tested[1].getContent().get("url")).isNotNull();
    }

    @Test
    public void test_should_convert_from_gmt_to_local_timezone() throws Exception {
        ResolveAction[] tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_action_with_timeframes_in_gmt,
                InstrumentationRegistry.getContext()), ResolveAction[].class);

        Assertions.assertThat(tested).hasSize(2);
        ResolveAction action1 = tested[0];
        ResolveAction action2 = tested[1];

        //action TimeFrames get converted into local time
        //we compare against System.currentTimeMillis() which also starts from GMT and adds default device time zone
        //as well as DateTime that we use to test here

        //action 1 is active between 9:15 and 17:15 GMT, May 10, 2016
        //action 2 is active between 14:15 and 18:15 GMT, May 10, 2016
        testClock.setNowInMillis(new DateTime(2016, 5, 10, 9, 30, 0, DateTimeZone.UTC).getMillis()); //local time
        Assertions.assertThat(action1.isValidNow(testClock.now())).isTrue();
        Assertions.assertThat(action2.isValidNow(testClock.now())).isFalse();

        //action 1 is not active
        //action 2 is active between 14:15 and 18:15 GMT, May 11, 2016
        testClock.setNowInMillis(new DateTime(2016, 5, 11, 15, 30, 0, DateTimeZone.UTC).getMillis());//local time
        Assertions.assertThat(action1.isValidNow(testClock.now())).isFalse();
        Assertions.assertThat(action2.isValidNow(testClock.now())).isTrue();
    }

    @Test
    public void test_should_use_cest_as_local_timezone() throws Exception {
        ResolveAction[] tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_action_with_timeframes_in_cest,
                InstrumentationRegistry.getContext()), ResolveAction[].class);

        Assertions.assertThat(tested).hasSize(2);
        ResolveAction action1 = tested[0];
        ResolveAction action2 = tested[1];

        //action TimeFrames arrive in CEST
        //we compare against System.currentTimeMillis() which also starts from GMT and adds default device time zone
        //as well as DateTime that we use to test here

        //action 1 is active between 9:15 and 17:15 CEST, May 10, 2016
        //action 2 is active between 14:15 and 18:15 CEST, May 10, 2016
        testClock.setNowInMillis(new DateTime(2016, 5, 10, 16, 0, 0, DateTimeZone.UTC).getMillis()); //local time
        Assertions.assertThat(action1.isValidNow(testClock.now())).isFalse();
        Assertions.assertThat(action2.isValidNow(testClock.now())).isTrue();

        //action 1 is not active
        //action 2 is active between 14:15 and 18:15 CEST, May 11, 2016
        testClock.setNowInMillis(new DateTime(2016, 5, 11, 14, 0, 0, DateTimeZone.UTC).getMillis());//local time
        Assertions.assertThat(action1.isValidNow(testClock.now())).isFalse();
        Assertions.assertThat(action2.isValidNow(testClock.now())).isTrue();
    }
}
