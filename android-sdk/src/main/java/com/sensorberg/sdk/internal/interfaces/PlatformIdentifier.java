package com.sensorberg.sdk.internal.interfaces;

public interface PlatformIdentifier {

    String getUserAgentString();

    String getDeviceInstallationIdentifier();

    String getAdvertiserIdentifier();

    void setAdvertisingIdentifier(String advertisingIdentifier);

    void addDeviceInstallationIdentifierChangeListener(DeviceInstallationIdentifierChangeListener listener);

    void addAdvertiserIdentifierChangeListener(AdvertiserIdentifierChangeListener listener);

    /**
     * Interface for device installation identifier.
     */
    interface DeviceInstallationIdentifierChangeListener {
        void deviceInstallationIdentifierChanged(String deviceInstallationIdentifier);
    }

    /**
     * Interface for advertising identifier.
     */
    interface AdvertiserIdentifierChangeListener {
        void advertiserIdentifierChanged(String advertiserIdentifier);
    }
}
