package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.sensorberg.sdk.testUtils.TestBluetoothPlatform;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestHandlerManager;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.SharedPreferences;
import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;


@RunWith(AndroidJUnit4.class)
public class TheScannerWithRestoredStateShould {

    @Inject
    TestFileManager testFileManager;

    @Inject
    TestServiceScheduler testServiceScheduler;

    @Inject
    TestBluetoothPlatform bluetoothPlatform;

    @Inject
    SharedPreferences sharedPreferences;

    TestHandlerManager testHandlerManager;

    private SettingsManager settings;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        sharedPreferences.edit().clear().commit();
        testHandlerManager = new TestHandlerManager();
        settings = new SettingsManager(new DumbSucessTransport(), sharedPreferences);
    }

    @Ignore
    public void should_trigger_exits_if_the_scanner_was_idle_for_too_long() throws Exception {

        //TODO should_trigger_exits_if_the_scanner_was_idle_for_too_long

//        long startTime = settings.getCleanBeaconMapRestartTimeout() / 2;
//
//        testHandlerManager.getCustomClock().setNowInMillis(startTime);
//        Scanner tested = new Scanner(settings, true, testHandlerManager.getCustomClock(), testFileManager, testServiceScheduler, testHandlerManager, bluetoothPlatform);
//        ScannerListener listener = mock(ScannerListener.class);
//        tested.addScannerListener(listener);
//        tested.start();
//
//        testHandlerManager.getCustomClock().increaseTimeInMillis(settings.getExitTimeoutMillis() - 1);
//        testHandlerManager.getCustomClock().increaseTimeInMillis(1);
//        testHandlerManager.getCustomClock().increaseTimeInMillis(1);
//
//        verify(listener, times(1)).onScanEventDetected(isExitEvent());
    }

    @Test
    public void should_not_trigger_exits_if_the_scanner_was_idle_for_too_long() throws Exception {

        long startTime = settings.getCleanBeaconMapRestartTimeout() + 1;

        testHandlerManager.getCustomClock().setNowInMillis(startTime);
        Scanner tested = new Scanner(settings, true, testHandlerManager.getCustomClock(), testFileManager, testServiceScheduler, testHandlerManager, bluetoothPlatform);
        ScannerListener listener = mock(ScannerListener.class);
        tested.addScannerListener(listener);
        tested.start();

        testHandlerManager.getCustomClock().increaseTimeInMillis(settings.getExitTimeoutMillis() - 1);
        testHandlerManager.getCustomClock().increaseTimeInMillis(1);
        testHandlerManager.getCustomClock().increaseTimeInMillis(1);

        verifyNoMoreInteractions(listener);
    }

    @Ignore
    public void should_not_trigger_entry_if_beacon_was_seen_again_after_restart() throws Exception {

        //TODO should_not_trigger_entry_if_beacon_was_seen_again_after_restart

//        long startTime = settings.getCleanBeaconMapRestartTimeout() - 1;
//
//        testHandlerManager.getCustomClock().setNowInMillis(startTime);
//        Scanner tested = new Scanner(settings, true, testHandlerManager.getCustomClock(), testFileManager, testServiceScheduler, testHandlerManager, bluetoothPlatform);
//        ScannerListener listener = mock(ScannerListener.class);
//        tested.addScannerListener(listener);
//        tested.start();
//
//        testHandlerManager.getCustomClock().increaseTimeInMillis(settings.getExitTimeoutMillis() - 1);
//        bluetoothPlatform.fakeIBeaconSighting();
//        testHandlerManager.getCustomClock().increaseTimeInMillis(1);
//        bluetoothPlatform.fakeIBeaconSighting();
//        testHandlerManager.getCustomClock().increaseTimeInMillis(1);
//        bluetoothPlatform.fakeIBeaconSighting();
//
//        verifyNoMoreInteractions(listener);
    }
}
