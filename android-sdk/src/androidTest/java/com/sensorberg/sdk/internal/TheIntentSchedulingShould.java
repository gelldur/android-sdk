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

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

@RunWith(AndroidJUnit4.class)
public class TheIntentSchedulingShould {

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

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        testServiceScheduler = new TestServiceScheduler(InstrumentationRegistry.getContext(), alarmManager, androidClock, persistentIntegerCounter,
                TimeConstants.ONE_SECOND);
        INTENT_BUNDLE = new Bundle();
        INTENT_BUNDLE.putString("foo", "bar");
        TestGenericBroadcastReceiver.reset();
    }

    @Test
    @RepeatFlaky(times = 5)
    public void testShouldScheduleAnIntent() throws Exception {
        testServiceScheduler.scheduleIntent(1, 500L, INTENT_BUNDLE);

        boolean intentFired = TestGenericBroadcastReceiver.getLatch().await(10, TimeUnit.SECONDS);
        Assertions.assertThat(intentFired)
                .overridingErrorMessage("The intent was not fired")
                .isTrue();
    }

    @Test
    @RepeatFlaky(times = 5)
    public void testShouldUnScheduleAnIntent() throws Exception {
        testServiceScheduler.scheduleIntent(2, 500L, INTENT_BUNDLE);

        testServiceScheduler.unscheduleIntent(2);
        boolean intentFired = TestGenericBroadcastReceiver.getLatch().await(10, TimeUnit.SECONDS);
        Assertions.assertThat(intentFired)
                .overridingErrorMessage("The intent was fired even though it was unscheduled")
                .isFalse();
    }
}
