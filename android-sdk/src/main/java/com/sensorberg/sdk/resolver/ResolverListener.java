package com.sensorberg.sdk.resolver;

import com.sensorberg.sdk.scanner.ScanEvent;

import java.util.List;

public interface ResolverListener {

    ResolverListener NONE = new ResolverListener() {
        @Override
        public void onResolutionFailed(Throwable cause, ScanEvent scanEvent) {

        }

        @Override
        public void onResolutionsFinished(List<BeaconEvent> events) {

        }
    };

    void onResolutionFailed(Throwable cause, ScanEvent scanEvent);

    void onResolutionsFinished(List<BeaconEvent> events);
}
