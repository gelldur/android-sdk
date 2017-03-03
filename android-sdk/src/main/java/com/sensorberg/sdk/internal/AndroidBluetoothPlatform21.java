package com.sensorberg.sdk.internal;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.scanner.AbstractScanner;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AndroidBluetoothPlatform21 implements BluetoothPlatform {


    private BluetoothAdapter bluetoothAdapter;

    private BluetoothLeScanner scanner;

    private final Context context;

    private boolean leScanRunning = false;

    private PermissionChecker permissionChecker;

    private AbstractScanner.CommonCallback callback;

    private ScanCallback callback21 = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (callback == null) return;
            if (result.getScanRecord() != null) {
                callback.onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
            } else {
                Logger.log.logError("Scanner returned null scanRecord for device "+result.getDevice().getAddress());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            if(callback == null) return;
            if (errorCode == SCAN_FAILED_ALREADY_STARTED) return;
            callback.onScanFailed(errorCode);
        }
    };

    public AndroidBluetoothPlatform21(Context ctx) {
        context = ctx;
        permissionChecker = new PermissionChecker(ctx);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        scanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    /**
     * Returns a flag indicating whether Bluetooth is enabled.
     *
     * @return a flag indicating whether Bluetooth is enabled
     */
    @Override
    public boolean isBluetoothLowEnergyDeviceTurnedOn() {
        //noinspection SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement
        return isBluetoothLowEnergySupported() && (bluetoothAdapter.isEnabled());
    }

    /**
     * Returns a flag indicating whether Bluetooth is supported.
     *
     * @return a flag indicating whether Bluetooth is supported
     */
    @Override
    public boolean isBluetoothLowEnergySupported() {
        return bluetoothAdapter != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
                && scanner != null;
    }

    @Override
    public void startLeScan(AbstractScanner.CommonCallback scanCallback) {
        if (isBluetoothLowEnergySupported()) {
            if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON
                    && permissionChecker.hasScanPermissionCheckAndroid6()) {
                try {
                    //noinspection deprecation old API compatability
                    callback = scanCallback;
                    scanner.startScan(callback21);
                    leScanRunning = true;
                } catch (IllegalStateException e) {
                    // even with the adapter state checking two lines above,
                    // this still crashes https://sensorberg.atlassian.net/browse/AND-248
                    Logger.log.logError("System bug throwing error.", e);
                    leScanRunning = false;
                }
            }
        }
    }

    @Override
    public void stopLeScan() {
        if (isBluetoothLowEnergySupported() && callback != null) {
            try {
                //noinspection deprecation old API compatability
                scanner.stopScan(callback21);
            } catch (Exception sentBySysteminternally) {
                Logger.log.logError("System bug throwing a NullPointerException internally.", sentBySysteminternally);
            } finally {
                leScanRunning = false;
                callback = null;
            }
        }
    }

    @Override
    public boolean isLeScanRunning() {
        return leScanRunning;
    }
}
