package com.sensorberg.sdk.model.server;

import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
public class TheTimeFrameShould {

    @Test
    public void be_valid_when_start_time_is_null() throws Exception {
        Timeframe tested = new Timeframe(null, 1000L);

        Assertions.assertThat(tested.valid(1)).isTrue();
        Assertions.assertThat(tested.valid(1000)).isTrue();
        Assertions.assertThat(tested.valid(1001)).isFalse();
    }

    @Test
    public void be_valid_when_end_time_is_null() throws Exception {
        Timeframe tested = new Timeframe(1000L, null);

        Assertions.assertThat(tested.valid(1)).isFalse();
        Assertions.assertThat(tested.valid(1000)).isTrue();
        Assertions.assertThat(tested.valid(1001)).isTrue();
    }

    @Test
    public void answer_correctly_with_both_values() throws Exception {
        Timeframe tested = new Timeframe(0L, 1000L);

        Assertions.assertThat(tested.valid(-1)).isFalse();
        Assertions.assertThat(tested.valid(1)).isTrue();
        Assertions.assertThat(tested.valid(1000)).isTrue();
        Assertions.assertThat(tested.valid(1001)).isFalse();
    }
}
