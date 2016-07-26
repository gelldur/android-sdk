package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.model.BeaconId;

import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class TheBeaconId{

    private static final BeaconId BEACON_ID_1 = new BeaconId(UUID.fromString("73676723-7400-0000-ffff-0000ffff0001"), 1, 1);

    private static final BeaconId BEACON_ID_2 = new BeaconId(UUID.fromString("73676723-7400-0000-FFFF-0000FFFF0001"), 1, 1);

    @Test
    public void test_should_be_equals() throws Exception {
       Assertions.assertThat(BEACON_ID_1.equals(BEACON_ID_2)).isTrue();
    }

    @Test
    public void test_hash_sbould_be_the_same() throws Exception {
        Assertions.assertThat(BEACON_ID_1.hashCode()).isEqualTo(BEACON_ID_2.hashCode());
    }

    @Test
    public void test_bid_generation(){
        Assertions.assertThat(BEACON_ID_1.getPid()).isEqualToIgnoringCase("7367672374000000ffff0000ffff00010000100001");
        Assertions.assertThat(BEACON_ID_2.getPid()).isEqualToIgnoringCase("7367672374000000ffff0000ffff00010000100001");
    }

    @Test
    public void test_proximityUUID_withoutDashes_Generation(){
        Assertions.assertThat(BEACON_ID_1.getProximityUUIDWithoutDashes()).isEqualTo("7367672374000000ffff0000ffff0001");
        Assertions.assertThat(BEACON_ID_2.getProximityUUIDWithoutDashes()).isEqualTo("7367672374000000ffff0000ffff0001");
    }
}
