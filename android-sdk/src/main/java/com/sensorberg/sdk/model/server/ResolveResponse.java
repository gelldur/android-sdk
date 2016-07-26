package com.sensorberg.sdk.model.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.utils.ListUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.ToString;

@ToString
public class ResolveResponse extends BaseResolveResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Expose
    private List<ResolveAction> actions = Collections.emptyList();

    @Expose
    private List<ResolveAction> instantActions = Collections.emptyList();

    @Expose
    @SerializedName("reportTrigger")
    public Long reportTriggerSeconds;

    public List<ResolveAction> resolve(ScanEvent scanEvent, long now) {
        ArrayList<ResolveAction> beaconEvents = new ArrayList<>();
        beaconEvents.addAll(getActionsFromLayout(scanEvent, now));
        beaconEvents.addAll(getInstantActions());
        return beaconEvents;
    }

    /**
     * used internally to create all the @{ResolveAction} from the @{actions} array
     *
     * @param scanEvent used to match
     * @return all matching BeaconEvents
     */
    private List<ResolveAction> getActionsFromLayout(final ScanEvent scanEvent, final long now) {
        if (actions == null) {
            return Collections.emptyList();
        }
        return ListUtils.filter(actions, new ListUtils.Filter<ResolveAction>() {
            @Override
            public boolean matches(ResolveAction resolveAction) {
                boolean matchTrigger = resolveAction.matchTrigger(scanEvent.getTrigger());
                if (matchTrigger) {
                    boolean matchBeacon = resolveAction.containsBeacon(scanEvent.getBeaconId());
                    if (matchBeacon) {
                        return resolveAction.isValidNow(now);
                    }
                }
                return false;
            }
        });
    }

    /**
     * @return all instantActions based on the @{instantAction} ResolveActionsArray.
     */
    public List<ResolveAction> getInstantActions() {
        if (instantActions == null) {
            return Collections.emptyList();
        }
        return ListUtils.filter(instantActions, new ListUtils.Filter<ResolveAction>() {
            @Override
            public boolean matches(ResolveAction object) {
                return true;
            }
        });
    }

    public List<BeaconEvent> getInstantActionsAsBeaconEvent() {
        return ListUtils.map(getInstantActions(), ResolveAction.BEACON_EVENT_MAPPER);
    }


    private ResolveResponse(List<String> accountProximityUUIDs, List<ResolveAction> actions, List<ResolveAction> instantActions,
            Long reportTriggerSeconds) {
        super(accountProximityUUIDs);
        this.actions = actions;
        this.instantActions = instantActions;
        this.reportTriggerSeconds = reportTriggerSeconds;
    }

    public static class Builder {

        private List<String> accountProximityUUIDs = Collections.emptyList();

        private List<ResolveAction> actions = Collections.emptyList();

        private List<ResolveAction> instantActions = Collections.emptyList();

        private Long reportTrigger = 0L;

        public Builder() {
        }

        public Builder withAccountProximityUUIDs(List<String> accountProximityUUIDs) {
            this.accountProximityUUIDs = accountProximityUUIDs;
            return this;
        }

        public Builder withActions(List<ResolveAction> actions) {
            this.actions = actions;
            return this;
        }

        public Builder withInstantActions(List<ResolveAction> instantActions) {
            this.instantActions = instantActions;
            return this;
        }

        public Builder withReportTrigger(long reportTrigger) {
            this.reportTrigger = reportTrigger;
            return this;
        }

        public ResolveResponse build() {
            return new ResolveResponse(accountProximityUUIDs, actions, instantActions, reportTrigger);
        }
    }
}
