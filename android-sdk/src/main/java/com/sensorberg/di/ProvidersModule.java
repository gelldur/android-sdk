package com.sensorberg.di;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.support.annotation.Nullable;

import com.gdubina.multiprocesspreferences.MultiprocessPreferenceManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sensorberg.sdk.internal.AndroidBluetoothPlatform;
import com.sensorberg.sdk.internal.AndroidClock;
import com.sensorberg.sdk.internal.AndroidFileManager;
import com.sensorberg.sdk.internal.AndroidHandlerManager;
import com.sensorberg.sdk.internal.AndroidPlatform;
import com.sensorberg.sdk.internal.AndroidPlatformIdentifier;
import com.sensorberg.sdk.internal.AndroidServiceScheduler;
import com.sensorberg.sdk.internal.PermissionChecker;
import com.sensorberg.sdk.internal.PersistentIntegerCounter;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.Platform;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.internal.transport.RetrofitApiServiceImpl;
import com.sensorberg.sdk.internal.transport.RetrofitApiTransport;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.location.GeofenceManager;
import com.sensorberg.sdk.location.LocationHelper;
import com.sensorberg.sdk.location.PlayServiceManager;
import com.sensorberg.sdk.model.ISO8601TypeAdapter;
import com.sensorberg.sdk.scanner.BeaconActionHistoryPublisher;
import com.sensorberg.sdk.settings.DefaultSettings;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.utils.PlayServicesUtils;

import java.util.Date;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ProvidersModule {

    private static final String SENSORBERG_PREFERENCE_IDENTIFIER = "com.sensorberg.preferences";

    private final Application application;

    public ProvidersModule(Application app) {
        application = app;
    }

    @Provides
    @Singleton
    public Context provideApplicationContext() {
        return application;
    }

    @Provides
    @Singleton
    public SharedPreferences provideSettingsSharedPrefs(Context context) {
        return MultiprocessPreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @Singleton
    public NotificationManager provideNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    @Singleton
    public LocationManager provideLocationManager(Context context) {
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Provides
    @Singleton
    public LocationHelper provideLocationHelper(LocationManager locationManager, @Named("realSettingsManager") SettingsManager settingsManager) {
        return new LocationHelper(locationManager, settingsManager);
    }

    @Provides
    @Singleton
    @Nullable
    public PlayServiceManager providePlayServiceManager(Context context, LocationHelper location, PermissionChecker checker) {
        if (PlayServicesUtils.isGooglePlayServicesAvailable(context)) {
            return new PlayServiceManager(context, location, checker);
        } else {
            return null;
        }
    }

    @Provides
    @Singleton
    public Clock provideRealClock() {
        return new AndroidClock();
    }

    @Provides
    @Singleton
    public FileManager provideFileManager(Context context) {
        return new AndroidFileManager(context);
    }

    @Provides
    @Singleton
    public PermissionChecker providePermissionChecker(Context context) {
        return new PermissionChecker(context);
    }

    @Provides
    @Singleton
    public PersistentIntegerCounter providePersistentIntegerCounter(SharedPreferences sharedPreferences) {
        return new PersistentIntegerCounter(sharedPreferences);
    }

    @Provides
    @Singleton
    public AlarmManager provideAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @Provides
    @Singleton
    public ServiceScheduler provideIntentScheduler(Context context, AlarmManager alarmManager, Clock clock,
                                                   PersistentIntegerCounter persistentIntegerCounter) {
        return new AndroidServiceScheduler(context, alarmManager, clock, persistentIntegerCounter,
                DefaultSettings.DEFAULT_MESSAGE_DELAY_WINDOW_LENGTH);
    }

    @Provides
    @Named("realHandlerManager")
    @Singleton
    public HandlerManager provideAndroidHandlerManager() {
        return new AndroidHandlerManager();
    }

    @Provides
    @Named("androidPlatformIdentifier")
    @Singleton
    public PlatformIdentifier provideAndroidPlatformIdentifier(Context ctx, SharedPreferences settingsSharedPrefs) {
        return new AndroidPlatformIdentifier(ctx, settingsSharedPrefs);
    }

    @Provides
    @Named("androidBluetoothPlatform")
    @Singleton
    public BluetoothPlatform provideAndroidBluetoothPlatform(Context context) {
        return new AndroidBluetoothPlatform(context);
    }

    @Provides
    @Named("realTransport")
    @Singleton
    public Transport provideRealTransport(@Named("realRetrofitApiService") RetrofitApiServiceImpl retrofitApiService, Clock clock, SharedPreferences sharedPreferences, Gson gson) {
        return new RetrofitApiTransport(retrofitApiService, clock, sharedPreferences, gson);
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Date.class, ISO8601TypeAdapter.DATE_ADAPTER)
                .setLenient()
                .create();
    }

    @Provides
    @Named("realBeaconActionHistoryPublisher")
    @Singleton
    public BeaconActionHistoryPublisher provideBeaconActionHistoryPublisher(
            @Named("realTransport") Transport transport,
            Clock clock,
            @Named("realHandlerManager") HandlerManager handlerManager,
            SharedPreferences sharedPreferences, Gson gson) {
        return new BeaconActionHistoryPublisher(transport, clock, handlerManager, sharedPreferences, gson);
    }

    @Provides
    @Named("realSettingsManager")
    @Singleton
    public SettingsManager provideSettingsManager(@Named("realTransport") Transport transport, SharedPreferences sharedPreferences) {
        return new SettingsManager(transport, sharedPreferences);
    }

    @Provides
    @Named("realRetrofitApiService")
    @Singleton
    public RetrofitApiServiceImpl provideRealRetrofitApiService(Context context, Gson gson,
                                                                @Named("androidPlatformIdentifier") PlatformIdentifier platformIdentifier) {
        return new RetrofitApiServiceImpl(context.getCacheDir(), gson, platformIdentifier, RetrofitApiTransport.RESOLVER_BASE_URL);
    }

    @Provides
    @Named("androidPlatform")
    @Singleton
    public Platform provideAndroidPlatform(Context context) {
        return new AndroidPlatform(context);
    }

    @Provides
    @Singleton
    @Nullable
    public GeofenceManager provideGeofenceManager(Context context, @Named("realSettingsManager") SettingsManager settings, SharedPreferences preferences, Gson gson, @Nullable PlayServiceManager play) {
        if (play != null) {
            return new GeofenceManager(context, settings, preferences, gson, play);
        } else {
            return null;
        }
    }
}
