package com.sensorberg.sdk.model.persistence;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
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

    @Getter
    @Setter
    private long sentToServerTimestamp;

    @Getter
    @Setter
    private String proximityUUID;

    @Getter
    @Setter
    private int proximityMajor;

    @Getter
    @Setter
    private int proximityMinor;

    @Getter
    @Setter
    private boolean isEntry;

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
}
