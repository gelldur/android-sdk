package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.model.BeaconId;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;

import util.Utils;

import static util.Utils.hexStringToByteArray;
import static util.Utils.wrapWithZeroBytes;

public class TestBluetoothPlatform implements BluetoothPlatform {

    public static final String ADVERTISEMENT_DATA_FLAGS = "020106";
    public static final String ADVERTISEMENT_DATA_FLAGS_ANDROID_NEXUS_9 = "020102";
    public static final String ADVERTISEMENT_DATA_FLAGS_ESTIMOTE_STICKERS = "020104";
    public static final String DIFFERENT_ADVERTISEMENT_DATA_FLAGS_1 = "029419";
    public static final String DIFFERENT_ADVERTISEMENT_DATA_FLAGS_2 = "02011a";
    public static final String INVALID_ADVERTISEMENT_DATA_FLAGS = "02FFFF";
    public static final String LONGER_ADVERTISEMENT_DATA_FLAGS = "0301061a";
    public static final String SHORTER_ADVERTISEMENT_DATA_FLAGS = "0132";

    public static final String IBEACON_HEADER = "1aff4c000215";

    public static final String ACCENT_SYSTEMS_IBEACON_HEADER_WITH_BATTERY_READOUT = "1bff4c000215";

    public static final String ALTBEACON_HEADER = "1bffc300beac"; // adidas was randomly chosen as manufacturer, any other non apple would fit.

    public static final String SENSORBERG_PROXIMITY_UUID_0 = "7367672374000000FFFF0000FFFF0000";
    public static final String ALIEN_PROXIMITY_UUID = "08077023A6C343FB91FA4EE34D8782E8";
    public static final String ESTIMOTE_STICKER_PROXIMITY_UUID = "2A8136699373D057C85E4D8F4AF794A9";
    public static final String MAJOR_ID = "0111"; //DEC: 273
    public static final String MINOR_ID = "0111"; //DEC: 273

    public static final String BEACON_ID_1 = SENSORBERG_PROXIMITY_UUID_0 + MAJOR_ID + MINOR_ID;
    public static final String ALIEN_ID_1 = ALIEN_PROXIMITY_UUID + MAJOR_ID + MINOR_ID;
    public static final String ESTIMOTE_ID = ESTIMOTE_STICKER_PROXIMITY_UUID + MAJOR_ID + MINOR_ID;

    public static final String CALIBRATED_TX_VALUE = "C6"; //DEC: -58
    public static final String ACCENT_SYSTEMS_IBEACON_FOOTER_WITH_BATTERY_STATUS = "42"; //66%
    public static final String ALT_BEACON_FOOTER = "23"; //Manufacturer specific (Alt beacon Spec)


    public static final byte[] BYTES_FOR_BEACON_WITHOUT_FLAGS = hexStringToByteArray( IBEACON_HEADER + ALIEN_ID_1 + CALIBRATED_TX_VALUE );

    public static final byte[] BYTES_FOR_BEACON_WITH_DIFFERENT_FLAGS_1 = hexStringToByteArray( DIFFERENT_ADVERTISEMENT_DATA_FLAGS_1 + IBEACON_HEADER + ALIEN_ID_1 + CALIBRATED_TX_VALUE );
    public static final byte[] BYTES_FOR_BEACON_WITH_DIFFERENT_FLAGS_2 = hexStringToByteArray( DIFFERENT_ADVERTISEMENT_DATA_FLAGS_2 + IBEACON_HEADER + ALIEN_ID_1 + CALIBRATED_TX_VALUE );
    public static final byte[] BYTES_FOR_BEACON_WITH_NEXUS9_FLAGS = hexStringToByteArray( ADVERTISEMENT_DATA_FLAGS_ANDROID_NEXUS_9 + IBEACON_HEADER + ALIEN_ID_1 + CALIBRATED_TX_VALUE );
    public static final byte[] BYTES_FOR_ESTIMOTE_STICKER_BEACON = hexStringToByteArray( ADVERTISEMENT_DATA_FLAGS_ESTIMOTE_STICKERS + IBEACON_HEADER + ESTIMOTE_ID + CALIBRATED_TX_VALUE );

    public static final byte[] BYTES_FOR_BEACON_WITH_ABSTRUSE_VARIATION_1 = hexStringToByteArray( LONGER_ADVERTISEMENT_DATA_FLAGS + IBEACON_HEADER + ALIEN_ID_1 + CALIBRATED_TX_VALUE );
    public static final byte[] BYTES_FOR_BEACON_WITH_ABSTRUSE_VARIATION_2 = hexStringToByteArray( SHORTER_ADVERTISEMENT_DATA_FLAGS + IBEACON_HEADER + ALIEN_ID_1 + CALIBRATED_TX_VALUE );
    public static final byte[] BYTES_FOR_BEACON_WITH_ABSTRUSE_VARIATION_3 = hexStringToByteArray( INVALID_ADVERTISEMENT_DATA_FLAGS + IBEACON_HEADER + ALIEN_ID_1 + CALIBRATED_TX_VALUE );

