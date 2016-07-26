package com.sensorberg.sdk;

import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.test.TestGenericBroadcastReceiver;
import com.sensorberg.sdk.test.TestGenericBroadcastReceiver2;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.content.Intent;
import android.os.Handler;
import android.os.Messenger;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import util.TestConstants;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;

@RunWith(AndroidJUnit4.class)
public class SensorbergServiceMessengerListTests {

    private SensorbergService tested;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        tested = new SensorbergService();
        tested.onCreate();

        Intent startIntent = SensorbergServiceIntents.getStartServiceIntent(InstrumentationRegistry.getContext(), TestConstants.API_TOKEN_DEFAULT);
        tested.onStartCommand(startIntent, -1, -1);

        TestGenericBroadcastReceiver.reset();
        TestGenericBroadcastReceiver2.reset();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void should_add_messenger() {
        Messenger messenger = new Messenger(new Handler(InstrumentationRegistry.getContext().getMainLooper()));
        SensorbergService.MessengerList list = spy(tested.presentationDelegates);
        tested.bootstrapper = spy(tested.bootstrapper);

        Assertions.assertThat(list.getSize()).isEqualTo(0);

        list.add(messenger);

        Assertions.assertThat(list.getSize()).isEqualTo(1);
        Mockito.verify(list, Mockito.times(1)).add(messenger);
        Mockito.verify(tested.bootstrapper, Mockito.times(1)).sentPresentationDelegationTo(any(SensorbergService.MessengerList.class));
    }

    @Test
    public void should_remove_messenger() {
        Messenger messenger = new Messenger(new Handler(InstrumentationRegistry.getContext().getMainLooper()));
        SensorbergService.MessengerList list = spy(tested.presentationDelegates);
        tested.bootstrapper = spy(tested.bootstrapper);

        Assertions.assertThat(list.getSize()).isEqualTo(0);

        list.add(messenger);
        list.remove(messenger);

        Assertions.assertThat(list.getSize()).isEqualTo(0);
        Mockito.verify(list, Mockito.times(1)).remove(messenger);
        //called once on add with messenger ref, and once on remove with null ref
        Mockito.verify(tested.bootstrapper, Mockito.times(2)).sentPresentationDelegationTo(any(SensorbergService.MessengerList.class));
    }
}
