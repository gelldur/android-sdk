package com.sensorberg.sdk.resolver;

import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.utils.Objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import lombok.ToString;

/**
 * Class {@link BeaconEvent} represents a {@link ScanEvent} that has been resolved by the sensorberg backend.
 */
@ToString
public class BeaconEvent implements Parcelable {

    /**
     * {@link android.os.Parcelable.Creator} for the {@link android.os.Parcelable} interface
     */
    @SuppressWarnings("hiding")
    public static final Creator<BeaconEvent> CREATOR = new Creator<BeaconEvent>() {
        public BeaconEvent createFromParcel(Parcel in) {
            return (new BeaconEvent(in));
        }

        public BeaconEvent[] newArray(int size) {
            return (new BeaconEvent[size]);
        }
    };

    private final Action action;

    private long resolvedTime;

    /**
     * time when the action is beeing actually presented, not used neccesary to be added to the @{Parcel}
     */
    private long presentationTime;

    private final long suppressionTimeMillis;

    public final boolean sendOnlyOnce;

    public final Date deliverAt;

    public int trigger;

    private BeaconId beaconId;

    private BeaconEvent(Action action, long resolvedTime, long presentationTime, long suppressionTime, boolean sendOnlyOnce, Date deliverAt,
            int trigger, BeaconId beaconId) {
        this.action = action;
        this.resolvedTime = resolvedTime;
        this.presentationTime = presentationTime;
        this.suppressionTimeMillis = suppressionTime;
        this.sendOnlyOnce = sendOnlyOnce;
        this.deliverAt = deliverAt;
        this.trigger = trigger;
        this.beaconId = beaconId;
    }

    private BeaconEvent(Parcel source) {
        action = source.readParcelable(Action.class.getClassLoader());
        resolvedTime = source.readLong();
        suppressionTimeMillis = source.readLong();
        sendOnlyOnce = source.readInt() == 1;
        boolean hasDeliverAt = source.readInt() == 1;
        if (hasDeliverAt) {
            deliverAt = new Date(source.readLong());
        } else {
            deliverAt = null;
        }
        trigger = source.readInt();
        beaconId = source.readParcelable(BeaconId.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return (0);
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeParcelable(action, flags);
        destination.writeLong(resolvedTime);
        destination.writeLong(suppressionTimeMillis);
        destination.writeInt(sendOnlyOnce ? 1 : 0);
        if (deliverAt != null) {
            destination.writeInt(1);
            destination.writeLong(deliverAt.getTime());
        } else {
            destination.writeInt(0);
        }
        destination.writeInt(trigger);
        destination.writeParcelable(beaconId, flags);
    }

    /**
     * Returns the {@link Action} to be triggered by the {@link BeaconEvent}.
     *
     * @return the {@link Action} to be triggered by the {@link BeaconEvent}
     */
    public Action getAction() {
        return (action);
    }

    /**
     * Returns the time the {@link BeaconEvent} was resolved.
     *
     * @return the time the {@link BeaconEvent} was resolved
     */
    public long getResolvedTime() {
        return (resolvedTime);
    }

    public void setResolvedTime(long resolvedTime) {
        this.resolvedTime = resolvedTime;
    }

    public long getPresentationTime() {
        return presentationTime;
    }

    public void setPresentationTime(long presentationTime) {
        this.presentationTime = presentationTime;
    }

    public long getSuppressionTimeMillis() {
        return suppressionTimeMillis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BeaconEvent that = (BeaconEvent) o;

        return Objects.equals(action, that.action);

    }

    @Override
    public int hashCode() {
        return action != null ? action.hashCode() : 0;
    }

    public void setBeaconId(BeaconId beaconId) {
        this.beaconId = beaconId;
    }

    public BeaconId getBeaconId() {
        return beaconId;
    }

    public void setTrigger(int trigger) {
        this.trigger = trigger;
    }

    @ToString
    public static class Builder {

        private Action action;

        private long resolvedTime;

        private long presentationTime;

        private long suppressionTime;

        private boolean sendOnlyOnce;

        private Date deliverAt;

        private int trigger;

        private BeaconId beaconId;

        public Builder() {
        }

        public Builder withAction(Action action) {
            this.action = action;
            return this;
        }

        public Builder withResolvedTime(long resolvedTime) {
            this.resolvedTime = resolvedTime;
            return this;
        }

        public Builder withSuppressionTime(long suppressionTime) {
            this.suppressionTime = suppressionTime;
            return this;
        }

        public Builder withPresentationTime(long presentationTime) {
            this.presentationTime = presentationTime;
            return this;
        }

        public Builder withBeaconId(BeaconId beaconId) {
            this.beaconId = beaconId;
            return this;
        }

        public Builder withSendOnlyOnce(boolean sentOnlyOnce) {
            this.sendOnlyOnce = sentOnlyOnce;
            return this;
        }

        public Builder withDeliverAtDate(Date deliverAt) {
            if (deliverAt != null) {
                this.sendOnlyOnce = true;
                this.deliverAt = deliverAt;
                this.suppressionTime = 0;
            }
            return this;
        }

        public Builder withTrigger(int trigger) {
            this.trigger = trigger;
            return this;
        }

        public BeaconEvent build() {
            return new BeaconEvent(action, resolvedTime, presentationTime, suppressionTime, sendOnlyOnce, deliverAt, trigger, beaconId);
        }
    }
}
