package com.sensorberg.sdk;

import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;

class MinimalBootstrapper {

    protected final ServiceScheduler serviceScheduler;

    public MinimalBootstrapper(ServiceScheduler scheduler) {
        serviceScheduler = scheduler;
    }

    public void unscheduleAllPendingActions() {
        serviceScheduler.clearAllPendingIntents();
    }

    public void stopAllScheduledOperations() {
        serviceScheduler.cancelAllScheduledTimer();
    }

    public void stopScanning() {
        //we don´ care, we´e not scanning
    }
}
