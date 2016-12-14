package com.sensorberg.sdk.model.persistence;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.utils.Objects;

import lombok.Getter;
import lombok.Setter;

public class BeaconScan {

    public static final String SHARED_PREFS_TAG = "com.sensorberg.sdk.BeaconScan";

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

    @Expose
    @Getter
    @Setter
    @SerializedName("dt")
    private long createdAt;

    @Expose
    @Getter
    @Setter
    @SerializedName("location")
    private String geohash;

    public BeaconScan() {
    }

    /**
     * Creates a BeaconScan Object.
     *
     * @param scanEvent - ScanEvent object.
     * @return - Returns a BeaconScan object.
     */
    public static BeaconScan from(ScanEvent scanEvent) {
        BeaconScan value = new BeaconScan();
        value.setTrigger(scanEvent.isEntry() ? ScanEventType.ENTRY.getMask() : ScanEventType.EXIT.getMask());
        if (scanEvent.getBeaconId().getGeofenceId() == null) {
            value.setPid(scanEvent.getBeaconId().getPid());
        } else {
            value.setPid(scanEvent.getBeaconId().getGeofenceId());
        }
        value.setCreatedAt(scanEvent.getEventTime());
        value.setGeohash(scanEvent.getGeohash());
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

        BeaconScan that = (BeaconScan) o;

        if (trigger != that.trigger) {
            return false;
        }
        if (createdAt != that.createdAt) {
            return false;
        }
        if (!Objects.equals(geohash, that.geohash)) {
            return false;
        }
        return !(pid != null ? !pid.equals(that.pid) : that.pid != null);

    }

    @Override
    public int hashCode() {
        int result = trigger;
        result = 31 * result + (pid != null ? pid.hashCode() : 0);
        result = 31 * result + (int) (createdAt ^ (createdAt >>> 32));
        result = 31 * result + (geohash != null ? geohash.hashCode() : 0);
        return result;
    }
}
