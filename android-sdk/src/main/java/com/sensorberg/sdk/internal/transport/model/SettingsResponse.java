package com.sensorberg.sdk.internal.transport.model;

import com.google.gson.annotations.Expose;

import lombok.Getter;

public class SettingsResponse {

    @Expose
    @Getter
    private long revision;

    @Expose
    @Getter
    private com.sensorberg.sdk.settings.Settings settings;

    public SettingsResponse(int rev, com.sensorberg.sdk.settings.Settings set) {
        revision = rev;
        settings = set;
    }
}
