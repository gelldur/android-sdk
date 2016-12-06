package com.sensorberg.sdk.settings;

import com.google.gson.Gson;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.model.SettingsResponse;

import junit.framework.Assert;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.mock.Calls;
import util.Utils;

@RunWith(AndroidJUnit4.class)
public class TheSettingsShould {

    @Inject
    Clock clock;

    @Inject
    @Named("testPlatformIdentifier")
    PlatformIdentifier testPlatformIdentifier;

    @Inject
    Gson gson;

    RetrofitApiServiceImpl mockRetrofitApiService = Mockito.mock(RetrofitApiServiceImpl.class);

    SettingsManager tested;

    private SharedPreferences testedSharedPreferences;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        Transport transport = new RetrofitApiTransport(mockRetrofitApiService, clock);
        testedSharedPreferences = InstrumentationRegistry.getContext().getSharedPreferences(Long.toString(System.currentTimeMillis()),
                Context.MODE_PRIVATE);
        tested = new SettingsManager(transport, testedSharedPreferences);
    }

    @Test
    public void test_new_settings_use_defaults() throws Exception {
        Settings settingsFromEmptyJson = new Settings();
        Assertions.assertThat(settingsFromEmptyJson.getBackgroundWaitTime()).isEqualTo(DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);
        Assertions.assertThat(settingsFromEmptyJson.getRevision()).isNull();
    }

    @Test
    public void test_initial_values_should_be_identical() throws Exception {
        Assertions.assertThat(tested.getBackgroundScanTime()).isEqualTo(DefaultSettings.DEFAULT_BACKGROUND_SCAN_TIME);
        Assertions.assertThat(tested.getBackgroundWaitTime()).isEqualTo(DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);
        Assertions.assertThat(tested.getExitTimeoutMillis()).isEqualTo(DefaultSettings.DEFAULT_EXIT_TIMEOUT_MILLIS);
        Assertions.assertThat(tested.getForeGroundScanTime()).isEqualTo(DefaultSettings.DEFAULT_FOREGROUND_SCAN_TIME);
        Assertions.assertThat(tested.getForeGroundWaitTime()).isEqualTo(DefaultSettings.DEFAULT_FOREGROUND_WAIT_TIME);
        Assertions.assertThat(tested.getBeaconReportLevel()).isEqualTo(DefaultSettings.DEFAULT_BEACON_REPORT_LEVEL);
        Assertions.assertThat(tested.getScannerMinRssi()).isEqualTo(DefaultSettings.DEFAULT_SCANNER_MIN_RSSI);
        Assertions.assertThat(tested.getScannerMaxDistance()).isEqualTo(DefaultSettings.DEFAULT_SCANNER_MAX_DISTANCE);
    }

    @Test
    public void test_parsing_settings_from_network_response() throws Exception {
        SettingsResponse settingsResponse = gson.fromJson(
                Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.response_settings_newdefaults, InstrumentationRegistry
                        .getContext()), SettingsResponse.class);
        Mockito.when(mockRetrofitApiService.getSettings()).thenReturn(Calls.response(settingsResponse));

        Assertions.assertThat(settingsResponse).isNotNull();
        Assertions.assertThat(settingsResponse.getRevision()).isEqualTo(1L);
        Assertions.assertThat(settingsResponse.getSettings().getBackgroundWaitTime()).isEqualTo(100000L);
        Assertions.assertThat(settingsResponse.getSettings().getBeaconReportLevel()).isEqualTo(1);
        Assertions.assertThat(settingsResponse.getSettings().getScannerMinRssi()).isEqualTo(-10);
        Assertions.assertThat(settingsResponse.getSettings().getScannerMaxDistance()).isEqualTo(5);
    }

    @Test
    public void test_fetch_values_from_the_network() throws Exception {
        SettingsResponse settingsResponse = gson.fromJson(
                Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.response_settings_newdefaults, InstrumentationRegistry.getContext()),
                SettingsResponse.class);
        Mockito.when(mockRetrofitApiService.getSettings()).thenReturn(Calls.response(settingsResponse));

        tested.updateSettingsFromNetwork();

        Assertions.assertThat(tested.getBackgroundWaitTime()).isNotEqualTo(DefaultSettings.DEFAULT_BACKGROUND_WAIT_TIME);
        Assertions.assertThat(tested.getSettingsRevision()).isNotNull();
        Assertions.assertThat(tested.getSettingsRevision()).isEqualTo(1L);
    }

    @SuppressLint("CommitPrefEdits")
    @Test
    public void test_update_the_default_values_if_the_constants_change() throws Exception {
        //prepare the shared preferences
        SharedPreferences.Editor editor = testedSharedPreferences.edit();
        editor.putLong(SharedPreferencesKeys.Scanner.BACKGROUND_WAIT_TIME, TimeConstants.ONE_MINUTE * 6);
        editor.commit();

        //load the last values from the shared preferences, as it happens after a restart
        Settings settingsFromPrefs = new Settings(testedSharedPreferences);
        Assertions.assertThat(settingsFromPrefs.getBackgroundWaitTime()).isEqualTo(TimeConstants.ONE_MINUTE * 6);
    }

    public void test_advertising_id_gets_persisted() throws Exception {
        //prepare the shared preferences
        Assert.fail();
//        Assertions.assertThat(untouched.getAdvertisingIdentifier()).isNull();
//        Assertions.assertThat(tested.getAdvertisingIdentifier()).isEqualTo(untouched.getAdvertisingIdentifier());
//        Assertions.assertThat(testedSharedPreferences.getString(Constants.SharedPreferencesKeys.Network.ADVERTISING_IDENTIFIER, "")).isEmpty();
//
//        tested.setAdvertisingIdentifier("TEST_ID");
//        Assertions.assertThat(testedSharedPreferences.getString(Constants.SharedPreferencesKeys.Network.ADVERTISING_IDENTIFIER, "")).isEqualTo(
//                "TEST_ID");
//
//        //load the last values from the shared preferences, as it happens after a restart
//        tested.restoreValuesFromPreferences();
//        Assertions.assertThat(tested.getAdvertisingIdentifier()).isEqualTo("TEST_ID");
//
//        //simulating a settings request without content
//        tested.onSettingsFound(new JSONObject());
//        Assertions.assertThat(tested.getAdvertisingIdentifier()).isEqualTo("TEST_ID");
    }
}
