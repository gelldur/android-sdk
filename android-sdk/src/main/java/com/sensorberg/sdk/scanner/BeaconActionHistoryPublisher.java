package com.sensorberg.sdk.scanner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.RunLoop;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.interfaces.TransportHistoryCallback;
import com.sensorberg.sdk.model.persistence.BeaconAction;
import com.sensorberg.sdk.model.persistence.BeaconScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolverListener;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.utils.ListUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private Set<BeaconScan> beaconScans = Collections.synchronizedSet(new HashSet<BeaconScan>());

    private Set<BeaconAction> beaconActions = Collections.synchronizedSet(new HashSet<BeaconAction>());

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
                BeaconScan scan = BeaconScan.from((ScanEvent) queueEvent.obj, clock.now());
                saveData(scan);
                break;
            case MSG_MARK_SCANS_AS_SENT:
                //noinspection unchecked -> see useage of MSG_MARK_SCANS_AS_SENT
                List<BeaconScan> scans = (List<BeaconScan>) queueEvent.obj;
                markBeaconScansAsSent(scans, now, settingsManager.getCacheTtl());
                break;
            case MSG_MARK_ACTIONS_AS_SENT:
                List<BeaconAction> actions = (List<BeaconAction>) queueEvent.obj;
                markBeaconActionsAsSent(actions, now, settingsManager.getCacheTtl());
                break;
            case MSG_PUBLISH_HISTORY:
                publishHistorySynchronously();
                break;
            case MSG_ACTION:
                BeaconAction beaconAction = BeaconAction.from((BeaconEvent) queueEvent.obj, clock);
                saveData(beaconAction);
                break;
            case MSG_DELETE_ALL_DATA:
                deleteAllData();
                break;
        }
    }

    private void publishHistorySynchronously() {
        List<BeaconScan> scans = new ArrayList<>();
        List<BeaconAction> actions = new ArrayList<>();

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
            public void onSuccess(List<BeaconScan> scanObjectList, List<BeaconAction> actionList) {
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
    private void saveData(BeaconScan beaconScan) {
        beaconScans.add(beaconScan);
        Logger.log.verbose("saving scan = " + beaconScan.getPid() + ", total saved = " + beaconScans.size());
    }

    public List<BeaconScan> notSentBeaconScans() {
        return ListUtils.filter(beaconScans, new ListUtils.Filter<BeaconScan>() {
            @Override
            public boolean matches(BeaconScan beaconEvent) {
                return beaconEvent.getSentToServerTimestamp2() == BeaconScan.NO_DATE;
            }
        });
    }

    private void markBeaconScansAsSent(List<BeaconScan> scans, long timeNow, long cacheTtl) {
        if (scans.size() > 0) {
            synchronized (beaconScansLock) {
                for (int i = scans.size() - 1; i >= 0; i--) {
                    if (beaconScans.contains(scans.get(i))) {
                        beaconScans.remove(scans.get(i));
                    }
                    scans.get(i).setSentToServerTimestamp2(timeNow);
                    saveData(scans.get(i));
                }
            }
        }
        removeBeaconScansOlderThan(timeNow, cacheTtl);
    }

    private void removeBeaconScansOlderThan(final long timeNow, final long cacheTtl) {
        List<BeaconScan> scansToDelete = ListUtils.filter(beaconScans, new ListUtils.Filter<BeaconScan>() {
            @Override
            public boolean matches(BeaconScan beaconEvent) {
                return beaconEvent.getCreatedAt() < (timeNow - cacheTtl)
                        && beaconEvent.getSentToServerTimestamp2() != BeaconScan.NO_DATE;
            }
        });

        synchronized (beaconScansLock) {
            if (scansToDelete.size() > 0) {
                for (int i = scansToDelete.size() - 1; i >= 0; i--) {
                    beaconScans.remove(scansToDelete.get(i));
                }
            }
        }
    }

    private void saveData(BeaconAction beaconAction) {
        beaconActions.add(beaconAction);
        Logger.log.verbose("saving action = " + beaconAction.getActionId() + ", total saved = " + beaconActions.size());
    }

    public List<BeaconAction> notSentBeaconActions() {
        return ListUtils.filter(beaconActions, new ListUtils.Filter<BeaconAction>() {
            @Override
            public boolean matches(BeaconAction beaconAction) {
                return beaconAction.getSentToServerTimestamp2() == BeaconAction.NO_DATE;
            }
        });
    }

    private void markBeaconActionsAsSent(List<BeaconAction> scans, long timeNow, long cacheTtl) {
        if (scans.size() > 0) {
            synchronized (beaconActionsLock) {
                for (int i = scans.size() - 1; i >= 0; i--) {
                    if (beaconActions.contains(scans.get(i))) {
                        beaconActions.remove(scans.get(i));
                    }
                    scans.get(i).setSentToServerTimestamp2(timeNow);
                    saveData(scans.get(i));
                }
            }
        }
        removeBeaconActionsOlderThan(timeNow, cacheTtl);
    }

    synchronized private void removeBeaconActionsOlderThan(final long timeNow, final long cacheTtl) {
        List<BeaconAction> actionsToDelete = ListUtils.filter(beaconActions, new ListUtils.Filter<BeaconAction>() {
            @Override
            public boolean matches(BeaconAction beaconEvent) {
                return beaconEvent.getCreatedAt() < (timeNow - cacheTtl)
                        && beaconEvent.getSentToServerTimestamp2() != BeaconAction.NO_DATE;
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
    public boolean getCountForSuppressionTime(final long lastAllowedPresentationTime, final UUID actionUUID) {
        List<BeaconAction> actionsToKeep = ListUtils.filter(beaconActions, new ListUtils.Filter<BeaconAction>() {
            @Override
            public boolean matches(BeaconAction beaconEvent) {
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
     * @param beaconActionSelect - The select statement you would like to save.
     */
    private void keepForever(List<BeaconAction> beaconActionSelect) {
        if (beaconActionSelect.size() > 0) {
            synchronized (beaconActionsLock) {
                for (int i = 0; i < beaconActionSelect.size(); i++) {
                    if (beaconActions.contains(beaconActionSelect.get(i))) {
                        beaconActions.remove(beaconActionSelect.get(i));
                    }
                    beaconActionSelect.get(i).setKeepForever(true);
                    saveData(beaconActionSelect.get(i));
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
        List<BeaconAction> actionsToKeep = ListUtils.filter(beaconActions, new ListUtils.Filter<BeaconAction>() {
            @Override
            public boolean matches(BeaconAction beaconEvent) {
                return beaconEvent.getActionId().equalsIgnoreCase(actionUUID.toString());
            }
        });

        keepForever(actionsToKeep);
        return actionsToKeep.size() > 0;
    }

    private void loadAllData() {
        String actionJson = sharedPreferences.getString(BeaconAction.SHARED_PREFS_TAG, "");
        if (!actionJson.isEmpty()) {
            Type listType = new TypeToken<Set<BeaconAction>>() {
            }.getType();

            synchronized (beaconActionsLock) {
                beaconActions = Collections.synchronizedSet((Set<BeaconAction>) gson.fromJson(actionJson, listType));
            }
        }

        String scanJson = sharedPreferences.getString(BeaconScan.SHARED_PREFS_TAG, "");
        if (!scanJson.isEmpty()) {
            Type listType = new TypeToken<Set<BeaconScan>>() {
            }.getType();

            synchronized (beaconScansLock) {
                beaconScans = Collections.synchronizedSet((Set<BeaconScan>) gson.fromJson(scanJson, listType));
            }
        }
    }

    public void saveAllData() {
        if (beaconActions.size() > 0) {
            deleteSavedBeaconActions();
            String actionsJson = gson.toJson(beaconActions);
            sharedPreferences.edit().putString(BeaconAction.SHARED_PREFS_TAG, actionsJson).apply();
            beaconActions = Collections.synchronizedSet(new HashSet<BeaconAction>());
        }

        if (beaconScans.size() > 0) {
            deleteSavedBeaconScans();
            String scansJson = gson.toJson(beaconScans);
            sharedPreferences.edit().putString(BeaconScan.SHARED_PREFS_TAG, scansJson).apply();
            beaconScans = Collections.synchronizedSet(new HashSet<BeaconScan>());
        }
    }

    private void deleteSavedBeaconScans() {
        if (sharedPreferences.contains(BeaconScan.SHARED_PREFS_TAG)) {
            sharedPreferences.edit().remove(BeaconScan.SHARED_PREFS_TAG).apply();
        }
    }

    private void deleteSavedBeaconActions() {
        if (sharedPreferences.contains(BeaconAction.SHARED_PREFS_TAG)) {
            sharedPreferences.edit().remove(BeaconAction.SHARED_PREFS_TAG).apply();
        }
    }

    public void deleteAllData() {
        beaconActions = Collections.synchronizedSet(new HashSet<BeaconAction>());
        beaconScans = Collections.synchronizedSet(new HashSet<BeaconScan>());

        deleteSavedBeaconScans();
        deleteSavedBeaconActions();
    }
}
