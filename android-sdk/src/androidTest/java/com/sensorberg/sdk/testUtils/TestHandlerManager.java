package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.internal.interfaces.RunLoop;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class TestHandlerManager implements HandlerManager {

    @Getter
    private final CustomClock customClock = new CustomClock();

    private List<NonThreadedRunLoopForTesting> runLoops = new ArrayList<>();

    @Override
    public RunLoop getResolverRunLoop(RunLoop.MessageHandlerCallback callback) {
        NonThreadedRunLoopForTesting loop = new NonThreadedRunLoopForTesting(callback, customClock);
        runLoops.add(loop);
        return loop;
    }

    @Override
    public RunLoop getBeaconPublisherRunLoop(RunLoop.MessageHandlerCallback callback) {
        NonThreadedRunLoopForTesting loop = new NonThreadedRunLoopForTesting(callback, customClock);
        runLoops.add(loop);
        return loop;
    }

    @Override
    public RunLoop getScannerRunLoop(RunLoop.MessageHandlerCallback callback) {
        NonThreadedRunLoopForTesting loop = new NonThreadedRunLoopForTesting(callback, customClock);
        runLoops.add(loop);
        return loop;
    }

    public void triggerRunLoop() {
        for (NonThreadedRunLoopForTesting runLoop : runLoops) {
            runLoop.loop();
        }
    }

    public class CustomClock implements Clock {

        private long nowInMillis = 0;

        @Override
        public long now() {
            return nowInMillis;
        }

        @Override
        public long elapsedRealtime() {
            return nowInMillis;
        }

        public void setNowInMillis(long nowInMillis) {
            this.nowInMillis = nowInMillis;
            triggerRunLoop();
        }

        public void increaseTimeInMillis(long value) {
            setNowInMillis(nowInMillis + value);
        }
    }
}
