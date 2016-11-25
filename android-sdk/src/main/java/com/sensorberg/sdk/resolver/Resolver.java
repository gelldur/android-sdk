package com.sensorberg.sdk.resolver;

import android.os.Message;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.RunLoop;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.scanner.ScanEvent;

import java.util.List;
import java.util.SortedMap;

import lombok.Setter;

public final class Resolver implements RunLoop.MessageHandlerCallback {

    public final ResolverConfiguration configuration;

    private final Transport transport;

    private final RunLoop runLoop;

    @Setter
    protected SortedMap<String, String> attributes;

    @Setter
    private ResolverListener listener = ResolverListener.NONE;

    public Resolver(ResolverConfiguration configuration, HandlerManager handlerManager, Transport transport, SortedMap<String, String> attributes) {
        this.configuration = configuration;
        runLoop = handlerManager.getResolverRunLoop(this);
        this.transport = transport;
        transport.setApiToken(configuration.apiToken);
        this.attributes = attributes;
    }

    @Override
    public void handleMessage(Message queueEvent) {
        switch (queueEvent.arg1) {
            case ResolverEvent.RESOLUTION_START_REQUESTED: {
                ScanEvent scanEvent= (ScanEvent) queueEvent.obj;
                queryServer(scanEvent);
                break;
            }
            default: {
                throw new IllegalArgumentException("unhandled default case");
            }
        }
    }


    public void resolve(ScanEvent scanEvent) {
        runLoop.add(ResolverEvent.asMessage(ResolverEvent.RESOLUTION_START_REQUESTED, scanEvent));
    }

    public void queryServer(final ScanEvent scanEvent) {
        Logger.log.beaconResolveState(scanEvent, "starting to resolve request");
        transport.getBeacon(scanEvent, attributes, new BeaconResponseHandler() {
            @Override
            public void onSuccess(List<BeaconEvent> beaconEvents) {
                listener.onResolutionsFinished(beaconEvents);
                for (BeaconEvent beaconEvent : beaconEvents) {
                    Logger.log.beaconResolveState(scanEvent, "success resolving action:" + beaconEvent.getAction());
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                listener.onResolutionFailed(throwable, scanEvent);
            }
        });
    }
}
