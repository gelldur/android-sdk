package com.sensorberg.sdk;

import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.Scanner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;

public class SensorbergServiceIntents {

    private SensorbergServiceIntents() {
        throw new IllegalAccessError("Utility class");
    }

    public static Intent getBasicServiceIntent(Context ctx) {
        return new Intent(ctx, SensorbergService.class);
    }

    public static Intent getStartServiceIntent(Context ctx, String apiKey) {
        Intent startIntent = getBasicServiceIntent(ctx);
        startIntent.putExtra(SensorbergServiceMessage.EXTRA_START_SERVICE, 1);
        if (apiKey != null) {
            startIntent.putExtra(SensorbergServiceMessage.EXTRA_API_KEY, apiKey);
        }
        return startIntent;
    }

    public static Intent getServiceIntentWithMessage(Context ctx, int message) {
        Intent intent = getBasicServiceIntent(ctx);
        intent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, message);
        return intent;
    }

    public static Intent getServiceIntentLoggingMessage(Context context, int message, boolean showMessage) {
        Intent intent = getBasicServiceIntent(context);
        intent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, message);
        intent.putExtra(SensorbergServiceMessage.EXTRA_SHOW_MESSAGE, showMessage);
        return intent;
    }

    public static Intent getShutdownServiceIntent(Context ctx) {
        return getServiceIntentWithMessage(ctx, SensorbergServiceMessage.MSG_SHUTDOWN);
    }

    public static Intent getBeaconLayoutUpdateIntent(Context ctx) {
        return getServiceIntentWithMessage(ctx, SensorbergServiceMessage.MSG_BEACON_LAYOUT_UPDATE);
    }

    public static Intent getUpdateSettingsIntent(Context ctx) {
        return getServiceIntentWithMessage(ctx, SensorbergServiceMessage.MSG_SETTINGS_UPDATE);
    }

    public static Intent getAppInForegroundIntent(Context ctx) {
        return getServiceIntentWithMessage(ctx, SensorbergServiceMessage.MSG_APPLICATION_IN_FOREGROUND);
    }

    public static Intent getAppInBackgroundIntent(Context ctx) {
        return getServiceIntentWithMessage(ctx, SensorbergServiceMessage.MSG_APPLICATION_IN_BACKGROUND);
    }

    public static Intent getUploadHistoryIntent(Context ctx) {
        return getServiceIntentWithMessage(ctx, SensorbergServiceMessage.MSG_UPLOAD_HISTORY);
    }

    public static Intent getPingIntent(Context ctx) {
        return getServiceIntentWithMessage(ctx, SensorbergServiceMessage.MSG_PING);
    }

    public static Intent getBeaconActionIntent(Context ctx, BeaconEvent beaconEvent, int index) {
        Intent serviceIntent = getBasicServiceIntent(ctx);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, SensorbergServiceMessage.GENERIC_TYPE_BEACON_ACTION);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_WHAT, beaconEvent);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_INDEX, index);

        return serviceIntent;
    }

    public static Intent getAdvertisingIdentifierIntent(Context ctx, String adId) {
        Intent serviceIntent = getServiceIntentWithMessage(ctx, SensorbergServiceMessage.MSG_SET_API_ADVERTISING_IDENTIFIER);
        serviceIntent.putExtra(SensorbergServiceMessage.MSG_SET_API_ADVERTISING_IDENTIFIER_ADVERTISING_IDENTIFIER, adId);

        return serviceIntent;
    }

    public static Intent getApiTokenIntent(Context ctx, String apiToken) {
        Intent serviceIntent = getServiceIntentWithMessage(ctx, SensorbergServiceMessage.MSG_SET_API_TOKEN);
        serviceIntent.putExtra(SensorbergServiceMessage.MSG_SET_API_TOKEN_TOKEN, apiToken);

        return serviceIntent;
    }

    public static Intent getBluetoothMessageIntent(Context ctx, boolean state) {
        Intent serviceIntent = getServiceIntentWithMessage(ctx, SensorbergServiceMessage.MSG_BLUETOOTH);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_BLUETOOTH_STATE, state);

        return serviceIntent;
    }

    public static Intent getSdkScannerMessageIntent(Context ctx, int scannerEvent) {
        Bundle bundle = new Bundle();
        bundle.putInt(Scanner.SCANNER_EVENT, scannerEvent);
        Intent serviceIntent = getServiceIntentWithMessage(ctx, SensorbergServiceMessage.MSG_SDK_SCANNER_MESSAGE);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_GENERIC_WHAT, bundle);

        return serviceIntent;
    }

    public static Intent getServiceLoggingIntent(Context ctx, boolean enableLogging, boolean showMessage) {
        int message = enableLogging ? SensorbergServiceMessage.MSG_TYPE_ENABLE_LOGGING : SensorbergServiceMessage.MSG_TYPE_DISABLE_LOGGING;
        return getServiceIntentLoggingMessage(ctx, message, showMessage);
    }

    public static Intent getIntentWithReplyToMessenger(Context ctx, int messageType, Messenger messenger) {
        Intent serviceIntent = getServiceIntentWithMessage(ctx, messageType);
        serviceIntent.putExtra(SensorbergServiceMessage.EXTRA_MESSENGER, messenger);
        return serviceIntent;
    }
}
