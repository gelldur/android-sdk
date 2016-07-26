package com.sensorberg.sdk;

import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.resolver.ResolverConfiguration;

import java.io.Serializable;

public class SensorbergServiceConfiguration implements Serializable {

    private static final long serialVersionUID = 3L;

    public ResolverConfiguration resolverConfiguration;

    public SensorbergServiceConfiguration(ResolverConfiguration resolverConfiguration) {
        this.resolverConfiguration = resolverConfiguration;
    }

    public boolean isComplete() {
        return resolverConfiguration != null &&
                resolverConfiguration.apiToken != null;
    }

    public static SensorbergServiceConfiguration loadFromDisk(FileManager fileManager) {
        return (SensorbergServiceConfiguration) fileManager.getContentsOfFileOrNull(
                fileManager.getFile(SensorbergServiceMessage.SERVICE_CONFIGURATION));
    }

    public static void removeConfigurationFromDisk(FileManager fileManager) {
        fileManager.removeFile(SensorbergServiceMessage.SERVICE_CONFIGURATION);
    }

    public void writeToDisk(FileManager fileManager) {
        fileManager.write(this, SensorbergServiceMessage.SERVICE_CONFIGURATION);
    }
}
