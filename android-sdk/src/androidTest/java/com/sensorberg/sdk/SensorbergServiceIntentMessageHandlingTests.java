package com.sensorberg.sdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.scanner.ScannerEvent;
import com.sensorberg.sdk.test.TestGenericBroadcastReceiver;
import com.sensorberg.sdk.test.TestGenericBroadcastReceiver2;
import com.sensorberg.sdk.testUtils.TestBluetoothPlatform;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import util.TestConstants;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;

@RunWith(AndroidJUnit4.class)
public class SensorbergServiceIntentMessageHandlingTests {

    private SensorbergService tested;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        tested = spy(new SensorbergService());
        tested.onCreate();
        tested.bluetoothPlatform = new TestBluetoothPlatform();

        Intent startIntent = SensorbergServiceIntents.getStartServiceIntent(InstrumentationRegistry.getContext(), TestConstants.API_TOKEN_DEFAULT);

        tested.onStartCommand(startIntent, -1, -1);
        tested.bootstrapper = spy(tested.bootstrapper);

        TestGenericBroadcastReceiver.reset();
        TestGenericBroadcastReceiver2.reset();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void should_handle_intent_with_generic_noop_message() {
        Assertions.assertThat(tested.bootstrapper).isNotNull();

        Intent serviceIntent = new Intent(InstrumentationRegistry.getContext(), SensorbergService.class);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, -1);
        tested.bootstrapper.scanner = spy(tested.bootstrapper.scanner);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(0)).updateBeaconLayout();
        Mockito.verify(tested.bootstrapper.scanner, Mockito.times(0)).handlePlatformMessage(any(Bundle.class));
        Mockito.verify(tested.bootstrapper, Mockito.times(0)).updateSettings();
        Mockito.verify(tested.bootstrapper, Mockito.times(0)).uploadHistory();
        Mockito.verify(tested, Mockito.times(0)).presentBeaconEvent(serviceIntent);
        Mockito.verify(tested.bootstrapper, Mockito.times(0)).hostApplicationInForeground();
        Mockito.verify(tested.bootstrapper, Mockito.times(0)).hostApplicationInBackground();
        Mockito.verify(tested, Mockito.times(0)).setApiToken(serviceIntent);
        Mockito.verify(tested, Mockito.times(0)).registerPresentationDelegate(serviceIntent);
        Mockito.verify(tested, Mockito.times(0)).unregisterPresentationDelegate(serviceIntent);
        Mockito.verify(tested.bootstrapper, Mockito.times(0)).startScanning();
        Mockito.verify(tested, Mockito.times(0)).processBluetoothStateMessage(serviceIntent);
        Mockito.verify(tested, Mockito.times(0)).setAdvertisingIdentifier(serviceIntent);
    }

    @Test
    public void should_handle_intent_with_beacon_layout_update_message() {
        Intent serviceIntent = SensorbergServiceIntents.getBeaconLayoutUpdateIntent(InstrumentationRegistry.getContext());

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(1)).updateBeaconLayout();
    }

    @Test
    public void should_handle_intent_with_scanner_message() {
        tested.bootstrapper.scanner = spy(tested.bootstrapper.scanner);

        tested.handleIntentMessage(
                SensorbergServiceIntents.getSdkScannerMessageIntent(InstrumentationRegistry.getContext(), ScannerEvent.UN_PAUSE_SCAN));

        Mockito.verify(tested.bootstrapper.scanner, Mockito.times(1)).handlePlatformMessage(any(Bundle.class));
    }

    @Test
    public void should_handle_intent_with_update_settings_message() {
        Intent serviceIntent = SensorbergServiceIntents.getUpdateSettingsIntent(InstrumentationRegistry.getContext());

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(1)).updateSettings();
    }

    @Test
    public void should_handle_intent_with_upload_history_message() {
        Intent serviceIntent = SensorbergServiceIntents.getUploadHistoryIntent(InstrumentationRegistry.getContext());

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(1)).uploadHistory();
    }

    @Test
    public void should_handle_intent_with_beacon_action_message() {
        Intent serviceIntent = SensorbergServiceIntents
                .getBeaconActionIntent(InstrumentationRegistry.getContext(), TestConstants.getBeaconEvent(), 0);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested, Mockito.times(1)).presentBeaconEvent(serviceIntent);
    }

    @Test
    public void should_handle_intent_with_app_in_foreground_message() {
        Intent serviceIntent = SensorbergServiceIntents.getAppInForegroundIntent(InstrumentationRegistry.getContext());

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(1)).hostApplicationInForeground();
    }

    @Test
    public void should_handle_intent_with_app_in_background_message() {
        Intent serviceIntent = SensorbergServiceIntents.getAppInBackgroundIntent(InstrumentationRegistry.getContext());

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(1)).hostApplicationInBackground();
    }

    @Test
    public void should_handle_intent_with_set_api_token_message() {
        Intent serviceIntent = SensorbergServiceIntents.getApiTokenIntent(InstrumentationRegistry.getContext(), "api_token");

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested, Mockito.times(1)).setApiToken(serviceIntent);
    }

    @Test
    public void should_handle_intent_with_register_presentation_delegate_message() {
        Intent serviceIntent = SensorbergServiceIntents.getServiceIntentWithMessage(InstrumentationRegistry.getContext(),
                SensorbergServiceMessage.MSG_REGISTER_PRESENTATION_DELEGATE);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested, Mockito.times(1)).registerPresentationDelegate(serviceIntent);
    }

    @Test
    public void should_handle_intent_with_unregister_presentation_delegate_message() {
        Intent serviceIntent = SensorbergServiceIntents.getServiceIntentWithMessage(InstrumentationRegistry.getContext(),
                SensorbergServiceMessage.MSG_UNREGISTER_PRESENTATION_DELEGATE);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested, Mockito.times(1)).unregisterPresentationDelegate(serviceIntent);
    }

    @Test
    public void should_handle_intent_with_ping_message() {
        Intent serviceIntent = SensorbergServiceIntents.getPingIntent(InstrumentationRegistry.getContext());

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested.bootstrapper, Mockito.times(1)).startScanning();
    }

    @Test
    public void should_handle_intent_with_bluetooth_message() {
        Intent serviceIntent = SensorbergServiceIntents.getBluetoothMessageIntent(InstrumentationRegistry.getContext(), true);

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested, Mockito.times(1)).processBluetoothStateMessage(serviceIntent);
    }

    @Test
    public void should_handle_intent_with_set_advertising_id_message() {
        Intent serviceIntent = SensorbergServiceIntents.getAdvertisingIdentifierIntent(InstrumentationRegistry.getContext(), "testId");

        tested.handleIntentMessage(serviceIntent);

        Mockito.verify(tested, Mockito.times(1)).setAdvertisingIdentifier(serviceIntent);
    }
}
