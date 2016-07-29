package com.sensorberg.sdk.model.persistence;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;

import lombok.Getter;
import lombok.Setter;

public class BeaconScan {

    public static final String SHARED_PREFS_TAG = "BeaconScans";

    public static final long NO_DATE = Long.MIN_VALUE;

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
    private long sentToServerTimestamp;

    @Expose
    @Getter
    @Setter
    @SerializedName("dt")
    private long createdAt;

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
        value.setPid(scanEvent.getBeaconId().getPid());
        value.setSentToServerTimestamp(NO_DATE);
        value.setCreatedAt(scanEvent.getEventTime());
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
        return !(pid != null ? !pid.equals(that.pid) : that.pid != null);

    }

    @Override
    public int hashCode() {
        int result = trigger;
        result = 31 * result + (pid != null ? pid.hashCode() : 0);
        result = 31 * result + (int) (createdAt ^ (createdAt >>> 32));
        return result;
    }
}
