package com.sensorberg.sdk.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.sensorberg.sdk.SensorbergServiceMessage;
import com.sensorberg.sdk.internal.PermissionChecker;

import javax.inject.Inject;

/**
 * @author skraynick
 * @date 16-06-13
 */
public class PermissionBroadcastReceiver extends BroadcastReceiver {

    @Inject
    PermissionChecker permissionChecker;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(SensorbergServiceMessage.EXTRA_LOCATION_PERMISSION)) {
            final int flagType = intent.getExtras().getInt("type");

        }
    }

    /**
     * Sends a flag for indicating whether to show a permissions dialogue or not.
     *
     * @param context
     * @param toShow
     */
    private void shouldDisplayPermission(Context context, boolean toShow) {

    }
}
