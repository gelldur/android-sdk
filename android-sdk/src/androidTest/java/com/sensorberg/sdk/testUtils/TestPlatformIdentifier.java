package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;

import java.util.UUID;

public class TestPlatformIdentifier implements PlatformIdentifier {

    public static final UUID deviceInstallationIdentifier = UUID.randomUUID();

    public static String googleAdertiserIdentifier = "google" + UUID.randomUUID();

    @Override
    public String getUserAgentString() {
        return "something";
    }

    @Override
    public String getDeviceInstallationIdentifier() {
        return deviceInstallationIdentifier.toString();
    }

    @Override
    public String getAdvertiserIdentifier() {
        return googleAdertiserIdentifier;
    }

    @Override
    public void setAdvertisingIdentifier(String advertisingIdentifier) {
        googleAdertiserIdentifier = advertisingIdentifier;
    }
}
