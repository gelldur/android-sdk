package com.sensorberg.sdk.resolver;

import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.model.server.ResolveAction;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.utils.Objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
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

    @Getter
    private final Action action;

    @Getter
    private final long suppressionTimeMillis;

    @Getter
    private final boolean sendOnlyOnce;

    @Getter
    private final boolean reportImmediately;

    @Getter
    private final Date deliverAt;

    @Getter
    @Setter
    private long resolvedTime;

    @Getter
    @Setter
    private int trigger;

    @Getter
    @Setter
    private BeaconId beaconId;

    @Getter
    @Setter
    private long presentationTime;

    @Getter
    @Setter
    private String geohash;

    private BeaconEvent(Action action, long resolvedTime, long presentationTime, long suppressionTime, boolean sendOnlyOnce, Date deliverAt,
                        int trigger, BeaconId beaconId, boolean reportImmediately) {
        this.action = action;
        this.resolvedTime = resolvedTime;
        this.presentationTime = presentationTime;
        this.suppressionTimeMillis = suppressionTime;
        this.sendOnlyOnce = sendOnlyOnce;
        this.deliverAt = deliverAt;
        this.trigger = trigger;
        this.beaconId = beaconId;
        this.reportImmediately = reportImmediately;
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
        reportImmediately = source.readInt() == 1;
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
        destination.writeInt(reportImmediately ? 1 : 0);
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
        private boolean reportImmediately;

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

        public Builder withReportImmediately(boolean reportImmediately) {
            this.reportImmediately = reportImmediately;
            return this;
        }

        public BeaconEvent build() {
            return new BeaconEvent(action, resolvedTime, presentationTime, suppressionTime, sendOnlyOnce, deliverAt, trigger, beaconId, reportImmediately);
        }
    }
}
