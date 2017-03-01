package com.sensorberg.bluetooth;

import com.radiusnetworks.bluetooth.BluetoothCrashResolver;
import com.sensorberg.sdk.scanner.AbstractScanner;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * convenience wrapper to abstract the {@link com.radiusnetworks.bluetooth.BluetoothCrashResolver} code
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CrashCallBackWrapper implements BluetoothAdapter.LeScanCallback{

    private final BluetoothCrashResolver bluetoothCrashResolver;

    private AbstractScanner.CommonCallback callback = null;

    /**
     * default constructor, internally setting up the {@link com.radiusnetworks.bluetooth.BluetoothCrashResolver}
     * @param application parameter, required for the initialization of the {@link com.radiusnetworks.bluetooth.BluetoothCrashResolver}
     */
    public CrashCallBackWrapper(Context application){
        if (application.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bluetoothCrashResolver = new BluetoothCrashResolver(application);
            bluetoothCrashResolver.start();
        } else {
            bluetoothCrashResolver = null;
        }
    }

    /**
     * set the callback and automatically stop/start the {@link com.radiusnetworks.bluetooth.BluetoothCrashResolver}
     */
    public void setCallback(AbstractScanner.CommonCallback incoming){
        callback = incoming;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (bluetoothCrashResolver != null) {
            bluetoothCrashResolver.notifyScannedDevice(device, this);
        }
        if (callback != null) {
            callback.onLeScan(device, rssi, scanRecord);
        }
    }
}