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

/**
 * Created by Burak on 22.09.2014.
 */
@RunWith(AndroidJUnit4.class)
public class TheForegroundScannerShould {

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

        tested.start();
    }

    private void setUpScanner() {
        testHandlerManager = new TestHandlerManager();
        tested = new UIScanner(new SettingsManager(new DumbSucessTransport(), sharedPreferences), testHandlerManager.getCustomClock(),
                testFileManager, testServiceScheduler, testHandlerManager, bluetoothPlatform);
        tested.waitTime = DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME;
        tested.scanTime = DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME;
    }

    @Test
    public void test_be_in_foreground_mode() {
        assertThat(tested.waitTime).isEqualTo(DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME);
        assertThat(tested.scanTime).isEqualTo(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME);
    }

    @Test
    public void test_detect_no_beacon_because_it_is_sleeping() {
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + 1);

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        bluetoothPlatform.fakeIBeaconSighting();

        verifyZeroInteractions(mockListener);
    }

    @Test
    public void test_detect_beacon_because_sleep_has_ended() {
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME - 1);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + 1);

        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME);
        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME + 1);

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        bluetoothPlatform.fakeIBeaconSighting();

        verify(mockListener).onScanEventDetected(isEntryEvent());
    }

    @Test
    public void test_foreground_times_should_be_switched_to_background_times() {
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME - 1);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + 1);

        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME / 2);

        tested.hostApplicationInBackground();
        assertThat(tested.waitTime).isEqualTo(DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);
        assertThat(tested.waitTime).isNotEqualTo(DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME);
        assertThat(tested.scanTime).isEqualTo(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME);
        assertThat(tested.scanTime).isNotEqualTo(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME);
    }

    @Test
    public void test_do_not_detect_beacon_because_sleep_has_not_ended_due_to_background() {
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME - 1);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + 1);

        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME / 2);

        tested.hostApplicationInBackground();

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        bluetoothPlatform.fakeIBeaconSighting();

        verifyZeroInteractions(mockListener);
    }

    @Test
    public void test_background_scan_times_are_applied() {

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME - 1);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + 1);

        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME / 2);

        tested.hostApplicationInBackground();

        //finish the foreground wait time
        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME - 1);
        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME);
        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME + 1);

        //set time just before the end of the Background scan time
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME
                + DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME - 1);
        //mock a beacon, since the scanner is active, this one should be recognized
        bluetoothPlatform.fakeIBeaconSighting();

        verify(mockListener).onScanEventDetected(isEntryEvent());
    }

    @Test
    public void test_background_wait_starts() {
        ScannerListener mockListener = Mockito.mock(ScannerListener.class);
        tested.addScannerListener(mockListener);

        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME - 1);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME);
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + 1);

        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME / 2);

        tested.hostApplicationInBackground();

        //finish the foreground wait time
        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME - 1);
        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME);
        testHandlerManager.getCustomClock()
                .setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME + 1);

        //set time just before the end of the Background scan time
        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME
                + DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME + 1);
        //mock a beacon, since the scanner is should be inactive, this should not be recognized
        bluetoothPlatform.fakeIBeaconSighting();

        //since it is one millis after, there should not be interactions
        verifyZeroInteractions(mockListener);

        testHandlerManager.getCustomClock().setNowInMillis(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME + DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME
                + DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME + DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME - 1);
        bluetoothPlatform.fakeIBeaconSighting();

        //is is one milli before the end...
        verifyZeroInteractions(mockListener);
    }
}
