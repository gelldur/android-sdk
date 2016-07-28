package com.sensorberg.sdk.internal.interfaces;

public interface PlatformIdentifier {

    String getUserAgentString();

    String getDeviceInstallationIdentifier();

    String getAdvertiserIdentifier();

    void setAdvertisingIdentifier(String advertisingIdentifier);

}
