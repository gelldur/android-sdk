package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.interfaces.Platform;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;

import android.content.BroadcastReceiver;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class TestPlatform implements Platform {

    public static final String TAG = "TestPlatform";

    @Inject
    Context context;

    @Inject
    @Named("testBluetoothPlatform")
    BluetoothPlatform bluetoothPlatform;

    public TestPlatform() {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
    }

    @Override
    public List<BroadcastReceiver> getBroadcastReceiver() {
        List<BroadcastReceiver> result = new ArrayList<>();
        result.add(new TestBroadcastReceiver());
        return result;
    }

    @Override
    public boolean registerBroadcastReceiver() {
        return false;
    }

    @Override
    public void registerBroadcastReceiver(List<BroadcastReceiver> broadcastReceiver) {
        android.util.Log.e(TAG, "NOT IMPLEMENTED");
    }

    public void cleanUp() {

    }
}

