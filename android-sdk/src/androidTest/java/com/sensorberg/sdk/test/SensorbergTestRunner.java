package com.sensorberg.sdk.test;

import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.internal.URLFactory;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTimeZone;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;

import java.util.TimeZone;

public class SensorbergTestRunner extends android.support.test.runner.AndroidJUnitRunner {

    @Override
    public Application newApplication(
            ClassLoader cl, String className, Context context)
            throws InstantiationException,
            IllegalAccessException,
            ClassNotFoundException {

        return super.newApplication(
                cl, SensorbergTestApplication.class.getName(), context);
    }

    @Override
    public void onCreate(Bundle arguments) {
        MultiDex.install(getTargetContext());

        super.onCreate(arguments);
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());

        if (com.sensorberg.sdk.BuildConfig.RESOLVER_URL != null) {
            URLFactory.setLayoutURL(com.sensorberg.sdk.BuildConfig.RESOLVER_URL);
        }
        JodaTimeAndroid.init(getContext());

        DateTimeZone e = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+01:00"));
        DateTimeZone.setDefault(e);
    }
}