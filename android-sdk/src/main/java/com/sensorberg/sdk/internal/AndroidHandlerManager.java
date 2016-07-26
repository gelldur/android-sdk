package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.RunLoop;

public class AndroidHandlerManager implements HandlerManager {

    @Override
    public RunLoop getScannerRunLoop(RunLoop.MessageHandlerCallback callback) {
        return new AndroidHandler(callback);
    }

    @Override
    public RunLoop getResolverRunLoop(RunLoop.MessageHandlerCallback callback) {
        return new AndroidHandler(callback);
    }

    @Override
    public RunLoop getBeaconPublisherRunLoop(RunLoop.MessageHandlerCallback callback) {
        return new AndroidHandler(callback);
    }
}
