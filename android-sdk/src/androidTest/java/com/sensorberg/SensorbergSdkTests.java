package com.sensorberg;

import com.sensorberg.sdk.resolver.BeaconEvent;

import com.gdubina.multiprocesspreferences.MultiprocessPreferenceManager;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import util.TestConstants;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class SensorbergSdkTests {

    //TODO add direct SensorbergService tests - maybe write a separate TestService which will extend SensorbergService and have a Binder which we can
    //use to get a reference?

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    private SensorbergSdk tested;

    @Before
    public void setup() {
        /* this is one way of solving the "Can't create handler inside thread that has not called Looper.prepare()" error when creating
         SensorbergApplicationBootstrapper inside a test */
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                tested = Mockito.spy(new SensorbergSdk(InstrumentationRegistry.getContext(), TestConstants.API_TOKEN_DEFAULT));
                MultiprocessPreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getContext());
            }
        });
        SensorbergSdk.listeners.clear();
    }

    @Test
    public void enableService_activates_service_and_puts_it_in_foreground() {
        tested.enableService(InstrumentationRegistry.getContext(), TestConstants.API_TOKEN_DEFAULT);

        verify(tested, times(1)).activateService(TestConstants.API_TOKEN_DEFAULT);
        verify(tested, times(1)).hostApplicationInForeground();
    }

    @Test
    public void add_listener_also_registers_messenger_with_service() {
        SensorbergSdkEventListener listener = new SensorbergSdkEventListener() {
            @Override
            public void presentBeaconEvent(BeaconEvent beaconEvent) {
                //do nothing;
            }
        };

        Assertions.assertThat(SensorbergSdk.listeners.size()).isEqualTo(0);

        tested.registerEventListener(listener);

        Assertions.assertThat(SensorbergSdk.listeners.size()).isEqualTo(1);
        verify(tested, times(1)).setPresentationDelegationEnabled(true);
        verify(tested, times(1)).registerForPresentationDelegation();
    }

    @Test
    public void remove_listener_also_unregisters_messenger_with_service() {
        SensorbergSdkEventListener listener = new SensorbergSdkEventListener() {
            @Override
            public void presentBeaconEvent(BeaconEvent beaconEvent) {
                //do nothing;
            }
        };

        Assertions.assertThat(SensorbergSdk.listeners.size()).isEqualTo(0);

        tested.registerEventListener(listener);
        tested.unregisterEventListener(listener);

        Assertions.assertThat(SensorbergSdk.listeners.size()).isEqualTo(0);
        verify(tested, times(1)).setPresentationDelegationEnabled(false);
        verify(tested, times(1)).unRegisterFromPresentationDelegation();
    }

    @Test
    public void hostApplicationInBackground_also_unregisters_messenger_with_service() {
        tested.hostApplicationInBackground();

        verify(tested, times(1)).unRegisterFromPresentationDelegation();
    }

    @Test
    public void hostApplicationInForeground_with_listeners_also_registers_messenger_with_service() {
        SensorbergSdkEventListener listener = new SensorbergSdkEventListener() {
            @Override
            public void presentBeaconEvent(BeaconEvent beaconEvent) {
                //do nothing;
            }
        };

        tested.registerEventListener(listener);
        tested.hostApplicationInForeground();

        Assertions.assertThat(SensorbergSdk.listeners.size()).isEqualTo(1);
        /* called once by registerEventListener and once by hostApplicationInForeground */
        verify(tested, times(2)).registerForPresentationDelegation();
    }

    @Test
    public void hostApplicationInForeground_without_listeners_doesnt_register_messenger_with_service() {
        tested.hostApplicationInForeground();

        Assertions.assertThat(SensorbergSdk.listeners.size()).isEqualTo(0);
        verify(tested, times(0)).registerForPresentationDelegation();
    }
}
