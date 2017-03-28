package com.sensorberg.sdk.internal.interfaces;

import android.os.Message;

import java.util.TimerTask;

public interface RunLoop {

    interface MessageHandlerCallback {
        MessageHandlerCallback NONE = new MessageHandlerCallback() {
            @Override
            public void handleMessage(Message queueEvent) {

            }
        };

        void handleMessage(Message queueEvent);
    }

    void add(Message event);

    void clearScheduledExecutions();

    void scheduleExecution(Runnable runnable, long wait_time);

    void scheduleAtFixedRate(TimerTask timerTask, long when, long interval);

    void cancelFixedRateExecution();

    Message obtainMessage(int what);

    Message obtainMessage(int what, Object obj);

    void sendMessage(int what);

    void sendMessage(int what, Object obj);

}