    public static final byte[] BYTES_FOR_BEACON_WITH_ACCENT_STYLE_BATTERY = hexStringToByteArray( ADVERTISEMENT_DATA_FLAGS + ACCENT_SYSTEMS_IBEACON_HEADER_WITH_BATTERY_READOUT + BEACON_ID_1 + CALIBRATED_TX_VALUE + ACCENT_SYSTEMS_IBEACON_FOOTER_WITH_BATTERY_STATUS );

    public static final byte[] BYTES_FOR_ALTBEACON_WITHOUT_FLAGS = hexStringToByteArray( ALTBEACON_HEADER + BEACON_ID_1 + CALIBRATED_TX_VALUE + ALT_BEACON_FOOTER );
    public static final byte[] BYTES_FOR_ALTBEACON_WITH_FLAGS = hexStringToByteArray( ADVERTISEMENT_DATA_FLAGS + ALTBEACON_HEADER + BEACON_ID_1 + CALIBRATED_TX_VALUE + ALT_BEACON_FOOTER );

    public static final byte[] BYTES_FOR_OTHER_BT_DEVICE_1 = hexStringToByteArray( "1DF5E591493F40F8B8FD716280C66358F52289B9C58C460692340DE138CE" );
    public static final byte[] BYTES_FOR_OTHER_BT_DEVICE_2 = hexStringToByteArray( "0011223344556677889900112233445566778899001122334455667788990011223344556677889900112233445566778899001122334455667788990011" );
    public static final byte[] BYTES_FOR_OTHER_BT_DEVICE_3 = hexStringToByteArray( "0201120100" );
    public static final byte[] BYTES_FOR_OTHER_BT_DEVICE_4 = hexStringToByteArray( "02011A14FF4C0001000000000000000000000004" );
    public static final byte[] BYTES_FOR_OTHER_BT_DEVICE_5 = hexStringToByteArray( "02011A0BFF4C0009060190AC110733" );

    public static final byte[] NON_STANDART_BYTES_THAT_FILLED_WITH_ZEROS_PRODUCE_A_BEACON = hexStringToByteArray( "0201061aff4c000215010203" ); // Left out on purpose. Impossible to catch without extreme workload.

    public static final BeaconId EXPECTED_BEACON_1 = new BeaconId(Utils.hexStringToByteArray(BEACON_ID_1));
    public static final BeaconId EXPECTED_ALIEN_1 = new BeaconId(Utils.hexStringToByteArray(ALIEN_ID_1));
    public static final BeaconId EXPECTED_ESTIMOTE_ID = new BeaconId(Utils.hexStringToByteArray(ESTIMOTE_ID));

    public static final byte[] BYTES_FOR_BEACON_1 = hexStringToByteArray( ADVERTISEMENT_DATA_FLAGS + IBEACON_HEADER + BEACON_ID_1 + CALIBRATED_TX_VALUE );
    public static final byte[] BYTES_FOR_SENSORBERG_BEACON_1 = hexStringToByteArray( ADVERTISEMENT_DATA_FLAGS + IBEACON_HEADER + BEACON_ID_1 + CALIBRATED_TX_VALUE );
    public static final byte[] BYTES_FOR_ALIEN_BEACON_1 = hexStringToByteArray( ADVERTISEMENT_DATA_FLAGS + IBEACON_HEADER + ALIEN_ID_1 + CALIBRATED_TX_VALUE );

    private BluetoothAdapter.LeScanCallback scanCallback;

    @Override
    public boolean isBluetoothLowEnergyDeviceTurnedOn() {
        return true;
    }

    @Override
    public boolean isBluetoothLowEnergySupported() {
        return true;
    }

    @Override
    public void startLeScan(BluetoothAdapter.LeScanCallback scanCallback) {
        this.scanCallback = scanCallback;
    }

    @Override
    public void stopLeScan() {
        this.scanCallback = null;
    }

    @Override
    public boolean isLeScanRunning() {
        return this.scanCallback != null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void fakeIBeaconSighting() {
        fakeIBeaconSighting(BYTES_FOR_BEACON_1);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void fakeSensorbergIBeaconSighting() {
        fakeIBeaconSighting(BYTES_FOR_SENSORBERG_BEACON_1);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void fakeAlienIBeaconSighting() {
        fakeIBeaconSighting(BYTES_FOR_ALIEN_BEACON_1);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void fakeIBeaconSighting(byte[] bytesForFakeScan){
        if (this.scanCallback != null){
            this.scanCallback.onLeScan(null, -100, wrapWithZeroBytes(bytesForFakeScan, 62));
        }
    }
}
