package com.sensorberg.mvp;

import android.app.Application;

import com.sensorberg.BackgroundDetector;
import com.sensorberg.SensorbergSdk;

/**
 * Created by ronaldo on 2/9/17.
 */
public class App extends Application {

    // TODO: replace this with your API KEY
    private static final String SENSORBERG_KEY = "4520f52ea9808ab39d2a02cb21ab63054cd876d7673677cf0773b6b66296479a";

    static { // temporary work-around to use for portal.sensorberg.com
        com.sensorberg.sdk.internal.transport.RetrofitApiTransport.RESOLVER_BASE_URL = "https://portal.sensorberg-cdn.com";
    }

    private SensorbergSdk sensorbergSdk;
    private BackgroundDetector sensorbergDetector;

    public SensorbergSdk getSensorbergSdk() {
        return sensorbergSdk;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (SENSORBERG_KEY == "0") {
            throw new IllegalArgumentException("Please register at portal.sensorberg.com and replace your API key on the `SENSORBERG_KEY` variable");
        }

        sensorbergSdk = new SensorbergSdk(this, SENSORBERG_KEY);
        sensorbergDetector = new BackgroundDetector(sensorbergSdk);
        registerActivityLifecycleCallbacks(sensorbergDetector);
    }
}
