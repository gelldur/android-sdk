package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.testUtils.TestBluetoothPlatform;

import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;
import android.util.Pair;

import static util.Utils.wrapWithZeroBytes;

@RunWith(AndroidJUnit4.class)
public class TheScannerHelperWithZeroBytesShould {

    @Test
    public void find_a_beacon() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_BEACON_1, 62));

        Assertions.assertThat(beacon.first).isEqualTo(TestBluetoothPlatform.EXPECTED_BEACON_1);
        Assertions.assertThat(beacon.second).isEqualTo(-58);
    }

    @Test
    public void find_a_beacon_with_sensorberg_id() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_SENSORBERG_BEACON_1, 62));

        Assertions.assertThat(beacon.first).isEqualTo(TestBluetoothPlatform.EXPECTED_BEACON_1);
        Assertions.assertThat(beacon.second).isEqualTo(-58);
    }

    @Test
    public void find_a_beacon_with_alien_id() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_ALIEN_BEACON_1, 62));

        Assertions.assertThat(beacon.first).isEqualTo(TestBluetoothPlatform.EXPECTED_ALIEN_1);
        Assertions.assertThat(beacon.second).isEqualTo(-58);
    }

    @Test
    public void find_a_beacon_with_short_advertisement_packet() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_BEACON_WITHOUT_FLAGS, 62));

        Assertions.assertThat(beacon.first).isEqualTo(TestBluetoothPlatform.EXPECTED_ALIEN_1);
        Assertions.assertThat(beacon.second).isEqualTo(-58);
    }

    @Test
    public void find_a_beacon_with_variation_in_advertisement_packet() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_BEACON_WITH_DIFFERENT_FLAGS_1, 62));

        Assertions.assertThat(beacon.first).isEqualTo(TestBluetoothPlatform.EXPECTED_ALIEN_1);
        Assertions.assertThat(beacon.second).isEqualTo(-58);
    }

    @Test
    public void find_a_beacon_with_estimote_sticker_specific_advertisement_packet() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_ESTIMOTE_STICKER_BEACON, 62));

        Assertions.assertThat(beacon.first).isEqualTo(TestBluetoothPlatform.EXPECTED_ESTIMOTE_ID);
        Assertions.assertThat(beacon.second).isEqualTo(-58);
    }

    @Test
    public void find_a_beacon_with_another_variation_in_advertisement_packet() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_BEACON_WITH_DIFFERENT_FLAGS_2, 62));

        Assertions.assertThat(beacon.first).isEqualTo(TestBluetoothPlatform.EXPECTED_ALIEN_1);
        Assertions.assertThat(beacon.second).isEqualTo(-58);
    }

    @Test
    public void find_a_beacon_with_nexus_style_flags() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_BEACON_WITH_NEXUS9_FLAGS, 62));

        Assertions.assertThat(beacon.first).isEqualTo(TestBluetoothPlatform.EXPECTED_ALIEN_1);
        Assertions.assertThat(beacon.second).isEqualTo(-58);
    }

    @Test
    public void find_a_beacon_with_weird_variation_in_advertisement_packet() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_BEACON_WITH_DIFFERENT_FLAGS_1, 62));

        Assertions.assertThat(beacon.first).isEqualTo(TestBluetoothPlatform.EXPECTED_ALIEN_1);
        Assertions.assertThat(beacon.second).isEqualTo(-58);
    }

    @Test
    public void find_a_beacon_with_accent_systems_style_battery_value_in_advertisement_packet() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_BEACON_WITH_ACCENT_STYLE_BATTERY, 62));

        Assertions.assertThat(beacon.first).isEqualTo(TestBluetoothPlatform.EXPECTED_BEACON_1);
        Assertions.assertThat(beacon.second).isEqualTo(-58);
    }

    @Test
    public void find_a_beacon_with_longer_header_in_advertisement_packet() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_BEACON_WITH_ABSTRUSE_VARIATION_1, 62));

        Assertions.assertThat(beacon.first).isEqualTo(TestBluetoothPlatform.EXPECTED_ALIEN_1);
        Assertions.assertThat(beacon.second).isEqualTo(-58);
    }

    @Test
    public void find_a_beacon_with_shorter_header_in_advertisement_packet() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_BEACON_WITH_ABSTRUSE_VARIATION_2, 62));

        Assertions.assertThat(beacon.first).isEqualTo(TestBluetoothPlatform.EXPECTED_ALIEN_1);
        Assertions.assertThat(beacon.second).isEqualTo(-58);
    }

    @Test
    public void find_a_beacon_with_invalid_header_in_advertisement_packet() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_BEACON_WITH_ABSTRUSE_VARIATION_3, 62));

        Assertions.assertThat(beacon.first).isEqualTo(TestBluetoothPlatform.EXPECTED_ALIEN_1);
        Assertions.assertThat(beacon.second).isEqualTo(-58);
    }

    @Test
    public void not_find_an_altbeacon_1() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_ALTBEACON_WITH_FLAGS, 62));

        Assertions.assertThat(beacon).isNull();
    }

    @Test
    public void not_find_an_altbeacon_2() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_ALTBEACON_WITHOUT_FLAGS, 62));

        Assertions.assertThat(beacon).isNull();
    }

    @Test
    public void not_find_other_bluetooth_devices_1() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_OTHER_BT_DEVICE_1, 62));

        Assertions.assertThat(beacon).isNull();
    }

    @Test
    public void not_find_other_bluetooth_devices_2() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_OTHER_BT_DEVICE_2, 62));

        Assertions.assertThat(beacon).isNull();
    }

    @Test
    public void not_find_other_bluetooth_devices_3() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_OTHER_BT_DEVICE_3, 62));

        Assertions.assertThat(beacon).isNull();
    }

    @Test
    public void not_find_other_bluetooth_devices_4() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_OTHER_BT_DEVICE_4, 62));

        Assertions.assertThat(beacon).isNull();
    }

    @Test
    public void not_find_other_bluetooth_devices_5() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.BYTES_FOR_OTHER_BT_DEVICE_5, 62));

        Assertions.assertThat(beacon).isNull();
    }

    @Test
    public void find_a_beacon_beacuse_of_zero_filling() {
        Pair<BeaconId, Integer> beacon = ScanHelper.getBeaconID(wrapWithZeroBytes(TestBluetoothPlatform.NON_STANDART_BYTES_THAT_FILLED_WITH_ZEROS_PRODUCE_A_BEACON, 62));

        Assertions.assertThat(beacon).isNotNull();
    }
}
