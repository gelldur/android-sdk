package com.sensorberg.sdk.internal.interfaces;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;

public interface BluetoothPlatform {

    /**
     * Returns a flag indicating whether Bluetooth is enabled.
     *
     * @return a flag indicating whether Bluetooth is enabled
     */
    boolean isBluetoothLowEnergyDeviceTurnedOn();

    /**
     * Returns a flag indicating whether Bluetooth is supported.
     *
     * @return a flag indicating whether Bluetooth is supported
     */
    boolean isBluetoothLowEnergySupported();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    void startLeScan(BluetoothAdapter.LeScanCallback scanCallback);

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    void stopLeScan();

    boolean isLeScanRunning();

}
