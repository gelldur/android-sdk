package com.sensorberg.sdk.model.persistence;

import com.google.gson.annotations.Expose;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.utils.ListUtils;

import lombok.Getter;
import lombok.Setter;

public class InternalBeaconAction {

    public static final String SHARED_PREFS_TAG = "com.sensorberg.sdk.InternalBeaconActions";

    public static final ListUtils.Mapper<InternalBeaconAction, BeaconAction> TO_BEACON_ACTION = new ListUtils.Mapper<InternalBeaconAction, BeaconAction>() {
        @Override
        public BeaconAction map(InternalBeaconAction internalBeaconAction) {
            return internalBeaconAction.getBeaconAction();
        }
    };

    @Expose
    @Getter
    @Setter
    private BeaconAction beaconAction;

    @Expose
    @Getter
    @Setter
    private long createdAt;

    @Expose
    @Getter
    @Setter
    private boolean keepForever;

    @Expose
    @Getter
    @Setter
    private Long sentToServerTimestamp;


    public static InternalBeaconAction from(BeaconEvent beaconEvent) {
        InternalBeaconAction value = new InternalBeaconAction();
        value.setCreatedAt(beaconEvent.getResolvedTime());

        if (beaconEvent.sendOnlyOnce || beaconEvent.getSuppressionTimeMillis() > 0) {
            value.setKeepForever(true);
        }

        value.setBeaconAction(BeaconAction.from(beaconEvent));
        value.setSentToServerTimestamp(null);

        return value;
    }


}
