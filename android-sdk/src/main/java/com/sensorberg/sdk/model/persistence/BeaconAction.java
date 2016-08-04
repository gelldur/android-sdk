package com.sensorberg.sdk.model.persistence;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.resolver.BeaconEvent;

import lombok.Getter;
import lombok.Setter;

public class BeaconAction {

    public static final String SHARED_PREFS_TAG = "com.sensorberg.sdk.InternalBeaconActions";

    @Expose
    @Getter
    @Setter
    @SerializedName("eid")
    private String actionId;

    @Expose
    @Getter
    @Setter
    @SerializedName("dt")
    private long timeOfPresentation;

    @Expose
    @Getter
    @Setter
    @SerializedName("trigger")
    private int trigger;

    @Expose
    @Getter
    @Setter
    @SerializedName("pid")
    private String pid;


    public BeaconAction() {
    }

    /**
     * Gets BeaconAction object.
     *
     * @param beaconEvent - The beacon object.
     * @return - Returns a BeaconAction class object.
     */
    public static BeaconAction from(BeaconEvent beaconEvent) {
        BeaconAction value = new BeaconAction();
        value.setActionId(beaconEvent.action.getUuid().toString());
        value.setTimeOfPresentation(beaconEvent.getPresentationTime());
        value.setTrigger(beaconEvent.getTrigger());

        if (beaconEvent.getBeaconId() != null) {
            value.setPid(beaconEvent.getBeaconId().getPid());
        }

        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BeaconAction that = (BeaconAction) o;

        if (timeOfPresentation != that.timeOfPresentation) {
            return false;
        }
        if (trigger != that.trigger) {
            return false;
        }
        if (!actionId.equals(that.actionId)) {
            return false;
        }
        return !(pid != null ? !pid.equals(that.pid) : that.pid != null);

    }

    @Override
    public int hashCode() {
        int result = actionId.hashCode();
        result = 31 * result + (int) (timeOfPresentation ^ (timeOfPresentation >>> 32));
        result = 31 * result + trigger;
        result = 31 * result + (pid != null ? pid.hashCode() : 0);
        return result;
    }
}
