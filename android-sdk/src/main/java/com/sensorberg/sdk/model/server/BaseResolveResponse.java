package com.sensorberg.sdk.model.server;

import com.google.gson.annotations.Expose;

import java.util.Collections;
import java.util.List;

import lombok.ToString;

@ToString
public class BaseResolveResponse {

    @Expose
    private List<String> accountProximityUUIDs = Collections.emptyList();

    @SuppressWarnings("unchecked")
    public List<String> getAccountProximityUUIDs() {
        return accountProximityUUIDs == null ? Collections.EMPTY_LIST : accountProximityUUIDs;
    }

    protected BaseResolveResponse(List<String> accountProximityUUIDs) {
        this.accountProximityUUIDs = accountProximityUUIDs;
    }
}
