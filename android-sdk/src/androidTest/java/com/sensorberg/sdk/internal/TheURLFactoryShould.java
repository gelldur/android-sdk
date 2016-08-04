package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.BuildConfig;

import org.fest.assertions.api.Assertions;

import android.app.Application;
import android.os.Build;
import android.test.ApplicationTestCase;

import java.io.UnsupportedEncodingException;

import util.TestConstants;
import util.URLAssertion;

public class TheURLFactoryShould extends ApplicationTestCase<Application> {

    private String apiKey = "doesnt-matter";

    public TheURLFactoryShould() {
        super(Application.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        createApplication();
    }

    public void test_proximity_uuid_conversion() {
        Assertions.assertThat(TestConstants.BEACON_SCAN_ENTRY_EVENT(0).getBeaconId().getNormalizedUUIDString()).isEqualTo(
                TestConstants.BEACON_ID_STRING);
    }

    public void test_should_provide_the_correct_settings_endpoint() throws UnsupportedEncodingException {
        URLAssertion.assertThat(URLFactory.getSettingsURLString(apiKey))
                .isHTTPS()
                .pathBeginsWith("/applications/" + apiKey + "/settings/android")
                .hasNoGetParameter("revision");
    }

}


