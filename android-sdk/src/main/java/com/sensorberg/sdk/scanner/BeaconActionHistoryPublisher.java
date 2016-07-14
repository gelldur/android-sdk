package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.RunLoop;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportHistoryCallback;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolverListener;
import com.sensorberg.sdk.settings.SettingsManager;

import android.content.Context;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

public class BeaconActionHistoryPublisher implements ScannerListener, RunLoop.MessageHandlerCallback {

    private static final int MSG_SCAN_EVENT = 2;

    private static final int MSG_MARK_SCANS_AS_SENT = 3;

    private static final int MSG_PUBLISH_HISTORY = 1;

    private static final int MSG_ACTION = 4;

    private static final int MSG_MARK_ACTIONS_AS_SENT = 5;

    private static final int MSG_DELETE_ALL_DATA = 6;

    private Context context;

    private Clock clock;

    private final RunLoop runloop;

    private final Transport transport;

    @Setter
    private ResolverListener resolverListener = ResolverListener.NONE;

    private final SettingsManager settingsManager;

    public BeaconActionHistoryPublisher(Context ctx, Transport transport, SettingsManager settingsManager, Clock clock,
            HandlerManager handlerManager) {
        context = ctx;
        this.settingsManager = settingsManager;
        this.transport = transport;
        this.clock = clock;
        runloop = handlerManager.getBeaconPublisherRunLoop(this);
    }

    @Override
    public void onScanEventDetected(ScanEvent scanEvent) {
        runloop.sendMessage(MSG_SCAN_EVENT, scanEvent);
    }

    @Override
    public void handleMessage(Message queueEvent) {
        long now = clock.now();
        switch (queueEvent.what) {
            case MSG_SCAN_EVENT:
                SugarScan scan = SugarScan.from((ScanEvent) queueEvent.obj, clock.now());
                scan.save();
                break;
            case MSG_MARK_SCANS_AS_SENT:
                //noinspection unchecked -> see useage of MSG_MARK_SCANS_AS_SENT
                List<SugarScan> scans = (List<SugarScan>) queueEvent.obj;
                SugarScan.maskAsSent(scans, now, settingsManager.getCacheTtl());
                break;
            case MSG_MARK_ACTIONS_AS_SENT:
                List<SugarAction> actions = (List<SugarAction>) queueEvent.obj;
                SugarAction.markAsSent(actions, now, settingsManager.getCacheTtl());
                break;
            case MSG_PUBLISH_HISTORY:
                publishHistorySynchronously();
                break;
            case MSG_ACTION:
                SugarAction sugarAction = SugarAction.from((BeaconEvent) queueEvent.obj, clock);
                sugarAction.save();
                break;
            case MSG_DELETE_ALL_DATA:
                SugarAction.deleteAll(SugarAction.class);
                SugarScan.deleteAll(SugarScan.class);
                break;
        }
    }

    private void publishHistorySynchronously() {
        List<SugarScan> scans = new ArrayList<>();
        List<SugarAction> actions = new ArrayList<>();

        try {
            scans = SugarScan.notSentScans();
            actions = SugarAction.notSentScans();
        } catch (Exception e) {
            Logger.log.logError("error fetching scans that were not sent from database", e);
        }

        if (scans.isEmpty() && actions.isEmpty()) {
            Logger.log.verbose("nothing to report");
            return;
        }

        transport.publishHistory(scans, actions, new TransportHistoryCallback() {

            @Override
            public void onSuccess(List<SugarScan> scanObjectList, List<SugarAction> actionList) {
                runloop.sendMessage(MSG_MARK_SCANS_AS_SENT, scanObjectList);
                runloop.sendMessage(MSG_MARK_ACTIONS_AS_SENT, actionList);
            }

            @Override
            public void onFailure(Exception throwable) {
                Logger.log.logError("not able to publish history", throwable);
            }

            @Override
            public void onInstantActions(List<BeaconEvent> instantActions) {
                resolverListener.onResolutionsFinished(instantActions);
            }
        });
    }

    public void publishHistory() {
        runloop.add(runloop.obtainMessage(MSG_PUBLISH_HISTORY));
    }

    public void onActionPresented(BeaconEvent beaconEvent) {
        runloop.sendMessage(MSG_ACTION, beaconEvent);
    }

    public void deleteAllObjects() {
        runloop.sendMessage(MSG_DELETE_ALL_DATA);
    }
}
