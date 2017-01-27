package com.sensorberg.sdk.presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import com.sensorberg.sdk.Logger;

import java.util.ArrayList;
import java.util.List;

public class ManifestParser {

    public static final String actionString = "com.sensorberg.android.PRESENT_ACTION";

    @SuppressWarnings("EmptyCatchBlock")
    public static List<BroadcastReceiver> findBroadcastReceiver(Context context) {
        List<BroadcastReceiver> result = new ArrayList<>();

        Intent actionIntent = new Intent();
        actionIntent.setPackage(context.getPackageName());
        actionIntent.setAction(actionString);

        List<ResolveInfo> infos = context.getPackageManager().queryBroadcastReceivers(actionIntent, 0);
        for (ResolveInfo resolveInfo : infos) {

            try {
                if (!resolveInfo.activityInfo.processName.endsWith(".sensorberg")) {
                    continue;
                }
                if (!resolveInfo.activityInfo.packageName.equals(context.getPackageName())) {
                    continue;
                }
                BroadcastReceiver broadcastReceiver = (BroadcastReceiver) Class.forName(resolveInfo.activityInfo.name).newInstance();
                result.add(broadcastReceiver);
            } catch (Exception e) {
                Logger.log.logError("could not find any broadcastreceiver", e);
            }
        }
        return result;
    }
}
