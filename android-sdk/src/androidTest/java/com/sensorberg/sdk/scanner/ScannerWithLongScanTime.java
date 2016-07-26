package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.sdk.settings.TimeConstants;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.sensorberg.sdk.testUtils.TestBluetoothPlatform;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestHandlerManager;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.SharedPreferences;
import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class ScannerWithLongScanTime {

    @Inject
    TestFileManager testFileManager;

    @Inject
    TestServiceScheduler testServiceScheduler;

    @Inject
    SharedPreferences sharedPreferences;

    TestHandlerManager handlerManager;

    private BluetoothPlatform spyBluetoothPlatform;

    private SettingsManager modifiedSettings;
    private UIScanner tested;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        sharedPreferences.edit().clear().commit();

        handlerManager = new TestHandlerManager();
        spyBluetoothPlatform = spy(new TestBluetoothPlatform());
        modifiedSettings = spy(new SettingsManager(new DumbSucessTransport(), sharedPreferences));

        when(modifiedSettings.getForeGroundScanTime()).thenReturn(TimeConstants.ONE_DAY);
        when(modifiedSettings.getForeGroundWaitTime()).thenReturn(TimeConstants.ONE_SECOND);
        tested = new UIScanner(modifiedSettings, handlerManager.getCustomClock(), testFileManager, testServiceScheduler, handlerManager, spyBluetoothPlatform);
    }

    @Test
    public void test_should_pause_when_going_to_the_background_and_scanning_was_running() throws Exception {
        tested.hostApplicationInForeground();
        tested.start();

        handlerManager.getCustomClock().setNowInMillis(modifiedSettings.getBackgroundScanTime() - 1);
        handlerManager.getCustomClock().setNowInMillis(modifiedSettings.getBackgroundScanTime());
        handlerManager.getCustomClock().setNowInMillis(modifiedSettings.getBackgroundScanTime() + 1 );

        tested.hostApplicationInBackground();

        verify(spyBluetoothPlatform).stopLeScan();
    }
}
