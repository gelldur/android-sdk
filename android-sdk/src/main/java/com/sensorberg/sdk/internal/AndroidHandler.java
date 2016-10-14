package com.sensorberg.sdk.internal;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.RunLoop;
import com.sensorberg.sdk.settings.TimeConstants;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class AndroidHandler implements RunLoop {
    private final LooperThread looper;
    private Timer timer;

    public AndroidHandler(MessageHandlerCallback messageHandlerCallback) {
        looper = new LooperThread(messageHandlerCallback);
        looper.start();
    }

    @Override
    public void add(Message event) {
        getHandler().sendMessage(event);
    }

    @Override
    public void addDelayed(Message event, long wait_time) {
        getHandler().sendMessageDelayed(event, wait_time);
    }

    @Override
    public void clearScheduledExecutions() {
        if (looper.handler != null) {
            looper.handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void clearMessage(int what) {
        getHandler().removeMessages(what);
    }

    @Override
    public void scheduleExecution(Runnable runnable, long wait_time) {
        if (wait_time > 0) {
            if (wait_time > TimeConstants.ONE_HOUR * 24) {
                wait_time = TimeConstants.ONE_HOUR * 24;
            }

            boolean result = getHandler().postDelayed(runnable, wait_time);
            if (!result) {
                Logger.log.logError("could not schedule the runnable in " + wait_time + " millis");
            }
        } else {
            runnable.run();
        }
    }

    private Handler getHandler() {
        while (looper.handler == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Logger.log.logError("looper was null, so we tried to sleep the thread...", e);
            }
        }
        return looper.handler;
    }

    @Override
    public void scheduleAtFixedRate(TimerTask timerTask, int when, long interval) {
        if (timer != null) {
            timer.cancel();
            timer = null;
            Logger.log.logError("There is already an execution scheduled");
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, when, interval);
    }

    @Override
    public void cancelFixedRateExecution() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public Message obtainMessage(int what) {
        return getHandler().obtainMessage(what);
    }

    @Override
    public Message obtainMessage(int what, Object obj) {
        return getHandler().obtainMessage(what, obj);
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
    public void sendMessageDelayed(int what, long wait_time) {
        addDelayed(obtainMessage(what), wait_time);
    }

    static class LooperThread extends Thread {
        public Handler handler;
        private MessageHandlerCallback messageHandlerCallback;

        LooperThread(MessageHandlerCallback messageHandlerCallback) {
            this.messageHandlerCallback = messageHandlerCallback;
        }

        public void run() {
            Looper.prepare();

            handler = new StaticHandler(messageHandlerCallback);
            messageHandlerCallback = null;

            Looper.loop();
        }

        private static class StaticHandler extends Handler {
            private final WeakReference<MessageHandlerCallback> messageHandlerCallback;

            public StaticHandler(MessageHandlerCallback messageHandlerCallback) {
                this.messageHandlerCallback = new WeakReference<>(messageHandlerCallback);
            }

                public void handleMessage(Message msg) {
                    MessageHandlerCallback messageHandlerCallback = this.messageHandlerCallback.get();
                    if (messageHandlerCallback != null) {
                        messageHandlerCallback.handleMessage(msg);
                    }
                }
        }
    }
}
