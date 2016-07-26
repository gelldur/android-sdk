package com.sensorberg.sdk.resolver;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
public class TheResolverConfigurationShould {

    private static final String INITIAL_API_TOKEN = "intial";

    private static final String OTHER_API_TOKEN = "other";

    private static final String INITIAL_API_TOKEN_OTHER_OBJECT = "intial";

    private ResolverConfiguration tested;

    @Before
    public void setUp() throws Exception {
        tested = new ResolverConfiguration();
    }

    @Test
    public void test_return_true_when_changing_the_apiToken() throws Exception {
        boolean result = tested.setApiToken(INITIAL_API_TOKEN);

        Assertions.assertThat(result).isFalse().overridingErrorMessage("setting an inital value should not callback a change");
    }

    @Test
    public void test_return_true_when_changing_the_value() throws Exception {
        tested.setApiToken(INITIAL_API_TOKEN);
        boolean result = tested.setApiToken(OTHER_API_TOKEN);

        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void test_return_false_when_setting_the_same_value_again() throws Exception {
        tested.setApiToken(INITIAL_API_TOKEN);
        boolean result = tested.setApiToken(INITIAL_API_TOKEN_OTHER_OBJECT);

        Assertions.assertThat(result).isFalse();
    }
}
