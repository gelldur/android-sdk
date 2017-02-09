package com.sensorberg.mvp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.sensorberg.sdk.action.Action;

public class SensorbergReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Action action = intent.getExtras().getParcelable(Action.INTENT_KEY);
        NotificationCompat.Builder b = new NotificationCompat.Builder(context);
        b.setContentTitle("SensorbergSDK");
        b.setContentText(action.toString());
        b.setSmallIcon(R.drawable.ic_beacon);
        NotificationManagerCompat.from(context).notify(1, b.build());
    }
}
