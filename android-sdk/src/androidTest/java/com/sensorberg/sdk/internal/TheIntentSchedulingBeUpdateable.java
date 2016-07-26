package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.settings.TimeConstants;
import com.sensorberg.sdk.test.RepeatFlaky;
import com.sensorberg.sdk.test.RepeatFlakyRule;
import com.sensorberg.sdk.test.TestGenericBroadcastReceiver;
import com.sensorberg.sdk.testUtils.TestClock;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.AlarmManager;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

@RunWith(AndroidJUnit4.class)
public class TheIntentSchedulingBeUpdateable {

    @Rule
    public RepeatFlakyRule mRepeatFlakyRule = new RepeatFlakyRule();

    @Inject
    AlarmManager alarmManager;

    @Inject
    @Named("testClock")
    TestClock androidClock;

    @Inject
    PersistentIntegerCounter persistentIntegerCounter;

    TestServiceScheduler testServiceScheduler;

    private Bundle INTENT_BUNDLE;

    private Bundle INTENT_BUNDLE_2;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        testServiceScheduler = new TestServiceScheduler(InstrumentationRegistry.getContext(), alarmManager, androidClock, persistentIntegerCounter,
                TimeConstants.ONE_SECOND / 10);
        INTENT_BUNDLE = new Bundle();
        INTENT_BUNDLE.putString("foo", "bar");

        INTENT_BUNDLE_2 = new Bundle();
        INTENT_BUNDLE_2.putString("bar", "foo");
        TestGenericBroadcastReceiver.reset();

    }

    @Test
    @RepeatFlaky(times = 5)
    public void testShouldUpdateAnIntent() throws InterruptedException {
        long time = System.currentTimeMillis();
        long index = System.currentTimeMillis();
        testServiceScheduler.scheduleIntent(index, 500L, INTENT_BUNDLE);
        testServiceScheduler.scheduleIntent(index, 500L, INTENT_BUNDLE_2);

        boolean intentFired = TestGenericBroadcastReceiver.getLatch().await(10, TimeUnit.SECONDS);
        Assertions.assertThat(intentFired)
                .overridingErrorMessage("The intent was not fired")
                .isTrue();
        Assertions.assertThat(TestGenericBroadcastReceiver.getIntent().getStringExtra("bar"))
                .overridingErrorMessage("the second scheduled intent should have been fired")
                .isNotNull()
                .isEqualTo("foo");
        long elapestime = System.currentTimeMillis() - time;
        Log.d("TEST", "The exact time was" + elapestime + "millis");
    }
}
