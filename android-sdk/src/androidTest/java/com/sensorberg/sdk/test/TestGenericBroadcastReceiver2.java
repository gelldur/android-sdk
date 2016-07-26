package com.sensorberg.sdk.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TestGenericBroadcastReceiver2 extends BroadcastReceiver{

    private static CountDownLatch latch;

    public static Intent getIntent() {
        return intent;
    }

    public static CountDownLatch getLatch() {
        return latch;
    }

    private static Intent intent;

    public static List<Intent> intentList = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        TestGenericBroadcastReceiver2.intent = intent;
        intentList.add(intent);
        latch.countDown();
    }

    public static void reset() {
        reset(1);
    }

    public static void reset(int count) {
        TestGenericBroadcastReceiver2.latch = new CountDownLatch(count);
        TestGenericBroadcastReceiver2.intent = null;
        intentList.clear();
    }
}
