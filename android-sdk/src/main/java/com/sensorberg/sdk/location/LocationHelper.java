package com.sensorberg.sdk.location;

import android.Manifest;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.sensorberg.sdk.internal.AndroidHandler;
import com.sensorberg.sdk.internal.PermissionChecker;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.RunLoop;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import hugo.weaving.DebugLog;
import lombok.Getter;

@Singleton
public class LocationHelper implements LocationListener, RunLoop.MessageHandlerCallback {

    private HandlerManager handlerManager;
    private LocationManager locationManager;
    private PermissionChecker permissionChecker;
    @Getter
    private Location location;
    private long minTime = TimeUnit.MINUTES.toMillis(30);
    private float minDistance = 50.0F;

    @Inject @DebugLog
    public LocationHelper(@Named("realHandlerManager") HandlerManager handlerManager, LocationManager locationManager, PermissionChecker permissionChecker) {
        Log.d("LocationHelper", "constructor");
        this.handlerManager = handlerManager;
        this.locationManager = locationManager;
        this.permissionChecker = permissionChecker;
        if (permissionChecker.checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            //We've got GPS.
            Log.d("LocationHelper", "ACCESS_FINE_LOCATION");
            AndroidHandler handler = (AndroidHandler) handlerManager.getLocationRunLoop(this);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
            //locationManager.requestLocationUpdates(minTime, minDistance, criteria, this, handler.getHandler().getLooper());
        } else if (permissionChecker.checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            //We have only coarse location permission
            Log.d("LocationHelper", "ACCURACY_COARSE");
            AndroidHandler handler = (AndroidHandler) handlerManager.getLocationRunLoop(this);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minTime, minDistance, this);
            //locationManager.requestLocationUpdates(minTime, minDistance, criteria, this, handler.getHandler().getLooper());
        } else {
            //No permission for location.
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Log.d("LocationHelper", location != null ? location.toString() : "null");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d("LocationHelper", s+" "+i+" "+bundle);
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d("LocationHelper", s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d("LocationHelper", s);
    }

    @Override
    public void handleMessage(Message msg) {
        Log.d("LocationHelper", msg.getData().toString());
    }
}
