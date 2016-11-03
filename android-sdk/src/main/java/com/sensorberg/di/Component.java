package com.sensorberg.di;

import com.sensorberg.BackgroundDetector;
import com.sensorberg.SensorbergSdk;
import com.sensorberg.sdk.InternalApplicationBootstrapper;
import com.sensorberg.sdk.SensorbergService;
import com.sensorberg.sdk.internal.AndroidPlatform;
import com.sensorberg.sdk.internal.PendingIntentStorage;
import com.sensorberg.sdk.scanner.BeaconMap;

import android.app.Application;

import javax.inject.Singleton;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Singleton
@dagger.Component(modules = {ProvidersModule.class})
public interface Component {

    void inject(InternalApplicationBootstrapper bootstrapper);

    void inject(PendingIntentStorage pendingIntentStorage);

    void inject(AndroidPlatform androidPlatform);

    void inject(SensorbergService sensorbergService);

    void inject(BeaconMap beaconMap);

    void inject(SensorbergSdk sensorbergSdk);

    void inject(BackgroundDetector backgroundDetector);

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class Initializer {

        public static Component init(Application app) {
            return DaggerComponent.builder()
                    .providersModule(new ProvidersModule(app))
                    .build();
        }
    }
}