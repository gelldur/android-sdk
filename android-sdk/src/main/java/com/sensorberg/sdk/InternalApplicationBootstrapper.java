package com.sensorberg.sdk;

import com.sensorberg.SensorbergSdk;
import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.internal.PermissionChecker;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.MessageDelayWindowLengthListener;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.presenter.LocalBroadcastManager;
import com.sensorberg.sdk.presenter.ManifestParser;
import com.sensorberg.sdk.receivers.GenericBroadcastReceiver;
import com.sensorberg.sdk.receivers.NetworkInfoBroadcastReceiver;
import com.sensorberg.sdk.receivers.ScannerBroadcastReceiver;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.Resolver;
import com.sensorberg.sdk.resolver.ResolverConfiguration;
import com.sensorberg.sdk.resolver.ResolverListener;
import com.sensorberg.sdk.scanner.BeaconActionHistoryPublisher;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.Scanner;
import com.sensorberg.sdk.scanner.ScannerListener;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.sdk.settings.SettingsUpdateCallback;
import com.sensorberg.utils.ListUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

public class InternalApplicationBootstrapper extends MinimalBootstrapper implements ScannerListener, SyncStatusObserver, Transport.ProximityUUIDUpdateHandler {

    private static final boolean SURVIVE_REBOOT = true;

    protected final Transport transport;

    protected final Resolver resolver;

    protected Scanner scanner;

    @Inject
    @Named("realSettingsManager")
    protected SettingsManager settingsManager;

    @Inject
    @Named("realBeaconActionHistoryPublisher")
    protected BeaconActionHistoryPublisher beaconActionHistoryPublisher;

    protected final Object proximityUUIDsMonitor = new Object();

    protected SensorbergService.MessengerList presentationDelegate;

    protected final Set<String> proximityUUIDs = new HashSet<>();

    @Inject
    protected Context context;

    protected Clock clock;

    @Inject
    protected FileManager fileManager;

    @Inject
    protected PermissionChecker permissionChecker;

    protected BluetoothPlatform bluetoothPlatform;

