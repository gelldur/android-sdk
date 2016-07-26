package com.sensorberg.sdk.di;

import com.google.gson.Gson;

import com.sensorberg.di.ProvidersModule;
import com.sensorberg.sdk.internal.PersistentIntegerCounter;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.settings.DefaultSettings;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.sdk.testUtils.DumbSucessTransport;
import com.sensorberg.sdk.testUtils.NoClock;
import com.sensorberg.sdk.testUtils.SuccessfulRetrofitApiService;
import com.sensorberg.sdk.testUtils.TestBluetoothPlatform;
import com.sensorberg.sdk.testUtils.TestClock;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestHandlerManager;
import com.sensorberg.sdk.testUtils.TestPlatformIdentifier;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TestProvidersModule extends ProvidersModule {

    public TestProvidersModule(Application app) {
        super(app);
    }

    @Provides
    @Named("noClock")
    @Singleton
    public Clock provideNoClock() {
        return NoClock.CLOCK;
    }

    @Provides
    @Named("testClock")
    @Singleton
    public TestClock provideTestClock() {
        return new TestClock();
    }

    @Provides
    @Singleton
    public TestFileManager provideTestFileManager(Context context) {
        return new TestFileManager(context);
    }

    @Provides
    @Singleton
    public TestServiceScheduler provideTestServiceScheduler(Context context, AlarmManager alarmManager, @Named("realClock") Clock clock,
            PersistentIntegerCounter persistentIntegerCounter) {
        return new TestServiceScheduler(context, alarmManager, clock, persistentIntegerCounter, DefaultSettings.DEFAULT_MESSAGE_DELAY_WINDOW_LENGTH);
    }

    @Provides
    @Named("testHandlerWithCustomClock")
    public HandlerManager provideTestHandlerManagerWithCustomClock() {
        return new TestHandlerManager();
    }

    @Provides
    public TestHandlerManager provideTestHandlerManager() {
        return new TestHandlerManager();
    }

    @Provides
    @Named("testPlatformIdentifier")
    public PlatformIdentifier provideTestPlatformIdentifier() {
        return new TestPlatformIdentifier();
    }

    @Provides
    @Named("testBluetoothPlatform")
    public BluetoothPlatform provideNamedTestBluetoothPlatform() {
        return new TestBluetoothPlatform();
    }

    @Provides
    public TestBluetoothPlatform provideTestBluetoothPlatform() {
        return new TestBluetoothPlatform();
    }

    @Provides
    @Named("dumbSuccessTransport")
    public Transport provideDumbSuccessTransport() {
        return new DumbSucessTransport();
    }

    @Provides
    @Named("dummyTransportSettingsManager")
    public SettingsManager provideDummyTransportSettingsManager(@Named("dumbSuccessTransport") Transport transport, SharedPreferences sharedPreferences) {
        return new SettingsManager(transport, sharedPreferences);
    }

    @Provides
    @Named("successfulRetrofitApiService")
    @Singleton
    public RetrofitApiServiceImpl provideSuccessfulRetrofitApiService(Context context, Gson gson, @Named("testPlatformIdentifier") PlatformIdentifier platformIdentifier) {
        return new SuccessfulRetrofitApiService(context, gson, platformIdentifier, "http://test.com");
    }
}
