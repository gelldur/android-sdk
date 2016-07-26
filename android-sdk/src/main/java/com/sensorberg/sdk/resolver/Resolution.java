package com.sensorberg.sdk.resolver;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.scanner.ScanEvent;

import java.util.List;

/**
 * Class {@link Resolution} implements a resolution of a {@link ScanEvent}; it cannot be instantiated directly but must be acquired via the
 * {@link Resolver#createResolution(ResolutionConfiguration)} factory method.
 */
public class Resolution {

    public final ResolutionConfiguration configuration;
    private final Resolver resolver;

    private final Transport transport;

    Resolution(Resolver resolver, ResolutionConfiguration configuration, Transport transport) {
        this.transport = transport;
        this.configuration = configuration;
        this.resolver = resolver;
    }

    public void queryServer() {
        Logger.log.beaconResolveState(configuration.getScanEvent(), "starting to resolve request");
        transport.getBeacon(configuration, new BeaconResponseHandler() {
            @Override
            public void onSuccess(List<BeaconEvent> beaconEvents) {
                resolver.onResolutionFinished(Resolution.this, beaconEvents);
                for (BeaconEvent beaconEvent : beaconEvents) {
                    Logger.log.beaconResolveState(configuration.getScanEvent(), "success resolving action:" + beaconEvent.getAction());
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Logger.log.beaconResolveState(configuration.getScanEvent(), "failure resolving throwable:" + throwable.getMessage());
                resolver.onResolutionFailed(Resolution.this, throwable);
            }
        });
    }

    /**
     * Starts the {@link Resolution}.
     */
    public void start() {
        resolver.startResolution(this);
    }

}
