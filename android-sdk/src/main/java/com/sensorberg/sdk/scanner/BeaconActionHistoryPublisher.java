package com.sensorberg.sdk.scanner;

import android.content.SharedPreferences;
import android.os.Message;

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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.Setter;

public class BeaconActionHistoryPublisher implements ScannerListener, RunLoop.MessageHandlerCallback {

    public static final String SUPRESSION_TIME_STORE_SHARED_PREFS_TAG = "com.sensorberg.sdk.SupressionTimeStore";

    private static final int MSG_PUBLISH_HISTORY = 1;
    private static final int MSG_DELETE_ALL_DATA = 6;
    private static final int MSG_SAVE_SUPPRESSION_STORE = 7;

    private Clock clock;

    private final RunLoop runloop;

    private final Transport transport;

    @Setter
    private ResolverListener resolverListener = ResolverListener.NONE;

    private final SharedPreferences sharedPreferences;

    private final Gson gson;

    private List<BeaconScan> beaconScans = Collections.synchronizedList(new LinkedList<BeaconScan>());

    private List<BeaconAction> beaconActions = Collections.synchronizedList(new LinkedList<BeaconAction>());

    private HashMap<String, Long> suppressionTimeStore = new HashMap<>();

    public BeaconActionHistoryPublisher(Transport transport, Clock clock,
                                        HandlerManager handlerManager, SharedPreferences sharedPrefs, Gson gson) {
        this.transport = transport;
        this.clock = clock;
        runloop = handlerManager.getBeaconPublisherRunLoop(this);
        sharedPreferences = sharedPrefs;
        this.gson = gson;

        loadAllData();
    }

