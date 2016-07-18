package com.sensorberg.sdk.model.sugarorm;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import com.sensorberg.sdk.model.ISO8601TypeAdapter;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;

import java.io.IOException;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * @author skraynick
 * @version 16-03-14
 */
public class SugarScan {

    public static final String SHARED_PREFS_TAG = "BeaconScans";

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
    public SugarScan() { //TODO do we need it?
    }

    public int getTrigger() {
        return isEntry() ? ScanEventType.ENTRY.getMask() : ScanEventType.EXIT.getMask();
    }

    public String getPid() {
        return this.getProximityUUID().replace("-", "") + String.format("%1$05d%2$05d", this.getProximityMajor(), this.getProximityMinor());
    }

    //Functionality.

    /**
     * Creates a SugarScan Object.
     *
     * @param scanEvent - Sugar Event object.
     * @param timeNow   -  the time now.
     * @return - Returns a sugar scan object.
     */
    public static SugarScan from(ScanEvent scanEvent, long timeNow) {
        SugarScan value = new SugarScan();
        value.setEventTime(scanEvent.getEventTime());
        value.setEntry(scanEvent.getEventMask() == ScanEventType.ENTRY.getMask());
        value.setProximityUUID(scanEvent.getBeaconId().getUuid().toString());
        value.setProximityMajor(scanEvent.getBeaconId().getMajorId());
        value.setProximityMinor(scanEvent.getBeaconId().getMinorId());
        value.setSentToServerTimestamp2(SugarFields.Scan.NO_DATE);
        value.setCreatedAt(timeNow);
        return value;
    }

    /**
     * Sugar scan object type adapter.
     */
    public static class SugarScanObjectTypeAdapter extends TypeAdapter<SugarScan> {

        @Override
        public void write(JsonWriter out, SugarScan value) throws IOException {
            out.beginObject();
            out.name("pid").value(value.getPid());
            out.name("trigger").value(value.getTrigger());
            out.name("dt");
            ISO8601TypeAdapter.DATE_ADAPTER.write(out, new Date(value.getEventTime()));
            out.endObject();
        }

        @Override
        public SugarScan read(JsonReader in) throws IOException {
            throw new IllegalArgumentException("You must not use this to read a SugarScanObject");
        }
    }

}
