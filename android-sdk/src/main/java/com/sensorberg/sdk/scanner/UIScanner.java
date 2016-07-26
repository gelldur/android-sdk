package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.settings.SettingsManager;

public class UIScanner extends AbstractScanner {

    public UIScanner(SettingsManager stgMgr, Clock clock, FileManager fileManager, ServiceScheduler scheduler,
            HandlerManager handlerManager, BluetoothPlatform bluetoothPlatform) {
        super(stgMgr, false, clock, fileManager, scheduler, handlerManager, bluetoothPlatform);
    }

    @Override
    protected void clearScheduledExecutions() {
        getRunLoop().clearScheduledExecutions();
    }

    @Override
    void scheduleExecution(final int type, long delay) {
        getRunLoop().scheduleExecution(new Runnable() {
            @Override
            public void run() {
                getRunLoop().sendMessage(type);
            }
        }, delay);
    }
}
