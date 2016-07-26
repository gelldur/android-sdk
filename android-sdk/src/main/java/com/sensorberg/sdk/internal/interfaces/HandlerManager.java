package com.sensorberg.sdk.internal.interfaces;

public interface HandlerManager {

    RunLoop getScannerRunLoop(RunLoop.MessageHandlerCallback callback);

    RunLoop getResolverRunLoop(RunLoop.MessageHandlerCallback callback);

    RunLoop getBeaconPublisherRunLoop(RunLoop.MessageHandlerCallback callback);
}
