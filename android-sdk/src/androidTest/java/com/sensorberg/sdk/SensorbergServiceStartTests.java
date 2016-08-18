package com.sensorberg.sdk;

import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Platform;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import util.TestConstants;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;

@RunWith(AndroidJUnit4.class)
public class SensorbergServiceStartTests {

    private SensorbergService tested;

    private Intent sensorbergServiceStartIntent;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        tested = spy(new SensorbergService());
        tested.onCreate();
        tested.bluetoothPlatform = Mockito.mock(BluetoothPlatform.class);
        tested.platform = Mockito.mock(Platform.class);

        sensorbergServiceStartIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        sensorbergServiceStartIntent.putExtra(SensorbergServiceMessage.EXTRA_API_KEY, TestConstants.API_TOKEN_DEFAULT);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void should_not_start_if_BLE_not_supported() throws Exception {
        Mockito.when(tested.bluetoothPlatform.isBluetoothLowEnergySupported()).thenReturn(false);

        tested.onStartCommand(sensorbergServiceStartIntent, -1, -1);

        Mockito.verify(tested, Mockito.times(1)).stopSensorbergService();
    }

    @Test
    public void should_not_start_if_BLE_is_not_turned_on() throws Exception {
        Mockito.when(tested.bluetoothPlatform.isBluetoothLowEnergyDeviceTurnedOn()).thenReturn(false);

        tested.onStartCommand(sensorbergServiceStartIntent, -1, -1);

        Mockito.verify(tested, Mockito.times(1)).stopSensorbergService();
    }

    @Test
    public void should_not_start_if_location_is_not_turned_on() throws Exception {
        Mockito.when(tested.bluetoothPlatform.isLocationServicesEnabled()).thenReturn(false);

        tested.onStartCommand(sensorbergServiceStartIntent, -1, -1);

        Mockito.verify(tested, Mockito.times(1)).stopSensorbergService();
    }

    @Test
    public void should_not_start_if_no_broadcast_receivers_registered() throws Exception {
        Mockito.when(tested.platform.registerBroadcastReceiver()).thenReturn(false);

        tested.onStartCommand(sensorbergServiceStartIntent, -1, -1);

        Mockito.verify(tested, Mockito.times(1)).logError(anyString());
        Mockito.verify(tested, Mockito.times(1)).stopSensorbergService();
    }

    @Test
    public void should_restart_service_if_intent_is_null() throws Exception {
        Mockito.when(tested.bluetoothPlatform.isBluetoothLowEnergySupported()).thenReturn(true);
        Mockito.when(tested.platform.registerBroadcastReceiver()).thenReturn(true);

        tested.onStartCommand(null, -1, -1);

        Mockito.verify(tested, Mockito.times(1)).restartSensorbergService();
    }

    @Test
    public void should_handle_intent_not_null() throws Exception {
        Mockito.when(tested.bluetoothPlatform.isBluetoothLowEnergySupported()).thenReturn(true);
        Mockito.when(tested.platform.registerBroadcastReceiver()).thenReturn(true);

        tested.onStartCommand(sensorbergServiceStartIntent, -1, -1);

        Mockito.verify(tested, Mockito.times(1)).handleIntent(sensorbergServiceStartIntent);
    }
}
