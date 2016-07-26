package com.sensorberg.sdk.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.presenter.LocalBroadcastManager;
import com.sensorberg.sdk.presenter.ManifestParser;
import com.sensorberg.sdk.test.TestGenericBroadcastReceiver2;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class TheAndroidPlatformShould {

    @After
    public void tearDown() throws Exception {
        TestGenericBroadcastReceiver2.reset();
    }

    @Test
    public void test_should_cache_the_permissions(){
        Context mockContext = mock(Context.class);

        when(mockContext.checkCallingOrSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_GRANTED);
        PackageManager mockPackageManager = mock(PackageManager.class);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);

        PermissionChecker tested = new PermissionChecker(mockContext);

        tested.hasVibratePermission();
        tested.hasVibratePermission();

        verify(mockContext, times(1)).checkCallingOrSelfPermission(anyString());
    }

    @Test
    public void test_should_not_registerBroadcastReceiver_twice(){
        TestGenericBroadcastReceiver2.reset(1);
        AndroidPlatform androidPlatform = new AndroidPlatform(InstrumentationRegistry.getContext());

        androidPlatform.registerBroadcastReceiver();
        androidPlatform.registerBroadcastReceiver();
        androidPlatform.registerBroadcastReceiver();

        Intent broadcastIntent = new Intent(ManifestParser.actionString);
        broadcastIntent.putExtra(Action.INTENT_KEY, "foo");
        LocalBroadcastManager.getInstance(InstrumentationRegistry.getContext()).sendBroadcastSync(broadcastIntent);

        Assertions.assertThat(TestGenericBroadcastReceiver2.intentList).hasSize(1);
    }

    @Test
    public void test_should_find_the_TestGenericBroadcastReceiver(){
        List<BroadcastReceiver> list = ManifestParser.findBroadcastReceiver(InstrumentationRegistry.getContext());

        Assertions.assertThat(list).hasSize(2);
    }
}
