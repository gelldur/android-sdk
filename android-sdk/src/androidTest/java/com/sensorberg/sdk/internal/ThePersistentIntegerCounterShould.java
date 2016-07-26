package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.settings.SharedPreferencesKeys;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
public class ThePersistentIntegerCounterShould {

    PersistentIntegerCounter tested;

    private PersistentIntegerCounter testedCloseToTheEnd;

    private SharedPreferences settings;

    @Before
    public void setUp() throws Exception {
        tested = new PersistentIntegerCounter(
                InstrumentationRegistry.getContext().getSharedPreferences(String.valueOf(System.currentTimeMillis()), Context.MODE_PRIVATE));

        settings = InstrumentationRegistry.getContext().getSharedPreferences(String.valueOf(System.currentTimeMillis()), Context.MODE_PRIVATE);
        settings.edit().putInt(SharedPreferencesKeys.Platform.POST_TO_SERVICE_COUNTER, Integer.MAX_VALUE - 1).apply();

        testedCloseToTheEnd = new PersistentIntegerCounter(settings);
    }

    @Test
    public void test_should_count_up() throws Exception {
        int first = tested.next();

        Assertions.assertThat(first).isEqualTo(tested.next() - 1);
    }

    @Test
    public void test_should_jump_back_to_0_when_getting_to_the_end_of_the_integer_number_space() throws Exception {
        testedCloseToTheEnd.next();

        Assertions.assertThat(testedCloseToTheEnd.next()).isEqualTo(0);
    }

    @Test
    public void test_should_not_jump_back_to_0_at_Integer_MAX_VALUE_Minus2() throws Exception {
        Assertions.assertThat(testedCloseToTheEnd.next()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void test_values_should_be_persisted() throws Exception {
        testedCloseToTheEnd.next();
        testedCloseToTheEnd.next();
        testedCloseToTheEnd.next();

        int lastOfOtherInstance = testedCloseToTheEnd.next();
        PersistentIntegerCounter otherInstanceSameSharedPrefs = new PersistentIntegerCounter(settings);

        Assertions.assertThat(otherInstanceSameSharedPrefs.next()).isEqualTo(lastOfOtherInstance + 1);
    }
}
