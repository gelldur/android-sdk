package com.sensorberg.sdk.action;

import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import util.TestConstants;

@RunWith(AndroidJUnit4.class)
public class TestTheUriMessageAction {

    @Test
    public void test_parcelable(){
        UriMessageAction tested = TestConstants.getUriMessageAction();
        Parcel output = Parcel.obtain();

        tested.writeToParcel(output, 0);
        output.setDataPosition(0);

        UriMessageAction copy = UriMessageAction.CREATOR.createFromParcel(output);

        Assertions.assertThat(copy.getUri()).isEqualTo(tested.getUri());
        Assertions.assertThat(copy.getTitle()).isEqualTo(tested.getTitle());
        Assertions.assertThat(copy.getContent()).isEqualTo(tested.getContent());

        Assertions.assertThat(copy.getType()).isEqualTo(ActionType.MESSAGE_URI);
    }
}
