package com.sensorberg.sdk.resolver;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
public class TheResolutionConfigurationShould {

    ResolutionConfiguration tested;

    @Before
    public void setUp() throws Exception {
        tested = new ResolutionConfiguration();
        tested.maxRetries = 3;
    }

    @Test
    public void test_should_by_default_setup_to_allow_an_request() throws Exception {
        Assertions.assertThat(tested.canTry()).isTrue();
    }

    @Test
    public void test_should_allow_3_retries() throws Exception {
        tested.retry++;
        tested.retry++;
        tested.retry++;

        Assertions.assertThat(tested.canTry()).isTrue();
        Assertions.assertThat(tested.canRetry()).isFalse();

    }

    @Test
    public void test_should_plus_plus_the_retries(){
        long origValue = tested.retry;
        tested.retry++;

        Assertions.assertThat(origValue).isEqualTo(tested.retry-1);
    }
}
