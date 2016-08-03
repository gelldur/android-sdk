package com.sensorberg.sdk;

import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolverConfiguration;
import com.sensorberg.sdk.scanner.BeaconActionHistoryPublisher;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.sensorberg.sdk.testUtils.TestHandlerManager;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;
import com.sensorberg.utils.ListUtils;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.SharedPreferences;
import android.support.test.runner.AndroidJUnit4;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import static org.mockito.Mockito.spy;

@RunWith(AndroidJUnit4.class)
public class TheInternalApplicationBootstrapperShould {

    private static final java.util.UUID UUID = java.util.UUID.randomUUID();

    private static final long SUPPRESSION_TIME = 10000;

    @Inject
    TestServiceScheduler testServiceScheduler;

    @Inject
    TestHandlerManager testHandlerManager;

    @Inject
    @Named("testBluetoothPlatform")
    BluetoothPlatform bluetoothPlatform;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    @Named("realBeaconActionHistoryPublisher")
    BeaconActionHistoryPublisher beaconActionHistoryPublisher;

    InternalApplicationBootstrapper tested;

    private BeaconEvent beaconEventSupressionTime;

    private BeaconEvent beaconEventSentOnlyOnce;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        beaconActionHistoryPublisher.deleteAllData();

        tested = spy(new InternalApplicationBootstrapper(new DumbSucessTransport(), testServiceScheduler, testHandlerManager,
                testHandlerManager.getCustomClock(), bluetoothPlatform, new ResolverConfiguration()));

        beaconEventSupressionTime = new BeaconEvent.Builder()
                .withAction(new InAppAction(UUID, "irrelevant", "irrelevant", null, null, 0))
                .withSuppressionTime(SUPPRESSION_TIME)
                .withPresentationTime(0)
                .build();

        beaconEventSentOnlyOnce = new BeaconEvent.Builder()
                .withAction(new InAppAction(UUID, "irrelevant", "irrelevant", null, null, 0))
                .withSendOnlyOnce(true)
                .withPresentationTime(0)
                .build();
    }

    @Test
    public void beaconEventFilterShouldHaveOneSuppressionTimeEvent() throws Exception {
        List<BeaconEvent> events = ListUtils.filter(Arrays.asList(beaconEventSupressionTime), tested.beaconEventFilter);
        Assertions.assertThat(events.size()).isEqualTo(1);
    }

    @Test
    public void beaconEventFilterShouldHaveEndOfSuppressionTimeEvent() {
        List<BeaconEvent> events = ListUtils.filter(Arrays.asList(beaconEventSupressionTime), tested.beaconEventFilter);
        Assertions.assertThat(events.size()).isEqualTo(1);

        testHandlerManager.getCustomClock().setNowInMillis(SUPPRESSION_TIME + 1);

        List<BeaconEvent> eventsWithSuppressionEvent = ListUtils.filter(Arrays.asList(beaconEventSupressionTime), tested.beaconEventFilter);
        Assertions.assertThat(eventsWithSuppressionEvent.size()).isEqualTo(1);
    }

    @Test
    public void beaconEventFilterShouldHaveSendOnlyOnceEvent() {
        List<BeaconEvent> events = ListUtils.filter(Arrays.asList(beaconEventSentOnlyOnce), tested.beaconEventFilter);
        Assertions.assertThat(events.size()).isEqualTo(1);
    }

    public void shouldReturnSyncEnabled() {
        Assertions.assertThat(tested.isSyncEnabled()).isTrue();
    }
}
