package com.sensorberg.sdk.scanner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.RunLoop;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportHistoryCallback;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarFields;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolverListener;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.utils.ListUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private final SharedPreferences sharedPreferences;

    private final Gson gson;

    private List<SugarScan> beaconScans = new ArrayList<>();

    private List<SugarAction> beaconActions = new ArrayList<>();

    private final Integer beaconScansLock = 5;

    private final Integer beaconActionsLock = 6;

    public BeaconActionHistoryPublisher(Context ctx, Transport transport, SettingsManager settingsManager, Clock clock,
            HandlerManager handlerManager, SharedPreferences sharedPrefs, Gson gson) {
        context = ctx;
        this.settingsManager = settingsManager;
        this.transport = transport;
        this.clock = clock;
        runloop = handlerManager.getBeaconPublisherRunLoop(this);
        sharedPreferences = sharedPrefs;
        this.gson = gson;

        loadAllData();
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
                saveData(scan);
                break;
            case MSG_MARK_SCANS_AS_SENT:
                //noinspection unchecked -> see useage of MSG_MARK_SCANS_AS_SENT
                List<SugarScan> scans = (List<SugarScan>) queueEvent.obj;
                markBeaconScansAsSent(scans, now, settingsManager.getCacheTtl());
                break;
            case MSG_MARK_ACTIONS_AS_SENT:
                List<SugarAction> actions = (List<SugarAction>) queueEvent.obj;
                markBeaconActionsAsSent(actions, now, settingsManager.getCacheTtl());
                break;
            case MSG_PUBLISH_HISTORY:
                publishHistorySynchronously();
                break;
            case MSG_ACTION:
                SugarAction sugarAction = SugarAction.from((BeaconEvent) queueEvent.obj, clock);
                saveData(sugarAction);
                break;
            case MSG_DELETE_ALL_DATA:
                deleteAllData();
                break;
        }
    }

    private void publishHistorySynchronously() {
        List<SugarScan> scans = new ArrayList<>();
        List<SugarAction> actions = new ArrayList<>();

        try {
            scans = notSentBeaconScans();
            actions = notSentBeaconActions();
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

    //local persistence

    synchronized private void saveData(SugarScan beaconScan) {
        if (!beaconScans.contains(beaconScan)) {
            beaconScans.add(beaconScan);
        }
    }

    synchronized private List<SugarScan> notSentBeaconScans() {
        return ListUtils.filter(beaconScans, new ListUtils.Filter<SugarScan>() {
            @Override
            public boolean matches(SugarScan beaconEvent) {
                return beaconEvent.getSentToServerTimestamp2() == SugarFields.Scan.NO_DATE;
            }
        });
    }

    synchronized private void markBeaconScansAsSent(List<SugarScan> scans, long timeNow, long cacheTtl) {
        if (scans.size() > 0) {
            synchronized (beaconScansLock) {
                for (int i = scans.size() - 1; i >= 0; i--) {
                    if (beaconScans.contains(scans.get(i))) {
                        beaconScans.remove(scans.get(i));
                    }
                    scans.get(i).setSentToServerTimestamp2(timeNow);
                    beaconScans.add(scans.get(i));
                }
            }
        }
        removeBeaconScansOlderThan(timeNow, cacheTtl);
    }

    synchronized private void removeBeaconScansOlderThan(final long timeNow, final long cacheTtl) {
        List<SugarScan> actionsToDelete = ListUtils.filter(beaconScans, new ListUtils.Filter<SugarScan>() {
            @Override
            public boolean matches(SugarScan beaconEvent) {
                return beaconEvent.getCreatedAt() < (timeNow - cacheTtl)
                        && beaconEvent.getSentToServerTimestamp2() != SugarFields.Scan.NO_DATE;
            }
        });

        synchronized (beaconScansLock) {
            if (actionsToDelete.size() > 0) {
                for (int i = actionsToDelete.size() - 1; i >= 0; i--) {
                    beaconScans.remove(actionsToDelete.get(i));
                }
            }
        }
    }

    synchronized private void saveData(SugarAction beaconAction) {
        if (!beaconActions.contains(beaconAction)) {
            beaconActions.add(beaconAction);
        }
    }

    synchronized private List<SugarAction> notSentBeaconActions() {
        return ListUtils.filter(beaconActions, new ListUtils.Filter<SugarAction>() {
            @Override
            public boolean matches(SugarAction beaconAction) {
                return beaconAction.getSentToServerTimestamp2() == SugarFields.Scan.NO_DATE;
            }
        });
    }

    synchronized private void markBeaconActionsAsSent(List<SugarAction> scans, long timeNow, long cacheTtl) {
        if (scans.size() > 0) {
            synchronized (beaconActionsLock) {
                for (int i = scans.size() - 1; i >= 0; i--) {
                    if (beaconActions.contains(scans.get(i))) {
                        beaconActions.remove(scans.get(i));
                    }
                    scans.get(i).setSentToServerTimestamp2(timeNow);
                    beaconActions.add(scans.get(i));
                }
            }
        }
        removeBeaconActionsOlderThan(timeNow, cacheTtl);
    }

    synchronized private void removeBeaconActionsOlderThan(final long timeNow, final long cacheTtl) {
        List<SugarAction> actionsToDelete = ListUtils.filter(beaconActions, new ListUtils.Filter<SugarAction>() {
            @Override
            public boolean matches(SugarAction beaconEvent) {
                return beaconEvent.getCreatedAt() < (timeNow - cacheTtl)
                        && beaconEvent.getSentToServerTimestamp2() != SugarFields.Scan.NO_DATE;
            }
        });

        synchronized (beaconActionsLock) {
            if (actionsToDelete.size() > 0) {
                for (int i = actionsToDelete.size() - 1; i >= 0; i--) {
                    beaconActions.remove(actionsToDelete.get(i));
                }
            }
        }
    }

    /**
     * List not sent scans.
     *
     * @return - A list of notSentBeaconScans.
     */

    synchronized public boolean getCountForSuppressionTime(final long lastAllowedPresentationTime, final UUID actionUUID) {
        List<SugarAction> actionsToKeep = ListUtils.filter(beaconActions, new ListUtils.Filter<SugarAction>() {
            @Override
            public boolean matches(SugarAction beaconEvent) {
                return beaconEvent.getTimeOfPresentation() >= lastAllowedPresentationTime
                        && beaconEvent.getActionId().equalsIgnoreCase(actionUUID.toString());
            }
        });

        keepForever(actionsToKeep);
        return actionsToKeep.size() > 0;
    }

    /**
     * Keep forever ie. save!
     *
     * @param sugarActionSelect - The select statement you would like to save.
     */
    private void keepForever(List<SugarAction> sugarActionSelect) {
        if (sugarActionSelect.size() > 0) {
            synchronized (beaconActionsLock) {
                for (int i = 0; i < sugarActionSelect.size(); i++) {
                    if (beaconActions.contains(sugarActionSelect.get(i))) {
                        beaconActions.remove(sugarActionSelect.get(i));
                    }
                    sugarActionSelect.get(i).setKeepForever(true);
                    beaconActions.add(sugarActionSelect.get(i));
                }
            }
        }
    }

    /**
     * Get the count for only once suppression.
     *
     * @param actionUUID - The beacon action UUID.
     * @return - Select class object.
     */
    public boolean getCountForShowOnlyOnceSuppression(final UUID actionUUID) {
        List<SugarAction> actionsToKeep = ListUtils.filter(beaconActions, new ListUtils.Filter<SugarAction>() {
            @Override
            public boolean matches(SugarAction beaconEvent) {
                return beaconEvent.getActionId().equalsIgnoreCase(actionUUID.toString());
            }
        });

        keepForever(actionsToKeep);
        return actionsToKeep.size() > 0;
    }

    synchronized private void loadAllData() {
        String actionJson = sharedPreferences.getString(SugarAction.SHARED_PREFS_TAG, "");
        if (!actionJson.isEmpty()) {
            Type listType = new TypeToken<List<SugarAction>>(){}.getType();
            beaconActions = gson.fromJson(actionJson, listType);
        }

        String scanJson = sharedPreferences.getString(SugarScan.SHARED_PREFS_TAG, "");
        if (!scanJson.isEmpty()) {
            Type listType = new TypeToken<List<SugarScan>>(){}.getType();
            beaconScans = gson.fromJson(actionJson, listType);
        }
    }

    //TODO should be called from Service, when closing
    synchronized public void saveAllData() {
        if (beaconActions.size() > 0) {
            deleteSavedBeaconActions();
            String actionsJson = gson.toJson(beaconActions);
            sharedPreferences.edit().putString(SugarAction.SHARED_PREFS_TAG, actionsJson).apply();
            beaconActions = new ArrayList<>();
        }

        if (beaconScans.size() > 0) {
            deleteSavedBeaconScans();
            String actionsJson = gson.toJson(beaconScans);
            sharedPreferences.edit().putString(SugarScan.SHARED_PREFS_TAG, actionsJson).apply();
            beaconScans = new ArrayList<>();
        }
    }

    private void deleteSavedBeaconScans() {
        if (sharedPreferences.contains(SugarScan.SHARED_PREFS_TAG)) {
            sharedPreferences.edit().remove(SugarScan.SHARED_PREFS_TAG).apply();
        }
    }

    private void deleteSavedBeaconActions() {
        if (sharedPreferences.contains(SugarAction.SHARED_PREFS_TAG)) {
            sharedPreferences.edit().remove(SugarAction.SHARED_PREFS_TAG).apply();
        }
    }

    synchronized private void deleteAllData() {
        beaconActions = new ArrayList<>();
        beaconScans = new ArrayList<>();

        deleteSavedBeaconScans();
        deleteSavedBeaconActions();
    }
}
