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
        URLAssertion.assertThat(URLFactory.getSettingsURLString(null, apiKey))
                .isHTTPS()
                .pathBeginsWith("/api/applications/" + apiKey + "/settings/android/" + BuildConfig.SDK_VERSION)
                .hasNoGetParameter("revision");
    }

    public void test_should_provide_the_correct_settings_containing_the_android_release_name() throws UnsupportedEncodingException {
        URLAssertion.assertThat(URLFactory.getSettingsURLString(TestConstants.REVISION, apiKey))
                .pathContains(Build.VERSION.RELEASE);
    }

    public void test_should_provide_the_correct_settings_containing_the_information_about_the_model() throws UnsupportedEncodingException {
        URLAssertion.assertThat(URLFactory.getSettingsURLString(TestConstants.REVISION, apiKey))
                .pathContains(android.os.Build.MODEL)
                .pathContains(android.os.Build.PRODUCT);
    }

    public void test_should_provide_the_correct_settings_containing_name_of_the_sdk_release() throws UnsupportedEncodingException {
        URLAssertion.assertThat(URLFactory.getSettingsURLString(TestConstants.REVISION, apiKey))
                .pathContains(Build.VERSION.RELEASE);
    }


}
