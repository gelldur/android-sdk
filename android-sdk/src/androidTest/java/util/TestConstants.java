package util;

import com.sensorberg.sdk.SensorbergServiceConfiguration;
import com.sensorberg.sdk.SensorbergServiceIntents;
import com.sensorberg.sdk.SensorbergServiceMessage;
import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.action.UriMessageAction;
import com.sensorberg.sdk.action.VisitWebsiteAction;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolverConfiguration;
import com.sensorberg.sdk.scanner.ScanEvent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.net.URL;
import java.util.UUID;

public class TestConstants {


    //    https://manage.sensorberg.com/#/beacon/edit/7e7c8ae1-e593-404e-a131-b4cbf149ed1d
    public static final BeaconId DELAY_BEACON_ID = new BeaconId(UUID.fromString("73676723-7400-0000-FFFF-0000FFFF0004"), 64202, 20003);

    public static final BeaconId LEET_BEACON_ID_1 = new BeaconId(UUID.fromString("73676723-7400-0000-FFFF-0000FFFF0001"), 1337, 1337);

    public static final BeaconId LEET_BEACON_ID_2 = new BeaconId(UUID.fromString("73676723-7400-0000-FFFF-0000FFFF0002"), 1337, 1337);

    public static final BeaconId LEET_BEACON_ID_3 = new BeaconId(UUID.fromString("73676723-7400-0000-FFFF-0000FFFF0003"), 1337, 1337);

    public static final BeaconId LEET_BEACON_ID_4 = new BeaconId(UUID.fromString("73676723-7400-0000-FFFF-0000FFFF0004"), 1337, 1337);

    public static final BeaconId LEET_BEACON_ID_5 = new BeaconId(UUID.fromString("73676723-7400-0000-FFFF-0000FFFF0005"), 1337, 1337);

    public static final BeaconId LEET_BEACON_ID_6 = new BeaconId(UUID.fromString("73676723-7400-0000-FFFF-0000FFFF0006"), 1337, 1337);

    public static final BeaconId LEET_BEACON_ID_7 = new BeaconId(UUID.fromString("73676723-7400-0000-FFFF-0000FFFF0007"), 1337, 1337);

    public static final BeaconId REGULAR_BEACON_ID = new BeaconId(UUID.fromString("192E463C-9B8E-4590-A23F-D32007299EF5"), 1337, 1337);


    public static final String API_TOKEN = "f257de3b91d141aa93b6a9b39c97b83df257de3b91d141aa93b6a9b39c97b83d";

    public static final String API_TOKEN_NO_SETTINGS = "9fa8eb37c8fe143cfee54def807bd778d05a8fa87c232d21aa9a45d4bd919181";

    public static final String API_TOKEN_DEFAULT = "0000000000000000000000000000000000000000000000000000000000000000";

    public static final BeaconId IN_APP_BEACON_ID = new BeaconId(UUID.fromString("73676723-7400-0000-FFFF-0000FFFF0005"), 59364, 12297);


    public static final UUID BEACON_PROXIMITY_ID = UUID.fromString("192E463C-9B8E-4590-A23F-D32007299EF5");

    public static final String BEACON_ID_STRING = "192e463c9b8e4590a23fd32007299ef5";

    public static final int MAJOR = 1337;

    public static final int MINOR = 1337;

    public static final Long REVISION = 5L;

    public static final BeaconId ANY_BEACON_ID = new BeaconId(BEACON_PROXIMITY_ID, MAJOR, MINOR);

    public static final BeaconId ANY_OTHER_BEACON_ID = new BeaconId(BEACON_PROXIMITY_ID, MAJOR + 1, MINOR + 2);


    public static BeaconId randomBeaconId() {
        return new BeaconId(UUID.randomUUID(), 1, 1);
    }

    public static ScanEvent REGULAR_BEACON_SCAN_EVENT(long now) {
        return new ScanEvent.Builder()
                .withBeaconId(TestConstants.REGULAR_BEACON_ID)
                .withEntry(true)
                .withEventTime(now)
                .build();
    }

    public static BeaconEvent BEACON_EVENT_IN_FUTURE = new BeaconEvent.Builder()
            .withAction(new VisitWebsiteAction(UUID.randomUUID(), "foo", "bar", null, null, 0))
            .withPresentationTime(1337)
            .build();

    public static final String ACTION_MESSAGE = "message";

    public static final String ACTION_TITLE = "title";

    public static final String ACTION_URL = "http://www.sensorberg.com";

    public static InAppAction getInAppAction() {
        return new InAppAction(UUID.randomUUID(), TestConstants.ACTION_MESSAGE, ACTION_TITLE, ACTION_URL, null, 0);
    }

    public static UriMessageAction getUriMessageAction() {
        return new UriMessageAction(UUID.randomUUID(), ACTION_MESSAGE, ACTION_TITLE, ACTION_URL, null, 0);
    }

    public static VisitWebsiteAction getVisitWebsiteAction() {
        return new VisitWebsiteAction(UUID.randomUUID(), ACTION_MESSAGE, ACTION_TITLE, Uri.parse(ACTION_URL), null, 0);
    }

    public static BeaconEvent getBeaconEvent() {
        return new BeaconEvent.Builder()
                .withBeaconId(ANY_BEACON_ID)
                .withAction(new InAppAction(TestConstants.BEACON_PROXIMITY_ID, "irrelevant", "irrelevant", null, null, 0))
                .withSendOnlyOnce(true)
                .withPresentationTime(0)
                .build();
    }

    public static SensorbergServiceConfiguration getDiskConfiguration() throws Exception {
        SensorbergServiceConfiguration diskConf = new SensorbergServiceConfiguration(new ResolverConfiguration());
        diskConf.resolverConfiguration.setApiToken("123456");
        diskConf.resolverConfiguration.setAdvertisingIdentifier("123456");
        diskConf.resolverConfiguration.setResolverLayoutURL(new URL("http://resolver-new.sensorberg.com"));

        return diskConf;
    }

    public static Intent getInvalidResolverEndpointIntent(Context ctx) {
        Intent serviceIntent = SensorbergServiceIntents.getServiceIntentWithMessage(ctx, SensorbergServiceMessage.MSG_TYPE_SET_RESOLVER_ENDPOINT);
        serviceIntent.putExtra(SensorbergServiceMessage.MSG_SET_RESOLVER_ENDPOINT_ENDPOINT_URL, "this is not a valid url");

        return serviceIntent;
    }
}
