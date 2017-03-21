package com.sensorberg.sdk.testUtils;

import android.os.Message;
import android.util.Pair;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.RunLoop;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class NonThreadedRunLoopForTesting implements RunLoop {

    private final MessageHandlerCallback messageCallback;
    private Clock clock;
    private final List<Pair<Long, Runnable>> scheduledEvents;
    private TimerTask timerTask;

    public NonThreadedRunLoopForTesting(MessageHandlerCallback messageMessageHandlerCallback, Clock clock) {
        this.messageCallback = messageMessageHandlerCallback;
        this.scheduledEvents = new ArrayList<>();
        this.clock = clock;
    }

    @Override
    public void add(Message event) {
        this.messageCallback.handleMessage(event);
    }

    @Override
    public void clearScheduledExecutions() {
        scheduledEvents.clear();
    }

    @Override
    public void scheduleExecution(Runnable runnable, long wait_time) {
        if (wait_time <= 0){
            runnable.run();
        } else {
            synchronized (this) {
                scheduledEvents.add(new Pair<>(clock.now() + wait_time, runnable));
            }
        }
    }

    @Override
    public void scheduleAtFixedRate(TimerTask timerTask, long when, long interval) {
        this.timerTask = timerTask;
    }

    @Override
    public void cancelFixedRateExecution() {
        this.timerTask = null;
    }

    @Override
    public void sendMessage(int what) {
        add(obtainMessage(what));
    }

    @Override
    public void sendMessage(int what, Object obj) {
        add(obtainMessage(what, obj));
    }

    @Override
    public Message obtainMessage(int what) {
        return obtainMessage(what, null);
    }

    @Override
    public Message obtainMessage(int what, Object object) {
        Message message = Message.obtain();
        message.what = what;
        message.obj = object;
        return message;
    }


    public void unschedule(Runnable runnable) {
        synchronized (this) {
            for (int i = scheduledEvents.size() - 1; i >= 0; i--) {
                if (scheduledEvents.get(i).second == runnable) {
                    scheduledEvents.remove(i);
                }
            }
        }
    }

    public void loop() {
        if (this.timerTask != null) {
            timerTask.run();
        } else {
            Logger.log.logError("Timertask was null");
        }
        scheduledEvents();
    }

    public void timeChanged() {
        loop();
    }

    private void scheduledEvents() {
        synchronized (this) {
            for (int i = scheduledEvents.size() - 1; i >= 0; i--) {
                Pair<Long, Runnable> pair = scheduledEvents.get(i);
                if (pair.first <= clock.now()) {
                    pair.second.run();
                    scheduledEvents.remove(i);
                }
            }
        }
    }
}
