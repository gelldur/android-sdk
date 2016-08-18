package com.sensorberg;

import com.sensorberg.di.Component;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.SensorbergServiceIntents;
import com.sensorberg.sdk.SensorbergServiceMessage;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Platform;
import com.sensorberg.sdk.receivers.ScannerBroadcastReceiver;
import com.sensorberg.sdk.resolver.BeaconEvent;

import net.danlew.android.joda.JodaTimeAndroid;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

import java.net.URL;
import java.util.Set;
import java.util.HashSet;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code SensorbergSdk} This is the entry point to the Sensorberg SDK. You should use this class to manage the SDK.
 *
 * @since 1.0
 */

public class SensorbergSdk implements Platform.ForegroundStateListener {

    protected static Context context;

    @Getter
    protected boolean presentationDelegationEnabled;

    protected final Messenger messenger = new Messenger(new IncomingHandler());

    protected static final Set<SensorbergSdkEventListener> listeners = new HashSet<>();

    @Setter
    private static Component component;

    @Inject
    @Named("androidBluetoothPlatform")
    protected BluetoothPlatform bluetoothPlatform;

    static class IncomingHandler extends Handler {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SensorbergServiceMessage.MSG_PRESENT_ACTION:
                    Bundle bundle = msg.getData();
                    bundle.setClassLoader(BeaconEvent.class.getClassLoader());
                    BeaconEvent beaconEvent = bundle.getParcelable(SensorbergServiceMessage.MSG_PRESENT_ACTION_BEACONEVENT);
                    notifyEventListeners(beaconEvent);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Constructor to be used for starting the SDK.
     *
     * @param ctx {@code Context} Context used for starting the service.
     * @param apiKey {@code String} Your API key that you can get from your Sensorberg dashboard.
     */
    public SensorbergSdk(Context ctx, String apiKey) {
        init(ctx);
        getComponent().inject(this);
        activateService(apiKey);
    }

    public static void init(Context ctx){
        context = ctx;
        initLibraries(context);
    }

    public static Component getComponent() {
        buildComponentAndInject(context);
        return component;
    }

    private static void buildComponentAndInject(Context context) {
        if (component == null && context != null) {
            component = Component.Initializer.init((Application) context.getApplicationContext());
        }
    }

    synchronized private static void initLibraries(Context ctx) {
        if (ctx != null) {
            JodaTimeAndroid.init(ctx);
        }
    }

    /**
     * To receive Sensorberg SDK events, you should register your {@code SensorbergSdkEventListener} with this method. Depending on how you structure
     * your app, this can be done on an Application or on an Activity level.
     *
     * @param listener {@code SensorbergSdkEventListener} Your implementation of the listener that will receive Sensorberg SDK events that
     *                                                should be presented via UI.
     */
    public void registerEventListener(SensorbergSdkEventListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }

        if (!listeners.isEmpty() && !isPresentationDelegationEnabled()) {
            setPresentationDelegationEnabled(true);
        }
    }

    /**
     * If you don't want to receive Sensorberg SDK events any more, you should unregister your {@code SensorbergSdkEventListener} with this method.
     * Depending on how you structure your app, this can be done on an Application or on an Activity level.
     *
     * @param listener {@code SensorbergSdkEventListener} Reference to your implementation of the listener that was registered with
     *                                                  {@code registerEventListener}.
     */
    public void unregisterEventListener(SensorbergSdkEventListener listener) {
        listeners.remove(listener);

        if (listeners.isEmpty() && isPresentationDelegationEnabled()) {
            setPresentationDelegationEnabled(false);
        }
    }

    protected void setPresentationDelegationEnabled(boolean value) {
        presentationDelegationEnabled = value;
        if (value) {
            registerForPresentationDelegation();
        } else {
            unRegisterFromPresentationDelegation();
        }
    }

    protected static void notifyEventListeners(BeaconEvent beaconEvent) {
        for (SensorbergSdkEventListener listener : listeners) {
            listener.presentBeaconEvent(beaconEvent);
        }
    }

    protected void activateService(String apiKey) {
        if (bluetoothPlatform.isBluetoothLowEnergySupported()) {
            context.startService(SensorbergServiceIntents.getStartServiceIntent(context, apiKey));
        }
    }

    public void enableService(Context context, String apiKey) {
        ScannerBroadcastReceiver.setManifestReceiverEnabled(true, context);
        activateService(apiKey);
        hostApplicationInForeground();
    }

    public void disableService(Context context) {
        context.startService(SensorbergServiceIntents.getShutdownServiceIntent(context));
    }

    public void hostApplicationInBackground() {
        Logger.log.applicationStateChanged("hostApplicationInBackground");
        context.startService(SensorbergServiceIntents.getAppInBackgroundIntent(context));
        unRegisterFromPresentationDelegation();
    }

    public void hostApplicationInForeground() {
        context.startService(SensorbergServiceIntents.getAppInForegroundIntent(context));
        if (presentationDelegationEnabled) {
            registerForPresentationDelegation();
        }
    }

    protected void unRegisterFromPresentationDelegation() {
        context.startService(SensorbergServiceIntents.getIntentWithReplyToMessenger(context,
                SensorbergServiceMessage.MSG_UNREGISTER_PRESENTATION_DELEGATE, messenger));
    }

    protected void registerForPresentationDelegation() {
        context.startService(SensorbergServiceIntents.getIntentWithReplyToMessenger(context,
                SensorbergServiceMessage.MSG_REGISTER_PRESENTATION_DELEGATE, messenger));
    }

    public void changeAPIToken(String newApiToken) {
        context.startService(SensorbergServiceIntents.getApiTokenIntent(context, newApiToken));
    }

    public void setAdvertisingIdentifier(String advertisingIdentifier) {
        Intent service = SensorbergServiceIntents.getAdvertisingIdentifierIntent(context, advertisingIdentifier);
        context.startService(service);
    }

    public void setLogging(boolean enableLogging) {
        context.startService(SensorbergServiceIntents.getServiceLoggingIntent(context, enableLogging));
    }

    public void sendLocationFlagToReceiver(int flagType) {
        Intent intent = new Intent();
        intent.setAction(SensorbergServiceMessage.EXTRA_LOCATION_PERMISSION);
        intent.putExtra("type", flagType);
        context.sendBroadcast(intent);
    }
}
