package com.sensorberg.sdk.model.sugarorm;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.model.ISO8601TypeAdapter;
import com.sensorberg.sdk.resolver.BeaconEvent;

import java.io.IOException;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by skraynick on 16-03-14.
 */
public class SugarAction {

    public static final String SHARED_PREFS_TAG = "BeaconActions";

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
    public SugarAction() {
    }

    /**
     * Gets SugarAction object.
     *
     * @param beaconEvent - The beacon object.
     * @param clock       - Clock class object.
     * @return - Returns a SugarAction class object.
     */
    public static SugarAction from(BeaconEvent beaconEvent, Clock clock) {
        SugarAction value = new SugarAction();
        value.setActionId(beaconEvent.getAction().getUuid().toString());
        value.setTimeOfPresentation(beaconEvent.getPresentationTime());
        value.setSentToServerTimestamp2(SugarFields.Action.NO_DATE);
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

    public static class SugarActionTypeAdapter extends TypeAdapter<SugarAction> {

        @Override
        public void write(JsonWriter out, SugarAction value) throws IOException {
            out.beginObject();
            out.name("eid").value(value.getActionId());
            out.name("trigger").value(value.getTrigger());
            out.name("pid").value(value.getPid());
            out.name("dt");
            ISO8601TypeAdapter.DATE_ADAPTER.write(out, new Date(value.getTimeOfPresentation()));
            out.endObject();
        }

        @Override
        public SugarAction read(JsonReader in) throws IOException {
            throw new IllegalArgumentException("You must not use this to read a RealmAction");
        }
    }
}
