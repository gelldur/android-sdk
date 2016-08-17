package com.sensorberg.sdk.resolver;

import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.action.UriMessageAction;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.scanner.ScanEvent;

import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Bundle;
import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class ParcelTests {

    @Test
    public void test_beaconId_parcelable() {
        BeaconId beaconId = new BeaconId(UUID.randomUUID(), 1, 2);
        Parcel parcel = Parcel.obtain();
        Bundle bundle = new Bundle();
        bundle.putParcelable("some_value", beaconId);
        bundle.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        Bundle reverse = Bundle.CREATOR.createFromParcel(parcel);
        reverse.setClassLoader(BeaconId.class.getClassLoader());
        BeaconId beaconId2 = reverse.getParcelable("some_value");

        Assertions.assertThat(beaconId.getMajorId()).isEqualTo(beaconId2.getMajorId());
        Assertions.assertThat(beaconId.getMinorId()).isEqualTo(beaconId2.getMinorId());
        Assertions.assertThat(beaconId.getUuid()).isEqualTo(beaconId2.getUuid());

        Assertions.assertThat(beaconId).isEqualTo(beaconId2);
    }

    @Test
    public void test_beaconId() {
        BeaconId beaconId = new BeaconId(UUID.randomUUID(), 1, 2);
        Parcel parcel = Parcel.obtain();
        beaconId.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        BeaconId beaconId2 = BeaconId.CREATOR.createFromParcel(parcel);

        Assertions.assertThat(beaconId.getMajorId()).isEqualTo(beaconId2.getMajorId());
        Assertions.assertThat(beaconId.getMinorId()).isEqualTo(beaconId2.getMinorId());
        Assertions.assertThat(beaconId.getUuid()).isEqualTo(beaconId2.getUuid());

        Assertions.assertThat(beaconId).isEqualTo(beaconId2);
    }

    @Test
    public void test_action_parcelable() {
        UriMessageAction action = new UriMessageAction(UUID.randomUUID(), "title", "content", "foo.bar", null, 0);

        try {
            Parcel parcel = Parcel.obtain();
            Bundle bundle = new Bundle();
            bundle.putParcelable("some_value", action);
            bundle.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);

            Bundle reverse = Bundle.CREATOR.createFromParcel(parcel);
            reverse.setClassLoader(BeaconId.class.getClassLoader());
            UriMessageAction action2 = reverse.getParcelable("some_value");

            Assertions.assertThat(action.getTitle()).isEqualTo(action2.getTitle());
            Assertions.assertThat(action.getContent()).isEqualTo(action2.getContent());
            Assertions.assertThat(action.getUri()).isEqualTo(action2.getUri());
            Assertions.assertThat(action.getType()).isEqualTo(action2.getType());

            Assertions.assertThat(action2).isEqualTo(action);
        } catch (Exception e) {
            Assertions.fail("could not parcel the BeaconEvent " + e.getMessage());
        }
    }

    @Test
    public void test_ScanEvent() {
        ScanEvent scanEvent = new ScanEvent.Builder()
                .withEventTime(10)
                .withBeaconId(new BeaconId(UUID.randomUUID(), 1, 1))
                .withEventTime(12)
                .build();

        try {
            Parcel parcel = Parcel.obtain();
            Bundle bundle = new Bundle();
            bundle.putParcelable("some_value", scanEvent);
            bundle.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);

            Bundle reverse = Bundle.CREATOR.createFromParcel(parcel);
            reverse.setClassLoader(BeaconId.class.getClassLoader());
            ScanEvent scanEvent2 = reverse.getParcelable("some_value");

            Assertions.assertThat(scanEvent.getBeaconId()).isEqualTo(scanEvent2.getBeaconId());
            Assertions.assertThat(scanEvent.getEventTime()).isEqualTo(scanEvent2.getEventTime());

            Assertions.assertThat(scanEvent2).isEqualTo(scanEvent);
        } catch (Exception e) {
            Assertions.fail("could not parcel the BeaconEvent " + e.getMessage());
        }

    }

    @Test
    public void testBeaconEvent() {
        Action action = new UriMessageAction(UUID.randomUUID(), "title", "content", "foo.bar", null, 0);
        BeaconEvent event = new BeaconEvent.Builder()
                .withAction(action)
                .build();

        try {
            Parcel parcel = Parcel.obtain();
            event.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);

            BeaconEvent beaconEvent2 = BeaconEvent.CREATOR.createFromParcel(parcel);

            Assertions.assertThat(beaconEvent2.getAction()).isEqualTo(event.getAction());
            Assertions.assertThat(beaconEvent2.getAction()).isEqualTo(event.getAction());

            Assertions.assertThat(beaconEvent2).isEqualTo(event);
        } catch (Exception e) {
            Assertions.fail("could not parcel the BeaconEvent " + e.getMessage());
        }
    }

    @Test
    public void testBeaconEvent_withBundle() {
        Action action = new UriMessageAction(UUID.randomUUID(), "title", "content", "foo.bar", null, 0);
        BeaconEvent event = new BeaconEvent.Builder()
                .withAction(action)
                .build();

        try {
            Parcel parcel = Parcel.obtain();
            Bundle bundle = new Bundle();
            bundle.putParcelable("some_value", event);
            bundle.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);

            Bundle reverse = Bundle.CREATOR.createFromParcel(parcel);
            reverse.setClassLoader(BeaconId.class.getClassLoader());
            BeaconEvent beaconEvent2 = reverse.getParcelable("some_value");

            Assertions.assertThat(beaconEvent2.getAction()).isEqualTo(event.getAction());
            Assertions.assertThat(beaconEvent2.getAction()).isEqualTo(event.getAction());

            Assertions.assertThat(beaconEvent2).isEqualTo(event);
        } catch (Exception e) {
            Assertions.fail("could not parcel the BeaconEvent " + e.getMessage());
        }
    }
}
