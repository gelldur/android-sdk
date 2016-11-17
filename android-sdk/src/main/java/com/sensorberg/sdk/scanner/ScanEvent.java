package com.sensorberg.sdk.scanner;

import android.os.Parcel;
import android.os.Parcelable;

import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.utils.Objects;

import lombok.Getter;
import lombok.ToString;

/**
 * Class {@link ScanEvent} represents an event.
 */
@ToString
public class ScanEvent implements Parcelable {

    /**
     * {@link android.os.Parcelable.Creator} for the {@link android.os.Parcelable} interface
     */
    public static final Creator<ScanEvent> CREATOR = new Creator<ScanEvent>() {
        public ScanEvent createFromParcel(Parcel in) {
            return (new ScanEvent(in));
        }

        public ScanEvent[] newArray(int size) {
            return (new ScanEvent[size]);
        }
    };

    /**
     * -- GETTER --
     * Returns the hardware address of this BluetoothDevice.
     * <p> For example, "00:11:22:AA:BB:CC".
     *
     * @return Bluetooth hardware address as string
     */
    @Getter private String hardwareAdress;

    /**
     * -- GETTER --
     * Get the initial RSSI of this event.
     *
     * @return the received signal strength in db
     */
    @Getter private int initialRssi;

    /**
     * -- GETTER --
     * The provided rssi provided by the beacon. Corresponds to the rssi of an iPhone 5S in 1 meter distance.
     * This value can be used for disatance calculations
     *
     * @return rssi in db
     */
    @Getter private int calRssi;

    /**
     * -- GETTER --
     * Returns the {@link BeaconId} of the {@link ScanEvent}.
     *
     * @return the {@link BeaconId} of the {@link ScanEvent}
     */
    @Getter private final BeaconId beaconId;

    /**
     * -- GETTER --
     * Returns the event time in milliseconds of the {@link ScanEvent}.
     *
     * @return the event time in milliseconds of the {@link ScanEvent}
     */
    @Getter private final long eventTime;

    private final boolean entry;

    /**
     * -- GETTER --
     * Returns the geohash for this {@link ScanEvent}.
     *
     * @return Geohash string or null if there is none.
     */
    @Getter private final String geohash;

    protected ScanEvent(BeaconId beaconId, long eventTime, boolean entry, String geohash) {
        this.beaconId = beaconId;
        this.eventTime = eventTime;
        this.entry = entry;
        this.geohash = geohash;
    }

    private ScanEvent(Parcel source) {
        this.beaconId = source.readParcelable(BeaconId.class.getClassLoader());
        this.eventTime = source.readLong();
        this.entry = source.readInt() == 1;
        this.hardwareAdress = source.readString();
        this.initialRssi = source.readInt();
        this.calRssi = source.readInt();
        this.geohash = source.readString();
    }

    public ScanEvent(BeaconId beaconId, long now, boolean entry, String address, int rssi, int calRssi, String geohash) {
        this(beaconId, now, entry, geohash);
        this.hardwareAdress = address;
        this.initialRssi = rssi;
        this.calRssi = calRssi;
    }

    public int getTrigger() {
        return isEntry() ? ScanEventType.ENTRY.getMask() : ScanEventType.EXIT.getMask();
    }

    public int describeContents() {
        return (0);
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeParcelable(beaconId, flags);
        destination.writeLong(eventTime);
        destination.writeInt(entry ? 1 : 0);
        destination.writeString(hardwareAdress);
        destination.writeInt(initialRssi);
        destination.writeInt(calRssi);
        destination.writeString(geohash);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return (true);
        }
        if (object == null) {
            return (false);
        }
        if (((Object) this).getClass() != object.getClass()) {
            return (false);
        }
        ScanEvent other = (ScanEvent) object;
        return Objects.equals(beaconId, other.beaconId) && entry == other.entry && Objects.equals(geohash, other.geohash);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((beaconId == null) ? 0 : beaconId.hashCode());
        result = prime * result + (entry ? 1 : 0);
        result = prime * result + (geohash == null ? 0 : geohash.hashCode());
        return (result);
    }


    /**
     * Returns the event type: entry or not entry i.e.exit.
     *
     * @return the entry boolean
     */
    public boolean isEntry() {
        return (entry);
    }

    public static class Builder {

        private BeaconId beaconId;

        private long eventTime;

        private boolean entry;

        private String geohash;

        public Builder() {
        }

        public Builder withBeaconId(BeaconId beaconId) {
            this.beaconId = beaconId;
            return this;
        }

        public Builder withEventTime(long eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public Builder withEntry(boolean entry) {
            this.entry = entry;
            return this;
        }

        public Builder withGeohash(String geohash) {
            this.geohash = geohash;
            return this;
        }

        public ScanEvent build() {
            return new ScanEvent(beaconId, eventTime, entry, geohash);
        }
    }

}
