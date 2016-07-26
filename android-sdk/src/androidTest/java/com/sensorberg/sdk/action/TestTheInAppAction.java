package com.sensorberg.sdk.action;

import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import util.TestConstants;

@RunWith(AndroidJUnit4.class)
public class TestTheInAppAction {

    @Test
    public void test_parcelable(){
        InAppAction tested = TestConstants.getInAppAction();
        Parcel output = Parcel.obtain();

        tested.writeToParcel(output, 0);
        output.setDataPosition(0);

        InAppAction copy = InAppAction.CREATOR.createFromParcel(output);

        Assertions.assertThat(copy.getUri()).isEqualTo(tested.getUri());
        Assertions.assertThat(copy.getSubject()).isEqualTo(tested.getSubject());
        Assertions.assertThat(copy.getBody()).isEqualTo(tested.getBody());

        Assertions.assertThat(copy.getType()).isEqualTo(ActionType.MESSAGE_IN_APP);
    }
}
