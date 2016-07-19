package com.sensorberg.sdk.model.persistence;

import com.google.gson.annotations.Expose;

import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.resolver.BeaconEvent;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by skraynick on 16-03-14.
 */
@EqualsAndHashCode
public class BeaconAction {

    public static final String SHARED_PREFS_TAG = "BeaconActions";

    public static final long NO_DATE = Long.MIN_VALUE;

    @Expose
    @Getter
    @Setter
    private String actionId;

    @Expose
    @Getter
    @Setter
    private long timeOfPresentation;

    @Expose
    @Getter
    @Setter
    private long sentToServerTimestamp2;

    @Expose
    @Getter
    @Setter
    private long createdAt;

    @Expose
    @Getter
    @Setter
    private int trigger;

    @Expose
    @Getter
    @Setter
    private String pid;

    @Expose
    @Getter
    @Setter
    private boolean keepForever;

    /**
     * Default constructor as required by SugarORM.
     */
    public BeaconAction() {
    }

    /**
     * Gets SugarAction object.
     *
     * @param beaconEvent - The beacon object.
     * @param clock       - Clock class object.
     * @return - Returns a SugarAction class object.
     */
    public static BeaconAction from(BeaconEvent beaconEvent, Clock clock) {
        BeaconAction value = new BeaconAction();
        value.setActionId(beaconEvent.getAction().getUuid().toString());
        value.setTimeOfPresentation(beaconEvent.getPresentationTime());
        value.setSentToServerTimestamp2(NO_DATE);
        value.setCreatedAt(clock.now());
        value.setTrigger(beaconEvent.trigger);

        if (beaconEvent.getBeaconId() != null) {
            value.setPid(beaconEvent.getBeaconId().getBid());
        }
        if (beaconEvent.sendOnlyOnce || beaconEvent.getSuppressionTimeMillis() > 0) {
            value.setKeepForever(true);
        }

        return value;
    }
}
