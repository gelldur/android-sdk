package com.sensorberg.sdk.scanner;

import android.os.Bundle;

import com.sensorberg.SensorbergSdk;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.SensorbergServiceMessage;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.settings.SettingsManager;

public class Scanner extends AbstractScanner {
    public static final String SCANNER_EVENT = "com.sensorberg.sdk.scanner.SDKScanner.SCANNER_EVENT";

    public Scanner(SettingsManager stgMgr, boolean shouldRestoreBeaconStates, Clock clock, FileManager fileManager,
                   ServiceScheduler scheduler, HandlerManager handlerManager, BluetoothPlatform bluetoothPlatform) {
        super(stgMgr, shouldRestoreBeaconStates, clock, fileManager, scheduler, handlerManager, bluetoothPlatform);
        SensorbergSdk.getComponent().inject(this);
    }

    @Override
    protected void clearScheduledExecutions() {
        getServiceScheduler().cancelServiceMessage(indexFor(ScannerEvent.PAUSE_SCAN));
        getServiceScheduler().cancelServiceMessage(indexFor(ScannerEvent.UN_PAUSE_SCAN));
    }

    @Override
    protected void scheduleExecution(int type, long delay) {
        Bundle bundle = new Bundle();
        bundle.putInt(Scanner.SCANNER_EVENT, type);
        getServiceScheduler().postToServiceDelayed(delay, SensorbergServiceMessage.MSG_SDK_SCANNER_MESSAGE, bundle, false, indexFor(type));
    }

    private int indexFor(int type) {
        return -1000 - type;
    }

    public void handlePlatformMessage(Bundle what){
        int messageId = what.getInt(SCANNER_EVENT, -1);
        if (messageId == ScannerEvent.UN_PAUSE_SCAN){
            getRunLoop().sendMessage(ScannerEvent.UN_PAUSE_SCAN);
        } else if(messageId == ScannerEvent.PAUSE_SCAN) {
            getRunLoop().sendMessage(ScannerEvent.PAUSE_SCAN);
        } else{
            Logger.log.logError("unknown scheduled execution:" + messageId);
        }
    }
}