    @Override
    public void onScanEventDetected(ScanEvent scanEvent) {
        beaconScans.add(BeaconScan.from(scanEvent));
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
            case MSG_SAVE_SUPPRESSION_STORE:
                saveSuppressionTimeStore();
                break;
        }
    }

    private void publishHistorySynchronously() {
        final List<BeaconScan> notSentScans = new LinkedList<>(beaconScans);
        final List<BeaconAction> notSentActions = new LinkedList<>(beaconActions);

        if (notSentScans.isEmpty() && notSentActions.isEmpty()) {
            Logger.log.logBeaconHistoryPublisherState("nothing to report");
            return;
        } else {
            Logger.log.logBeaconHistoryPublisherState("reporting " + notSentScans.size() + " scans and " + notSentActions.size() + " actions");
        }

        transport.publishHistory(notSentScans, notSentActions, new TransportHistoryCallback() {
            @Override
            public void onSuccess(List<BeaconScan> scanObjectList, List<BeaconAction> actionList) {
                beaconActions.removeAll(notSentActions);
                beaconScans.removeAll(notSentScans);
                Logger.log.logBeaconHistoryPublisherState("published " + notSentActions.size() + " campaignStats and " + notSentScans.size() + " beaconStats successfully.");
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
        beaconActions.add(BeaconAction.from(beaconEvent));
    }

    public void deleteAllObjects() {
        runloop.sendMessage(MSG_DELETE_ALL_DATA);
    }


    /**
     * List not sent scans.
     *
     * @return - A list of notSentBeaconScans.
     */
    public boolean actionShouldBeSuppressed(final long lastAllowedPresentationTime, final UUID actionUUID) {
        boolean value = suppressionTimeStore.get(actionUUID.toString()) >= lastAllowedPresentationTime;
        if (!value) {
            suppressionTimeStore.put(actionUUID.toString(), clock.now());
            runloop.add(runloop.obtainMessage(MSG_SAVE_SUPPRESSION_STORE));
        }
        return value;
    }


    /**
     * Get the count for only once suppression.
     *
     * @param actionUUID - The beacon action UUID.
     * @return - Select class object.
     */
    public boolean actionWasShownBefore(final UUID actionUUID) {
        boolean value = suppressionTimeStore.containsKey(actionUUID.toString());
        if (!value){
            suppressionTimeStore.put(actionUUID.toString(), clock.now());
            runloop.add(runloop.obtainMessage(MSG_SAVE_SUPPRESSION_STORE));
        }
        return value;
    }

    private void loadAllData() {
        String actionJson = sharedPreferences.getString(BeaconAction.SHARED_PREFS_TAG, "");
        if (!actionJson.isEmpty()) {
            Type listType = new TypeToken<List<BeaconAction>>() {}.getType();
            beaconActions = Collections.synchronizedList((List<BeaconAction>) gson.fromJson(actionJson, listType));
        }

        String scanJson = sharedPreferences.getString(BeaconScan.SHARED_PREFS_TAG, "");
        if (!scanJson.isEmpty()) {
            Type listType = new TypeToken<List<BeaconScan>>() {}.getType();
            beaconScans = Collections.synchronizedList((List<BeaconScan>) gson.fromJson(scanJson, listType));
        }
        Logger.log.logBeaconHistoryPublisherState("loaded " + beaconActions.size() + " campaignStats and " + beaconScans.size()  +" beaconStats from shared preferences");

        String supressionTimeStoreString = sharedPreferences.getString(SUPRESSION_TIME_STORE_SHARED_PREFS_TAG,"");
        if (!supressionTimeStoreString.isEmpty()){
            Type hashMapType = new TypeToken<HashMap<String, Long>>(){}.getType();
            suppressionTimeStore = gson.fromJson(supressionTimeStoreString, hashMapType);
        }
    }

    public void saveAllData() {
        String actionsJson = gson.toJson(beaconActions);
        String scansJson = gson.toJson(beaconScans);
        String supressionTimeStoreJson = gson.toJson(suppressionTimeStore);
        if(Logger.isVerboseLoggingEnabled()){
            try {
                Logger.log.logBeaconHistoryPublisherState("size of the stats campaignStats:" + actionsJson.getBytes("UTF-8").length + " beaconStats:" + scansJson.getBytes("UTF-8").length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor
                .putString(BeaconAction.SHARED_PREFS_TAG, actionsJson)
                .putString(BeaconScan.SHARED_PREFS_TAG, scansJson)
                .putString(SUPRESSION_TIME_STORE_SHARED_PREFS_TAG, supressionTimeStoreJson)
                .apply();
        Logger.log.logBeaconHistoryPublisherState("saved " + beaconActions.size() + " campaignStats and " + beaconScans.size()  +" beaconStats and " + suppressionTimeStore.size() + " supression related items to shared preferences");
    }

    public void saveSuppressionTimeStore(){
        String supressionTimeStoreJson = gson.toJson(suppressionTimeStore);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor
                .putString(SUPRESSION_TIME_STORE_SHARED_PREFS_TAG, supressionTimeStoreJson)
                .apply();
    }

    private void deleteSavedBeaconScansFromSharedPreferences() {
        if (sharedPreferences.contains(BeaconAction.SHARED_PREFS_TAG)) {
            sharedPreferences.edit().remove(BeaconScan.SHARED_PREFS_TAG).apply();
        }
    }

    private void deleteSavedBeaconActionsFromSharedPreferences() {
        if (sharedPreferences.contains(BeaconAction.SHARED_PREFS_TAG)) {
            sharedPreferences.edit().remove(BeaconScan.SHARED_PREFS_TAG).apply();
        }
    }

    private void deleteSuppressionTimeStore() {
        if (sharedPreferences.contains(SUPRESSION_TIME_STORE_SHARED_PREFS_TAG)) {
            sharedPreferences.edit().remove(SUPRESSION_TIME_STORE_SHARED_PREFS_TAG).apply();
        }
    }

    public void deleteAllData() {
        Logger.log.logBeaconHistoryPublisherState("will purge the saved data of " + beaconActions.size() + " campaignStats and " + beaconScans.size()  +" beaconStats");
        beaconActions = Collections.synchronizedList(new LinkedList<BeaconAction>());
        beaconScans = Collections.synchronizedList(new LinkedList<BeaconScan>());
        suppressionTimeStore = new HashMap<>();

        deleteSuppressionTimeStore();
        deleteSavedBeaconScansFromSharedPreferences();
        deleteSavedBeaconActionsFromSharedPreferences();
    }
}
