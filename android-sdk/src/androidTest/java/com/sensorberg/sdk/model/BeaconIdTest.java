package com.sensorberg.sdk.model;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class BeaconIdTest {

    BeaconId beacon1_from_byte_array = new BeaconId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
    BeaconId beacon1_from_byte_array_zero_offset = new BeaconId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19}, 0);
    BeaconId beacon1_from_byte_array_with_offset = new BeaconId(new byte[]{0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19}, 1);
    BeaconId beacon1_from_string = new BeaconId("000102030405060708090A0B0C0D0E0F10111213");
    BeaconId beacon2_from_string = new BeaconId("000102030405060700090A0B0C0D0E0F10111213");

    @Test
    public void beacon_from_array_equals_beacon_from_string() {
        assertThat(beacon1_from_byte_array).isEqualTo(beacon1_from_string);
    }

    @Test
    public void beacon_from_array_equals_beacon_from_array_with_zero_offset() {
        assertThat(beacon1_from_byte_array).isEqualTo(beacon1_from_byte_array_zero_offset);
    }

    @Test
    public void beacon_from_array_equals_beacon_from_array_with_offset() {
        assertThat(beacon1_from_byte_array).isEqualTo(beacon1_from_byte_array_with_offset);
    }

    @Test
    public void beacon1_not_equals_beacon2() {
        assertThat(beacon1_from_byte_array).isNotEqualTo(beacon2_from_string);
        assertThat(beacon1_from_string).isNotEqualTo(beacon2_from_string);
    }

    @Test
    public void beacon1_uuid_constructor_equals_beacon1_from_array() {
        BeaconId id = new BeaconId(beacon1_from_byte_array.getUuid(), beacon1_from_byte_array.getMajorId(), beacon1_from_byte_array.getMinorId());
        assertThat(beacon1_from_byte_array).isEqualTo(id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void beacon_array_constructor_too_short() {
        BeaconId id = new BeaconId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18});
    }

    @Test(expected = IllegalArgumentException.class)
    public void beacon_array_with_offset_constructor_with_null() {
        BeaconId id = new BeaconId(null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void beacon_array_with_offset_constructor_too_short() {
        BeaconId id = new BeaconId(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19}, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void beacon_string_constructor_too_short() {
        BeaconId id = new BeaconId("000102030405060708090A0B0C0D0E0F1011121");
    }

    @Test(expected = IllegalArgumentException.class)
    public void beacon_string_constructor_too_long() {
        BeaconId id = new BeaconId("000102030405060708090A0B0C0D0E0F101112131");
    }

    @Test(expected = IllegalArgumentException.class)
    public void beacon_string_constructor_invalid_values() {
        BeaconId id = new BeaconId("000102030409960708090J0B0C0D0E0F1011121");
    }

    @Test
    public void beacon_describe_contents_is_0() {
        assertThat(beacon1_from_byte_array.describeContents()).isEqualTo(0);
    }

    @Test
    public void beacon_to_string_not_empty() {
        assertThat(beacon1_from_byte_array.toString()).isNotEmpty();
    }
}
