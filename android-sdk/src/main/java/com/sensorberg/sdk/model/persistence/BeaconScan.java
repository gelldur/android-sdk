package com.sensorberg.sdk.model.persistence;

import com.google.gson.annotations.Expose;

import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author skraynick
 * @version 16-03-14
 */
@EqualsAndHashCode
public class BeaconScan {

    public static final String SHARED_PREFS_TAG = "BeaconScans";

    public static final long NO_DATE = Long.MIN_VALUE;

    @Expose
    @Getter
    @Setter
    private long eventTime;

    @Expose
    @Getter
    @Setter
    private boolean isEntry;

    @Expose
    @Getter
    @Setter
    private String proximityUUID;

    @Expose
    @Getter
    @Setter
    private int proximityMajor;

    @Expose
    @Getter
    @Setter
    private int proximityMinor;

    @Expose
    @Getter
    @Setter
    private long sentToServerTimestamp2;

    @Expose
    @Getter
    @Setter
    private long createdAt;

    /**
     * Default constructor as required by SugarORM.
     */
    public BeaconScan() { //TODO do we need it?
    }

    public int getTrigger() {
        return isEntry() ? ScanEventType.ENTRY.getMask() : ScanEventType.EXIT.getMask();
    }

    public String getPid() {
        return this.getProximityUUID().replace("-", "") + String.format("%1$05d%2$05d", this.getProximityMajor(), this.getProximityMinor());
    }

    /**
     * Creates a SugarScan Object.

     *
     * @param scanEvent - Sugar Event object.
     * @param timeNow   -  the time now.
     * @return - Returns a sugar scan object.
     */
    public static BeaconScan from(ScanEvent scanEvent, long timeNow) {
        BeaconScan value = new BeaconScan();
        value.setEventTime(scanEvent.getEventTime());
        value.setEntry(scanEvent.getEventMask() == ScanEventType.ENTRY.getMask());
        value.setProximityUUID(scanEvent.getBeaconId().getUuid().toString());
        value.setProximityMajor(scanEvent.getBeaconId().getMajorId());
        value.setProximityMinor(scanEvent.getBeaconId().getMinorId());
        value.setSentToServerTimestamp2(NO_DATE);
        value.setCreatedAt(timeNow);
        return value;
    }
}
