package com.sensorberg.sdk.service;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.SensorbergServiceConfiguration;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.resolver.ResolverConfiguration;
import com.sensorberg.sdk.testUtils.TestFileManager;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import java.io.File;

import javax.inject.Inject;

@RunWith(AndroidJUnit4.class)
public class TheServiceConfiguration {

    @Inject
    TestFileManager testFileManager;

    SensorbergServiceConfiguration tested;

    private String API_TOKEN = "SOMETHING";
    private String ADVERTISING_ID = "SOMETHING_ADVERTISING_ID";

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);

        ResolverConfiguration resolverConf = new ResolverConfiguration();
        resolverConf.setApiToken(API_TOKEN);
        resolverConf.setAdvertisingIdentifier(ADVERTISING_ID);

        tested = new SensorbergServiceConfiguration(resolverConf);
    }

    @Test
    public void service_configuration_should_be_serializable() throws Exception {
        File file = File.createTempFile("test" + System.currentTimeMillis(), "tmp");
        testFileManager.write(tested, file);

        SensorbergServiceConfiguration deserialized = (SensorbergServiceConfiguration) testFileManager.getContentsOfFileOrNull(file);

        Assertions.assertThat(deserialized).isNotNull();
        Assertions.assertThat(deserialized.resolverConfiguration.apiToken).isEqualTo(API_TOKEN);
        Assertions.assertThat(deserialized.resolverConfiguration.getAdvertisingIdentifier()).isEqualTo(ADVERTISING_ID);
        Assertions.assertThat(deserialized.isComplete()).isTrue();
    }
}
