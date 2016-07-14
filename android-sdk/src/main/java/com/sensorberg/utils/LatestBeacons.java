package com.sensorberg.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;

import com.sensorberg.sdk.SensorbergService;
import com.sensorberg.sdk.SensorbergServiceMessage;
import com.sensorberg.sdk.model.BeaconId;

/**
 * + * @author skraynick
 * + * @date 16-07-11
 * +
 */
public class LatestBeacons {

    static class LooperThread extends HandlerThread {

        final CountDownLatch latch;
        IncomingHandler handler;

        LooperThread(CountDownLatch latch) {
            super("LatestBeacons thread");
            this.latch = latch;
        }

        public synchronized void waitUntilReady() {
            handler = new IncomingHandler(getLooper(), this.latch);
        }
    }

    static class IncomingHandler extends Handler {
        private final CountDownLatch latch;
        public ArrayList<BeaconId> beaconIds;

        public IncomingHandler(Looper looper, CountDownLatch latch) {
            super(looper);
            this.latch = latch;
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case SensorbergServiceMessage.MSG_LIST_OF_BEACONS:
                    Bundle bundle = message.getData();
                    bundle.setClassLoader(BeaconId.class.getClassLoader());
                    beaconIds = bundle.getParcelableArrayList(SensorbergServiceMessage.MSG_LIST_OF_BEACONS_BEACON_IDS);
                    latch.countDown();
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    }

    public static Collection<BeaconId> getLatestBeacons(Context context, long duration, TimeUnit timeUnit) {
        return getLatestBeacons(context, duration, timeUnit, 5, TimeUnit.SECONDS);
    }

    public static Collection<BeaconId> getLatestBeacons(Context context, long duration, TimeUnit timeUnit,
                                                        long timeoutduration, TimeUnit timeoutTimeUnit) {

        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new IllegalArgumentException("Calling this from your main thread can lead to deadlock");
        }

        LooperThread thread = new LooperThread(new CountDownLatch(1));
        thread.start();
        thread.waitUntilReady();

        Messenger messenger = new Messenger(thread.handler);

        Intent intent = new Intent(context, SensorbergService.class);
        intent.putExtra(SensorbergServiceMessage.MSG_LIST_OF_BEACONS_MESSENGER, messenger);
        intent.putExtra(SensorbergServiceMessage.MSG_LIST_OF_BEACONS_MILLIS, timeUnit.toMillis(duration));
        intent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.MSG_LIST_OF_BEACONS);


        context.startService(intent);
        try {
            if (thread.latch.await(timeoutduration, timeoutTimeUnit)) {
                thread.quit();
                return thread.handler.beaconIds;
            }
            throw new TimeoutException("The inter process communication timed out. Timeout was set to " + timeoutTimeUnit.toMillis(duration) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }
}
