package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.action.UriMessageAction;
import com.sensorberg.sdk.resolver.BeaconEvent;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class TheSQLiteStoreShould {

    private static final Action URI_ACTION = new UriMessageAction(UUID.randomUUID(), "title", "content", "http://something.com", null, 3000, UUID.randomUUID().toString());
    public static final String BEACON_EVENT_KEY = "beaconEvent";
    public static final int TIMESTAMP_OF_EVENT = 4000;
    private static final int IRRELEVANT = 1;
    SQLiteStore tested;
    private Bundle bundle;

    @Before
    public void setUp() throws Exception {
        tested = new SQLiteStore(Long.toString(System.currentTimeMillis()), InstrumentationRegistry.getContext());

        BeaconEvent beaconEvent = new BeaconEvent.Builder()
                .withAction(URI_ACTION)
                .build();
        bundle = new Bundle();
        bundle.putParcelable(BEACON_EVENT_KEY, beaconEvent);
    }

    @Test
    public void test_should_initially_be_empty() throws Exception {
        Assertions.assertThat(tested.size()).isEqualTo(0);
    }

    @Test
    public void test_should_store_a_ScanEvent() throws Exception {
        tested.put(new SQLiteStore.Entry(1, TIMESTAMP_OF_EVENT, IRRELEVANT, bundle));

        Assertions.assertThat(tested.size()).isEqualTo(1);
    }

    @Test
    public void test_should_delete_a_scanEvent() throws Exception {
        tested.put(new SQLiteStore.Entry(1, TIMESTAMP_OF_EVENT, IRRELEVANT, bundle));
        tested.delete(1);
        Assertions.assertThat(tested.size()).isEqualTo(0);
    }

    @Test
    public void test_load_all_entries_to_memory() throws Exception {
        tested.put(new SQLiteStore.Entry(1, TIMESTAMP_OF_EVENT, IRRELEVANT, bundle));

        ArrayList<SQLiteStore.Entry> all = tested.loadRegistry();
        Assertions.assertThat(all).isNotNull().hasSize(1);
    }

    @Test
    public void test_should_restore_the_content_of_an_entry() throws Exception {
        tested.put(new SQLiteStore.Entry(1, TIMESTAMP_OF_EVENT, IRRELEVANT, bundle));

        ArrayList<SQLiteStore.Entry> all = tested.loadRegistry();
        SQLiteStore.Entry entry = all.get(0);
        Assertions.assertThat((BeaconEvent) entry.bundle.get(BEACON_EVENT_KEY)).isEqualsToByComparingFields((BeaconEvent) bundle.get(BEACON_EVENT_KEY));
    }

    @Test
    public void test_should_not_restore_entries_that_are_expired() throws Exception {
        tested.put(new SQLiteStore.Entry(1, TIMESTAMP_OF_EVENT, IRRELEVANT, bundle));

        tested.deleteOlderThan(TIMESTAMP_OF_EVENT +1);

        Assertions.assertThat(tested.size()).isEqualTo(0);
    }

    @Test
    public void test_should_be_able_to_delete_one_entry() throws Exception {
        tested.put(new SQLiteStore.Entry(1, TIMESTAMP_OF_EVENT, IRRELEVANT, bundle));
        tested.put(new SQLiteStore.Entry(2, TIMESTAMP_OF_EVENT, IRRELEVANT, bundle));

        tested.delete(2);

        Assertions.assertThat(tested.size()).isEqualTo(1);
    }

    @Test
    public void test_clear_all_entries() throws Exception {
        tested.put(new SQLiteStore.Entry(1, TIMESTAMP_OF_EVENT, IRRELEVANT, bundle));
        tested.put(new SQLiteStore.Entry(2, TIMESTAMP_OF_EVENT, IRRELEVANT, bundle));

        Assertions.assertThat(tested.size()).isEqualTo(2).overridingErrorMessage("two values should have been added");

        tested.clear();

        Assertions.assertThat(tested.size()).isEqualTo(0);
    }
}
