package com.sensorberg.sdk.internal;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class TheThreadedRunLoop {

    private CountDownLatch latch;
    private MyRunnable myRunnable;
    private AndroidHandler tested;
    private static boolean prepared;

    @Before
    public void setUp() throws Exception {
        latch = new CountDownLatch(1);
        myRunnable = new MyRunnable(latch);

        if(!prepared){
            prepared = true;
            Looper.prepare();
        }

        tested = new AndroidHandler(AndroidHandler.MessageHandlerCallback.NONE);
    }

    @Ignore
    public void should_run_a_scheduled_runnable() throws InterruptedException {

        //this cannot work, since the instanciated handler is running this thread, so we´e pausing this thread
        // while waiting for something to happen on it...

        tested.scheduleExecution(myRunnable, 100);

        long before = System.currentTimeMillis();
        boolean countedDown = latch.await(200, TimeUnit.MILLISECONDS);
        long after = System.currentTimeMillis();

        Assertions.assertThat(after - before).isGreaterThan(300);
        Assertions.assertThat(countedDown).isTrue();
    }

    @Test
    public void test_should_unschedule_a_runable() throws InterruptedException {
        tested.scheduleExecution(myRunnable, 100);
        tested.clearScheduledExecutions();
        boolean countedDown = latch.await(200, TimeUnit.MILLISECONDS);
        Assertions.assertThat(countedDown).isFalse();
    }

    private class MyRunnable implements Runnable {

        private CountDownLatch latch;

        public MyRunnable(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            latch.countDown();
        }
    }
}
