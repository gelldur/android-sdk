package com.sensorberg.sdk.internal.interfaces;

import android.content.BroadcastReceiver;

import java.util.List;

public interface Platform {

    List<BroadcastReceiver> getBroadcastReceiver();

    boolean registerBroadcastReceiver();

    void registerBroadcastReceiver(List<BroadcastReceiver> broadcastReceiver);

    interface ForegroundStateListener {

        ForegroundStateListener NONE = new ForegroundStateListener() {
            @Override
            public void hostApplicationInBackground() {

            }

            @Override
            public void hostApplicationInForeground() {

            }
        };

        void hostApplicationInBackground();

        void hostApplicationInForeground();
    }

}
