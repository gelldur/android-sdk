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

    void addDelayed(Message event, long wait_time);

    void clearScheduledExecutions();

    void clearMessage(int what);

    void scheduleExecution(Runnable runnable, long wait_time);

    void scheduleAtFixedRate(TimerTask timerTask, int when, long interval);

    void cancelFixedRateExecution();

    Message obtainMessage(int what);

    Message obtainMessage(int what, Object obj);

    void sendMessage(int what);

    void sendMessage(int what, Object obj);

    void sendMessageDelayed(int what, long wait_time);

}