    public InternalApplicationBootstrapper(Transport transport, ServiceScheduler scheduler, HandlerManager handlerManager,
                                           Clock clk, BluetoothPlatform btPlatform, ResolverConfiguration resolverConfiguration) {
        super(scheduler);
        SensorbergSdk.getComponent().inject(this);

        this.transport = transport;
        transport.setApiToken(resolverConfiguration.apiToken);
        settingsManager.setSettingsUpdateCallback(settingsUpdateCallbackListener);
        settingsManager.setMessageDelayWindowLengthListener((MessageDelayWindowLengthListener) scheduler);
        clock = clk;
        bluetoothPlatform = btPlatform;

        beaconActionHistoryPublisher.setResolverListener(resolverListener);

        scanner = new Scanner(settingsManager, settingsManager.isShouldRestoreBeaconStates(), clock, fileManager, scheduler, handlerManager,
                btPlatform);
        resolver = new Resolver(resolverConfiguration, handlerManager, transport);
        resolver.setListener(resolverListener);

        scanner.addScannerListener(this);


        serviceScheduler.restorePendingIntents();

        ScannerBroadcastReceiver.setManifestReceiverEnabled(true, context);
        GenericBroadcastReceiver.setManifestReceiverEnabled(true, context);

        setUpAlarmsForSettings();
        setUpAlarmForBeaconActionHistoryPublisher();
        updateAlarmsForActionLayoutFetch();

        //cache the current network state
        NetworkInfoBroadcastReceiver.triggerListenerWithCurrentState(context);
        ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS, this);
    }

    private void setUpAlarmForBeaconActionHistoryPublisher() {
        serviceScheduler
                .scheduleRepeating(SensorbergServiceMessage.MSG_UPLOAD_HISTORY, settingsManager.getHistoryUploadInterval(), TimeUnit.MILLISECONDS);
    }

    private void setUpAlarmsForSettings() {
        serviceScheduler
                .scheduleRepeating(SensorbergServiceMessage.MSG_SETTINGS_UPDATE, settingsManager.getSettingsUpdateInterval(), TimeUnit.MILLISECONDS);
    }

    private void updateAlarmsForActionLayoutFetch() {
        if (isSyncEnabled()) {
            serviceScheduler
                    .scheduleRepeating(SensorbergServiceMessage.MSG_BEACON_LAYOUT_UPDATE, settingsManager.getLayoutUpdateInterval(),
                            TimeUnit.MILLISECONDS);
        } else {
            serviceScheduler.cancelIntent(SensorbergServiceMessage.MSG_BEACON_LAYOUT_UPDATE);
        }
    }

    @Override
    public void onScanEventDetected(ScanEvent scanEvent) {
        beaconActionHistoryPublisher.onScanEventDetected(scanEvent);

        boolean contained;
        synchronized (proximityUUIDsMonitor) {
            contained = proximityUUIDs.isEmpty() || proximityUUIDs.contains(scanEvent.getBeaconId().getProximityUUIDWithoutDashes());
        }
        if (contained) {
            resolver.resolve(scanEvent);

        }
    }

    public void presentBeaconEvent(BeaconEvent beaconEvent) {
        if (beaconEvent != null && beaconEvent.action != null) {
            Action beaconEventAction = beaconEvent.action;

            if (beaconEvent.deliverAt != null) {
                serviceScheduler.postDeliverAtOrUpdate(beaconEvent.deliverAt, beaconEvent);
            } else if (beaconEventAction.getDelayTime() > 0) {
                serviceScheduler
                        .postToServiceDelayed(beaconEventAction.getDelayTime(), SensorbergServiceMessage.GENERIC_TYPE_BEACON_ACTION, beaconEvent,
                                SURVIVE_REBOOT);

                Logger.log.beaconResolveState(beaconEvent, "delaying the display of this BeaconEvent");
            } else {
                presentEventDirectly(beaconEvent);
            }
        }
    }

    private void presentEventDirectly(BeaconEvent beaconEvent) {
        if (beaconEvent.action != null) {
            beaconEvent.setPresentationTime(clock.now());
            beaconActionHistoryPublisher.onActionPresented(beaconEvent);
            if (presentationDelegate == null) {
                Intent broadcastIntent = new Intent(ManifestParser.actionString);
                broadcastIntent.putExtra(Action.INTENT_KEY, beaconEvent.action);
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
            } else {
                Logger.log.beaconResolveState(beaconEvent, "delegating the display of the beacon event to the application");
                presentationDelegate.send(beaconEvent);
            }
        }
    }

    public void presentEventDirectly(BeaconEvent beaconEvent, int index) {
        serviceScheduler.removeStoredPendingIntent(index);
        if (beaconEvent != null) {
            presentEventDirectly(beaconEvent);
        }
    }

    public void sentPresentationDelegationTo(SensorbergService.MessengerList messengerList) {
        presentationDelegate = messengerList;
    }

    public void startScanning() {
        if (bluetoothPlatform.isBluetoothLowEnergySupported()
                && bluetoothPlatform.isBluetoothLowEnergyDeviceTurnedOn()) {
            if (!permissionChecker.hasScanPermissionCheckAndroid6()) {
                Logger.log.logError("User needs to be shown runtime dialogue asking for coarse location services");
            } else {
                scanner.start();
            }
        }
    }

    public void stopScanning() {
        scanner.stop();
    }

    public void saveAllDataBeforeDestroy() {
        beaconActionHistoryPublisher.saveAllData();
    }

    public void hostApplicationInForeground() {
        scanner.hostApplicationInForeground();
        updateSettings();
        //we do not care if sync is disabled, the app is in the foreground so we cache!
        transport.updateBeaconLayout();
        beaconActionHistoryPublisher.publishHistory();
    }

    public void hostApplicationInBackground() {
        scanner.hostApplicationInBackground();
        beaconActionHistoryPublisher.publishHistory();
    }

    public void setApiToken(String apiToken) {
        transport.setApiToken(apiToken);
        beaconActionHistoryPublisher.publishHistory();
        if (resolver.configuration.setApiToken(apiToken)) {
            unscheduleAllPendingActions();
            beaconActionHistoryPublisher.deleteAllObjects();
        }
    }

    public void updateSettings() {
        settingsManager.updateSettingsFromNetwork();
    }

    public void uploadHistory() {
        if (NetworkInfoBroadcastReceiver.latestNetworkInfo != null) {
            beaconActionHistoryPublisher.publishHistory();
        } else {
            Logger.log.logError("Did not try to upload the history because it seems we´e offline.");
        }
    }

    @SuppressWarnings("SimplifiableIfStatement")
    boolean isSyncEnabled() {
        if (permissionChecker.hasReadSyncSettingsPermissions()) {
            return ContentResolver.getMasterSyncAutomatically();
        } else {
            return true;
        }
    }

    public void updateBeaconLayout() {
        if (isSyncEnabled()) {
            transport.updateBeaconLayout();
        }
    }

    @Override
    public void onStatusChanged(int which) {
        updateAlarmsForActionLayoutFetch();
    }

    @Override
    public void proximityUUIDListUpdated(List<String> proximityUUIDs) {
        synchronized (proximityUUIDsMonitor) {
            this.proximityUUIDs.clear();
            for (String proximityUUID : proximityUUIDs) {
                this.proximityUUIDs.add(proximityUUID.toLowerCase());
            }
        }
    }

    public ListUtils.Filter<BeaconEvent> beaconEventFilter = new ListUtils.Filter<BeaconEvent>() {
        @Override
        public boolean matches(BeaconEvent beaconEvent) {
            if (beaconEvent.suppressionTimeMillis > 0) {
                long lastAllowedPresentationTime = clock.now() - beaconEvent.suppressionTimeMillis;
                if (beaconActionHistoryPublisher.actionShouldBeSuppressed(lastAllowedPresentationTime, beaconEvent.action.getUuid())) {
                    return false;
                }
            }
            if (beaconEvent.sendOnlyOnce) {
                Log.i("this", "sendOnlyOnce");
                System.out.print("sendOnlyOnce");
                if (beaconActionHistoryPublisher.actionWasShownBefore(beaconEvent.action.getUuid())) {
                    return false;
                }
            }
            return true;
        }
    };

    private ResolverListener resolverListener = new ResolverListener() {
        @Override
        public void onResolutionFailed(Throwable cause, ScanEvent scanEvent) {
            Logger.log.logError("resolution failed:" + scanEvent.getBeaconId().toTraditionalString(), cause);
        }

        @Override
        public void onResolutionsFinished(List<BeaconEvent> beaconEvents) {
            List<BeaconEvent> events = ListUtils.filter(beaconEvents, beaconEventFilter);
            for (BeaconEvent event : events) {
                presentBeaconEvent(event);
            }
        }
    };

    private SettingsUpdateCallback settingsUpdateCallbackListener = new SettingsUpdateCallback() {
        @Override
        public void onSettingsUpdateIntervalChange(Long updateIntervalMillies) {
            serviceScheduler.cancelIntent(SensorbergServiceMessage.MSG_SETTINGS_UPDATE);
            serviceScheduler.scheduleRepeating(SensorbergServiceMessage.MSG_SETTINGS_UPDATE, updateIntervalMillies, TimeUnit.MILLISECONDS);
        }

        @Override
        public void onSettingsBeaconLayoutUpdateIntervalChange(long newLayoutUpdateInterval) {
            serviceScheduler.cancelIntent(SensorbergServiceMessage.MSG_BEACON_LAYOUT_UPDATE);
            serviceScheduler.scheduleRepeating(SensorbergServiceMessage.MSG_BEACON_LAYOUT_UPDATE, newLayoutUpdateInterval, TimeUnit.MILLISECONDS);
        }

        @Override
        public void onHistoryUploadIntervalChange(long newHistoryUploadInterval) {
            serviceScheduler.cancelIntent(SensorbergServiceMessage.MSG_UPLOAD_HISTORY);
            serviceScheduler.scheduleRepeating(SensorbergServiceMessage.MSG_UPLOAD_HISTORY, newHistoryUploadInterval, TimeUnit.MILLISECONDS);
        }
    };
}
