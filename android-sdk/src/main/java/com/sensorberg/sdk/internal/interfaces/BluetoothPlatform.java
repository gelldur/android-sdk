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

    /**
     * Returns a flag indication whether location services are enabled. Location is needed
     * in order for scanning to work. Will leave here for now, if we do more location
     * based functionality, let's pull it out into its own interface.
     *
     * @return - A flag for whether location services are enabled.
     */
    boolean isLocationServicesEnabled();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    void startLeScan(BluetoothAdapter.LeScanCallback scanCallback);

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    void stopLeScan();

    boolean isLeScanRunning();

}
