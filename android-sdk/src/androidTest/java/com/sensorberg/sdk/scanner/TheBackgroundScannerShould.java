package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.settings.DefaultSettings;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.sensorberg.sdk.testUtils.TestBluetoothPlatform;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestHandlerManager;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.content.SharedPreferences;
import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;

import static com.sensorberg.sdk.testUtils.SensorbergMatcher.isEntryEvent;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(AndroidJUnit4.class)
public class TheBackgroundScannerShould {

    @Inject
    TestFileManager testFileManager;

    @Inject
    TestServiceScheduler testServiceScheduler;

    @Inject
    TestBluetoothPlatform bluetoothPlatform;

    @Inject
    SharedPreferences sharedPreferences;

    TestHandlerManager testHandlerManager;

    private UIScanner tested;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        sharedPreferences.edit().clear().commit();
        setUpScanner();
    }

    private void setUpScanner() {
        testHandlerManager = new TestHandlerManager();
        tested = new UIScanner(new SettingsManager(new DumbSucessTransport(), sharedPreferences), testHandlerManager.getCustomClock(),
                testFileManager, testServiceScheduler, testHandlerManager, bluetoothPlatform);
        tested.hostApplicationInBackground();
        tested.start();
    }

    @Test
    public void test_be_in_background_mode() {
        assertThat(tested.waitTime).isEqualTo(DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);
        assertThat(tested.scanTime).isEqualTo(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME);
    }

    @Test
    public void test_detect_no_beacon_because_it_is_sleeping() {
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME + 1);

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        bluetoothPlatform.fakeIBeaconSighting();

        verifyZeroInteractions(mockListener);
    }

    @Test
    public void test_detect_beacon_because_sleep_has_ended() {
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME - 1);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME + 1);

        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME + DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);
        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME + DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME + 1);

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        bluetoothPlatform.fakeIBeaconSighting();

        verify(mockListener).onScanEventDetected(isEntryEvent());
    }

    @Test
    public void test_background_times_should_be_switched_to_foreground_times() {
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME - 1);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME + 1);

        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME + DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME / 2);

        tested.hostApplicationInForeground();
        assertThat(tested.waitTime).isNotEqualTo(DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);
        assertThat(tested.waitTime).isEqualTo(DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME);
        assertThat(tested.scanTime).isNotEqualTo(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME);
        assertThat(tested.scanTime).isEqualTo(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME);
    }

    @Test
    public void test_detect_beacon_because_sleep_has_ended_due_to_foreground() {
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME - 1);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME + 1);

        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME + DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME / 2);

        tested.hostApplicationInForeground();

        testHandlerManager.getCustomClock().increaseTimeInMillis(1);

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        bluetoothPlatform.fakeIBeaconSighting();

        verify(mockListener).onScanEventDetected(isEntryEvent());
    }

}
