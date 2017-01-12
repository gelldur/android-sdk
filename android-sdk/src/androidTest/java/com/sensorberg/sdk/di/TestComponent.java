package com.sensorberg.sdk.di;

import com.sensorberg.di.Component;
import com.sensorberg.sdk.SensorbergServiceIntentHandlingTests;
import com.sensorberg.sdk.SensorbergServiceIntentMessageHandlingTests;
import com.sensorberg.sdk.SensorbergServiceInternalTests;
import com.sensorberg.sdk.SensorbergServiceMessengerListTests;
import com.sensorberg.sdk.SensorbergServiceStartTests;
import com.sensorberg.sdk.TheInternalApplicationBootstrapperShould;
import com.sensorberg.sdk.TheInternalBootstrapperIntegration;
import com.sensorberg.sdk.action.ActionFactoryTest;
import com.sensorberg.sdk.action.TheActionShould;
import com.sensorberg.sdk.internal.TheIntentSchedulingBeUpdateable;
import com.sensorberg.sdk.internal.TheIntentSchedulingShould;
import com.sensorberg.sdk.internal.http.HttpStackShouldCacheTheSettings;
import com.sensorberg.sdk.internal.http.TransportShould;
import com.sensorberg.sdk.internal.transport.ApiServiceInGeneralShould;
import com.sensorberg.sdk.internal.transport.ApiServiceShould;
import com.sensorberg.sdk.location.GeofenceStorageTest;
import com.sensorberg.sdk.location.LocationHelperTest;
import com.sensorberg.sdk.model.persistence.TheBeaconActionShould;
import com.sensorberg.sdk.model.persistence.TheBeaconScanShould;
import com.sensorberg.sdk.model.server.ResolveActionTest;
import com.sensorberg.sdk.model.server.TheResolveResponse;
import com.sensorberg.sdk.resolver.TheResolveResponseShould;
import com.sensorberg.sdk.resolver.TheResolverWithMockApiShould;
import com.sensorberg.sdk.resolver.TheResolverWithRealApiShould;
import com.sensorberg.sdk.scanner.ScannerWithLongScanTime;
import com.sensorberg.sdk.scanner.TheBackgroundScannerShould;
import com.sensorberg.sdk.scanner.TheBeaconActionHistoryPublisherIntegrationShould;
import com.sensorberg.sdk.scanner.TheBeaconActionHistoryPublisherShould;
import com.sensorberg.sdk.scanner.TheBeaconMapShould;
import com.sensorberg.sdk.scanner.TheBluetoothChangesShould;
import com.sensorberg.sdk.scanner.TheDefaultScannerSetupShould;
import com.sensorberg.sdk.scanner.TheForegroundScannerShould;
import com.sensorberg.sdk.scanner.TheScannerWithRestoredStateShould;
import com.sensorberg.sdk.scanner.TheScannerWithTimeoutsShould;
import com.sensorberg.sdk.scanner.TheScannerWithoutPausesShould;
import com.sensorberg.sdk.service.TheServiceConfiguration;
import com.sensorberg.sdk.settings.TheSettingsShould;

import android.app.Application;

import javax.inject.Singleton;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Singleton
@dagger.Component(modules = {TestProvidersModule.class})
public interface TestComponent extends Component {

    void inject(com.sensorberg.sdk.testUtils.TestPlatform testPlatform);

    void inject(ScannerWithLongScanTime scannerWithLongScanTime);

    void inject(TheScannerWithRestoredStateShould theScannerWithRestoredStateShould);

    void inject(TheForegroundScannerShould theForegroundScannerShould);

    void inject(TheBluetoothChangesShould theBluetoothChangesShould);

    void inject(TheDefaultScannerSetupShould theDefaultScannerSetupShould);

    void inject(TheScannerWithTimeoutsShould theScannerWithTimeoutsShould);

    void inject(TheScannerWithoutPausesShould theScannerWithoutPausesShould);

    void inject(SensorbergServiceInternalTests sensorbergServiceInternalTests);

    void inject(TheBackgroundScannerShould theBackgroundScannerShould);

    void inject(TheBeaconMapShould theBeaconMapShould);

    void inject(TheServiceConfiguration theServiceConfiguration);

    void inject(TheInternalApplicationBootstrapperShould theInternalApplicationBootstrapperShould);

    void inject(TheInternalBootstrapperIntegration theInternalBootstrapperIntegration);

    void inject(TheIntentSchedulingBeUpdateable theIntentSchedulingBeUpdateable);

    void inject(TheIntentSchedulingShould theIntentSchedulingShould);

    void inject(TheBeaconActionHistoryPublisherIntegrationShould theBeaconActionHistoryPublisherIntegrationShould);

    void inject(TheResolverWithMockApiShould theResolverWithMockApiShould);

    void inject(TheBeaconActionHistoryPublisherShould theBeaconActionHistoryPublisherShould);

    void inject(TransportShould transportShould);

    void inject(HttpStackShouldCacheTheSettings httpStackShouldCacheTheSettings);

    void inject(TheSettingsShould theSettingsShould);

    void inject(ResolveActionTest resolveActionTest);

    void inject(TheResolveResponse theResolveResponse);

    void inject(TheResolveResponseShould theResolveResponseShould);


    void inject(SensorbergServiceStartTests sensorbergServiceStartTests);

    void inject(SensorbergServiceIntentHandlingTests sensorbergServiceIntentHandlingTests);

    void inject(SensorbergServiceIntentMessageHandlingTests sensorbergServiceIntentMessageHandlingTests);

    void inject(SensorbergServiceMessengerListTests sensorbergServiceMessengerListTests);

    void inject(TheBeaconActionShould theBeaconActionShould);

    void inject(TheBeaconScanShould theBeaconrScanShould);

    void inject(TheResolverWithRealApiShould theResolverWithRealApiShould);

    void inject(ApiServiceShould apiServiceShould);

    void inject(ApiServiceInGeneralShould apiServiceInGeneralShould);

    void inject(ActionFactoryTest actionFactoryTest);

    void inject(TheActionShould theActionShould);

    void inject(LocationHelperTest locationHelperTest);

    void inject(GeofenceStorageTest locationHelperTest);

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class Initializer {

        public static TestComponent init(Application app) {
            return DaggerTestComponent.builder()
                    .testProvidersModule(new TestProvidersModule(app))
                    .build();
        }
    }
}