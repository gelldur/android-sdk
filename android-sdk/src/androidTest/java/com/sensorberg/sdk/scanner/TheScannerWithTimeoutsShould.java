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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.content.SharedPreferences;
import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;

import util.Utils;

import static com.sensorberg.sdk.testUtils.SensorbergMatcher.isExitEvent;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(AndroidJUnit4.class)
public class TheScannerWithTimeoutsShould {

    @Inject
    TestFileManager testFileManager;

    @Inject
    TestServiceScheduler testServiceScheduler;

    @Inject
    TestHandlerManager testHandlerManager;

    @Inject
    TestBluetoothPlatform bluetoothPlatform;

    @Inject
    SharedPreferences sharedPreferences;

    private UIScanner tested;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        sharedPreferences.edit().clear().commit();

        testHandlerManager.getCustomClock().setNowInMillis(0);
        tested = new UIScanner(new SettingsManager(new DumbSucessTransport(), sharedPreferences), testHandlerManager.getCustomClock(),
                testFileManager, testServiceScheduler,
                testHandlerManager, bluetoothPlatform);

        tested.start();
    }

    @Test
    public void test_scanner_waits_to_the_edge_of_second_pause() {
        this.testHandlerManager.getCustomClock().setNowInMillis(0);

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);

        long earliestBeaconSighting = Utils.EXIT_TIME + tested.scanTime;
        long beaconSighting = earliestBeaconSighting + (tested.scanTime - Utils.EXIT_TIME); //Exactly to the edge of a scan
        long shouldNotSeeBeaconExitUntil = beaconSighting + Utils.EXIT_TIME + tested.waitTime;
        long shouldSeeExitEvent = shouldNotSeeBeaconExitUntil + tested.waitTime;

        while (testHandlerManager.getCustomClock().now() < Utils.ONE_MINUTE * 2) {
            if (testHandlerManager.getCustomClock().now() == beaconSighting) {
                bluetoothPlatform.fakeIBeaconSighting();
                tested.addScannerListener(mockListener);
            }

            if (testHandlerManager.getCustomClock().now() < shouldNotSeeBeaconExitUntil) {
                verifyNoMoreInteractions(mockListener);
            }
            if (testHandlerManager.getCustomClock().now() > shouldSeeExitEvent) {
                verify(mockListener).onScanEventDetected(isExitEvent());
            }

            testHandlerManager.getCustomClock().increaseTimeInMillis(Utils.ONE_SECOND);
        }
    }

    @Test
    public void test_scanner_waits_one_pause() {
        this.testHandlerManager.getCustomClock().setNowInMillis(0);

        ScannerListener mockListener = Mockito.mock(ScannerListener.class);

        long earliestBeaconSighting = Utils.EXIT_TIME + tested.scanTime;
        long beaconSighting = earliestBeaconSighting + (tested.scanTime / 2); //Somewhere in the middle of a Scan
        long shouldNotSeeBeaconExitUntil = beaconSighting + Utils.EXIT_TIME + tested.waitTime;
        long shouldSeeExitEvent = shouldNotSeeBeaconExitUntil + tested.waitTime;

        while (testHandlerManager.getCustomClock().now() < Utils.ONE_MINUTE * 2) {
            if (testHandlerManager.getCustomClock().now() == beaconSighting) {
                bluetoothPlatform.fakeIBeaconSighting();
                tested.addScannerListener(mockListener);
            }

            if (testHandlerManager.getCustomClock().now() < shouldNotSeeBeaconExitUntil) {
                verifyNoMoreInteractions(mockListener);
            }
            if (testHandlerManager.getCustomClock().now() > shouldSeeExitEvent) {
                verify(mockListener).onScanEventDetected(isExitEvent());
            }

            testHandlerManager.getCustomClock().increaseTimeInMillis(Utils.ONE_SECOND);
        }
    }
}
