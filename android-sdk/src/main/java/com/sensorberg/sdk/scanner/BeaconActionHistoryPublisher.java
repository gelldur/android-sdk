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
import com.sensorberg.sdk.model.persistence.InternalBeaconAction;
import com.sensorberg.sdk.model.persistence.InternalBeaconScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolverListener;
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.utils.ListUtils;
import com.sensorberg.utils.Objects;

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


    private static final int MSG_PUBLISH_HISTORY = 1;

    private static final int MSG_DELETE_ALL_DATA = 6;

    private Clock clock;

    private final RunLoop runloop;

    private final Transport transport;

    @Setter
    private ResolverListener resolverListener = ResolverListener.NONE;

    private final SettingsManager settingsManager;

    private final SharedPreferences sharedPreferences;

    private final Gson gson;

    private Set<InternalBeaconScan> beaconScans = Collections.synchronizedSet(new HashSet<InternalBeaconScan>());

    private Set<InternalBeaconAction> beaconActions = Collections.synchronizedSet(new HashSet<InternalBeaconAction>());

    public BeaconActionHistoryPublisher(Transport transport, SettingsManager settingsManager, Clock clock,
                                        HandlerManager handlerManager, SharedPreferences sharedPrefs, Gson gson) {
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
        beaconScans.add(InternalBeaconScan.from(scanEvent));
    }

    @Override
    public void handleMessage(Message queueEvent) {
        switch (queueEvent.what) {
            case MSG_PUBLISH_HISTORY:
                publishHistorySynchronously();
                break;
            case MSG_DELETE_ALL_DATA:
                deleteAllData();
                break;
        }
    }

    private void publishHistorySynchronously() {
        final List<InternalBeaconScan> internalScans = notSentBeaconScans();
        final List<InternalBeaconAction> internalActions = notSentBeaconActions();

        final List<BeaconScan> scans = ListUtils.map(internalScans, InternalBeaconScan.TO_BEACON_SCAN);
        final List<BeaconAction> actions = ListUtils.map(internalActions, InternalBeaconAction.TO_BEACON_ACTION);


        if (scans.isEmpty() && actions.isEmpty()) {
            Logger.log.verbose("nothing to report");
            return;
        } else {
            Logger.log.verbose("reporting " + scans.size() + " scans and " + actions.size() + " actions");
        }

        transport.publishHistory(scans, actions, new TransportHistoryCallback() {
            @Override
            public void onSuccess(List<BeaconScan> scanObjectList, List<BeaconAction> actionList) {
                for (InternalBeaconAction internalAction : internalActions) {
                    internalAction.setSentToServerTimestamp(clock.now());
                }
                for (InternalBeaconScan internalScan : internalScans) {
                    internalScan.setSentToServerTimestamp(clock.now());
                }
                removeBeaconScansOlderThan(clock.now(), settingsManager.getCacheTtl());
                removeBeaconActionsOlderThan(clock.now(), settingsManager.getCacheTtl());
                Logger.log.verbose("published " +internalActions.size() + " actions and " + internalScans.size() + " scans to the resolver sucessfully.");
                saveAllData();
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
        beaconActions.add(InternalBeaconAction.from(beaconEvent));
    }

    public void deleteAllObjects() {
        runloop.sendMessage(MSG_DELETE_ALL_DATA);
    }


    public List<InternalBeaconScan> notSentBeaconScans() {
        return ListUtils.filter(beaconScans, new ListUtils.Filter<InternalBeaconScan>() {
            @Override
            public boolean matches(InternalBeaconScan beaconEvent) {
                return beaconEvent.getSentToServerTimestamp() == null;
            }
        });
    }

    private void removeBeaconScansOlderThan(final long timeNow, final long cacheTtl) {
        List<InternalBeaconScan> scansToDelete = ListUtils.filter(beaconScans, new ListUtils.Filter<InternalBeaconScan>() {
            @Override
            public boolean matches(InternalBeaconScan beaconEvent) {
                return beaconEvent.getBeaconScan().getCreatedAt() < (timeNow - cacheTtl)
                        && beaconEvent.getSentToServerTimestamp() != null;
            }
        });
        beaconScans.removeAll(scansToDelete);
    }

    public List<InternalBeaconAction> notSentBeaconActions() {
        return ListUtils.filter(beaconActions, new ListUtils.Filter<InternalBeaconAction>() {
            @Override
            public boolean matches(InternalBeaconAction internalBeaconAction) {
                return internalBeaconAction.getSentToServerTimestamp() == null;
            }
        });
    }

    synchronized private void removeBeaconActionsOlderThan(final long timeNow, final long cacheTtl) {
        List<InternalBeaconAction> actionsToDelete = ListUtils.filter(beaconActions, new ListUtils.Filter<InternalBeaconAction>() {
            @Override
            public boolean matches(InternalBeaconAction beaconEvent) {
                return !beaconEvent.isKeepForever() && (beaconEvent.getCreatedAt() < (timeNow - cacheTtl)
                        && beaconEvent.getSentToServerTimestamp() != null);
            }
        });

        beaconActions.removeAll(actionsToDelete);
    }

    /**
     * List not sent scans.
     *
     * @return - A list of notSentBeaconScans.
     */
    public boolean getCountForSuppressionTime(final long lastAllowedPresentationTime, final UUID actionUUID) {
        List<InternalBeaconAction> actionsToKeep = ListUtils.filter(beaconActions, new ListUtils.Filter<InternalBeaconAction>() {
            @Override
            public boolean matches(InternalBeaconAction beaconEvent) {
                return beaconEvent.getBeaconAction().getTimeOfPresentation() >= lastAllowedPresentationTime
                        && beaconEvent.getBeaconAction().getActionId().equalsIgnoreCase(actionUUID.toString());
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
    private void keepForever(List<InternalBeaconAction> beaconActionSelect) {
        for (InternalBeaconAction internalBeaconAction : beaconActionSelect) {
            internalBeaconAction.setKeepForever(true);
        }
    }

    /**
     * Get the count for only once suppression.
     *
     * @param actionUUID - The beacon action UUID.
     * @return - Select class object.
     */
    public boolean getCountForShowOnlyOnceSuppression(final UUID actionUUID) {
        List<InternalBeaconAction> actionsToKeep = ListUtils.filter(beaconActions, new ListUtils.Filter<InternalBeaconAction>() {
            @Override
            public boolean matches(InternalBeaconAction internalBeaconAction) {
                return internalBeaconAction.getBeaconAction().getActionId().equalsIgnoreCase(actionUUID.toString());
            }
        });

        keepForever(actionsToKeep);
        return actionsToKeep.size() > 0;
    }

    private void loadAllData() {
        String actionJson = sharedPreferences.getString(InternalBeaconAction.SHARED_PREFS_TAG, "");
        if (!actionJson.isEmpty()) {
            Type listType = new TypeToken<Set<InternalBeaconAction>>() {}.getType();
            beaconActions = Collections.synchronizedSet((Set<InternalBeaconAction>) gson.fromJson(actionJson, listType));
        }

        String scanJson = sharedPreferences.getString(InternalBeaconScan.SHARED_PREFS_TAG, "");
        if (!scanJson.isEmpty()) {
            Type listType = new TypeToken<Set<InternalBeaconScan>>() {}.getType();
            beaconScans = Collections.synchronizedSet((Set<InternalBeaconScan>) gson.fromJson(scanJson, listType));
        }
    }

    public void saveAllData() {
        String actionsJson = gson.toJson(beaconActions);
        String scansJson = gson.toJson(beaconScans);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor
                .putString(InternalBeaconAction.SHARED_PREFS_TAG, actionsJson)
                .putString(InternalBeaconScan.SHARED_PREFS_TAG, scansJson)
                .apply();
    }

    private void deleteSavedBeaconScansFromSharedPreferences() {
        if (sharedPreferences.contains(InternalBeaconScan.SHARED_PREFS_TAG)) {
            sharedPreferences.edit().remove(InternalBeaconScan.SHARED_PREFS_TAG).apply();
        }
    }

    private void deleteSavedBeaconActionsFromSharedPreferences() {
        if (sharedPreferences.contains(InternalBeaconAction.SHARED_PREFS_TAG)) {
            sharedPreferences.edit().remove(InternalBeaconAction.SHARED_PREFS_TAG).apply();
        }
    }

    public void deleteAllData() {
        beaconActions = Collections.synchronizedSet(new HashSet<InternalBeaconAction>());
        beaconScans = Collections.synchronizedSet(new HashSet<InternalBeaconScan>());

        deleteSavedBeaconScansFromSharedPreferences();
        deleteSavedBeaconActionsFromSharedPreferences();
    }
}
