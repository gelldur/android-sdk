package com.sensorberg.sdk.action;

import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import util.TestConstants;

@RunWith(AndroidJUnit4.class)
public class TestTheVisitWebsiteAction {

    @Test
    public void test_parcelable(){
        VisitWebsiteAction tested = TestConstants.getVisitWebsiteAction();
        Parcel output = Parcel.obtain();

        tested.writeToParcel(output, 0);
        output.setDataPosition(0);

        VisitWebsiteAction copy = VisitWebsiteAction.CREATOR.createFromParcel(output);

        Assertions.assertThat(copy.getUri()).isEqualTo(tested.getUri());
        Assertions.assertThat(copy.getSubject()).isEqualTo(tested.getSubject());
        Assertions.assertThat(copy.getBody()).isEqualTo(tested.getBody());

        Assertions.assertThat(copy.getType()).isEqualTo(ActionType.MESSAGE_WEBSITE);
    }
}
